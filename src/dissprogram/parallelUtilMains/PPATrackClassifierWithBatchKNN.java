/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package parallelUtilMains;

import dissprogram.DissUtils;
import dissprogram.classification.BatchDistanceWeightedKNN;
import dissprogram.classification.BatchKNN;
import dissprogram.classification.BatchSimpleKNN;
import dissprogram.classification.ClassedArray;
import dissprogram.classification.ShapeClassifier;
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
public class PPATrackClassifierWithBatchKNN {

    public void PPATrackClassifierWithBatchKNN(){        
    }
    
    public static void main(String[] args){
        try{

//            MethodClassifier simpleGAMAffine = new MethodClassifier("GAMAffine", true);
//            MethodClassifier dwGAMAffine = new MethodClassifier("GAMAffine", false);
//            MethodClassifier simpleGAMSluzek = new MethodClassifier("GAMFullSluzek", true);
//            MethodClassifier dwGAMSluzek = new MethodClassifier("GAMFullSluzek", false);
//            MethodClassifier simpleGAMLi = new MethodClassifier("GAMLi", true);
//            MethodClassifier dwGAMLi = new MethodClassifier("GAMLi", false);
            MethodClassifier simpleGAMHu = new MethodClassifier("GAM", true);
            MethodClassifier dwGAMHu = new MethodClassifier("GAM", false);            

//            Thread simpleGAMAffineThread = new Thread(simpleGAMAffine);
//            Thread dwGAMAffineThread = new Thread(dwGAMAffine);
//            Thread simpleGAMSluzekThread = new Thread(simpleGAMSluzek);
//            Thread dwGAMSluzekThread = new Thread(dwGAMSluzek);
//            Thread simpleGAMLiThread = new Thread(simpleGAMLi);
//            Thread dwGAMLiThread = new Thread(dwGAMLi);
            Thread simpleGAMHuThread = new Thread(simpleGAMHu);
            Thread dwGAMHuThread = new Thread(dwGAMHu);
            
//            simpleGAMAffineThread.start();
//            dwGAMAffineThread.start();   
//            simpleGAMSluzekThread.start();
//            dwGAMSluzekThread.start(); 
//            simpleGAMLiThread.start();
//            dwGAMLiThread.start(); 
            simpleGAMHuThread.start();
            dwGAMHuThread.start();            

//            simpleGAMAffineThread.join();
//            dwGAMAffineThread.join();   
//            simpleGAMSluzekThread.join();
//            dwGAMSluzekThread.join(); 
//            simpleGAMLiThread.join();
//            dwGAMLiThread.join(); 
            simpleGAMHuThread.join();
            dwGAMHuThread.join();             
        } catch (InterruptedException ie){
            System.out.println(ie.getMessage());
            threadMessage("This thread was interrupted");
        }
    } 
    
    private static void threadMessage(String message){
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }     
    
    private static class MethodClassifier implements Runnable{
    
        String method;
        boolean useSimpleKNN;
        int maxK = 300;
        
        
        public MethodClassifier(String sMeth, boolean bKNN){
            method = sMeth;
            useSimpleKNN = bKNN;
        }
        
