#!/usr/bin/env python3
"""
TDA Universal Launcher & Artifact Bootstrapper

Autonomous launcher for the Thread Dump Analyzer (TDA) MCP server.
Enables AI coding agents (Claude Code, OpenAI Codex, JetBrains Junie, Cursor/Windsurf)
to invoke TDA diagnostics without manual build or classpath configuration.

Key capabilities:
1. Automated zero-dependency bootstrapping: Fetches pre-compiled shaded release
   jar from GitHub Releases if not locally present.
2. Graceful offline/air-gapped fallback: Searches local cache, workspace build
   directories, and explicit environment variables.
3. Java runtime verification: Validates Java 11+ presence with actionable diagnostics.
4. Clean stdio protocol preservation: Dispatches directly to JVM process via execv
   (or subprocess on Windows) ensuring all stdout is pure JSON-RPC.
"""

import argparse
import os
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.request
from pathlib import Path
from typing import List, Optional, Tuple

GITHUB_REPO = "irockel/tda"
MIN_JAVA_VERSION = 11
FALLBACK_RELEASE_TAG = "3.2"
FALLBACK_DOWNLOAD_URL = f"https://github.com/{GITHUB_REPO}/releases/download/{FALLBACK_RELEASE_TAG}/tda-{FALLBACK_RELEASE_TAG}.jar"
CACHE_DIR = Path.home() / ".cache" / "tda"
ALT_CACHE_DIR = Path.home() / ".tda" / "bin"


def log_err(msg: str) -> None:
    """Print log message to stderr to preserve stdout for JSON-RPC."""
    print(f"[TDA Launcher] {msg}", file=sys.stderr, flush=True)


