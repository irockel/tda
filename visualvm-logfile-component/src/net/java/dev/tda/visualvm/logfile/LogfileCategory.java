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
 *
 * $Id: LogfileCategory.java,v 1.1 2008-09-30 19:21:59 irockel Exp $
 */
package net.java.dev.tda.visualvm.logfile;

import org.graalvm.visualvm.core.snapshot.SnapshotCategory;
import org.openide.util.NbBundle;

/**
 *
 * @author irockel
 */
public class LogfileCategory extends SnapshotCategory<Logfile> {
    
    private static final String NAME = NbBundle.getMessage(LogfileDumpView.class, "LBL_Logfile");   // NOI18N
    private static final String PREFIX = NbBundle.getMessage(LogfileDumpView.class, "LBL_Prefix");
    private static final String SUFFIX = null;
    
    public LogfileCategory() {
        super(NAME, Logfile.class, PREFIX, SUFFIX, POSITION_NONE);
    }

}
