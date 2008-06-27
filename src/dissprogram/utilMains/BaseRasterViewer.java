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
        float[][] raster = new float[975][975];
        float[][] smoothed = new float[975][975];
            
        try{
            File f = new File("E:/endOfSummer/synthData/road/run0/road75time800.csv");
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            values = csvp.getAllValues();         
        
            for (int i = 1; i < values.length; i++){
                int x = Integer.parseInt(values[i][0]);
                int y = Integer.parseInt(values[i][1]);
                int cases = Integer.parseInt(values[i][3]);
                int rasterMinX = 13*x;
                int rasterMinY = 13*y;
                int rasterMaxX = 13*x + 12;
                int rasterMaxY = 13*y + 12;

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

                    int rasterMinX = Math.max(0, i-6);
                    int rasterMaxX = Math.min(i+6, raster.length-1);
                    int rasterMinY = Math.max(0, j-6);
                    int rasterMaxY = Math.min(j+6, raster[i].length-1);

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
