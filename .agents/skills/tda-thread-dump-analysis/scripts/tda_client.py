#!/usr/bin/env python3
"""
TDA CLI Bridge - Standalone Command-Line Client for Thread Dump Analyzer (TDA)

Allows AI agents without native MCP support (such as Pi Agent, OpenClaw, or bash agents)
to perform headless analysis of Java thread dumps via standard terminal commands.
Communicates with `tda.jar --mcp` over stdio JSON-RPC 2.0.

Zero external dependencies - standard library only (Python 3.8+).
"""

import argparse
import glob
import json
import os
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple, Union

DEFAULT_TIMEOUT = 60  # seconds

SESSION_FILE = Path(tempfile.gettempdir()) / f"tda_session_{os.getuid() if hasattr(os, 'getuid') else 'user'}.json"


def find_tda_jar(explicit_path: Optional[str] = None) -> str:
    """Resolve the location of tda.jar from arguments, env var, or search paths."""
    candidates = []

    if explicit_path:
        candidates.append(Path(explicit_path).expanduser().resolve())

    env_path = os.environ.get("TDA_JAR_PATH")
    if env_path:
        candidates.append(Path(env_path).expanduser().resolve())

    cwd = Path.cwd()
    script_dir = Path(__file__).parent.resolve()
    repo_root = script_dir.parent.parent.parent

    # Candidate patterns
    search_dirs = [cwd, cwd / "tda" / "target", cwd / "target", repo_root / "tda" / "target", repo_root / "target", Path.home() / ".tda"]

    for d in search_dirs:
        if d.is_dir():
            # Check for shaded jar first (tda-*.jar excluding original-*.jar)
            for f in sorted(d.glob("tda*.jar"), reverse=True):
                if not f.name.startswith("original-"):
                    candidates.append(f.resolve())

    for c in candidates:
        if c.is_file():
            return str(c)

    raise FileNotFoundError(
        "Could not find tda.jar. Please specify the path using --jar <path> or "
        "set the TDA_JAR_PATH environment variable, or run 'mvn package' in the TDA repository."
    )


def verify_java_runtime() -> str:
    """Ensure a working java executable is available in PATH."""
    java_bin = shutil.which("java")
    if not java_bin:
        raise RuntimeError("Java runtime ('java') not found in PATH. Please install Java 11 or higher.")
    return java_bin


class TDAMCPClient:
    """Manages the background TDA MCP server process and JSON-RPC transactions."""

    def __init__(self, jar_path: str, timeout: int = DEFAULT_TIMEOUT):
        self.jar_path = jar_path
        self.timeout = timeout
        self.proc: Optional[subprocess.Popen] = None
        self._req_id = 1

    def __enter__(self):
        self.start()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()

    def start(self):
        verify_java_runtime()
        cmd = [
            "java",
            "-Djava.awt.headless=true",
            "-jar",
            self.jar_path,
            "--mcp",
        ]
        self.proc = subprocess.Popen(
            cmd,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1,
        )
        self._initialize()

    def _initialize(self):
        init_payload = {
            "jsonrpc": "2.0",
            "id": self._next_id(),
            "method": "initialize",
            "params": {"protocolVersion": "2024-11-05"},
        }
        resp = self._send(init_payload)
        if "error" in resp:
            raise RuntimeError(f"MCP initialization error: {resp['error']}")

    def _next_id(self) -> int:
        cur = self._req_id
        self._req_id += 1
        return cur

    def _send(self, payload: Dict[str, Any]) -> Dict[str, Any]:
        if not self.proc or self.proc.poll() is not None:
            raise RuntimeError("TDA MCP server process is not running.")

        line = json.dumps(payload) + "\n"
        try:
            self.proc.stdin.write(line)
            self.proc.stdin.flush()
        except BrokenPipeError:
            err = self.proc.stderr.read() if self.proc.stderr else ""
            raise RuntimeError(f"TDA process pipe broken. Stderr: {err}")

        # Read line from stdout
        resp_line = self.proc.stdout.readline()
        if not resp_line:
            err = self.proc.stderr.read() if self.proc.stderr else ""
            raise RuntimeError(f"TDA process closed connection unexpectedly. Stderr: {err}")

        try:
            return json.loads(resp_line.strip())
        except json.JSONDecodeError as e:
            raise RuntimeError(f"Invalid JSON response from TDA: {resp_line!r}") from e

    def call_tool(self, tool_name: str, arguments: Optional[Dict[str, Any]] = None) -> Any:
        payload = {
            "jsonrpc": "2.0",
            "id": self._next_id(),
            "method": "tools/call",
            "params": {
                "name": tool_name,
                "arguments": arguments or {},
            },
        }
        resp = self._send(payload)
        if "error" in resp:
            raise RuntimeError(f"Tool '{tool_name}' failed: {resp['error'].get('message', resp['error'])}")

        result = resp.get("result", {})
        content = result.get("content", [])
        if content and isinstance(content, list):
            first = content[0]
            if isinstance(first, dict) and "text" in first:
                raw_text = first["text"]
                try:
                    return json.loads(raw_text)
                except json.JSONDecodeError:
                    return raw_text
        return result

    def close(self):
        if self.proc:
            try:
                self.proc.stdin.close()
            except Exception:
                pass
            try:
                self.proc.terminate()
                self.proc.wait(timeout=2)
            except Exception:
                try:
                    self.proc.kill()
                except Exception:
                    pass
            self.proc = None


