/*
 * AbstractUnsupervisedClassifier.java
 *
 * Created on January 25, 2007, 11:07 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.classification;

/**
 *
 * @author jfc173
 */
public interface AbstractUnsupervisedClassifier {  
    
    public void setMatrix(double[][] array);    
    
    public void run();
    
    public int[] getClassArray();
    
}


