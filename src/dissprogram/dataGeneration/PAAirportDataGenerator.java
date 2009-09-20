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
public class PAAirportDataGenerator {
    
    /** Creates a new instance of PARoadDataGenerator */
    public PAAirportDataGenerator() {
    }

    static int[][] susceptible;
    static int[][] infectious;
    static int[][] recovered;
    static int[][] dead;
    static int[][][] recoverNow;
    static int[] infectedSummary;
    static int[] recoveredSummary;
    static int[] deadSummary; 
    static int[][] roadHere;
    static int[][] airportHere;
    static int[][] airConnections;
    static int[][] airportList;
    
    public static void main (String[] args){
        String[][] values = new String[84][84];
        String[][] roadValues = new String[84][84];
        String[][] airValues = new String[84][84];
        roadHere = new int[84][84];
        airportHere = new int[84][84];
        airportList = new int[30][3];
        
        try{
            File f = new File("D:/landscan/namerica06/export/highwaygrid.csv");        
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            roadValues = csvp.getAllValues();                       
        } catch (IOException ioe){
            System.out.println("IOException: " + ioe.getMessage());
        }        
        for (int i = 0; i < roadValues.length; i++){
            for (int j = 0; j < roadValues[i].length; j++){
                if (Integer.parseInt(roadValues[i][j]) > 0){  //in the susceptible double[][] creation, i and j get flipped so north remains at the top.  Make sure the roads do the same!
                    roadHere[j][i] = 1;
                } else {
                    roadHere[j][i] = 0;
                }
            }
        }
        
        try{
            File f = new File("D:/landscan/namerica06/export/airportgrid.csv");        
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            airValues = csvp.getAllValues();                       
        } catch (IOException ioe){
            System.out.println("IOException: " + ioe.getMessage());
        }        
        int q = 0;
        for (int i = 0; i < airValues.length; i++){
            for (int j = 0; j < airValues[i].length; j++){
                //in the susceptible double[][] creation, i and j get flipped so north remains at the top.  Make sure the airports do the same!
                if (Integer.parseInt(airValues[i][j]) > 0){
                    airportHere[j][i] = Integer.parseInt(airValues[i][j]);
                    airportList[q] = new int[]{airportHere[j][i], j, i};
                    q++;
                } else {
                    airportHere[j][i] = 0;
                }
            }
        }        
        
        String[][] connectionValues = null;
        try{
            File f = new File("D:/landscan/namerica06/export/airportConnections.csv");        
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            connectionValues = csvp.getAllValues();                       
        } catch (IOException ioe){
            System.out.println("IOException: " + ioe.getMessage());
        }    
        airConnections = new int[connectionValues.length][connectionValues[0].length];
        for (int i = 0; i < connectionValues.length; i++){
            for (int j = 0; j < connectionValues[i].length; j++){
                airConnections[i][j] = Integer.parseInt(connectionValues[i][j]);
            }
        }          
        
        for (int run = 0; run < 50; run++){
            File folder = new File("D:/synthLandscan/airport/run" + run);
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
                //reproductive rate will be slightly smaller.  With the way I'm dealing with 
                //transportation routes, it will be considerably smaller (a little above half).
                double rateOfInfection = 0.20;
                double mortalityRate = 0.02;

                for (int i = 0; i < values.length; i++){
                    Arrays.fill(infectious[i], 0);
                    Arrays.fill(recovered[i], 0);
                    Arrays.fill(dead[i], 0);            
                }

                //seed with patient zero
                infectious[57][68]++;
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
                                    //we may have a winner!    
                                    //on the highways, these are preferentially located along the transport routes.
                                    double incXProb, decXProb, incYProb, decYProb;
                                    double hereProb = 0.3;
                                    double airProb = 0.1;
                                    
                                    if (i+1 < roadHere.length){
                                        if ((roadHere[i][j] == 1) && (roadHere[i+1][j] == 1)){
                                            incXProb = 0.15;
                                        } else {
                                            incXProb = 0.05;
                                        }
                                    } else {
                                        incXProb = 0;
                                    }

                                    if (j+1 < roadHere[i].length){
                                        if ((roadHere[i][j] == 1) && (roadHere[i][j+1] == 1)){
                                            incYProb = 0.15;
                                        } else {
                                            incYProb = 0.05;
                                        }    
                                    } else {
                                        incYProb = 0;
                                    }
                                    
                                    if (i > 0){
                                        if ((roadHere[i][j] == 1) && (roadHere[i-1][j] == 1)){
                                            decXProb = 0.15;
                                        } else {
                                            decXProb = 0.05;
                                        }     
                                    } else {
                                        decXProb = 0;
                                    }
                                    
                                    if (j > 0){ 
                                        if ((roadHere[i][j] == 1) && (roadHere[i][j-1] == 1)){
                                            decYProb = 0.15;
                                        } else {
                                            decYProb = 0.05;
                                        } 
                                    } else {
                                        decYProb = 0;
                                    }
                                    
                                    //now that I know what the probability of moving in each direction is...
                                    r = Math.random();
                                    if (r < incXProb){
                                        //increment x
                                        if (i+1 < values.length){
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
                                        if (j+1 < values[i].length){
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
                                    } else if (r < incXProb + decXProb + incYProb + decYProb + hereProb + airProb){
                                        //new case is via an air connection.
                                        //find a connected airport.
                                        if (airportHere[i][j] > 0){
                                            int[] connectedAirports = new int[30];
                                            int v = 0;
                                            for (int w = 0; w < airConnections.length; w++){
                                                if (airConnections[w][0] == airportHere[i][j]){
                                                    connectedAirports[v] = airConnections[w][1];
                                                    v++;
                                                }
                                            }
                                            int r2 = (int) Math.floor(Math.random() * v);
                                            int recipientAirport = connectedAirports[r2];
                                            int recipientX = 0;
                                            int recipientY = 0;
                                            for (int f = 0; f < airportList.length; f++){
                                                if (airportList[f][0] == recipientAirport){
                                                    recipientX = airportList[f][1];
                                                    recipientY = airportList[f][2];
                                                }
                                            }
                                            if (susceptible[recipientX][recipientY] > 0){
                                                //can only start a new case here if someone's susceptible
                                                susceptible[recipientX][recipientY]--;
                                                infectious[recipientX][recipientY]++;
                                                totalInfected++;
                                                recoverNow[time % 10][recipientX][recipientY]++;
                                            }                                                                                         
                                        }
                                    }else {
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
                        writeData(time, run);
//                        System.out.println("there are " + totalInfected + " infected.");
//                        System.out.println(totalRecovered + " have recovered.");
//                        System.out.println(totalDead + " have died.");

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
            String outFileName = "airportPAtime" + time + ".csv";
            FileWriter outFile = new FileWriter("D:/synthLandscan/airport/run" + run + "/" + outFileName);
            
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
            FileWriter outFile = new FileWriter("D:/synthLandscan/airport/run" + run + "/summary.csv");
            
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
