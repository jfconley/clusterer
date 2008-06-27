/*
 * KernelUtilMain.java
 *
 * Created on June 4, 2007, 8:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.image.EpanechnikovKernel;
import edu.psu.geovista.gam.FitnessRelativePct;
import edu.psu.geovista.gam.Gene;
import edu.psu.geovista.gam.InitGAMFile;
import edu.psu.geovista.gam.SystematicGam;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author z4x
 */
public class KernelUtilMain {
    
    /** Creates a new instance of KernelUtilMain */
    public KernelUtilMain() {
    }
    
    public static void main(String[] args){
        //These numbers are for GAM with the synthetic infectious datasets
        double[] radii = new double[]{0.37, 0.74, 1.11, 1.48, 1.85, 2.22, 2.59, 2.96, 3.33, 3.70};
        
        double[][][] grids = new double[10][101][101];
        for (int i = 0; i < radii.length; i++){
            //set up Epanechnikov kernel centered at (0,0) with radius radii[i].
            EpanechnikovKernel kernel = new EpanechnikovKernel();
            kernel.setCentroid(new Point2D.Double(0,0));
            kernel.setHeight(1);
            kernel.setMajorAxis(radii[i]);
            kernel.setMinorAxis(radii[i]);
            kernel.setMajorAxisOrientation(0);
            
            for (int j = 0; j < grids[i].length; j++){
                for (int k = 0; k < grids[i][j].length; k++){
                    //convert j and k to (x, y) coordinates
                    //want centroid (i=50, j=50) at (0,0) and 0.074 per increment
                    double x = 0.074*(j-50);
                    double y = 0.074*(k-50);                    
                    grids[i][j][k] = kernel.getValueAt(new Point2D.Double(x, y));                    
                }
            }              
        
            try{
                FileWriter outFile = new FileWriter("C:/z4xNoSpaces/kernels/kernel" + i + ".csv");

                for (int x = 0; x < grids[i].length; x++){
                    for (int y = 0; y < grids[i][x].length; y++){
                        outFile.write(grids[i][x][y] + ", "); 
                    }
                    outFile.write('\n');
                }

                outFile.close();
                System.out.println("wrote grid " + i);

            } catch (IOException ioe){
                System.out.println("IOException when saving summary file: " + ioe.getMessage());
            }   
        }
        
    }
    
}