        public void run(){
            //read in the training data
            File f;
            String[][] values;
            try{
                f = new File("D:/synthLandscan/randomStarts/momentData/" + method + "testData.csv");
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

            ClassedArray[] z = new ClassedArray[matrix.length];
            double[] means = new double[matrix[0].getArray().length];
            double[] stdevs = new double[matrix[0].getArray().length];
            double[] logMeans = new double[matrix[0].getArray().length];
            double[] logStDevs = new double[matrix[0].getArray().length];
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
                logStDevs[k] = Math.sqrt(logSums[k] / matrix.length);
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

            double[] medianDistances = new double[maxK];            
            if (useSimpleKNN){
                //intentionally empty
            } else {
                medianDistances = medianDistances(z, maxK);                
            }

            //didn't think the initialization would take 100 lines...  But the training data
            //and classifier should now be ready.

            int[] modes = new int[]{SetClassifier.MODE, SetClassifier.AVERAGE, SetClassifier.SIMPLE_DS, SetClassifier.PROP_BEL_TRANS, SetClassifier.COMBINED_AVERAGE, SetClassifier.MODIFIED_AVERAGE, SetClassifier.TREE_PBT};
            String[] modeStrings = {"mode", "average", "simple DS", "prop bel trans", "comb. ave.", "modified ave.", "tree prop bel trans"};


                //read in the moment data
            File f2;
            String[][] momentValues;
            String[] types = new String[]{"inf", "road", "wind", "river", "airport"};                
//            double[][][][] allClassifications = new double[299][800*5][][];
            String[] classNames = DissUtils.extractClassNames(z);
            int allIndex = 0;
            for (int type = 0; type < types.length; type++){
                System.out.println(method + ": " + useSimpleKNN + ": " + types[type]);                    
                for (int run = 200; run < 1000; run++){
                    try{
                        f2 = new File("D:/synthLandscan/randomStarts/" + types[type] + "/run"  + run + "/" + method + "Moments.csv");
                        if (f2.exists()){
                            FileReader reader = new FileReader(f2);
                            CSVParser csvp = new CSVParser(reader);
                            momentValues = csvp.getAllValues(); 
                        } else { 
                            momentValues = new String[40][];
                            for (int x = 0; x < 40; x++){
                                File next = new File("D:/synthLandscan/randomStarts/" + types[type] + "/run" + run + "/" + method + "Moments" + x + ".csv");
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
                    if (method.equalsIgnoreCase("GAMLi")){
                        len = len - 1;
                    }
                    
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
                            
                            String newDirectory = "D:/synthLandscan/randomStarts/momentData/" + method + "/miniFiles/" + kn + "/" + types[type] + "/" + kForKNN;
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
            }           //end for type
            
//            for (int kForKNN = 2; kForKNN < maxK+1; kForKNN++){
//                try{
//                    String kn;
//                    if (useSimpleKNN){
//                        kn = "simple";
//                    } else {
//                        kn = "DW";
//                    }
//                    FileWriter outFile = new FileWriter("D:/synthLandscan/randomStarts/momentData/" + method + "/"+ kn + method + "Classifications" + kForKNN + ".csv");
//
//                    outFile.write("mode, SIR, river, road, wind, airport, THETA" + '\n');
//                    for (int i = 0; i < allClassifications[kForKNN-2].length; i++){
//                        for (int j = 0; j < allClassifications[kForKNN-2][i].length; j++){
//                            outFile.write("" + modeStrings[j]);
//                            for (int k = 0; k < allClassifications[kForKNN-2][i][j].length; k++){
//                                outFile.write("," + allClassifications[kForKNN-2][i][j][k]);
//                            }
//                            outFile.write('\n');
//                        }
//                    }
//
//                    outFile.close();
//
//                } catch (IOException ioe){
//                    System.out.println("IOException when saving summary file: " + ioe.getMessage());
//                }                        
//            }   //end for kForKNN            
        }    
        
        public double[] standardizeMe(double[] invs, double[] means, double[] stdevs){
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
        
        public String[] neighborClasses(double[] dataPoint, ClassedArray[] trainingData, int maxK){
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

        public double[] neighborDistances(double[] dataPoint, ClassedArray[] trainingData, int maxK){
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
        
        public double[] medianDistances(ClassedArray[] trainingData, int maxK){
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
        
        public double[] classifyProbSimple(double[] dataPoint, String[] thisOnesNeighborClasses, String[] classNames, int k){
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
        
        private int stringCount(String[] array, String s, int k){
            int ret = 0;
            for (int i = 0; i < k; i++){
                if (array[i].equalsIgnoreCase(s)){
                    ret++;
                }
            }
            return ret;
        }          
        
        public double[] classifyProbDW(double[] dataPoints, String[] thisOnesNeighborClasses, double[] thisOnesNeighborDistances, double[] medianDistances, String[] classNames, int k){
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
        
        private double weightedStringCount(String[] array, double[] distArray, double[] medianDistances, String s, int k){
            double ret = 0;
            for (int i = 0; i < k; i++){
                if (array[i].equalsIgnoreCase(s)){
                    ret = ret + calculateWeight(distArray[i], medianDistances[0], medianDistances[k-1]);
                }
            }
            return ret;
        }     

        private double calculateWeight(double distance, double medianDist1, double medianDistK){
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
        
        public String classify(double[] probs, String[] classNames){           
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
