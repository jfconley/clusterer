/*
 * KNearestNeighbor.java
 *
 * Created on February 28, 2007, 11:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.classification;

import dissprogram.DissUtils;
import java.util.Arrays;

/**
 *
 * @author jfc173
 */
public class KNearestNeighbor{        
    
    String[] classes;
    
    ClassedArray[] input;
//    double[][] distMatrix;
    String[][] rankMatrix;
    String[][] classMatrix;
    int maxK;
    
    /** Creates a new instance of KNearestNeighbor */
    public KNearestNeighbor() {
    }
    
    public void setData(ClassedArray[] array){
        input = array;
        classes = DissUtils.extractClassNames(input);
    }
    
    public void setMaxK(int i){
        maxK = i;
    }
    
    public void calculateMatrices(){
//        distMatrix = new double[input.length][input.length];
//        for (int i = 0; i < input.length; i++){
//            for (int j = i; j < input.length; j++){
//                double dist = euclideanDist(input[i].getArray(), input[j].getArray());
//                distMatrix[i][j] = dist;
//                distMatrix[j][i] = dist;
//            }
//        }
        
        rankMatrix = new String[input.length][maxK];
        for (int l = 0; l < input.length; l++){
//            System.out.println("looking at #" + l);            
            double[] distances = new double[input.length];
            for (int j = 0; j < input.length; j++){
                distances[j] = euclideanDist(input[l].getArray(), input[j].getArray());                
            }            
            Arrays.fill(rankMatrix[l], "empty");
            double[] l_dists = new double[input.length];
            System.arraycopy(distances, 0, l_dists, 0, input.length);  //using arraycopy to make sure the distance matrix doesn't get messed up.
            Arrays.sort(l_dists);
            double thresh = l_dists[maxK];  //add one to ignore the zero at distMatrix[l][l]
//            System.out.println("threshold is " + thresh);
            for (int m = 0; m < input.length; m++){
                if ((l != m) && (distances[m] <= thresh)){
                    //it's less than the threshold, so get the exact position
                    int blob = 1;
                    boolean found = false;
                    while (!(found) && (blob <= maxK)){
//                        System.out.println(blob + ": comparing " + l_dists[blob] + " and " + distances[m]);
                        if ((l_dists[blob] == distances[m]) && (rankMatrix[l][blob-1].equalsIgnoreCase("empty"))){  //do the "empty" thing in case of ties.
//                            System.out.println("it's true");
                            rankMatrix[l][blob-1] = input[m].getKlass();
                            found = true;
                        }
                        blob++;
                    }  //end while
                }  //end if ((l != m)...
            }  //end for m              
        }  //end for l
        
        classMatrix = new String[input.length][maxK];
        for (int k = 1; k <= maxK; k++){
            String[] kClasses = getLeaveOneOutClasses(k);
            for (int l = 0; l < kClasses.length; l++){
                classMatrix[l][k-1] = kClasses[l];
            }
        }        
        
    }
    
//    public double[][] getDistanceMatrix(){
//        return distMatrix;
//    }
    
    public String[][] getLeaveOneOutRankMatrix(){
        return rankMatrix;
    }
    
    public String[] getLeaveOneOutClasses(int k){
        if (k > maxK){
            throw new IllegalArgumentException("k(" + k + ") must be less than or equal to maxK(" + maxK + ")");
        }
        String[] ret = new String[rankMatrix.length];
        
        for (int i = 0; i < rankMatrix.length; i++){
            String[] neighbors = new String[k];
            System.arraycopy(rankMatrix[i], 0, neighbors, 0, k);
            int[] stringCounts = new int[classes.length];
            for (int j = 0; j < classes.length; j++){
                stringCounts[j] = stringCount(neighbors, classes[j]);
            }
            int max = -1;
            for (int l = 0; l < classes.length; l++){
                if (stringCounts[l] > max){
                    max = stringCounts[l];
                    ret[i] = classes[l];
                }
            }
        }
        
        return ret;
    }        
    
    public String[][] getLeaveOneOutKNNClassMatrix(){
        String[][] ret = new String[input.length][maxK];
        for (int k = 1; k <= maxK; k++){
            String[] kClasses = getLeaveOneOutClasses(k);
            for (int l = 0; l < kClasses.length; l++){
                ret[l][k-1] = kClasses[l];
            }
        }
        return ret;
    }
    
    public double[][] getErrorRates(){
        double[][] ret = new double[classes.length][maxK];
        int[] correctSoFar = new int[classes.length];
        int[] foundSoFar = new int[classes.length];
        for (int k = 1; k <= maxK; k++){
            Arrays.fill(correctSoFar, 0);
            Arrays.fill(foundSoFar, 0);
            String[] guesses = getLeaveOneOutClasses(k);
            for (int i = 0; i < input.length; i++){
                String klass = input[i].getKlass();
                String guess = guesses[i];
                int index = whichClass(klass);
                foundSoFar[index]++;
                if (klass.equalsIgnoreCase(guess)){
                    correctSoFar[index]++;
                }
            }
            for (int j = 0; j < classes.length; j++){
                ret[j][k-1] = (double) correctSoFar[j] / (double) foundSoFar[j];
            }            
        }
        return ret;
    }
    
    public int[][] getConfusionMatrix(int k){
        int[][] ret = new int[classes.length][classes.length];
        for (int i = 0; i < input.length; i++){
            int row = whichClass(input[i].getKlass());
            int column = whichClass(classMatrix[i][k-1]);
            ret[row][column]++;
        }
        return ret;
    }
    
    public double[][] getProbMatrix(int k){
        double[][] ret = new double[classes.length][classes.length];
        for (int i = 0; i < input.length; i++){
            for (int j = 0; j < k; j++){
                int row = whichClass(input[i].getKlass());
                int column = whichClass(rankMatrix[i][j]);
                ret[row][column]++;         
            }            
        }
        for (int i = 0; i < ret.length; i++){
            for (int j = 0; j < ret[i].length; j++){
                ret[i][j] = ret[i][j] / (input.length * k / classes.length);
            }
        }
        return ret;
    }
    
    public double[][] getKProbMatrix(){
        double[][] ret = new double[classes.length][maxK];
        for (int i = 0; i < maxK; i++){
            double[][] probMatrix = getProbMatrix(i+1);
            for (int j = 0; j < classes.length; j++){
                ret[j][i] = probMatrix[j][j];
            }
        }
        return ret;
    }
    
    private int whichClass(String s){
        int ret = -1;
        int i = 0;
        boolean found = false;
        while (!(found) && (i < classes.length)){
            if (classes[i].equalsIgnoreCase(s)){
                found = true;
                ret = i;
            }
            i++;
        }
        if (!(found)){
            throw new RuntimeException("couldn't find class " + s + " in class list");
        }
        return ret;
    }
    
    private int stringCount(String[] array, String s){
        int ret = 0;
        for (int i = 0; i < array.length; i++){
            if (array[i].equalsIgnoreCase(s)){
                ret++;
            }
        }
        return ret;
    }
    
    private double euclideanDist(double[] a, double[] b){
        if (a.length != b.length){
            throw new RuntimeException("arrays must be same length: a.length = " + a.length + " and b.length = " + b.length);
        }
        double sum = 0;
        for (int i = 0; i < a.length; i++){
            sum = sum + ((a[i] - b[i]) * (a[i] - b[i]));
        }
        return Math.sqrt(sum);
    }        
    
}