def verify_java_runtime() -> str:
    """
    Ensure a working Java executable is available and meets the minimum version.
    Returns the absolute path to the java binary.
    """
    java_bin = shutil.which("java")
    if not java_bin:
        log_err("Error: Java runtime ('java') was not found in PATH.")
        log_err("TDA requires Java 11 or higher to run.")
        log_err("Please install a compatible JDK/JRE (e.g. OpenJDK 11, 17, or 21) and ensure 'java' is on PATH.")
        sys.exit(1)

    try:
        proc = subprocess.run(
            [java_bin, "-version"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            timeout=10,
        )
        version_output = proc.stderr or proc.stdout
        major_version = parse_java_version(version_output)
        if major_version is not None and major_version < MIN_JAVA_VERSION:
            log_err(f"Error: Detected Java version {major_version}, but TDA requires Java {MIN_JAVA_VERSION} or higher.")
            log_err("Please upgrade your Java installation or point PATH to a Java 11+ runtime.")
            sys.exit(1)
    except Exception as e:
        log_err(f"Warning: Could not verify Java version: {e}. Proceeding with detected binary.")

    return java_bin


def parse_java_version(version_str: str) -> Optional[int]:
    """Extract the major version number from java -version output."""
    match = re.search(r'version "(?:1\.)?(\d+)', version_str)
    if match:
        try:
            return int(match.group(1))
        except ValueError:
            pass
    return None


def search_local_jar(explicit_path: Optional[str] = None) -> Optional[Path]:
    """Search for existing tda.jar in local filesystem locations."""
    candidates = []

    if explicit_path:
        candidates.append(Path(explicit_path).expanduser().resolve())

    env_path = os.environ.get("TDA_JAR_PATH")
    if env_path:
        candidates.append(Path(env_path).expanduser().resolve())

    cwd = Path.cwd()
    script_dir = Path(__file__).parent.resolve()
    repo_root = script_dir.parent

    search_dirs = [
        cwd / "tda" / "target",
        cwd / "target",
        cwd,
        repo_root / "tda" / "target",
        repo_root / "target",
        repo_root,
        CACHE_DIR,
        ALT_CACHE_DIR,
        Path.home() / ".tda",
    ]

    for d in search_dirs:
        if d.is_dir():
            for f in sorted(d.glob("tda*.jar"), reverse=True):
                if not f.name.startswith("original-") and f.is_file() and f.stat().st_size > 10000:
                    candidates.append(f.resolve())

    for c in candidates:
        if c.is_file() and c.stat().st_size > 10000:
            return c

    return None


def fetch_release_jar_url() -> Tuple[str, str]:
    """
    Query the GitHub API for the latest release asset url and filename.
    Falls back to the known release URL on rate-limiting or network issues.
    """
    api_url = f"https://api.github.com/repos/{GITHUB_REPO}/releases/latest"
    headers = {
        "User-Agent": "tda-universal-launcher",
        "Accept": "application/vnd.github.v3+json",
    }
    req = urllib.request.Request(api_url, headers=headers)

    try:
        with urllib.request.urlopen(req, timeout=8) as response:
            import json
            data = json.loads(response.read().decode())
            assets = data.get("assets", [])
            for asset in assets:
                name = asset.get("name", "")
                download_url = asset.get("browser_download_url", "")
                if name.startswith("tda") and name.endswith(".jar") and not name.startswith("original-"):
                    return download_url, name
    except Exception as e:
        log_err(f"GitHub API lookup notice: {e}. Using direct release fallback URL.")

    fallback_name = f"tda-{FALLBACK_RELEASE_TAG}.jar"
    return FALLBACK_DOWNLOAD_URL, fallback_name


def bootstrap_download_jar() -> Path:
    """
    Download the pre-compiled tda.jar release from GitHub Releases.
    Saves the file to ~/.cache/tda/ or fallback cache.
    """
    target_dir = CACHE_DIR
    try:
        target_dir.mkdir(parents=True, exist_ok=True)
    except OSError:
        target_dir = ALT_CACHE_DIR
        try:
            target_dir.mkdir(parents=True, exist_ok=True)
        except OSError:
            target_dir = Path(tempfile.gettempdir()) / "tda"
            target_dir.mkdir(parents=True, exist_ok=True)

    download_url, file_name = fetch_release_jar_url()
    dest_path = target_dir / file_name
    canonical_symlink = target_dir / "tda.jar"

    log_err(f"No local tda.jar found. Downloading {file_name} from GitHub Releases...")
    log_err(f"Source: {download_url}")
    log_err(f"Destination: {dest_path}")

    temp_path = dest_path.with_suffix(".tmp")
    headers = {
        "User-Agent": "tda-universal-launcher",
    }
    req = urllib.request.Request(download_url, headers=headers)

    try:
        with urllib.request.urlopen(req, timeout=60) as resp, open(temp_path, "wb") as out:
            shutil.copyfileobj(resp, out)

        # Verify jar header (ZIP magic bytes PK\x03\x04) and non-trivial size
        if temp_path.stat().st_size < 10000:
            raise ValueError("Downloaded file is too small to be a valid jar.")

        with open(temp_path, "rb") as check_f:
            magic = check_f.read(4)
            if magic != b"PK\x03\x04":
                raise ValueError("Downloaded file lacks valid JAR/ZIP magic bytes.")

        os.replace(temp_path, dest_path)

        # Create or update tda.jar convenience pointer
        try:
            if canonical_symlink.is_symlink() or canonical_symlink.is_file():
                canonical_symlink.unlink(missing_ok=True)
            canonical_symlink.symlink_to(dest_path.name)
        except OSError:
            pass

        log_err(f"Successfully downloaded and verified {dest_path.name} ({dest_path.stat().st_size // 1024} KB).")
        return dest_path

    except Exception as e:
        if temp_path.exists():
            temp_path.unlink(missing_ok=True)
        raise RuntimeError(f"Failed to download release jar: {e}") from e


def resolve_jar(explicit_path: Optional[str] = None) -> Path:
    """Find local jar or bootstrap via download."""
    found = search_local_jar(explicit_path)
    if found:
        return found

    try:
        return bootstrap_download_jar()
    except Exception as e:
        log_err(f"Error: {e}")
        log_err("Could not find local tda.jar and automated download failed.")
        log_err("In offline or network air-gapped environments, please supply tda.jar via:")
        log_err("  1. Setting TDA_JAR_PATH environment variable (e.g. export TDA_JAR_PATH=/path/to/tda.jar)")
        log_err("  2. Placing tda.jar into ~/.cache/tda/ or ~/.tda/bin/")
        log_err("  3. Compiling the project locally: mvn package (in repository root)")
        sys.exit(1)


def launch_mcp(jar_path: Path, extra_args: List[str]) -> None:
    """Launch the TDA MCP server with stdio piping."""
    java_bin = verify_java_runtime()

    # Base JVM options
    env_opts = os.environ.get("JAVA_OPTS", "")
    jvm_opts = env_opts.split() if env_opts else []

    # Ensure headless mode is set
    if not any("java.awt.headless" in opt for opt in jvm_opts):
        jvm_opts.append("-Djava.awt.headless=true")

    cmd = [java_bin] + jvm_opts + ["-jar", str(jar_path), "--mcp"] + extra_args

    # Flush stdio before handing over
    sys.stdout.flush()
    sys.stderr.flush()

    if os.name == "posix":
        try:
            os.execv(java_bin, cmd)
        except OSError as e:
            log_err(f"os.execv failed ({e}), falling back to subprocess execution.")

    # Fallback for Windows or if execv fails
    proc = subprocess.run(cmd)
    sys.exit(proc.returncode)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="TDA Universal Launcher & MCP Bootstrapper",
        add_help=False,
    )
    parser.add_argument("--jar", dest="jar_path", help="Explicit path to tda.jar")
    parser.add_argument("--check", action="store_true", help="Verify Java and tda.jar without launching MCP")
    parser.add_argument("--download-only", action="store_true", help="Download tda.jar if missing, then exit")
    parser.add_argument("-h", "--help", action="store_true", help="Show this help message")

    # Split known arguments from arbitrary MCP parameters
    known_args, forwarded_args = parser.parse_known_args()

    if known_args.help:
        parser.print_help(file=sys.stderr)
        log_err("\nWhen invoked without inspection flags, the launcher verifies Java,")
        log_err("resolves or bootstraps tda.jar, and starts the headless MCP server over stdio.")
        sys.exit(0)

    if known_args.check:
        java_bin = verify_java_runtime()
        jar = resolve_jar(known_args.jar_path)
        log_err(f"Check OK: Java found at {java_bin}")
        log_err(f"Check OK: TDA jar resolved at {jar}")
        sys.exit(0)

    if known_args.download_only:
        jar = resolve_jar(known_args.jar_path)
        log_err(f"Download check complete. Jar available at {jar}")
        sys.exit(0)

    jar = resolve_jar(known_args.jar_path)
    launch_mcp(jar, forwarded_args)


if __name__ == "__main__":
    main()
