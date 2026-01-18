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
 */
package net.java.dev.tda.visualvm;

import java.util.HashMap;
import java.util.Map;
import net.java.dev.tda.visualvm.logfile.Logfile;
import org.graalvm.visualvm.core.datasource.DataSource;
import org.graalvm.visualvm.core.snapshot.Snapshot;
import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.DataSourceViewProvider;
import org.graalvm.visualvm.core.ui.DataSourceViewsManager;
import org.graalvm.visualvm.threaddump.ThreadDump;

/**
 * provides a tda view.
 * 
 * @author irockel
 */
public class TDAViewProvider extends DataSourceViewProvider<DataSource> {
    /*
     * FIXME: this is just a hack to add newly added thread dumps to an existing thread dump view.
     */
    private final Map<DataSource, TDAView> views = new HashMap<>();
    
    static void initialize() {
        DataSourceViewsManager.sharedInstance().addViewProvider(new TDAViewProvider(), DataSource.class);
    }

    @Override
    protected boolean supportsViewFor(DataSource logContent) {
        return ((logContent instanceof ThreadDump) || (logContent instanceof Logfile));
    }

    @Override
    protected DataSourceView createView(DataSource logContent) {
        TDAView tdaView;
        if(views.containsKey(logContent.getMaster())) {
            tdaView = views.get(logContent.getMaster());
            tdaView.addToTDA(((Snapshot) logContent).getFile().getAbsolutePath());
            return(tdaView);
        } else {
            tdaView = new TDAView(logContent);
            views.put(logContent.getMaster(), tdaView);
        }
        return(tdaView);
    }
}
