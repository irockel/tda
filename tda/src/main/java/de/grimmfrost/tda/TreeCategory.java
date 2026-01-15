/*
 * TreeCategory.java
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
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: TreeCategory.java,v 1.2 2008-01-07 14:55:06 irockel Exp $
 */

package de.grimmfrost.tda;

import de.grimmfrost.tda.filter.FilterChecker;
import de.grimmfrost.tda.utils.PrefManager;
import de.grimmfrost.tda.utils.TreeRenderer;
import java.io.Serializable;
import java.util.Comparator;
import java.util.EventListener;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class represent a category node.
 *
 * @author irockel
 */
public class TreeCategory extends AbstractCategory implements Serializable {
    private transient JTree filteredCatTree;
    
    /** 
     * Creates a new instance of TreeCategory 
     */
    public TreeCategory(String name, int iconID) {
        this(name, iconID, true);
    }

    /** 
     * Creates a new instance of TreeCategory 
     */
    public TreeCategory(String name, int iconID, boolean filtering) {
        setName(name);
        setFilterEnabled(filtering);
        setIconID(iconID);
    }
    
    /**
     * return category tree with filtered child nodes
     */
    public JComponent getCatComponent(EventListener listener) {
        if(isFilterEnabled() && ((filteredCatTree == null) || (getLastUpdated() < PrefManager.get().getFiltersLastChanged()))) {
            // first refresh filter checker with current filters
            setFilterChecker(FilterChecker.getFilterChecker());
            
            // apply new filter settings.
            filteredCatTree = new JTree(filterNodes(getRootNode()));
            if(getName().startsWith("Monitors") || getName().startsWith("Threads blocked by Monitors")) {
                filteredCatTree.setShowsRootHandles(true);
            }
            filteredCatTree.setCellRenderer(new TreeRenderer());
            filteredCatTree.setRootVisible(false);
            filteredCatTree.addTreeSelectionListener((TreeSelectionListener) listener);
            setLastUpdated();
        } else if (!isFilterEnabled() && (filteredCatTree == null) || (getLastUpdated() < PrefManager.get().getFiltersLastChanged())) {
            filteredCatTree = new JTree(getRootNode());
            if(getName().startsWith("Monitors") || getName().startsWith("Threads blocked by Monitors")) {
                filteredCatTree.setShowsRootHandles(true);
            }
            filteredCatTree.setCellRenderer(new TreeRenderer());
            filteredCatTree.setRootVisible(false);
            filteredCatTree.addTreeSelectionListener((TreeSelectionListener) listener);
        }
        return(filteredCatTree);
    }
    
    /**
     * get the currently selected user object. 
     * @return the currently selected user object, null if nothing is selected.
     */
    public ThreadInfo getCurrentlySelectedUserObject() {
        return(getFilteredCatTree() == null || getFilteredCatTree().getSelectionPath() == null ? null : 
            (ThreadInfo) ((DefaultMutableTreeNode) ((JTree) getFilteredCatTree()).getSelectionPath().getLastPathComponent()).getUserObject());
    }
    
    private JTree getFilteredCatTree() {
        return filteredCatTree;
    }

    private void setFilteredCatTree(JTree filteredCatTree) {
        this.filteredCatTree = filteredCatTree;
    }
    
    /**
     * {@inheritDoc}
     */
    public void sort(Comparator nodeComp) {
        super.sort(nodeComp);
        setFilteredCatTree(null);
    }
}
