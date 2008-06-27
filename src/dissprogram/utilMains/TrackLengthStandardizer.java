/*
 * TrackLengthStandardizer.java
 *
 * Created on July 26, 2007, 1:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

/**
 *
 * @author z4x
 */
public class TrackLengthStandardizer {
    
    /** Creates a new instance of TrackLengthStandardizer */
    public TrackLengthStandardizer() {
    }
    
    public static void main(String[] args){
        int[] windLengths = new int[]{461, 445, 470, 430, 462, 443, 421, 477, 468, 438};  //wind (finished 0-9)       
        int[] roadLengths = new int[]{597, 537, 431, 467, 461, 391, 440, 467, 461, 589};  //road (finished 0-9)
        int[] infLengths = new int[]{754, 1059, 886, 832, 903, 883, 830, 805, 832, 982};  //infect (finished 0-9)
        
        int finalSize = 40;                
        
        for (int i = 0; i < 10; i++){
            double infInterval = (double) infLengths[i] / (double) finalSize;
            System.out.print("inf " + i);
            for (int j = 0; j < finalSize; j++){
                System.out.print("," + (int) Math.round(j * infInterval));
            }            
            System.out.println();
            
            double roadInterval = (double) roadLengths[i] / (double) finalSize;
            System.out.print("road " + i);
            for (int j = 0; j < finalSize; j++){
                System.out.print("," + (int) Math.round(j * roadInterval));
            }
            System.out.println();
            
            double windInterval = (double) windLengths[i] / (double) finalSize;
            System.out.print("wind " + i);
            for (int j = 0; j < finalSize; j++){
                System.out.print("," + (int) Math.round(j * windInterval));
            }
            System.out.println();           
            System.out.println();
        }
 
    }
    
}
