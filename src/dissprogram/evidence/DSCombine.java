/*
 * DSCombine.java
 *
 * Created on April 30, 2007, 3:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.evidence;

import dissprogram.DissUtils;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author jfc173
 */
public class DSCombine {
    
    private static String[] singletons;
    
    /** Creates a new instance of DSCombine */
    public DSCombine() {
    }
    
    public static void setClassNames(String[] names){
        singletons = names;
    }
    
    public static double[] combineSimple(double[] mass1, double[] mass2) throws DSTotalConflictException{
        if (mass1.length != mass2.length){
            throw new RuntimeException("Two mass functions should have the same length.");
        }
        
        double[] ret = new double[mass1.length];
        
        //calculate the amount of disagreement sum(mass1[a]*mass2[b] over all (a!=b))
        
        double sum = 0;
        for (int a = 0; a < mass1.length; a++){
            for (int b = 0; b < mass2.length; b++){
                if (a != b){
                    sum = sum + mass1[a] * mass2[b];
                }
            }
        }
        
        if (sum >= 1){
            throw new DSTotalConflictException();
        }
        
        //calculate each ret[c]
        for (int c = 0; c < mass1.length; c++){
            ret[c] = (mass1[c] * mass2[c])/(1 - sum);
            
        }
        
        //because of precision issues, the sum over all c of ret[c] could be > 1.
        //As these precision errors propagate, this could be a major problem.
        double probSum = 0;
        for (int c = 0; c < ret.length; c++){
            probSum = probSum + ret[c];
        }
        if (probSum > 1){
            for (int c = 0; c < ret.length; c++){
                ret[c] = ret[c]/probSum;
            }
        }
        
        return ret;
    }
    
    public static BeliefFunction combineShafer(BeliefFunction bf1, BeliefFunction bf2) throws DSTotalConflictException{
//        System.out.println("Combining using standard Dempster-Shafer combination operator");
//        System.out.println("In are " + '\n' + bf1 + " and " + '\n' + bf2);
        if (Double.isNaN(bf1.getMass(new String[]{BeliefFunction.THETA})) || 
            Double.isNaN(bf2.getMass(new String[]{BeliefFunction.THETA}))){
            //this is the case when something in bf1's history or bf2's history got a DSTotalConflictException
            //keep pushing a bunch of NaN's on.
            BeliefFunction ret = new BeliefFunction(singletons);
            double[] nans = new double[singletons.length];
            Arrays.fill(nans, Double.NaN);
            ret.addSingletons(nans, singletons);  
            return ret;
        }
                
        String[][] powerSet1 = bf1.getPowerSet();
        String[][] powerSet2 = bf2.getPowerSet();
        
        //calculate the disagreement (kappa)
        double kappa = 0;
        for (int i = 0; i < powerSet1.length; i++){
            for (int j = 0; j < powerSet2.length; j++){
                String[] intersection = intersect(powerSet1[i], powerSet2[j]);
                if ((intersection.length == 1) && (intersection[0].equalsIgnoreCase(BeliefFunction.NULL))){
                    kappa = kappa + (bf1.getMass(powerSet1[i]) * bf2.getMass(powerSet2[j]));
                }
            }
        }
        
        if (kappa >= 1){
            throw new DSTotalConflictException();
        }        
        
        BeliefFunction combined = new BeliefFunction(singletons);
        String[][] hypotheses1 = bf1.getNonZeroMassHypos();
        String[][] hypotheses2 = bf2.getNonZeroMassHypos();
        
        for (int i = 0; i < hypotheses1.length; i++){
            for (int j = 0; j < hypotheses2.length; j++){
                String[] intersection = intersect(hypotheses1[i], hypotheses2[j]);
                if (!((intersection.length == 1) && (intersection[0].equalsIgnoreCase(BeliefFunction.NULL)))){
                    //non-null intersection
                    double top = roundMe(bf1.getMass(hypotheses1[i]) * bf2.getMass(hypotheses2[j]));
                    double bottom = roundMe(1 - kappa);
                    double mass = top/bottom;
                    if ((mass > 1) && (mass < 1.0000001)){
                        mass = 1;
                    }
                    combined.addMass(mass, intersection);
                }
            }
        }
        
        combined.hackAroundPrecisionError();
//        System.out.println("out is " + '\n' + combined);
//        System.out.println("---------------------------------------------------------------------------");
        return combined;
    }
    
