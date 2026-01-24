/*
 * AbstractInfo.java
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
 * $Id: AbstractInfo.java,v 1.1 2007-12-08 09:58:34 irockel Exp $
 */

package de.grimmfrost.tda.model;

import java.io.Serializable;

/**
 * abstract info for presenting node data in the main tree.
 * 
 * @author irockel
 */
public abstract class AbstractInfo implements Serializable {
    private String name;

    /**
     * get the name of the node.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * set the name of the node.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
