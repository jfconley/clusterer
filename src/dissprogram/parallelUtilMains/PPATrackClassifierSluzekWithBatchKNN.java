/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package parallelUtilMains;

import dissprogram.DissUtils;
import dissprogram.classification.ClassedArray;
import dissprogram.evidence.SetClassifier;
import dissprogram.evidence.StaticEvidenceCombiner;
import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author jconley
 */
public class PPATrackClassifierSluzekWithBatchKNN {

    static int maxK = 300;  
    static double[] means, stdevs, logMeans, logStdevs, medianDistances;
    static ClassedArray[] z;
    static String[] classNames, modeStrings;
    static int[] modes;
    
    public void PPATrackClassifierSluzekWithBatchKNN(){
        
    }
    
    public static void main(String[] args){
        
        File f;
        String[][] values;
        try{
            f = new File("D:/synthLandscan/randomStarts/momentData/GAMtestData.csv");   //DOING HU, NOT SLUZEK!!!
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            values = csvp.getAllValues();
        } catch (FileNotFoundException fnfe){
            throw new RuntimeException("No file!  " + fnfe.getMessage());
        } catch (IOException ioe){
            throw new RuntimeException("IO Exception!  " + ioe.getMessage());
        }

        int numInvs = values[1].length-1;

        //to see if leaving one or two invariants out improves classification (Auroop from ORNL's suggestion)
        boolean[] include = new boolean[numInvs];
        for (int i = 0; i < include.length; i++) {
            include[i] = true;                
        }
        int numInvsUsed = 0;
        for (int i = 0; i < include.length; i++){
            if (include[i]){
                numInvsUsed++;
            }
        }

        ClassedArray[] matrix = new ClassedArray[values.length-1];
        for (int i = 1; i < values.length; i++){
            double[] data = new double[numInvsUsed]; //was values[i].length - 1
            int index = 0;  //because I can't assume it will be data[j]--some values of j might be skipped by the include array
            for (int j = 0; j < values[i].length - 1; j++){
                if (include[j]){
                    data[index] = Double.parseDouble(values[i][j]);
                    index++;
                }
            }
            ClassedArray ca = new ClassedArray();
            ca.setArray(data);
            ca.setKlass(values[i][values[i].length - 1]);
            matrix[i-1] = ca;
        }

        z = new ClassedArray[matrix.length];
        means = new double[matrix[0].getArray().length];
        stdevs = new double[matrix[0].getArray().length];
        logMeans = new double[matrix[0].getArray().length];
        logStdevs = new double[matrix[0].getArray().length];
        double tolerance = 0.001;  //if the mean is within tolerance of 0 and the standard deviation is within tolerance of 1, then assume it's already standardized.

        //compute means
        double[] sums = new double[means.length];
        double[] logSums = new double[logMeans.length];
        for (int i = 0; i < matrix.length; i++){
            double[] snuh = matrix[i].getArray();
            for (int j = 0; j < snuh.length; j++){
                sums[j] = sums[j] + snuh[j];
                logSums[j] = logSums[j] + Math.log(Math.abs(snuh[j]));
            }
        }
        for (int k = 0; k < means.length; k++){
            means[k] = sums[k] / matrix.length;
            logMeans[k] = logSums[k] / matrix.length;
        }

        //compute stdevs
        Arrays.fill(sums, 0);
        Arrays.fill(logSums, 0);
        for (int i = 0; i < matrix.length; i++){
            double[] snuh = matrix[i].getArray();
            for (int j = 0; j < snuh.length; j++){
                sums[j] = sums[j] + ((snuh[j] - means[j]) * (snuh[j] - means[j]));
                logSums[j] = logSums[j] + ((snuh[j] - logMeans[j]) * (snuh[j] - logMeans[j]));
            }
        }
        for (int k = 0; k < stdevs.length; k++){
            stdevs[k] = Math.sqrt(sums[k] / matrix.length);
            logStdevs[k] = Math.sqrt(logSums[k] / matrix.length);
        }

        //compute standardized values (if needed)
        double[][] zMatrix = new double[matrix.length][means.length];

        for (int i = 0; i < means.length; i++){
            boolean standardized = (Math.abs(means[i]) < tolerance) && (Math.abs(stdevs[i] - 1) < tolerance);
            for (int j = 0; j < matrix.length; j++){
                if (standardized){
                    zMatrix[j][i] = matrix[j].getArray()[i];
                } else {
                    zMatrix[j][i] = (matrix[j].getArray()[i] - means[i])/stdevs[i];
                }
            }
        }

        for (int k = 0; k < matrix.length; k++){
            ClassedArray ca = new ClassedArray();
            ca.setArray(zMatrix[k]);
            ca.setKlass(matrix[k].getKlass());
            z[k] = ca;
        }

        medianDistances = new double[maxK];            
        medianDistances = medianDistances(z, maxK);                


        //didn't think the initialization would take 100 lines...  But the training data
        //and classifier should now be ready.

        modes = new int[]{SetClassifier.MODE, SetClassifier.AVERAGE, SetClassifier.SIMPLE_DS, SetClassifier.PROP_BEL_TRANS, SetClassifier.COMBINED_AVERAGE, SetClassifier.MODIFIED_AVERAGE, SetClassifier.TREE_PBT};
        modeStrings = new String[]{"mode", "average", "simple DS", "prop bel trans", "comb. ave.", "modified ave.", "tree prop bel trans"};

        classNames = DissUtils.extractClassNames(z);        
        
        System.out.println("done initializing");
        
        try{
            MethodClassifier simpleGAMSluzekInf = new MethodClassifier("GAM", true, "inf");   //DOING THE HU MOMENTS, NOT SLUZEK!!!
            MethodClassifier dwGAMSluzekInf = new MethodClassifier("GAM", false, "inf");
            MethodClassifier simpleGAMSluzekRoad = new MethodClassifier("GAM", true, "road");
            MethodClassifier dwGAMSluzekRoad = new MethodClassifier("GAM", false, "road");
            MethodClassifier simpleGAMSluzekRiver = new MethodClassifier("GAM", true, "river");
            MethodClassifier dwGAMSluzekRiver = new MethodClassifier("GAM", false, "river");
            MethodClassifier simpleGAMSluzekWind = new MethodClassifier("GAM", true, "wind");
            MethodClassifier dwGAMSluzekWind = new MethodClassifier("GAM", false, "wind");
            MethodClassifier simpleGAMSluzekAirport = new MethodClassifier("GAM", true, "airport");
            MethodClassifier dwGAMSluzekAirport = new MethodClassifier("GAM", false, "airport");            
            
            Thread simpleGAMSluzekThreadInf = new Thread(simpleGAMSluzekInf);
            Thread dwGAMSluzekThreadInf = new Thread(dwGAMSluzekInf);
            Thread simpleGAMSluzekThreadRoad = new Thread(simpleGAMSluzekRoad);
            Thread dwGAMSluzekThreadRoad = new Thread(dwGAMSluzekRoad);
            Thread simpleGAMSluzekThreadRiver = new Thread(simpleGAMSluzekRiver);
            Thread dwGAMSluzekThreadRiver = new Thread(dwGAMSluzekRiver);
            Thread simpleGAMSluzekThreadWind = new Thread(simpleGAMSluzekWind);
            Thread dwGAMSluzekThreadWind = new Thread(dwGAMSluzekWind);
            Thread simpleGAMSluzekThreadAirport = new Thread(simpleGAMSluzekAirport);
            Thread dwGAMSluzekThreadAirport = new Thread(dwGAMSluzekAirport);            
            
            simpleGAMSluzekThreadInf.start();
            dwGAMSluzekThreadInf.start(); 
            simpleGAMSluzekThreadRoad.start();
            dwGAMSluzekThreadRoad.start();
            simpleGAMSluzekThreadRiver.start();
            dwGAMSluzekThreadRiver.start();
            simpleGAMSluzekThreadWind.start();
            dwGAMSluzekThreadWind.start();
            simpleGAMSluzekThreadAirport.start();
            dwGAMSluzekThreadAirport.start();            
            
            simpleGAMSluzekThreadInf.join();
            dwGAMSluzekThreadInf.join(); 
            simpleGAMSluzekThreadRoad.join();
            dwGAMSluzekThreadRoad.join();
            simpleGAMSluzekThreadRiver.join();
            dwGAMSluzekThreadRiver.join();
            simpleGAMSluzekThreadWind.join();
            dwGAMSluzekThreadWind.join();
            simpleGAMSluzekThreadAirport.join();
            dwGAMSluzekThreadAirport.join();   
            
        } catch (InterruptedException ie){
            System.out.println(ie.getMessage());
            threadMessage("This thread was interrupted");
        }
    } 
    