    public static BeliefFunction combineProportionalBeliefTransfer(BeliefFunction bf1, BeliefFunction bf2){
//        System.out.println("Proportional Belief Transfer (Anand et al)");
//        System.out.println("In are " + '\n' + bf1 + " and " + '\n' + bf2);

        //Proportional Belief Transfer 
        //Anand, Bell & Hughes (1996), Data & Knowl. Eng., vol. 18 pp. 212-213.
        //(1) in ABH is mass(NULL) = 0, which is already ensured by BeliefFunction
        //(2) in ABH is the average of two parts, a mr(Bij) sum and a mc(Cji) sum.
        //translating terms, mf1 is mass function m1(B) in ABH, mf2 is mass function m2(C), 
        //mass function m(A) is ret, n is nonZero1, m is nonZero2.  I think all else is the same.
        BeliefFunction ret = new BeliefFunction(singletons);
        double mrBijSum = 0;
        double mcCjiSum = 0;        
        String[][] nonZero1 = bf1.getNonZeroMassHypos();
        String[][] nonZero2 = bf2.getNonZeroMassHypos();
        
        double[] internalSumsFor_mrBij = new double[nonZero1.length];
        double[] internalSumsFor_mcCji = new double[nonZero2.length];
        
        //this combines the calculation of the internal sums in mr(Bij) and mc(Cji).
        //the notation of i and k is from the mr(Bij) sum.  For the other sum, substitute k for i and j for k.        
        for (int i = 0; i < nonZero1.length; i++){
            double internalSum = 0;
            for (int k = 0; k < nonZero2.length; k++){
                String[] intersection = intersect(nonZero1[i], nonZero2[k]);
                if (!((intersection.length == 1) && (intersection[0].equalsIgnoreCase(BeliefFunction.NULL)))){
                    //non-NULL intersection
                    internalSumsFor_mrBij[i] = internalSumsFor_mrBij[i] + bf2.getMass(nonZero2[k]);
                    internalSumsFor_mcCji[k] = internalSumsFor_mcCji[k] + bf1.getMass(nonZero1[i]);
                }
            }  
        }
        
        //make the calculations of (2) at the top of p. 213
        //the way BeliefFunction is structured, I can add mass incrementally, 
        //so I won't explicitly calculate the sums here, but add it to ret.
        for (int i = 0; i < nonZero1.length; i++){
            for (int j = 0; j < nonZero2.length; j++){
                //for each Bi and Cj, find the intersection, and add to the intersection's mass (if it isn't NULL)
                //1/2 * (mrBij + mcCji)                
                String[] intersection = intersect(nonZero1[i], nonZero2[j]);
                if (!((intersection.length == 1) && (intersection[0].equalsIgnoreCase(BeliefFunction.NULL)))){
                    //non-NULL intersection
                    double mrBij = 0;
                    if (internalSumsFor_mrBij[i] == 0){
                        //this should be the case when Bi intersect Ck is NULL for all k
                        mrBij = bf1.getMass(nonZero1[i]);
                    } else {
                        mrBij = bf1.getMass(nonZero1[i]) * (bf2.getMass(nonZero2[j]) / internalSumsFor_mrBij[i]);
                    }
                    
                    double mcCji = 0;
                    if (internalSumsFor_mcCji[j] == 0){
                        //again, this should be the case when Cj intersect Bk is NULL for all k
                        mcCji = bf2.getMass(nonZero2[j]);
                    } else {
                        mcCji = bf2.getMass(nonZero2[j]) * (bf1.getMass(nonZero1[i]) / internalSumsFor_mcCji[j]);
                    }
                    
                    //I've got my mrBij and my mcCji, so now add the appropriate amount to the appropriate mass
                    ret.addMass(0.5 * (mrBij + mcCji), intersection);
                    
                }  //end if (!((intersection.length...
            }  //end for (int j...
        }  //end for (int i...
        
        //these next loops are not in ABH article--the cases when Bi intersect Ck is NULL for all k
        //and when Cj intersect Bk is NULL for all k cannot get activated in the above loop--that code
        //only executes for non-NULL intersections (which is how I read the formulas at the top of p. 213 in ABH
        //So, to make use of this evidence, and follow the "more acceptable result" on p. 212 of ABH,
        //I'm adding the mass as it would be if the above code could get activated.  Keep in mind that
        //whenever internalSumsFor_mrBij[i] == 0, mcCji == 0, and similarly internalSumsFor_mcCji[i] == 0 ==> mrBij == 0
        for (int i = 0; i < internalSumsFor_mrBij.length; i++){
            if (internalSumsFor_mrBij[i] == 0){
                ret.addMass(0.5 * bf1.getMass(nonZero1[i]), nonZero1[i]);
            }
        }
        for (int i = 0; i < internalSumsFor_mcCji.length; i++){
            if (internalSumsFor_mcCji[i] == 0){
                ret.addMass(0.5 * bf2.getMass(nonZero2[i]), nonZero2[i]);
            }
        }        
        
        ret.hackAroundPrecisionError();
//        System.out.println("out is " + '\n' + ret);
//        System.out.println("--------------------------------------------------------------------------");
        
        return ret;
    }
    
