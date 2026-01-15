/*
 * DumpStore.java
 *
 * This file is part of TDA - Thread Dump Analysis Tool.
 *
 * Foobar is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with TDA; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: DumpStore.java,v 1.2 2007-11-01 14:59:39 irockel Exp $
 */
package de.grimmfrost.tda;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * stores a tree of dump files
 * @author irockel
 */
public class DumpStore implements Serializable {
    
    private Map dumpFiles;
    
    /**
     * Creates a new instance of DumpStore
     */
    public DumpStore() {
    }
    
    /**
     * add the found thread dumps of a dump file to dump store
     * @param key the key to store the thread dumps in, usually the file name
     * @param threadDumpsInFile new found thread dumps to add.
     */
    public void addFileToDumpFiles(String key, Map threadDumpsInFile) {
        // first check if map is null, and if so, create new instance
        if(dumpFiles == null) {
            dumpFiles = new HashMap();
        }
        if(threadDumpsInFile != null) {
            dumpFiles.put(key, threadDumpsInFile);
        }
    }
    
    /**
     * get the thread dumps for the specified file key from the store
     */
    public Map getFromDumpFiles(String key) {
        return(dumpFiles != null ? (Map) dumpFiles.get(key) : null);
    }
    
    /**
     * get an iterator on the dumps file keys
     */
    public Iterator iterOfDumpFilesKeys() {
        return(dumpFiles != null ? dumpFiles.keySet().iterator() : null);
    }
    
}
