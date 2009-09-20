/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package parallelUtilMains;

import dissprogram.DissUtils;
import dissprogram.image.ImageProcessor;
import dissprogram.image.MomentGenerator;
import edu.psu.geovista.gam.FitnessRelativePct;
import edu.psu.geovista.gam.InitGAMFile;
import edu.psu.geovista.gam.SystematicGam;
import edu.psu.geovista.io.csv.CSVParser;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.coverage.processing.Operations;
import org.opengis.coverage.processing.Operation;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author jconley
 */
public class TheBigOne {

    public TheBigOne(){
    }
    
    public void main(String[] args) {
        for (int batchNumber = 69; batchNumber < 125; batchNumber++){  //was up to 125, stopped at 34
            try{
                singleRunThrough runMod0 = new singleRunThrough(batchNumber * 8);
                singleRunThrough runMod1 = new singleRunThrough(batchNumber * 8 + 1);
                singleRunThrough runMod2 = new singleRunThrough(batchNumber * 8 + 2);
                singleRunThrough runMod3 = new singleRunThrough(batchNumber * 8 + 3);
                singleRunThrough runMod4 = new singleRunThrough(batchNumber * 8 + 4);
                singleRunThrough runMod5 = new singleRunThrough(batchNumber * 8 + 5);
                singleRunThrough runMod6 = new singleRunThrough(batchNumber * 8 + 6);
                singleRunThrough runMod7 = new singleRunThrough(batchNumber * 8 + 7);

                Thread thread0 = new Thread(runMod0);
                Thread thread1 = new Thread(runMod1);
                Thread thread2 = new Thread(runMod2);
                Thread thread3 = new Thread(runMod3);
                Thread thread4 = new Thread(runMod4);
                Thread thread5 = new Thread(runMod5);
                Thread thread6 = new Thread(runMod6);
                Thread thread7 = new Thread(runMod7);

                thread0.start();
                thread1.start();
                thread2.start();
                thread3.start();
                thread4.start();
                thread5.start();
                thread6.start();
                thread7.start();

                thread0.join();
                thread1.join();
                thread2.join();
                thread3.join();
                thread4.join();
                thread5.join();
                thread6.join();
                thread7.join();
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
    
    private class singleRunThrough implements Runnable{
        
        int run;
        
        public singleRunThrough(int i){
            run = i;
        }
        
        public void run(){
            infDataGenerator myInf = new infDataGenerator(run);
            roadDataGenerator myRoad = new roadDataGenerator(run);
            riverDataGenerator myRiver = new riverDataGenerator(run);
            windDataGenerator myWind = new windDataGenerator(run);
            airportDataGenerator myAirport = new airportDataGenerator(run);
            rasterAndMomentGenerator infRasters = new rasterAndMomentGenerator("inf", "GAM", run);
            rasterAndMomentGenerator roadRasters = new rasterAndMomentGenerator("road", "GAM", run);
            rasterAndMomentGenerator riverRasters = new rasterAndMomentGenerator("river", "GAM", run);
            rasterAndMomentGenerator windRasters = new rasterAndMomentGenerator("wind", "GAM", run);
            rasterAndMomentGenerator airportRasters = new rasterAndMomentGenerator("airport", "GAM", run);
            
            myInf.run();
            myRoad.run();
            myRiver.run();
            myWind.run();
            myAirport.run();
            
            infRasters.run();
            roadRasters.run();
            riverRasters.run();
            windRasters.run();
            airportRasters.run();
        }
    }
    
    private class infDataGenerator implements Runnable{        
        
        int[][] susceptible;
        int[][] infectious;
        int[][] recovered;
        int[][] dead;
        int[][][] recoverNow;
        int[] infectedSummary;
        int[] recoveredSummary;
        int[] deadSummary;    
        int run;
        Random r = new Random();
        
        public infDataGenerator(int i){
            run = i;
        }
        
        public void run(){
            String[][] values = new String[84][84];

            File folder = new File("D:/synthLandscan/randomStarts/inf/run" + run);
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

//                System.out.println("values is " + values.length + "x" + values[1].length);

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

                //seed with patient zero at a random location
                int randomX = 0;
                int randomY = 0;
                boolean inhabited = false;
                while (!(inhabited)){
                    randomX = r.nextInt(84);
                    randomY = r.nextInt(84);
                    if (susceptible[randomX][randomY] > 0){
                        infectious[randomX][randomY]++;  
                        recoverNow[0][randomX][randomY]++;
                        susceptible[randomX][randomY]--;
                        totalInfected++;
                        inhabited = true;
//                        System.out.println("starting at " + randomX + ", " + randomY);
                    } 
                }

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

                writeSummaries(run, randomX, randomY);
//                System.out.println("time is " + time);
                if (time > 250){
                    done = true;
                    System.out.println("finished inf #" + run);
                } else {
//                    System.out.println("restart");
                }
            }
            
        }

        private void writeData(int time, int run){
            try{
                String outFileName = "infPAtime" + time + ".csv";
                FileWriter outFile = new FileWriter("D:/synthLandscan/randomStarts/inf/run" + run + "/" + outFileName);

                outFile.write("x, y, pop, cases" + '\n');
                for (int i = 0; i < susceptible.length; i++){
                    for (int j = 0; j < susceptible[i].length; j++){
                        int background = susceptible[i][j] + infectious[i][j] + recovered[i][j];
                        outFile.write(i + ", " + j + ", " + background + ", " + infectious[i][j] + '\n');
                    }
                }

                outFile.close();
//                System.out.println("wrote " + outFileName);
            } catch (IOException ioe){
                System.out.println("IOException when saving file: " + ioe.getMessage());
            }        
        }

        private void writeSummaries(int run, int startX, int startY){ 
            try{
                FileWriter outFile = new FileWriter("D:/synthLandscan/randomStarts/inf/run" + run + "/summary.csv");
                outFile.write("starting location (" + startX + ". " + startY + ")" + '\n');
                outFile.write("time, infected, recovered, dead" + '\n');
                for (int i = 0; i < infectedSummary.length; i++){
                    if ((infectedSummary[i] > 0) || (recoveredSummary[i] > 0)){
                        outFile.write(i*5 + ", " + infectedSummary[i] + ", " + recoveredSummary[i] + ", " + deadSummary[i] + '\n');
                    }
                }

                outFile.close();
//                System.out.println("wrote summary");

            } catch (IOException ioe){
                System.out.println("IOException when saving summary file: " + ioe.getMessage());
            } 
        }

        private void expandSummaryArraySizes(){
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
    
    private class roadDataGenerator implements Runnable{

        int[][] susceptible;
        int[][] infectious;
        int[][] recovered;
        int[][] dead;
        int[][][] recoverNow;
        int[] infectedSummary;
        int[] recoveredSummary;
        int[] deadSummary; 
        int[][] roadHere;        
        int run;
        Random r = new Random();
        
        public roadDataGenerator(int i){
            run = i;
        }
        
        public void run(){
            String[][] values = new String[84][84];
            String[][] roadValues = new String[84][84];
            roadHere = new int[84][84];

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

            File folder = new File("D:/synthLandscan/randomStarts/road/run" + run);
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

//                System.out.println("values is " + values.length + "x" + values[1].length);

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
                int randomX = 0;
                int randomY = 0;
                boolean inhabited = false;
                while (!(inhabited)){
                    randomX = r.nextInt(84);
                    randomY = r.nextInt(84);
                    if (susceptible[randomX][randomY] > 0){
                        infectious[randomX][randomY]++;  
                        recoverNow[0][randomX][randomY]++;
                        susceptible[randomX][randomY]--;
                        totalInfected++;
                        inhabited = true;
                    }
                }

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

                writeSummaries(run, randomX, randomY);
//                System.out.println("time is " + time);
                if (time > 250){
                    done = true;
                    System.out.println("finished road #" + run);
                }
            }
            
        }

        private void writeData(int time, int run){
            try{
                String outFileName = "roadPAtime" + time + ".csv";
                FileWriter outFile = new FileWriter("D:/synthLandscan/randomStarts/road/run" + run + "/" + outFileName);

                outFile.write("x, y, pop, cases" + '\n');
                for (int i = 0; i < susceptible.length; i++){
                    for (int j = 0; j < susceptible[i].length; j++){
                        int background = susceptible[i][j] + infectious[i][j] + recovered[i][j];
                        outFile.write(i + ", " + j + ", " + background + ", " + infectious[i][j] + '\n');
                    }
                }

                outFile.close();
//                System.out.println("wrote " + outFileName);
            } catch (IOException ioe){
                System.out.println("IOException when saving file: " + ioe.getMessage());
            }        
        }

        private void writeSummaries(int run, int startX, int startY){ 
            try{
                FileWriter outFile = new FileWriter("D:/synthLandscan/randomStarts/road/run" + run + "/summary.csv");
                outFile.write("start at (" + startX + ". " + startY + ")" + '\n');
                outFile.write("time, infected, recovered, dead" + '\n');
                for (int i = 0; i < infectedSummary.length; i++){
                    if ((infectedSummary[i] > 0) || (recoveredSummary[i] > 0)){
                        outFile.write(i*5 + ", " + infectedSummary[i] + ", " + recoveredSummary[i] + ", " + deadSummary[i] + '\n');
                    }
                }

                outFile.close();
//                System.out.println("wrote summary");

            } catch (IOException ioe){
                System.out.println("IOException when saving summary file: " + ioe.getMessage());
            } 
        }

        private void expandSummaryArraySizes(){
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
    
    private class riverDataGenerator implements Runnable{
        
        int[][] concentration;
        int[][] susceptible;
        int[][] infectious;
        int[][] recovered;
        int[][] dead;
        int[][][] recoverNow;
        int[] infectedSummary;
        int[] recoveredSummary;
        int[] deadSummary; 
        int[] pollutionSummary;
        double[][] riverHere;
        int initTotalPollution = 2500;
        double infectionProbability = 0.001;
        double depositionProbability = 0.002;
        double motionProbability = 0.20; 
        int run;
        Random r = new Random();
        
        public riverDataGenerator(int i){
            run = i;
        }
        
        public void run(){
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

            File folder = new File("D:/synthLandscan/randomStarts/river/run" + run);
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

//                System.out.println("values is " + values.length + "x" + values[1].length);

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

                //start with all the pollution at a random source
                boolean foundRiver = false;
                int startX = 0;
                int startY = 0;                
                while (!(foundRiver)){
                    startX = r.nextInt(84);
                    startY = r.nextInt(84);
                    if (riverHere[startX][startY] > 0){
                        foundRiver = true;
                        concentration[startX][startY] = initTotalPollution;
                    }
                }

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
                                    //for debugging purposes
//                                    if ((i == 83) || (j == 83)){
//                                        System.out.println("STOP!");
//                                    }
                                    double mileageNorth;
                                    if (i >= riverHere.length - 1){
                                        mileageNorth = -2;
                                    } else {
                                        mileageNorth = riverHere[i+1][j];
                                    }
                                    double mileageEast;
                                    if (j < riverHere[i].length - 1){
                                        mileageEast = riverHere[i][j+1];
                                    } else {
                                        mileageEast = -2;
                                    }                                   
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

                writeSummaries(run, startX, startY);
//                System.out.println("time is " + time);
                if (time > 250){
                    done = true;
                    System.out.println("finished river #" + run);
                }
            }            
        }

        private void writeData(int time, int run){
            try{
                String outFileName = "riverPAtime" + time + ".csv";
                FileWriter outFile = new FileWriter("D:/synthLandscan/randomStarts/river/run" + run + "/" + outFileName);

                outFile.write("x, y, pop, cases" + '\n');
                for (int i = 0; i < susceptible.length; i++){
                    for (int j = 0; j < susceptible[i].length; j++){
                        int background = susceptible[i][j] + infectious[i][j] + recovered[i][j];
                        outFile.write(i + ", " + j + ", " + background + ", " + infectious[i][j] + '\n');
                    }
                }

                outFile.close();
//                System.out.println("wrote " + outFileName);
            } catch (IOException ioe){
                System.out.println("IOException when saving file: " + ioe.getMessage());
            }        
        }

        private void writeSummaries(int run, int startX, int startY){ 
            try{
                FileWriter outFile = new FileWriter("D:/synthLandscan/randomStarts/river/run" + run + "/summary.csv");
                outFile.write("start location is (" + startX + ". " + startY + ")" + '\n');
                outFile.write("time, infected, recovered, dead" + '\n');
                for (int i = 0; i < infectedSummary.length; i++){
                    if ((infectedSummary[i] > 0) || (recoveredSummary[i] > 0)){
                        outFile.write(i*5 + ", " + infectedSummary[i] + ", " + recoveredSummary[i] + ", " + deadSummary[i] + '\n');
                    }
                }

                outFile.close();
//                System.out.println("wrote summary");

            } catch (IOException ioe){
                System.out.println("IOException when saving summary file: " + ioe.getMessage());
            } 
        }

        private void expandSummaryArraySizes(){
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
    
    private class windDataGenerator implements Runnable{
        
        int[][] susceptible;
        int[][] concentration;
        int[][] infectious;
        int[][] recovered;
        int[][][] recoverNow;    
        int initTotalPollution = 2500;
        int[] pollutionSummary;
        int[] infectedSummary;
        int[] recoveredSummary;    
        double infectionProbability = 0.001;
        double depositionProbability = 0.002;
        double motionProbability = 0.08;          
        int run;
        Random r = new Random();
        
        public windDataGenerator(int i){
            run = i;
        }
        
        public void run(){
            String[][] values = new String[84][84];

            File folder = new File("D:/synthLandscan/randomStarts/wind/run" + run);
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

//                System.out.println("values is " + values.length + "x" + values[1].length);

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
                infectedSummary = new int[25];
                recoveredSummary = new int[25];
                pollutionSummary = new int[25];

                for (int i = 0; i < concentration.length; i++){
                    Arrays.fill(concentration[i], 0);
                }

                //start with all the pollution at a random source
                int startX = r.nextInt(84);
                int startY = r.nextInt(84);
                concentration[startX][startY] = initTotalPollution;

                double northProb, southProb, eastProb, westProb;
                if (startX < 43){
                    northProb = 0.05;
                    southProb = 0.45;                             
                } else {
                    southProb = 0.05;
                    northProb = 0.45;
                }
                
                if (startY < 43){
                    eastProb = 0.45;
                    westProb = 0.05;
                } else {
                    westProb = 0.45;
                    eastProb = 0.05;
                }
                
                
                
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
                                    if (s < westProb){  //i controls east/west
                                        if (i > 0){
                                            nextStepConcentration[i-1][j]++;
                                        } else {
                                            airbornePollution--;
                                        }                                       
                                    } else if (s < (northProb + westProb)){
                                        if (j > 0){
                                            nextStepConcentration[i][j-1]++;                                
                                        } else {
                                            airbornePollution--;
                                        } 
                                    } else if (s < (northProb + westProb + eastProb)){  
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
//                        System.out.println("total airborne pollution is " + airbornePollution);
//                        System.out.println("there are " + totalInfected + " infected.");
//                        System.out.println(totalRecovered + " have recovered.");

                        if (time/5 >= pollutionSummary.length){
                            expandSummaryArraySizes();
                        }

                        pollutionSummary[time/5] = airbornePollution;
                        infectedSummary[time/5] = totalInfected;
                        recoveredSummary[time/5] = totalRecovered;                       
                    }

                    time++;
                }  //end while (airbornePollution > 0)                

                writeSummaries(run, startX, startY);                
//                System.out.println("time is " + time);
                if (time > 250){
                    done = true;
                    System.out.println("finished wind #" + run);
                }  
            }            
        }

        private void expandSummaryArraySizes(){
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

        private void writeData(int time, int run){
            try{
                String outFileName = "windPAtime" + time + ".csv";
                FileWriter outFile = new FileWriter("D:/synthLandscan/randomStarts/wind/run" + run + "/" + outFileName);

                outFile.write("x, y, pop, cases" + '\n');
                for (int i = 0; i < susceptible.length; i++){
                    for (int j = 0; j < susceptible[i].length; j++){
                        int background = susceptible[i][j] + infectious[i][j] + recovered[i][j];
                        outFile.write(i + ", " + j + ", " + background + ", " + infectious[i][j] + '\n');
                    }
                }

                outFile.close();
//                System.out.println("wrote " + outFileName);
            } catch (IOException ioe){
                System.out.println("IOException when saving file: " + ioe.getMessage());
            }        
        }

        private void writeSummaries(int run, int startX, int startY){ 
            try{
                FileWriter outFile = new FileWriter("D:/synthLandscan/randomStarts/wind/run" + run + "/summary.csv");
                outFile.write("start location (" + startX + ". " + startY + ")" + '\n');
                outFile.write("time, infected, recovered, pollution" + '\n');
                for (int i = 0; i < infectedSummary.length; i++){
                    if ((infectedSummary[i] > 0) || (recoveredSummary[i] > 0)){
                        outFile.write(i*5 + ", " + infectedSummary[i] + ", " + recoveredSummary[i] + ", " + pollutionSummary[i] + '\n');
                    }
                }

                outFile.close();
//                System.out.println("wrote summary");

            } catch (IOException ioe){
                System.out.println("IOException when saving summary file: " + ioe.getMessage());
            } 
        }    
    }    
    
    private class airportDataGenerator implements Runnable{
        
        int[][] susceptible;
        int[][] infectious;
        int[][] recovered;
        int[][] dead;
        int[][][] recoverNow;
        int[] infectedSummary;
        int[] recoveredSummary;
        int[] deadSummary; 
        int[][] roadHere;
        int[][] airportHere;
        int[][] airConnections;
        int[][] airportList;
        int run;
        Random r = new Random();
        
        public airportDataGenerator(int i){
            run = i;
        }
        
        public void run(){
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

            File folder = new File("D:/synthLandscan/randomStarts/airport/run" + run);
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

//                System.out.println("values is " + values.length + "x" + values[1].length);

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
                int startX = 0;
                int startY = 0;
                boolean inhabited = false;
                while (!(inhabited)){
                    startX = r.nextInt(84);
                    startY = r.nextInt(84);
                    if (susceptible[startX][startY] > 0){
                        infectious[startX][startY]++;  
                        recoverNow[0][startX][startY]++;
                        susceptible[startX][startY]--;
                        totalInfected++;
                        inhabited = true;
                    }
                }

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

                writeSummaries(run, startX, startY);
//                System.out.println("time is " + time);
                if (time > 250){
                    done = true;
                    System.out.println("finished airport #" + run);
                }
            }            
        }

        private void writeData(int time, int run){
            try{
                String outFileName = "airportPAtime" + time + ".csv";
                FileWriter outFile = new FileWriter("D:/synthLandscan/randomStarts/airport/run" + run + "/" + outFileName);

                outFile.write("x, y, pop, cases" + '\n');
                for (int i = 0; i < susceptible.length; i++){
                    for (int j = 0; j < susceptible[i].length; j++){
                        int background = susceptible[i][j] + infectious[i][j] + recovered[i][j];
                        outFile.write(i + ", " + j + ", " + background + ", " + infectious[i][j] + '\n');
                    }
                }

                outFile.close();
//                System.out.println("wrote " + outFileName);
            } catch (IOException ioe){
                System.out.println("IOException when saving file: " + ioe.getMessage());
            }        
        }

        private void writeSummaries(int run, int startX, int startY){ 
            try{
                FileWriter outFile = new FileWriter("D:/synthLandscan/randomStarts/airport/run" + run + "/summary.csv");
                outFile.write("start location is {" + startX + ". " + startY + ")" + '\n');
                outFile.write("time, infected, recovered, dead" + '\n');
                for (int i = 0; i < infectedSummary.length; i++){
                    if ((infectedSummary[i] > 0) || (recoveredSummary[i] > 0)){
                        outFile.write(i*5 + ", " + infectedSummary[i] + ", " + recoveredSummary[i] + ", " + deadSummary[i] + '\n');
                    }
                }

                outFile.close();
//                System.out.println("wrote summary");

            } catch (IOException ioe){
                System.out.println("IOException when saving summary file: " + ioe.getMessage());
            } 
        }

        private void expandSummaryArraySizes(){
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
    
    private class rasterAndMomentGenerator implements Runnable{  //partialDataGenerator from PLandscanTrainingGenerator       
        
        String type, method;
        int run;
        
        public rasterAndMomentGenerator(String typ, String meth, int i){
            type = typ;
            method = meth;
            run = i;
        }
        
        public void run(){        
            MomentGenerator mg = new MomentGenerator(); 
                             
            //for each of the three types             
            double[][] moments = new double[40][];                         
            //get the track length from the summary
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

            //for 40 evenly-spaced time steps
            for (int step = 0; step < 40; step++){
                double interval = (double) length / 40d;
                int time = nearestMultipleOfFive((int) Math.round(interval * step));
                File file = new File("D:/synthLandscan/randomStarts/" + type + "/run" + run + "/" + type + "PAtime" + time + ".csv");
                float[][] raster;
                //run GAM & get a coverage out of it (or use the base raster)
                if (method.equals("GAM")){
                    raster = GAMRasterFromFile(file);
                } else {
                    raster = baseRasterFromFile(file);
                }
                mg.setRaster(raster);
                //calculate moments
                double[] temp = mg.calculateMomentInvariants();
                moments[step] = new double[temp.length];
                for (int blob = 0; blob < temp.length; blob++){
                    moments[step][blob] = temp[blob];
                }
//                System.out.println("finished moments for method" + method + ", type " + type + ", run " + run + ", step " + step); 
                try{
                    File momentFile = new File("D:/synthLandscan/randomStarts/" + type + "/run" + run + "/" + method + "Moments" + step + ".csv");
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
//                    }
            }  //end for (int step...
            //stash away for safekeeping and repeat, repeat, repeat!                 
            try{
                File momentFile = new File("D:/synthLandscan/randomStarts/" + type + "/run" + run + "/" + method + "Moments.csv");
                FileWriter momentWriter = new FileWriter(momentFile);
                for (int k = 0; k < moments.length; k++){
                    momentWriter.write(""+moments[k][0]);
                    for (int l = 1; l < moments[k].length; l++){
                        momentWriter.write("," + moments[k][l]);
                    }
                    momentWriter.write('\n');
                }   
                momentWriter.close();
            } catch (IOException ioe){
                System.out.println("IOException writing moment file.  " + ioe.getMessage());
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

        private float[][] GAMRasterFromFile(File inFile){
            //most of this stuff is from BatchMain
            //run GAM on this file
            InitGAMFile igf = new InitGAMFile();
            try{
                igf.processTextFileWithoutDialog(inFile);
            } catch (FileNotFoundException fnfe){
                System.out.println("why isn't " + inFile + " found?  " + fnfe.getMessage());
            } catch (IOException ioe){
                System.out.println("io exception: " + ioe.getMessage());
            }

            SystematicGam gam = new SystematicGam();
            double largeDimension = Math.max(igf.getMaxX() - igf.getMinX(),
                                             igf.getMaxY() - igf.getMinY());              
            gam.setMaxRadius(0.05 * largeDimension);
            gam.setMinRadius(0.005 * largeDimension);
            gam.setMinPoints(3);
            gam.setMinAccepted(1.5);
            gam.setFitnessFunction(new FitnessRelativePct(igf.getDataSet()));  
            gam.setInitializer(igf);  

            ImageProcessor ip = new ImageProcessor(); 
            ip.setInitializer(igf);
            ip.setGam(gam);                 

            //get the image
            GridCoverage2D coverage = ip.generateCoverageGAMOpt();

            //binarize it
            int floor = (int) Math.floor(ip.getMinHeight());
            int ceiling = (int) Math.ceil(ip.getMaxHeight());             

            AbstractProcessor def = DefaultProcessor.getInstance(); 
            Operations ops = new Operations(new RenderingHints(null));
            Operation binOp = def.getOperation("Binarize");
            ParameterValueGroup pvg = binOp.getParameters();

            GridCoverage2D band0 = (GridCoverage2D) ops.selectSampleDimension(coverage, new int[]{0});
            //I have the space, so use it!
            String noExtension = inFile.getPath().substring(0, inFile.getPath().length() - 4);        
            String filePath = noExtension + "GAMRaster.csv";
            saveRaster(DissUtils.coverageToMatrix(band0), filePath);

            pvg.parameter("Source").setValue(band0);

            pvg.parameter("Threshold").setValue(new Double(floor + (ceiling - floor)/4));
            GridCoverage2D binarized = (GridCoverage2D) def.doOperation(pvg);                 
            return DissUtils.coverageToMatrix(binarized);  
        }    

        private float[][] baseRasterFromFile(File inFile){
    //        int rasterSum = 0;
            float[][] raster = new float[1105][1105];
            float[][] smoothed = new float[1105][1105];
            String[][] values;
            try{
                FileReader in = new FileReader(inFile);
                CSVParser csvp = new CSVParser(in);
                values = csvp.getAllValues();         

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
    //                            rasterSum++;
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
                //I have the space, so use it!
                String noExtension = inFile.getPath().substring(0, inFile.getPath().length() - 4);
                String filePath = noExtension + "baseRaster.csv";
                saveRaster(smoothed, filePath);              
                return smoothed;
            } catch (IOException ioe){
                System.out.println("what the? " + ioe.getMessage());
                return null;  //deliberately returning null instead of smoothed to generate an error instead of chugging along with a raster full of zeros.
            }            

        }

        private void saveRaster(float[][] raster, String filePath){
            File f = new File(filePath);
            try{
                FileWriter out = new FileWriter(f);
                for (int i = 0; i < raster.length; i++){
                    for (int j = 0; j < raster[i].length; j++){
                        out.write(raster[i][j] + ",");
                    }
                    out.write('\n');
                }
                out.close();
                System.out.println("raster saved to " + filePath);
            } catch (IOException ioe){
                System.out.println("IOException when saving raster to " + filePath);
            }
        }
    }
        
}

