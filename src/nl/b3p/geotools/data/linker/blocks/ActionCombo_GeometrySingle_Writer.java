
package nl.b3p.geotools.data.linker.blocks;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.HashMap;
import java.util.Map;
import nl.b3p.geotools.data.linker.ActionFactory;

/**
 *
 * @author Gertjan Al, B3Partners
 */
public class ActionCombo_GeometrySingle_Writer extends ActionCombo {

 public ActionCombo_GeometrySingle_Writer(Map params, boolean append, boolean dropFirst) {

        ActionCondition_Feature_Class condition_P = new ActionCondition_Feature_Class(Action.THE_GEOM, Point.class);
        ActionCondition_Feature_Class condition_L = new ActionCondition_Feature_Class(Action.THE_GEOM, LineString.class);
        ActionCondition_Feature_Class condition_V = new ActionCondition_Feature_Class(Action.THE_GEOM, Polygon.class);

        ActionCondition_Feature_Class condition_MP = new ActionCondition_Feature_Class(Action.THE_GEOM, MultiPoint.class);
        ActionCondition_Feature_Class condition_ML = new ActionCondition_Feature_Class(Action.THE_GEOM, MultiLineString.class);
        ActionCondition_Feature_Class condition_MV = new ActionCondition_Feature_Class(Action.THE_GEOM, MultiPolygon.class);

        actionList.add(condition_P);
        condition_P.addActionToList(false, condition_L);
        condition_L.addActionToList(false, condition_V);
        condition_V.addActionToList(false, condition_MP);
        condition_MP.addActionToList(false, condition_ML);
        condition_ML.addActionToList(false, condition_MV);

        condition_P.addActionToList(true, new ActionFeatureType_Replace_Class(Action.THE_GEOM, Point.class, true));
        ActionDataStore_Writer dsw_p = new ActionDataStore_Writer(new HashMap(params), append, dropFirst);
        condition_P.addActionToList(true, dsw_p);

        condition_L.addActionToList(true, new ActionFeatureType_Replace_Class(Action.THE_GEOM, LineString.class, true));
        ActionDataStore_Writer dsw_l = new ActionDataStore_Writer(new HashMap(params), append, dropFirst);
        condition_L.addActionToList(true, dsw_l);

        condition_V.addActionToList(true, new ActionFeatureType_Replace_Class(Action.THE_GEOM, Polygon.class, true));
        ActionDataStore_Writer dsw_v = new ActionDataStore_Writer(new HashMap(params), append, dropFirst);
        condition_V.addActionToList(true, dsw_v);

        condition_MP.addActionToList(true, new ActionFeatureType_Replace_Class(Action.THE_GEOM, MultiPoint.class, true));
        ActionDataStore_Writer dsw_mp = new ActionDataStore_Writer(new HashMap(params), append, dropFirst);
        condition_MP.addActionToList(true, dsw_mp);

        condition_ML.addActionToList(true, new ActionFeatureType_Replace_Class(Action.THE_GEOM, MultiLineString.class, true));
        ActionDataStore_Writer dsw_ml = new ActionDataStore_Writer(new HashMap(params), append, dropFirst);
        condition_ML.addActionToList(true, dsw_ml);

        condition_MV.addActionToList(true, new ActionFeatureType_Replace_Class(Action.THE_GEOM, MultiPolygon.class, true));
        ActionDataStore_Writer dsw_mv = new ActionDataStore_Writer(new HashMap(params), append, dropFirst);
        condition_MV.addActionToList(true, dsw_mv);
    }

    public static String[][] getConstructors() {
        return new String[][]{
                    new String[]{
                        ActionFactory.PARAMS,
                        ActionFactory.APPEND,
                        ActionFactory.DROPFIRST
                    }
                    // Constructors supported using ActionFactory
                    ,new String[]{
                        ActionFactory.PARAMS,
                        ActionFactory.APPEND,
                    }
                    ,new String[]{
                        ActionFactory.PARAMS,
                        ActionFactory.DROPFIRST
                    }
                };
    }

    public String getDescription_NL() {
        return "Pas de geometry in het featureType aan aan de geometry in de feature. Let op; er mag dus maar één geometry soort worden ingeladen";
    }
}