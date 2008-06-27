/*
 * WindDataGenerator.java
 *
 * Created on June 15, 2007, 8:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.dataGeneration;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author z4x
 */
public class WindDataGenerator {
    
    /** Creates a new instance of WindDataGenerator */
    public WindDataGenerator() {
    }
    
    static int size = 75;
    static int[][] concentration;
    static int initTotalPollution = 2500;
    static int[] pollutionSummary;
    static double depositionProbability = 0.002;
    static double motionProbability = 0.08;
    
    public static void main(String[] args){
        concentration = new int[size][size];
        pollutionSummary = new int[25];
        
        //assumptions here: start with all the pollution at a single source; wind constant
        //and strong in a single direction from the northwest; constant rate of deposition
        //(or rather, probability of a unit of pollution being deposited at each time step)
        
        for (int i = 0; i < size; i++){
            Arrays.fill(concentration[i], 0);
        }
        
        //start with all the pollution at a source at [10][10]
        concentration[10][10] = initTotalPollution;
        
        int[][] nextStepConcentration = new int[size][size];
                
        int time = 0;
        int airbornePollution = initTotalPollution;
        //for each time step
        while (airbornePollution > 0){
            //make a copy to store the next time step without interfering with the current one.
            for (int i = 0; i < size; i++){
                nextStepConcentration[i] = new int[size];
                System.arraycopy(concentration[i], 0, nextStepConcentration, 0, size);  //java 5
//               nextStepConcentration[i] = Arrays.copyOf(concentration[i], size);  //java 6
            }               
            for (int i = 0; i < size; i++){
                for (int j = 0; j < size; j++){
                    //for each particle at each location, it has a motionProbability chance of moving,
                    //a depositionProbability chance of being deposited, and if it moves, it is most likely
                    //but not guaranteed to move to the south or east.
                    for (int k = 0; k < concentration[i][j]; k++){
                        double r = Math.random();                        
                        if (r < depositionProbability){
                            //deposition is a simple decrement
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
                                if (i < size-1){
                                    nextStepConcentration[i+1][j]++;
                                } else {
                                    airbornePollution--;
                                } 
                            } else {
                                if (j < size-1){
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
            if (time % 5 == 0){
                writeData(time);
                System.out.println("total airborne pollution is " + airbornePollution);
                
                if (time/5 >= pollutionSummary.length){
                    expandSummaryArraySizes();
                }
                
                pollutionSummary[time/5] = airbornePollution;
            }
            
            time++;
        }  //end while (airbornePollution > 0)                
        
        writeSummary();
    }        
    
    private static void writeData(int time){
        try{
            String outFileName = "wind" + size + "time" + time + ".csv";
            FileWriter outFile = new FileWriter("C:/z4xNoSpaces/synthData/wind/run8/" + outFileName);
            
            outFile.write("x, y, pop, cases" + '\n');
            for (int i = 0; i < size; i++){
                for (int j = 0; j < size; j++){
                    //background population is initTotalPollution, which is the maximum possible value for concentration[i][j]                    
                    outFile.write(i + ", " + j + ", " + initTotalPollution + ", " + concentration[i][j] + '\n');
                }
            }
            
            outFile.close();
            System.out.println("wrote " + outFileName);
        } catch (IOException ioe){
            System.out.println("IOException when saving file: " + ioe.getMessage());
        }        
    }    
    
    private static void writeSummary(){ 
        try{
            FileWriter outFile = new FileWriter("C:/z4xNoSpaces/synthData/wind/run8/summary.csv");
            
            outFile.write("time, airborne" + '\n');
            for (int i = 0; i < pollutionSummary.length; i++){
                if (pollutionSummary[i] > 0){
                    outFile.write(i*5 + ", " + pollutionSummary[i] + '\n');
                }
            }
            
            outFile.close();
            System.out.println("wrote summary");
            
        } catch (IOException ioe){
            System.out.println("IOException when saving summary file: " + ioe.getMessage());
        } 
    }    
    
    private static void expandSummaryArraySizes(){
        int[] newPollArray = new int[pollutionSummary.length * 2];
        for (int i = 0; i < pollutionSummary.length; i++){
            newPollArray[i] = pollutionSummary[i];
        }
        
        pollutionSummary = newPollArray;
    }    
    
}
