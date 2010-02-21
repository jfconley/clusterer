/*
 * BeliefFunction.java
 *
 * Created on July 27, 2007, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.evidence;

import dissprogram.DissUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author z4x
 */
public class BeliefFunction {
    
    private Map map;    
    public static final String THETA = "THETA";
    public static final String NULL = "NULL";
//    public Vector singletons;
    public String[] classNames;
    private static double tolerance = 0.00001;
    
    /** Creates a new instance of BeliefFunction */
    public BeliefFunction(String[] names) {
        map = new HashMap();
//        singletons = new Vector();
        classNames = names;
//        System.out.print("creating a belief function with the following class names: ");
//        for (int i = 0; i < names.length; i++){
//            System.out.print(names[i] + ", ");
//        }
//        System.out.println();
    }
    
    public String[][] getNonZeroMassHypos(){
        int s = map.keySet().size();
        String[][] ret;
        if (getMass(new String[]{THETA}) != 0){
            ret = new String[s+1][];
            ret[s] = new String[]{THETA};
        } else {
            ret = new String[s][];
        }
        int index = 0;
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()){
            String[] next = (String[]) keys.next();
            ret[index] = next;
            index++;
        }
        return ret;
    }            
    
    public String[][] getPowerSet(){
//        String[] singletonArray = new String[singletons.size()];
//        for (int i = 0; i < singletons.size(); i++){
//            singletonArray[i] = (String) singletons.get(i);
//        }
//        String[][] subsets = properSubsets(singletonArray);
        String[][] subsets = properSubsets(classNames);
        String[][] ret = new String[subsets.length+1][];
        for (int i = 0; i < subsets.length; i++){
            ret[i] = subsets[i];
        }
        ret[subsets.length] = new String[]{THETA};
        return ret;
    }
    
    public void addMass(double d, String[] s){
        if ((s.length == 1) && (s[0].equalsIgnoreCase(THETA))){
            //do nothing--the value for THETA is what isn't included in anything else
        } else {
            if (isProperHypothesis(d, s)){
                if (haveThisKey(s)){
                    //this hypothesis already in the function
                    Double bigD = retrieveDouble(s, true);  //this method also removes the key and bigD from the map                
                    map.put(s, new Double(bigD.doubleValue() + d));
                } else {
                    //this hypothesis not in the function
                    map.put(s, new Double(d));         
//                    for (int i = 0; i < s.length; i++){
//                        if (!(singletons.contains(s[i]))){
//                            singletons.add(s[i]);
//                        }
//                    }
                }
            }
        }
    }
    
    public void addSingletons(double[] d, String[] s){
        if (d.length != s.length){
            System.out.println("Error--masses and classes (double[] and String[]) must be the same length");
        } else {
            for (int i = 0; i < d.length; i++){
                addMass(d[i], new String[]{s[i]});
            }
        }
    }
    
    public double getMass(String[] hypothesis){
        if ((hypothesis.length == 1) && (hypothesis[0] == THETA)){
            if (Math.abs(1 - sumMasses()) <= tolerance){
                return 0;
            } else {
                return 1 - sumMasses();
            }
        } else if (haveThisKey(hypothesis)){
            return retrieveDouble(hypothesis, false).doubleValue();
        } else {
            return 0;
        }
    }
    
    public double getBelief(String[] hypothesis){
        //belief is the mass of the hypothesis plus the sum of the masses for 
        //all non-null subsets of the hypothesis               
        if (hypothesis.length == 1){
            if (hypothesis[0].equalsIgnoreCase(THETA)){
                return 1;
            } else {
                return getMass(hypothesis);
            }
        } else {
            double ret = getMass(hypothesis);
            String[][] subsets = properSubsets(hypothesis);
            for (int i = 0; i < subsets.length; i++){
                ret = ret + getMass(subsets[i]);
            }            
            return ret;
        }
    }
    
    public double getPlausibility(String[] hypothesis){
        //the plausibility is  1 minus the sum of all hypotheses for which the intersection
        //with hypothesis is NULL
        double sum = 0;
        String[][] subsets = getPowerSet();
        for (int i = 0; i < subsets.length; i++){
            String[] intersection = DSCombine.intersect(hypothesis, subsets[i]);
            if ((intersection.length == 1) && (intersection[0].equalsIgnoreCase(NULL))){
                sum = sum + getMass(subsets[i]);
            }
        }
        return 1 - sum;
    }
    
    public double getUncertainty(String[] hypothesis){        
        return getPlausibility(hypothesis) - getBelief(hypothesis);
    }
    
    public void hackAroundPrecisionError(){
        //occasionally, because of precision errors, the sum of masses can end up at something like
        //1.0000000000536, which is really annoying.  So hack around this by dividing by the sum if this happens.
        double tolerance = 0.00000001;
        if (sumMasses() > 1 - tolerance){
            double sum = sumMasses();
            Iterator keys = map.keySet().iterator();
            while (keys.hasNext()){
                Object nextKey = keys.next();
                Double d = (Double) map.get(nextKey);
                Double newD = new Double(d.doubleValue()/sum);
                map.put(nextKey, newD);
            }
        }
    }
    
    public String toString(){
        Iterator keys = map.keySet().iterator();
        String ret = "";
        while (keys.hasNext()){
            String[] next = (String[]) keys.next();
            ret = ret + next[0];
            for (int i = 1; i < next.length; i++){
                ret = ret + "+" + next[i];
            }
            ret = ret + ": " + DissUtils.roundToThousandths(((Double) map.get(next)).doubleValue()) + ", ";
        }
        ret = ret + "THETA: " + DissUtils.roundToThousandths(getMass(new String[]{THETA})) + '\n';
        return ret;
    }
    
    private double sumMasses(){
        double sum = 0;
        Iterator it = map.keySet().iterator();
        while (it.hasNext()){
            Double d = (Double) map.get(it.next());
            sum = sum + d.doubleValue();
        }
        return sum;
    }    
    
    private boolean haveThisKey(String[] s){
        boolean ret = false;
        boolean[] found = new boolean[s.length];
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()){
            String[] next = (String[]) keys.next();
            Arrays.fill(found, false);  
            boolean foundAll = true;  
            //initializing foundAll as true may seem odd, but I want it such that any false 
            //in found gives false, so I want to && a bunch of things together, so 
            //I should initialize to true.
            if (s.length == next.length){
                //because of the length test, this will fail if a hypothesis appears more than once
                //but then again, the point of this method is to prevent a hypothesis from appearing
                //more than once...
                for (int i = 0; i < s.length; i++){
                    for (int j = 0; j < next.length; j++){
                        if (s[i].equalsIgnoreCase(next[j])){
                            found[i] = true;
                        }
                    }
                }
                for (int i = 0; i < found.length; i++){
                    foundAll = foundAll && found[i];
                }
            } else {
                foundAll = false;
            }
            ret = ret || foundAll;
        }
        return ret;        
    }
    
    private Double retrieveDouble(String[] key, boolean remove){
        Double ret = null;
        Object removeMe = null;
        boolean[] found = new boolean[key.length];
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()){
            String[] next = (String[]) keys.next();
            Arrays.fill(found, false);  
            boolean foundAll = true;  
            //initializing foundAll as true may seem odd, but I want it such that any false 
            //in found gives false, so I want to && a bunch of things together, so 
            //I should initialize to true.
            if (key.length == next.length){
                //because of the length test, this will fail if a hypothesis appears more than once
                //but then again, the point of this method is to prevent a hypothesis from appearing
                //more than once...
                for (int i = 0; i < key.length; i++){
                    for (int j = 0; j < next.length; j++){
                        if (key[i].equalsIgnoreCase(next[j])){
                            found[i] = true;
                        }
                    }
                }
                for (int i = 0; i < found.length; i++){
                    foundAll = foundAll && found[i];
                }
            }
            if (foundAll){
                ret = (Double) map.get(next);
                removeMe = next;
            }
        }
        if (remove && (removeMe != null)){
            map.remove(removeMe);
        }
        return ret;          
    }
    
    private boolean isProperHypothesis(double d, String[] s){
        if (DissUtils.contains(s, NULL)){
            System.out.println("Error--cannot have non-zero belief in the null set");
            return false;
        } else if ((d <= 0) || (d > 1)){
            if (Math.abs(d) > tolerance){
                //I'm tired of seeing errors for mass d=0.0
                System.out.println("Error--each hypothesis must have a mass in (0,1]--d=" + d);
            }
            return false;
        } else if ((DissUtils.contains(s, THETA)) && (s.length > 1)){
            System.out.println("Error--a hypothesis cannot contain everything (theta) and something else too");
            return false;
        } else if (sumMasses() + d > 1 + tolerance){
            System.out.println("Error--adding this mass will make the total mass > 1--d=" + d + " and sum=" + sumMasses());
            return false;
        } else {
            boolean ret = true;
            for (int i = 0; i < s.length; i++){
//                System.out.println("checking " + s[i] + " against the list");
                if (classNames == null){
                    System.out.println("We have a problem.");
                }
                if (!(DissUtils.contains(classNames, s[i]))){
                    System.out.println("Error--one of the singletons, " + s[i] + ", is not in the list of class names");
                    ret = false;
                }
            }                        
            return ret;
        }
    }  
    
    private String[][] properSubsets(String[] s){            
        Vector v = new Vector();
        for (int i = 1; i < s.length; i++){
            Vector temp = (Vector) v.clone();
            v = addSubsetsOfSize(s, i, temp);
        }
        String[][] subsets = new String[v.size()][];
        for (int k = 0; k < v.size(); k++){
            subsets[k] = (String[]) v.get(k);
        }
        return subsets;
    }
    
    private Vector addSubsetsOfSize(String[] s, int i, Vector v){
        if ((i < 1) || (i > s.length)){
            System.out.println("adding nothing--no possible subsets of this size: " + i);
        } else if (i == 1){
            for (int j = 0; j < s.length; j++){
                v.add(new String[]{s[j]});
            }
        } else {
            Vector smaller = (Vector) v.clone();
            //use the clone because adding something to a vector while its 
            //iterator is running could get exciting
            Iterator it = smaller.iterator();
            while (it.hasNext()){
                String[] next = (String[]) it.next();                
                if (next.length == i-1){  //add one to the arrays of length i-1
                    for (int k = 0; k < s.length; k++){                    
                        if (!(DissUtils.contains(next, s[k]))){
                            String[] addMe = new String[next.length + 1];
                            for (int l = 0; l < next.length; l++){
                                addMe[l] = next[l];
                            }
                            addMe[next.length] = s[k];
                            if (!(hasReordered(v, addMe))){
                                v.add(addMe);
                            }
                        }   //end if (!(contains...
                    }   //end for (int k...
                }   //end if (next.length...
            }  //end while...
        }                
        return v;
    }
    
    private boolean hasReordered(Vector v, String[] s){
        boolean done = false;
        int vIndex = 0;
        while ((!(done)) && (vIndex < v.size())){
            String[] vStrings = (String[]) v.get(vIndex);
            done = DissUtils.hasSameElements(vStrings, s);
            vIndex++;
        }
        return done;
    }
    
    private boolean isProperSubset(String[] inside, String[] outside){
        if (inside.length >= outside.length){
            return false;
        } else {
            boolean ret = true;
            for (int i = 0; i < inside.length; i++){
                if (!(DissUtils.contains(outside, inside[i]))){
                    ret = false;
                }
            }
            return ret;
        }        
    }
    
}
