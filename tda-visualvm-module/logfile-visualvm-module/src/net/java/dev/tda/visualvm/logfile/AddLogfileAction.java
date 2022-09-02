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
 * $Id: AddLogfileAction.java,v 1.1 2008-09-30 19:21:59 irockel Exp $
 */
package net.java.dev.tda.visualvm.logfile;

import com.pironet.tda.utils.PrefManager;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import org.graalvm.visualvm.core.ui.actions.SingleDataSourceAction;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * popup menu action for adding log files.
 * @author irockel
 */
public class AddLogfileAction extends SingleDataSourceAction<LogfileDataSource> {
    
    private static final String ICON_PATH = "net/java/dev/tda/visualvm/logfile/resources/logfileadd.png";    // NOI18N
    private static final Image ICON =  Utilities.loadImage(ICON_PATH);
    private static JFileChooser fc = null;
    
    private boolean tracksSelection = false;
    
    private static AddLogfileAction alwaysEnabled;
    private static AddLogfileAction selectionAware;
        
    private AddLogfileAction() {
        super(LogfileDataSource.class);
        putValue(NAME, NbBundle.getMessage(LogfileDumpView.class, "LBL_Add_Logfile"));  // NOI18N
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(LogfileDumpView.class, "ToolTip_Add_Logfile")); // NOI18N
    }
    
    public static synchronized AddLogfileAction alwaysEnabled() {
        if (alwaysEnabled == null) {
            alwaysEnabled = new AddLogfileAction();
            alwaysEnabled.putValue(SMALL_ICON, new ImageIcon(ICON));
            alwaysEnabled.putValue("iconBase", ICON_PATH);  // NOI18N
        }
        return alwaysEnabled;
    }
    
    public static synchronized AddLogfileAction selectionAware() {
        if (selectionAware == null) {
            selectionAware = new AddLogfileAction();
            selectionAware.tracksSelection = true;
        }
        return selectionAware;
    }
    
    public void actionPerformed(LogfileDataSource container, ActionEvent e) {
        if(fc == null) {
            fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            fc.setCurrentDirectory(PrefManager.get().getSelectedPath());
        }
        if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File[] files = fc.getSelectedFiles();
            for(int i = 0; i < files.length; i++) {
                LogfileProvider.createLogfile(files[i]);
            }
            PrefManager.get().setSelectedPath(fc.getCurrentDirectory());
        }
    }
    
    
    protected boolean isEnabled(LogfileDataSource container) {
        return true;
    }
    
    @Override
    protected void initialize() {
        if (tracksSelection) {
            super.initialize();
        }
    }
}

