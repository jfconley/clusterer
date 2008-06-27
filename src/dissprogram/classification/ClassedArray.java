/*
 * ClassedArray.java
 *
 * Created on February 28, 2007, 12:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.classification;

/**
 *
 * @author jfc173
 */
public class ClassedArray {
    
    double[] array;
    String klass;
    
    /** Creates a new instance of ClassedArray */
    public ClassedArray() {
    }
    
    public void setArray(double[] d){
        array = d;
    }
    
    public void setKlass(String s){
        klass = s;
    }
    
    public double[] getArray(){
        return array;
    }
    
    public String getKlass(){
        return klass;
    }
    
}
