/*
 * LandscanGamOptTest.java
 *
 * Created on February 21, 2008, 11:35 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.image.ImageProcessor;
import edu.psu.geovista.gam.FitnessRelativePct;
import edu.psu.geovista.gam.InitGAMFile;
import edu.psu.geovista.gam.SystematicGam;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.media.jai.RenderedOp;
import javax.media.jai.WritableRenderedImageAdapter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.AbstractProcessor;

/**
 *
 * @author jfc173
 */
public class LandscanGamOptTest {
    
    /** Creates a new instance of LandscanGamOptTest */
    public LandscanGamOptTest() {
    }
    
    public static void main(String[] args){
        File file = new File("D:/synthLandscan/inf/run0/infPAtime600.csv");
        GridCoverage2D coverage;
        //run GAM & get a coverage out of it
        coverage = rasterFromFile(file);
        showMeTheImage(coverage);        
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
    
    private static GridCoverage2D rasterFromFile(File inFile){
        //most of this stuff is from BatchMain
        //run GAM on this file
        InitGAMFile igf = new InitGAMFile();
        try{
            igf.processTextFileWithoutDialog(inFile);
        } catch (FileNotFoundException fnfe){
            System.out.println("why isn't " + inFile + " found?  " + fnfe.getMessage());
        } catch (IOException ioe){
            System.out.println("io exception: " + ioe.getMessage());
        }

        SystematicGam gam = new SystematicGam();
        double largeDimension = Math.max(igf.getMaxX() - igf.getMinX(),
                                         igf.getMaxY() - igf.getMinY());              
        gam.setMaxRadius(0.05 * largeDimension);
        gam.setMinRadius(0.005 * largeDimension);
        gam.setMinPoints(3);
        gam.setMinAccepted(1.5);
        gam.setFitnessFunction(new FitnessRelativePct(igf.getDataSet()));  
        gam.setInitializer(igf);  

        ImageProcessor ip = new ImageProcessor(); 
        ip.setInitializer(igf);
        ip.setGam(gam);                 
        System.out.println("running GAM");
        //get the image
        GridCoverage2D coverage = ip.generateCoverageGAMOpt();           
        return coverage;                    
    }        
    
}