    private static void threadMessage(String message){
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }   
    
    public static double[] medianDistances(ClassedArray[] trainingData, int maxK){
        double[] ret = new double[maxK];
        double[][] distances = new double[maxK][trainingData.length];

        double[] distsForMe = new double[trainingData.length];
        for (int i = 0; i < trainingData.length; i++){
            for (int j = 0; j < trainingData.length; j++){
                distsForMe[j] = DissUtils.euclideanDist(trainingData[i].getArray(), trainingData[j].getArray());
            }
            Arrays.sort(distsForMe);
            for (int k = 0; k < maxK; k++){
                distances[k][i] = distsForMe[k+1];  //is k+1 instead of k because distsForMe[0] should be the distance from point i to point i (i.e., 0)
            }
        }
        for (int k = 0; k < maxK; k++){
            Arrays.sort(distances[k]);
            ret[k] = distances[k][(int) Math.ceil(distances[k].length/2)];
        }
        return ret;
    }    
    
    private static class MethodClassifier implements Runnable{
    
        String method, type;
        boolean useSimpleKNN;        
        
        public MethodClassifier(String sMeth, boolean bKNN, String sType){
            method = sMeth;
            useSimpleKNN = bKNN;
            type = sType;
        }
        
        public void run(){
            //read in the moment data
            File f2;
            String[][] momentValues;
            int allIndex = 0;
            System.out.println(method + ": " + useSimpleKNN + ": " + type);                    
            for (int run = 200; run < 1000; run++){
                try{
                    f2 = new File("D:/synthLandscan/randomStarts/" + type + "/run"  + run + "/" + method + "Moments.csv");
                    if (f2.exists()){
                        FileReader reader = new FileReader(f2);
                        CSVParser csvp = new CSVParser(reader);
                        momentValues = csvp.getAllValues(); 
                    } else { 
                        momentValues = new String[40][];
                        for (int x = 0; x < 40; x++){
                            File next = new File("D:/synthLandscan/randomStarts/" + type + "/run" + run + "/" + method + "Moments" + x + ".csv");
                            FileReader reader = new FileReader(next);
                            CSVParser csvp = new CSVParser(reader);
                            String[][] localValues = csvp.getAllValues(); 
                            momentValues[x] = localValues[0];
                        }
                    }                            

                } catch (FileNotFoundException fnfe){
                    throw new RuntimeException("No file!  " + fnfe.getMessage());
                } catch (IOException ioe){
                    throw new RuntimeException("IO Exception!  " + ioe.getMessage());
                }

                int len = momentValues[0].length;

                double[][] moments = new double[momentValues.length][len];
                for (int i = 0; i < momentValues.length; i++){
                    for (int j = 0; j < len; j++){
                        moments[i][j] = Double.parseDouble(momentValues[i][j]);
                        if (moments[i][j] == Double.NaN){
                            moments[i][j] = Math.pow(2, -200);
                        }
                    }
                }                    

                //classify the set using each of the set classification methods                        
                double[][] zMoments = new double[moments.length][];
                String[][] neighborClasses = new String[moments.length][maxK];
                double[][] neighborDists = new double[moments.length][maxK];

                for (int i = 0; i < moments.length; i++){
                    //standardize all the moment sets in this track
                    zMoments[i] = standardizeMe(moments[i], means, stdevs);
                    //find the maxK neighbors for each of the moment sets of this track                       
                    neighborClasses[i] = neighborClasses(zMoments[i], z, maxK);
                    //find the maxK distances for each of the moments sets of this track
                    neighborDists[i] = neighborDistances(zMoments[i], z, maxK);
                }

                for (int kForKNN = 2; kForKNN < maxK+1; kForKNN++){
//                        allClassifications[kForKNN-2][allIndex] = new double[modes.length][];  
                    double[][] timeStepClassProbs = new double[zMoments.length][];
                    String[] timeStepClassWinners = new String[zMoments.length];
                    //classify each of the moment sets individually
                    for (int step = 0; step < zMoments.length; step++){                        
                        if (useSimpleKNN){
                            timeStepClassProbs[step] = classifyProbSimple(zMoments[step], neighborClasses[step], classNames, kForKNN);
                        } else {
                            timeStepClassProbs[step] = classifyProbDW(zMoments[step], neighborClasses[step], neighborDists[step], medianDistances, classNames, kForKNN);
                        }
                        timeStepClassWinners[step] = classify(timeStepClassProbs[step], classNames);                            
                    }
                    //combine for a track classification using each mode
                    double[][] thisTrackClassifications = new double[modes.length][];
                    for (int modeIndex = 0; modeIndex < modes.length; modeIndex++){
                        int mode = modes[modeIndex];
                        //store the track classifications for each mode in the appropriate slot in allClassifications
                        thisTrackClassifications[modeIndex] = StaticEvidenceCombiner.combineEvidence(timeStepClassProbs, timeStepClassWinners, classNames, mode);
                    }

                    try{
                        String kn;
                        if (useSimpleKNN){
                            kn = "simple";
                        } else {
                            kn = "DW";
                        }

                        String newDirectory = "D:/synthLandscan/randomStarts/momentData/" + method + "/miniFiles/" + kn + "/" + type + "/" + kForKNN;
                        File newDir = new File(newDirectory);
                        if (!(newDir.exists())){
                            newDir.mkdirs();        
                        }
                        FileWriter outFile = new FileWriter(newDirectory + "/" +  method + "ClassificationsRun" + run + ".csv");

                        outFile.write("mode, SIR, river, road, wind, airport, THETA" + '\n');
                        for (int j = 0; j < thisTrackClassifications.length; j++){
                            outFile.write("" + modeStrings[j]);
                            for (int k = 0; k < thisTrackClassifications[j].length; k++){
                                outFile.write("," + thisTrackClassifications[j][k]);
                            }
                            outFile.write('\n');
                        }


                        outFile.close();

                    } catch (IOException ioe){
                        System.out.println("IOException when saving summary file: " + ioe.getMessage());
                    }

//                        System.out.println("finished " + method + " simple=" + useSimpleKNN + " k=" + kForKNN + " track#" + run + " allIndex=" + allIndex);
                }   //end for kForKNN
                allIndex++;
                System.out.println("finished " + method + " simple=" + useSimpleKNN + " track#" + run + " allIndex=" + allIndex);
            }       //end for run                
            
      
        }    
        
