/*
 * EpanechnikovKernelGAM.java
 *
 * Created on June 4, 2007, 12:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.image;

import edu.psu.geovista.gam.Gene;
import edu.psu.geovista.io.csv.CSVParser;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author z4x
 */
public class EpanechnikovKernelGAM {
    
    double[][][] grids = new double[10][101][101];
    
    /**
     * Creates a new instance of EpanechnikovKernelGAM
     */
    public EpanechnikovKernelGAM() {
        for (int i = 0; i < grids.length; i++){                        
            try{
                File f = new File("C:/jconley/DissProgram/kernels/kernel" + i + ".csv");
                FileReader in = new FileReader(f);
                CSVParser csvp = new CSVParser(in);
                String[][] values = csvp.getAllValues();             
            
                for (int q = 0; q < values.length; q++){
                    for (int r = 0; r < values[q].length-1; r++){  //there's an extra comma at the end of each row, confusing the CSVParser
//                        System.out.println("getting value[" + i + "][" + q + "][" + r + "]");
                        grids[i][q][r] = Double.parseDouble(values[q][r]);
                    }
                }
//                System.out.println("loaded file #" + i);
            } catch (IOException ioe){
                System.out.println("Don't just sit there, DO SOMETHING!!!");
            }
        }
    }
    
    public double getValueAt(int pointX, int pointY, int geneX, int geneY, int radius, double height){        
//        System.out.println("trying to get value at [" + radius + "][" + (pointX - geneX + 50) + "][" + (pointY - geneY + 50) + "]");
        return grids[radius][pointX - geneX + 50][pointY - geneY + 50] * height;
    }
        
    
    
}
