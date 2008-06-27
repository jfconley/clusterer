/*
 * PAWindDataGenerator.java
 *
 * Created on February 18, 2008, 2:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.dataGeneration;

import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author jfc173
 */
public class PAWindDataGenerator {
    
    /** Creates a new instance of PAWindDataGenerator */
    public PAWindDataGenerator() {
    }
    
    static int[][] susceptible;
    static int[][] concentration;
    static int[][] infectious;
    static int[][] recovered;
    static int[][][] recoverNow;    
    static int initTotalPollution = 2500;
    static int[] pollutionSummary;
    static int[] infectedSummary;
    static int[] recoveredSummary;    
    static double infectionProbability = 0.001;
    static double depositionProbability = 0.002;
    static double motionProbability = 0.08;    
    
    public static void main (String[] args){
        String[][] values = new String[84][84];
        
        for (int run = 1; run < 50; run++){
            File folder = new File("E:/synthLandscan/wind/run" + run);
            folder.mkdir();        
            
            boolean done = false;
            while (!(done)){
                try{
                    File f = new File("E:/landscan/namerica06/export/nameraggcsv.csv");        
                    FileReader in = new FileReader(f);
                    CSVParser csvp = new CSVParser(in);
                    values = csvp.getAllValues();                       
                } catch (IOException ioe){
                    System.out.println("IOException: " + ioe.getMessage());
                }

                System.out.println("values is " + values.length + "x" + values[1].length);

                susceptible = new int[values.length][values[0].length];
                for (int i = 0; i < 84; i++){
                    for (int j = 1; j < 85; j++){
        //                System.out.println("values[" + i + "][" + j + "] is " + values[i][j]);
                        susceptible[j-1][i] = Integer.parseInt(values[i][j]);
                    }
                }            
            
                infectious = new int[values.length][values[0].length];
                recovered = new int[values.length][values[0].length];
                concentration = new int[values.length][values[0].length];
                //this assumes people recover after exactly 10 days.
                recoverNow = new int[10][values.length][values[0].length];
                int totalInfected = 0;     
                int totalRecovered = 0;
                int totalDead = 0;
                infectedSummary = new int[25];
                recoveredSummary = new int[25];
                pollutionSummary = new int[25];

                for (int i = 0; i < concentration.length; i++){
                    Arrays.fill(concentration[i], 0);
                }

                //start with all the pollution at a source at [10][10]
                concentration[10][10] = initTotalPollution;

                int[][] nextStepConcentration = new int[concentration.length][concentration[0].length];

                int time = 0;
                int airbornePollution = initTotalPollution;
                //for each time step
                while (airbornePollution > 0){
                    //make a copy to store the next time step without interfering with the current one.
                    nextStepConcentration = concentration;
//                    for (int i = 0; i < concentration.length; i++){
//                        nextStepConcentration[i] = new int[concentration[i].length];
//                        System.arraycopy(concentration[i], 0, nextStepConcentration[i], 0, concentration[i].length);  //java 5
        //               nextStepConcentration[i] = Arrays.copyOf(concentration[i], size);  //java 6
//                    }               
                    for (int i = 0; i < concentration.length; i++){
                        for (int j = 0; j < concentration[i].length; j++){
//                            System.out.println("deposition/motion at time " + time);
                            //for each particle at each location, it has a motionProbability chance of moving,
                            //a depositionProbability chance of being deposited, and if it moves, it is most likely
                            //but not guaranteed to move to the south or east.
                            for (int k = 0; k < concentration[i][j]; k++){
                                double r = Math.random();                        
                                if (r < depositionProbability){
                                    //deposition is a simple decrement
//                                    System.out.println("deposition at time step " + time);
                                    nextStepConcentration[i][j]--;
                                    airbornePollution--;
                                } else if (r < depositionProbability + motionProbability){
                                    //motion, decrement from here and increment in one of the neighbors
                                    nextStepConcentration[i][j]--;
                                    double s = Math.random();
                                    if (s < 0.05){
                                        if (i > 0){
                                            nextStepConcentration[i-1][j]++;
                                        } else {
                                            airbornePollution--;
                                        }                                       
                                    } else if (s < 0.1){
                                        if (j > 0){
                                            nextStepConcentration[i][j-1]++;                                
                                        } else {
                                            airbornePollution--;
                                        } 
                                    } else if (s < 0.55){
                                        if (i < concentration.length-1){
                                            nextStepConcentration[i+1][j]++;
                                        } else {
                                            airbornePollution--;
                                        } 
                                    } else {
                                        if (j < concentration[i].length-1){
                                            nextStepConcentration[i][j+1]++;
                                        } else {
                                            airbornePollution--;
                                        } 
                                    }
                                }  //end if (r < depositionProbability)
                            }  //end for (int k...
                        }  //end for (int j...
                    }  //end for (int i...   
                    concentration = nextStepConcentration;

                    if (time > 9){
                        for (int i = 0; i < infectious.length; i++){
                            for (int j = 0; j < infectious[i].length; j++){
                                for (int k = 0; k < recoverNow[time % 10][i][j]; k++){
                                    infectious[i][j]--;
                                    totalInfected--;        
                                    recovered[i][j]++;
                                    totalRecovered++;
                                }  //end for(int k...
                                recoverNow[time % 10][i][j] = 0;                        
                            }   //end for(int j...
                        }
                    }                

                    for (int i = 0; i < susceptible.length; i++){
                        for (int j = 0; j < susceptible[i].length; j++){
                            //for each person, they can get the disease from a particle in their 
                            //cell with probability infectionProbability (more particles means higher
                            //probability, as the localInfProb calculation shows).
                            double localInfProb = 1 - Math.pow((1 - infectionProbability), concentration[i][j]);
                            
                            if (localInfProb > 0){
                                for (int k = 0; k < susceptible[i][j]; k++){
                                    if (Math.random() <= localInfProb){
                                        susceptible[i][j]--;
                                        infectious[i][j]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i][j]++;                                
                                    }
                                }
                            }
                        }
                    }

                    if (time % 5 == 0){
                        writeData(time, run);
                        System.out.println("total airborne pollution is " + airbornePollution);
                        System.out.println("there are " + totalInfected + " infected.");
                        System.out.println(totalRecovered + " have recovered.");

                        if (time/5 >= pollutionSummary.length){
                            expandSummaryArraySizes();
                        }

                        pollutionSummary[time/5] = airbornePollution;
                        infectedSummary[time/5] = totalInfected;
                        recoveredSummary[time/5] = totalRecovered;                       
                    }

                    time++;
                }  //end while (airbornePollution > 0)                

                writeSummaries(run);                
                System.out.println("time is " + time);
                if (time > 250){
                    done = true;
                    System.out.println("finished one");
                }  
            }
        }
    }
    
    private static void expandSummaryArraySizes(){
        int[] newPollArray = new int[pollutionSummary.length * 2];
        int[] newInfArray = new int[infectedSummary.length * 2];
        int[] newRecArray = new int[recoveredSummary.length * 2];
        
        for (int i = 0; i < pollutionSummary.length; i++){
            newPollArray[i] = pollutionSummary[i];
            newInfArray[i] = infectedSummary[i];
            newRecArray[i] = recoveredSummary[i];            
        }
        
        pollutionSummary = newPollArray;
        infectedSummary = newInfArray;
        recoveredSummary = newRecArray;
    }     
    
    private static void writeData(int time, int run){
        try{
            String outFileName = "windPAtime" + time + ".csv";
            FileWriter outFile = new FileWriter("E:/synthLandscan/wind/run" + run + "/" + outFileName);
            
            outFile.write("x, y, pop, cases" + '\n');
            for (int i = 0; i < susceptible.length; i++){
                for (int j = 0; j < susceptible[i].length; j++){
                    int background = susceptible[i][j] + infectious[i][j] + recovered[i][j];
                    outFile.write(i + ", " + j + ", " + background + ", " + infectious[i][j] + '\n');
                }
            }
            
            outFile.close();
            System.out.println("wrote " + outFileName);
        } catch (IOException ioe){
            System.out.println("IOException when saving file: " + ioe.getMessage());
        }        
    }
    
    private static void writeSummaries(int run){ 
        try{
            FileWriter outFile = new FileWriter("E:/synthLandscan/wind/run" + run + "/summary.csv");
            
            outFile.write("time, infected, recovered, pollution" + '\n');
            for (int i = 0; i < infectedSummary.length; i++){
                if ((infectedSummary[i] > 0) || (recoveredSummary[i] > 0)){
                    outFile.write(i*5 + ", " + infectedSummary[i] + ", " + recoveredSummary[i] + ", " + pollutionSummary[i] + '\n');
                }
            }
            
            outFile.close();
            System.out.println("wrote summary");
            
        } catch (IOException ioe){
            System.out.println("IOException when saving summary file: " + ioe.getMessage());
        } 
    }    
    
}