        public static double[] standardizeMe(double[] invs, double[] means, double[] stdevs){
            //the data being compared against is Z-score(ln(abs(inv))).  
            //I have the means and stdevs for the Z-scores, so apply them 
            //to the ln(abs()) of the detected invariants
            double[] ret = new double[invs.length];
            for (int i = 0; i < invs.length; i++){
                ret[i] = (Math.log(Math.abs(invs[i])) - means[i])/stdevs[i];
            }
    //        System.out.print(invs[0]);
    //        for (int i = 1; i < invs.length; i++){
    //            System.out.print("," + invs[i]);
    //        }
    //        System.out.print("-->");
    //        
    //        System.out.print(ret[0]);
    //        for (int i = 1; i < ret.length; i++){
    //            System.out.print("," + ret[i]);
    //        }
    //        System.out.println();

            return ret;
        }        
        
        public static String[] neighborClasses(double[] dataPoint, ClassedArray[] trainingData, int maxK){
            String[] rankArray = new String[maxK];
            Arrays.fill(rankArray, "empty");
            double[] distArray = new double[trainingData.length];
            for (int i = 0; i < trainingData.length; i++){
                distArray[i] = DissUtils.euclideanDist(dataPoint, trainingData[i].getArray());
            }
            double[] copy = new double[trainingData.length];
            System.arraycopy(distArray, 0, copy, 0, trainingData.length);  //using arraycopy to make sure the distArray doesn't get messed up.
            Arrays.sort(copy);
            double thresh = copy[maxK-1];
            for (int j = 0; j < trainingData.length; j++){
                if (distArray[j] <= thresh){
                    //it's less than the threshold, so get the exact position
                    int blob = 0;
                    boolean found = false;
                    while (!(found) && (blob < maxK)){
                        if ((copy[blob] == distArray[j]) && (rankArray[blob].equalsIgnoreCase("empty"))){  //do the "empty" thing in case of ties.
                            rankArray[blob] = trainingData[j].getKlass();
                            found = true;
                        }
                        blob++;
                    }  
                }             
            }   
            return rankArray;
        }        

