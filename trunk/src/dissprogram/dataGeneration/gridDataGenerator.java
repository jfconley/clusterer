/*
 * GridDataGenerator.java
 *
 * Created on March 19, 2007, 1:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.dataGeneration;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

/**
 *
 * @author jfc173
 */
public class gridDataGenerator {    

    private static double baseBandwidth = 2;
    private static int size = 200;    
    
    private static int[][] popArray;
    private static int[][] caseArray;
    private static long totalPop;
    private static Vector lines = new Vector();
    private static Vector polygons = new Vector();
    private static Random r = new Random();
    private static GeometryFactory factory = new GeometryFactory();
    private static double bandwidth = baseBandwidth;
    
    /**
     * Creates a new instance of GridDataGenerator
     */
    public gridDataGenerator() {
    }
    
    public static void loadLines(File f){
        try{
            ShapefileDataStore sfds = new ShapefileDataStore(f.toURL());
            FeatureCollection fc = sfds.getFeatureSource().getFeatures();
            FeatureIterator features = fc.features();
            while (features.hasNext()){
                Geometry geom = features.next().getDefaultGeometry();
//                System.out.println("bounds are " + geom.getEnvelopeInternal());
                if (geom instanceof MultiLineString){
//                    System.out.println("added a line string");
                    lines.add(geom);
                } else {
                    System.out.println("discarded a " + geom.toString());
                }                    
            }
        } catch (MalformedURLException mue){
            throw new RuntimeException("malformed URL: " + mue.getMessage());
        } catch (IOException ioe){
            throw new RuntimeException("io exception: " + ioe.getMessage());
        }    
    }
    
    public static void loadShapes(File f){
        try{
            ShapefileDataStore sfds = new ShapefileDataStore(f.toURL());
            FeatureCollection fc = sfds.getFeatureSource().getFeatures();
            FeatureIterator features = fc.features();
            while (features.hasNext()){
                Geometry geom = features.next().getDefaultGeometry();
//                System.out.println("bounds are " + geom.getEnvelopeInternal());
                if (geom instanceof MultiPolygon){
//                    System.out.println("added a polygon");
                    polygons.add(geom);
                } else {
                    System.out.println("discarded a " + geom.toString());
                }                    
            }
        } catch (MalformedURLException mue){
            throw new RuntimeException("malformed URL: " + mue.getMessage());
        } catch (IOException ioe){
            throw new RuntimeException("io exception: " + ioe.getMessage());
        }          
    }
    
    //generates n background points
    private static void createBackgroundCases(int n){
        double maxProb = -1;
        double[][] probArray = new double[size][size];
        for (int x = 0; x < size; x++){
            for (int y = 0; y < size; y++){
                probArray[x][y] = (double) popArray[x][y] / (double) totalPop;
                if (probArray[x][y] > maxProb){
                    maxProb = probArray[x][y];
                }
            }
        }
        
        if (maxProb == 0){
            throw new RuntimeException("max probability is zero, and I don't want to run forever.");
        }        
        
        for (int i = 0; i < n; i++){       
            boolean assigned = false;
            do{
                int randX = r.nextInt(size);
                int randY = r.nextInt(size);

                double u = r.nextDouble() * maxProb;

                if (u < probArray[randX][randY]){
                    caseArray[randX][randY]++;
                    assigned = true;
                }
            } while (!(assigned));
        }
    }
    
    private static void createLineCases(int n, MultiLineString l){
        double maxProb = -1;
        double[][] probArray = new double[size][size];
        for (int x = 0; x < size; x++){
            for (int y = 0; y < size; y++){
                //will want to transform x and y somehow to align with the line
                Point p;
                if (isLatLong(l)){
//                    System.out.println("lat long");
                    p = convertXYLatLong(x, y);
                } else {
//                    System.out.println("UTM");
                    p = convertXYUTM(x, y);
                }                               
                double dist = l.distance(p);
                //note: in the Brunsdon paper, the negation induced by the "0 -" term is missing, but if this is supposed to be 
                //proportional to a normal distribution, then the negation is needed.
                double prob = ((double) popArray[x][y] / (double) totalPop) * Math.exp(0 - (dist * dist) / (2 * bandwidth * bandwidth));
                probArray[x][y] = prob;
                if (prob > maxProb){
                    maxProb = prob;
                }
            }
        }
        
        if (maxProb == 0){
            throw new RuntimeException("max probability is zero, and I don't want to run forever.");
        }
        
        for (int i = 0; i < n; i++){       
            boolean assigned = false;
            do{
                int randX = r.nextInt(size);
                int randY = r.nextInt(size);

                double u = r.nextDouble() * maxProb;

                if (u < probArray[randX][randY]){
                    caseArray[randX][randY]++;
                    assigned = true;
                }
            } while (!(assigned));
        }        
    }
    
