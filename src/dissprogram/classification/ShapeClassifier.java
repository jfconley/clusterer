/*
 * ShapeClassifier.java
 *
 * Created on April 2, 2007, 10:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.classification;
import dissprogram.image.MomentGenerator;
/**
 *
 * @author jfc173
 */
public class ShapeClassifier {
    
    private float[][] raster;
    private AbstractSupervisedClassifier classifier;
    private MomentGenerator generator;
    private boolean[] include;
    private int numInvsUsed;
    private double[] means, stdevs, zInvs;
    
    /** Creates a new instance of ShapeClassifier */
    public ShapeClassifier() {
        generator = new MomentGenerator();
    }
    
    public void setIncludeArray(boolean[] b){
        include = b;
        numInvsUsed = 0;
        for (int i = 0; i < include.length; i++){
            if (include[i]){
                numInvsUsed++;
            }
        }
    }
    
    public void setRaster(float[][] arr){
        raster = arr;
    }
    
    public void setClassifier(AbstractSupervisedClassifier asc){
        classifier = asc;
    }
    
    public void setMeans(double[] in){
        means = in;
    }
    
    public void setStDevs(double[] in){
        stdevs = in;
    }
    
    public double[] getZInvs(){
        return zInvs;
    }
    
    public double[] classifyProb(){
        generator.setRaster(raster);
        zInvs = standardizeMe(generator.calculateMomentInvariants());      
        double[] useMe = trimInvArray(zInvs);
        return classifier.classifyProb(useMe);
    }
    
    public String classify(){
        generator.setRaster(raster);
        zInvs = standardizeMe(generator.calculateMomentInvariants());
        double[] useMe = trimInvArray(zInvs);
        return classifier.classify(useMe);        
    }
    
    public AbstractSupervisedClassifier getClassifier(){
        return classifier;
    }
    
    private double[] trimInvArray(double[] invs){
        double[] ret = new double[numInvsUsed];
        //may want to do an error check to ensure invs.length == include.length
        int index = 0;
        for (int i = 0; i < invs.length; i++){
            if (include[i]){
                if (Double.isInfinite(invs[i])){
                    if (invs[i] < 0){
                        ret[index] = -50;  //since the values have been logarithmed by now, +-50 is really big
                    } else {
                        ret[index] = 50;
                    }
                } else {                
                    ret[index] = invs[i];
                }
                index++;
            }
        }
        return ret;
    }
    
    public double[] standardizeMe(double[] invs){
        //the data being compared against is Z-score(ln(abs(inv))).  
        //I have the means and stdevs for the Z-scores, so apply them 
        //to the ln(abs()) of the detected invariants
        double[] ret = new double[invs.length];
        for (int i = 0; i < invs.length; i++){
            ret[i] = (Math.log(Math.abs(invs[i])) - means[i])/stdevs[i];
        }
//        System.out.print(invs[0]);
//        for (int i = 1; i < invs.length; i++){
//            System.out.print("," + invs[i]);
//        }
//        System.out.print("-->");
//        
//        System.out.print(ret[0]);
//        for (int i = 1; i < ret.length; i++){
//            System.out.print("," + ret[i]);
//        }
//        System.out.println();
        
        return ret;
    }
    
    public String[] getClassNames(){
        return classifier.getClassNames();
    }        
    
}
