/*
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
 * $Id: Logfile.java,v 1.1 2008-09-30 19:21:59 irockel Exp $
 */
package net.java.dev.tda.visualvm.logfile;

import java.io.File;
import java.io.IOException;
import org.graalvm.visualvm.core.datasource.DataSource;
import org.graalvm.visualvm.core.snapshot.Snapshot;

/**
 *
 * @author irockel
 */
public class Logfile extends Snapshot {
    
    public Logfile(File file) throws IOException {
        this(file, null);
    }
    
    public Logfile(File file, DataSource master) throws IOException {
        super(file, LogfileSupport.getCategory(), master);
        
        if (!file.exists() || !file.isFile())
            throw new IOException("File " + file.getAbsolutePath() + " does not exist");    // NOI18N        
    }
        
    @Override
    public boolean supportsSaveAs() {
        return false;
    }
}

