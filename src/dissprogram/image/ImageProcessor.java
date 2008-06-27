/*
 * ImageProcessor.java
 *
 * Created on May 2, 2006, 2:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.image;

import dissprogram.*;
import edu.psu.geovista.gam.AbstractGam;
import edu.psu.geovista.gam.Gene;
import edu.psu.geovista.gam.InitGAMFile;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Vector;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
//import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
//import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.spatialschema.geometry.Envelope;

/**
 *
 * @author jfc173
 */
public class ImageProcessor {
    
    InitGAMFile igf;
    AbstractGam gam;
    float maxHeight, minHeight;
    boolean writeFileFlag = true;
    
    /** Creates a new instance of ImageProcessor */
    public ImageProcessor() {
    }
    
    public void setInitializer(InitGAMFile d){
        igf = d;        
    }
    
    public void setGam(AbstractGam g){
        gam = g;
    }
        
    public AbstractGam getGam(){
        return gam;
    }
    
    public float getMinHeight(){
        return minHeight;
    }
    
    public float getMaxHeight(){
        return maxHeight;
    }
    
    public GridCoverage2D generateCoverageFromFile(File f){
        String[][] data;
        try{
            FileReader dataReader = new FileReader(f);           
            edu.psu.geovista.io.csv.CSVParser csvp = new edu.psu.geovista.io.csv.CSVParser(dataReader);
            data = csvp.getAllValues();              
        } catch (Exception e){
            throw new RuntimeException("Exception reading file: " + e.getMessage());
        }
        
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        
        Vector kernels = new Vector();        
        for (int i = 1; i < data.length; i++){
            EpanechnikovKernel ep = new EpanechnikovKernel();
            ep.setCentroid(new Point2D.Double(Double.parseDouble(data[i][1]), Double.parseDouble(data[i][3])));
            ep.setHeight(Double.parseDouble(data[i][15]));  //was Double.parsedouble(data[i][17])
            ep.setMajorAxis(Double.parseDouble(data[i][5]));
            ep.setMajorAxisOrientation(0);  //was Double.parseDouble(data[i][9])
            ep.setMinorAxis(Double.parseDouble(data[i][7]));
            kernels.add(ep);
            if (Double.parseDouble(data[i][1]) - Double.parseDouble(data[i][5]) < minX){
                minX = Double.parseDouble(data[i][1]) - Double.parseDouble(data[i][5]);
            }
            if (Double.parseDouble(data[i][1]) + Double.parseDouble(data[i][5]) > maxX){
                maxX = Double.parseDouble(data[i][1]) + Double.parseDouble(data[i][5]);
            }   
            if (Double.parseDouble(data[i][3]) - Double.parseDouble(data[i][5]) < minY){
                minY = Double.parseDouble(data[i][3]) - Double.parseDouble(data[i][5]);
            }
            if (Double.parseDouble(data[i][3]) + Double.parseDouble(data[i][5]) > maxY){
                maxY = Double.parseDouble(data[i][3]) + Double.parseDouble(data[i][5]);
            }              
        }
        
        double deltaX = maxX - minX;
        double deltaY = maxY - minY;
        System.out.println("minX = " + minX);
        System.out.println("maxX = " + maxX);
        System.out.println("minY = " + minY);
        System.out.println("maxY = " + maxY);
        
        double cellSize = Math.max(deltaX / 1000, deltaY / 1000);
        
        int xCells = (int) Math.round(deltaX / cellSize);
        int yCells = (int) Math.round(deltaY / cellSize);
        
        //for some reason, having the normal [xCells][yCells] order flipped it around
        //same goes for (i and j) and (x and y) in assignment statements in loops further down.
        float[][] rasterArray = new float[yCells][xCells];
        
        minHeight = Float.MAX_VALUE;
        maxHeight = Float.MIN_VALUE;
        
        for (int i = 0; i < xCells; i++){
            for (int j = 0; j < yCells; j++){
                rasterArray[j][i] = 0;
            }
        }
        
        int debugIndex = 0;
        System.out.println("There are " + kernels.size() + " kernels.");
        Iterator kernelIt = kernels.iterator();
        while (kernelIt.hasNext()){
            EpanechnikovKernel nextKernel = (EpanechnikovKernel) kernelIt.next();
            for (int x = 0; x < xCells; x++){
                for (int y = 0; y < yCells; y++){
                    Point2D.Double here = new Point2D.Double(minX + cellSize * x, minY + cellSize * y);
                    rasterArray[y][x] = rasterArray[y][x] + (float) nextKernel.getValueAt(here);
                }
            }
            debugIndex++;
            System.out.println("processed kernel #" + debugIndex);
        }

        for (int x = 0; x < xCells; x++){  //there should be a way to fold this into the above loop.  I'll look at that later.
            for (int y = 0; y < yCells; y++){
                if (rasterArray[y][x] < minHeight){
                    minHeight = rasterArray[y][x];
                }
                if (rasterArray[y][x] > maxHeight){
                    maxHeight = rasterArray[y][x];
                }
            }
        }        
        
        GridCoverage2D gc = DissUtils.matrixToCoverage(rasterArray, new GeneralEnvelope(new Rectangle2D.Double(minX, minY, deltaX, deltaY)));        
        
        return gc; 
    }
    
    public void processLoadedGridCoverage(GridCoverage2D coverage){
        //I need to do this to get the maximum and minimum values of the coverage.
        maxHeight = (float) coverage.getSampleDimension(0).getMaximumValue();
        minHeight = (float) coverage.getSampleDimension(0).getMinimumValue();
    }
    
