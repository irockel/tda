/*
 * PrefManager.java
 *
 * Thread Dump Analysis Tool, parses Thread Dump input and displays it as tree
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
 * $Id: PrefManager.java,v 1.28 2010-04-01 09:20:28 irockel Exp $
 */
package de.grimmfrost.tda.utils;

import de.grimmfrost.tda.model.CustomCategory;
import de.grimmfrost.tda.filter.Filter;
import de.grimmfrost.tda.filter.FilterChecker;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

/**
 * Singleton class for accessing system preferences.
 * Window sizes, and positions are stored here and also the last accessed path
 * is stored here.
 *
 * @author irockel
 */
public class PrefManager {
    private static final Logger LOGGER = LogManager.getLogger(PrefManager.class);
    public static final String PARAM_DELIM = "\u00A7\u00A7\u00A7\u00A7";
    
    public static final String FILTER_SEP = "\u00ac\u00ac\u00ac\u00ac";

    
    private final static PrefManager prefManager = new PrefManager();
    
    private final Preferences toolPrefs;
    
    /** Creates a new instance of PrefManager */
    private PrefManager() {
        toolPrefs = Preferences.userNodeForPackage(this.getClass());
    }
    
    public static PrefManager get() {
        return(prefManager);
    }

    public long getMaxLogfileSize() {
        return(toolPrefs.getInt("maxlogfilesize", 1024));
    }
    
    public int getWindowState() {
        return(toolPrefs.getInt("windowState", -1));
    }
    
    public void setWindowState(int windowState) {
        toolPrefs.putInt("windowState", windowState);
    }
    
    public File getSelectedPath() {
        return(new File(toolPrefs.get("selectedPath", "")));
    }
    
    public void setSelectedPath(File directory) {
        toolPrefs.put("selectedPath", directory.getAbsolutePath());
    }
    
    public Dimension getPreferredSize() {
        return(new Dimension(toolPrefs.getInt("windowWidth", 800),
               toolPrefs.getInt("windowHeight", 600)));
    }
    
    public void setPreferredSize(Dimension size) {
        toolPrefs.putInt("windowHeight", size.height);
        toolPrefs.putInt("windowWidth", size.width);
    }
    
    public Dimension getPreferredSizeFileChooser() {
        return(new Dimension(toolPrefs.getInt("fileChooser.windowWidth", 0),
               toolPrefs.getInt("fileChooser.windowHeight", 0)));
    }
    
    public void setPreferredSizeFileChooser(Dimension size) {
        toolPrefs.putInt("fileChooser.windowHeight", size.height);
        toolPrefs.putInt("fileChooser.windowWidth", size.width);
    }
    
    public void setMaxLogfileSize(int size) {
        toolPrefs.putInt("maxlogfilesize", size);
    }
    
    public Point getWindowPos() {
        Point point = new Point(toolPrefs.getInt("windowPosX", 0),
                toolPrefs.getInt("windowPosY", 0));
        return(point);
    }
    
    public void setWindowPos(int x, int y) {
        toolPrefs.putInt("windowPosX", x);
        toolPrefs.putInt("windowPosY", y);
    }
    
    public int getMaxRows() {
        return(toolPrefs.getInt("maxRowsForChecking", 10));
    }
    
    public void setMaxRows(int rows) {
        toolPrefs.putInt("maxRowsForChecking", rows);
    }
    
    public int getTopDividerPos() {
        return(toolPrefs.getInt("top.dividerPos", 0));
    }
    
    public void setTopDividerPos(int pos) {
        toolPrefs.putInt("top.dividerPos", pos);
    }

    public int getDividerPos() {
        return(toolPrefs.getInt("dividerPos", 0));
    }
    
    public void setDividerPos(int pos) {
        toolPrefs.putInt("dividerPos", pos);
    }

    public int getStreamResetBuffer() {
        return(toolPrefs.getInt("streamResetBuffer", 16384));
    }
    
    public void setStreamResetBuffer(int buffer) {
        toolPrefs.putInt("streamResetBuffer", buffer);
    }
    
