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

import com.pironet.tda.CustomCategoriesDialog;
import com.pironet.tda.CustomCategoriesDialog.CategoriesPanel;
import com.pironet.tda.FilterDialog;
import com.pironet.tda.FilterDialog.FilterPanel;
import com.pironet.tda.PreferencesDialog;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

final class VisualvmOptionsPanelController extends OptionsPanelController {

    private JTabbedPane panel;
    private PreferencesDialog prefDialog;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    private FilterPanel filterPanel;
    private CategoriesPanel catPanel;

    @Override
    public void update() {
        prefDialog.loadSettings();
        changed = false;
    }

    @Override
    public void applyChanges() {
        prefDialog.saveSettings();
        filterPanel.saveSettings();
        catPanel.saveSettings();
        changed = false;
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set

    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private JTabbedPane getPanel() {
        WindowManager wm = WindowManager.getDefault();
        if (panel == null) {
            prefDialog = new PreferencesDialog(wm.getMainWindow());
            filterPanel = new FilterDialog.FilterPanel(wm.getMainWindow());
            catPanel = new CustomCategoriesDialog.CategoriesPanel(wm.getMainWindow());
            
            panel = prefDialog.getPane();
            panel.addTab(NbBundle.getMessage(TDAView.class, "LBL_Filters"), filterPanel);
            panel.addTab(NbBundle.getMessage(TDAView.class, "LBL_Categories"), catPanel);
        }
        return panel;
    }

    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
