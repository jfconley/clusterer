/*
 * BatchMain.java
 *
 * Created on January 22, 2007, 4:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram;

import dissprogram.image.ImageBreaker;
import dissprogram.image.ImageProcessor;
import dissprogram.image.MomentGenerator;
import edu.psu.geovista.gam.FitnessRelativePct;
import edu.psu.geovista.gam.InitGAMFile;
import edu.psu.geovista.gam.RandomGam;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.media.jai.RenderedOp;
import javax.media.jai.WritableRenderedImageAdapter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.coverage.processing.Operations;
import org.opengis.coverage.processing.Operation;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author jfc173
 */
public class BatchMain {
    
    /** Creates a new instance of BatchMain */
    public BatchMain() {        
    }
    
    private static void showMeTheImage(GridCoverage2D gc){
        Image image = null;
        if (gc.getRenderedImage() instanceof WritableRenderedImageAdapter){
            image = ((WritableRenderedImageAdapter) gc.getRenderedImage()).getAsBufferedImage();
        } else if (gc.getRenderedImage() instanceof RenderedOp){
            image = ((RenderedOp) gc.getRenderedImage()).getAsBufferedImage();
        }
        JLabel imageLabel = new JLabel(new ImageIcon(image));         
        JFrame frame = new JFrame();
        frame.setTitle("All clusters");
        frame.getContentPane().add(imageLabel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);    
    }
    
    public static void main(String[] args) {
        //load file
        File f = null;        
        InitGAMFile initializer = new InitGAMFile();
        ImageProcessor ip = new ImageProcessor();              
        
        try{
            f = new File("C:/jconley/data/popset1a.csv");
            initializer.processTextFileWithoutDialog(f); 
        }
        catch (FileNotFoundException fnfe){
            throw new RuntimeException("No file!  " + fnfe.getMessage());            
        }
        catch (IOException ioe){
            throw new RuntimeException("IO Exception!  " + ioe.getMessage());
        }
        
        RandomGam gam = new RandomGam();
        ((RandomGam) gam).setNumTests(500);
        double largeDimension = Math.max(initializer.getMaxX() - initializer.getMinX(),
                                         initializer.getMaxY() - initializer.getMinY());              
        gam.setMaxRadius(0.05 * largeDimension);
        gam.setMinRadius(0.005 * largeDimension);
        gam.setMinPoints(3);
        gam.setMinAccepted(1.5);
        gam.setFitnessFunction(new FitnessRelativePct(initializer.getDataSet()));  
        gam.setInitializer(initializer);  

        ip.setInitializer(initializer);
        ip.setGam(gam);         
        
        int runs = 1000;
        System.out.println("Cluster, back, pixels, Inv 1, Inv 2, Inv 3, Inv 4, Inv 5, Inv 6, Inv 7");        
        for (int i = 0; i < runs; i++){   
            //create the coverage
//            RandomGam gam = new RandomGam();
//            ((RandomGam) gam).setNumTests(500);
//            double largeDimension = Math.max(initializer.getMaxX() - initializer.getMinX(),
//                                             initializer.getMaxY() - initializer.getMinY());              
//            gam.setMaxRadius(0.05 * largeDimension);
//            gam.setMinRadius(0.005 * largeDimension);
//            gam.setMinPoints(3);
//            gam.setMinAccepted(1.5);
//            gam.setFitnessFunction(new FitnessRelativePct(initializer.getDataSet()));  
//            gam.setInitializer(initializer);  
//            
//            ip.setInitializer(initializer);
//            ip.setGam(gam);
            GridCoverage2D coverage = ip.generateCoverage();
            
            //binarize it
            int floor = (int) Math.floor(ip.getMinHeight());
            int ceiling = (int) Math.ceil(ip.getMaxHeight());
            double scale = (double) 256 / (ceiling - floor);
            int increment = (Math.max(1, (int) Math.round((ceiling - floor) / 10)));

//            System.out.println("floor = " + floor);
//            System.out.println("ceiling = " + ceiling);
//            System.out.println("scale = " + scale);
//            System.out.println("increment = " + increment);

            AbstractProcessor def = DefaultProcessor.getInstance(); 
            Operations ops = new Operations(new RenderingHints(null));
            Operation binOp = def.getOperation("Binarize");
            ParameterValueGroup pvg = binOp.getParameters();

            GridCoverage2D band0 = (GridCoverage2D) ops.selectSampleDimension(coverage, new int[]{0});
            pvg.parameter("Source").setValue(band0);

            pvg.parameter("Threshold").setValue(new Double(floor + (ceiling - floor)/4));
            GridCoverage2D binarized = (GridCoverage2D) def.doOperation(pvg);  
          
//            showMeTheImage(binarized);
            
            //segment it
            ImageBreaker ib = new ImageBreaker();
            ib.setBatch(true);      //this causes it to skip creating grid coverages that aren't used in batch mode and that can cause errors.
            ib.setImage(binarized);
            ib.isolateClusters();
            
            //do the moment stuff
            float[][][] rasters = ib.getRasterArrays();      
            boolean[] isBackground = ib.getIsBackground();
            double[][] momentMatrix = new double[rasters.length][];
            int[] pixelCounts = ib.getPixelCounts();
            for (int j = 0; j < rasters.length; j++){
                MomentGenerator mgf = new MomentGenerator();
                mgf.setRaster(rasters[j]);
                momentMatrix[j] = mgf.calculateMomentInvariants();                
            }
            
            //eventually do something other than sout
            for (int j = 0; j < momentMatrix.length; j++){
                System.out.print(j + ", ");
                if (isBackground[j]){
                    System.out.print("back, ");
                } else {
                    System.out.print("clust, ");
                }
                System.out.print(pixelCounts[j]);
                for (int k = 0; k < momentMatrix[j].length; k++){
                    System.out.print(", " + momentMatrix[j][k]);
                }           
                System.out.println();
            }
        }
        
    }
    
}