        public static double[] neighborDistances(double[] dataPoint, ClassedArray[] trainingData, int maxK){
            double[] ret = new double[maxK];
            double[] currentDists = new double[trainingData.length];
            for (int i = 0; i < trainingData.length; i++){
                currentDists[i] = DissUtils.euclideanDist(dataPoint, trainingData[i].getArray());
            }
            double[] copy = new double[trainingData.length];
            System.arraycopy(currentDists, 0, copy, 0, trainingData.length);  //using arraycopy to make sure the distArray doesn't get messed up.
            Arrays.sort(copy);
            for (int i = 0; i < maxK; i++){
                ret[i] = copy[i];
            }
            return ret;
        }        
        
        public static double[] classifyProbSimple(double[] dataPoint, String[] thisOnesNeighborClasses, String[] classNames, int k){
            String[] rankArray = thisOnesNeighborClasses;
            int[] stringCounts = new int[classNames.length];
            for (int j = 0; j < classNames.length; j++){
                stringCounts[j] = stringCount(rankArray, classNames[j], k);
            }
            double[] ret = new double[classNames.length];
            for (int m = 0; m < classNames.length; m++){
                ret[m] = (double) stringCounts[m] / (double) k;
            }
            return ret;
        }                
        
        private static int stringCount(String[] array, String s, int k){
            int ret = 0;
            for (int i = 0; i < k; i++){
                if (array[i].equalsIgnoreCase(s)){
                    ret++;
                }
            }
            return ret;
        }          
        
