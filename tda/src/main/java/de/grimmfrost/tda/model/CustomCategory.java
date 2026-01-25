/*
 * CustomCategory.java
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
 * $Id: CustomCategory.java,v 1.1 2008-03-09 06:36:51 irockel Exp $
 */
package de.grimmfrost.tda.model;

import de.grimmfrost.tda.filter.Filter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * stores information for a custom category.
 * @author irockel
 */
public class CustomCategory {
    private String name = null;
    
    private Map filters = null;
    
    public CustomCategory(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * get iterator over all filters for this custom category
     * @return
     */
    public Iterator iterOfFilters() {
        return filters != null ? filters.values().iterator() : null;
    }

    /**
     * add filter to category filters
     * @param filter
     */
    public void addToFilters(Filter filter) {
        if(filters == null) {
            filters = new HashMap();
        }
        
        filters.put(filter.getName(), filter);
    }
    
    /**
     * checks if given name is in list of filters
     * @param name the key to check.
     * @return true if found, false otherwise.
     */
    public boolean hasInFilters(String name) {
        return (filters != null && filters.containsKey(name));
    }
    
    /**
     * resets the filter set to null
     */
    public void resetFilters() {
        filters = null;
    }
    
    public String toString() {
        return(getName());
    }
}
