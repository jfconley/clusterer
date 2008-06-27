/*
 * GAMKernelTest.java
 *
 * Created on June 4, 2007, 1:14 PM
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
import java.io.IOException;
import javax.media.jai.RenderedOp;
import javax.media.jai.WritableRenderedImageAdapter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.geotools.coverage.grid.GridCoverage2D;

/**
 *
 * @author z4x
 */
public class GAMKernelTest {
    
    /** Creates a new instance of GAMKernelTest */
    public GAMKernelTest() {
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
    
    public static void main(String[] args){
        try{
            File f = new File("C:/z4xNoSpaces/synthData/infect/run8/inf75time1500.csv");

            //most of this stuff is from BatchMain
            //run GAM on this file
            InitGAMFile igf = new InitGAMFile();
            igf.processTextFileWithoutDialog(f);
            
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

            //get the image
            GridCoverage2D coverage = ip.generateCoverageGAMOpt();

//            showMeTheImage(coverage);
        } catch (IOException ioe){
            System.out.println("exception when loading file: " + ioe.getMessage());
        }
    }
    
}
