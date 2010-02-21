/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dissprogram.evidence;

import dissprogram.DissUtils;
import java.util.Arrays;

/**
 *
 * @author jconley
 */
public class StaticEvidenceCombiner {

    public final static int MODE = 0;
    public final static int AVERAGE = 1;
    public final static int SIMPLE_DS = 2;
    public final static int PROP_BEL_TRANS = 3;
    public final static int COMBINED_AVERAGE = 4;
    public final static int MODIFIED_AVERAGE = 5;  //NOT TESTED!!!
    public final static int TREE_PBT = 6;    
    
    public static double[] combineEvidence(double[][] inProbs, String[] inWinners, String[] classNames, int mode){
        DSCombine.setClassNames(classNames);
        double[] setProbs = new double[classNames.length + 1];
        //do something with the klasses and/or klassProbs!
        switch (mode){
            case MODE:
                setProbs = caseMode(inWinners, classNames);
                break;
            case AVERAGE:
                setProbs = caseAverage(inProbs, classNames);
                break;
            case SIMPLE_DS:
                setProbs = caseSimpleDS(inProbs, classNames);
                break;
            case PROP_BEL_TRANS:
                setProbs = casePropBelTrans(inProbs, classNames);
                break;
            case COMBINED_AVERAGE:
                setProbs = caseCombinedAverage(inProbs, classNames);
                break;  
            case MODIFIED_AVERAGE:
                setProbs = caseModifiedAverage(inProbs, classNames);
                break;
            case TREE_PBT:
                setProbs = caseTreePBT(inProbs, classNames);
                break;
            default:
                System.out.println("I shouldn't be here: mode is " + mode);
        }
                
        return setProbs;        
    }
    
    private static double[] caseMode(String[] klasses, String[] classNames){
        //determine the probabilities by the proportion of the points in the set with
        //the winner-take-all classification (the klasses array) in each class
        double[] setProbs = new double[classNames.length + 1];  //the + 1 is for unclassified points
        int[] counts = new int[setProbs.length];
        for (int i = 0; i < klasses.length; i++){
            int index = DissUtils.findIndex(classNames, klasses[i]);
            if (index == -1){
                //no class found, thus the point is unclassified.
                counts[counts.length-1]++;  
            } else {
                counts[index]++;
            }
        }                
        for (int j = 0; j < setProbs.length; j++){
            setProbs[j] = (double) counts[j] / (double) klasses.length;
        }      
        return setProbs;
    }    
    
    private static double[] caseAverage(double[][] klassProbs, String[] classNames){
        //determine the probabilities by the average over all the points in the set of
        //the probability that the points belong to each class (using the klassProbs array)
        BeliefFunction[] beliefs = new BeliefFunction[klassProbs.length];
        for (int i = 0; i < klassProbs.length; i++){
            beliefs[i] = new BeliefFunction(classNames);
            beliefs[i].addSingletons(klassProbs[i], classNames);
        }
        BeliefFunction average = DSCombine.average(beliefs);

        //I somehow have to get a double[] out of this.  Just get the singletons, since in
        //this case (but NOT the general case) all non-THETA non-zero masses belong to singletons?
        String[][] nonZeroCombined = average.getNonZeroMassHypos();
        double[] setProbs = new double[klassProbs[0].length + 1];  //the + 1 is for unclassified points
        for (int i = 0; i < nonZeroCombined.length; i++){
            int length = nonZeroCombined[i].length;
            if (length != 1){
                System.out.print("Whoa!  Length is not one");
                for (int j = 0; j < length; j++){
                    System.out.print(", " + nonZeroCombined[i][j]);
                }
                System.out.println();
            } else {
                String klass = nonZeroCombined[i][0];
                int index = DissUtils.findIndex(classNames, klass);
                if (index != -1){
                    setProbs[index] = average.getBelief(nonZeroCombined[i]);  //getBelief and getMass should be equivalent for singletons.
                } else if (klass.equalsIgnoreCase(BeliefFunction.THETA)){
                    setProbs[setProbs.length-1] = average.getMass(new String[]{BeliefFunction.THETA});  //need to use getMass b/c getBelief(THETA) = 1
                } else if (klass.equalsIgnoreCase(BeliefFunction.NULL)){
                    System.out.println("I have the feeling this shouldn't happen--NULL should not have a non-zero mass.");                            
                }
            }
        }
        return setProbs;
    }
    
