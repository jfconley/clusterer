/*
 * ShapeFileDataGenerator.java
 *
 * Created on June 7, 2007, 2:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.dataGeneration;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import dissprogram.DissUtils;
import dissprogram.image.MomentGenerator;
import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JFileChooser;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.graph.util.SimpleFileFilter;

/**
 *
 * @author z4x
 */
public class ShapeFileDataGenerator {
    
    /** Creates a new instance of ShapeFileDataGenerator */
    public ShapeFileDataGenerator() {
    }
    
    public static void main(String[] args){
        FeatureCollection fc = FeatureCollections.newCollection();
        String fileName = "dummyFileName";
        boolean is90 = false;
        boolean is00 = false;
        boolean is06 = false;
        
        try{
            JFileChooser jfc = new JFileChooser();
            jfc.setFileFilter(new SimpleFileFilter("shp", "Shapefile"));
            int result = jfc.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION){
                URL url = jfc.getSelectedFile().toURI().toURL();  //File file.toURL() is deprecated, so put in a URI transition to avoid deprecation warnings
                fileName = jfc.getSelectedFile().getName();
                is90 = fileName.endsWith("d90.shp");
                is00 = fileName.endsWith("d00.shp");
                is06 = fileName.endsWith("ap06.shp");
                fc = DissUtils.shapeFileToFeatures(url);
                System.out.println(fileName);
            }
        } catch (MalformedURLException mue){
            System.out.println("Malformed URL Exception! " + mue.getMessage());
        }
        
        double cellSize = 0.01;  //this will get reset later on.
        
        GeometryFactory gf = new GeometryFactory();
        
        int maxXCells = 500;
        int maxYCells = 500;                
        
        int index = 0;  
        HashMap map = new HashMap();
        FeatureIterator features = fc.features();
        while (features.hasNext()){
            Feature f = features.next();
            String name = "dummy";
            if (is06){
                name = f.getAttribute("NAME").toString() + " " + f.getAttribute("STATE");
            } else if (is00 || is90){
                name = f.getAttribute("NAME").toString();
            } else {
//                name = f.getAttribute("NAME").toString();  //OBJECTID for soils, NAME for original urban areas
                name = f.getID();  //for roads and rivers (any ArcGIS buffers, really)
            }
            
            if (is90 || is00){
                //remove the commas.
                name = removeCommas(name);
                HashMap keyMap;
                if (is90){
                    keyMap = create90Keys();
                } else { //is00
                    keyMap = create00Keys();
                }
                
                String key = (String) keyMap.get(name);
                
                if (name != ""){
                    if (map.containsKey(key)){
                        ((Vector) map.get(key)).add(f);
                    } else {
                        Vector v = new Vector();
                        v.add(f);
                        map.put(key, v);
                    }
                }                
            } else {            
                if (name != ""){
                    if (map.containsKey(name)){
                        ((Vector) map.get(name)).add(f);
                    } else {
                        Vector v = new Vector();
                        v.add(f);
                        map.put(name, v);
                    }
                }
            }
        }
        
//        Iterator mapKeys = map.keySet().iterator();
        Object[] toArray = map.keySet().toArray();
        double[][] moments = new double[map.keySet().size()][7];
        String[] names = new String[map.keySet().size()];
        double[] pixels = new double[map.keySet().size()];        
        
