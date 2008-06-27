/*
 * PAInfectTest.java
 *
 * Created on February 16, 2008, 4:27 PM
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
public class PAInfectTest {
    
    /** Creates a new instance of PAInfectTest */
    public PAInfectTest() {
    }

    static int[][] susceptible;
    static int[][] infectious;
    static int[][] recovered;
    static int[][] dead;
    static int[][][] recoverNow;
    static int[] infectedSummary;
    static int[] recoveredSummary;
    static int[] deadSummary;    
    
    public static void main (String[] args){
        String[][] values = new String[84][84];
        
        for (int run = 0; run < 50; run++){
            File folder = new File("E:/synthLandscan/infect/run" + run);
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
                dead = new int[values.length][values[0].length];
                //this assumes people recover or die after exactly 10 days.
                recoverNow = new int[10][values.length][values[0].length];
                int totalInfected = 0;     
                int totalRecovered = 0;
                int totalDead = 0;
                infectedSummary = new int[25];
                recoveredSummary = new int[25];
                deadSummary = new int[25];

                //may want to tweak the rateOfInfection.  With a recovery time of 10, 
                //the theoretical reproductive rate is 10*rateOfInfection.  Because of how
                //I handle edge cases and cells where everyone's already infected, the actual
                //reproductive rate will be slightly smaller.
                double rateOfInfection = 0.105;
                double mortalityRate = 0.02;

                for (int i = 0; i < values.length; i++){
                    Arrays.fill(infectious[i], 0);
                    Arrays.fill(recovered[i], 0);
                    Arrays.fill(dead[i], 0);            
                }

                //seed with patient zero
                infectious[57][68]++;  //in this grid, (57, 68) is near Philadelphia, PA
                recoverNow[0][57][68]++;
                susceptible[57][68]--;
                totalInfected++;

                //loop through time steps
                int time = 0;
                while(totalInfected > 0){
                    //if the time has been long enough, start recovering people.
                    //this has to be a separate loop to avoid interference in the recoverNow variable.
                    if (time > 9){
                        for (int i = 0; i < values.length; i++){
                            for (int j = 0; j < values[i].length; j++){
                                for (int k = 0; k < recoverNow[time % 10][i][j]; k++){
                                    infectious[i][j]--;
                                    totalInfected--;        
                                    double r = Math.random();
                                    if (r < mortalityRate){
                                        dead[i][j]++;
                                        totalDead++;
                                    } else {
                                        recovered[i][j]++;
                                        totalRecovered++;
                                    }
                                }  //end for(int k...
                                recoverNow[time % 10][i][j] = 0;                        
                            }   //end for(int j...
                        }
                    }

                    for (int i = 0; i < values.length; i++){
                        for (int j = 0; j < values[i].length; j++){                                       
                            //for each infectious person, infect others, dividing the new cases between the current cell and the neighboring cells.
                            for (int k = 0; k < infectious[i][j]; k++){
                                double r = Math.random();
                                if (r < rateOfInfection){
                                    //we have a winner!  another person gets infected.
                                    r = Math.random();
        //                            System.out.println("random " + r);
                                    if (r < 0.5){
                                        //half the time, infection is in same cell
                                        if (susceptible[i][j] > 0){
                                            //can only start a new case here if someone's susceptible
        //                                    System.out.println("infecting someone here");
                                            susceptible[i][j]--;
                                            infectious[i][j]++;
                                            totalInfected++;
                                            recoverNow[time % 10][i][j]++;
                                        }
                                    } else if (r < 0.625){
                                        if (i+1 < values.length){
                                            if (susceptible[i+1][j] > 0){
                                                //can only start a new case here if someone's susceptible
        //                                        System.out.println("infecting someone north");
                                                susceptible[i+1][j]--;
                                                infectious[i+1][j]++;
                                                totalInfected++;
                                                recoverNow[time % 10][i+1][j]++;
                                            }               
                                        }                 
                                    } else if (r < 0.75){
                                        if (i-1 >= 0){
                                            if (susceptible[i-1][j] > 0){
                                                //can only start a new case here if someone's susceptible
        //                                        System.out.println("infecting someone south");
                                                susceptible[i-1][j]--;
                                                infectious[i-1][j]++;
                                                totalInfected++;
                                                recoverNow[time % 10][i-1][j]++;
                                            }    
                                        }
                                    } else if (r < 0.875){
                                        if (j+1 < values[i].length){
                                            if (susceptible[i][j+1] > 0){
                                                //can only start a new case here if someone's susceptible
        //                                        System.out.println("infecting someone east");
                                                susceptible[i][j+1]--;
                                                infectious[i][j+1]++;
                                                totalInfected++;
                                                recoverNow[time % 10][i][j+1]++;
                                            }  
                                        }
                                    } else {
                                        if (j-1 >= 0){
                                            if (susceptible[i][j-1] > 0){
                                                //can only start a new case here if someone's susceptible
        //                                        System.out.println("infecting someone west");
                                                susceptible[i][j-1]--;
                                                infectious[i][j-1]++;
                                                totalInfected++;
                                                recoverNow[time % 10][i][j-1]++;
                                            }    
                                        }
                                    }
                                }  //end if (r < rateOfInfection)
                            }  //end for (int k = 0; k < infectious[i][j]                                        
                        }  //end for (int j...
                    }   //end for (int i...

                    if (time % 5 == 0){
                        writeData(time, run);
                        System.out.println("there are " + totalInfected + " infected.");
                        System.out.println(totalRecovered + " have recovered.");
                        System.out.println(totalDead + " have died.");

                        if (time/5 >= infectedSummary.length){
                            expandSummaryArraySizes();
                        }

                        infectedSummary[time/5] = totalInfected;
                        recoveredSummary[time/5] = totalRecovered;
                        deadSummary[time/5] = totalDead;
                    }

                    time++;
                }  //end while(totalInfected > 0)

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
            String outFileName = "infPAtime" + time + ".csv";
            FileWriter outFile = new FileWriter("E:/synthLandscan/infect/run" + run + "/" + outFileName);
            
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
            FileWriter outFile = new FileWriter("E:/synthLandscan/infect/run" + run + "/summary.csv");
            
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
        
        for (int i = 0; i < infectedSummary.length; i++){
            newInfArray[i] = infectedSummary[i];
            newRecArray[i] = recoveredSummary[i];
            newDeadArray[i] = deadSummary[i];
        }
        
        infectedSummary = newInfArray;
        recoveredSummary = newRecArray;
        deadSummary = newDeadArray;
    }        
    
    
}
