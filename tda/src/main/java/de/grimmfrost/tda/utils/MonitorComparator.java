/*
 * MonitorComparator.java
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
 * $Id: MonitorComparator.java,v 1.2 2007-11-22 14:47:24 irockel Exp $
 */
package de.grimmfrost.tda.utils;

import java.util.Comparator;
import javax.swing.tree.DefaultMutableTreeNode;

import de.grimmfrost.tda.ThreadInfo;

/**
 * compares monitor nodes based on the amount of threads refering to the monitors. 
 * It return 0 for two monitors having the same amount of threads refering to them. 
 * Using this in a TreeSet is not feasible as only one thread of on thread amount
 * refering to it would survive, the others would be lost.
 * 
 * @author irockel
 */
public class MonitorComparator implements Comparator {

    /**
     * compares two monitor nodes based on the amount of threads refering to the monitors.
     * @param arg0 first monitor node
     * @param arg1 second monitor node
     * @return difference between amount of refering threads.
     */
    public int compare(Object arg0, Object arg1) {
        if (arg0 instanceof DefaultMutableTreeNode && arg1 instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode) arg0;
            DefaultMutableTreeNode secondNode = (DefaultMutableTreeNode) arg1;
            Object o1 = firstNode.getUserObject();
            Object o2 = secondNode.getUserObject();
            if (o1 instanceof ThreadInfo && o2 instanceof ThreadInfo) {
                return ((ThreadInfo) o2).getChildCount() - ((ThreadInfo) o1).getChildCount();
            }
            return (secondNode.getChildCount() - firstNode.getChildCount());
        }
        return (0);
    }
}