    public static BeliefFunction average(BeliefFunction[] beliefs){
//        System.out.println("Combining using a simple average");
//        System.out.println("in are");
//        for (int i = 0; i < beliefs.length; i++){
//            System.out.println(beliefs[i]);
//            System.out.println("- - - - - - - - - - - - - - - - - - - - - - -");
//        }
        
        //this is the average approach described Murphy (2000) Decision Support Systems 29:1-9
        BeliefFunction average = new BeliefFunction(singletons);
        Vector v = new Vector();
        for (int i = 0; i < beliefs.length; i++){
            String[][] nonZeroI = beliefs[i].getNonZeroMassHypos();
            for (int j = 0; j < nonZeroI.length; j++){
                if (!(vectorHasThis(v, nonZeroI[j]))){
                    v.add(nonZeroI[j]);
                }
            }
        }
        
        Iterator it = v.iterator();
        while (it.hasNext()){
            String[] hypo = (String[]) it.next();
            double sum = 0;
            for (int i = 0; i < beliefs.length; i++){
                sum = sum + beliefs[i].getMass(hypo);
            }
            average.addMass(sum / beliefs.length, hypo);
        }        
        average.hackAroundPrecisionError();
//        System.out.println("out is " + '\n' + average);
//        System.out.println("-------------------------------------------------------------------");
        return average;
    }
    
    public static BeliefFunction combineAverageApproach(BeliefFunction[] beliefs){
//        System.out.println("Combining using a combine average approach (Murphy 2000)");
//        System.out.println("in are");
//        for (int i = 0; i < beliefs.length; i++){
//            System.out.println(beliefs[i]);
//            System.out.println("- - - - - - - - - - - - - - - - - - - - - - -");
//        }        
        
        //this is the combined average approach also from Murphy (2000) Decision Support Systems 29:1-9
        //this is described in section 3.3 and is the last line in table 3.
        BeliefFunction ret = new BeliefFunction(singletons);
        
        //first, find the average.
        BeliefFunction average = average(beliefs);
        
        //then DS-combine it with itself beliefs.length-1 times
        try{
            ret = average;
            for (int i = 0; i < beliefs.length-1; i++){
                ret = combineShafer(ret, average);
            }                
        } catch (DSTotalConflictException dtce){
            System.out.println("This shouldn't happen--how can there be total conflict when iteratively combining identical functions!");
            System.out.println(average);
        }        
//        System.out.println("out is " + '\n' + ret);
//        System.out.println("-------------------------------------------------------------------");
        return ret;
    }
    
