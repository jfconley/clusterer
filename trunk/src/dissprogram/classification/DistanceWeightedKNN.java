/*
 * DistanceWeightedKNN.java
 *
 * Created on July 9, 2007, 12:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.classification;

import dissprogram.DissUtils;
import java.util.Arrays;

/**
 *
 * @author z4x
 */
public class DistanceWeightedKNN implements AbstractSupervisedClassifier{
    
    private String[] classes;
    private ClassedArray[] input;    
    private int k; 
    private double medianDist1;
    private double medianDistK;
    private boolean trained = false;
    
    /** Creates a new instance of DistanceWeightedKNN */
    public DistanceWeightedKNN() {
    }
    
    public void setTrainingData(ClassedArray[] array){
        input = array;
        classes = DissUtils.extractClassNames(input); 
        trained = false;
    }    
    
    public void setK(int i){
        k = i;
        trained = false;
    }
    
    public void train(){
        trained = true;
        double[] dist1Array = new double[input.length];
        double[] distKArray = new double[input.length];
        
        double[] distsForMe = new double[input.length];
        for (int i = 0; i < input.length; i++){
            for (int j = 0; j < input.length; j++){
                distsForMe[j] = DissUtils.euclideanDist(input[i].getArray(), input[j].getArray());
            }
            Arrays.sort(distsForMe);
            dist1Array[i] = distsForMe[1];  //is 1 instead of 0 because distsForMe[0] should be the distance from point i to point i (i.e., 0)
            distKArray[i] = distsForMe[k];  //is k instead of k-1 becase...
            Arrays.fill(distsForMe, 0);     //just to be sure there's no crossover from one i to the next
        } 
        Arrays.sort(dist1Array);
        Arrays.sort(distKArray);
        medianDist1 = dist1Array[(int) Math.ceil(dist1Array.length/2)];
        medianDistK = distKArray[(int) Math.ceil(distKArray.length/2)];
        System.out.println("the median distance to 1st neighbor is " + medianDist1);
        System.out.println("the median distance to kth neighbor is " + medianDistK + " (k=" + k + ")");
    }
    
    public String classify(double[] dataPoint){
       double[] probs = classifyProb(dataPoint);
       double max = 0;
       String ret = "null";
       for (int i = 0; i < classes.length; i++){
           if (probs[i] > max){
               ret = classes[i];
               max = probs[i];
           }
       }
       return ret;
    }
    
    public double[] classifyProb(double[] dataPoint){
        if (!(trained)){
            train();
        }
        String[] rankArray = new String[k];
        Arrays.fill(rankArray, "empty");
        double[] distArray = new double[input.length];
        for (int i = 0; i < input.length; i++){
            distArray[i] = DissUtils.euclideanDist(dataPoint, input[i].getArray());
        }
        double[] copy = new double[input.length];
        System.arraycopy(distArray, 0, copy, 0, input.length);  //using arraycopy to make sure the distArray doesn't get messed up.
        Arrays.sort(copy);
        double thresh = copy[k-1];
        for (int j = 0; j < input.length; j++){
            if (distArray[j] <= thresh){
                //it's less than the threshold, so get the exact position
                int blob = 0;
                boolean found = false;
                while (!(found) && (blob < k)){
                    if ((copy[blob] == distArray[j]) && (rankArray[blob].equalsIgnoreCase("empty"))){  //do the "empty" thing in case of ties.
                        rankArray[blob] = input[j].getKlass();
                        found = true;
                    }
                    blob++;
                }  
            }             
        }  
        double[] stringCounts = new double[classes.length];
        for (int j = 0; j < classes.length; j++){
            stringCounts[j] = stringCount(rankArray, copy, classes[j]);
        }
        double[] ret = new double[classes.length];
        for (int m = 0; m < classes.length; m++){
            ret[m] = stringCounts[m] / (double) k;
        }
        return ret;
    }
    
    public String[] neighborClasses(double[] dataPoint){
        String[] rankArray = new String[k];
        Arrays.fill(rankArray, "empty");
        double[] distArray = new double[input.length];
        for (int i = 0; i < input.length; i++){
            distArray[i] = DissUtils.euclideanDist(dataPoint, input[i].getArray());
        }
        double[] copy = new double[input.length];
        System.arraycopy(distArray, 0, copy, 0, input.length);  //using arraycopy to make sure the distArray doesn't get messed up.
        Arrays.sort(copy);
        double thresh = copy[k-1];
        for (int j = 0; j < input.length; j++){
            if (distArray[j] <= thresh){
                //it's less than the threshold, so get the exact position
                int blob = 0;
                boolean found = false;
                while (!(found) && (blob < k)){
                    if ((copy[blob] == distArray[j]) && (rankArray[blob].equalsIgnoreCase("empty"))){  //do the "empty" thing in case of ties.
                        rankArray[blob] = input[j].getKlass();
                        found = true;
                    }
                    blob++;
                }  
            }             
        }   
        return rankArray;
    }
    
    private double stringCount(String[] array, double[] distArray, String s){
        double ret = 0;
        for (int i = 0; i < array.length; i++){
            if (array[i].equalsIgnoreCase(s)){
                ret = ret + calculateWeight(distArray[i]);
            }
        }
        return ret;
    }     
    
    private double calculateWeight(double distance){
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
    
    public String[] getClassNames(){
        return classes;
    }    
    
}
