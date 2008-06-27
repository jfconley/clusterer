/*
 * ClassifiedRaster.java
 *
 * Created on May 2, 2007, 2:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.image;

import dissprogram.evidence.BeliefFunction;

/**
 *
 * @author jfc173
 */
public class ClassifiedRaster {
    
    private float[][] raster;
//    private double[] probabilities;
    private BeliefFunction belief;
    private double[] zInvs;
    private String tempString;
    private double count = 0;
    
    /** Creates a new instance of classifiedCoverage */
    public ClassifiedRaster() {
    }
    
    public void setRaster(float[][] f){
        raster = f;
        count = 0;
        for (int i = 0; i < f.length; i++){
            for (int j = 0; j < f[i].length; j++){
                count = count + f[i][j];
            }
        }
    }
    
    public float[][] getRaster(){
        return raster;
    }
    
//    public void setNames(String[] s){
//        names = s;
//    }
//    
//    public void setProbabilities(double[] d){
//        belief.addSingletons(d, names);
//    }
    
    public void setBeliefFunction(BeliefFunction bf){
        belief = bf;
    }
    
    public BeliefFunction getBeliefFunction(){
        return belief;
    }
    
    public void setZInvs(double[] d){
        zInvs = d;        
    }
    
    public double[] getZInvs(){
        return zInvs;
    }
    
    public void setTempString(String s){
        tempString = s;
    }
    
    public String getTempString(){
        return tempString;
    }
    
    public boolean contains(ClassifiedRaster cr){
        //this is assuming binary 0-1 rasters with 0 as the background
        float[][] checkMe = cr.getRaster();        
        boolean found = false;
        boolean ret = false;
        int i = 0;
        int j = 0;
        while (!(found)){
            if (checkMe[i][j] == 1){
                ret = raster[i][j] == 1;
                found = true;
            }
            j++;
            if (j == checkMe[i].length){
                j = 0;
                i++;
            }
            if (i == checkMe.length){
                found = true;  //technically, no, we didn't find a 1 in checkMe, but we've exhausted all the locations, so there isn't a 1 in checkMe to find.
            }            
        }
        return ret;
    }
    
    public String toString(){
        return tempString + "; size: " + count + "; " + belief.toString();
    }        
    
    public String toFileString(){
        String ret = "" + zInvs[0];
        for (int i = 1; i < zInvs.length; i++){
            ret = ret + "," + zInvs[i];
        }
        return ret + "; " + this.toString();
    }
    
}
