/*
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
 */

package net.java.dev.tda.visualvm;

import com.pironet.tda.TDA;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.graalvm.visualvm.core.datasource.DataSource;
import org.graalvm.visualvm.core.snapshot.Snapshot;
import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.components.DataViewComponent;
import org.openide.util.*;

/**
 * tda main display view for visualvm.
 * 
 * @author Ingo Rockel <mailto:irockel@dev.java.net>
 */
public class TDAView extends DataSourceView {
    private static final String IMAGE_PATH = "net/java/dev/tda/visualvm/resources/tda.png"; // NOI18N
    private Snapshot logContent;
    
    private JButton collapseAllButton = null;
    private JButton expandAllButton = null;
    private TDA tdaPanel = null;
    
    public TDAView(DataSource logContent) {
        super(logContent, "Thread Dump Analyzer", new ImageIcon(Utilities.loadImage(IMAGE_PATH, true)).getImage(), 60, false);

        this.logContent = (Snapshot) logContent;
    }

    @Override
    protected DataViewComponent createComponent() {
        tdaPanel = new TDA(false, logContent.getFile().getAbsolutePath());
        tdaPanel.init(true, true);
        tdaPanel.initDumpDisplay(null);
            
        tdaPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JPanel viewPanel = createView();
        
        DataViewComponent dvc = new DataViewComponent(new DataViewComponent.MasterView(NbBundle.getMessage(TDAView.class, 
                "MSG_Dump"), null, viewPanel), 
                new DataViewComponent.MasterViewConfiguration(false));
        
        dvc.configureDetailsArea(new DataViewComponent.DetailsAreaConfiguration(NbBundle.getMessage(TDAView.class, 
                "LBL_Dump_results"), false), DataViewComponent.TOP_LEFT);   // NOI18N
        
        dvc.addDetailsView(new DataViewComponent.DetailsView(NbBundle.getMessage(TDAView.class, 
                "MSG_Dump_results"), null, 10, tdaPanel, null), DataViewComponent.TOP_LEFT);
        return(dvc);
    }
    
    /**
     * add given file to existing tda panel.
     * @param file the file string path to add.
     */
    public void addToTDA(String file) {
        tdaPanel.addDumpFile(file);
    }
    
    private JPanel createView() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 3, 0));
        
        collapseAllButton = new JButton(NbBundle.getMessage(TDAView.class, "LBL_CollapseTree"), TDA.createImageIcon("Collapsed.gif"));
        collapseAllButton.addActionListener((ActionEvent e) -> {
            tdaPanel.expandAllDumpNodes(false);
        });

        expandAllButton = new JButton(NbBundle.getMessage(TDAView.class, "LBL_ExpandTree"), TDA.createImageIcon("Expanded.gif"));
        expandAllButton.addActionListener((ActionEvent e) -> {
            tdaPanel.expandAllDumpNodes(true);
        });
        
        buttonPanel.add(new JLabel("<html><body><b>Dump Actions:"));
        buttonPanel.add(collapseAllButton);
        buttonPanel.add(expandAllButton);
                
        return(buttonPanel);
    }
    
}