    public boolean getForceLoggcLoading() {
        return(toolPrefs.getBoolean("forceLoggcLoading", false));
    }
    
    public void setForceLoggcLoading(boolean force) {
        toolPrefs.putBoolean("forceLoggcLoading", force);
    }
    
    public boolean getJDK16DefaultParsing() {
        return(toolPrefs.getBoolean("jdk16DefaultParsing", true));
    }
    
    public void setJDK16DefaultParsing(boolean defaultParsing) {
        toolPrefs.putBoolean("jdk16DefaultParsing", defaultParsing);
    }
    
    public boolean getShowToolbar() {
        return(toolPrefs.getBoolean("showToolbar", true));
    }
    
    public void setShowToolbar(boolean state) {
        toolPrefs.putBoolean("showToolbar", state);
    }

    public String getDateParsingRegex() {
        return(toolPrefs.get("dateParsingRegex", "(\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d\\s\\d\\d:\\d\\d:\\d\\d).*"));
    }
    
    public void setDateParsingRegex(String dateRegex) {
        if(dateRegex == null) {
            // don't save null values.
            dateRegex = "";
        }
        toolPrefs.put("dateParsingRegex", dateRegex);
    }
    
    public String[] getDateParsingRegexs() {
        String elems = toolPrefs.get("dateParsingRegexs", "(\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d\\s\\d\\d:\\d\\d:\\d\\d).*");
        if(elems.equals("")) {
            elems = getDateParsingRegex();
        }
        return(elems.split(PARAM_DELIM));
    }
    
    public void setDateParsingRegexs(ListModel regexs) {
        toolPrefs.put("dateParsingRegexs", regexsToString(regexs));
    }
    
    private String regexsToString(ListModel regexs) {
        StringBuffer elems = new StringBuffer();
        for(int i = 0; i < regexs.getSize(); i++) {
            elems.append(regexs.getElementAt(i));
            if(i+1 < regexs.getSize()) {
                elems.append(PARAM_DELIM);
            }
        }
        return(elems.toString());
    }
    
    public void addToRecentFiles(String file) {
        String[] currentFiles = getRecentFiles();
        
        // only add files already in it
        if(!hasInRecentFiles(file, currentFiles)) {
            int start = currentFiles.length == 10 ? 1 : 0;
            StringBuffer recentFiles = new StringBuffer();
            
            for(int i = start; i < currentFiles.length; i++) {
                recentFiles.append(currentFiles[i]);
                recentFiles.append(PARAM_DELIM);
            }
            
            // append new files
            recentFiles.append(file);
            toolPrefs.put("recentFiles", recentFiles.toString());
        }
    }
    
    public String[] getRecentFiles() {
        return(toolPrefs.get("recentFiles", "").split(PARAM_DELIM));
    }
    
    public void addToRecentSessions(String file) {
        String[] currentFiles = getRecentSessions();
        
        // only add files already in it
        if(!hasInRecentFiles(file, currentFiles)) {
            int start = currentFiles.length == 10 ? 1 : 0;
            StringBuffer recentSessions = new StringBuffer();
            
            for(int i = start; i < currentFiles.length; i++) {
                recentSessions.append(currentFiles[i]);
                recentSessions.append(PARAM_DELIM);
            }
            
            // append new files
            recentSessions.append(file);
            toolPrefs.put("recentSessions", recentSessions.toString());
        }
    }
    
    public String[] getRecentSessions() {
        return(toolPrefs.get("recentSessions", "").split(PARAM_DELIM));
    }

    public void setUseGTKLF(boolean value) {
        toolPrefs.putBoolean("useGTKLF", value);
    }
    
    public boolean isUseGTKLF() {
        return(toolPrefs.getBoolean("useGTKLF", System.getProperty("os.name").startsWith("Linux")));
    }
    
    public void setMillisTimeStamp(boolean value) {
        toolPrefs.putBoolean("millisTimeStamp", value);
    }
    
    public boolean getMillisTimeStamp() {
        return(toolPrefs.getBoolean("millisTimeStamp", false));
    }
    
