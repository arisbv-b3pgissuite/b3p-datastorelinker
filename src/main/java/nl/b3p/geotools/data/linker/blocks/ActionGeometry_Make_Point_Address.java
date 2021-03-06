package nl.b3p.geotools.data.linker.blocks;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.b3p.geotools.data.linker.ActionFactory;
import nl.b3p.geotools.data.linker.feature.EasyFeature;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import java.util.Map;
import nl.b3p.geotools.data.linker.Status;


/**
 *
 * @author Boy de Wit
 */
public class ActionGeometry_Make_Point_Address extends Action {

    private int attributeIDAddres1;
    private int attributeIDAddres2;
    private int attributeIDAddres3;
    private int attributeIDCity;

    private String attributeNameAddres1;
    private String attributeNameAddres2;
    private String attributeNameAddres3;
    private String attributeNameCity;
    private String projectie;

    private boolean useID = true;
    private String srs;

    public ActionGeometry_Make_Point_Address(int attributeIDAddres1, int attributeIDAddres2,
            int attributeIDAddres3, int attributeIDCity, String projectie) {

        this.attributeIDAddres1 = attributeIDAddres1;
        this.attributeIDAddres2 = attributeIDAddres2;
        this.attributeIDAddres3 = attributeIDAddres3;
        this.attributeIDCity = attributeIDCity;
        this.srs = projectie;
    }

    public ActionGeometry_Make_Point_Address(String attributeNameAddres1, String attributeNameAddres2,
            String attributeNameAddres3, String attributeNameCity, String projectie) {

        this.attributeNameAddres1 = attributeNameAddres1;
        this.attributeNameAddres2 = attributeNameAddres2;
        this.attributeNameAddres3 = attributeNameAddres3;
        this.attributeNameCity = attributeNameCity;
        this.srs = projectie;

        this.useID = false;
    }

    public EasyFeature execute(EasyFeature feature) throws Exception {

        attributeIDAddres1 = -1;
        attributeIDAddres2 = -1;
        attributeIDAddres3 = -1;
        attributeIDCity = -1;

        if (!useID) {
            if (attributeNameAddres1 != null && !attributeNameAddres1.equals(""))
                attributeIDAddres1 = feature.getAttributeDescriptorIDbyName(attributeNameAddres1);
            
            if (attributeNameAddres2 != null && !attributeNameAddres2.equals(""))
                attributeIDAddres2 = feature.getAttributeDescriptorIDbyName(attributeNameAddres2);

            if (attributeNameAddres3 != null && !attributeNameAddres3.equals(""))
                attributeIDAddres3 = feature.getAttributeDescriptorIDbyName(attributeNameAddres3);

            if (attributeNameCity != null && !attributeNameCity.equals(""))
                attributeIDCity = feature.getAttributeDescriptorIDbyName(attributeNameCity);
        }

        EasyFeature f = setGeomToNewPoint(feature);

        return f;
    }

    public String toString() {
        return "";
    }

    public String getDescription_NL() {
        return "Adres omzetten naar een Point geometrie.";
    }

    public static List<List<String>> getConstructors() {
        List<List<String>> constructors = new ArrayList<List<String>>();

        constructors.add(Arrays.asList(new String[]{
                    ActionFactory.ATTRIBUTE_NAME_ADDRESS1,
                    ActionFactory.ATTRIBUTE_NAME_ADDRESS2,
                    ActionFactory.ATTRIBUTE_NAME_ADDRESS3,
                    ActionFactory.ATTRIBUTE_NAME_CITY,
                    ActionFactory.SRS
                }));

        return constructors;
    }

    private static double fixDecimals(String value) {
        value = value.trim();
        if (value.contains(",")) {
            if (value.contains(".")) {
                value = value.replaceAll("[.]", "");
            }
            value = value.replaceAll("[,]", ".");
        }
        return Double.parseDouble(value);
    }