    public static BeliefFunction combineModifiedAverageApproach(BeliefFunction[] beliefs){
//        System.out.println("Combining using a modified average approach (Yong et al 2004)");
//        System.out.println("in are");
//        for (int i = 0; i < beliefs.length; i++){
//            System.out.println(beliefs[i]);
//            System.out.println("- - - - - - - - - - - - - - - - - - - - - - -");
//        }           
        
        //this is the approach in Yong et al. (2004) Decision Support Systems 38:489-493.
        //I first need the credibility of each belief function, which in turn requires the 
        //support for each belief function
        
        double supportSum = 0;
        double[] supports = new double[beliefs.length];
        double[] credibilities = new double[beliefs.length];
        
        for (int i = 0; i < beliefs.length; i++){
            double supportI = 0;
            for (int j = 0; j < beliefs.length; j++){
                if (i != j){
                    //the distance is in Jousselme et al. (2001) Information Fusion 2:91-101.
                    //equation 16 on p. 95
                    double scalar_i_i = scalarProduct(beliefs[i], beliefs[i]);
                    double scalar_j_j = scalarProduct(beliefs[j], beliefs[j]);
                    double scalar_i_j = scalarProduct(beliefs[i], beliefs[j]);
                    double temp = 0.5*(scalar_i_i + scalar_j_j - 2 * scalar_i_j);
                    if ((temp < 0) && (temp > -0.00001)){
                        temp = 0;
                    }
                    double dist_i_j = Math.sqrt(temp);  
                    
                    supportI = supportI + 1 - dist_i_j;                     
//                    if (Double.isNaN(dist_i_j)){
//                        System.out.println("I've got a NaN!");  //really, just stop for the debugger!
//                    }                      
                }              
            }
            supports[i] = supportI;
            supportSum = supportSum + supportI;
            
        }
        for (int i = 0; i < beliefs.length; i++){
            credibilities[i] = supports[i] / supportSum;
        }        
        
        //then compute the average weighted by the credibilities.
        BeliefFunction average = new BeliefFunction(singletons);
        Vector v = new Vector();
        for (int i = 0; i < beliefs.length; i++){
            String[][] nonZeroI = beliefs[i].getNonZeroMassHypos();
            for (int j = 0; j < nonZeroI.length; j++){
                if (!(v.contains(nonZeroI[j]))){
                    v.add(nonZeroI[j]);
                }
            }
        }
        
        Iterator it = v.iterator();
        while (it.hasNext()){
            String[] hypo = (String[]) it.next();
            double sum = 0;
            for (int i = 0; i < beliefs.length; i++){
                sum = sum + credibilities[i] * beliefs[i].getMass(hypo);
            }
            average.addMass(sum / beliefs.length, hypo);           
        }                   
        
        //finally, combine the weighted average with itself n-1 times as in combineAverageApproach
        BeliefFunction ret = new BeliefFunction(singletons);
        try{
            ret = average;
            for (int i = 0; i < beliefs.length-1; i++){
                ret = combineShafer(ret, average);
            }                
        } catch (DSTotalConflictException dtce){
            System.out.println("This shouldn't happen--how can there be total conflict when iteratively combining identical functions!");
            System.out.println(average);
        }    
        
//        System.out.println("out is " + ret);
//        System.out.println("-------------------------------------------------------------------");        
        
        return ret;   
    }
    
    public static String[] intersect(String[] hypo1, String[] hypo2){
        if ((hypo1.length == 1) && (hypo1[0].equalsIgnoreCase(BeliefFunction.THETA))){
            return hypo2;
        }
        if ((hypo2.length == 1) && (hypo2[0].equalsIgnoreCase(BeliefFunction.THETA))){
            return hypo1;
        }    
        Vector v = new Vector();        
        for (int i = 0; i < hypo1.length; i++){
            if (DissUtils.contains(hypo2, hypo1[i])){
                v.add(hypo1[i]);
            }
        }
        if (v.size() == 0){
            return new String[]{BeliefFunction.NULL};
        } else {
            String[] ret = new String[v.size()];
            for (int i = 0; i < v.size(); i++){
                ret[i] = (String) v.get(i);
            }
            return ret;
        }
    }
    
