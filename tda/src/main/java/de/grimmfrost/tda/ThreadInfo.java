/*
 * ThreadDumpInfo.java
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
 * $Id: ThreadInfo.java,v 1.9 2008-03-13 21:16:08 irockel Exp $
 */

package de.grimmfrost.tda;

/**
 * Info (name, content tuple) for thread dump display tree.
 *
 * @author irockel
 */
public class ThreadInfo extends AbstractInfo {
    private String content;
    private String info;
    private int stackLines;
    private String[] tokens;
    private boolean aLotOfWaiting;
    private int childCount;
    
    public ThreadInfo(String name, String info, String content, int stackLines, String[] tableTokens) {
        setName(name);
        this.info = info;
        this.content = content;
        this.stackLines = stackLines;
        tokens = tableTokens;
    }
    
    public String toString() {
        return getName();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getStackLines() {
        return stackLines;
    }

    public void setStackLines(int stackLines) {
        this.stackLines = stackLines;
    }
    
    public String[] getTokens() {
        return(tokens);
    }

    public void setALotOfWaiting(boolean b) {
        aLotOfWaiting = b;
    }
    
    public boolean areALotOfWaiting() {
        return(aLotOfWaiting);
    }

    public void setChildCount(int childCount) {
      this.childCount = childCount;
    }

    public int getChildCount() {
      return childCount;
    }
}