    public GridCoverage2D generateCoverage(){
        gam.setInitializer(igf);
        Vector solutions = gam.run();             
        
        if (writeFileFlag){
            File out = new File("E:/OH_stuff/GAMcrashClustersTrimmed.csv");
            try{
                java.io.FileWriter outWriter = new java.io.FileWriter(out);
                Iterator blah = solutions.iterator();
                while (blah.hasNext()){
                    outWriter.write(((Gene) blah.next()).toCSVString());
                }
            } catch (java.io.IOException ioe){
                System.out.println("whinge horribly.");
            }
        }
        
        Vector kernels = new Vector();
        Iterator it = solutions.iterator();
        while (it.hasNext()){
            Gene next = (Gene) it.next();
            EpanechnikovKernel ep = new EpanechnikovKernel();
            ep.setCentroid(new Point2D.Double(next.getX(), next.getY()));
            ep.setHeight(next.getFitness());
            ep.setMajorAxis(next.getMajorAxisRadius());
            ep.setMajorAxisOrientation(next.getOrientation());
            ep.setMinorAxis(next.getMinorAxisRadius());
            kernels.add(ep);
        }
        
        double deltaX = igf.getMaxX() - igf.getMinX();
        double deltaY = igf.getMaxY() - igf.getMinY();
        
        double cellSize = Math.max(deltaX / 1000, deltaY / 1000);
        
        int xCells = (int) Math.round(deltaX / cellSize);
        int yCells = (int) Math.round(deltaY / cellSize);
        
        //for some reason, having the normal [xCells][yCells] order flipped it around
        //same goes for (i and j) and (x and y) in assignment statements in loops further down.        
        float[][] rasterArray = new float[yCells][xCells];
        
        minHeight = Float.MAX_VALUE;
        maxHeight = Float.MIN_VALUE;
        
        for (int i = 0; i < xCells; i++){
            for (int j = 0; j < yCells; j++){
                rasterArray[j][i] = 0;
            }
        }
        
        Iterator kernelIt = kernels.iterator();
        while (kernelIt.hasNext()){
            EpanechnikovKernel nextKernel = (EpanechnikovKernel) kernelIt.next();
            for (int x = 0; x < xCells; x++){
                for (int y = 0; y < yCells; y++){
                    Point2D.Double here = new Point2D.Double(igf.getMinX() + cellSize * x, igf.getMinY() + cellSize * y);
                    rasterArray[y][x] = rasterArray[y][x] + (float) nextKernel.getValueAt(here);
                }
            }
        }

        for (int x = 0; x < xCells; x++){  //there should be a way to fold this into the above loop.  I'll look at that later.
            for (int y = 0; y < yCells; y++){
                if (rasterArray[y][x] < minHeight){
                    minHeight = rasterArray[y][x];
                }
                if (rasterArray[y][x] > maxHeight){
                    maxHeight = rasterArray[y][x];
                }
            }
        }        
        
        GridCoverage2D gc = DissUtils.matrixToCoverage(rasterArray, new GeneralEnvelope(new Rectangle2D.Double(igf.getMinX(), igf.getMinY(), deltaX, deltaY)));        
        
        return gc;
    }
    
    public GridCoverage2D generateCoverageGAMOpt(){
        //optimized for using GAM on the synthetic infectious, road, and wind data
        //DO NOT USE FOR ANYTHING ELSE!
        //Adjusted for synthLandScan by increasing cellSize (that should work, right?)
//        System.out.println("using the GAM optimization in ImageProcessor");
        
        gam.setInitializer(igf);
        Vector solutions = gam.run();       
                
        double cellSize = 0.084;
        float[][] rasterArray = new float[1000][1000];
        
        minHeight = Float.MAX_VALUE;
        maxHeight = Float.MIN_VALUE;           
        EpanechnikovKernelGAM ekg = new EpanechnikovKernelGAM();        
        
        Iterator kernelIt = solutions.iterator();
//        int size = solutions.size();
//        int index = 0;  //only used for debugging.
        while (kernelIt.hasNext()){            
            Gene next = (Gene) kernelIt.next();
            int geneX = (int) Math.round((next.getX() / cellSize) + 50);
            int geneY = (int) Math.round((next.getY() / cellSize) + 50);
            double height = next.getFitness();
            
            int minX = Math.max(0, geneX - 50);
            int minY = Math.max(0, geneY - 50);
            int maxX = Math.min(1000, geneX + 51);
            int maxY = Math.min(1000, geneY + 51);
            int radius = (int) Math.round(next.getMajorAxisRadius() / (cellSize * 5));  //*5 to get it into integer territory (rather than 0.1, 0.2, ...)
            if (radius > 9){
                radius = 9; //this little statement for FZ, which can occasionally give 10.
            }
            System.out.println("real radius is " + next.getMajorAxisRadius() + " so radius is " + radius);
            for (int x = minX; x < maxX; x++){  
                for (int y = minY; y < maxY; y++){    
                    rasterArray[x][y] = rasterArray[x][y] + (float) ekg.getValueAt(x, y, geneX, geneY, radius, height);                                    
                }
            }
//            System.out.println("finished gene " + index + " of " + size);
        }
        
        for (int x = 0; x < 1000; x++){
            for (int y = 0; y < 1000; y++){
                if (rasterArray[x][y] < minHeight){
                    minHeight = rasterArray[x][y];
                }
                if (rasterArray[x][y] > maxHeight){
                    maxHeight = rasterArray[x][y];
                }
            }
        }
        
       return DissUtils.matrixToCoverage(rasterArray, new GeneralEnvelope(new Rectangle2D.Double(igf.getMinX(), igf.getMinY(), 74, 74)));                
    }
    
}
