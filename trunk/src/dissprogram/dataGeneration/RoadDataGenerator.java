/*
 * RoadDataGenerator.java
 *
 * Created on June 19, 2007, 3:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.dataGeneration;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 *
 * @author z4x
 */
public class RoadDataGenerator {
    
    /**
     * Creates a new instance of RoadDataGenerator
     */
    public RoadDataGenerator() {
    }
    
    static int size = 75;
    static int[][] susceptible;
    static int[][] infectious;
    static int[][] recovered;
    static int[][] dead;
    static int[][][] recoverNow;
    static int[] infectedSummary;
    static int[] recoveredSummary;
    static int[] deadSummary;          
    static int[] xRoads = new int[]{37};
    static int[] yRoads = new int[]{37};
    static int[] xHighways = new int[]{10, 65};
    static int[] yHighways = new int[]{10, 65};
    
    public static void main(String[] args){
        //this is very similar to the infectious data generator, with the change that infections travel
        //faster along roads (e.g., mosquitoes in cars).  There are two highways at x=10 and x=65.
        //There are two orthogonal highways at y=10 and y=65.  Finally, there are smaller roads at x=37 and
        //y=37.
        
        susceptible = new int[size][size];
        infectious = new int[size][size];
        recovered = new int[size][size];
        dead = new int[size][size];
        //this assumes people recover or die after exactly 10 time steps.
        recoverNow = new int[10][size][size];
        int totalInfected = 0;     
        int totalRecovered = 0;
        int totalDead = 0;
        infectedSummary = new int[25];
        recoveredSummary = new int[25];
        deadSummary = new int[25];
        
        //may want to tweak the rateOfInfection.  With a recovery time of 10, 
        //the theoretical reproductive rate is 10*rateOfInfection.  Because of how
        //I handle edge cases and cells where everyone's already infected, the actual
        //reproductive rate will be slightly smaller.  With the way I'm dealing with 
        //transportation routes, it will be considerably smaller (a little above half).
        double rateOfInfection = 0.20;
        double mortalityRate = 0.02;
        
        for (int i = 0; i < size; i++){
            Arrays.fill(susceptible[i], 1000);
            Arrays.fill(infectious[i], 0);
            Arrays.fill(recovered[i], 0);
            Arrays.fill(dead[i], 0);            
        }
        
        //seed with patient zero
        infectious[37][37]++;
        recoverNow[0][37][37]++;
        susceptible[37][37]--;
        totalInfected++;
        
        //loop through time steps
        int time = 0;
        while(totalInfected > 0){
            //if the time has been long enough, start recovering people.
            //this has to be a separate loop to avoid interference in the recoverNow variable.
            if (time > 9){
                for (int i = 0; i < size; i++){
                    for (int j = 0; j < size; j++){
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
            
            for (int i = 0; i < size; i++){
                for (int j = 0; j < size; j++){                                       
                    //for each infectious person, infect others, dividing the new cases between the current cell and the neighboring cells.
                    for (int k = 0; k < infectious[i][j]; k++){
                        double r = Math.random();
                        if (r < rateOfInfection){
                            //we may have a winner!    
                            //on the roads and highways, these are preferentially located along the transport routes.
                            boolean xRoad = onRoadX(i);
                            boolean yRoad = onRoadY(j);
                            boolean xHighway = onHighwayX(i);
                            boolean yHighway = onHighwayY(j);
                            double incXProb, decXProb, incYProb, decYProb;
                            double hereProb = 0.3;
                            if (xRoad){
                                if (yRoad){
                                    //at intersection of two roads, all directions equally likely, prob. of movement is 0.4
                                    incXProb = 0.10;
                                    decXProb = 0.10;
                                    incYProb = 0.10;
                                    decYProb = 0.10;
                                } else if (yHighway) {
                                    //intersection of highway and road, highway more likely, prob. of movement is 0.5
                                    //travel along Y highway changes X value and vice versa, so X is more likely to be changed along a Y highway
                                    incXProb = 0.15;
                                    decXProb = 0.15;
                                    incYProb = 0.10;
                                    decYProb = 0.10;                                        
                                } else {
                                    //just along a X road, prob. of movement is 0.3
                                    incXProb = 0.05;
                                    decXProb = 0.05;
                                    incYProb = 0.10;
                                    decYProb = 0.10;
                                }  
                            } else if (xHighway){
                                if (yRoad){
                                    //intersection of highway and road, highway more likely, prob. of movement is 0.5
                                    //travel along Y highway changes X value and vice versa, so X is more likely to be changed along a Y highway
                                    incXProb = 0.10;
                                    decXProb = 0.10;
                                    incYProb = 0.15;
                                    decYProb = 0.15;
                                } else if (yHighway) {
                                    //at intersection of two highways, all directions equally likely, prob. of movement is 0.6
                                    incXProb = 0.15;
                                    decXProb = 0.15;
                                    incYProb = 0.15;
                                    decYProb = 0.15;                                      
                                } else {
                                    //just along a X highway, prob. of movement is 0.4
                                    incXProb = 0.05;
                                    decXProb = 0.05;
                                    incYProb = 0.15;
                                    decYProb = 0.15;
                                }                                      
                            } else {
                                if (yRoad){
                                    //just a y Road, prob. of movement is 0.3
                                    incXProb = 0.10;
                                    decXProb = 0.10;
                                    incYProb = 0.05;
                                    decYProb = 0.05;                                        
                                } else if (yHighway){
                                    //just along a Y highway, prob. of movement is 0.4
                                    incXProb = 0.15;
                                    decXProb = 0.15;
                                    incYProb = 0.05;
                                    decYProb = 0.05;
                                } else {
                                    //no transportation route, all directions equally likely, prob. of movement is 0.2
                                    incXProb = 0.05;
                                    decXProb = 0.05;
                                    incYProb = 0.05;
                                    decYProb = 0.05;                                         
                                }
                            }
                            //now that I know what the probability of moving in each direction is...
                            r = Math.random();
                            if (r < incXProb){
                                //increment x
                                if (i+1 < size){
                                    if (susceptible[i+1][j] > 0){
                                        //can only start a new case here if someone's susceptible
                                        susceptible[i+1][j]--;
                                        infectious[i+1][j]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i+1][j]++;
                                    }               
                                }                                    
                            } else if (r < incXProb + decXProb){
                                //decrement x
                                if (i-1 >= 0){
                                    if (susceptible[i-1][j] > 0){
                                        //can only start a new case here if someone's susceptible
                                        susceptible[i-1][j]--;
                                        infectious[i-1][j]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i-1][j]++;
                                    }    
                                }
                            } else if (r < incXProb + decXProb + incYProb){
                                //increment y
                                if (j+1 < size){
                                    if (susceptible[i][j+1] > 0){
                                        //can only start a new case here if someone's susceptible
                                        susceptible[i][j+1]--;
                                        infectious[i][j+1]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i][j+1]++;
                                    }  
                                }
                            } else if (r < incXProb + decXProb + incYProb + decYProb){
                                //decrement y
                                if (j-1 >= 0){
                                    if (susceptible[i][j-1] > 0){
                                        //can only start a new case here if someone's susceptible
                                        susceptible[i][j-1]--;
                                        infectious[i][j-1]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i][j-1]++;
                                    }    
                                }
                            } else if (r < incXProb + decXProb + incYProb + decYProb + hereProb){
                                //new case is here.
                                if (susceptible[i][j] > 0){
                                    //can only start a new case here if someone's susceptible
                                    susceptible[i][j]--;
                                    infectious[i][j]++;
                                    totalInfected++;
                                    recoverNow[time % 10][i][j]++;
                                }                                    
                            } else {
                                //no new case after all.  This is done so that the reproductive rate
                                //is higher along the transportation routes.  Without a higher RR there,
                                //the shape doesn't change much.  (Cases build up in isolated areas, and 
                                //this allows transmission about as fast as the roads with fewer cases.)
                            }   //end if (r < incXProb)...else...else...else...else...else         
                        }  //end if (r < rateOfInfection)
                    }  //end for (int k...
                }  //end for (int j...
            }   //end for (int i...
            
            if (time % 5 == 0){
                writeData(time);
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
        
        writeSummaries();
    }
    
    private static boolean onRoadX(int x){
        boolean ret = false;
        for (int i = 0; i < xRoads.length; i++){
            ret = ret || (xRoads[i] == x);
        }
        return ret;
    }
    
    private static boolean onRoadY(int y){
        boolean ret = false;
        for (int i = 0; i < yRoads.length; i++){
            ret = ret || (yRoads[i] == y);
        }
        return ret;
    }    
    
    private static boolean onHighwayX(int x){
        boolean ret = false;
        for (int i = 0; i < xHighways.length; i++){
            ret = ret || (xHighways[i] == x);
        }
        return ret;
    }    
    
    private static boolean onHighwayY(int y){
        boolean ret = false;
        for (int i = 0; i < yHighways.length; i++){
            ret = ret || (yHighways[i] == y);
        }
        return ret;
    }        
    
    private static void writeData(int time){
        try{
            String outFileName = "road" + size + "time" + time + ".csv";
            FileWriter outFile = new FileWriter("C:/z4xNoSpaces/synthData/road/run9/" + outFileName);
            
            outFile.write("x, y, pop, cases" + '\n');
            for (int i = 0; i < size; i++){
                for (int j = 0; j < size; j++){
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
    
    private static void writeSummaries(){ 
        try{
            FileWriter outFile = new FileWriter("C:/z4xNoSpaces/synthData/road/run9/summary.csv");
            
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
