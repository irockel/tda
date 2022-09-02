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
 * $Id: LogfileDataSource.java,v 1.1 2008-09-30 19:21:59 irockel Exp $
 */
package net.java.dev.tda.visualvm.logfile;

import java.awt.Image;
import org.graalvm.visualvm.core.datasource.DataSource;
import org.graalvm.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import org.graalvm.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import org.graalvm.visualvm.core.model.AbstractModelProvider;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Logfile node in explorer view.
 * 
 * @author irockel
 */
public class LogfileDataSource extends DataSource {
    private static LogfileDataSource sharedInstance;
    
    
    public static synchronized LogfileDataSource sharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new LogfileDataSource();
        }
        
        return sharedInstance;
    }
    
    
    private LogfileDataSource() {
        DataSourceDescriptorFactory.getDefault().registerProvider(new LogfileDataSourceDescriptorProvider());
        DataSource.ROOT.getRepository().addDataSource(this);
    }
    
    
    private static class LogfileDataSourceDescriptor extends DataSourceDescriptor {
        private static final Image NODE_ICON = Utilities.loadImage("net/java/dev/tda/visualvm/logfile/resources/logfiles.png", true);    // NOI18N

        LogfileDataSourceDescriptor() {
            super(LogfileDataSource.sharedInstance(), NbBundle.getMessage(LogfileDumpView.class, "ExplorerNode_Name_Logfiles"), null, NODE_ICON, 20, EXPAND_ON_EACH_NEW_CHILD);   // NOI18N
        }

    }
    
    private static class LogfileDataSourceDescriptorProvider extends AbstractModelProvider<DataSourceDescriptor,DataSource> {
    
        public DataSourceDescriptor createModelFor(DataSource ds) {
            if (LogfileDataSource.sharedInstance().equals(ds)) {
                return new LogfileDataSourceDescriptor();
            }
            return null;
        }
    }


}
