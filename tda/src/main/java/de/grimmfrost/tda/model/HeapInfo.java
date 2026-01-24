/*
 * HeapInfo.java
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
 * $Id: HeapInfo.java,v 1.1 2008-08-13 15:52:19 irockel Exp $
 */

package de.grimmfrost.tda.model;

/**
 * stores a heap information block for a thread dump
 * heap Information are only available for Sun JDK 1.6 so
 * far.
 * No parsing is done so far, the block is just stored "as-is".
 * @author irockel
 */
public class HeapInfo {
    private String heapInfo = null;

    public HeapInfo(String value) {
        this.heapInfo = value;
    }

    /**
     * @return the heapInfo
     */
    public String getHeapInfo() {
        return heapInfo;
    }

    /**
     * @param heapInfo the heapInfo to set
     */
    public void setHeapInfo(String heapInfo) {
        this.heapInfo = heapInfo;
    }

    public String toString() {
        StringBuffer info = new StringBuffer();
        info.append("<tr bgcolor=\"#ffffff\"<td></td></tr>");
        info.append("<tr bgcolor=\"#cccccc\"><td colspan=2><font face=System ");
        info.append("<b><u>Heap Information:</u></b><br/>");
        info.append("</font><pre>\n");
        info.append(heapInfo);
        info.append("</pre></td></tr>");

        return(info.toString());
    }
}
