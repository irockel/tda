/*
 * LongThreadDialog.java
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
 * TDA should have received a copy of the Lesser GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: LongThreadDialog.java,v 1.10 2008-02-14 14:36:08 irockel Exp $
 */

package de.grimmfrost.tda;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * long running thread detection dialog.
 * 
 * @author irockel
 */
public class LongThreadDialog extends JDialog {
    private JTabbedPane prefsPane;
    private SettingsPanel settingsPanel;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;
    private TDA backRef;
    private TreePath[] dumps;
    private DefaultMutableTreeNode top;
    private Map threadDumps;
    
    /** 
     * Creates a new instance of PreferencesDialog 
     */
    public LongThreadDialog(TDA owner, TreePath[] dumps, DefaultMutableTreeNode top, Map threadDumps) {        
        super(TDA.frame, "Detect long running Threads", true);
        backRef = owner;
        this.dumps = dumps;
        this.threadDumps = threadDumps;
        this.top = top;
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        initPanel();
        setLocationRelativeTo(owner);
    }
    
    private void initPanel() {
        prefsPane = new JTabbedPane();
        settingsPanel = new SettingsPanel();
        prefsPane.addTab("Settings", settingsPanel);        
        getContentPane().add(prefsPane,BorderLayout.CENTER);
        okButton = new JButton("Start Detection");
        cancelButton = new JButton("Cancel");
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int divider = 0;
                if(backRef.isThreadDisplay()) {
                    divider = backRef.topSplitPane.getDividerLocation();
                }

                ((Logfile) top.getUserObject()).getUsedParser().findLongRunningThreads(top, threadDumps, dumps, Integer.parseInt(settingsPanel.minOccurenceField.getText()), settingsPanel.threadRegExField.getText());
                backRef.createTree();
                backRef.tree.expandRow(1);

                backRef.getRootPane().revalidate();
                if(divider > 0) {
                    backRef.topSplitPane.setDividerLocation(divider);
                }
                dispose();
            }
        });
        
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        reset();        
    }
    
    public void reset() {
        getRootPane().setDefaultButton(okButton);
    }
    
    class SettingsPanel extends JPanel {
        JTextField minOccurenceField;
        JTextField threadRegExField;
        
        public SettingsPanel() {
            setLayout(new BorderLayout());
            
            JPanel layoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));            
            layoutPanel.add(new JLabel("Minimum occurence of a thread"));
            minOccurenceField = new JTextField(3);
            minOccurenceField.setText(String.valueOf(dumps.length));
            layoutPanel.add(minOccurenceField);
            add(layoutPanel, BorderLayout.NORTH);
            
            layoutPanel = new JPanel(new BorderLayout());
            layoutPanel.add(new JLabel("Regular Expression thread identifier matches"), BorderLayout.NORTH);
            threadRegExField = new JTextField(30);
            layoutPanel.add(threadRegExField, BorderLayout.CENTER);
            add(layoutPanel, BorderLayout.CENTER);
            
            layoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel example = new JLabel("<html><body>Example is <b>\"AJPRequestHandler(.*)</b>");
            example.setFont(new Font("SansSerif", Font.PLAIN, 10));
            layoutPanel.add(example);
            add(layoutPanel, BorderLayout.SOUTH);
        }
    }
    
    /**
     * Must be called from the event-dispatching thread.
     */
    public void resetFocus() {
    }
    
}
