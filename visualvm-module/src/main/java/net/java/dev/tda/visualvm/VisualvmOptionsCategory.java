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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class VisualvmOptionsCategory extends OptionsCategory {

    @Override
    public Icon getIcon() {
        return new ImageIcon(Utilities.loadImage("net/java/dev/tda/visualvm/resources/options.png"));
    }

    @Override
    public String getCategoryName() {
        return NbBundle.getMessage(VisualvmOptionsCategory.class, "OptionsCategory_Name_Visualvm");
    }

    @Override
    public String getTitle() {
        return NbBundle.getMessage(VisualvmOptionsCategory.class, "OptionsCategory_Title_Visualvm");
    }

    @Override
    public OptionsPanelController create() {
        return new VisualvmOptionsPanelController();
    }
}
