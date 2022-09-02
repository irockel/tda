/*
 * ColoredTable.java
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
 * $Id: ColoredTable.java,v 1.2 2008-01-10 20:36:11 irockel Exp $
 */
package com.pironet.tda.utils;

import java.awt.Color;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * GrayWhiteTable renders its rows with a sequential color combination of white
 * and gray. Rows with even indicies are rendered white, odd indicies light grey.
 * Note: Do not use GrayWhiteTable for tables with custom renderers such as
 * check boxes. Use JTable instead and modify DefaultTableCellRenderer. Just keep
 * in mind that in order to display a table with more than 1 row colors, you
 * must have 2 separate intances of the renderer, one for each color.
 * 
 * @author irockel
 */
public class ColoredTable extends JTable {

    private DefaultTableCellRenderer whiteRenderer;
    private DefaultTableCellRenderer grayRenderer;

    public ColoredTable() {
        super();
    }

    public ColoredTable(TableModel tm) {
        super(tm);
    }

    public ColoredTable(Object[][] data, Object[] columns) {
        super(data, columns);
    }

    public ColoredTable(int rows, int columns) {
        super(rows, columns);
    }

    /**
     * If row is an even number, getCellRenderer() returns a DefaultTableCellRenderer
     * with white background. For odd rows, this method returns a DefaultTableCellRenderer
     * with a light gray background.
     */
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (whiteRenderer == null) {
            whiteRenderer = new DefaultTableCellRenderer();
            whiteRenderer.setBackground(Color.WHITE);
            whiteRenderer.setForeground(Color.BLACK);
        }

        if (grayRenderer == null) {
            grayRenderer = new DefaultTableCellRenderer();
            grayRenderer.setBackground(new Color(240, 240, 240));
            grayRenderer.setForeground(Color.BLACK);
        }

        if ((row % 2) == 0) {
            return whiteRenderer;
        } else {
            return grayRenderer;
        }
    }
}
