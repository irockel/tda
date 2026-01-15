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
 * $Id: AbstractCategory.java,v 1.4 2008-03-09 06:36:51 irockel Exp $
 */
package de.grimmfrost.tda;

import de.grimmfrost.tda.filter.FilterChecker;
import de.grimmfrost.tda.utils.IconFactory;
import de.grimmfrost.tda.utils.PrefManager;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author irockel
 */
public abstract class AbstractCategory extends AbstractInfo implements Category {
    private String info;
    
    private DefaultMutableTreeNode rootNode = null;
    private DefaultMutableTreeNode filteredRootNode = null;
    
    private transient JScrollPane lastView = null;
    
    private transient FilterChecker filterChecker = null;
    
    private boolean filterEnabled = true;
    
    private int iconID = -1;
    
    /**
     * sorts the category tree by the given comparator.
     * @param nodeComp 
     */
    public void sort(Comparator nodeComp) {
        Object[] arrayCat = new Object[getRootNode().getChildCount()];
        for(int i = 0; i < getRootNode().getChildCount(); i++) {
            // add nodes to sorting tree
            arrayCat[i] = getRootNode().getChildAt(i);
        }
        setRootNode(new DefaultMutableTreeNode("root"));

        Arrays.sort(arrayCat, nodeComp);
        for(int i = 0; i < arrayCat.length; i++) {
            getRootNode().add((DefaultMutableTreeNode) arrayCat[i]);
        }

        // reset filter.
        setLastView(null);
        
        setFilteredRootNode(null);
        
    }

    public Icon getIcon() {
        return(IconFactory.get().getIconFor(iconID));
    }
    
    public void setIconID(int iconID) {
        this.iconID = iconID;
    }
        
    private long lastUpdated = -1;
    
    /**
     * return amount of filtered nodes
     */
    public int howManyFiltered() {
       return(getFilteredRootNode() != null && getRootNode() != null ? getRootNode().getChildCount() - getFilteredRootNode().getChildCount() : 0); 
    }
    
    public int getNodeCount() {
        return(getRootNode() == null ? 0 : getRootNode().getChildCount());
    }
    
    public int showing() {
       return(getFilteredRootNode() != null ? getFilteredRootNode().getChildCount() : 0);
    }
    
    public String toString() {
        return(getName());
    }
    
    /**
     * add the passed node to the category tree
     */
    public void addToCatNodes(DefaultMutableTreeNode node) {
        if(getRootNode() == null) {
            setRootNode(new DefaultMutableTreeNode("root"));
        }
        getRootNode().add(node);
    }
    
    /**
     * get the node at the given position in the unfiltered tree.
     * @param index the index to look up.
     * @return the node at the given index, null otherwise.
     */
    public DefaultMutableTreeNode getNodeAt(int index) {
        return(getRootNode() != null ? (DefaultMutableTreeNode) getRootNode().getChildAt(index) : null);
    }
    
    public void setLastView(JScrollPane view) {
        lastView = view;
    }
    
    /**
     * get the last view if there is one, null otherwise.
     * @return the last view
     */
    public JScrollPane getLastView() {
        if(getLastUpdated() < PrefManager.get().getFiltersLastChanged()) {
            // reset view as changed filters need to be applied.
            lastView = null;
        }
        return(lastView);
    }

    protected long getLastUpdated() {
        return lastUpdated;
    }

    protected void setLastUpdated() {
        this.lastUpdated = System.currentTimeMillis();
    }

    protected DefaultMutableTreeNode filterNodes(DefaultMutableTreeNode rootNode) {
        setFilteredRootNode(new DefaultMutableTreeNode("root"));
        if(rootNode != null) {
            for(int i = 0; i < rootNode.getChildCount(); i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                if(getFilterChecker().recheck((ThreadInfo) childNode.getUserObject())) {
                    // node needs to be cloned as it is otherwise removed from rootNode.
                    DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(childNode.getUserObject());    
                    getFilteredRootNode().add(newChild);
                }
            }
        }
        return(getFilteredRootNode());
    }

    public FilterChecker getFilterChecker() {
        if(filterChecker == null) {
            setFilterChecker(FilterChecker.getFilterChecker());
        }
        return filterChecker;
    }

    protected void setFilterChecker(FilterChecker filterChecker) {
        this.filterChecker = filterChecker;
    }

    protected boolean isFilterEnabled() {
        return filterEnabled;
    }

    protected void setFilterEnabled(boolean filterEnabled) {
        this.filterEnabled = filterEnabled;
    }

    protected DefaultMutableTreeNode getRootNode() {
        return rootNode;
    }

    protected void setRootNode(DefaultMutableTreeNode rootNode) {
        this.rootNode = rootNode;
    }

    protected DefaultMutableTreeNode getFilteredRootNode() {
        return filteredRootNode;
    }

    protected void setFilteredRootNode(DefaultMutableTreeNode filteredRootNode) {
        this.filteredRootNode = filteredRootNode;
    }
    
    public void setInfo(String info) {
        this.info = info;
    }
    
    public String getInfo() {
        return(info);
    }
}