def save_session_log(log_path: str):
    """Save active log path to user session cache for subsequent subcommands."""
    try:
        data = {"log_path": str(Path(log_path).resolve())}
        SESSION_FILE.write_text(json.dumps(data))
    except Exception:
        pass


def get_session_log() -> Optional[str]:
    """Retrieve the last used log path from user session cache."""
    try:
        if SESSION_FILE.is_file():
            data = json.loads(SESSION_FILE.read_text())
            p = data.get("log_path")
            if p and Path(p).is_file():
                return p
    except Exception:
        pass
    return None


def resolve_log_path(explicit_path: Optional[str]) -> str:
    """Resolve log path from argument or previous session."""
    if explicit_path:
        p = Path(explicit_path).expanduser().resolve()
        if not p.is_file():
            raise FileNotFoundError(f"Thread dump log file not found: {explicit_path}")
        save_session_log(str(p))
        return str(p)

    sess = get_session_log()
    if sess:
        return sess

    raise ValueError(
        "No log file specified. Please provide the log file path or run 'parse <logfile>' first."
    )


# --- Formatting Helpers ---

def format_summary(summary_data: Union[List[Dict[str, Any]], Any]) -> str:
    if not isinstance(summary_data, list):
        return str(summary_data)

    lines = []
    lines.append(f"Parsed {len(summary_data)} thread dump(s):")
    lines.append(f"{'Idx':<4} | {'Name':<32} | {'Timestamp':<24} | {'Threads':<8} | {'Deadlocks':<10}")
    lines.append("-" * 88)

    for item in summary_data:
        idx = item.get("index", "")
        name = str(item.get("name", ""))[:32]
        time_str = str(item.get("time", "N/A"))[:24]
        threads = item.get("threadCount", 0)
        deadlocks = item.get("deadlockCount", 0)
        dl_str = f"{deadlocks} ⚠️" if deadlocks > 0 else str(deadlocks)
        lines.append(f"{idx:<4} | {name:<32} | {time_str:<24} | {threads:<8} | {dl_str:<10}")

    return "\n".join(lines)


def format_list(items: Union[List[Any], Any], title: str) -> str:
    if not isinstance(items, list):
        return f"{title}:\n  {items}"
    if not items:
        return f"{title}: None reported."
    lines = [f"{title} ({len(items)} items):"]
    for it in items:
        if isinstance(it, dict):
            if "threadName" in it and "nativeMethod" in it:
                lines.append(f"  • {it['threadName']} => {it['nativeMethod']}")
            else:
                lines.append(f"  • {json.dumps(it)}")
        else:
            lines.append(f"  • {it}")
    return "\n".join(lines)


# --- CLI Commands ---

