/*
 * KernelTest.java
 *
 * Created on September 26, 2006, 11:55 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.*;
import dissprogram.image.EpanechnikovKernel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author jfc173
 */
public class KernelTest {
    
    /** Creates a new instance of KernelTest */
    public KernelTest() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        double centroidx1 = 153;
        double centroidy1 = 153;
        double majorAxis1 = 81;
        double minorAxis1 = 81;
        double majorAxisOrientation1 = 0;
        double height1 = 100;
        
        double centroidx2 = 224;
        double centroidy2 = 132;
        double majorAxis2 = 93;
        double minorAxis2 = 48;
        double majorAxisOrientation2 = 60;
        double height2 = 155;
        
        EpanechnikovKernel kernel1 = new EpanechnikovKernel();
        EpanechnikovKernel kernel2 = new EpanechnikovKernel();
        
        kernel1.setCentroid(new Point2D.Double(centroidx1, centroidy1));
        kernel1.setHeight(height1);
        kernel1.setMajorAxis(majorAxis1);
        kernel1.setMinorAxis(minorAxis1);
        kernel1.setMajorAxisOrientation(majorAxisOrientation1);
        
        kernel2.setCentroid(new Point2D.Double(centroidx2, centroidy2));
        kernel2.setHeight(height2);
        kernel2.setMajorAxis(majorAxis2);
        kernel2.setMinorAxis(minorAxis2);
        kernel2.setMajorAxisOrientation(majorAxisOrientation2);
        
        double[][] rasterArray = new double[250][250];
        
        for(int x = 0; x < 250; x++){
            for (int y = 0; y < 250; y++){                
                rasterArray[x][y] = kernel1.getValueAt(new Point2D.Double(x, y)) +
                                    kernel2.getValueAt(new Point2D.Double(x, y));
            }
        }
        
        KernelTestPanel ktp = new KernelTestPanel();
        ktp.setArray(rasterArray);
        
        JFrame frame = new JFrame();          
        frame.getContentPane().add(ktp);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
}