        public static double[] classifyProbDW(double[] dataPoints, String[] thisOnesNeighborClasses, double[] thisOnesNeighborDistances, double[] medianDistances, String[] classNames, int k){
            double[] stringCounts = new double[classNames.length];
            for (int j = 0; j < classNames.length; j++){
                stringCounts[j] = weightedStringCount(thisOnesNeighborClasses, thisOnesNeighborDistances, medianDistances, classNames[j], k);
            }
            double[] ret = new double[classNames.length];
            for (int m = 0; m < classNames.length; m++){
                ret[m] = stringCounts[m] / (double) k;
            }
            return ret;
        }
        
        private static double weightedStringCount(String[] array, double[] distArray, double[] medianDistances, String s, int k){
            double ret = 0;
            for (int i = 0; i < k; i++){
                if (array[i].equalsIgnoreCase(s)){
                    ret = ret + calculateWeight(distArray[i], medianDistances[0], medianDistances[k-1]);
                }
            }
            return ret;
        }     

        private static double calculateWeight(double distance, double medianDist1, double medianDistK){
            //this is based on Dudani (1976) IEEE Trans. SMC-6, 325-327
            //however, so that the weights are consistent across the dataset, in place of the distance
            //to the nearest neighbor, I use the median of all such distances, and same with the distance
            //to the kth nearest neighbor.  This necessitates handling of cases where the distance is less
            //than the median 1st or greater than the median kth.
            double ret;
            if (distance <= medianDist1){
                ret = 1;
            } else if (distance > medianDistK){
                ret = 0;
            } else {
                ret = (medianDistK - distance)/(medianDistK - medianDist1);
            }
    //        System.out.println("distance is " + distance + " so weight is " + ret);
            return ret;
        }        
        
        public static String classify(double[] probs, String[] classNames){           
           double max = -1;
           String ret = "null";
           for (int i = 0; i < classNames.length; i++){
               if ((probs[i] > max) && (probs[i] > 0)){  //need the >0 test b/c max starts at -1
                   ret = classNames[i];
                   max = probs[i];
               }
           }
           return ret;
        }        
        
    }        
    
}
