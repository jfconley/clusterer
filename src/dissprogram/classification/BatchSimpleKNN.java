/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dissprogram.classification;

import dissprogram.DissUtils;
import java.util.Arrays;

/**
 *
 * @author jconley
 */
public class BatchSimpleKNN implements BatchKNN{
   
    private String[] classes;
    private ClassedArray[] input;    
    private int maxK;
    private int k;
    private double[] currentPoint;
    private String[] currentNeighbors;
    
    /** Creates a new instance of SimpleKNN */
    public BatchSimpleKNN() {
    }
    
    public void setTrainingData(ClassedArray[] array){
        input = array;
        classes = DissUtils.extractClassNames(input);
    }    
    
    public void setMaxK(int i){
        maxK = i;
    }
    
    public void setK(int i){
        k = i;
    }
    
    public void train(){
        //don't need to do anything because the ClassedArray[] already has the class values.
    }
    
    public String classify(double[] dataPoint){
        double[] probs = classifyProb(dataPoint);
        double max = -1;
        String ret = "null";
        for (int i = 0; i < classes.length; i++){
            if ((probs[i] > max) && (probs[i] > 0)){  //need the >0 test b/c max starts at -1
                ret = classes[i];
                max = probs[i];
            }
        }
        return ret;
    }
    
    public double[] classifyProb(double[] dataPoint){
        if (dataPoint != currentPoint){
            currentPoint = dataPoint;
            currentNeighbors = neighborClasses(dataPoint);
        }
        int[] stringCounts = new int[classes.length];
        for (int j = 0; j < classes.length; j++){
            stringCounts[j] = stringCount(currentNeighbors, classes[j]);
        }
        double[] ret = new double[classes.length];
        for (int m = 0; m < classes.length; m++){
            ret[m] = (double) stringCounts[m] / (double) k;
        }
        return ret;        
    }
    
    public String[] neighborClasses(double[] dataPoint){
        System.out.println("finding the neighbors for a new dataPoint");
        String[] rankArray = new String[maxK];
        Arrays.fill(rankArray, "empty");
        double[] distArray = new double[input.length];
        for (int i = 0; i < input.length; i++){
            distArray[i] = DissUtils.euclideanDist(dataPoint, input[i].getArray());
        }
        double[] copy = new double[input.length];
        System.arraycopy(distArray, 0, copy, 0, input.length);  //using arraycopy to make sure the distArray doesn't get messed up.
        Arrays.sort(copy);
        double thresh = copy[maxK-1];
        for (int j = 0; j < input.length; j++){
            if (distArray[j] <= thresh){
                //it's less than the threshold, so get the exact position
                int blob = 0;
                boolean found = false;
                while (!(found) && (blob < maxK)){
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
    
    private int stringCount(String[] array, String s){
        int ret = 0;
        for (int i = 0; i < k; i++){
            if (array[i].equalsIgnoreCase(s)){
                ret++;
            }
        }
        return ret;
    }     
    
    public String[] getClassNames(){
        return classes;
    }
}
