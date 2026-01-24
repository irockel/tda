/*
 * DumpGeneralInfo.java
 *
 *
 * This file is part of TDA - Thread Dump Analysis Tool.
 *
 * TDA is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * TDA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with TDA; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: HistogramInfo.java,v 1.1 2006-03-01 19:19:37 irockel Exp $
 */

package de.grimmfrost.tda.model;

/**
 *
 * @author irockel
 */
public class HistogramInfo {
    public String threadDumpName;
    public Object content;
    
    public HistogramInfo(String name, Object content) {
        threadDumpName = name;
        this.content = content;
    }
    
    public String toString() {
        return threadDumpName;
    }   
}
