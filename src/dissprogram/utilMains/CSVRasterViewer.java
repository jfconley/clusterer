/*
 * CSVRasterViewer.java
 *
 * Created on March 11, 2008, 6:25 PM
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
 * @author Owner
 */
public class CSVRasterViewer {
    
    /** Creates a new instance of CSVRasterViewer */
    public CSVRasterViewer() {
    }
    
    public static void main(String[] args){
        String[][] values;
        float[][] raster;
        try{
            File f = new File("D:/synthLandscan/randomStarts/inf/run1/infPAtime2205GAMRaster.csv");
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            values = csvp.getAllValues();         
            raster = new float[values.length][(values[1].length-1)];
            float max = 0;
            for (int i = 0; i < values.length; i++){
                for (int j = 0; j < values[i].length-1; j++){
                    raster[i][j] = Float.parseFloat(values[i][j]);
                    max = Math.max(raster[i][j], max);
//                    for (int k = 0; k < 10; k++){
//                        for (int l = 0; l < 10; l++){
//                            raster[i*10+k][j*10+l] = Float.parseFloat(values[i][j]);
//                            max = Math.max(raster[i*10+k][j*10+l], max);
//                        }
//                    }
                }
            }

            for (int i = 0; i < raster.length; i++){
                for (int j = 0; j < raster[i].length; j++){
                    float temp = raster[i][j];
                    if (temp == 0){
                        raster[i][j] = 255;
                    } else {
                        raster[i][j] = 0;
                    }
                }
            }
            
            JFrame frame = new JFrame();
            KernelTestPanel panel = new KernelTestPanel();
            panel.setArray(raster);
            frame.getContentPane().add(panel);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
        } catch (Exception ioe){
            System.out.println("exception when loading file: " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }
    
}
