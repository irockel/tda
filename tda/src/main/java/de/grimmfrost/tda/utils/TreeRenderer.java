/*
 * TreeRenderer.java
  *
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
 * $Id: TreeRenderer.java,v 1.10 2008-03-13 21:16:08 irockel Exp $
 */

package de.grimmfrost.tda.utils;

import de.grimmfrost.tda.*;
import de.grimmfrost.tda.model.*;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * adds icons to tda root tree
 * @author irockel
 */
public class TreeRenderer extends DefaultTreeCellRenderer {

    public TreeRenderer() {
       // empty constructor
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                        boolean expanded, boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (leaf && isCategory(value)) {
            setIcon(getIconFromCategory(value));
        } else if (leaf && isThreadInfo(value)) {
            setIcon(TDA.createImageIcon("Thread.png"));
        } else if(leaf && isHistogramInfo(value)) {
            setIcon(TDA.createImageIcon("Histogram.png"));
        } else if (leaf && isLogfile(value)) {
            setIcon(TDA.createImageIcon("Root.png"));
        } else if (leaf && isLogFileContent(value)) {
            setIcon(TDA.createImageIcon("LogfileContent.png"));
        } else if(!leaf) {
            if(((DefaultMutableTreeNode) value).isRoot() || isLogfile(value)) {
                setIcon(TDA.createImageIcon("Root.png"));
            } else if(isThreadInfo(value)) {
                if(((ThreadInfo) ((DefaultMutableTreeNode) value).getUserObject()).areALotOfWaiting()) {
                    setIcon(TDA.createImageIcon("MonitorRed.png"));
                } else {
                    setIcon(TDA.createImageIcon("Monitor.png"));
                }
            } else {
                setIcon(TDA.createImageIcon("ThreadDump.png"));
            }
        }
        this.setBackgroundNonSelectionColor(new Color(0,0,0,0));
        
        return this;
    }

    protected boolean isCategory(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        return(node.getUserObject() instanceof Category);
    }
        
    protected Icon getIconFromCategory(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Category nodeInfo = (Category) node.getUserObject();
        
        return(nodeInfo.getIcon());
    }    

    private boolean isHistogramInfo(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        return(node.getUserObject() instanceof HistogramInfo);
    }
    
    private boolean isThreadInfo(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        return((node.getUserObject() instanceof ThreadInfo));
    }
    
    private boolean isLogfile(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        return(node.getUserObject() instanceof Logfile);
    }

    private boolean isLogFileContent(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        return(node.getUserObject() instanceof LogFileContent);
    }
}
