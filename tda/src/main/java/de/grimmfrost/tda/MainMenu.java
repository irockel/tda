/*
 * MainMenu.java
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
 * $Id: MainMenu.java,v 1.38 2008-09-18 14:44:10 irockel Exp $
 */

package de.grimmfrost.tda;

import de.grimmfrost.tda.utils.PrefManager;
import de.grimmfrost.tda.utils.ResourceManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

/**
 * provides instances of the main menu (though there is typically only one).
 *
 * @author irockel
 */
public class MainMenu extends JMenuBar {
    
    private JMenuItem closeMenuItem;
    private JMenuItem longMenuItem;
    private JMenuItem recentFilesMenu;
    private JMenuItem recentSessionsMenu;
    private JMenuItem closeAllMenuItem;
    private JMenuItem expandAllMenuItem;
    private JMenuItem collapseAllMenuItem;
    
    private TDA listener;
    private JToolBar toolBar;
    private JButton closeToolBarButton;
    private JMenuItem saveSessionMenuItem;
    private JButton findLRThreadsButton;
    private JButton expandButton;
    private JButton collapseButton;



    /** 
     * Creates a new instance of the MainMenu 
     */
    public MainMenu(TDA listener) {
        this.listener = listener;
        createMenuBar();
    }
    
    /**
     * get the close file menu item
     */
    public JMenuItem getCloseMenuItem() {
        return(closeMenuItem);
    }
    
    /**
     * get the close file menu item
     */
    public JButton getCloseToolBarButton() {
        return(closeToolBarButton);
    }
    
    /**
     * get the close file menu item
     */
    public JButton getExpandButton() {
        return(expandButton);
    }
    
    /**
     * get the close file menu item
     */
    public JButton getCollapseButton() {
        return(collapseButton);
    }
    
    /**
     * get the close file menu item
     */
    public JButton getFindLRThreadsToolBarButton() {
        return(findLRThreadsButton);
    }
    
    /**
     * get the close all file menu item
     */
    public JMenuItem getCloseAllMenuItem() {
        return(closeAllMenuItem);
    }
    
    public JMenuItem getLongMenuItem() {
        return(longMenuItem);
    }
    
    /**
     * get the close all file menu item
     */
    public JMenuItem getExpandAllMenuItem() {
        return(expandAllMenuItem);
    }
    
    public JMenuItem getCollapseAllMenuItem() {
        return(collapseAllMenuItem);
    }

    public JMenuItem getSaveSessionMenuItem() {
        return(saveSessionMenuItem);
    }
    
    /**
     * create the top level menu bar
     */
    private void createMenuBar() {
        add(createFileMenu());
        add(createViewMenu());
        add(createToolsMenu());
        add(createHelpMenu());
    }
    
