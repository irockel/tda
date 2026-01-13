/*
 * ThreadsTableModel.java
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
 * $Id: ThreadsTableModel.java,v 1.6 2008-04-27 20:31:14 irockel Exp $
 */
package com.pironet.tda.utils;

import com.pironet.tda.ThreadInfo;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * table model for displaying thread overview.
 * 
 * @author irockel
 */
public class ThreadsTableModel extends AbstractTableModel {
    
    private Vector elements;
    
    private String[] columnNames = null;
    
    /**
     * 
     * @param rootNode
     */
    public ThreadsTableModel(DefaultMutableTreeNode rootNode) {
        // transform child nodes in proper vector.
        if(rootNode != null) {
            elements = new Vector();
            for(int i = 0; i < rootNode.getChildCount(); i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                elements.add(childNode.getUserObject());
                ThreadInfo ti = (ThreadInfo) childNode.getUserObject();
                if(columnNames == null) {
                    if(ti.getTokens().length > 3) {
                        columnNames = new String[] {"Name", "Type", "Prio", "Thread-ID", "Native-ID", "State", "Address Range"};
                    } else {
                        columnNames = new String[] {"Name", "Thread-ID", "State"};
                    }
                }
            }
        }
    }
    
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public int getRowCount() {
        return(elements.size());
    }

    public int getColumnCount() {
        return(columnNames.length);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ThreadInfo ti = ((ThreadInfo) elements.elementAt(rowIndex));
        String[] columns = ti.getTokens();
        //System.out.println("Info: " + ti.getInfo() + ", rowIndex"  + rowIndex + ", columnIndex: " + columnIndex);
        if(getColumnCount() > 3) {
            if (columnIndex > 1 && columnIndex < 5) {
                // Handle null or empty strings safely
                if (columns[columnIndex] != null && !columns[columnIndex].trim().isEmpty()) {
                    try {
                        return new Long(columns[columnIndex]);
                    } catch (NumberFormatException e) {
                        return 0L; // Return 0 for invalid numbers
                    }
                } else {
                    return 0L; // Return 0 for null or empty values
                }
            } else {
                return columns[columnIndex] != null ? columns[columnIndex] : "";
            }
        } else {
            if (columnIndex == 1) {
                // Handle null or empty strings safely
                if (columns[columnIndex] != null && !columns[columnIndex].trim().isEmpty()) {
                    try {
                        return new Long(columns[columnIndex]);
                    } catch (NumberFormatException e) {
                        return 0L; // Return 0 for invalid numbers
                    }
                } else {
                    return 0L; // Return 0 for null or empty values
                }
            } else {
                return columns[columnIndex] != null ? columns[columnIndex] : "";
            }
            
        }
    }
    
    /**
     * get the thread info object at the specified line
     * @param rowIndex the row index
     * @return thread info object at this line.
     */
    public ThreadInfo getInfoObjectAtRow(int rowIndex) {
        return(rowIndex >= 0 && rowIndex < getRowCount() ? (ThreadInfo) elements.get(rowIndex) : null);
    }
    
    /**
     * {@inheritDoc}
     */
    public Class getColumnClass(int columnIndex) {
        if(columnIndex > 1 && columnIndex < 5) {
            return Integer.class;
        } else {
            return String.class;
        }
    }
    
    /**
     * search for the specified (partial) name in thread names
     * 
     * @param startRow row to start the search
     * @param name the (partial) name
     * @return the index of the row or -1 if not found.
     */
    public int searchRowWithName(int startRow, String name) {
        int i = startRow;
        boolean found = false;
        while(!found && (i < getRowCount())) {
            found = getInfoObjectAtRow(i++).getTokens()[0].indexOf(name) >= 0;
        }
        
        return(found ? i-1 : -1);
    }

}
