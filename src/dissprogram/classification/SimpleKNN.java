/*
 * SimpleKNN.java
 *
 * Created on April 2, 2007, 11:27 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.classification;

import dissprogram.DissUtils;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 *
 * @author jfc173
 */
public class SimpleKNN implements AbstractSupervisedClassifier{
   
    private String[] classes;
    private ClassedArray[] input;    
    private int k;
    
    /** Creates a new instance of SimpleKNN */
    public SimpleKNN() {
    }
    
    public void setTrainingData(ClassedArray[] array){
        input = array;
        classes = DissUtils.extractClassNames(input);
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
        String[] rankArray = neighborClasses(dataPoint);
        int[] stringCounts = new int[classes.length];
        for (int j = 0; j < classes.length; j++){
            stringCounts[j] = stringCount(rankArray, classes[j]);
        }
        double[] ret = new double[classes.length];
        for (int m = 0; m < classes.length; m++){
            ret[m] = (double) stringCounts[m] / (double) k;
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
    
    private int stringCount(String[] array, String s){
        int ret = 0;
        for (int i = 0; i < array.length; i++){
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
