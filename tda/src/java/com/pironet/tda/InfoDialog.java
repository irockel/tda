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
 */

package com.pironet.tda;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * display copyright notice.
 * @author irockel
 */
public class InfoDialog extends JDialog {
    
    /**
     * Creates a new instance of PreferencesDialog
     */
    public InfoDialog(JFrame owner) {
        super(owner, "About TDA", true);  
        BorderLayout layout = new BorderLayout();
        layout.setHgap(30);
        getContentPane().setLayout(layout);
        initPanel();        
    }
    
    private void initPanel() {
        BorderLayout upperLayout = new BorderLayout();
        upperLayout.setVgap(10);
        upperLayout.setHgap(10);
        JPanel upperPane = new JPanel(upperLayout);
        upperPane.add(new JLabel(TDA.createImageIcon("tda-logo.png")), BorderLayout.CENTER);
        upperPane.add(new JLabel(" "), BorderLayout.EAST);
        upperPane.add(new JLabel(" "), BorderLayout.WEST);
        upperPane.add(new JLabel(""), BorderLayout.NORTH);
        upperPane.add(new JLabel(""), BorderLayout.SOUTH);
        getContentPane().add(upperPane, BorderLayout.NORTH);
        
        String info = "<html><body>" +
                "<p><i>Java Version: " + System.getProperty("java.version") + "<br><br>" +
                "(C) 2006-2020 by Ingo Rockel &lt;ingo.rockel@gmail.com&gt;,<br>" +
                "with contributions from Robert Whitehurst &lt;maestoso@java.net&gt;.<br><br>" +
                "Icons used are based on Benno System Icons by Benno Meyer.<br>" +
                "Welcome screen uses icons from NuoveXT2 icon set by Alexandre Moore.<br>" +
                "Contains classes from JEdit for logfile display.</i></p><br>" +
                "<p>TDA is free software; you can redistribute it and/or modify it under<br>" +
                "the terms of the Lesser GNU General Public License as published by<br>" +
                "the Free Software Foundation; either version 2.1 of the License, or<br>" +
                "(at your option) any later version.</p><br>" +
                "TDA is distributed in the hope that it will be useful, but WITHOUT ANY<br>" +
                "WARRANTY; without even the implied warranty of MERCHANTABILITY or <br>" +
                "FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public<br>" +
                "License for more details.<p><br>";
        
        getContentPane().add(new JLabel(info), BorderLayout.CENTER);
        getContentPane().add(new JLabel(""), BorderLayout.EAST);
        getContentPane().add(new JLabel(""), BorderLayout.WEST);
        JButton okButton = new JButton("Close");
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);        
    }    
}
