/**
 * Thread Dump Analysis Tool, parses Thread Dump input and displays it as tree
 *
 * This file is part of TDA - Thread Dump Analysis Tool.
 *
 * TDA is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * TDA is distributed in the hope that it will be useful,h
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * TDA should have received a copy of the Lesser GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package com.pironet.tda;

import com.pironet.tda.jconsole.MBeanDumper;
import com.pironet.tda.utils.AppInfo;
import com.pironet.tda.utils.Browser;
import com.pironet.tda.utils.HistogramTableModel;
import com.pironet.tda.utils.MonitorComparator;
import com.pironet.tda.utils.PrefManager;
import com.pironet.tda.utils.StatusBar;
import com.pironet.tda.utils.SwingWorker;
import com.pironet.tda.utils.TableSorter;
import com.pironet.tda.utils.ThreadsTableModel;
import com.pironet.tda.utils.ThreadsTableSelectionModel;
import com.pironet.tda.utils.TipOfDay;
import com.pironet.tda.utils.TreeRenderer;
import com.pironet.tda.utils.ViewScrollPane;
import com.pironet.tda.utils.jedit.JEditTextArea;
import com.pironet.tda.utils.jedit.PopupMenu;

import java.awt.*;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.FileNotFoundException;
import java.util.*;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import javax.swing.JTree;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.io.IOException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * main class of the Thread Dump Analyzer. Start using static main method.
 *
 * @author irockel
 */
public class TDA extends JPanel implements ListSelectionListener, TreeSelectionListener, ActionListener, MenuListener {
    private static FileDialog fc;
    private static JFileChooser sessionFc;
    private static int DIVIDER_SIZE = 4;
    protected static JFrame frame;
    
    private static String dumpFile;
    
    private static String loggcFile;
    
    private static int fontSizeModifier = 0;

    private JEditorPane htmlPane;
    private JEditTextArea jeditPane;
    protected JTree tree;
    protected DefaultTreeModel treeModel;
    private JSplitPane splitPane;
    protected JSplitPane topSplitPane;
    private DumpStore dumpStore;
    private Vector topNodes;
    private ViewScrollPane htmlView;
    private ViewScrollPane tableView;
    private ViewScrollPane dumpView;
    private JTextField filter;
    private JCheckBox checkCase;
    private PreferencesDialog prefsDialog;
    private FilterDialog filterDialog;
    private CustomCategoriesDialog categoriesDialog;
    private JTable histogramTable;
    private JMenuItem showDumpMenuItem;
    boolean runningAsJConsolePlugin;
    boolean runningAsVisualVMPlugin;
    private DefaultMutableTreeNode logFile;
    private MBeanDumper mBeanDumper;
    private MainMenu pluginMainMenu;
    private boolean isFoundClassHistogram = false;
    private DropTarget dt = null;
    private DropTarget hdt = null;
    private int dumpCounter;

    
    private StatusBar statusBar;
    
    private SearchDialog searchDialog;
    
    /**
     * constructor (needs to be public for plugin)
     */
    public TDA(boolean setLF) {
        super(new BorderLayout());
        
        if(setLF) {
           // init L&F
           setupLookAndFeel();        
        }
    }

    /**
     * constructor (needs to be public for plugin)
     */
    public TDA(boolean setLF, MBeanDumper mBeanDumper) {
        this(setLF);
        
        this.mBeanDumper = mBeanDumper;
    }
    
    public TDA(boolean setLF, String dumpFile) {
        this(setLF);
        TDA.dumpFile = dumpFile;
    }

    /**
     * initializes tda panel
     * @param asJConsolePlugin specifies if tda is running as jconsole plugin
     * @param asVisualVMPlugin specifies if tda is running as visualvm plugin
     */
    public void init(boolean asJConsolePlugin, boolean asVisualVMPlugin) {
        // init everything
        tree = new JTree();
        addTreeListener(tree);
        runningAsJConsolePlugin = asJConsolePlugin;
        runningAsVisualVMPlugin = asVisualVMPlugin;
        
        //Create the HTML viewing pane.
        if(!this.runningAsVisualVMPlugin && !this.runningAsJConsolePlugin) {
            InputStream is = TDA.class.getResourceAsStream("doc/welcome.html");

            htmlPane = new JEditorPane();
            String welcomeText = parseWelcomeURL(is);
            htmlPane.setContentType("text/html");
            htmlPane.setText(welcomeText);
        } else if(asJConsolePlugin) {
            htmlPane = new JEditorPane("text/html", "<html><body bgcolor=\"ffffff\"><i>Press Button above to request a thread dump.</i></body></html>");
        } else {
            htmlPane = new JEditorPane("text/html", "<html><body bgcolor=\"ffffff\"></body></html>");
        }
        htmlPane.setEditable(false);
        
        if(!asJConsolePlugin && !asVisualVMPlugin) {
            hdt = new DropTarget(htmlPane, new FileDropTargetListener());
        }
        
        JEditorPane emptyPane = new JEditorPane("text/html", "");
        emptyPane.setEditable(false);
        
        htmlPane.addHyperlinkListener(
                new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                // if a link was clicked
                if(evt.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
                    if(evt.getDescription().startsWith("monitor")) {
                        navigateToMonitor(evt.getDescription());
                    } else if(evt.getDescription().startsWith("dump")) {
                        navigateToDump();
                    } else if(evt.getDescription().startsWith("wait")) {
                        navigateToChild("Threads waiting");
                    } else if(evt.getDescription().startsWith("sleep")) {
                        navigateToChild("Threads sleeping");
                    } else if(evt.getDescription().startsWith("dead")) {
                        navigateToChild("Deadlocks");
                    } else if(evt.getDescription().startsWith("threaddump")) {
                        addMXBeanDump();
                    } else if(evt.getDescription().startsWith("openlogfile") && !evt.getDescription().endsWith("//")) {
                        File[] files = { new File(evt.getDescription().substring(14)) };
                        openFiles(files, false);
                    } else if(evt.getDescription().startsWith("openlogfile")) {
                        chooseFile();
                    } else if(evt.getDescription().startsWith("opensession") && !evt.getDescription().endsWith("//")) {
                        File file = new File(evt.getDescription().substring(14));
                        openSession(file, true);
                    } else if(evt.getDescription().startsWith("opensession")) {
                        openSession();
                    } else if(evt.getDescription().startsWith("preferences")) {
                        showPreferencesDialog();
                    } else if(evt.getDescription().startsWith("filters")) {
                        showFilterDialog();
                    } else if(evt.getDescription().startsWith("categories")) {
                        showCategoriesDialog();
                    } else if(evt.getURL() != null) {
                        try {
                            // launch a browser with the appropriate URL
                            Browser.open(evt.getURL().toString());
                        } catch(InterruptedException e) {
                            System.out.println("Error launching external browser.");
                        } catch(IOException e) {
                            System.out.println("I/O error launching external browser." + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }

        });

        htmlView = new ViewScrollPane(htmlPane, runningAsVisualVMPlugin);
        ViewScrollPane emptyView = new ViewScrollPane(emptyPane, runningAsVisualVMPlugin);
        
        // create the top split pane
        topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topSplitPane.setLeftComponent(emptyView);
        topSplitPane.setDividerSize(DIVIDER_SIZE);
        topSplitPane.setContinuousLayout(true);
        
        //Add the scroll panes to a split pane.
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBottomComponent(htmlView);
        splitPane.setTopComponent(topSplitPane);
        splitPane.setDividerSize(DIVIDER_SIZE);
        splitPane.setContinuousLayout(true);
        
        splitPane.setForeground(Color.red);
        
        if(this.runningAsVisualVMPlugin) {
            setOpaque(true);
            setBackground(Color.WHITE);            
            setBorder(BorderFactory.createEmptyBorder(6, 0, 3, 0));
            topSplitPane.setBorder(BorderFactory.createEmptyBorder());
            topSplitPane.setOpaque(false);
            topSplitPane.setBackground(Color.WHITE);
            htmlPane.setBorder(BorderFactory.createEmptyBorder());
            htmlPane.setOpaque(false);
            htmlPane.setBackground(Color.WHITE);
            splitPane.setBorder(BorderFactory.createEmptyBorder());
            splitPane.setOpaque(false);
            splitPane.setBackground(Color.WHITE);
        }
        
        Dimension minimumSize = new Dimension(200, 50);
        htmlView.setMinimumSize(minimumSize);
        emptyView.setMinimumSize(minimumSize);
        
        //Add the split pane to this panel.
        add(htmlView, BorderLayout.CENTER);
        
        statusBar = new StatusBar(!(asJConsolePlugin || asVisualVMPlugin));
        add(statusBar, BorderLayout.SOUTH);

        firstFile = true;
        setFileOpen(false);
        
        if(!runningAsVisualVMPlugin) {
            setShowToolbar(PrefManager.get().getShowToolbar());        
        }
        
        if(firstFile && runningAsVisualVMPlugin) {
            // init filechooser
            fc = new FileDialog(getFrame());
            fc.setMultipleMode(true);
            try {
                fc.setDirectory(PrefManager.get().getSelectedPath().getCanonicalPath());
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (!runningAsJConsolePlugin && !runningAsVisualVMPlugin) {
            new DropTarget(htmlPane, DnDConstants.ACTION_REFERENCE | DnDConstants.ACTION_LINK, getDropTargetListener());
        }
    }

    private DropTargetListener getDropTargetListener() {
        return new DropTargetListener() {
                public void dragEnter(DropTargetDragEvent event) {
                }

                public void dragOver(DropTargetDragEvent event) {
                }

                public void dropActionChanged(DropTargetDragEvent event) {
                }

                public void dragExit(DropTargetEvent event) {
                }

                public void drop(DropTargetDropEvent event) {
                    try {
                        event.acceptDrop(DnDConstants.ACTION_REFERENCE);
                        Transferable transfer = event.getTransferable();
                        File[] files = getAcceptedFiles(transfer);
                        if (null != files && files.length != 0) {
                            openFiles(files, false);
                            event.getDropTargetContext().dropComplete(true);
                        }
                    } catch (InvalidDnDOperationException ex) {
                        // ignore
                    }
                }
            };
    }

    private File[] getAcceptedFiles(Transferable transferable) {
        try {
            if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return null;
            }

            List filelist = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            List acceptedFiles = new ArrayList();
            File[] files = (File[]) filelist.toArray(new File[0]);
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isDirectory()) {
                    acceptedFiles.add(files[i]);
                }
            }
            return (File[]) acceptedFiles.toArray(new File[0]);
        } catch (IOException ex) {
            return null;
        } catch (UnsupportedFlavorException ex) {
            return null;
        }
    }

    private void getLogfileFromClipboard() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String text = null;
    
        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                text = (String)t.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        if(text != null) {
            if(topNodes == null) {
                initDumpDisplay(text);
            } else {
                addDumpStream(new ByteArrayInputStream(text.getBytes()), "Clipboard at " + new Date(System.currentTimeMillis()), false);
                addToLogfile(text);
                if (this.getRootPane() != null) {
                    this.getRootPane().revalidate();
                }
                displayContent(null);
            }            

            if (!this.runningAsVisualVMPlugin) {
                getMainMenu().getFindLRThreadsToolBarButton().setEnabled(true);
                getMainMenu().getExpandButton().setEnabled(true);
                getMainMenu().getCollapseButton().setEnabled(true);
            }
        }
    }
    
