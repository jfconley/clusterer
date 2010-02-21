/*
 * BaseRasterViewer.java
 *
 * Created on July 19, 2007, 10:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileReader;
import javax.swing.JFrame;

/**
 *
 * @author z4x
 */
public class BaseRasterViewer {
    
    /** Creates a new instance of BaseRasterViewer */
    public BaseRasterViewer() {
    }
    
    public static void main(String[] args){
        String[][] values;
        float[][] raster = new float[765][765];  //975 for dissData, 765 for PA data
        float[][] smoothed = new float[765][765];
            
        try{
            File f = new File("D:/synthLandscan/randomStarts/inf/run1/infPAtime2205.csv");
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            values = csvp.getAllValues();         
        
            for (int i = 1; i < values.length; i++){
                int x = Integer.parseInt(values[i][0]);
                int y = Integer.parseInt(values[i][1]);
                int cases = Integer.parseInt(values[i][3]);
                int rasterMinX = 9*x;  //was 13
                int rasterMinY = 9*y;
                int rasterMaxX = 9*x + 8;
                int rasterMaxY = 9*y + 8;

                if (cases == 0){
                    for (int j = rasterMinX; j <= rasterMaxX; j++){
                        for (int k = rasterMinY; k <= rasterMaxY; k++){
                            raster[j][k] = 0;
                        }
                    }                        
                } else {
                    for (int j = rasterMinX; j <= rasterMaxX; j++){
                        for (int k = rasterMinY; k <= rasterMaxY; k++){
                            raster[j][k] = 1;
    //                        rasterSum++;
                        }
                    }                                
                }
            }

            //raster is a bit pixellated right now, so smooth it.
            for (int i = 0; i < raster.length; i++){
                for (int j = 0; j < raster[i].length; j++){
                    int zeros = 0;
                    int ones = 0;

                    int rasterMinX = Math.max(0, i-4);  //these were 6 for dissData, 4 for PA
                    int rasterMaxX = Math.min(i+4, raster.length-1);
                    int rasterMinY = Math.max(0, j-4);
                    int rasterMaxY = Math.min(j+4, raster[i].length-1);

                    for (int k = rasterMinX; k <= rasterMaxX; k++){
                        for (int l = rasterMinY; l <= rasterMaxY; l++){
                            if (raster[k][l] == 0){
                                zeros++;
                            } else{
                                ones++;
                            }
                        }
                    }

                    if (ones >= zeros){
                        smoothed[i][j] = 0;
                    } else {
                        smoothed[i][j] = 255;
                    }
                }
            }

            JFrame frame = new JFrame();
            KernelTestPanel panel = new KernelTestPanel();
            panel.setArray(smoothed);
            frame.getContentPane().add(panel);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
        } catch (Exception ioe){
            System.out.println("exception when loading file: " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }
    
}
