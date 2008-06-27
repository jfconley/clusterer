/*
 * DissUtils.java
 *
 * Created on January 12, 2007, 3:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram;

import dissprogram.classification.ClassedArray;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.Raster;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.grid.GridRange;
import org.opengis.spatialschema.geometry.Envelope;

/**
 *
 * @author jfc173
 */
public class DissUtils {
    
    /** Creates a new instance of DissUtils */
    public DissUtils() {
    }
    
    public static FeatureCollection shapeFileToFeatures(URL shapefile){
        FeatureCollection fsShape = null;
        try{
            ShapefileDataStore store = new ShapefileDataStore(shapefile);
            String name = store.getTypeNames()[0];
            FeatureSource source = store.getFeatureSource(name);
            fsShape = source.getFeatures();        
        } catch (MalformedURLException mue){
            System.out.println("MalformedURLException for the shapefile.  So form it right!" + mue.getMessage());            
        } catch (IOException ioe){
            System.out.println("IOException loading the feature source." + ioe.getMessage());
        }
        return fsShape;
    }
    
    public static GridCoverage2D matrixToCoverage(float[][] rasterArray, Envelope bounds){
        GridCoverageFactory gcf = new GridCoverageFactory();
        
        CharSequence name = "this is a name";
               
        //the following code is taken from the GridCoverageFactory class, which chooses not to work in jar format for unknown resasons
        int width  = 0;
        int height = rasterArray.length;
        for (int j=0; j<height; j++) {
            final float[] row = rasterArray[j];
            if (row != null) {
                if (row.length > width) {
                    width = row.length;
                }
            }
        }
        final java.awt.image.WritableRaster raster;
        // Need to use JAI raster factory, since WritableRaster
        // does not supports TYPE_FLOAT as of J2SE 1.5.0_06.
        raster = com.sun.media.jai.codecimpl.util.RasterFactory.createBandedRaster(java.awt.image.DataBuffer.TYPE_FLOAT, width, height, 1, null);
        for (int j=0; j<height; j++) {
            int i=0;
            final float[] row = rasterArray[j];
            if (row != null) {
                for (; i<row.length; i++) {
                    raster.setSample(i, j, 0, row[i]);
                }
            }
            for (; i<width; i++) {
                raster.setSample(i, j, 0, Float.NaN);
            }
        }
        GridCoverage2D gc = gcf.create(name, raster, bounds);
        // end copying from GridCoverageFactory
        
        return gc;        
    }
    
    public static float[][] coverageToMatrix(GridCoverage2D coverage){
        Envelope bounds = coverage.getEnvelope();
        GridRange range = coverage.getGridGeometry().getGridRange();
        int dimensions = range.getDimension();
        int[] lengths = new int[dimensions];
        for (int i = 0; i < dimensions; i++){
            lengths[i] = range.getLength(i);
        }
        int[] lowers = range.getLowers();
        int[] uppers = range.getUppers();
        
        System.out.println("there are " + dimensions + " dimensions.");
        for (int i = 0; i < dimensions; i++){
            System.out.println("Dimension " + i);
            System.out.println("Min: " + lowers[i]);
            System.out.println("Max: " + uppers[i]);
            System.out.println("Len: " + lengths[i]);
        }
        
        double minXEnv = coverage.getEnvelope2D().getMinX();
        double minYEnv = coverage.getEnvelope2D().getMinY();
        double maxXEnv = coverage.getEnvelope2D().getMaxX();
        double maxYEnv = coverage.getEnvelope2D().getMaxY();
                
        System.out.println("Coverage envelope:");
        System.out.println("(" + minXEnv + "," + minYEnv + ") -- (" + maxXEnv + "," + maxYEnv + ")");
        
        double xIncrement = (maxXEnv - minXEnv)/lengths[0];
        double yIncrement = (maxYEnv - minYEnv)/lengths[1];
        
        System.out.println("xIncrement: " + xIncrement);
        System.out.println("yIncrement: " + yIncrement);
        
        float[][] raster = new float[lengths[1]][lengths[0]];
        Raster r = coverage.getRenderedImage().getData();
        for (int i = 0; i < lengths[0]; i++){
            for (int j = 0; j < lengths[1]; j++){
                float[] temp = new float[coverage.getDimension()];
                temp = r.getPixel(i, j, temp);
//                temp = coverage.evaluate(new Point2D.Double(minXEnv + i * xIncrement, minYEnv + j * yIncrement), temp);
                raster[j][i] = temp[0];  //yes, this seems backwards as does the [lengths[1]][lengths[0]] in the r declaration, but it works.
            }
        }
        
        return raster;
    }
    
    public static double[][] shiftToZScores(double[][] in){
        double[][] out = new double[in.length][in[0].length];
        double[] columnMeans = new double[in[0].length];
        double[] columnStDevs = new double[in[0].length];       
        
        //and now you see why matrix algebra features prominently in tests of processor speed...
        for (int i = 0; i < in.length; i++){
            for (int j = 0; j < in[i].length; j++){
                columnMeans[j] = columnMeans[j] + in[i][j];
            }
        }
        
        for (int j = 0; j < columnMeans.length; j++){
            columnMeans[j] = columnMeans[j] / in.length;
        }
        
        for (int i = 0; i < in.length; i++){
            for (int j = 0; j < in[i].length; j++){
                columnStDevs[j] = columnStDevs[j] + Math.pow(in[i][j] - columnMeans[j], 2);
            }
        }
        
        for (int j = 0; j < columnStDevs.length; j++){
            //do I want to use n or n-1?
            columnStDevs[j] = Math.sqrt(columnStDevs[j] / (in.length - 1));
        }
        
        for (int i = 0; i < in.length; i++){
            for (int j = 0; j < in[i].length; j++){
                out[i][j] = (in[i][j] - columnMeans[j]) / columnStDevs[j];
            }
        }
        
        return out;
    }     
    
    public static String[] extractClassNames(ClassedArray[] matrix){
        Vector v = new Vector();
        for (int i = 0; i < matrix.length; i++){
            String klass = matrix[i].getKlass();
            if (!(v.contains(klass))){
                v.add(klass);
            }
        }
        String[] ret = new String[v.size()];
        for (int j = 0; j < v.size(); j++){
            ret[j] = (String) v.get(j);
        }
        return ret;
    }
    
    public static double euclideanDist(double[] d1, double[] d2){
        if (d1.length != d2.length){
            throw new RuntimeException("I need two arrays of the same length for a distance calculation, not " + d1.length + " and " + d2.length);
        }
        double sum = 0;
        for (int i = 0; i < d1.length; i++){
            sum = sum + ((d1[i] - d2[i]) * (d1[i] - d2[i]));
        }
        return Math.sqrt(sum);
    }

    public static int findIndex(String[] classes, String klass){
        int ret = -1;
        for (int i = 0; i < classes.length; i++){
            if (klass.equalsIgnoreCase(classes[i])){
                ret = i;
            }
        }
        return ret;
    }    
    
    public static boolean contains(String[] array, String s){
        boolean ret = false;
        for (int i = 0; i < array.length; i++){
            if (array[i].equalsIgnoreCase(s)){
                ret = true;
            }
        }
        return ret;
    }   
    
    public static boolean hasSameElements(String[] array1, String[] array2){
        if (array1.length != array2.length){
            return false;
        } else {
            int numSame = 0;
            for (int i = 0; i < array1.length; i++){
                if (DissUtils.contains(array2, array1[i])){
                    numSame++;
                }
            }
            return numSame == array1.length;        
        }
    }

    public static double roundToThousandths(double d){
        return ((double) Math.round(d * 1000))/1000;
    }    
    
}


