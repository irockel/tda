/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.grimmfrost.tda.utils;

import java.util.ResourceBundle;

/**
 *
 * @author irockel
 */
public class ResourceManager {
    private static ResourceBundle locale;
    
    public static String translate(String key) {
        if(locale == null) {
            locale = ResourceBundle.getBundle("de/grimmfrost/tda/locale");
        }
        
        return(locale.getString(key));
    }
   
}
