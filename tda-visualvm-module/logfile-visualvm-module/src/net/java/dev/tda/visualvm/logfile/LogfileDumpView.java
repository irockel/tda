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
 * $Id: LogfileDumpView.java,v 1.1 2008-09-30 19:21:59 irockel Exp $
 */
package net.java.dev.tda.visualvm.logfile;

import com.pironet.tda.TDA;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.ImageIcon;
import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.components.DataViewComponent;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author irockel
 */
public class LogfileDumpView extends DataSourceView {
    private static final String IMAGE_PATH = "net/java/dev/tda/visualvm/logfile/resources/logfile.png";  // NOI18N
    private Logfile logfile;
    private TDA tdaPanel;
    private LogPanel logPanel = null;
    
    public LogfileDumpView(Logfile logfile) {
        super(logfile, NbBundle.getMessage(LogfileDumpView.class, "LBL_DumpView"), new ImageIcon(Utilities.loadImage(IMAGE_PATH, true)).getImage(), 0, false);    // NOI18N
        this.logfile = logfile;
    }

    @Override
    protected DataViewComponent createComponent() {
        tdaPanel = new TDA(false, logfile.getFile().getAbsolutePath());
        
        // init panel and set border
        tdaPanel.init(false, true);
        
        // display the logfile
        tdaPanel.initDumpDisplay(null);
        
        logPanel = new LogPanel(tdaPanel);
        logPanel.setText(readText());
        
        DataViewComponent dvc = new DataViewComponent(new DataViewComponent.MasterView(NbBundle.getMessage(LogfileDumpView.class, 
                "MSG_DumpView"), null, logPanel), 
                new DataViewComponent.MasterViewConfiguration(true));
        logPanel.revalidate();
        return(dvc);
    }

    /**
     * read log file
     * 
     * @return
     */
    private String readText() {
        BufferedReader br = null;
        try {
            StringBuffer text = new StringBuffer();
            br = new BufferedReader(new FileReader(logfile.getFile()));
            while(br.ready()) {
                text.append(br.readLine());
                text.append("\n");
            }
            return(text.toString());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if(br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return("");
    }
}
