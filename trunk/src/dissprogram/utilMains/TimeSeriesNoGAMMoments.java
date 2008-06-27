/*
 * TimeSeriesNoGAMMoments.java
 *
 * Created on June 6, 2007, 10:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.image.MomentGenerator;
import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author z4x
 */
public class TimeSeriesNoGAMMoments {
    
    /** Creates a new instance of TimeSeriesNoGAMMoments */
    public TimeSeriesNoGAMMoments() {
    }
    
    public static void main(String[] args){
        int[] sizes = new int[]{754, 1059, 886, 832, 903, 883, 830, 805, 832, 982};
        
        for (int folder = 2; folder < 10; folder++){
        
            float[][] raster = new float[975][975];
            float[][] smoothed = new float[975][975];
            double[][] moments = new double[sizes[folder]][7];  

            int time = 0;
            String[][] values;
            boolean notDone = true;
            MomentGenerator mg = new MomentGenerator();

            while (notDone){
                try{
                    File f = new File("C:/z4xNoSpaces/synthData/infect/run" + folder + "/inf75time" + time + ".csv");
                    FileReader in = new FileReader(f);
                    CSVParser csvp = new CSVParser(in);
                    values = csvp.getAllValues(); 
                    int rasterSum = 0;

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
                                    rasterSum++;
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
                                smoothed[i][j] = 1;
                            } else {
                                smoothed[i][j] = 0;
                            }
                        }
                    }

                    //create the moments from the smoothed raster.
                    mg.setRaster(raster);
                    moments[time/5] = new double[7];
                    System.arraycopy(mg.calculateMomentInvariants(), 0, moments[time/5], 0, 7);  //java 5
//                    moments[time/5] = Arrays.copyOf(mg.calculateMomentInvariants(), 7);  //java 6

    //                System.out.println("finished time step # " + time);
    //                System.out.println("this raster has " + rasterSum + " ones.");
                    System.out.print(time);
                    for (int a = 0; a < 7; a++){
                        System.out.print(", " + moments[time/5][a]);
                    }
                    System.out.println();                
                    time = time + 5;   

                } catch (FileNotFoundException fnfe){
                    //done.
                    notDone = false;
                } catch (IOException ioe){
                    System.out.println("exception when loading file: " + ioe.getMessage());
                }
            }        

            //write out the array
            try{
                FileWriter outFile = new FileWriter("C:/z4xNoSpaces/synthData/infect/run" + folder + "/baseMoments.csv");

                outFile.write("time, inv1, inv2, inv3, inv4, inv5, inv6, inv7" + '\n');
                for (int i = 0; i < moments.length; i++){
                    outFile.write(i*5 + "");
                    for (int j = 0; j < moments[i].length; j++){
                        outFile.write(", " + moments[i][j]); 
                    }
                    outFile.write('\n');
                }

                outFile.close();
                System.out.println("wrote base moment summary");

            } catch (IOException ioe){
                System.out.println("IOException when saving summary file: " + ioe.getMessage());
            }         

        }
    }
    
}
