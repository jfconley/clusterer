/*
 * KNNTestWithValidation.java
 *
 * Created on July 5, 2007, 12:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.DissUtils;
import dissprogram.classification.ClassedArray;
import dissprogram.classification.DistanceWeightedKNN;
import dissprogram.classification.SimpleKNN;
import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author z4x
 */
public class KNNTestWithValidation {
    
    /** Creates a new instance of KNNTestWithValidation */
    public KNNTestWithValidation() {
    }
    
    private static String getClassesString(String[] classes){
        String ret = classes[0];
        for (int i = 1; i < classes.length; i++){
            ret = ret + "," + classes[i];
        }
        return ret + ",null";
    }
    
    public static void main(String[] args) {
        //read in file
        File f;
        String[][] training;
        try{
            //this is the training data
            f = new File("C:/z4xNoSpaces/synthData/comparison/GAM/GAMZTraining.csv");
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            training = csvp.getAllValues();
        } catch (FileNotFoundException fnfe){
            throw new RuntimeException("No file!  " + fnfe.getMessage());
        } catch (IOException ioe){
            throw new RuntimeException("IO Exception!  " + ioe.getMessage());
        }
        
        //to see if leaving one or two invariants out improves classification (Auroop from ORNL's suggestion)
        boolean[] include = new boolean[]{true, true, true, true, true, true, true};
        int numInvsUsed = 0;
        for (int i = 0; i < include.length; i++){
            if (include[i]){
                numInvsUsed++;
            }
        }
        
        System.out.print("include = {" + include[0]);
        for (int i = 1; i < include.length; i++){
            System.out.print(", " + include[i]);
        }
        System.out.println("}");
        
        ClassedArray[] trainingMatrix = new ClassedArray[training.length-1];
        for (int i = 1; i < training.length; i++){
            double[] data = new double[numInvsUsed]; //was values[i].length - 1
            int index = 0;  //because I can't assume it will be data[j]--some values of j might be skipped by the include array
            for (int j = 0; j < training[i].length - 1; j++){
                if (include[j]){
                    data[index] = Double.parseDouble(training[i][j]);
                    index++;
                }
            }
            ClassedArray ca = new ClassedArray();
            ca.setArray(data);
            ca.setKlass(training[i][training[i].length - 1]);
            trainingMatrix[i-1] = ca;
        }
        
        String[] classes = DissUtils.extractClassNames(trainingMatrix);
        
        String[][] validation;
        try{
            //this is the validation data
            File v = new File("C:/z4xNoSpaces/synthData/comparison/GAM/GAMZValidation.csv");
            FileReader in = new FileReader(v);
            CSVParser csvp = new CSVParser(in);
            validation = csvp.getAllValues();
        } catch (FileNotFoundException fnfe){
            throw new RuntimeException("No file!  " + fnfe.getMessage());
        } catch (IOException ioe){
            throw new RuntimeException("IO Exception!  " + ioe.getMessage());
        }
        
        int maxK = 500;
        
//        String[][] neighborClasses = new String[validation.length-1][maxK];
        String[][] klasses = new String[validation.length-1][maxK];
        String[] realClasses = new String[validation.length-1];
        double[][] accuracyRates = new double[classes.length+1][maxK];
        int[][][] confusionMatrices = new int[maxK][classes.length][classes.length+1];
        double[][] probRates = new double[classes.length+1][maxK];
        double[][][] probabilityMatrices = new double[maxK][classes.length][classes.length];
        
//        SimpleKNN knn = new SimpleKNN();
        DistanceWeightedKNN knn = new DistanceWeightedKNN();
        knn.setTrainingData(trainingMatrix);
        
        //for each value of k
        for (int k = 1; k <= maxK; k++){
            knn.setK(k);
            //for each data point in the validation dataset
            for (int i = 1; i < validation.length; i++){
                double[] data = new double[numInvsUsed]; //was values[i].length - 1
                int index = 0;  //because I can't assume it will be data[j]--some values of j might be skipped by the include array
                for (int j = 0; j < validation[i].length - 1; j++){
                    if (include[j]){
                        data[index] = Double.parseDouble(validation[i][j]);
                        index++;
                    }
                }
                
                //have the data point
//                String[] myNeighborClasses = knn.neighborClasses(data);
//                neighborClasses[i-1] = myNeighborClasses;
                double[] probabilities = knn.classifyProb(data);
                String myEstimatedClass = knn.classify(data);
                klasses[i-1][k-1] = myEstimatedClass;
                String myRealClass = validation[i][validation[i].length-1];
                realClasses[i-1] = myRealClass;
                int myEstimatedIndex = findIndex(classes, myEstimatedClass);
                int myRealIndex = findIndex(classes, myRealClass);
                if (myEstimatedIndex != -1){
                    confusionMatrices[k-1][myRealIndex][myEstimatedIndex]++;
                } else {
                    confusionMatrices[k-1][myRealIndex][classes.length]++;
                }
                for (int z = 0; z < probabilities.length; z++){
                    probabilityMatrices[k-1][myRealIndex][z] = probabilityMatrices[k-1][myRealIndex][z] + probabilities[z];
                }
            }
            for (int a = 0; a < probabilityMatrices[k-1].length; a++){
                //this loop figures out how many have the real class of row a.
                int count = arraySum(confusionMatrices[k-1][a]);
                //divide each of the sums in the probability matrices by the number with the real class of row a to find
                //the average for that cell
                for (int b = 0; b < probabilityMatrices[k-1][a].length; b++){
                    probabilityMatrices[k-1][a][b] = probabilityMatrices[k-1][a][b]/count;
                }
                //now I can update probRates, too
                for (int d = 0; d < classes.length; d++){
                    probRates[d][k-1] = probabilityMatrices[k-1][d][d];
                }
                
            }
            System.out.println("finished k=" + k);
        }
        
        //finished making the confusion matrices and recording the neighbor classes.  Now calculate accuracy rates.
        for (int k = 0; k < maxK; k++){
            int totalCorrect = 0;
            int total = 0;
            double probSum = 0;
            for (int c = 0; c < classes.length; c++){
                int arraysum = arraySum(confusionMatrices[k][c]);
                accuracyRates[c][k] = (double) confusionMatrices[k][c][c] / (double) arraysum;
                totalCorrect = totalCorrect + confusionMatrices[k][c][c];
                total = total + arraysum;
                probRates[c][k] = probabilityMatrices[k][c][c];
                probSum = probSum + (probRates[c][k] * (double) arraysum);
            }
            accuracyRates[classes.length][k] = (double) totalCorrect / (double) total;
            probRates[classes.length][k] = probSum / (double) total;
        }
        
        printResults(classes, klasses, realClasses, accuracyRates, confusionMatrices, probabilityMatrices, probRates);
    }
    
