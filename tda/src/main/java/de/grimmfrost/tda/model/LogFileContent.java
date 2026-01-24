/*
 * LogFileContent.java
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
 * $Id: LogFileContent.java,v 1.7 2008-01-16 11:33:27 irockel Exp $
 */

package de.grimmfrost.tda.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;

/**
 * logfile content info object of log file thread dump information.
 * @author irockel
 */
public class LogFileContent extends AbstractInfo implements Serializable {
    
    private String logFile;
    
    /**
     * stored as soft reference, as this content might get quite big.
     */
    private transient SoftReference content;
    
    private transient StringBuffer contentBuffer;
    
    /** 
     * Creates a new instance of LogFileContent 
     */
    public LogFileContent(String logFile) {
        setLogFile(logFile);
    }
    
    public String getLogfile() {
        return(logFile);
    }
    
    public void setLogFile(String value) {
        logFile = value;
    }
    
    public String toString() {
        return("Logfile");
    }
    
    /**
     * get the content as string, it is stored as soft reference,
     * so it might be loaded from disk again, as the vm needed memory
     * after the last access to it.
     */
    public String getContent() {
        if(contentBuffer == null) {
            if (content == null || content.get() == null) {
                readContent();
            }

            return (((StringBuffer) content.get()).toString());
        } else {
            return (contentBuffer.toString());
        }
    }
        
    /**
     * append the given string to the content buffer for this logfile
     * @param append the string to append.
     */
    public void appendToContentBuffer(String append) {
        if(contentBuffer == null) {
            contentBuffer = new StringBuffer(append);
        } else {
            contentBuffer.append("\n");
            contentBuffer.append(append);
        }
    }

    /**
     * read the content in the soft reference object, currently used
     * StringBuffer to maintain 1.4 compability. Should be switched 
     * to StringReader if switched to 1.5 for better performance as 
     * synchronization is not needed here.
     */
    private void readContent() {
        StringBuffer contentReader = new StringBuffer();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(getLogfile()));
            while(br.ready()) {
                contentReader.append(br.readLine());
                contentReader.append("\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            contentReader.append("The Logfile unavailable! " + ex.getMessage());
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        content = new SoftReference(contentReader);
    }
    
}