    private EasyFeature setGeomToNewPoint(EasyFeature feature) throws Exception {

        // Retrieve geometryColumn name
        String geometryDescriptorName = Action.THE_GEOM;
        if (feature.getFeatureType().getGeometryDescriptor() != null) {
            geometryDescriptorName = feature.getFeatureType().getGeometryDescriptor().getName().getLocalPart();
        }

        String address1 = "";
        String address2 = "";
        String address3 = "";
        String city = "";

        if (attributeIDAddres1 > 0)
            address1 = feature.getAttribute(attributeIDAddres1).toString();

        if (attributeIDAddres2 > 0)
            address2 = feature.getAttribute(attributeIDAddres2).toString();

        if (attributeIDAddres3 > 0)
            address3 = feature.getAttribute(attributeIDAddres3).toString();

        if (attributeIDCity > 0)
            city = feature.getAttribute(attributeIDCity).toString();

        Point point = null;

        try {
            point = convertAddressToPoint(address1, address2, address3, city);
        } catch (Exception ex) {
            throw new Exception(ex);
        }

        if (point != null) {
            feature.setAttribute(geometryDescriptorName, point);
        }

        return feature;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private Point convertWktToRdsPoint(String wkt) throws Exception {
        Point p = null;

        try {
            Geometry sourceGeometry = createGeomFromWKTString(wkt);

            if (sourceGeometry != null) {
                CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
                CoordinateReferenceSystem targetCRS = CRS.decode(srs);

                MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

                if (transform != null) {
                    Geometry targetGeometry = JTS.transform(sourceGeometry, transform);
                    
                    if (targetGeometry != null) {
                        targetGeometry.setSRID(4326);
                        p = targetGeometry.getCentroid();
                    }
                }
            }

        } catch (Exception ex) {
            throw new Exception(ex);
        }

        return p;
    }

    public static Geometry createGeomFromWKTString(String wktstring) throws Exception {
        WKTReader wktreader = new WKTReader(new GeometryFactory(new PrecisionModel(), 28992));
        try {
            return wktreader.read(wktstring);
        } catch (ParseException ex) {
            throw new Exception(ex);
        }
    }
    
    private String createGoogleUrl(String adres, String city) throws Exception {

        String url = null;

        String googleBaseUrl = "http://maps.google.nl/maps/geo?q=";
        
        String country = "Nederland";
        String plaats = ",+" + city + ",+" + country;

        String hl = "&hl=nl";
        String output = "&output=json";

        String encodedParams = null;

        try {
            encodedParams = URLEncoder.encode(adres + plaats, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new Exception(ex);
        }

        String otherParams = hl + output;

        url = googleBaseUrl + encodedParams + otherParams;

        return url;
    }
    
    private String createAlternateUrl(String address) throws Exception {

        String url = null;
        
        String baseUrl = "http://bag42.nl/api/v0/geocode/json?maxitems=1&address=";
        String encodedParams = null;

        try {
            encodedParams = URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new Exception(ex);
        }

        url = baseUrl + encodedParams;

        return url;
    }

    private String createGisGraphyurl() {
        String url = null;

        return url;
    }

    private Point convertAddressToPoint(String address1, String address2,
            String address3, String city) throws Exception {
        Point p1 = null;
        
        /* TODO: Google Plaats is verplicht. Controleren of deze gevuld is 
         Het gehele adres mag ook niet leeg zijn!
        if (city == null || city.equals("")) {
            throw new Exception("Plaats is leeg.");
        }
        */

        String adres = (address1 + " " + address2 + " " + address3).trim();

        if (adres == null || adres.equals("")) {
            throw new Exception("Adres is leeg.");
        }

        //String url = createGoogleUrl(adres, city);
        String newAdres = adres.replaceAll(" ", "");
        String encodeAdres = null;
        
        /* Deze geocoder geeft geen resultaten op een aantal plaatnamen */        
        if (city != null && city.toUpperCase().equals("'S-HERTOGENBOSCH")
                || city.toUpperCase().equals("'S HERTOGENBOSCH") ) {
            city = "DEN BOSCH";
        }
        
        if (city != null && city.toUpperCase().equals("'S-GRAVENHAGE")
                || city.toUpperCase().equals("'S GRAVENHAGE")) {
            city = "DEN HAAG";
        }
        
        if (city != null && city.toUpperCase().equals("'S-GRAVENDEEL")) {
            city = "GRAVENDEEL";
        }
        
        if (city != null && city.toUpperCase().equals("'S-GRAVENZANDE")
                || city.toUpperCase().equals("'S GRAVENZANDE")) {
            city = "GRAVENZANDE";
        }
        
        if (city != null && city.toUpperCase().equals("DRIEBERGEN-RIJSENBURG")) {
            city = "DRIEBERGEN";
        }
        
        if (city != null && city.toUpperCase().equals("HARDINXVELD-GIESSENDAM")) {
            city = "HARDINXVELD";
        }
        
        if (city != null && !city.equals("")) {
            encodeAdres = newAdres + "+" + city;
        } else {
            encodeAdres = newAdres;
        }        
        
        String url = createAlternateUrl(encodeAdres);

        JSONObject json;
        try {
            json = readJsonFromUrl(url);
            
            if (json.getString("status").equals("ZERO_RESULTS")) {
                throw new Exception("Geen resultaat voor adres: " + encodeAdres);
            }
            
            /* Geocoder Stefan de Koning */                
            Double x = new Double(json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat"));
            Double y = new Double(json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lng"));
            
            /* Google
            try {
                json.getJSONArray("Placemark");
            } catch (JSONException ex) {
                throw new Exception("Geen resultaat voor adres: " + adres);
            }
            
            Double x = null;
            Double y = null;
            if (json.getJSONArray("Placemark").length() > 1) {
                throw new Exception("Teveel resultaten voor adres: " + adres);
            }
                
            x = (Double) json.getJSONArray("Placemark").getJSONObject(0).getJSONObject("Point").getJSONArray("coordinates").get(0);
            y = (Double) json.getJSONArray("Placemark").getJSONObject(0).getJSONObject("Point").getJSONArray("coordinates").get(1);
            */
            
            /* Lat is in nederland groter dan lon. Ongeveer Lon 4 en Lat 52 */
            try {
                Double lat = x;
                Double lon = y;

                if (x < y) {
                    lat = x;
                    lon = y;
                } else {
                    lat = y;
                    lon = x;
                }

                /* TODO: Checken of Point in bbox Nederland valt anders
                 * exception gooien voor deze feature */

                p1 = convertWktToRdsPoint("POINT("+lat+" " +lon+")");

            } catch (Exception ex) {
                throw new Exception(ex);
            }

        } catch (IOException ex) {
            throw new Exception(ex);
        } catch (JSONException ex) {
            throw new Exception(ex);
        }

        return p1;
    }

    @Override
    public void flush(Status status, Map properties) throws Exception {
    }
    
    @Override
    public void processPostCollectionActions(Status status, Map properties) throws Exception {
    }
}
