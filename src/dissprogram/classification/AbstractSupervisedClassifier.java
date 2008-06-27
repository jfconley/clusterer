/*
 * AbstractSupervisedClassifier.java
 *
 * Created on April 2, 2007, 10:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.classification;

/**
 *
 * @author jfc173
 */
public interface AbstractSupervisedClassifier {
    
    public void setTrainingData(ClassedArray[] array);
    
    public void train();    
    
    public String classify(double[] dataPoint);
    
    public double[] classifyProb(double[] dataPoint);
    
    public String[] getClassNames();
    
}