    public void setShowHotspotClasses(boolean value) {
        toolPrefs.putBoolean("showHotspotClasses", value);
    }
    
    public boolean getShowHotspotClasses() {
        return(toolPrefs.getBoolean("showHotspotClasses", false));
    }
    
    /**
     * temporary storage for filters to not to have them be parsed again
     */
    private final java.util.List cachedFilters = new ArrayList();
    
    public ListModel getFilters() {
        DefaultListModel filters = null;
        if(this.cachedFilters.isEmpty()) {
            String filterString = toolPrefs.get("filters", "");
            if(filterString.length() > 0) {
                filters = new DefaultListModel();
                String[] sFilters = filterString.split(PARAM_DELIM);
                filters.ensureCapacity(sFilters.length);
                try {
                    for(int i = 0; i < sFilters.length; i++) {
                        String[] filterData = sFilters[i].split(FILTER_SEP);
                        Filter newFilter = new Filter(filterData[0],
                                filterData[1], Integer.parseInt(filterData[2]),
                                filterData[3].equals("true"), filterData[4].equals("true"), filterData[5].equals("true"));
                        filters.add(i, newFilter);
                    }
                } catch (ArrayIndexOutOfBoundsException aioob) {
                    // fall back to default filters
                    filters = getPredefinedFilters();
                }
		// initialize cached filters
		setFilterCache(filters);
            } else {
                filters = getPredefinedFilters();
            }
        } else {
	    // populate filters from cache
            filters = getCachedFilters();
        }
        return(filters);
    }
    
    /**
     * Populates a new DefaultModelList object with the current list of filters.
     * 
     * @return populated DefaultModelList object
     */
    private DefaultListModel getCachedFilters() {
        DefaultListModel modelFilters = new DefaultListModel();
        Iterator it = this.cachedFilters.iterator();
        while (it.hasNext()) {
            modelFilters.addElement(it.next());
        }
        return modelFilters;
    }

    /**
     * Populates the cached filters using a new list of filters.
     * 
     * @param filters updated list of filters
     */
    private void setFilterCache(DefaultListModel filters) {
        // remove existing filters
        this.cachedFilters.clear();
        for (int f = 0; f < filters.size(); f++) {
            this.cachedFilters.add(filters.get(f));
        }
    }

    /**
     * temporary storage for categories to not to have them be parsed again
     */
    private final java.util.List cachedCategories = new ArrayList();
    
    /**
     * get custom categories.
     * @return list model with custom categories.
     */
    public ListModel getCategories() {
        DefaultListModel categories = null;
        if (this.cachedCategories.isEmpty()) {
            String categoryString = toolPrefs.get("categories", "");
            if (categoryString.length() > 0) {
                categories = new DefaultListModel();
                String[] sCategories = categoryString.split(PARAM_DELIM);
                categories.ensureCapacity(sCategories.length);
                try {
                    FilterChecker fc = FilterChecker.getFilterChecker();
                    for (int i = 0; i < sCategories.length; i++) {
                        String[] catData = sCategories[i].split(FILTER_SEP);
                        CustomCategory newCat = new CustomCategory(catData[0]);
                        
                        for(int j = 1; j < catData.length; j++) {
                            Filter filter = getFromFilters(catData[j].trim());
                            if(filter != null) {
                                newCat.addToFilters(filter);
                            }
                        }
                        categories.add(i, newCat);
                    }
                } catch (ArrayIndexOutOfBoundsException aioob) {
                    System.out.println("couldn't parse categories, " + aioob.getMessage());
                    aioob.printStackTrace();
                    // fall back to default categories
                    categories = new DefaultListModel();
                }
                // initialize cache
                setCategoryCache(categories);
            } else {
                categories = new DefaultListModel();
            }
        } else {
            // populate categories from cache
            categories = getCachedCategories();
        }
        return (categories);
    }
    
    /**
     * Populates a new DefaultModelList object with the current list of
     * categories.
     * 
     * @return populated DefaultModelList object
     */
    private DefaultListModel getCachedCategories() {
        DefaultListModel modelFilters = new DefaultListModel();
        Iterator it = this.cachedCategories.iterator();
        while (it.hasNext()) {
            modelFilters.addElement(it.next());
        }
        return modelFilters;
    }

