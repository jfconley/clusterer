/*
 * KMeans.java
 *
 * Created on January 25, 2007, 11:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.classification;

//import org.jmat.data.Matrix;

/**
 *
 * @author jfc173
 */
public class KMeans implements AbstractUnsupervisedClassifier{
    
    private double[][] matrix;
    private int[] classes;
    private int k;
    private double[][] means;
    
    /** Creates a new instance of KMeans */
    public KMeans() {
    }
    
    public void setMatrix(double[][] array){
        matrix = array;
    }
    
    public void setK(int i){
        k = i;
    }
    
    public double[][] getMeans(){
        return means;
    }
    
    public void run(){
        //initialize means as k vectors randomly selected from the matrix
        means = new double[k][matrix[0].length];
        for (int i = 0; i < k; i++){
            means[i] = matrix[(int) Math.round(Math.random() * matrix.length)];
        }
        
        //initialize the classes array
        classes = new int[matrix.length];
        for (int i = 0; i < matrix.length; i++){
            double nearestDist = Double.MAX_VALUE;
            for (int j = 0; j < k; j++){
                //find the distance from the ith vector to the jth mean.
                //want to allow other distance metric than Euclidean (e.g., Mahalanobis and/or Manhattan)
                double dist = calculateEuclideanDist(i, j);
                if (dist < nearestDist){
                    nearestDist = dist;
                    classes[i] = j;
                }                
            }
        }
        
        boolean someoneMoved = true;
        
        while (someoneMoved){
            //relocate means.  Only need to relocate means if someone moved, so 
            //this can be done at the beginning of the next cycle through the 
            //loop instead of the end.  This saves one recalculation of the means.
            for (int i = 0; i < k; i++){
                moveMean(i);
            }
            
            someoneMoved = false;
            //check the vectors in the matrix against the new means.
            for (int i = 0; i < matrix.length; i++){
                double nearestDist = Double.MAX_VALUE;
                int temp = classes[i];
                for (int j = 0; j < k; j++){
                    //find the distance from the ith vector to the jth mean.
                    //want to allow other distance metric than Euclidean (e.g., Mahalanobis and/or Manhattan)
                    double dist = calculateEuclideanDist(i, j);                    
                    if (dist < nearestDist){
                        nearestDist = dist;
                        temp = j;
                    }
                }
                if (temp != classes[i]){
                    classes[i] = temp;
                    someoneMoved = true;
                }
            } 
        }
        
    }
    
    public int[] getClassArray(){
        return classes;
    }
    
    private double calculateEuclideanDist(int i, int j){
        double[] vector = matrix[i];
        double[] mean = means[j];
        if (vector.length != mean.length){
            throw new RuntimeException("can't calculate Euclidean distance if vector and mean don't have the same number of dimensions.");
        }
        double sum = 0;
        for (int l = 0; l < vector.length; l++){
            sum = sum + Math.pow(vector[l] - mean[l], 2);
        }
//        System.out.println("dist from vector " + i + " to mean " + j + " is " + Math.sqrt(sum));
        return Math.sqrt(sum);
    }
    
    private void moveMean(int i){
        double[] sumArray = new double[means[i].length];
        int count = 0;
        for (int a = 0; a < matrix.length; a++){
            if (classes[a] == i){
                for (int q = 0; q < sumArray.length; q++){
                    sumArray[q] = sumArray[q] + matrix[a][q];
                }
                count++;
            }
        }
        for (int b = 0; b < means[i].length; b++){
            means[i][b] = sumArray[b] / count;
        }
    }
    
}