    private static void createPolygonCases(int n, MultiPolygon p){
        double maxProb = -1;
        double[][] probArray = new double[size][size];
        for (int x = 0; x < size; x++){
            for (int y = 0; y < size; y++){
                //will almost certainly want to transform x and y somehow to align with the polygon
                Point point;
                if (isLatLong(p)){
//                    System.out.println("lat long");
                    point = convertXYLatLong(x, y);
                } else {
//                    System.out.println("UTM");
                    point = convertXYUTM(x, y);
                }  
                double dist;
                if (p.contains(point)){
                    dist = 0;  //this will give a maximum rate because the exponential term will be e^0=1
                } else {
                    dist = p.distance(point);
                }
                //note: in the Brunsdon paper, the negation induced by the "0 -" term is missing, but if this is supposed to be 
                //proportional to a normal distribution, then the negation is needed.
                double prob = ((double) popArray[x][y] / (double) totalPop) * Math.exp(0 - (dist * dist) / (2 * bandwidth * bandwidth));
                probArray[x][y] = prob;
                if (prob > maxProb){
                    maxProb = prob;
                }
            }
        }
        
        if (maxProb == 0){
            throw new RuntimeException("max probability is zero, and I don't want to run forever.");
        }        
        
        for (int i = 0; i < n; i++){       
            boolean assigned = false;
            do{
                int randX = r.nextInt(size);
                int randY = r.nextInt(size);

                double u = r.nextDouble() * maxProb;

                if (u < probArray[randX][randY]){
                    caseArray[randX][randY]++;
                    assigned = true;
//                    System.out.println("assigned number " + i);
                }
            } while (!(assigned));
        }        
    }    
    
    private static void initArrays(){        
        popArray = new int[size][size];
        caseArray = new int[size][size];
        totalPop = 0;
        for (int x = 0; x < size; x++){
            for (int y = 0; y < size; y++){
                popArray[x][y] = 10000;
                caseArray[x][y] = 0;
                totalPop = totalPop + 10000;
            }
        }
    }
    
    private static Point convertXYLatLong(int x, int y){
        double newX = (x / (size / 5d)) - 80.5;
        double newY = (y / (size / 5d)) + 39;
        bandwidth = baseBandwidth / (size / 5d);  //yes, technically this is a side effect not proclaimed in the method name.  Oh well.
        return factory.createPoint(new Coordinate(newX, newY));
    }
    
    private static Point convertXYUTM(int x, int y){
        double newX = (x * (600000 / size)) + 300000;
        double newY = (y * (600000 / size)) + 4000000;
        bandwidth = baseBandwidth * (600000 / size);  //yes, technically this is a side effect not proclaimed in the method name.  Oh well.
        return factory.createPoint(new Coordinate(newX, newY));
    }
    
    private static boolean isLatLong(Geometry g){
        //note: only works for Western Hemisphere!
        return (g.getEnvelopeInternal().getMaxX() < 0);
    }
    