    private String parseWelcomeURL(InputStream is) {
        BufferedReader br = null;
        String resultString = null;
        
        StringBuffer result = new StringBuffer();
        
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while(br.ready()) {
                result.append(br.readLine());
                result.append("\n");
            }
            resultString = result.toString();
            resultString = resultString.replaceFirst("./important.png", TDA.class.getResource("doc/important.png").toString());
            resultString = resultString.replaceFirst("./fileopen.png", TDA.class.getResource("doc/fileopen.png").toString());
            resultString = resultString.replaceFirst("./settings.png", TDA.class.getResource("doc/settings.png").toString());
            resultString = resultString.replaceFirst("./help.png", TDA.class.getResource("doc/help.png").toString());
            resultString = resultString.replaceFirst("<!-- ##tipofday## -->", TipOfDay.getTipOfDay());
            resultString = resultString.replaceFirst("<!-- ##recentlogfiles## -->", getAsTable("openlogfile://", PrefManager.get().getRecentFiles()));
            resultString = resultString.replaceFirst("<!-- ##recentsessions## -->", getAsTable("opensession://", PrefManager.get().getRecentSessions()));
        } catch (IllegalArgumentException ex) {
            // hack to prevent crashing of the app because off unparsed replacer.
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if(br != null) {
                    br.close();
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        // remove unparsed replacers.
        resultString = resultString.replaceFirst("<!-- ##tipofday## -->", "");
        resultString = resultString.replaceFirst("<!-- ##recentlogfiles## -->", "");
        resultString = resultString.replaceFirst("<!-- ##recentsessions## -->", "");
        return(resultString);
    }

    /**
     * convert the given elements into a href-table to be included into the
     * welcome page. Only last four elements are taken.
     * @param prefix link prefix to use
     * @param elements list of elements.
     * @return given elements as table.
     */
    private String getAsTable(String prefix, String[] elements) {
        StringBuffer result = new StringBuffer();
        int from = elements.length > 4 ? elements.length - 4 : 0;
        
        for(int i = from; i < elements.length; i++) {
            if(elements[i].trim().length() > 0) {
                // remove backslashes as they confuse the html display.
                String elem = elements[i].replaceAll("\\\\", "/");
                result.append("<tr><td width=\"20px\"></td><td><a href=\"");
                result.append(prefix);
                result.append(elem);
                result.append("\">");
                result.append(cutLink(elem, 80));
                result.append("</a></td></tr>\n");
            }
        }
        
        return(result.toString());
    }
    
    /**
     * cut the given link string to the specified length + three dots.
     * @param link
     * @param len
     * @return cut link or original link if link.length() <= len
     */
    private String cutLink(String link, int len) {
        if(link.length() > len) {
            String cut = link.substring(0, len / 2) +
                    "..." + link.substring(link.length() - (len / 2));
            return(cut);
        }
        
        return(link);
    }
    
    /**
     * request jmx dump
     */
    public LogFileContent addMXBeanDump() {
        String dump = mBeanDumper.threadDump();
        String locks = mBeanDumper.findDeadlock();
        
        // if deadlocks were found, append them to dump output.
        if(locks != null && !"".equals(locks)) {
            dump += "\n" + locks;
        }
        //System.out.println(dump);
        if(topNodes == null) {
            initDumpDisplay(null);
        }
        addDumpStream(new ByteArrayInputStream(dump.getBytes()), "Logfile", false);
        dumpCounter++;
        LogFileContent lfc = addToLogfile(dump);
        
        if(this.getRootPane() != null) {
            this.getRootPane().revalidate();
        }
        tree.setShowsRootHandles(false);
        displayContent(null);
        
        if(!this.runningAsVisualVMPlugin) {
            getMainMenu().getFindLRThreadsToolBarButton().setEnabled(true);
            getMainMenu().getExpandButton().setEnabled(true);
            getMainMenu().getCollapseButton().setEnabled(true);
        }
        return(lfc);
    }

    private LogFileContent addToLogfile(String dump) {
        ((LogFileContent) logFile.getUserObject()).appendToContentBuffer(dump);
        return(((LogFileContent) logFile.getUserObject()));
    }
    
    /**
     * create file filter for session files.
     * @return file filter instance.
     */
    private static FileFilter getSessionFilter() {
        FileFilter filter = new FileFilter() {

            public boolean accept(File arg0) {
                return(arg0 != null && (arg0.isDirectory() || arg0.getName().endsWith("tsf")));
            }

            public String getDescription() {
                return("TDA Session Files");
            }
            
        };
        return(filter);
    }
    
    /**
     * initializes session file chooser, if not already done.
     */
    private static void initSessionFc() {

        sessionFc = new JFileChooser();
        sessionFc.setMultiSelectionEnabled(true);
        sessionFc.setCurrentDirectory(PrefManager.get().getSelectedPath());
        if ((PrefManager.get().getPreferredSizeFileChooser().height > 0)) {
            sessionFc.setPreferredSize(PrefManager.get().getPreferredSizeFileChooser());
        }
        sessionFc.setFileFilter(getSessionFilter());
            
        sessionFc.setSelectedFile(null);
    }
    
    /**
     * expand all dump nodes in the root tree
     * @param expand true=expand, false=collapse.
     */
    public void expandAllDumpNodes(boolean expand) {
        TreeNode root = (TreeNode)tree.getModel().getRoot();
        expandAll(tree, new TreePath(root), expand);
    }

    /**
     * expand all nodes of the currently selected category, only works for tree categories.
     */
    private void expandAllCatNodes(boolean expand) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        JTree catTree = (JTree) ((TreeCategory) node.getUserObject()).getCatComponent(this);
            if(expand) {
          for (int i = 0; i < catTree.getRowCount(); i++) {
                catTree.expandRow(i);
          }
            } else {
          for (int i = 0; i < catTree.getRowCount(); i++) {
                catTree.collapseRow(i);
            }
        }
    }