    private static double[] caseSimpleDS(double[][] klassProbs, String[] classNames){
        //apply the Dempster-Shafer combination operator successively to each set of probabilities
        //in the klassProbs array.  This could have a problem if we get complete incompatibility.
        BeliefFunction belief = new BeliefFunction(classNames);
        belief.addSingletons(klassProbs[0], classNames);
        for (int i = 1; i < klassProbs.length; i++){
            try{
                BeliefFunction next = new BeliefFunction(classNames);
                next.addSingletons(klassProbs[i], classNames);
                BeliefFunction combined = DSCombine.combineShafer(belief, next);
                belief = combined;
            } catch (DSTotalConflictException dtce){
//                System.out.println("INCOMPATIBLE!!!");
                belief = new BeliefFunction(classNames);
                double[] nans = new double[classNames.length];
                Arrays.fill(nans, Double.NaN);
                belief.addSingletons(nans, classNames);
            }
        }
        //I somehow have to get a double[] out of this.  Just get the singletons, since in
        //this case (but NOT the general case) all non-THETA non-zero masses belong to singletons?
        String[][] nonZeroCombined = belief.getNonZeroMassHypos();
        double[] setProbs = new double[klassProbs[0].length + 1];  //the + 1 is for unclassified points
        for (int i = 0; i < nonZeroCombined.length; i++){
            int length = nonZeroCombined[i].length;
            if (length != 1){
                System.out.print("Whoa!  Length is not one");
                for (int j = 0; j < length; j++){
                    System.out.print(", " + nonZeroCombined[i][j]);
                }
                System.out.println();
            } else {
                String klass = nonZeroCombined[i][0];
                int index = DissUtils.findIndex(classNames, klass);
                if (index != -1){
                    setProbs[index] = belief.getBelief(nonZeroCombined[i]);  //getBelief and getMass should be equivalent for singletons.
                } else if (klass.equalsIgnoreCase(BeliefFunction.THETA)){
                    setProbs[setProbs.length-1] = belief.getMass(new String[]{BeliefFunction.THETA});  //need to use getMass b/c getBelief(THETA) = 1
                } else if (klass.equalsIgnoreCase(BeliefFunction.NULL)){
                    System.out.println("I have the feeling this shouldn't happen--NULL should not have a non-zero mass.");                            
                }
            }
        } 
        return setProbs;
    }
       
    private static double[] casePropBelTrans(double[][] klassProbs, String[] classNames){
        //apply the Proportional Belief Transfer combination operator (Anand, Bell & Hughes 1996)
        //successively to each set of probabilities in the klassProbs array.  
        //This could have a problem if we get complete incompatibility.
        BeliefFunction belief = new BeliefFunction(classNames);
        belief.addSingletons(klassProbs[0], classNames);
        for (int i = 1; i  < klassProbs.length; i++){
            BeliefFunction next = new BeliefFunction(classNames);
            next.addSingletons(klassProbs[i], classNames);
            BeliefFunction combined = DSCombine.combineProportionalBeliefTransfer(belief, next);
            belief = combined;
        }
        //I somehow have to get a double[] out of this.  Just get the singletons, since in
        //this case (maybe so, maybe not?) all non-THETA non-zero masses belong to singletons?
        String[][] nonZeroCombined = belief.getNonZeroMassHypos();
        double[] setProbs = new double[klassProbs[0].length + 1];  //the + 1 is for unclassified points
        for (int i = 0; i < nonZeroCombined.length; i++){
            int length = nonZeroCombined[i].length;
            if (length != 1){
                System.out.print("Whoa!  Length is not one");
                for (int j = 0; j < length; j++){
                    System.out.print(", " + nonZeroCombined[i][j]);
                }
                System.out.println();
            } else {
                String klass = nonZeroCombined[i][0];
                int index = DissUtils.findIndex(classNames, klass);
                if (index != -1){
                    setProbs[index] = belief.getBelief(nonZeroCombined[i]);  //getBelief and getMass should be equivalent for singletons.
                } else if (klass.equalsIgnoreCase(BeliefFunction.THETA)){
                    setProbs[setProbs.length-1] = belief.getMass(new String[]{BeliefFunction.THETA});  //need to use getMass b/c getBelief(THETA) = 1
                } else if (klass.equalsIgnoreCase(BeliefFunction.NULL)){
                    System.out.println("I have the feeling this shouldn't happen--NULL should not have a non-zero mass.");                            
                }
            }
        } 
        return setProbs;
    }
    