    private static void createData(int s, int baseBand, int numBack, int numLine, int numPoly){
        lines.clear();
        polygons.clear();
        size = s;
        baseBandwidth = baseBand;
        bandwidth = baseBand;  //this may not be needed, but just to be sure.
                
        loadLines(new File("C:/jconley/data/PA_stuff/testSeeds/rivers.shp"));
        loadLines(new File("C:/jconley/data/PA_stuff/testSeeds/roads.shp"));
        loadShapes(new File ("C:/jconley/data/PA_stuff/testSeeds/cities.shp"));
        loadShapes(new File ("C:/jconley/data/PA_stuff/testSeeds/soil1.shp"));
        loadShapes(new File ("C:/jconley/data/PA_stuff/testSeeds/soil2.shp"));
        
        initArrays();
        
        createBackgroundCases(numBack);  
        
        Iterator lineIt = lines.iterator();
        while (lineIt.hasNext()){
            createLineCases(numLine, (MultiLineString) lineIt.next());
//            System.out.println("finished another line");
        }
        
        Iterator polyIt = polygons.iterator();
        while (polyIt.hasNext()){
//            System.out.println("finished another polygon");
            createPolygonCases(numPoly, (MultiPolygon) polyIt.next());
        }
        
        //do something with the casesArray!!!

        
        try{
            String outFileName = "s" + size + "bw" + baseBand + "bk" + numBack + "l" + numLine + "p" + numPoly + ".csv";
            FileWriter outFile = new FileWriter("C:/jconley/diss/synthData/grid/" + outFileName);
            
            outFile.write("x, y, pop, cases" + '\n');
            for (int i = 0; i < size; i++){
                for (int j = 0; j < size; j++){
                    outFile.write(i + ", " + j + ", " + popArray[i][j] + ", " + caseArray[i][j] + '\n');
                }
            }
            
            outFile.close();
            System.out.println("wrote " + outFileName);
        } catch (IOException ioe){
            System.out.println("IOException when saving file: " + ioe.getMessage());
        }
        
    }
    
    
    public static void main(String[] args){
        createData(100, 1, 16000, 0, 0);
        createData(100, 1, 12000, 1000, 0);
        createData(100, 1, 8000, 2000, 0);
        createData(100, 1, 0, 4000, 0);
        createData(100, 1, 12000, 0, 1000);
        createData(100, 1, 8000, 0, 2000);
        createData(100, 1, 0, 0, 4000);
        createData(100, 1, 12000, 500, 500);
        createData(100, 1, 8000, 1000, 1000);
        createData(100, 1, 0, 2000, 2000);
        createData(100, 2, 16000, 0, 0);
        createData(100, 2, 12000, 1000, 0);
        createData(100, 2, 8000, 2000, 0);
        createData(100, 2, 0, 4000, 0);
        createData(100, 2, 12000, 0, 1000);
        createData(100, 2, 8000, 0, 2000);
        createData(100, 2, 0, 0, 4000);
        createData(100, 2, 12000, 500, 500);
        createData(100, 2, 8000, 1000, 1000);
        createData(100, 2, 0, 2000, 2000);
        createData(100, 3, 16000, 0, 0);
        createData(100, 3, 12000, 1000, 0);
        createData(100, 3, 8000, 2000, 0);
        createData(100, 3, 0, 4000, 0);
        createData(100, 3, 12000, 0, 1000);
        createData(100, 3, 8000, 0, 2000);
        createData(100, 3, 0, 0, 4000);
        createData(100, 3, 12000, 500, 500);
        createData(100, 3, 8000, 1000, 1000);
        createData(100, 3, 0, 2000, 2000);
        
        createData(200, 1, 16000, 0, 0);
        createData(200, 1, 12000, 1000, 0);
        createData(200, 1, 8000, 2000, 0);
        createData(200, 1, 0, 4000, 0);
        createData(200, 1, 12000, 0, 1000);
        createData(200, 1, 8000, 0, 2000);
        createData(200, 1, 0, 0, 4000);
        createData(200, 1, 12000, 500, 500);
        createData(200, 1, 8000, 1000, 1000);
        createData(200, 1, 0, 2000, 2000);
        createData(200, 2, 16000, 0, 0);
        createData(200, 2, 12000, 1000, 0);
        createData(200, 2, 8000, 2000, 0);
        createData(200, 2, 0, 4000, 0);
        createData(200, 2, 12000, 0, 1000);
        createData(200, 2, 8000, 0, 2000);
        createData(200, 2, 0, 0, 4000);
        createData(200, 2, 12000, 500, 500);
        createData(200, 2, 8000, 1000, 1000);
        createData(200, 2, 0, 2000, 2000);
        createData(200, 3, 16000, 0, 0);
        createData(200, 3, 12000, 1000, 0);
        createData(200, 3, 8000, 2000, 0);
        createData(200, 3, 0, 4000, 0);
        createData(200, 3, 12000, 0, 1000);
        createData(200, 3, 8000, 0, 2000);
        createData(200, 3, 0, 0, 4000);
        createData(200, 3, 12000, 500, 500);
        createData(200, 3, 8000, 1000, 1000);
        createData(200, 3, 0, 2000, 2000);
        
        createData(500, 1, 16000, 0, 0);
        createData(500, 1, 12000, 1000, 0);
        createData(500, 1, 8000, 2000, 0);
        createData(500, 1, 0, 4000, 0);
        createData(500, 1, 12000, 0, 1000);
        createData(500, 1, 8000, 0, 2000);
        createData(500, 1, 0, 0, 4000);
        createData(500, 1, 12000, 500, 500);
        createData(500, 1, 8000, 1000, 1000);
        createData(500, 1, 0, 2000, 2000);
        createData(500, 2, 16000, 0, 0);
        createData(500, 2, 12000, 1000, 0);
        createData(500, 2, 8000, 2000, 0);
        createData(500, 2, 0, 4000, 0);
        createData(500, 2, 12000, 0, 1000);
        createData(500, 2, 8000, 0, 2000);
        createData(500, 2, 0, 0, 4000);
        createData(500, 2, 12000, 500, 500);
        createData(500, 2, 8000, 1000, 1000);
        createData(500, 2, 0, 2000, 2000);
        createData(500, 3, 16000, 0, 0);
        createData(500, 3, 12000, 1000, 0);
        createData(500, 3, 8000, 2000, 0);
        createData(500, 3, 0, 4000, 0);
        createData(500, 3, 12000, 0, 1000);
        createData(500, 3, 8000, 0, 2000);
        createData(500, 3, 0, 0, 4000);
        createData(500, 3, 12000, 500, 500);
        createData(500, 3, 8000, 1000, 1000);
        createData(500, 3, 0, 2000, 2000);
    }
    
}
