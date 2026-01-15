/*
 * FilterChecker.java
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
 * $Id: FilterChecker.java,v 1.8 2008-03-12 10:44:19 irockel Exp $
 */
package de.grimmfrost.tda.filter;

import de.grimmfrost.tda.ThreadInfo;
import de.grimmfrost.tda.utils.PrefManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.ListModel;

/**
 * has a list of filters and checks for a given thread if it matches any of the filters.
 * @author irockel
 */
public class FilterChecker {
    /**
     * filters checked by this checker instance.
     */
    private Map filters = null;
    
    private static Map generalFilters = null;
    
    /** 
     * Creates a new instance of FilterChecker 
     */
    public FilterChecker(Map checkFilters) {
        filters = checkFilters;
    }
    
    /**
     * return a filter checker for all general filters
     */
    public static FilterChecker getFilterChecker() {
        if(generalFilters == null) {
            setGeneralFilters();
        }
        
        return(new FilterChecker(generalFilters));
    }
    
    private static void setGeneralFilters() {
        generalFilters = new HashMap();
        ListModel filters = PrefManager.get().getFilters();
        for(int i = 0; i < filters.getSize(); i++) {
            Filter currentFilter = (Filter) filters.getElementAt(i);
            if(currentFilter.isEnabled() && currentFilter.isGeneralFilter()) {
                generalFilters.put(currentFilter.getName(), currentFilter);
            }
        }
    }
    
    /**
     * add the given filter to the lists of filters
     */
    public void addToFilters(Filter filter) {
        if(filters == null) {
            filters = new HashMap();
        }
        
        filters.put(filter.getName(), filter);
    }
    
    /**
     * get filter from filter list
     * @param key the name of the filter.
     * @return filter with the given name, null otherwise.
     */
    public Filter getFromFilters(String key) {
        System.out.println("getFromFilters= " + key);
        return (filters != null ? (Filter) filters.get(key) : null);
    }
    
    /**
     * checks if the given thread info passes the filters of
     * this filter checker instance
     */
    public boolean check(ThreadInfo ti) {
        boolean result = true;
        Iterator filterIter = filters.values().iterator();
        while(result && filterIter.hasNext()) {
            Filter filter = (Filter) filterIter.next();
            result = filter.matches(ti);
        }
        return(result);
    }
    
    public boolean recheck(ThreadInfo ti) {
        // reset general filters
        setGeneralFilters();
        Iterator iter = filters.values().iterator();
        
        // remove disabled filters
        while(iter.hasNext()) {
            Filter filter = (Filter) iter.next();
            if(!filter.isEnabled()) {
                filters.remove(filter.getName());
                iter = filters.values().iterator();
                
            }
        }

        // add new or enabled filters
        iter = generalFilters.values().iterator();
        while(iter.hasNext()) {
            Filter filter = (Filter) iter.next();
            addToFilters(filter);
        }
        return(check(ti));
    }
    
    /**
     * get iterator on all set filters
     */
    public Iterator iterOfFilters() {
        return(filters.values().iterator());
    }
}
