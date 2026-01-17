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
 * $Id: LogfileDumpViewProvider.java,v 1.1 2008-09-30 19:21:59 irockel Exp $
 */
package net.java.dev.tda.visualvm.logfile;

import java.util.Set;
import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.PluggableDataSourceViewProvider;

/**
 * datasource view provider for logfile view.
 * 
 * @author irockel
 */
public class LogfileDumpViewProvider extends PluggableDataSourceViewProvider<Logfile>{
    
    protected boolean supportsViewFor(Logfile coreDump) {
        return true;
    }

    protected DataSourceView createView(Logfile logfile) {
        return new LogfileDumpView(logfile);
    }
    
    public Set<Integer> getPluggableLocations(DataSourceView view) {
        return ALL_LOCATIONS;
    }

}

