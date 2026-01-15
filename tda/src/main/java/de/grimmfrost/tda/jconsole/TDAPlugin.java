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
 * $Id: TDAPlugin.java,v 1.4 2007-11-07 17:02:05 irockel Exp $
 */

package de.grimmfrost.tda.jconsole;

import de.grimmfrost.tda.TDA;
import com.sun.tools.jconsole.JConsoleContext;
import com.sun.tools.jconsole.JConsoleContext.ConnectionState;
import com.sun.tools.jconsole.JConsolePlugin;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.SwingWorker;

/**
 * The TDAPlugin capsulates TDA to be displayed in jconsole.
 */
public class TDAPlugin extends JConsolePlugin implements PropertyChangeListener
{
    private MBeanDumper mBeanDumper;
    private TDA tda = null;
    private Map tabs = null;

    public TDAPlugin() {
        // register itself as a listener
        addContextPropertyChangeListener(this);
    }

    /*
     * Returns a Thread Dumps tab to be added in JConsole.
     */
    public synchronized Map getTabs() {
        if (tabs == null) {
            try {
                mBeanDumper = new MBeanDumper(getContext().getMBeanServerConnection());
                tda = new TDA(false, mBeanDumper);
                
                tda.init(true, false);
                tabs = new LinkedHashMap();
                tabs.put("Thread Dumps", tda);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return tabs;
    }
    
    

    /*
     * Returns a SwingWorker which is responsible for updating the TDA tab.
     */
    public SwingWorker newSwingWorker() {
        return(new Worker(tda));
    }
    
    /** 
     * SwingWorker responsible for updating the GUI
     */
    class Worker extends SwingWorker {
        private TDA tda;
        Worker(TDA tda) {
            this.tda = tda;
        }
                                                                                
        protected void done() {
            // nothing to do atm
        }

        protected Object doInBackground() throws Exception {
            // nothing to do atm
            return null;
        }
    }    

    /*
     * Property listener to reset the MBeanServerConnection
     * at reconnection time.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        String prop = ev.getPropertyName();
        if (JConsoleContext.CONNECTION_STATE_PROPERTY.equals(prop)) {
            ConnectionState newState = (ConnectionState)ev.getNewValue();
            
            /* 
               JConsole supports disconnection and reconnection
               The MBeanServerConnection will become invalid when
               disconnected. Need to use the new MBeanServerConnection object
               created at reconnection time. 
             */
            if (newState == ConnectionState.CONNECTED && tda != null) {
                mBeanDumper.setMBeanServerConnection(getContext().getMBeanServerConnection());
            }
        }
    }

    
}