    public static String[] union(String[] hypo1, String[] hypo2){
        if ((hypo1.length == 1) && (hypo1[0].equalsIgnoreCase(BeliefFunction.THETA))){
            return hypo1;
        }
        if ((hypo2.length == 1) && (hypo2[0].equalsIgnoreCase(BeliefFunction.THETA))){
            return hypo2;
        }    
        if ((hypo1.length == 1) && (hypo1[0].equalsIgnoreCase(BeliefFunction.NULL))){
            return hypo2;
        }
        if ((hypo2.length == 1) && (hypo2[0].equalsIgnoreCase(BeliefFunction.NULL))){
            return hypo1;
        }            
        Vector v = new Vector();        
        for (int i = 0; i < hypo1.length; i++){
            v.add(hypo1[i]);
        }
        for (int i = 0; i < hypo2.length; i++){
            if (!(DissUtils.contains(hypo1, hypo2[i]))){
                v.add(hypo2[i]);
            }
        }        
        if (v.size() == 0){
            return new String[]{BeliefFunction.NULL};
        } else if (v.size() == singletons.length){
            return new String[]{BeliefFunction.THETA};
        } else {
            String[] ret = new String[v.size()];
            for (int i = 0; i < v.size(); i++){
                ret[i] = (String) v.get(i);
            }
            return ret;
        }
    }    
    
    private static boolean vectorHasThis(Vector v, String[] s){
        boolean ret = false;
        boolean[] found = new boolean[s.length];
        Iterator keys = v.iterator();
        while (keys.hasNext()){
            String[] next = (String[]) keys.next();
            java.util.Arrays.fill(found, false);  
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
    
    private static double roundMe(double d){
        //yet another attempt to quash precision-error bugs
        //round to nearest 1E-16 because the largest ulps (level of precision) encountered are xE-16.
        double temp = d * 1E16;
        temp = Math.round(temp);
        return temp * 1E-16;
        
    }
    
    private static double scalarProduct(BeliefFunction bf1, BeliefFunction bf2){
        //As defined in equation 17 of Jousselme et al. (see combineModifiedAverageApproach for reference)
        double ret = 0;
        String[][] hypos1 = bf1.getNonZeroMassHypos();
        String[][] hypos2 = bf2.getNonZeroMassHypos();
        for (int i = 0; i < hypos1.length; i++){
            for (int j = 0; j < hypos2.length; j++){
                //all parts of the double sum in the article equation that are not covered
                //by these two loops are zero.
                String[] hypo1 = hypos1[i];
                String[] hypo2 = hypos2[j];                
                double mass1 = bf1.getMass(hypo1);
                double mass2 = bf2.getMass(hypo2);
                int intersectionLength;
                int unionLength;
                String[] intersection = intersect(hypo1, hypo2);
                if ((intersection.length == 1) && (intersection[0].equalsIgnoreCase(BeliefFunction.NULL))){
                    intersectionLength = 0;
                } else if ((intersection.length == 1) && (intersection[0].equalsIgnoreCase(BeliefFunction.THETA))){
                    intersectionLength = singletons.length;
                } else {
                    intersectionLength = intersection.length;
                }
                String[] union = union(hypo1, hypo2);
                if ((union.length == 1) && (union[0].equalsIgnoreCase(BeliefFunction.NULL))){
                    unionLength = 0;
                } else if ((union.length == 1) && (union[0].equalsIgnoreCase(BeliefFunction.THETA))){
                    unionLength = singletons.length;
                } else {
                    unionLength = union.length;
                } 
                double fraction = (double) intersectionLength / (double) unionLength;
                ret = ret + mass1 * mass2 * fraction;
            }
        }
                
        return ret;
    }
    
}
