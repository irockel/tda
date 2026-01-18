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
 * $Id: LogfileProvider.java,v 1.1 2008-09-30 19:21:59 irockel Exp $
 */

package net.java.dev.tda.visualvm.logfile;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graalvm.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import org.graalvm.visualvm.core.snapshot.RegisteredSnapshotCategories;
import org.graalvm.visualvm.core.ui.DataSourceViewsManager;

/**
 * provides logfile support.
 * 
 * @author irockel
 */
public class LogfileProvider {
    private static final Logger LOGGER = Logger.getLogger(LogfileProvider.class.getName());
    
    public static void createLogfile(File logfile) {
        try {
            Logfile newLogfile = new Logfile(logfile);
            
            // add data source.
            LogfileDataSource.sharedInstance().getRepository().addDataSource(newLogfile);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error loading logfile", ex);
        }
    }
    
    public static void initialize() {
        DataSourceDescriptorFactory.getDefault().registerProvider(new LogfileDescriptorProvider());
        LogfileDataSource.sharedInstance();
        //CoreDumpProvider.register(); registers persisted core dumps
        RegisteredSnapshotCategories.sharedInstance().registerCategory(LogfileSupport.getCategory());
        DataSourceViewsManager.sharedInstance().addViewProvider(LogfileSupport.getOverviewView(), Logfile.class);
    }
}
