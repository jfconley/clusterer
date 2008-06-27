/*
 * KNNTest.java
 *
 * Created on February 28, 2007, 12:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.DissUtils;
import dissprogram.classification.ClassedArray;
import dissprogram.classification.KNearestNeighbor;
import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author jfc173
 */
public class KNNTest {
    
    /** Creates a new instance of KNNTest */
    public KNNTest() {
    }        
    
    private static String getClassesString(String[] classes){
        String ret = classes[0];
        for (int i = 1; i < classes.length; i++){
            ret = ret + "," + classes[i];
        }
        return ret;
    }
    
    public static void main(String[] args) {
        //read in file
        File f;
        String[][] values;
        try{
            f = new File("C:/z4xNoSpaces/synthData/comparison/GAM/GAMMomentsZMerge3.csv");
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            values = csvp.getAllValues(); 
        }
        catch (FileNotFoundException fnfe){
            throw new RuntimeException("No file!  " + fnfe.getMessage());            
        }       
        catch (IOException ioe){
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
        
//        //create classedArray[]
//        ClassedArray[] matrix = new ClassedArray[values.length-1];
//        for (int i = 1; i < values.length; i++){
//            double[] data = new double[values[i].length - 1];
//            for (int j = 0; j < values[i].length - 1; j++){
//                data[j] = Double.parseDouble(values[i][j]);                
//            }
//            ClassedArray ca = new ClassedArray();
//            ca.setArray(data);
//            ca.setKlass(values[i][values[i].length - 1]);  
//            matrix[i-1] = ca;
//        }        
        
        String[] classes = DissUtils.extractClassNames(matrix);
        
        //run the KNN
        KNearestNeighbor knn = new KNearestNeighbor();
        knn.setMaxK(100);
        knn.setData(matrix);
        knn.calculateMatrices();
                
        //get the rank matrix
//        String[][] rankMatrix = knn.getLeaveOneOutRankMatrix();
//        for (int i = 0; i < rankMatrix.length; i++){
//            System.out.print(matrix[i].getKlass());
//            for (int j = 0; j < rankMatrix[i].length; j++){
//                System.out.print("," + rankMatrix[i][j]);
//            }
//            System.out.println();
//        }
        
        //get the class matrix
        System.out.println("classification matrix");
        String[][] classMatrix = knn.getLeaveOneOutKNNClassMatrix();
        for (int i = 0; i < classMatrix.length; i++){
            System.out.print(matrix[i].getKlass());
            for (int j = 0; j < classMatrix[i].length; j++){
                System.out.print("," + classMatrix[i][j]);
            }
            System.out.println();
        }       
        
        //get the error rates
        System.out.println();
        System.out.println("error rates");
        double[][] errorRates = knn.getErrorRates();
        for (int i = 0; i < errorRates.length; i++){
            System.out.print(classes[i]);
            for (int j = 0; j < errorRates[i].length; j++){
                System.out.print("," + errorRates[i][j]);
            }
            System.out.println();
        }
        
        //Confusion matrices        
        System.out.println();
        System.out.println("confusion matrix");
        int[] kArray = new int[]{5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100};
        for (int h = 0; h < kArray.length; h++){
            int k = kArray[h];
            System.out.println();
            System.out.println();
            System.out.println("k=" + k + "," + getClassesString(classes));
            int[][] confusionMatrix = knn.getConfusionMatrix(k);
            for (int i = 0; i < confusionMatrix.length; i++){
                System.out.print(classes[i]);
                for (int j = 0; j < confusionMatrix[i].length; j++){
                    System.out.print("," + confusionMatrix[i][j]);
                }
                System.out.println();
            }
        }
        
        //Probability matrices
        System.out.println();
        System.out.println("probability matrices");
        for (int h = 0; h < kArray.length; h++){
            int k = kArray[h];
            System.out.println();
            System.out.println();
            System.out.println("k=" + k + "," + getClassesString(classes));
            double[][] probabilityMatrix = knn.getProbMatrix(k);
            for (int i = 0; i < probabilityMatrix.length; i++){
                System.out.print(classes[i]);
                for (int j = 0; j < probabilityMatrix[i].length; j++){
                    System.out.print("," + probabilityMatrix[i][j]);
                }
                System.out.println();
            }
        }        
        
        //KNN probability correct
        System.out.println();
        System.out.println("KNN probability correct");
        double[][] kpMatrix = knn.getKProbMatrix();
        for (int i = 0; i < kpMatrix.length; i++){
            System.out.print(classes[i]);
            for (int j = 0; j < kpMatrix[i].length; j++){
                System.out.print("," + kpMatrix[i][j]);
            }            
            System.out.println();
        }
        
    }
    
}
