/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package parallelUtilMains;

import dissprogram.image.MomentGenerator;
import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author jconley
 */
public class AffineMomentGenerator {

    public static void main(String[] args) {
        String[] types = new String[]{"airport"/*, "inf", "river", "road", "wind"*/};
        for (int type = 0; type < types.length; type++){
            try{
                SubsetGenerator zero = new SubsetGenerator(types[type], 250, 270);
                SubsetGenerator one = new SubsetGenerator(types[type], 270, 290);
                SubsetGenerator two = new SubsetGenerator(types[type], 290, 310);
                SubsetGenerator three = new SubsetGenerator(types[type], 310, 330);
                SubsetGenerator four = new SubsetGenerator(types[type], 330, 350);
                SubsetGenerator five = new SubsetGenerator(types[type], 350, 365);
                SubsetGenerator six = new SubsetGenerator(types[type], 365, 375);
//                SubsetGenerator seven = new SubsetGenerator(types[type], 875, 1000);

                Thread thread0 = new Thread(zero);
                Thread thread1 = new Thread(one);
                Thread thread2 = new Thread(two);
                Thread thread3 = new Thread(three);
                Thread thread4 = new Thread(four);
                Thread thread5 = new Thread(five);
                Thread thread6 = new Thread(six);
//                Thread thread7 = new Thread(seven);

                thread0.start();
                thread1.start();
                thread2.start();
                thread3.start();
                thread4.start();
                thread5.start();
                thread6.start();
//                thread7.start();

                thread0.join();
                thread1.join();
                thread2.join();
                thread3.join();
                thread4.join();
                thread5.join();
                thread6.join();
//                thread7.join();      
            } catch (InterruptedException ie){
                System.out.println(ie.getMessage());
                threadMessage("This thread was interrupted");
            }            
        }
    }

    private static void threadMessage(String message){
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }    

    private static class SubsetGenerator implements Runnable{

        String type;
        int min, max;
        
        public SubsetGenerator(String sType, int iMin, int iMax){
            type = sType;
            min = iMin;
            max = iMax;
        }
        
        @Override
        public void run() {
            generateMoments();
        }
                
        public void generateMoments(){
            MomentGenerator mg = new MomentGenerator();
                for(int run = min; run < max; run++){            
                int length = 0;
                try{
                    File summary = new File("D:/synthLandscan/randomStarts/" + type + "/run" + run + "/summary.csv");
                    FileReader in = new FileReader(summary);
                    CSVParser csvp = new CSVParser(in);
                    String[][] values = csvp.getAllValues();   
                    length = (values.length - 2) * 5;                    
                } catch (IOException ioe){
                    System.out.println("what the? " + ioe.getMessage());
                }        
                double[][] moments = new double[40][];
                for (int step = 0; step < 40; step++){
                    double interval = (double) length / 40d;
                    int time = nearestMultipleOfFive((int) Math.round(interval * step));
                    File file = new File("D:/synthLandscan/randomStarts/" + type + "/run" + run + "/" + type + "PAtime" + time + "GAMRaster.csv");
                    float[][] raster = rasterFromFile(file);
                    //run GAM & get a coverage out of it (or use the base raster)

                    mg.setRaster(raster);
                    //calculate moments
                    double[] temp = mg.calculateAffineInvariants();
                    moments[step] = new double[temp.length];
                    for (int blob = 0; blob < temp.length; blob++){
                        moments[step][blob] = temp[blob];
                    }
                    System.out.println("finished moments type " + type + ", run " + run + ", step " + step); 
                    try{
                        File momentFile = new File("D:/synthLandscan/randomStarts/" + type + "/run" + run + "/GAMAffineMoments" + step + ".csv");
                        FileWriter momentWriter = new FileWriter(momentFile);
                        momentWriter.write(""+temp[0]);
                        for (int l = 1; l < temp.length; l++){
                            momentWriter.write("," + temp[l]);
                        }
                        momentWriter.write('\n'); 
                        momentWriter.close();
                    } catch (IOException ioe){
                        System.out.println("IOException writing moment file.  " + ioe.getMessage());
                    }  
                }
            }
            
        }

        private int nearestMultipleOfFive(int in){
            int mod = in % 5;
            switch (mod){
                case 0:
                    return in;
                case 1:
                    return in - 1;
                case 2:
                    return in - 2;
                case 3:
                    return in + 2;
                case 4:
                    return in + 1;
                default:
                    System.out.println("this shouldn't happen.");
                    return in;
            }  
        }    

        private float[][] rasterFromFile(File f){
            float[][] ret = new float[1000][1000];
            try{
                FileReader in = new FileReader(f);        
                CSVParser csvp = new CSVParser(in);
                String[][] values = csvp.getAllValues(); 
                ret = new float[1000][1000];
                for (int i = 0; i < 1000; i++) {
                    for (int j = 0; j < 1000; j++){
    //                    System.out.println("[" + i + "," + j + "]=" + values[i][j]);
                        ret[i][j] = Float.parseFloat(values[i][j]);
                    }                       
                }
            } catch (IOException ioe){
                System.out.println("IOException: " + ioe.getMessage());
            }
            return ret;
        }    
    
    }    
    
}
