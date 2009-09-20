/*
 * To change this template, choose Tools | Templates
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
public class PARiverDataGenerator {
    
    /** Creates a new instance of PARoadDataGenerator */
    public PARiverDataGenerator() {
    }

    static int[][] concentration;
    static int[][] susceptible;
    static int[][] infectious;
    static int[][] recovered;
    static int[][] dead;
    static int[][][] recoverNow;
    static int[] infectedSummary;
    static int[] recoveredSummary;
    static int[] deadSummary; 
    static int[] pollutionSummary;
    static double[][] riverHere;
    static int initTotalPollution = 2500;
    static double infectionProbability = 0.001;
    static double depositionProbability = 0.002;
    static double motionProbability = 0.20; 
    
    public static void main (String[] args){
        String[][] values = new String[84][84];
        String[][] riverValues = new String[84][84];
        riverHere = new double[84][84];
        
        try{
            File f = new File("D:/landscan/namerica06/export/rivergrid.csv");        
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            riverValues = csvp.getAllValues();                       
        } catch (IOException ioe){
            System.out.println("IOException: " + ioe.getMessage());
        }        
        for (int i = 0; i < riverValues.length; i++){
            for (int j = 0; j < riverValues[i].length; j++){
                riverHere[j][i] = Double.parseDouble(riverValues[i][j]);  //in the susceptible double[][] creation, i and j get flipped so north remains at the top.  Make sure the rivers do the same!
            }
        }
        
        for (int run = 0; run < 50; run++){
            File folder = new File("D:/synthLandscan/river/run" + run);
            folder.mkdir();        
            
            boolean done = false;
            while (!(done)){
                try{
                    File f = new File("D:/landscan/namerica06/export/nameraggcsv.csv");        
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
                dead = new int[values.length][values[0].length];
                //this assumes people recover or die after exactly 10 days.
                recoverNow = new int[10][values.length][values[0].length];
                int totalInfected = 0;     
                int totalRecovered = 0;
                infectedSummary = new int[25];
                recoveredSummary = new int[25];
                deadSummary = new int[25];
                pollutionSummary = new int[25];                

                for (int i = 0; i < values.length; i++){
                    Arrays.fill(infectious[i], 0);
                    Arrays.fill(recovered[i], 0);
                    Arrays.fill(dead[i], 0); 
                    Arrays.fill(concentration[i], 0);
                }

                //start with all the pollution at a source at [35][35]
                concentration[35][35] = initTotalPollution;

                int[][] nextStepConcentration = new int[concentration.length][concentration[0].length];
                
                //loop through time steps
                int time = 0;
                int activePollution = initTotalPollution;
                //for each time step
                while (activePollution > 0){
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
                            //a depositionProbability chance of being deposited, and if it moves, it will move downstream.
                            for (int k = 0; k < concentration[i][j]; k++){
                                double r = Math.random();                        
                                if (r < depositionProbability){
                                    //deposition is a simple decrement
//                                    System.out.println("deposition at time step " + time);
                                    nextStepConcentration[i][j]--;
                                    activePollution--;
                                } else if (r < depositionProbability + motionProbability){
                                    //motion
                                    //find the downstream neighbor(s).  If none exist, it becomes inactive--same as deposition
                                    double mileageHere = riverHere[i][j];
                                    double mileageSouth;
                                    if (i > 0){
                                        mileageSouth = riverHere[i-1][j];
                                    } else {
                                        mileageSouth = -2;
                                    }
                                    double mileageNorth = riverHere[i+1][j];
                                    double mileageEast = riverHere[i][j+1];                                    
                                    double mileageWest;
                                    if (j > 0){
                                        mileageWest = riverHere[i][j-1];
                                    } else {
                                        mileageWest = -2;
                                    }
                                    double r2 = Math.random();
                                    
                                    //the mileageSouth >= 0 is included b/c riverHere[i][j] = -1 if no river there
                                    boolean southDown = (mileageSouth <= mileageHere) && (mileageSouth >= 0);
                                    boolean northDown = (mileageNorth <= mileageHere) && (mileageNorth >= 0);
                                    boolean eastDown = (mileageEast <= mileageHere) && (mileageEast >= 0);
                                    boolean westDown = (mileageWest <= mileageHere) && (mileageWest >= 0);
                                                                       
                                    nextStepConcentration[i][j]--;
                                    //one direction is down
                                    if (southDown && !northDown && !eastDown && !westDown){
                                        nextStepConcentration[i-1][j]++;
                                    } else if (!southDown && northDown && !eastDown && !westDown){
                                        nextStepConcentration[i+1][j]++;                                     
                                    } else if (!southDown && !northDown && eastDown && !westDown){
                                        nextStepConcentration[i][j+1]++;
                                    } else if (!southDown && !northDown && !eastDown && westDown){
                                        nextStepConcentration[i][j-1]++;
                                        
                                    //two directions are down    
                                    } else if (southDown && northDown && !eastDown && !westDown){
                                        if (r2 < 0.5){
                                            nextStepConcentration[i-1][j]++;
                                        } else {
                                            nextStepConcentration[i+1][j]++;
                                        }
                                    } else if (southDown && !northDown && eastDown && !westDown){
                                        if (r2 < 0.5){
                                            nextStepConcentration[i-1][j]++;
                                        } else {
                                            nextStepConcentration[i][j+1]++;
                                        }                                        
                                    } else if (southDown && !northDown && !eastDown && westDown){
                                        if (r2 < 0.5){
                                            nextStepConcentration[i-1][j]++;
                                        } else {
                                            nextStepConcentration[i][j-1]++;
                                        }                                        
                                    } else if (!southDown && northDown && eastDown && !westDown){
                                        if (r2 < 0.5){
                                            nextStepConcentration[i+1][j]++;
                                        } else {
                                            nextStepConcentration[i][j+1]++;
                                        }                                        
                                    } else if (!southDown && northDown && !eastDown && westDown){
                                        if (r2 < 0.5){
                                            nextStepConcentration[i+1][j]++;
                                        } else {
                                            nextStepConcentration[i][j-1]++;
                                        }                                        
                                    } else if (!southDown && !northDown && eastDown && westDown){
                                        if (r2 < 0.5){
                                            nextStepConcentration[i][j+1]++;
                                        } else {
                                            nextStepConcentration[i][j-1]++;
                                        }    
                                        
                                    //three directions are down    
                                    }else if (southDown && northDown && eastDown && !westDown){
                                        if (r2 < 0.33){
                                            nextStepConcentration[i-1][j]++;
                                        } else if (r2 < 0.66){
                                            nextStepConcentration[i+1][j]++;
                                        } else {
                                            nextStepConcentration[i][j+1]++;
                                        }                                          
                                    }else if (southDown && northDown && !eastDown && westDown){
                                        if (r2 < 0.33){
                                            nextStepConcentration[i-1][j]++;
                                        } else if (r2 < 0.66){
                                            nextStepConcentration[i+1][j]++;
                                        } else {
                                            nextStepConcentration[i][j-1]++;
                                        }                                           
                                    }else if (southDown && !northDown && eastDown && westDown){
                                        if (r2 < 0.33){
                                            nextStepConcentration[i-1][j]++;
                                        } else if (r2 < 0.66){
                                            nextStepConcentration[i][j+1]++;
                                        } else {
                                            nextStepConcentration[i][j-1]++;
                                        }                                           
                                    }else if (!southDown && northDown && eastDown && westDown){
                                        if (r2 < 0.33){
                                            nextStepConcentration[i][j-1]++;
                                        } else if (r2 < 0.66){
                                            nextStepConcentration[i+1][j]++;
                                        } else {
                                            nextStepConcentration[i][j+1]++;
                                        }   
                                    //all directions are down    
                                    } else if (!southDown && !northDown && eastDown && westDown){
                                        if (r2 < 0.25){
                                            nextStepConcentration[i][j-1]++;
                                        } else if (r2 < 0.5){
                                            nextStepConcentration[i+1][j]++;
                                        } else if (r2 < 0.75){
                                            nextStepConcentration[i][j+1]++;
                                        } else {
                                            nextStepConcentration[i-1][j]++;
                                        }                                          
                                    } else {
                                        //all false.  I think this means it fell off the edge of the map or into the ocean
                                        //so just make it inactive
                                        activePollution--;
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
//                        System.out.println("total active pollution is " + activePollution);
//                        System.out.println("there are " + totalInfected + " infected.");
//                        System.out.println(totalRecovered + " have recovered.");

                        if (time/5 >= pollutionSummary.length){
                            expandSummaryArraySizes();
                        }

                        pollutionSummary[time/5] = activePollution;
                        infectedSummary[time/5] = totalInfected;
                        recoveredSummary[time/5] = totalRecovered;                       
                    }

                    time++;
                }  //end while (activePollution > 0)                      

                writeSummaries(run);
                System.out.println("time is " + time);
                if (time > 250){
                    done = true;
                    System.out.println("finished one");
                }
            }
        }
    }
    
    private static void writeData(int time, int run){
        try{
            String outFileName = "riverPAtime" + time + ".csv";
            FileWriter outFile = new FileWriter("D:/synthLandscan/river/run" + run + "/" + outFileName);
            
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
            FileWriter outFile = new FileWriter("D:/synthLandscan/river/run" + run + "/summary.csv");
            
            outFile.write("time, infected, recovered, dead" + '\n');
            for (int i = 0; i < infectedSummary.length; i++){
                if ((infectedSummary[i] > 0) || (recoveredSummary[i] > 0)){
                    outFile.write(i*5 + ", " + infectedSummary[i] + ", " + recoveredSummary[i] + ", " + deadSummary[i] + '\n');
                }
            }
            
            outFile.close();
            System.out.println("wrote summary");
            
        } catch (IOException ioe){
            System.out.println("IOException when saving summary file: " + ioe.getMessage());
        } 
    }
    
    private static void expandSummaryArraySizes(){
        int[] newInfArray = new int[infectedSummary.length * 2];
        int[] newRecArray = new int[recoveredSummary.length * 2];
        int[] newDeadArray = new int[deadSummary.length * 2];
        int[] newPollArray = new int[pollutionSummary.length * 2];
        
        for (int i = 0; i < infectedSummary.length; i++){
            newPollArray[i] = pollutionSummary[i];            
            newInfArray[i] = infectedSummary[i];
            newRecArray[i] = recoveredSummary[i];
            newDeadArray[i] = deadSummary[i];
        }
        
        infectedSummary = newInfArray;
        recoveredSummary = newRecArray;
        deadSummary = newDeadArray;
        pollutionSummary = newPollArray;
    }        
    
    
}
