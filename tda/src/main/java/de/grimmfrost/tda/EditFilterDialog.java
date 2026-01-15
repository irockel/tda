/*
 * EditFilterDialog.java
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
 * $Id: EditFilterDialog.java,v 1.12 2008-04-30 09:03:33 irockel Exp $
 */

package de.grimmfrost.tda;

import de.grimmfrost.tda.filter.Filter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * dialog for editing filters.
 * @author irockel
 */
public class EditFilterDialog extends JDialog {
    private SettingsPanel settingsPanel;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;
    private Frame frame;
    private JList filterList;
    private boolean isAdd = false;
    
    /**
     * Creates a new instance of PreferencesDialog
     */
    public EditFilterDialog(Frame owner, String frameTitle, JList filterList, boolean isAdd) {
        super(owner, frameTitle, true);
        try {
            setIconImage(TDA.createImageIcon("Filters.png").getImage());
        } catch (NoSuchMethodError nsme) {
            // ignore, for 1.4 backward compatibility
        }
        
        this.isAdd = isAdd;
        this.filterList = filterList;
        frame = owner;
        getContentPane().setLayout(new BorderLayout());
        initPanel();
    }

    private void initPanel() {
        settingsPanel = new SettingsPanel(!isAdd ? (Filter) filterList.getSelectedValue() : null);
        getContentPane().add(settingsPanel,BorderLayout.CENTER);
        okButton = new JButton("Ok");
        cancelButton = new JButton("Cancel");
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(frame != null) {
                    frame.setEnabled(true);
                }
                if(!isAdd) {
                    Filter filter = (Filter) filterList.getModel().getElementAt(filterList.getSelectedIndex());
                    applyFilter(filter);
                    // reset to fire change event.
                    ((DefaultListModel) filterList.getModel()).setElementAt(filter, filterList.getSelectedIndex());
                    
                } else {
                    Filter filter = new Filter();
                    applyFilter(filter);
                    addToList(filter);
                }
                dispose();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(frame != null) {
                    frame.setEnabled(true);
                }
                dispose();
            }
        });
        reset();
    }
    
    private void applyFilter(Filter filter) {
        filter.setName(settingsPanel.filterName.getText());
        filter.setFilterExpression(settingsPanel.regEx.getText());
        filter.setFilterRule(settingsPanel.filterRule.getSelectedIndex());
        filter.setGeneralFilter(true);
        filter.setExclusionFilter(settingsPanel.isExclusionFilter.isSelected());
        filter.setEnabled(settingsPanel.isEnabled.isSelected());
    }
    
    private void addToList(Filter filter) {
        DefaultListModel dlm = ((DefaultListModel) filterList.getModel());
        
        dlm.ensureCapacity(dlm.getSize() +1);
        dlm.addElement(filter);
        filterList.ensureIndexIsVisible(dlm.getSize());
    }
                    
    public void reset() {
        getRootPane().setDefaultButton(okButton);
    }
    
    class SettingsPanel extends JPanel {
        JTextField filterName = null;
        JTextField regEx = null;
        JCheckBox isExclusionFilter = null;
        JCheckBox isEnabled = null;
        JComboBox filterRule = null;
        
        public SettingsPanel(Filter presetFilter) {
            setLayout(new BorderLayout());
            FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
            JPanel innerSettingsPanel = new JPanel(fl);
            
            filterName = new JTextField(30);
            innerSettingsPanel.add(new JLabel("Filter Name"));
            innerSettingsPanel.add(filterName);
            add(innerSettingsPanel, BorderLayout.NORTH);
            
            innerSettingsPanel = new JPanel(fl);
            regEx = new JTextField(30);
            innerSettingsPanel.add(new JLabel("Match Expression"));
            innerSettingsPanel.add(regEx);
            add(innerSettingsPanel, BorderLayout.CENTER);
            
            innerSettingsPanel = new JPanel(new BorderLayout());
            JPanel innerInnerSettingsPanel = new JPanel(fl);
            innerInnerSettingsPanel.add(new JLabel("Filter rule"));
            innerInnerSettingsPanel.add(filterRule = new JComboBox(new String[] {"has in title", "matches title", "has in stack",
               "matches stack", "waiting on", "waiting for", "locking", "sleeping", "stack line count >"}));
            innerSettingsPanel.add(innerInnerSettingsPanel, BorderLayout.NORTH);
            
            innerInnerSettingsPanel = new JPanel(fl);
            innerInnerSettingsPanel.add(new JLabel("Filter is a exclusion filter"));
            innerInnerSettingsPanel.add(isExclusionFilter = new JCheckBox());
            innerSettingsPanel.add(innerInnerSettingsPanel, BorderLayout.CENTER);
            
            innerInnerSettingsPanel = new JPanel(fl);
            innerInnerSettingsPanel.add(new JLabel("Is Filter enabled in default categories"));
            innerInnerSettingsPanel.add(isEnabled = new JCheckBox());
            innerSettingsPanel.add(innerInnerSettingsPanel, BorderLayout.SOUTH);
            
            add(innerSettingsPanel, BorderLayout.SOUTH);
            
            if(presetFilter != null) {
                fillFilterData(presetFilter);
            }
            
        }
        
        /**
         * fill the dialog with the preset filter data.
         */
        private void fillFilterData(Filter presetFilter) {
            filterName.setText(presetFilter.getName());
            regEx.setText(presetFilter.getFilterExpression());
            filterRule.setSelectedIndex(presetFilter.getFilterRule());
            isExclusionFilter.setSelected(presetFilter.isExclusionFilter());
            isEnabled.setSelected(presetFilter.isEnabled());
        }

        /**
         * return the set up data as filter
         * @return filte with filled in data.
         */
        public Filter getAsFilter() {
            Filter newFilter = new Filter(filterName.getText(),
                    regEx.getText(), filterRule.getSelectedIndex(), true, 
                    isExclusionFilter.isSelected(), isEnabled.isSelected());
            return newFilter;
        }
    }
}