    private static double[] caseTreePBT(double[][] klassProbs, String[] classNames){
        //apply the Dempster-Shafer combination operator to each set of probabilities in the 
        //klassProbs array.  This could have a problem if we get complete incompatibility.
        //Instead of (((((1+2)+3)+4)+5)+...+n), which is the simple approach,
        //try (((1+2)+(3+4))+...+((n-3+n-2)+(n-1+n))), which I can do recursively.
        BeliefFunction combined = new BeliefFunction(classNames);
        if (klassProbs.length < 2){
            System.out.println("error!  I shouldn't be combining less than two belief functions");
        } else if (klassProbs.length == 2){
            BeliefFunction bf1 = new BeliefFunction(classNames);
            BeliefFunction bf2 = new BeliefFunction(classNames);
            bf1.addSingletons(klassProbs[0], classNames);
            bf2.addSingletons(klassProbs[1], classNames);
            combined = DSCombine.combineProportionalBeliefTransfer(bf1, bf2);            
        } else if (klassProbs.length == 3){
            BeliefFunction bf1 = new BeliefFunction(classNames);
            BeliefFunction bf2 = new BeliefFunction(classNames);
            BeliefFunction bf3 = new BeliefFunction(classNames);
            bf1.addSingletons(klassProbs[0], classNames);
            bf2.addSingletons(klassProbs[1], classNames);
            bf3.addSingletons(klassProbs[2], classNames);
            combined = DSCombine.combineProportionalBeliefTransfer(bf1, DSCombine.combineProportionalBeliefTransfer(bf2, bf3));              
        } else {
            int middle = (int) Math.round(klassProbs.length / 2);
            double[][] firstHalf = new double[middle][];
            double[][] secondHalf = new double[klassProbs.length - middle][];
            for (int i = 0; i < middle; i++){
                firstHalf[i] = klassProbs[i];
            }
            for (int i = middle; i < klassProbs.length; i++){
                secondHalf[i - middle] = klassProbs[i];
            }
            double[] first = caseTreePBT(firstHalf, classNames);
            double[] second = caseTreePBT(secondHalf, classNames);

            //need to remove the last item, THETA
            double[] trimmedFirst = new double[first.length-1];
            double[] trimmedSecond = new double[second.length-1];
            for (int i = 0; i < trimmedFirst.length; i++){
                trimmedFirst[i] = first[i];
            }
            for (int i = 0; i < trimmedSecond.length; i++){
                trimmedSecond[i] = second[i];
            }

            BeliefFunction bf1 = new BeliefFunction(classNames);
            BeliefFunction bf2 = new BeliefFunction(classNames);
            bf1.addSingletons(trimmedFirst, classNames);
            bf2.addSingletons(trimmedSecond, classNames);
            combined = DSCombine.combineProportionalBeliefTransfer(bf1, bf2);             
        }
        
        //I somehow have to get a double[] out of this.  Just get the singletons, since in
        //this case (but NOT the general case) all non-THETA non-zero masses belong to singletons?
        String[][] nonZeroCombined = combined.getNonZeroMassHypos();
        double[] setProbs = new double[klassProbs[0].length + 1];  //the + 1 is for unclassified points
        for (int i = 0; i < nonZeroCombined.length; i++){
            int length = nonZeroCombined[i].length;
            if (length != 1){
                System.out.print("Whoa!  Length is not one");
                for (int j = 0; j < length; j++){
                    System.out.print(", " + nonZeroCombined[i][j]);
                }
                System.out.println();
            } else {
                String klass = nonZeroCombined[i][0];
                int index = DissUtils.findIndex(classNames, klass);
                if (index != -1){
                    setProbs[index] = combined.getBelief(nonZeroCombined[i]);  //getBelief and getMass should be equivalent for singletons.
                } else if (klass.equalsIgnoreCase(BeliefFunction.THETA)){
                    setProbs[setProbs.length-1] = combined.getMass(new String[]{BeliefFunction.THETA});  //need to use getMass b/c getBelief(THETA) = 1
                } else if (klass.equalsIgnoreCase(BeliefFunction.NULL)){
                    System.out.println("I have the feeling this shouldn't happen--NULL should not have a non-zero mass.");                            
                }
            }
        } 
        return setProbs;
    }        
    