def cmd_parse(client: TDAMCPClient, args: argparse.Namespace) -> int:
    log_path = resolve_log_path(args.logfile)
    res = client.call_tool("parse_log", {"path": log_path})
    summary = client.call_tool("get_summary", {})
    if args.json:
        print(json.dumps({"message": res, "summary": summary}, indent=2))
    else:
        print(f"✓ {res}\n")
        print(format_summary(summary))
    return 0


def cmd_summary(client: TDAMCPClient, args: argparse.Namespace) -> int:
    log_path = resolve_log_path(args.logfile)
    client.call_tool("parse_log", {"path": log_path})
    res = client.call_tool("get_summary", {})
    if args.json:
        print(json.dumps(res, indent=2))
    else:
        print(format_summary(res))
    return 0


def cmd_deadlocks(client: TDAMCPClient, args: argparse.Namespace) -> int:
    log_path = resolve_log_path(args.logfile)
    client.call_tool("parse_log", {"path": log_path})
    res = client.call_tool("check_deadlocks", {})
    if args.json:
        print(json.dumps(res, indent=2))
    else:
        print(format_list(res, "Deadlock Assessment"))
    return 0


def cmd_long_running(client: TDAMCPClient, args: argparse.Namespace) -> int:
    log_path = resolve_log_path(args.logfile)
    client.call_tool("parse_log", {"path": log_path})
    res = client.call_tool("find_long_running", {})
    if args.json:
        print(json.dumps(res, indent=2))
    else:
        print(format_list(res, "Long Running Threads Across Dumps"))
    return 0


def cmd_virtual_threads(client: TDAMCPClient, args: argparse.Namespace) -> int:
    log_path = resolve_log_path(args.logfile)
    client.call_tool("parse_log", {"path": log_path})
    res = client.call_tool("analyze_virtual_threads", {})
    if args.json:
        print(json.dumps(res, indent=2))
    else:
        print(format_list(res, "Virtual Thread Carrier Pinning Analysis"))
    return 0


def cmd_native_threads(client: TDAMCPClient, args: argparse.Namespace) -> int:
    log_path = resolve_log_path(args.logfile)
    client.call_tool("parse_log", {"path": log_path})
    res = client.call_tool("get_native_threads", {"dump_index": args.dump_index})
    if args.json:
        print(json.dumps(res, indent=2))
    else:
        print(format_list(res, f"Threads in Native Methods (Dump #{args.dump_index})"))
    return 0


def cmd_zombies(client: TDAMCPClient, args: argparse.Namespace) -> int:
    log_path = resolve_log_path(args.logfile)
    client.call_tool("parse_log", {"path": log_path})
    res = client.call_tool("get_zombie_threads", {})
    if args.json:
        print(json.dumps(res, indent=2))
    else:
        print(format_list(res, "SMR Zombie Threads (Unresolved Addresses)"))
    return 0


def cmd_clear(client: TDAMCPClient, args: argparse.Namespace) -> int:
    res = client.call_tool("clear", {})
    if SESSION_FILE.is_file():
        try:
            SESSION_FILE.unlink()
        except Exception:
            pass
    if args.json:
        print(json.dumps({"result": res}, indent=2))
    else:
        print(f"✓ {res} (session file cleared)")
    return 0


def cmd_analyze(client: TDAMCPClient, args: argparse.Namespace) -> int:
    """End-to-end multi-check analysis pipeline for AI agents."""
    log_path = resolve_log_path(args.logfile)
    parse_msg = client.call_tool("parse_log", {"path": log_path})
    summary = client.call_tool("get_summary", {})
    deadlocks = client.call_tool("check_deadlocks", {})
    vthreads = client.call_tool("analyze_virtual_threads", {})
    long_running = client.call_tool("find_long_running", {})
    zombies = client.call_tool("get_zombie_threads", {})

    report = {
        "file": log_path,
        "parse_message": parse_msg,
        "summary": summary,
        "deadlocks": deadlocks,
        "virtual_threads": vthreads,
        "long_running": long_running,
        "zombie_threads": zombies,
    }

    if args.json:
        print(json.dumps(report, indent=2))
        return 0

    print("=" * 70)
    print(" 🔍 TDA Automated Thread Dump Diagnostic Report")
    print("=" * 70)
    print(f"File: {log_path}\n")

    print(format_summary(summary))
    print()
    print(format_list(deadlocks, "Deadlock Check"))
    print()
    print(format_list(vthreads, "Virtual Thread Carrier Pinning"))
    print()
    print(format_list(long_running, "Long-Running Thread Analysis"))
    print()
    print(format_list(zombies, "SMR Zombie Threads"))
    print("=" * 70)
    return 0