    /**
     * Populates the cached categories using a new list of categories.
     * 
     * @param categories
     *            populated object of {@link CustomCategory} objects
     */
    private void setCategoryCache(DefaultListModel categories) {
        // remove existing categories
        this.cachedCategories.clear();
        for (int f = 0; f < categories.size(); f++) {
            this.cachedCategories.add(categories.get(f));
        }
    }

    /**
     * get filter for given key from filters
     * @param key filter key to look up
     * @return filter, null otherwise.
     */
    private Filter getFromFilters(String key) {
        ListModel filters = getFilters();
        for(int i = 0; i < filters.getSize(); i++) {
            Filter filter = (Filter) filters.getElementAt(i);
            if(filter.getName().equals(key)) {
                return(filter);
            } 
        }
        
        return(null);
    }
    
    /**
     * generate the default filter set.
     */
    private DefaultListModel getPredefinedFilters() {
        Filter newFilter = new Filter("System Thread Exclusion Filter", ".*at\\s.*", Filter.HAS_IN_STACK_RULE, true, false, false);
        DefaultListModel filters = new DefaultListModel();
        filters.ensureCapacity(2);
        filters.add(0, newFilter);
        newFilter = new Filter("Idle Threads Filter", "", Filter.SLEEPING_RULE, true, true, false);
        filters.add(1, newFilter);
        return(filters);
    }
    
    public void setFilters(DefaultListModel filters) {
        // store into cache
        StringBuffer filterString = new StringBuffer();
        for(int i = 0; i < filters.getSize(); i++) {
            if(i > 0) {
                filterString.append(PARAM_DELIM);
            }
            filterString.append(((Filter)filters.getElementAt(i)).getName());
            filterString.append(FILTER_SEP);
            filterString.append(((Filter)filters.getElementAt(i)).getFilterExpression());
            filterString.append(FILTER_SEP);
            filterString.append(((Filter)filters.getElementAt(i)).getFilterRule());
            filterString.append(FILTER_SEP);
            filterString.append(((Filter)filters.getElementAt(i)).isGeneralFilter());
            filterString.append(FILTER_SEP);
            filterString.append(((Filter)filters.getElementAt(i)).isExclusionFilter());
            filterString.append(FILTER_SEP);
            filterString.append(((Filter)filters.getElementAt(i)).isEnabled());
        }
        toolPrefs.put("filters", filterString.toString());
	setFilterCache(filters);
        setFilterLastChanged();
    }
    
    /**
     * store categories
     * @param categories
     */
    public void setCategories(DefaultListModel categories) {
        // store into cache
        StringBuffer catString = new StringBuffer();
        for (int i = 0; i < categories.getSize(); i++) {
            if (i > 0) {
                catString.append(PARAM_DELIM);
            }
            CustomCategory cat = (CustomCategory) categories.getElementAt(i);
            catString.append(cat.getName());
            catString.append(FILTER_SEP);
            Iterator catIter = cat.iterOfFilters();
            while ((catIter != null) && (catIter.hasNext())) {
                Filter filter = (Filter) catIter.next();
                catString.append(filter.getName());
                catString.append(FILTER_SEP);
                
            }
        }
        toolPrefs.put("categories", catString.toString());
	setCategoryCache(categories);
    }

    
    private long filterLastChanged = -1;
    
    /**
     * return time stamp of last change time stamp of filter settings
     */
    public long getFiltersLastChanged(){
        return(filterLastChanged);
    }
    
    private void setFilterLastChanged() {
        filterLastChanged = System.currentTimeMillis();
    }
        
    public void flush() {
        try {
            toolPrefs.flush();
        } catch (BackingStoreException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * check if new file is in given recent file list
     */
    private boolean hasInRecentFiles(String file, String[] currentFiles) {
        boolean found = false;
        
        for(int i = 0; i < currentFiles.length; i++) {
           if(file.equals(currentFiles[i])) {
               found = true;
               break;
           } 
        }
        return found;
    }
}