    private static double[] caseCombinedAverage(double[][] klassProbs, String[] classNames){
        //determine the probabilities by the average over all the points in the set of
        //the probability that the points belong to each class (using the klassProbs array)
        //and then combining the average with itself n-1 times (where n is the number of belief functions)
        BeliefFunction[] beliefs = new BeliefFunction[klassProbs.length];
        for (int i = 0; i < klassProbs.length; i++){
            beliefs[i] = new BeliefFunction(classNames);
            beliefs[i].addSingletons(klassProbs[i], classNames);
        }
        BeliefFunction average = DSCombine.combineAverageApproach(beliefs);

        //I somehow have to get a double[] out of this.  Just get the singletons, since in
        //this case (but NOT the general case) all non-THETA non-zero masses belong to singletons?
        String[][] nonZeroCombined = average.getNonZeroMassHypos();
        double[] setProbs = new double[klassProbs[0].length + 1];  //the + 1 is for unclassified points
        for (int i = 0; i < nonZeroCombined.length; i++){
            int length = nonZeroCombined[i].length;
            if (length != 1){
                System.out.print("Whoa!  Length is not one");
                for (int j = 0; j < length; j++){
                    System.out.print(", " + nonZeroCombined[i][j]);
                }
                System.out.println();
            } else {
                String klass = nonZeroCombined[i][0];
                int index = DissUtils.findIndex(classNames, klass);
                if (index != -1){
                    setProbs[index] = average.getBelief(nonZeroCombined[i]);  //getBelief and getMass should be equivalent for singletons.
                } else if (klass.equalsIgnoreCase(BeliefFunction.THETA)){
                    setProbs[setProbs.length-1] = average.getMass(new String[]{BeliefFunction.THETA});  //need to use getMass b/c getBelief(THETA) = 1
                } else if (klass.equalsIgnoreCase(BeliefFunction.NULL)){
                    System.out.println("I have the feeling this shouldn't happen--NULL should not have a non-zero mass.");                            
                }
            }
        }
        return setProbs;
    }
    
    private static double[] caseModifiedAverage(double[][] klassProbs, String[] classNames){
        //From Yong et al (2004) Decision Support Systems 38:489-493.
        BeliefFunction[] beliefs = new BeliefFunction[klassProbs.length];
        for (int i = 0; i < klassProbs.length; i++){
            beliefs[i] = new BeliefFunction(classNames);
            beliefs[i].addSingletons(klassProbs[i], classNames);
        }
        BeliefFunction average = DSCombine.combineModifiedAverageApproach(beliefs);

        //I somehow have to get a double[] out of this.  Just get the singletons, since in
        //this case (but NOT the general case) all non-THETA non-zero masses belong to singletons?
        String[][] nonZeroCombined = average.getNonZeroMassHypos();
        double[] setProbs = new double[klassProbs[0].length + 1];  //the + 1 is for unclassified points
        for (int i = 0; i < nonZeroCombined.length; i++){
            int length = nonZeroCombined[i].length;
            if (length != 1){
                System.out.print("Whoa!  Length is not one");
                for (int j = 0; j < length; j++){
                    System.out.print(", " + nonZeroCombined[i][j]);
                }
                System.out.println();
            } else {
                String klass = nonZeroCombined[i][0];
                int index = DissUtils.findIndex(classNames, klass);
                if (index != -1){
                    setProbs[index] = average.getBelief(nonZeroCombined[i]);  //getBelief and getMass should be equivalent for singletons.
                } else if (klass.equalsIgnoreCase(BeliefFunction.THETA)){
                    setProbs[setProbs.length-1] = average.getMass(new String[]{BeliefFunction.THETA});  //need to use getMass b/c getBelief(THETA) = 1
                } else if (klass.equalsIgnoreCase(BeliefFunction.NULL)){
                    System.out.println("I have the feeling this shouldn't happen--NULL should not have a non-zero mass.");                            
                }
            }
        } 
        return setProbs;
    }    
    
}