    /**
     * sort monitors by thread amount
     */
    private void sortCatByThreads() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        ((TreeCategory) node.getUserObject()).sort(new MonitorComparator());
        displayCategory(node.getUserObject());
    }
    
    /**
     * expand or collapse all nodes of the specified tree
     * @param tree the tree to expand all/collapse all
     * @param parent the parent to start with
     * @param expand expand=true, collapse=false
     */
    private void expandAll(JTree catTree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(catTree, path, expand);
            }
        }
    
        if(parent.getPathCount() > 1) {
            // Expansion or collapse must be done bottom-up
            if (expand) {
                catTree.expandPath(parent);
            } else {
                catTree.collapsePath(parent);
            }
        }
    }    
    
    private void saveSession() {
        initSessionFc();
        int returnVal = sessionFc.showSaveDialog(this.getRootPane());
        sessionFc.setPreferredSize(sessionFc.getSize());
        
        PrefManager.get().setPreferredSizeFileChooser(sessionFc.getSize());

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = sessionFc.getSelectedFile();
            // check if file has a suffix
            if(file.getName().indexOf(".") < 0) {
                file = new File(file.getAbsolutePath() + ".tsf");
            } 
            int selectValue = 0;
            if (file.exists()) {
                Object[] options = {"Overwrite", "Cancel"};
                selectValue = JOptionPane.showOptionDialog(null, "<html><body>File exists<br><b>" + file +
                        "</b></body></html>", "Confirm overwrite",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);
            }
            if (selectValue == 0) {
                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
                    
                    oos.writeObject(dumpFile);
                    oos.writeObject(topNodes);
                    oos.writeObject(dumpStore);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        oos.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                PrefManager.get().addToRecentSessions(file.getAbsolutePath());
            }
        }
    }
    
    
    
    private void openSession() {
        initSessionFc();
        
        int returnVal = sessionFc.showOpenDialog(this.getRootPane());
        sessionFc.setPreferredSize(sessionFc.getSize());
        PrefManager.get().setPreferredSizeFileChooser(sessionFc.getSize());

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = sessionFc.getSelectedFile();
            int selectValue = 0;
            if ((selectValue == 0) && (file.exists())) {
                openSession(file, false);
            }
        }
    }
    
    /**
     * open the specified session
     * @param file
     */
    private void openSession(File file, boolean isRecent) {
        try {
            loadSession(file, isRecent);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this.getRootPane(),
                    "Error opening " + ex.getMessage() + ".",
                    "Error opening session", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this.getRootPane(),
                    "Error opening " + ex.getMessage() + ".",
                    "Error opening session", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSession(File file, boolean isRecent) throws IOException {
        final ObjectInputStream ois = new ObjectInputStream(new ProgressMonitorInputStream(this, "Opening session " + file,
                new GZIPInputStream(new FileInputStream(file))));
        
        setFileOpen(true);
        firstFile = false;
        resetMainPanel();
        initDumpDisplay(null);
        
        final SwingWorker worker = new SwingWorker() {

                    public Object construct() {
                        synchronized (syncObject) {
                            try {
                                dumpFile = (String) ois.readObject();
                                topNodes = (Vector) ois.readObject();
                                dumpStore = (DumpStore) ois.readObject();
                                ois.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            } catch (ClassNotFoundException ex) {
                                ex.printStackTrace();
                            } finally {
                                try {
                                    ois.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            createTree();
                        }

                        return null;
                    }
                };
        worker.start();
        if (!isRecent) {
            PrefManager.get().addToRecentSessions(file.getAbsolutePath());
        }
    }
            
    private void setShowToolbar(boolean state) {
        if(state) {
            add(getMainMenu().getToolBar(), BorderLayout.PAGE_START);
        } else {
            remove(getMainMenu().getToolBar());
        }
        revalidate();
        PrefManager.get().setShowToolbar(state);
    }
    
    /**
     * tries the native look and feel on mac and windows and metal on unix (gtk still
     * isn't looking that nice, even in 1.6)
     */
    private void setupLookAndFeel() {
        try {
            //--- set the desired preconfigured plaf ---
            UIManager.LookAndFeelInfo currentLAFI = null;
            
            // retrieve plaf param.
            String plaf = "Mac,Windows,Metal";
            if(PrefManager.get().isUseGTKLF()) {
                plaf = "GTK,Mac,Windows,Metal";
            }
            
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TDA");
            
            // this line needs to be implemented in order to make L&F work properly
            UIManager.getLookAndFeelDefaults().put("ClassLoader", getClass().getClassLoader());
            
            // query list of L&Fs
            UIManager.LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
            
            if (!"".equals(plaf)) {
                
                String[] instPlafs = plaf.split(",");
                search:
                    for(int i = 0; i < instPlafs.length; i++) {
                    for(int j=0; j<plafs.length; j++) {
                        currentLAFI = plafs[j];
                        if(currentLAFI.getName().startsWith(instPlafs[i])) {
                            UIManager.setLookAndFeel(currentLAFI.getClassName());
                            break search;
                        }
                    }
                    }
            }
            
        } catch (Exception except) {
            except.printStackTrace();
        }
    }
    
    /**
     * init the basic display for showing dumps
     * 
     * @param content initial logfile content may also be parsed, can also be null.
     *        only used for clipboard operations.
     */
    public void initDumpDisplay(String content) {
        // clear tree
        dumpStore = new DumpStore();

        topNodes = new Vector();
        if(!runningAsJConsolePlugin && !runningAsVisualVMPlugin) {
            getMainMenu().getLongMenuItem().setEnabled(true);
            getMainMenu().getSaveSessionMenuItem().setEnabled(true);
            getMainMenu().getExpandButton().setEnabled(true);
            getMainMenu().getCollapseButton().setEnabled(true);
            getMainMenu().getFindLRThreadsToolBarButton().setEnabled(true);
            getMainMenu().getCloseAllMenuItem().setEnabled(true);
            getMainMenu().getExpandAllMenuItem().setEnabled(true);
            getMainMenu().getCollapseAllMenuItem().setEnabled(true);
        }
        if(!runningAsJConsolePlugin || (dumpFile != null)) {
            if(dumpFile != null) {
                addDumpFile();
            } else if (content != null) {
                addDumpStream(new ByteArrayInputStream(content.getBytes()), "Clipboard at " + new Date(System.currentTimeMillis()), false);
                addToLogfile(content);
            }
        }
        if(runningAsJConsolePlugin || runningAsVisualVMPlugin || isFileOpen()) {
            if(topSplitPane.getDividerLocation() <= 0) {
                topSplitPane.setDividerLocation(200);
            }
            
            // change from html view to split pane
            remove(0);
            revalidate();
            htmlPane.setText("");
            splitPane.setBottomComponent(htmlView);
            add(splitPane, BorderLayout.CENTER);
            if(PrefManager.get().getDividerPos() > 0) {
                splitPane.setDividerLocation(PrefManager.get().getDividerPos());
            } else {
                // set default divider location
                splitPane.setDividerLocation(100);
            }
            revalidate();
        }
    }

    /**
     * add the set dumpFileStream to the tree
     */
    private void addDumpFile() {
        addDumpFile(dumpFile);
    }

    /**
     * add the set dumpFileStream to the tree
     */
    public void addDumpFile(String filePath) {
        String[] file = new String[1];
        file[0] = filePath;
        addDumpFiles(file);
    }
    
    private boolean isLogfileSizeOk(String fileName) {
        File file = new File(fileName);
        return(file.isFile() && ((PrefManager.get().getMaxLogfileSize() == 0) || 
                (file.length() <= (PrefManager.get().getMaxLogfileSize() * 1024))));
    }
    
    /**
     * sync object is needed to synchronize opening of multiple files.
     */
    private static Object syncObject = new Object();
    
    /**
     * add the set dumpFileStream to the tree
     */
    private void addDumpFiles(String[] files) {
        for(int i = 0; i < files.length; i++) {
            try {
                dumpCounter = 1;
                addDumpStream(new FileInputStream(files[i]), files[i], true);
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this.getRootPane(),
                        "Error opening "  + ex.getMessage() + ".",
                        "Error opening file", JOptionPane.ERROR_MESSAGE);
            }
        }        
    }
    
    private void addDumpStream(InputStream inputStream, String file, boolean withLogfile) {
        final InputStream parseFileStream = new ProgressMonitorInputStream(this, "Parsing " + file, inputStream);

        //Create the nodes.
        if(!runningAsJConsolePlugin || topNodes.size() == 0) {
            topNodes.add(new DefaultMutableTreeNode(new Logfile(file)));
        }
        final DefaultMutableTreeNode top = (DefaultMutableTreeNode) topNodes.get(topNodes.size()-1);
        
        if ((!withLogfile && logFile == null) || isLogfileSizeOk(file)) {
            logFile = new DefaultMutableTreeNode(new LogFileContent(file));
            if(!runningAsVisualVMPlugin) {
                top.add(logFile);
            }
        }
        setFileOpen(true);

        final SwingWorker worker = new SwingWorker() {

                    public Object construct() {
                        synchronized (syncObject) {
                            int divider = topSplitPane.getDividerLocation();
                            addThreadDumps(top, parseFileStream);
                            createTree();
                            tree.expandRow(1);

                            topSplitPane.setDividerLocation(divider);
                        }

                        return null;
                    }
                };
        worker.start();
    }
    
    protected void createTree() {
        //Create a tree that allows multiple selection at a time.
        if(topNodes.size() == 1) {
            treeModel = new DefaultTreeModel((DefaultMutableTreeNode) topNodes.get(0));
            tree = new JTree(treeModel);
            tree.setRootVisible(!runningAsJConsolePlugin && !runningAsVisualVMPlugin);
            addTreeListener(tree);
            if(!runningAsJConsolePlugin && !runningAsVisualVMPlugin) {
                frame.setTitle("TDA - Thread Dumps of " + dumpFile);
            }
        } else {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Thread Dump Nodes");
            treeModel = new DefaultTreeModel(root);
            for(int i = 0; i < topNodes.size(); i++) {
                root.add((DefaultMutableTreeNode) topNodes.get(i));
            }
            tree = new JTree(root);
            tree.setRootVisible(false);
            addTreeListener(tree);
            if(!runningAsJConsolePlugin && !runningAsVisualVMPlugin) {
                if(!frame.getTitle().endsWith("...")) {
                    frame.setTitle(frame.getTitle() + " ...");
                }
            }
        }
        
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
        tree.setCellRenderer(new TreeRenderer());
        
        //Create the scroll pane and add the tree to it.
        ViewScrollPane treeView = new ViewScrollPane(tree, runningAsVisualVMPlugin);
        
        topSplitPane.setLeftComponent(treeView);
        
        Dimension minimumSize = new Dimension(200, 50);
        treeView.setMinimumSize(minimumSize);
        
        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);
        
        if(!runningAsJConsolePlugin && !runningAsVisualVMPlugin) {
            dt = new DropTarget(tree, new FileDropTargetListener());
        }
        
        createPopupMenu();
        
    }
    
    /**
     * add a tree listener for enabling/disabling menu and toolbar icons.
     * @param tree
     */
    private void addTreeListener(JTree tree) {
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            ViewScrollPane emptyView = null;

            public void valueChanged(TreeSelectionEvent e) {
                getMainMenu().getCloseMenuItem().setEnabled(e.getPath() != null);
                if(getMainMenu().getCloseToolBarButton() != null) {
                    getMainMenu().getCloseToolBarButton().setEnabled(e.getPath() != null);
                }
                // reset right pane of the top view:
                
                if(emptyView == null) {
                    JEditorPane emptyPane = new JEditorPane("text/html", "<html><body bgcolor=\"ffffff\">   </body></html>");
                    emptyPane.setEditable(false);

                    emptyView = new ViewScrollPane(emptyPane, runningAsVisualVMPlugin);                    
                }

                if(e.getPath() == null || 
                        !(((DefaultMutableTreeNode) e.getPath().getLastPathComponent()).
                        getUserObject() instanceof Category)) {
                    resetPane();
                }
            }
            
            private void resetPane() {
                int dividerLocation = topSplitPane.getDividerLocation();
                topSplitPane.setRightComponent(emptyView);
                topSplitPane.setDividerLocation(dividerLocation);
            }
            
        });
    }


    private boolean threadDisplay = false;
    
    private void setThreadDisplay(boolean value) {
        threadDisplay = value;
        /*if(!value) {
            // clear thread pane
            topSplitPane.setRightComponent(null);
        }*/
    }
    
    public boolean isThreadDisplay() {
        return(threadDisplay);
    }
    
    /**
     * Required by TreeSelectionListener interface.
     */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        e.getPath().getLastPathComponent();
        
        if (node == null) {
            return;
        }
        
        Object nodeInfo = node.getUserObject();
        if (nodeInfo instanceof ThreadInfo) {
            displayThreadInfo(nodeInfo);
            setThreadDisplay(true);
        } else if (nodeInfo instanceof ThreadDumpInfo) {
            displayThreadDumpInfo(nodeInfo);
        } else if (nodeInfo instanceof HistogramInfo) {
            HistogramInfo tdi = (HistogramInfo)nodeInfo;
            displayTable((HistogramTableModel) tdi.content);
            setThreadDisplay(false);
        } else if(nodeInfo instanceof LogFileContent) {
            displayLogFileContent(nodeInfo);
        } else if (nodeInfo instanceof Logfile && ((String)((Logfile)nodeInfo).getContent()).startsWith("Thread Dumps")) {
            displayLogFile();
            setThreadDisplay(false);
        } else if (nodeInfo instanceof Category) {
            displayCategory(nodeInfo);
            setThreadDisplay(true);
        } else {
            setThreadDisplay(false);
            displayContent(null);
        }
    }
    
    /**
     * process table selection events (thread display)
     * @param e the event to process.
     */
    public void valueChanged(ListSelectionEvent e) {
        //displayCategory(e.getFirstIndex());
        ThreadsTableSelectionModel ttsm = (ThreadsTableSelectionModel) e.getSource();
        TableSorter ts = (TableSorter) ttsm.getTable().getModel();
        
        int[] rows = ttsm.getTable().getSelectedRows();
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < rows.length; i++) {
            appendThreadInfo(sb, ((ThreadsTableModel) ts.getTableModel()).
                    getInfoObjectAtRow(ts.modelIndex(rows[i])));
        }
        displayContent(sb.toString());
        setThreadDisplay(true);
    }
    
    private void displayThreadInfo(Object nodeInfo) {
        StringBuffer sb = new StringBuffer("");
        appendThreadInfo(sb, nodeInfo);
        displayContent(sb.toString());
    }

    private void appendThreadInfo(StringBuffer sb, Object nodeInfo) {
        ThreadInfo ti = (ThreadInfo)nodeInfo;
        if(ti.getInfo() != null) {
            sb.append(ti.getInfo());
            sb.append(ti.getContent());
        } else {
            sb.append(ti.getContent());
        }
    }
    
    /**
     * display thread dump information for the give node object.
     * @param nodeInfo
     */
    private void displayThreadDumpInfo(Object nodeInfo) {
        ThreadDumpInfo ti = (ThreadDumpInfo)nodeInfo;
        displayContent(ti.getOverview());
    } 

    private void displayLogFile() {
        if(splitPane.getBottomComponent() != htmlView) {
            splitPane.setBottomComponent(htmlView);
        }
        htmlPane.setContentType("text/html");
        htmlPane.setText("");
        htmlPane.setCaretPosition(0);
        threadDisplay = false;
        statusBar.setInfoText(AppInfo.getStatusBarInfo());
    }
    
    private void displayLogFileContent(Object nodeInfo) {
        int dividerLocation = splitPane.getDividerLocation();
        if(splitPane.getBottomComponent() != jeditPane) {
            if(jeditPane == null) {
                initJeditView();
            }
            splitPane.setBottomComponent(jeditPane);
        }
        
        LogFileContent lfc = (LogFileContent) nodeInfo;
        jeditPane.setText(lfc.getContent());
        jeditPane.setCaretPosition(0);
        splitPane.setDividerLocation(dividerLocation);
        statusBar.setInfoText(AppInfo.getStatusBarInfo());
    }
    
    /**
     * initialize the base components needed for the jedit view of the
     * log file
     */
    private void initJeditView() {
        jeditPane = new JEditTextArea();
        jeditPane.setEditable(false);
        jeditPane.setCaretVisible(false);
        jeditPane.setCaretBlinkEnabled(false);
        jeditPane.setRightClickPopup(new PopupMenu(jeditPane, this, runningAsVisualVMPlugin));
        jeditPane.getInputHandler().addKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), (ActionListener) jeditPane.getRightClickPopup());
        jeditPane.getInputHandler().addKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK), (ActionListener) jeditPane.getRightClickPopup());
    }
    
    /**
     * display selected category in upper right frame
     */
    private void displayCategory(Object nodeInfo) {
        Category cat = ((Category) nodeInfo);
        Dimension size = null;
        ((JScrollPane) topSplitPane.getLeftComponent()).setPreferredSize(topSplitPane.getLeftComponent().getSize());
        boolean needDividerPos = false;
        
        if(topSplitPane.getRightComponent() != null) {
            size = topSplitPane.getRightComponent().getSize();
        } else {
            needDividerPos = true;
        }
        setThreadDisplay(true);
        if(cat.getLastView() == null) {
            JComponent catComp = cat.getCatComponent(this);
            if(cat.getName().startsWith("Monitors") || cat.getName().startsWith("Threads blocked by Monitors")) {
                catComp.addMouseListener(getMonitorsPopupMenu());
            } else {
                catComp.addMouseListener(getCatPopupMenu());
            }
            dumpView = new ViewScrollPane(catComp, runningAsVisualVMPlugin);
            if(size != null) {
                dumpView.setPreferredSize(size);
            }
            
            topSplitPane.setRightComponent(dumpView);
            cat.setLastView(dumpView);
        } else {
            if(size != null) {
                cat.getLastView().setPreferredSize(size);
            }
            topSplitPane.setRightComponent(cat.getLastView());
        }
        
        if(cat.getCurrentlySelectedUserObject() != null) {
            displayThreadInfo(cat.getCurrentlySelectedUserObject());
        } else {
            displayContent(null);
        }
        if(needDividerPos) {
            topSplitPane.setDividerLocation(PrefManager.get().getTopDividerPos());
        }
        if (cat.howManyFiltered() > 0) {
            statusBar.setInfoText("Filtered " + cat.howManyFiltered() + " elements in this category. Showing remaining " + cat.showing() + " elements.");
        } else {
            statusBar.setInfoText(AppInfo.getStatusBarInfo());
        }
        
        displayContent(cat.getInfo());
    }
    
    private void displayContent(String text) {
        if(splitPane.getBottomComponent() != htmlView) {
            splitPane.setBottomComponent(htmlView);
        }
        if (text != null) {
            htmlPane.setContentType("text/html");
            htmlPane.setText(text);
            htmlPane.setCaretPosition(0);
            htmlPane.setForeground(Color.BLACK);
        } else {
            htmlPane.setText("");
        }
    }
    
    private void displayTable(HistogramTableModel htm) {
        setThreadDisplay(false);

        htm.setFilter("");
        htm.setShowHotspotClasses(PrefManager.get().getShowHotspotClasses());
        
        TableSorter ts = new TableSorter(htm);
        histogramTable = new JTable(ts);
        ts.setTableHeader(histogramTable.getTableHeader());
        histogramTable.getColumnModel().getColumn(0).setPreferredWidth(700);
        tableView = new ViewScrollPane(histogramTable, runningAsVisualVMPlugin);

        JPanel histogramView = new JPanel(new BorderLayout());
        JPanel histoStatView = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        Font font = new Font("SansSerif", Font.PLAIN, 10);
        JLabel infoLabel = new JLabel(NumberFormat.getInstance().format(htm.getRowCount()) + " classes and base types");
        infoLabel.setFont(font);
        histoStatView.add(infoLabel);
        infoLabel = new JLabel(NumberFormat.getInstance().format(htm.getBytes()) + " bytes");
        infoLabel.setFont(font);
        histoStatView.add(infoLabel);
        infoLabel = new JLabel(NumberFormat.getInstance().format(htm.getInstances()) + " live objects");
        infoLabel.setFont(font);
        histoStatView.add(infoLabel);
        if(htm.isOOM()) {
            infoLabel = new JLabel("<html><b>OutOfMemory found!</b>");
            infoLabel.setFont(font);
            histoStatView.add(infoLabel);
        }
        if(htm.isIncomplete()) {
            infoLabel = new JLabel("<html><b>Class Histogram is incomplete! (broken logfile?)</b>");
            infoLabel.setFont(font);
            histoStatView.add(infoLabel);
        }
        JPanel filterPanel = new JPanel(new FlowLayout());
        infoLabel = new JLabel("Filter-Expression");
        infoLabel.setFont(font);
        filterPanel.add(infoLabel);
        
        filter = new JTextField(30);
        filter.setFont(font);
        filter.addCaretListener(new FilterListener(htm));
        filterPanel.add(infoLabel);
        filterPanel.add(filter);
        checkCase = new JCheckBox();
        checkCase.addChangeListener(new CheckCaseListener(htm));
        infoLabel = new JLabel("Ignore Case");
        infoLabel.setFont(font);
        filterPanel.add(infoLabel);
        filterPanel.add(checkCase);
        histoStatView.add(filterPanel);
        histogramView.add(histoStatView, BorderLayout.SOUTH);
        histogramView.add(tableView, BorderLayout.CENTER);
        
        histogramView.setPreferredSize(splitPane.getBottomComponent().getSize());
        
        splitPane.setBottomComponent(histogramView);
    }

    private class FilterListener implements CaretListener {
        HistogramTableModel htm;
        String currentText = "";
        FilterListener(HistogramTableModel htm) {
            this.htm = htm;
        }
        
        public void caretUpdate(CaretEvent event) {
            if(!filter.getText().equals(currentText)) {
                htm.setFilter(filter.getText());
                histogramTable.revalidate();
            }
        }
    }
    
    private class CheckCaseListener implements ChangeListener {
        HistogramTableModel htm;
        
        CheckCaseListener(HistogramTableModel htm) {
            this.htm = htm;
        }
        
        public void stateChanged(ChangeEvent e) {
            htm.setIgnoreCase(checkCase.isSelected());
            histogramTable.revalidate();
        }
    }
    
    private void addThreadDumps(DefaultMutableTreeNode top, InputStream dumpFileStream) {
        DumpParser dp = null;
        try {
            String fileName = top.getUserObject().toString();
            Map dumpMap = null;
            if(runningAsJConsolePlugin || runningAsVisualVMPlugin) {
                dumpMap = dumpStore.getFromDumpFiles(fileName);
            }
            
            if(dumpMap == null) {
                dumpMap = new HashMap();
                dumpStore.addFileToDumpFiles(fileName, dumpMap);
            }
            dp = DumpParserFactory.get().getDumpParserForLogfile(dumpFileStream, dumpMap, runningAsJConsolePlugin, 
                    dumpCounter);
            ((Logfile) top.getUserObject()).setUsedParser(dp);
            
            while((dp != null) && dp.hasMoreDumps()) {
                top.add(dp.parseNext());
                if(!isFoundClassHistogram) {
                    isFoundClassHistogram = dp.isFoundClassHistograms();
                }
            }
        } finally {
            if(dp != null) {
                try {
                    dp.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * navigate to the currently selected dump in logfile
     */
    private void navigateToDumpInLogfile() {
        Object userObject = ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject();
        if(userObject instanceof ThreadDumpInfo) {
            ThreadDumpInfo ti = (ThreadDumpInfo) userObject;
            int lineNumber = ti.getLogLine();
            
            // find log file node.
            TreePath selPath = tree.getSelectionPath();
            while (selPath != null && !checkNameFromNode((DefaultMutableTreeNode) selPath.getLastPathComponent(), File.separator)) {

                selPath = selPath.getParentPath();
            }
            
            tree.setSelectionPath(selPath);
            tree.scrollPathToVisible(selPath);

            Enumeration childs = ((DefaultMutableTreeNode) selPath.getLastPathComponent()).children();
            boolean found = false;
            DefaultMutableTreeNode logfileContent = null;
            while(!found && childs.hasMoreElements()) {
                logfileContent = (DefaultMutableTreeNode) childs.nextElement();
                found = logfileContent.getUserObject() instanceof LogFileContent;
            }
            
            if(found) {
                TreePath monitorPath = new TreePath(logfileContent.getPath());
                tree.setSelectionPath(monitorPath);
                tree.scrollPathToVisible(monitorPath);
                displayLogFileContent(logfileContent.getUserObject());
                jeditPane.setFirstLine(lineNumber-1);
            }           
        }
    }
    
    /**
     * navigate to monitor
     * @param monitorLink the monitor link to navigate to
     */
    private void navigateToMonitor(String monitorLink) {
        String monitor = monitorLink.substring(monitorLink.lastIndexOf('/')+1);
        
        // find monitor node for this thread info
        DefaultMutableTreeNode dumpNode = null;
        if(monitorLink.indexOf("Dump No.") > 0) {
            dumpNode = getDumpRootNode(monitorLink.substring(monitorLink.indexOf('/')+1, monitorLink.lastIndexOf('/')),
                    (DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
        } else {
            dumpNode = getDumpRootNode((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
        }
        Enumeration childs = dumpNode.children();
        DefaultMutableTreeNode monitorNode = null;
        DefaultMutableTreeNode monitorWithoutLocksNode = null;
        while(childs.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) childs.nextElement();
            if(child.getUserObject() instanceof TreeCategory) {
                if (((TreeCategory) child.getUserObject()).getName().startsWith("Monitors (")) {
                    monitorNode = child;
                } else if (((TreeCategory) child.getUserObject()).getName().startsWith("Monitors without")) {
                    monitorWithoutLocksNode = child;
                }
            }
        }
        
        // highlight chosen monitor
        JTree searchTree = (JTree) ((TreeCategory) monitorNode.getUserObject()).getCatComponent(this);
        TreePath searchPath = searchTree.getNextMatch(monitor,0,Position.Bias.Forward);
        if((searchPath == null) && (monitorWithoutLocksNode != null)) {
            searchTree = (JTree) ((TreeCategory) monitorWithoutLocksNode.getUserObject()).getCatComponent(this);
            searchPath = searchTree.getNextMatch(monitor,0,Position.Bias.Forward);
            monitorNode = monitorWithoutLocksNode;
        }
            
        if(searchPath != null) {
            TreePath monitorPath = new TreePath(monitorNode.getPath());
            tree.setSelectionPath(monitorPath);
            tree.scrollPathToVisible(monitorPath);
            
            displayCategory(monitorNode.getUserObject());
            
            TreePath threadInMonitor = searchPath.pathByAddingChild(((DefaultMutableTreeNode)searchPath.getLastPathComponent()).getLastChild());
            searchTree.setSelectionPath(threadInMonitor);
            searchTree.scrollPathToVisible(searchPath);
            searchTree.setSelectionPath(searchPath);
        }
    }
    
    /**
     * navigate to root node of currently active dump
     */
    private void navigateToDump() {
        TreePath currentPath = tree.getSelectionPath();
        tree.setSelectionPath(currentPath.getParentPath());
        tree.scrollPathToVisible(currentPath.getParentPath());
    }

    /**
     * navigate to child of currently selected node with the given prefix in name
     * @param startsWith node name prefix (e.g. "Threads waiting")
     */
    private void navigateToChild(String startsWith) {
        TreePath currentPath = tree.getSelectionPath();
        DefaultMutableTreeNode dumpNode = (DefaultMutableTreeNode) currentPath.getLastPathComponent();
        Enumeration childs = dumpNode.children();
        
        TreePath searchPath = null;
        while((searchPath == null) && childs.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) childs.nextElement();
            String name = child.toString();
            if(name != null && name.startsWith(startsWith)) {
                searchPath = new TreePath(child.getPath());
            }
        }
        
        if(searchPath != null) {
            tree.makeVisible(searchPath);
            tree.setSelectionPath(searchPath);
            tree.scrollPathToVisible(searchPath);
        }
    }
    
    protected MainMenu getMainMenu() {
        if((frame != null) && (frame.getJMenuBar() != null)) {
            return((MainMenu) frame.getJMenuBar());
        } else {
            if(pluginMainMenu == null) {
                pluginMainMenu = new MainMenu(this);
            }
            return(pluginMainMenu);
        }
    }
    
    public void createPopupMenu() {
        JMenuItem menuItem;
        
        //Create the popup menu.
        JPopupMenu popup = new JPopupMenu();
        
        menuItem = new JMenuItem("Diff Selection");
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("Find long running threads...");
        menuItem.addActionListener(this);
        popup.add(menuItem);
        
        showDumpMenuItem = new JMenuItem("Show selected Dump in logfile");
        showDumpMenuItem.addActionListener(this);
        showDumpMenuItem.setEnabled(false);
        if(!runningAsJConsolePlugin && !runningAsVisualVMPlugin) {
            popup.addSeparator();
            menuItem = new JMenuItem("Parse loggc-logfile...");
            menuItem.addActionListener(this);
            if(!PrefManager.get().getForceLoggcLoading()) {
                menuItem.setEnabled(!isFoundClassHistogram);
            }
            popup.add(menuItem);
            
            menuItem = new JMenuItem("Close logfile...");
            menuItem.addActionListener(this);
            popup.add(menuItem);
            popup.addSeparator();
            popup.add(showDumpMenuItem);
        } else {
            popup.addSeparator();
            if(!runningAsVisualVMPlugin) {
                menuItem = new JMenuItem("Request Thread Dump...");
                menuItem.addActionListener(this);
                popup.add(menuItem);
                popup.addSeparator();
                menuItem = new JMenuItem("Preferences");
                menuItem.addActionListener(this);
                popup.add(menuItem);
                menuItem = new JMenuItem("Filters");
                menuItem.addActionListener(this);
                popup.add(menuItem);
                popup.addSeparator();
                menuItem = new JMenuItem("Save Logfile...");
                menuItem.addActionListener(this);
                popup.add(menuItem);
                popup.addSeparator();
                menuItem = new JCheckBoxMenuItem("Show Toolbar", PrefManager.get().getShowToolbar());
                menuItem.addActionListener(this);
                popup.add(menuItem);
                popup.addSeparator();
                menuItem = new JMenuItem("Help");
                menuItem.addActionListener(this);
                popup.add(menuItem);
                popup.addSeparator();
            }
            menuItem = new JMenuItem("About TDA");
            menuItem.addActionListener(this);
            popup.add(menuItem);
        }
        
        //Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new PopupListener(popup);
        tree.addMouseListener(popupListener);
    }
    
    private PopupListener catPopupListener = null;
    
    /**
     * create a instance of this menu for a category
     */
    private PopupListener getCatPopupMenu() {
        if(catPopupListener == null) {
            JMenuItem menuItem;
            
            //Create the popup menu.
            JPopupMenu popup = new JPopupMenu();
            
            menuItem = new JMenuItem("Search...");
            menuItem.addActionListener(this);
            popup.add(menuItem);
            
            //Add listener to the text area so the popup menu can come up.
            catPopupListener = new PopupListener(popup);
        }
        
        return(catPopupListener);
    }
    
    private PopupListener monitorsPopupListener = null;
    
    /**
     * create a instance of this menu for a category
     */
    private PopupListener getMonitorsPopupMenu() {
        if(monitorsPopupListener == null) {
            JMenuItem menuItem;
            
            //Create the popup menu.
            JPopupMenu popup = new JPopupMenu();
            
            menuItem = new JMenuItem("Search...");
            menuItem.addActionListener(this);
            popup.add(menuItem);
            popup.addSeparator();
            menuItem = new JMenuItem("Expand all nodes");
            menuItem.addActionListener(this);
            popup.add(menuItem);
            menuItem = new JMenuItem("Collapse all nodes");
            menuItem.addActionListener(this);
            popup.add(menuItem);
            popup.addSeparator();
            menuItem = new JMenuItem("Sort by thread count");
            menuItem.addActionListener(this);
            popup.add(menuItem);
            
            //Add listener to the text area so the popup menu can come up.
            monitorsPopupListener = new PopupListener(popup);
        }
        
        return(monitorsPopupListener);
    }

    class PopupListener extends MouseAdapter {
        JPopupMenu popup;
        
        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                        e.getX(), e.getY());
                showDumpMenuItem.setEnabled((tree.getSelectionPath() != null) 
                        && ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).
                        getUserObject() instanceof ThreadDumpInfo);
            }
        }
    }
        
    /**
     * check menu and button events.
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof JMenuItem) {
            JMenuItem source = (JMenuItem) (e.getSource());
            if (source.getText().substring(1).startsWith(":\\") || source.getText().startsWith("/")) {
                if(source.getText().endsWith(".tsf")) {
                    try {
                        loadSession(new File(source.getText()), true);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    dumpFile = source.getText();
                    openFiles(new File[]{new File(dumpFile)}, true);
                }
            } else if ("Open...".equals(source.getText())) {
                chooseFile();
            } else if ("Open loggc file...".equals(source.getText())) {
                openLoggcFile();
            } else if ("Save Logfile...".equals(source.getText())) {
                saveLogFile();
            } else if ("Save Session...".equals(source.getText())) {
                saveSession();
            } else if ("Open Session...".equals(source.getText())) {
                openSession();
            } else if ("Preferences".equals(source.getText())) {
                showPreferencesDialog();
            } else if ("Filters".equals(source.getText())) {
                showFilterDialog();
            } else if ("Categories".equals(source.getText())) {
                showCategoriesDialog();
            } else if ("Get Logfile from clipboard".equals(source.getText())) {
                getLogfileFromClipboard();
            } else if ("Exit TDA".equals(source.getText())) {
                saveState();
                frame.dispose();
            } else if ("Release Notes".equals(source.getText())) {
                showInfoFile("Release Notes", "doc/README", "Document.png");
            } else if ("License".equals(source.getText())) {
                showInfoFile("License Information", "doc/COPYING", "Document.png");
            } else if ("About TDA".equals(source.getText())) {
                showInfo();
            } else if ("Search...".equals(source.getText())) {
                showSearchDialog();
            } else if ("Parse loggc-logfile...".equals(source.getText())) {
                parseLoggcLogfile();
            } else if ("Find long running threads...".equals(source.getText())) {
                findLongRunningThreads();
            } else if (("Close logfile...".equals(source.getText())) || ("Close...".equals(source.getText()))) {
                closeCurrentDump();
            } else if ("Close all...".equals(source.getText())) {
                closeAllDumps();
            } else if ("Diff Selection".equals(source.getText())) {
                TreePath[] paths = tree.getSelectionPaths();
                if ((paths != null) && (paths.length < 2)) {
                    JOptionPane.showMessageDialog(this.getRootPane(),
                            "You must select at least two dumps for getting a diff!\n",
                            "Error", JOptionPane.ERROR_MESSAGE);

                } else {
                    DefaultMutableTreeNode mergeRoot = fetchTop(tree.getSelectionPath());
                    Map dumpMap = dumpStore.getFromDumpFiles(mergeRoot.getUserObject().toString());
                    ((Logfile) mergeRoot.getUserObject()).getUsedParser().mergeDumps(mergeRoot,
                            dumpMap, paths, paths.length, null);
                    createTree();
                    this.getRootPane().revalidate();
                }
            } else if ("Show selected Dump in logfile".equals(source.getText())) {
                navigateToDumpInLogfile();
            } else if ("Show Toolbar".equals(source.getText())) {
                setShowToolbar(((JCheckBoxMenuItem) source).getState());
            } else if ("Request Thread Dump...".equals(source.getText())) {
                addMXBeanDump();
            } else if ("Expand all nodes".equals(source.getText())) {
                expandAllCatNodes(true);
            } else if ("Collapse all nodes".equals(source.getText())) {
                expandAllCatNodes(false);
            } else if ("Sort by thread count".equals(source.getText())) {
                sortCatByThreads();
            } else if ("Expand all Dump nodes".equals(source.getText())) {
                expandAllDumpNodes(true);
            } else if ("Collapse all Dump nodes".equals(source.getText())) {
                expandAllDumpNodes(false);
            }
        } else if (e.getSource() instanceof JButton) {
            JButton source = (JButton) e.getSource();
            if("Open Logfile".equals(source.getToolTipText())) {
                chooseFile();
            } else if("Close selected Logfile".equals(source.getToolTipText())) {
                closeCurrentDump();
            } else if("Preferences".equals(source.getToolTipText())) {
                showPreferencesDialog();
            } else if("Find long running threads".equals(source.getToolTipText())) {
                findLongRunningThreads();
            } else if("Expand all nodes".equals(source.getToolTipText())) {
                expandAllDumpNodes(true);
            } else if ("Collapse all nodes".equals(source.getToolTipText())) {
                expandAllDumpNodes(false);
            } else if("Find long running threads".equals(source.getToolTipText())) {
                findLongRunningThreads();
            } else if("Filters".equals(source.getToolTipText())) {
                showFilterDialog();
            } else if("Custom Categories".equals(source.getToolTipText())) {
                showCategoriesDialog();
            } else if("Request a Thread Dump".equals(source.getToolTipText())) {
                addMXBeanDump();
            }
            source.setSelected(false);
        }
    }
    
    private void showInfo() {
        InfoDialog infoDialog = new InfoDialog(getFrame());
        infoDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        //Display the window.
        infoDialog.pack();
        infoDialog.setLocationRelativeTo(getFrame());
        infoDialog.setVisible(true);
    }
    
    /**
     * display the specified file in a info window.
     * @param title title of the info window.
     * @param file the file to display.
     */
    private void showInfoFile(String title, String file, String icon) {
        HelpOverviewDialog infoDialog = new HelpOverviewDialog(getFrame(), title, file, TDA.createImageIcon(icon).getImage());
        infoDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        //Display the window.
        infoDialog.pack();
        infoDialog.setLocationRelativeTo(getFrame());
        infoDialog.setVisible(true);
    }
    
    private JFrame getFrame() {
        Container owner = this.getParent();
        while (owner != null && !(owner instanceof JFrame)) {
            owner = owner.getParent();
        }
        
        return(owner != null ? (JFrame) owner : null);
    }
    
    private void showPreferencesDialog() {
        //Create and set up the window.
        if(prefsDialog == null) {
            prefsDialog = new PreferencesDialog(getFrame());
            prefsDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        
        //Display the window.
        prefsDialog.reset();
        prefsDialog.pack();
        prefsDialog.setLocationRelativeTo(getFrame());
        prefsDialog.setVisible(true);
    }
    
    public void showFilterDialog() {
        
        //Create and set up the window.
        if(filterDialog == null) {
            filterDialog = new FilterDialog(getFrame());
            filterDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        
        //Display the window.
        filterDialog.reset();
        filterDialog.pack();
        filterDialog.setLocationRelativeTo(getFrame());
        filterDialog.setVisible(true);
    }
    
    /**
     * display categories settings.
     */
    private void showCategoriesDialog() {
        //Create and set up the window.
        if(categoriesDialog == null) {
            categoriesDialog = new CustomCategoriesDialog(getFrame());
            categoriesDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        
        //Display the window.
        categoriesDialog.reset();
        categoriesDialog.pack();
        categoriesDialog.setLocationRelativeTo(getFrame());
        categoriesDialog.setVisible(true);
    }
     
    /**
     * flag indicates if next file to open will be the first file (so fresh open)
     * or if a add has to be performed.
     */
    private boolean firstFile = true;
    
    /**
     * save the current logfile (only used in plugin mode)
     */
    public void saveLogFile() {
        if(fc == null) {
            fc = new FileDialog(getFrame());
            fc.setMultipleMode(false);
            try {
                fc.setDirectory(PrefManager.get().getSelectedPath().getCanonicalPath());
            } catch (IOException ioe) {
                // ignore
            }
        }
        if(firstFile && (PrefManager.get().getPreferredSizeFileChooser().height > 0)) {
            fc.setPreferredSize(PrefManager.get().getPreferredSizeFileChooser());
        }
        fc.setMode(FileDialog.SAVE);
        fc.setPreferredSize(fc.getSize());
        fc.setVisible(true);
        PrefManager.get().setPreferredSizeFileChooser(fc.getSize());

        String selectedFile = fc.getFile();
        if (selectedFile != null) {
            File file = new File(selectedFile);
            int selectValue = 0;
            if(file.exists()) {
                Object[] options = { "Overwrite", "Cancel" };
                selectValue = JOptionPane.showOptionDialog(null, "<html><body>File exists<br><b>" + file +
                        "</b></body></html>", "Confirm overwrite",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);
            }
            if(selectValue == 0) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    fos.write(((LogFileContent) logFile.getUserObject()).getContent().getBytes());
                    fos.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * choose a log file.
     * @param addFile check if a log file should be added or if tree should be cleared.
     */
    private void chooseFile() {
        if(firstFile && (PrefManager.get().getPreferredSizeFileChooser().height > 0)) {
            fc.setPreferredSize(PrefManager.get().getPreferredSizeFileChooser());
        }
        fc.setPreferredSize(fc.getSize());
        fc.setVisible(true);
        PrefManager.get().setPreferredSizeFileChooser(fc.getSize());

        File[] files = fc.getFiles();

        if (files.length > 0) {
            openFiles(files, false);
        }
    }
    
    /**
     * open the provided files. If isRecent is set to true, passed files
     * are not added to the recent file list.
     * @param files the files array to open
     * @param isRecent true, if passed files are from recent file list.
     */
    private void openFiles(File[] files, boolean isRecent) {
        for (int i = 0; i < files.length; i++) {
            dumpFile = files[i].getAbsolutePath();
            if(dumpFile != null) {
                if(!firstFile) {
                    // root nodes are moved down.
                    setRootNodeLevel(1);
                    
                    // do direct add without re-init.
                    addDumpFile();
                } else {
                    initDumpDisplay(null);
                    if(isFileOpen()) {
                       firstFile = false;
                    }
                }
            }
            
            if(!isRecent) {
                PrefManager.get().addToRecentFiles(files[i].getAbsolutePath());
            }
        }

        if(isFileOpen()) {
            this.getRootPane().revalidate();
            displayContent(null);
        }
    }
    
    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = TDA.class.getResource("icons/" + path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    /**
     * search for dump root node of for given node
     * @param node starting to search for
     * @return root node returns null, if no root was found.
     */
    private DefaultMutableTreeNode getDumpRootNode(DefaultMutableTreeNode node) {
        // search for starting node
        while(node != null && !(node.getUserObject() instanceof ThreadDumpInfo)) {
            node = (DefaultMutableTreeNode) node.getParent();
        }
        
        return(node);
    }
    
    /**
     * get the dump with the given name, starting from the provided node.
     * @param dumpName
     * @return
     */
    private DefaultMutableTreeNode getDumpRootNode(String dumpName, DefaultMutableTreeNode node) {
        DefaultMutableTreeNode lastNode = null;
        DefaultMutableTreeNode dumpNode = null;
        // search for starting node
        while(node != null && !(node.getUserObject() instanceof Logfile)) {
            lastNode = node;
            node = (DefaultMutableTreeNode) node.getParent();
        }
        
        if(node == null) {
            node = lastNode;
        }
        
        for(int i = 0; i < node.getChildCount(); i++) {
            Object userObject = ((DefaultMutableTreeNode) node.getChildAt(i)).getUserObject();
            if((userObject instanceof ThreadDumpInfo) && ((ThreadDumpInfo) userObject).getName().startsWith(dumpName)) {
                dumpNode = (DefaultMutableTreeNode) node.getChildAt(i);
                break;
            }
        }
        
        return(dumpNode);
    }
        
    /**
     * load a loggc log file based on the current selected thread dump
     */
    private void parseLoggcLogfile() {
        DefaultMutableTreeNode node = getDumpRootNode((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
        if (node == null) {
          return;
        }
        
        // get pos of this node in the thread dump hierarchy.
        int pos = node.getParent().getIndex(node);
        
        ((Logfile) ((DefaultMutableTreeNode) node.getParent()).getUserObject()).getUsedParser().setDumpHistogramCounter(pos);
        openLoggcFile();
    }
    
    /**
     * close the currently selected dump.
     */
    private void closeCurrentDump() {
        TreePath selPath = tree.getSelectionPath();
        
        while(selPath != null && !(checkNameFromNode((DefaultMutableTreeNode) selPath.getLastPathComponent(), File.separator) ||
                checkNameFromNode((DefaultMutableTreeNode) selPath.getLastPathComponent(), 2, File.separator))) {
            selPath = selPath.getParentPath();
        }
        
        Object[] options = { "Close File", "Cancel close" };
        
        String fileName = ((DefaultMutableTreeNode) selPath.getLastPathComponent()).getUserObject().toString();
        fileName = fileName.substring(fileName.indexOf(File.separator));
        
        int selectValue = JOptionPane.showOptionDialog(null, "<html><body>Are you sure, you want to close the currently selected dump file<br><b>" + fileName +
                "</b></body></html>", "Confirm closing...",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        
        // if first option "close file" is selected.
        if(selectValue == 0) {
            // remove stuff from the top nodes
            topNodes.remove(selPath.getLastPathComponent());
            
            if(topNodes.size() == 0) {
                // simply do a reinit, as there isn't anything to display
                removeAll();
                revalidate();
                
                init(runningAsJConsolePlugin, runningAsVisualVMPlugin);
                getMainMenu().getLongMenuItem().setEnabled(false);
                getMainMenu().getCloseMenuItem().setEnabled(false);
                getMainMenu().getSaveSessionMenuItem().setEnabled(false);
                getMainMenu().getCloseToolBarButton().setEnabled(false);
                getMainMenu().getExpandButton().setEnabled(false);
                getMainMenu().getCollapseButton().setEnabled(false);
                getMainMenu().getFindLRThreadsToolBarButton().setEnabled(false);
                getMainMenu().getCloseAllMenuItem().setEnabled(false);
                getMainMenu().getExpandAllMenuItem().setEnabled(false);
                getMainMenu().getCollapseAllMenuItem().setEnabled(false);

            } else {
                // rebuild jtree
                getMainMenu().getCloseMenuItem().setEnabled(false);
                getMainMenu().getCloseToolBarButton().setEnabled(false);
                createTree();
            }
            revalidate();
        }
        
    }
    
    /**
     * close all open dumps
     */
    private void closeAllDumps() {        
        Object[] options = { "Close all", "Cancel close" };
        
        int selectValue = JOptionPane.showOptionDialog(null, "<html><body>Are you sure, you want to close all open dump files", "Confirm closing...",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        
        // if first option "close file" is selected.
        if(selectValue == 0) {
            // remove stuff from the top nodes
            topNodes = new Vector();
            
            // simply do a reinit, as there is anything to display
            resetMainPanel();
        }        
    }
    
    /**
     * reset the main panel to start up
     */
    private void resetMainPanel() {
        removeAll();
        revalidate();
        
        init(runningAsJConsolePlugin, runningAsVisualVMPlugin);
        revalidate();
        
        getMainMenu().getLongMenuItem().setEnabled(false);
        getMainMenu().getCloseMenuItem().setEnabled(false);
        getMainMenu().getSaveSessionMenuItem().setEnabled(false);
        getMainMenu().getCloseToolBarButton().setEnabled(false);
        getMainMenu().getExpandButton().setEnabled(false);
        getMainMenu().getCollapseButton().setEnabled(false);
        getMainMenu().getFindLRThreadsToolBarButton().setEnabled(false);
        getMainMenu().getCloseAllMenuItem().setEnabled(false);
        getMainMenu().getExpandAllMenuItem().setEnabled(false);
        getMainMenu().getCollapseAllMenuItem().setEnabled(false);

    }

    /**
     * check if name of node starts with passed string
     * @param node the node name to check
     * @param startsWith the string to compare.
     * @return true if startsWith and beginning of node name matches.
     */
    private boolean checkNameFromNode(DefaultMutableTreeNode node, String startsWith) {
        return(checkNameFromNode(node, 0, startsWith));
    }

    /**
     * check if name of node starts with passed string
     * @param node the node name to check
     * @param startIndex the index to start with comparing, 0 if comparing should happen
     *                   from the beginning.
     * @param startsWith the string to compare.
     * @return true if startsWith and beginning of node name matches.
     */
    private boolean checkNameFromNode(DefaultMutableTreeNode node, int startIndex, String startsWith) {
        Object info = node.getUserObject();
        String result = null;
        if((info != null) && (info instanceof AbstractInfo)) {
            result = ((AbstractInfo) info).getName();
        } else if ((info != null) && (info instanceof String)) {
            result = (String) info;
        }
        
        if(startIndex > 0) {
            result = result.substring(startIndex);
        }
        
        return(result != null && result.startsWith(startsWith));
    }
    
    /**
     * open and parse loggc file
     */
    private void openLoggcFile() {
        fc.setVisible(true);

        File[] selectedFiles = fc.getFiles();

        if (selectedFiles.length > 0) {
            File file = selectedFiles[1];
            loggcFile = file.getAbsolutePath();
            if(loggcFile != null) {
                try {
                    final InputStream loggcFileStream = new ProgressMonitorInputStream(
                            this,
                            "Parsing " + loggcFile,
                            new FileInputStream(loggcFile));
                    
                    final SwingWorker worker = new SwingWorker() {
                        public Object construct() {
                            try {
                                DefaultMutableTreeNode top = fetchTop(tree.getSelectionPath());
                                
                                ((Logfile) top.getUserObject()).getUsedParser().parseLoggcFile(loggcFileStream, top);
                                
                                addThreadDumps(top, loggcFileStream);
                                createTree();
                                getRootPane().revalidate();
                                displayContent(null);
                            } finally {
                                if(loggcFileStream != null) {
                                    try {
                                        loggcFileStream.close();
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                            return null;
                        }
                    };
                    worker.start();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * find long running threads either in all parsed thread dumps or in marked thread
     * dump range.
     */
    private void findLongRunningThreads() {
        TreePath[] paths = tree.getSelectionPaths();
        if((paths == null) || (paths.length < 2)) {
            JOptionPane.showMessageDialog(this.getRootPane(),
                    "You must select at least two dumps for long thread run detection!\n",
                    "Error", JOptionPane.ERROR_MESSAGE);
            
        } else {
            DefaultMutableTreeNode mergeRoot = fetchTop(tree.getSelectionPath());
            Map dumpMap = dumpStore.getFromDumpFiles(mergeRoot.getUserObject().toString());

            LongThreadDialog longThreadDialog = new LongThreadDialog(this, paths, mergeRoot, dumpMap);
            
            //Display the window.
            longThreadDialog.reset();
            longThreadDialog.pack();
            longThreadDialog.setLocationRelativeTo(frame);
            longThreadDialog.setVisible(true);
            
        }
    }
        
    private int rootNodeLevel = 0;
    
    private int getRootNodeLevel() {
        return(rootNodeLevel);
    }
    
    private void setRootNodeLevel(int value) {
        rootNodeLevel = value;
    }
    
    private DefaultMutableTreeNode fetchTop(TreePath pathToRoot) {
        return((DefaultMutableTreeNode) pathToRoot.getPathComponent(getRootNodeLevel()));
    }
    
    /**
     * save the application state to preferences.
     */
    private void saveState() {
        PrefManager.get().setWindowState(frame.getExtendedState());
        if (fc.getDirectory() != null) {
            PrefManager.get().setSelectedPath(new File(fc.getDirectory()));
        }
        PrefManager.get().setPreferredSize(frame.getRootPane().getSize());
        PrefManager.get().setWindowPos(frame.getX(), frame.getY());
        if(isThreadDisplay()) {
            PrefManager.get().setTopDividerPos(topSplitPane.getDividerLocation());
            PrefManager.get().setDividerPos(splitPane.getDividerLocation());
        }
        PrefManager.get().flush();
    }
    
    /**
     * trigger, if a file is opened
     */
    private boolean fileOpen = false;

    private boolean isFileOpen() {
        return fileOpen;
    }
    
    private void setFileOpen(boolean value) {
        fileOpen = value;
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        frame = new JFrame("TDA - Thread Dump Analyzer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Image image = Toolkit.getDefaultToolkit().getImage( "TDA.gif" );
        Image image = TDA.createImageIcon("TDA.png").getImage();
        frame.setIconImage( image );
        
        final TDA tdaInstance = new TDA(true);
        frame.getRootPane().setPreferredSize(PrefManager.get().getPreferredSize());

        frame.setJMenuBar(new MainMenu(tdaInstance));
        tdaInstance.init(false, false);
        
        //Create and set up the content pane.
        if(dumpFile != null) {
            tdaInstance.initDumpDisplay(null);
        }
        
        tdaInstance.setOpaque(true); //content panes must be opaque
        frame.setContentPane(tdaInstance);
        
        // init filechooser
        fc = new FileDialog(tdaInstance.getFrame());
        fc.setMultipleMode(true);
        try {
            fc.setDirectory(PrefManager.get().getSelectedPath().getCanonicalPath());
        } catch (IOException ioe) {
            // ignore
        }

        /**
         * add window listener for persisting state of main frame
         */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                tdaInstance.saveState();
            }
            
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.setLocation(PrefManager.get().getWindowPos());
        
        //Display the window.
        frame.pack();
        
        // restore old window settings.
        frame.setExtendedState(PrefManager.get().getWindowState());
        
        frame.setVisible(true);
    }

    /**
     * display search dialog for current category
     */
    private void showSearchDialog() {
        // get the currently select category tree
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        JComponent catComp = ((Category) node.getUserObject()).getCatComponent(this);
        
        //Create and set up the window.
        searchDialog = new SearchDialog(getFrame(), catComp);
        
        //Display the window.
        searchDialog.reset();
        searchDialog.pack();
        searchDialog.setLocationRelativeTo(getFrame());
        searchDialog.setVisible(true);
        
        searchDialog.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    getFrame().setEnabled(true);
                }
            });
    }
    
    /**
     * main startup method for TDA
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            dumpFile = args[0];
        }
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    /**
     * check file menu
     */
    public void menuSelected(MenuEvent e) {
        JMenu source = (JMenu) e.getSource();
        if((source != null) && "File".equals(source.getText())) {
            // close menu item only active, if something is selected.
            getMainMenu().getCloseMenuItem().setEnabled(tree.getSelectionPath() != null);
            getMainMenu().getCloseToolBarButton().setEnabled(tree.getSelectionPath() != null);
        }
    }

    public void menuDeselected(MenuEvent e) {
        // nothing to do
    }

    public void menuCanceled(MenuEvent e) {
        // nothing to do
    }

    public static String getFontSizeModifier(int add) {
        String result = String.valueOf(fontSizeModifier + add);
        if((fontSizeModifier + add)> 0) {
            result = "+" + (fontSizeModifier + add);
        } 
        return(result);
    }
    
    public static void setFontSizeModifier(int value) {
        fontSizeModifier = value;
    }
    
    /**
     * handles dragging events for new files to open.
     */
    private class FileDropTargetListener extends DropTargetAdapter {

        public void drop(DropTargetDropEvent dtde) {
            try {
                DataFlavor[] df = dtde.getTransferable().getTransferDataFlavors();
                for (int i = 0; i < df.length; i++) {
                    if (df[i].isMimeTypeEqual("application/x-java-serialized-object")) {
                        dtde.acceptDrop(dtde.getDropAction());
                        String[] fileStrings = ((String) dtde.getTransferable().getTransferData(df[i])).split("\n");
                        File[] files = new File[fileStrings.length];
                        for (int j = 0; j < fileStrings.length; j++) {
                            files[j] = new File(fileStrings[j].substring(7));
                        }
                        openFiles(files, false);
                        dtde.dropComplete(true);
                    }
                }
            } catch (UnsupportedFlavorException ex) {
                ex.printStackTrace();
                dtde.rejectDrop();
            } catch (IOException ex) {
                ex.printStackTrace();
                dtde.rejectDrop();
            }

        }
    }

    
    
}