        System.out.println(map.keySet().size() + " features");
        System.out.println("ID, pixels, cityName, Inv 1, Inv 2, Inv 3, Inv 4, Inv 5, Inv 6, Inv 7");
        
//        while (mapKeys.hasNext()){
//            String name = (String) mapKeys.next();
        for (int z = 213; z < toArray.length; z++){
            String name = (String) toArray[z];
            Vector featureVec = (Vector) map.get(name);
            Iterator featureIt = featureVec.iterator();
            double localMinX = Double.POSITIVE_INFINITY;
            double localMaxX = Double.NEGATIVE_INFINITY;
            double localMinY = Double.POSITIVE_INFINITY;
            double localMaxY = Double.NEGATIVE_INFINITY;
            int count = 0;
            
            while (featureIt.hasNext()){
                Feature nextFeature = (Feature) featureIt.next();
                double myMinX = nextFeature.getDefaultGeometry().getEnvelopeInternal().getMinX();
                double myMinY = nextFeature.getDefaultGeometry().getEnvelopeInternal().getMinY();
                double myMaxX = nextFeature.getDefaultGeometry().getEnvelopeInternal().getMaxX();
                double myMaxY = nextFeature.getDefaultGeometry().getEnvelopeInternal().getMaxY();
                if (myMinX < localMinX){
                    localMinX = myMinX;
                }
                if (myMinY < localMinY){
                    localMinY = myMinY;
                }
                if (myMaxX > localMaxX){
                    localMaxX = myMaxX;
                }
                if (myMaxY > localMaxY){
                    localMaxY = myMaxY;
                }
            }
            
            cellSize = Math.max((localMaxX - localMinX) / maxXCells, (localMaxY - localMinY) / maxYCells);
            int xCells = (int) Math.round((localMaxX - localMinX) / cellSize);
            int yCells = (int) Math.round((localMaxY - localMinY) / cellSize);
            
            float[][] rasterArray = new float[xCells][yCells];
            
            //need to restart the feature iterator
            Iterator featureIt2 = featureVec.iterator();
            while (featureIt2.hasNext()){
                Feature nextFeature = (Feature) featureIt2.next();
                Geometry geom = nextFeature.getDefaultGeometry();
                for (int x = 0; x < xCells; x++){
                    for (int y = 0; y < yCells; y++){
                        Point p = gf.createPoint(new Coordinate(localMinX + cellSize * x, localMinY + cellSize * y));
                        if (geom.contains(p)){
                            if (rasterArray[x][y] == 0){  //this pixel could already be 1 from a previous feature
                                rasterArray[x][y] = 1;
                                count++;
                            }
                        } else {
                            //don't change it!
//                            rasterArray[x][y] = 0;
                        }
                    }
                }                
            }
            
            MomentGenerator gen = new MomentGenerator();
            gen.setRaster(rasterArray);
            double[] these = gen.calculateMomentInvariants();
            moments[index] = these;
            names[index] = name;
            pixels[index] = count;
            System.out.print(index + ", " + count + ", " + name );
            for (int q = 0; q < 7; q++){
                System.out.print(", " + these[q]);
            }
            System.out.println();
            index++;            
            
        }
        
        try{
            FileWriter outFile = new FileWriter("C:/z4xNoSpaces/timeData/varyPixelSizeMoments/" + fileName + "Moments.csv");
            
            outFile.write("id, cityName, pixels, inv1, inv2, inv3, inv4, inv5, inv6, inv7" + '\n');
            for (int i = 0; i < moments.length; i++){
                outFile.write(i+ ", " + names[i] + ", " + pixels[i]);
                for (int j = 0; j < moments[i].length; j++){
                    outFile.write(", " + moments[i][j]); 
                }
                outFile.write('\n');
            }
            
            outFile.close();
            System.out.println("wrote moments for " + fileName);
            
        } catch (IOException ioe){
            System.out.println("IOException when saving summary file: " + ioe.getMessage());
        }                        
    }
    
    private static HashMap create90Keys(){
        HashMap ret = new HashMap();
        try{
            File f = new File("C:/z4xNoSpaces/timeData/moments/keys1990.csv");
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            String[][] values = csvp.getAllValues();
            
            for (int i = 0; i < values.length; i++){
                String key = values[i][0];
                for (int j = 1; j < values[i].length; j++){
                    if (!(values[i][j].equalsIgnoreCase("null"))){
                        ret.put(values[i][j], values[i][0]);
                    }
                }               
            }
        } catch (IOException ioe){
            System.out.println("IOException loading key file.  " + ioe.getMessage());
        }                
        return ret;
    }
    
    private static HashMap create00Keys(){
        HashMap ret = new HashMap();
        try{
            File f = new File("C:/z4xNoSpaces/timeData/moments/keys2000.csv");
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            String[][] values = csvp.getAllValues();
            
            for (int i = 0; i < values.length; i++){
                String key = values[i][0];
                for (int j = 1; j < values[i].length; j++){
                    if (!(values[i][j].equalsIgnoreCase("null"))){
                        ret.put(values[i][j], values[i][0]);
                    }
                }               
            }
        } catch (IOException ioe){
            System.out.println("IOException loading key file.  " + ioe.getMessage());
        }                
        return ret;       
    }
    
    private static String removeCommas(String in){
//        System.out.println("in string is " + in);
        int lastComma = in.lastIndexOf(",");
        String out = in.substring(lastComma + 1);
        int nextComma = lastComma;
        while (nextComma > 0){
            nextComma = in.lastIndexOf(",", lastComma - 1);
            out = in.substring(nextComma + 1, lastComma).concat(out);
            lastComma = nextComma;
        }        
//        System.out.println("out string is " + out);
        return out;
    }
    
}
