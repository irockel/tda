/*
 * StatusBar.java
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
 * $Id: StatusBar.java,v 1.5 2008-04-27 20:31:14 irockel Exp $
 */
package de.grimmfrost.tda.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.text.NumberFormat;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * status bar of tda
 *
 * @author irockel
 */
public class StatusBar extends JPanel {
    private JLabel infoLabel = null;
    private JProgressBar memStatus = null;
    
    /** 
     * Creates a new instance of StatusBar 
     */
    public StatusBar(boolean showMemory) {
        super(new BorderLayout());
        add(createInfoPanel(), BorderLayout.WEST);
        if(showMemory) {
            add(createMemoryStatus(), BorderLayout.CENTER);
            JPanel iconPanel = new JPanel(new BorderLayout());
            iconPanel.add(new JLabel(new AngledLinesWindowsCornerIcon()), BorderLayout.SOUTH);
            add(iconPanel, BorderLayout.EAST);
        } else { // plugin mode
            setBackground(Color.WHITE);
        }
    }
    
    /**
     * set the info text of the status bar
     */
    public void setInfoText(String text) {
        infoLabel.setText(text);
    }
    
    private JPanel createInfoPanel() {
        infoLabel = new JLabel(AppInfo.getStatusBarInfo());
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        fl.setHgap(5);
        JPanel infoPanel = new JPanel(fl);
        infoPanel.setOpaque(false);
        infoPanel.add(infoLabel);
        
        return(infoPanel);
    }
    
    /**
     * create the memory status panel, also includes a resize icon on the lower right
     */
    private JPanel createMemoryStatus() {
        memStatus = new JProgressBar(0, 100);
        memStatus.setPreferredSize(new Dimension(100, 15));
        memStatus.setStringPainted(true);
        JPanel memPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        memPanel.add(memStatus);
        
        // Start Updater
        new Thread(new MemoryStatusUpdater(memStatus)).start();
        
        return memPanel;
    }
    
}

/**
 * runnable object for running in a background thread to update the memory
 * status of the application
 */
class MemoryStatusUpdater implements Runnable {
    private JProgressBar memStatus = null;
    private Runtime rt = Runtime.getRuntime();

    private NumberFormat formatter;
    
    public MemoryStatusUpdater(JProgressBar memStatus) {
        this.memStatus = memStatus;
        this.formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(1);
    }
    
    public void run() {
        try {
            while(true) {
                double factor = (int) rt.totalMemory() / 100;
                int perc = (int) ((rt.totalMemory() - rt.freeMemory()) / factor);
                memStatus.setValue(perc);
                double usedMem = (rt.totalMemory() - rt.freeMemory()) / 1024.0 / 1024.0;
                double totalMem = rt.totalMemory() / 1024.0 / 1024.0;
                memStatus.setString(formatter.format(usedMem) + "MB/" +
                        formatter.format(totalMem) +"MB");
                memStatus.setToolTipText(memStatus.getString() + " Memory used.");
                Thread.sleep(5000);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
}

/**
 * paint a resize corner icon
 */
class AngledLinesWindowsCornerIcon implements Icon {
    private static final Color WHITE_LINE_COLOR = new Color(255, 255, 255);
    
    private static final Color GRAY_LINE_COLOR = new Color(172, 168, 153);
    private static final int WIDTH = 13;
    
    private static final int HEIGHT = 13;
    
    public int getIconHeight() {
        return WIDTH;
    }
    
    public int getIconWidth() {
        return HEIGHT;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        
        g.setColor(WHITE_LINE_COLOR);
        g.drawLine(0, 12, 12, 0);
        g.drawLine(5, 12, 12, 5);
        g.drawLine(10, 12, 12, 10);
        
        g.setColor(GRAY_LINE_COLOR);
        g.drawLine(1, 12, 12, 1);
        g.drawLine(2, 12, 12, 2);
        g.drawLine(3, 12, 12, 3);
        
        g.drawLine(6, 12, 12, 6);
        g.drawLine(7, 12, 12, 7);
        g.drawLine(8, 12, 12, 8);
        
        g.drawLine(11, 12, 12, 11);
        g.drawLine(12, 12, 12, 12);
        
    }
}
