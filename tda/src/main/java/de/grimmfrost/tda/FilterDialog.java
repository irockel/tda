/*
 * FilterDialog.java
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
 * $Id: FilterDialog.java,v 1.12 2008-04-30 09:03:33 irockel Exp $
 */
package de.grimmfrost.tda;

import de.grimmfrost.tda.utils.PrefManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * overview of all available filters
 * @author irockel
 */
public class FilterDialog extends JDialog {

    private FilterPanel filterPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private Frame frame;

    /**
     * Creates a new instance of PreferencesDialog
     */
    public FilterDialog(Frame owner) {
        super(owner, "Filter Settings", true);
        try {
            setIconImage(TDA.createImageIcon("Filters.png").getImage());
        } catch (NoSuchMethodError nsme) {
            // ignore, for 1.4 backward compatibility
        }

        frame = owner;
        getContentPane().setLayout(new BorderLayout());
        initPanel();
    }

    private void initPanel() {
        filterPanel = new FilterPanel((JFrame) this.getOwner());
        getContentPane().add(filterPanel, BorderLayout.CENTER);
        closeButton = new JButton("Close");
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        //buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                filterPanel.saveSettings();
                dispose();
            }
        });
        reset();
    }

    public void reset() {
        getRootPane().setDefaultButton(closeButton);
    }

    public static class FilterPanel extends JPanel implements ActionListener, ListSelectionListener {

        JButton addButton = null;
        JButton removeButton = null;
        JButton editButton = null;
        JPanel buttonFlow = null;
        JList filterList = null;
        JScrollPane scrollPane = null;
        Frame owner = null;

        public FilterPanel(Frame owner) {
            this.owner = owner;
            setLayout(new BorderLayout());

            buttonFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            add(Box.createVerticalStrut(5), BorderLayout.NORTH);
            add(Box.createHorizontalStrut(5), BorderLayout.WEST);
            JPanel innerButtonPanel = new JPanel(new GridLayout(3, 1, 5, 5));

            innerButtonPanel.add(addButton = new JButton("Add"));
            innerButtonPanel.add(removeButton = new JButton("Remove"));
            innerButtonPanel.add(editButton = new JButton("Edit"));
            removeButton.setEnabled(false);
            editButton.setEnabled(false);

            addButton.addActionListener(this);
            removeButton.addActionListener(this);
            editButton.addActionListener(this);

            buttonFlow.add(innerButtonPanel);

            add(buttonFlow, BorderLayout.EAST);
            setPreferredSize(new Dimension(380, 290));

            //createList();
            filterList = new JList(PrefManager.get().getFilters());
            scrollPane = new JScrollPane(filterList);
            filterList.addListSelectionListener(this);

            add(scrollPane, BorderLayout.CENTER);

        }

        public void createList() {
            int selectedIndex = -1;
            if (filterList != null) {
                selectedIndex = filterList.getSelectedIndex();
            }

            if (selectedIndex > -1) {
                filterList.setSelectedIndex(selectedIndex);
            }
        }

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();

            if ("Add".equals(cmd)) {
                createFilterDialog("Add Filter", true, -1);
            } else if ("Edit".equals(cmd)) {
                createFilterDialog("Edit Filter", false, filterList.getSelectedIndex());
            } else if ("Remove".equals(cmd)) {
                removeFilter();
            }
        }

        private void removeFilter() {
            if (JOptionPane.showConfirmDialog(null, "Are you sure, you want to remove the selected filter?", "Confirm Remove",
                    JOptionPane.YES_NO_OPTION) == 0) {
                ((DefaultListModel) filterList.getModel()).removeElementAt(filterList.getSelectedIndex());
            }
        }

        private void createFilterDialog(String title, boolean isAdd, int selectedIndex) {
            EditFilterDialog fDiag = new EditFilterDialog(owner, title, filterList, isAdd);
            fDiag.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            //Display the window.
            fDiag.reset();
            fDiag.pack();
            fDiag.setLocationRelativeTo(owner);
            fDiag.setVisible(true);
        }

        public void valueChanged(ListSelectionEvent e) {
            if (filterList.getSelectedIndex() >= 0) {
                removeButton.setEnabled(true);
                editButton.setEnabled(true);
            } else {
                removeButton.setEnabled(false);
                editButton.setEnabled(false);
            }
        }
        
        public void saveSettings() {
            PrefManager.get().setFilters((DefaultListModel) filterList.getModel());
        }
    }

    //Must be called from the event-dispatching thread.
    public void resetFocus() {
    }
}
