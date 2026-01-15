/*
 * PreferencesDialog.java
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
 * $Id: ViewScrollPane.java,v 1.1 2008-04-27 20:31:14 irockel Exp $
 */
package de.grimmfrost.tda.utils;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

/**
 * custom scroll pane to be used in tda for consistent look and feel in the tool.
 * 
 * @author irockel
 */
public class ViewScrollPane extends JScrollPane {
    
    /**
     * constructor for the view scroll pane
     * 
     * @param comp the component to wrap into the scroll pane
     * @param white true, if the scrollpane should be in flat white.
     */
    public ViewScrollPane(Component comp, boolean white) {
        super(comp);
        if(white) {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder());
            setBackground(Color.WHITE);
            getHorizontalScrollBar().setBackground(Color.WHITE);
        }
    }
}
