package nl.b3p.geotools.data.linker.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import nl.b3p.geotools.data.linker.ActionFactory;
import nl.b3p.geotools.data.linker.FeatureException;
import static nl.b3p.geotools.data.linker.blocks.Action.log;
import nl.b3p.geotools.data.linker.feature.EasyFeature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.filter.text.cql2.CQL;
import org.opengis.filter.Filter;

/**
 *
 * @author Boy de Wit
 */
public class ActionFeature_Filter_Column_Value extends Action {

    protected static final Log logger = LogFactory.getLog(ActionFeature_Filter_Column_Value.class);
    private String columnName = null;
    private String operator = null;
    private String filterValue = null;

    public ActionFeature_Filter_Column_Value (
            String columnName,
            String operator,
            String filterValue) {

        this.columnName = columnName;
        this.operator = operator;
        this.filterValue = filterValue;
    }

    public EasyFeature execute(EasyFeature feature) throws Exception {
        long start = new Date().getTime();
        
        try {
            feature.getAttributeDescriptorIDbyName(columnName);
        } catch (FeatureException fex) {
            String err = "Kolom " + columnName + " in filter blok bestaat niet.";
            
            throw new Exception(err);
        }
        
        Filter filter = CQL.toFilter(columnName + operator + "'" + filterValue + "'");
        boolean result = filter.evaluate(feature.getFeature());

        Map userData = feature.getFeature().getUserData();
        if (!result && userData != null) {
            String err = "Record kwam niet door het filter: " + filter.toString();
            userData.put("SKIP", err);
        }
        
        long end = new Date().getTime() - start;        
        
        log.debug("FILTER BLOCK: " + end);

        return feature;
    }

    public String toString() {
        return "";
    }

    public String getDescription_NL() {
        return "Filter feature op basis van kolomwaarde.";
    }

    public static List<List<String>> getConstructors() {
        List<List<String>> constructors = new ArrayList<List<String>>();

        constructors.add(Arrays.asList(new String[]{
            ActionFactory.ATTRIBUTE_NAME_FILTER_COLUMN,
            ActionFactory.ATTRIBUTE_NAME_FILTER_OPERATOR,
            ActionFactory.ATTRIBUTE_NAME_FILTER_VALUE
        }));

        return constructors;
    }

    @Override
    public void flush(String typeName2Read) throws Exception {
    }
}