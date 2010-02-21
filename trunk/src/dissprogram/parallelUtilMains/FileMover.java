/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package parallelUtilMains;

import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author jconley
 */
public class FileMover {

    public void FileMover(){        
    }
    
    public static void main(String[] args) {
//        String[] knnMethods = new String[]{"DW", "simple"};
//        String[] momentTypes = new String[]{"GAMAffine", "GAMLi"};
//        for (int i = 0; i < momentTypes.length; i++) {
//            String momentType = momentTypes[i];
//            for (int j = 0; j < knnMethods.length; j++) {
//                String knnMethod = knnMethods[j];
//                for (int k = 2; k < 301; k++){
                    try{
//                        MoveSome ms0 = new MoveSome("inf", knnMethod, momentType, 200, 1000, k);
//                        MoveSome ms1 = new MoveSome("river", knnMethod, momentType, 200, 1000, k);
//                        MoveSome ms2 = new MoveSome("road", knnMethod, momentType, 200, 1000, k);
//                        MoveSome ms3 = new MoveSome("wind", knnMethod, momentType, 200, 1000, k);
//                        MoveSome ms4 = new MoveSome("airport", knnMethod, momentType, 200, 1000, k);

                        CondenseSome cs0 = new CondenseSome("simple", "");
                        CondenseSome cs1 = new CondenseSome("DW", "");
//                        CondenseSome cs2 = new CondenseSome("simple", "Li");
//                        CondenseSome cs3 = new CondenseSome("DW", "Li");
//                        CondenseSome cs4 = new CondenseSome("simple", "FullSluzek");
//                        CondenseSome cs5 = new CondenseSome("DW", "FullSluzek");
                        
                        Thread t0 = new Thread(cs0);
                        Thread t1 = new Thread(cs1);
//                        Thread t2 = new Thread(cs2);
//                        Thread t3 = new Thread(cs3);
//                        Thread t4 = new Thread(cs4);
//                        Thread t5 = new Thread(cs5);

                        t0.start();
                        t1.start();
//                        t2.start();
//                        t3.start();
//                        t4.start();
//                        t5.start();

                        t0.join();
                        t1.join();
//                        t2.join();
//                        t3.join();
//                        t4.join();
//                        t5.join();
                        
                    } catch (InterruptedException ie){
                        System.out.println(ie.getMessage());
                        threadMessage("This thread was interrupted");
                    }
//                }
//            }
//        }

    }
    
    private static void threadMessage(String message){
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }      
    
    private static class MoveSome implements Runnable{
        
        String method;
        String knnType;
        String type;
        int minRun;
        int maxRun;
        int k;
        
        public MoveSome(String sType, String sKType, String sMethod, int iMinRun, int iMaxRun, int iK){
            method = sMethod;
            minRun = iMinRun;
            maxRun = iMaxRun;
            knnType = sKType;
            type = sType;
            k = iK;
        }
        
        public void run(){
            String newDirectory = "D:/synthLandscan/randomStarts/momentData/" + method + "/miniFiles/" + knnType + "/" + type + "/" + k;
            File newDir = new File(newDirectory);
            newDir.mkdirs();
            for (int run = minRun; run < maxRun; run++) {
                String fileString = "D:/synthLandscan/randomStarts/momentData/" + method + "/miniFiles/"+ knnType + method + "Classifications" + k + type + "run" + run + ".csv";
                File f = new File(fileString);
                if (f.exists()){
                    String newFileName = newDirectory + "/" +  method + "ClassificationsRun" + run + ".csv";
                    File newFile = new File(newFileName);
                    boolean success = f.renameTo(newFile);
                    if (success){
                        System.out.println("Moved " + newFileName);
                    } else {
                        System.out.println("Didn't move " + newFileName);
                    }
                } else {
                    System.out.println(fileString + "didn't exist.");
                }
            }
        }
        
    }
    
    private static class CondenseSome implements Runnable{
        
        String knn;
        String moment;
        
        public CondenseSome(String knnType, String momentType){
            knn = knnType;
            moment = momentType;
        }
        
        public void run(){
            for (int k = 2; k < 301; k++){
                try{
                    File newFile = new File("D:/synthLandscan/randomStarts/momentData/GAM" + moment + "/" + knn + "GAM" + moment + "Classifications" + k + ".csv");
                    FileWriter newFileWriter = new FileWriter(newFile);
                    String[] modelTypes = new String[]{"inf", "road", "wind", "river", "airport"};
                    newFileWriter.write("mode, SIR, river, road, wind, airport, THETA, correct" + '\n');
                    for (int model = 0; model < modelTypes.length; model++){
                        String modelString = modelTypes[model];                    
                        for (int run = 200; run < 1000; run++){
                            File inFile = new File("D:/synthLandscan/randomStarts/momentData/GAM" + moment + "/minifiles/" + knn + "/" + modelString + "/" + k + "/GAM" + moment + "ClassificationsRun" + run + ".csv");
                            FileReader inReader = new FileReader(inFile);
                            CSVParser csvp = new CSVParser(inReader);
                            String[][] values = csvp.getAllValues();
                            for (int line = 1; line < values.length; line++){
                                for (int cell = 0; cell < values[line].length; cell++){
                                    newFileWriter.write(values[line][cell] + ", ");
                                }
                                newFileWriter.write(modelString + '\n');
                            }
                        }
                    }
                    newFileWriter.close();
                } catch (IOException ioe){
                    System.out.println("IOException: " + ioe.getMessage());
                }
                System.out.println("Finished " + knn + "GAM" + moment + "Classifications" + k + ".csv");                
            }
        }
        
    }            
    
}