    private JMenu createFileMenu() {
        JMenuItem menuItem;
        JMenu menu;
        //Build the first menu.
        menu = new JMenu(ResourceManager.translate("file.menu"));
        menu.setMnemonic(KeyStroke.getKeyStroke(ResourceManager.translate("file.menu.mnem")).getKeyCode());
        menu.getAccessibleContext().setAccessibleDescription(ResourceManager.translate("file.menu.description"));
        menu.addMenuListener(listener);
        
        //a group of JMenuItems
        menuItem = new JMenuItem(ResourceManager.translate("file.open"),
                KeyStroke.getKeyStroke(ResourceManager.translate("file.open.mnem")).getKeyCode());
        menuItem.setIcon(TDA.createImageIcon("FileOpen.png"));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                ResourceManager.translate("file.open.accel")));
        menuItem.getAccessibleContext().setAccessibleDescription(
                ResourceManager.translate("file.open.description"));
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        closeMenuItem = new JMenuItem(ResourceManager.translate("file.close"),
                KeyStroke.getKeyStroke(ResourceManager.translate("file.close.mnem")).getKeyCode());
        closeMenuItem.setIcon(TDA.createImageIcon("CloseFile.png"));
        closeMenuItem.getAccessibleContext().setAccessibleDescription(
                ResourceManager.translate("file.open.description"));
        closeMenuItem.addActionListener(listener);
        closeMenuItem.setEnabled(false);
        menu.add(closeMenuItem);
        
        closeAllMenuItem = new JMenuItem(ResourceManager.translate("file.closeall"),
                KeyStroke.getKeyStroke(ResourceManager.translate("file.closeall.mnem")).getKeyCode());
        closeAllMenuItem.getAccessibleContext().setAccessibleDescription(
                ResourceManager.translate("file.closeall.description"));
        closeAllMenuItem.addActionListener(listener);
        closeAllMenuItem.setEnabled(false);
        menu.add(closeAllMenuItem);
        
        createRecentFileMenu();
        menu.add(recentFilesMenu);
        
        menu.addSeparator();
        menuItem = new JMenuItem(ResourceManager.translate("file.getfromclipboard"),
                KeyStroke.getKeyStroke(ResourceManager.translate("file.getfromclipboard.mnem")).getKeyCode());
        menuItem.setIcon(TDA.createImageIcon("Empty.gif"));
        menuItem.getAccessibleContext().setAccessibleDescription(
                ResourceManager.translate("file.getfromclipboard.description"));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                ResourceManager.translate("file.getfromclipboard.accel")));        
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menu.addSeparator();
        saveSessionMenuItem = new JMenuItem(ResourceManager.translate("file.savesession"),
                KeyStroke.getKeyStroke(ResourceManager.translate("file.savesession.mnem")).getKeyCode());
        saveSessionMenuItem.getAccessibleContext().setAccessibleDescription(
                ResourceManager.translate("file.savesession.description"));
        saveSessionMenuItem.addActionListener(listener);
        menu.add(saveSessionMenuItem);
        saveSessionMenuItem.setEnabled(false);
        
        menuItem = new JMenuItem(ResourceManager.translate("file.opensession"),
                KeyStroke.getKeyStroke(ResourceManager.translate("file.opensession.mnem")).getKeyCode());
        menuItem.getAccessibleContext().setAccessibleDescription(
                ResourceManager.translate("file.opensession.description"));
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        createRecentSessionsMenu();
        menu.add(recentSessionsMenu);
        
        menu.addSeparator();

        menuItem = new JMenuItem(ResourceManager.translate("file.exit"),
                KeyStroke.getKeyStroke(ResourceManager.translate("file.exit.mnem")).getKeyCode());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                ResourceManager.translate("file.exit.accel")));
        menuItem.getAccessibleContext().setAccessibleDescription(
                ResourceManager.translate("file.exit.description"));
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        return(menu);
        
    }

    /**
     * Build tools menu in the menu bar.
     */
    private JMenu createViewMenu() {
        JMenuItem menuItem;
        JMenu menu;
        menu = new JMenu(ResourceManager.translate("view.menu"));
        menu.setMnemonic(KeyStroke.getKeyStroke(ResourceManager.translate("view.menu.mnem")).getKeyCode());
        menu.getAccessibleContext().setAccessibleDescription(ResourceManager.translate("view.menu.description"));
        add(menu);
        
        expandAllMenuItem = new JMenuItem(ResourceManager.translate("view.expand"),
                KeyStroke.getKeyStroke(ResourceManager.translate("view.expand.mnem")).getKeyCode());
        expandAllMenuItem.setIcon(TDA.createImageIcon("Expanded.png"));
        expandAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                ResourceManager.translate("view.expand.accel")));
        expandAllMenuItem.getAccessibleContext().setAccessibleDescription(
                ResourceManager.translate("view.expand.description"));
        expandAllMenuItem.addActionListener(listener);
        expandAllMenuItem.setEnabled(false);
        menu.add(expandAllMenuItem);

        collapseAllMenuItem = new JMenuItem(ResourceManager.translate("view.collapse"),
                KeyStroke.getKeyStroke(ResourceManager.translate("view.collapse.mnem")).getKeyCode());
        collapseAllMenuItem.setIcon(TDA.createImageIcon("Collapsed.png"));
        collapseAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                ResourceManager.translate("view.collapse.accel")));
        collapseAllMenuItem.getAccessibleContext().setAccessibleDescription(
                ResourceManager.translate("view.collapse.description"));
        collapseAllMenuItem.setEnabled(false);
        collapseAllMenuItem.addActionListener(listener);
        menu.add(collapseAllMenuItem);
                
        menu.addSeparator();
        menuItem = new JCheckBoxMenuItem(ResourceManager.translate("view.showtoolbar"), PrefManager.get().getShowToolbar());
        menuItem.setMnemonic(KeyStroke.getKeyStroke(ResourceManager.translate("view.showtoolbar.mnem")).getKeyCode());
        menuItem.addActionListener(listener);
        menuItem.setIcon(TDA.createImageIcon("Empty.gif"));
        menu.add(menuItem);
        
        return(menu);
    }
    
    /**
     * Build tools menu in the menu bar.
     */
    private JMenu createToolsMenu() {
        JMenuItem menuItem;
        JMenu menu;
        menu = new JMenu(ResourceManager.translate("tools.menu"));
        menu.setMnemonic(KeyStroke.getKeyStroke(ResourceManager.translate("tools.menu.mnem")).getKeyCode());
        menu.getAccessibleContext().setAccessibleDescription(ResourceManager.translate("tools.menu.description"));
        add(menu);
        
        longMenuItem = new JMenuItem(ResourceManager.translate("tools.longrunning"),
                KeyStroke.getKeyStroke(ResourceManager.translate("tools.longrunning.mnem")).getKeyCode());
        longMenuItem.setIcon(TDA.createImageIcon("FindLRThreads.png"));
        longMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_L, ActionEvent.ALT_MASK));
        longMenuItem.getAccessibleContext().setAccessibleDescription(
                "Find long running threads...");
        longMenuItem.addActionListener(listener);
        longMenuItem.setEnabled(false);
        menu.add(longMenuItem);
        menu.addSeparator();

        menuItem = new JMenuItem("Filters",
                KeyEvent.VK_F);
        menuItem.setIcon(TDA.createImageIcon("Filters.png"));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_F, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Setup Filter");
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Categories",
                KeyEvent.VK_F);
        menuItem.setIcon(TDA.createImageIcon("CustomCat.png"));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_G, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Setup Categories");
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();

        menuItem = new JMenuItem(ResourceManager.translate("file.preferences"),
                KeyStroke.getKeyStroke(ResourceManager.translate("file.preferences.mnem")).getKeyCode());
        menuItem.setIcon(TDA.createImageIcon("Preferences.png"));
        menuItem.getAccessibleContext().setAccessibleDescription(
                ResourceManager.translate("file.preferences.description"));
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        /*menu.addSeparator();
        
        menuItem = new JMenuItem("Load Configuration Set...",
                KeyEvent.VK_F);
        menuItem.setIcon(TDA.createImageIcon("Empty.gif"));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Load Configuration Set");
        menuItem.addActionListener(listener);
        menu.add(menuItem);*/
        return(menu);
    }
    
    /**
     * Build help menu in the menu bar.
     */
    private JMenu createHelpMenu() {
        JMenuItem menuItem;
        JMenu menu;
        menu = new JMenu(ResourceManager.translate("help.menu"));
        menu.setMnemonic(KeyStroke.getKeyStroke(ResourceManager.translate("help.menu.mnem")).getKeyCode());
        menu.getAccessibleContext().setAccessibleDescription(
                ResourceManager.translate("help.menu.description"));
                
        menuItem = new JMenuItem("Release Notes",null);
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Release Notes");
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuItem = new JMenuItem("License",null);
        menuItem.getAccessibleContext().setAccessibleDescription(
                "TDA Distribution License");
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menu.addSeparator();
        menuItem = new JMenuItem("About TDA",
                KeyEvent.VK_A);
        menuItem.setIcon(TDA.createImageIcon("About.png"));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "About Thread Dump Analyzer");
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        return(menu);
    }
    
    /**
     * create the menu for opening recently selected files.
     */
    private void createRecentFileMenu() {
        String[] recentFiles = PrefManager.get().getRecentFiles();
        
        recentFilesMenu = new JMenu(ResourceManager.translate("file.recentfiles"));
        recentFilesMenu.setMnemonic(KeyStroke.getKeyStroke(ResourceManager.translate("file.recentfiles.mnem")).getKeyCode());
        if(recentFiles.length > 1) {
            for(int i = 1; i < recentFiles.length; i++) {
                if(!recentFiles[i].equals("")) {
                    JMenuItem item = new JMenuItem(recentFiles[i]);
                    ((JMenu) recentFilesMenu).add(item);
                    item.addActionListener(listener);
                }
            }
        } else {
            recentFilesMenu.setEnabled(false);
        }
    }
    
    /**
     * create the menu for opening recently selected files.
     */
    private void createRecentSessionsMenu() {
        String[] recentFiles = PrefManager.get().getRecentSessions();
        
        recentSessionsMenu = new JMenu(ResourceManager.translate("file.recentsessions"));
        recentSessionsMenu.setMnemonic(KeyStroke.getKeyStroke(ResourceManager.translate("file.recentsessions.mnem")).getKeyCode());
        if(recentFiles.length > 1) {
            
            for(int i = 1; i < recentFiles.length; i++) {
                if(!recentFiles[i].equals("")) {
                    JMenuItem item = new JMenuItem(recentFiles[i]);
                    ((JMenu) recentSessionsMenu).add(item);
                    item.addActionListener(listener);
                }
            }
        } else {
            recentSessionsMenu.setEnabled(false);
        }
    }

    /**
     * creates and returns a toolbar for the main menu with most
     * important entries.
     * @return toolbar instance, is created on demand.
     */
    public JToolBar getToolBar() {
        if(toolBar == null) {
            createToolBar();
        }
        return toolBar;
    }
    
    /**
     * create a toolbar showing the most important main menu entries.
     */
    private void createToolBar() {
        toolBar = new JToolBar("TDA Toolbar");
        if(listener.runningAsJConsolePlugin || listener.runningAsVisualVMPlugin) {
            toolBar.add(createToolBarButton("Request a Thread Dump", "FileOpen.png"));
            toolBar.setFloatable(false);
        } else {
            toolBar.add(createToolBarButton("Open Logfile", "FileOpen.png"));
            closeToolBarButton = createToolBarButton("Close selected Logfile", "CloseFile.png");
            closeToolBarButton.setEnabled(false);
            toolBar.add(closeToolBarButton);
        }
        toolBar.addSeparator();
        toolBar.add(createToolBarButton("Preferences", "Preferences.png"));
        toolBar.addSeparator();
        expandButton = createToolBarButton("Expand all nodes", "Expanded.png");
        expandButton.setEnabled(false);
        toolBar.add(expandButton);
        collapseButton = createToolBarButton("Collapse all nodes", "Collapsed.png");
        collapseButton.setEnabled(false);
        toolBar.add(collapseButton);
        toolBar.addSeparator();
        findLRThreadsButton = createToolBarButton("Find long running threads", "FindLRThreads.png");
        findLRThreadsButton.setEnabled(false);
        toolBar.add(findLRThreadsButton);
        
        toolBar.add(createToolBarButton("Filters", "Filters.png"));
        toolBar.add(createToolBarButton("Custom Categories", "CustomCat.png"));
        toolBar.addSeparator();
    }
    
    /**
     * create a toolbar button with tooltip and given icon.
     * @param text tooltip text
     * @param fileName filename for the icon to load
     * @return toolbar button
     */
    private JButton createToolBarButton(String text, String fileName) {
        JButton toolbarButton = new JButton(TDA.createImageIcon(fileName));
        if(text != null) {
            toolbarButton.setToolTipText(text);
        }
        toolbarButton.addActionListener(listener);
        toolbarButton.setFocusable(false);
        return(toolbarButton);
    }
}