    private static int findIndex(String[] classes, String klass){
        int ret = -1;
        for (int i = 0; i < classes.length; i++){
            if (klass.equalsIgnoreCase(classes[i])){
                ret = i;
            }
        }
        return ret;
    }
    
    private static int arraySum(int[] arr){
        int ret = 0;
        for (int i = 0; i < arr.length; i++){
            ret = ret + arr[i];
        }
        return ret;
    }
    
    private static void printResults(String[] classList, String[][] neighborClasses, String[] realClasses, double[][] accuracyRates, int[][][] confusionMatrices, double[][][] probMatrices, double[][] probRates){
        //because Excel is stupid enough to limit to 256 columns.
        boolean vertical = neighborClasses[0].length > 255;
        int[] unclassified = new int[neighborClasses[0].length];
        

            
        if (vertical){
            //get the error rates
            System.out.println();
            System.out.println("accuracy rates");
            for (int i = 0; i < classList.length; i++){
                System.out.print(classList[i] + ",");
            }
            System.out.println("total,unclassified");
            
            for (int i = 0; i < accuracyRates[0].length; i++){
                for (int j = 0; j < accuracyRates.length; j++){
                    System.out.print("," + accuracyRates[j][i]);
                }
                System.out.println("," + ((double) unclassified[i] / (double) neighborClasses.length));
            }
        } else {
            //get the class matrix (only if not vertical, because it won't fit if the vertical is used
            System.out.println("classification matrix");
            for (int i = 0; i < neighborClasses.length; i++){
                System.out.print(realClasses[i]);
                for (int j = 0; j < neighborClasses[i].length; j++){
                    System.out.print("," + neighborClasses[i][j]);
                    if (neighborClasses[i][j].equalsIgnoreCase("null")){
                        unclassified[j]++;
                    }
                }
                System.out.println();
            }

            //get the error rates
            System.out.println();
            System.out.println("accuracy rates");
            for (int i = 0; i < accuracyRates.length; i++){
                if (i < classList.length){
                    System.out.print(classList[i]);
                } else {
                    System.out.print("total");
                }
                for (int j = 0; j < accuracyRates[i].length; j++){
                    System.out.print("," + accuracyRates[i][j]);
                }
                System.out.println();
            }
        }
        
        //Confusion matrices
        System.out.println();
        System.out.println("confusion matrix");
        for (int k = 5; k <= confusionMatrices.length; k=k+5){
            System.out.println();
            System.out.println("k=" + k + "," + getClassesString(classList));
            int[][] confusionMatrix = confusionMatrices[k-1];
            for (int i = 0; i < confusionMatrix.length; i++){
                System.out.print(classList[i]);
                for (int j = 0; j < confusionMatrix[i].length; j++){
                    System.out.print("," + confusionMatrix[i][j]);
                }
                System.out.println();
            }
        }
        
        //Probability matrices
        System.out.println();
        System.out.println("probability matrices");
        for (int k = 5; k <= probMatrices.length; k=k+5){
            System.out.println();
            System.out.println("k=" + k + "," + getClassesString(classList));
            double[][] probabilityMatrix = probMatrices[k-1];
            for (int i = 0; i < probabilityMatrix.length; i++){
                System.out.print(classList[i]);
                for (int j = 0; j < probabilityMatrix[i].length; j++){
                    System.out.print("," + probabilityMatrix[i][j]);
                }
                System.out.println();
            }
        }
        
        if (vertical){
            //get the error rates
            System.out.println();
            System.out.println("KNN probability correct");
            for (int i = 0; i < classList.length; i++){
                System.out.print(classList[i] + ",");
            }
            System.out.println("total");
            
            for (int i = 0; i < probRates[0].length; i++){
                for (int j = 0; j < probRates.length; j++){
                    System.out.print("," + probRates[j][i]);
                }
                System.out.println();
            }
        } else {
            //KNN probability correct        
            System.out.println();
            System.out.println("KNN probability correct");
            for (int i = 0; i < probRates.length; i++){
                if (i < classList.length){
                    System.out.print(classList[i]);
                } else {
                    System.out.print("total");
                }
                for (int j = 0; j < probRates[i].length; j++){
                    System.out.print("," + probRates[i][j]);
                }
                System.out.println();
            }
        }
    }
    
}