def build_parser() -> argparse.ArgumentParser:
    common_parser = argparse.ArgumentParser(add_help=False)
    common_parser.add_argument(
        "--jar",
        metavar="PATH",
        help="Explicit path to tda.jar (or set TDA_JAR_PATH env var)",
    )
    common_parser.add_argument(
        "--json",
        action="store_true",
        help="Output raw structured JSON instead of formatted text",
    )
    common_parser.add_argument(
        "--timeout",
        type=int,
        default=DEFAULT_TIMEOUT,
        help=f"Timeout in seconds for operations (default: {DEFAULT_TIMEOUT})",
    )

    parser = argparse.ArgumentParser(
        prog="tda_client.py",
        description="TDA CLI Bridge - Standalone JSON-RPC Client for Java Thread Dump Analysis",
        parents=[common_parser],
    )

    subparsers = parser.add_subparsers(dest="command", required=True)

    # analyze (composite)
    p_analyze = subparsers.add_parser("analyze", parents=[common_parser], help="Run full end-to-end diagnostic suite on a log file")
    p_analyze.add_argument("logfile", nargs="?", help="Path to thread dump log file")
    p_analyze.set_defaults(func=cmd_analyze)

    # parse
    p_parse = subparsers.add_parser("parse", parents=[common_parser], help="Parse log file and display summary")
    p_parse.add_argument("logfile", help="Path to thread dump log file")
    p_parse.set_defaults(func=cmd_parse)

    # summary
    p_summary = subparsers.add_parser("summary", parents=[common_parser], help="Show summary of all parsed dumps")
    p_summary.add_argument("logfile", nargs="?", help="Path to thread dump log file (optional if parsed)")
    p_summary.set_defaults(func=cmd_summary)

    # deadlocks
    p_deadlocks = subparsers.add_parser("deadlocks", parents=[common_parser], help="Check for deadlocks")
    p_deadlocks.add_argument("logfile", nargs="?", help="Path to thread dump log file (optional if parsed)")
    p_deadlocks.set_defaults(func=cmd_deadlocks)

    # long-running
    p_lr = subparsers.add_parser("long-running", parents=[common_parser], help="Find long running threads across dumps")
    p_lr.add_argument("logfile", nargs="?", help="Path to thread dump log file (optional if parsed)")
    p_lr.set_defaults(func=cmd_long_running)

    # virtual-threads
    p_vt = subparsers.add_parser("virtual-threads", parents=[common_parser], help="Detect carrier thread pinning in virtual threads")
    p_vt.add_argument("logfile", nargs="?", help="Path to thread dump log file (optional if parsed)")
    p_vt.set_defaults(func=cmd_virtual_threads)

    # native-threads
    p_nt = subparsers.add_parser("native-threads", parents=[common_parser], help="List threads in native methods for a dump index")
    p_nt.add_argument("dump_index", type=int, help="Index of dump (from summary)")
    p_nt.add_argument("logfile", nargs="?", help="Path to thread dump log file (optional if parsed)")
    p_nt.set_defaults(func=cmd_native_threads)

    # zombies
    p_zombies = subparsers.add_parser("zombies", parents=[common_parser], help="List unresolved SMR zombie threads")
    p_zombies.add_argument("logfile", nargs="?", help="Path to thread dump log file (optional if parsed)")
    p_zombies.set_defaults(func=cmd_zombies)

    # clear
    p_clear = subparsers.add_parser("clear", parents=[common_parser], help="Clear thread store and session cache")
    p_clear.set_defaults(func=cmd_clear)

    return parser


def main():
    parser = build_parser()
    args = parser.parse_args()

    try:
        jar_path = find_tda_jar(args.jar)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

    try:
        with TDAMCPClient(jar_path, timeout=args.timeout) as client:
            sys.exit(args.func(client, args))
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
