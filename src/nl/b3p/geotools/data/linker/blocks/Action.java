/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.geotools.data.linker.blocks;


import java.util.List;
import nl.b3p.geotools.data.linker.feature.EasyFeature;
import nl.b3p.geotools.data.linker.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Every action extends Action
 * Every Comboblock extends actionCombo
 * Every Condition extends Condition
 * @author Gertjan Al, B3Partners
 */
public abstract class Action {

    protected String attributeName = "";
    protected int attributeID = -1;
    protected static final Log log = LogFactory.getLog(DataStoreLinker.class);
    public static final String THE_GEOM = "the_geom";

    abstract public EasyFeature execute(EasyFeature feature) throws Exception;

    @Override
    abstract public String toString();

    abstract public String getDescription_NL();

    public static List<List<String>> getConstructors() {
        return null;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Constructor has filled attributeID or attibuteName. With this funtion attributeID will be fixed (set / filled), using attributeName
     */
    protected void fixAttributeID(EasyFeature feature) throws Exception {
        if (attributeID == -1) {
            attributeID = feature.getAttributeDescriptorIDbyName(attributeName);
        }else{
            attributeName = feature.getAttributeDescriptorNameByID(attributeID);
        }
    }


    /**
     * Fix a string and filter characters not allowed
     */
    public static String fixTypename(String in) {
        String allowed = "qwertyuiopasdfghjklzxcvbnm1234567890_";
        String out = "";

        for (int i = 0; i < in.length(); i++) {
            for (int j = 0; j < allowed.length(); j++) {
                if (in.substring(i, i + 1).toLowerCase().equals(allowed.substring(j, j + 1))) {
                    out += in.substring(i, i + 1);
                }
            }
        }

        return out;
    }

    public void close() throws Exception {
        // Override this if necessary
        // Used for closing iterator or reader / writer
    }
}
