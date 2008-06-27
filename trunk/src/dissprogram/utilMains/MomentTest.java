/*
 * MomentTest.java
 *
 * Created on February 9, 2007, 1:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.*;
import dissprogram.image.MomentGenerator;

/**
 *
 * @author jfc173
 */
public class MomentTest {
    
    /** Creates a new instance of MomentTest */
    public MomentTest() {
    }
    

    public static void main(String[] args) {
        float[][] raster = new float[][]{{0,0,0,0,0,0,0,0,0,0},
                                         {0,0,0,0,0,0,1,0,0,0},
                                         {0,0,0,0,1,1,1,0,1,0},
                                         {0,0,0,1,1,1,1,1,1,0},
                                         {0,1,1,1,1,1,1,1,1,1},
                                         {0,1,1,1,1,1,1,1,0,0},
                                         {0,0,1,1,1,1,1,0,0,0},
                                         {0,1,1,1,1,1,0,0,0,0},
                                         {0,0,0,0,1,1,0,0,0,0},
                                         {0,0,0,0,1,0,0,0,0,0}};
        MomentGenerator mg = new MomentGenerator();
        mg.setRaster(raster);
        System.out.println("m");
        System.out.println("0,0: " + mg.calculateMoment(0,0));
        System.out.println("0,1: " + mg.calculateMoment(0,1));
        System.out.println("0,2: " + mg.calculateMoment(0,2));
        System.out.println("0,3: " + mg.calculateMoment(0,3));
        System.out.println("1,0: " + mg.calculateMoment(1,0));
        System.out.println("1,1: " + mg.calculateMoment(1,1));
        System.out.println("1,2: " + mg.calculateMoment(1,2));
        System.out.println("2,0: " + mg.calculateMoment(2,0));
        System.out.println("2,1: " + mg.calculateMoment(2,1));
        System.out.println("3,0: " + mg.calculateMoment(3,0));
        
        System.out.println("mu");
        System.out.println("0,0: " + mg.calculateCentralMoment(0,0));
        System.out.println("0,1: " + mg.calculateCentralMoment(0,1));
        System.out.println("0,2: " + mg.calculateCentralMoment(0,2));
        System.out.println("0,3: " + mg.calculateCentralMoment(0,3));
        System.out.println("1,0: " + mg.calculateCentralMoment(1,0));
        System.out.println("1,1: " + mg.calculateCentralMoment(1,1));
        System.out.println("1,2: " + mg.calculateCentralMoment(1,2));
        System.out.println("2,0: " + mg.calculateCentralMoment(2,0));
        System.out.println("2,1: " + mg.calculateCentralMoment(2,1));
        System.out.println("3,0: " + mg.calculateCentralMoment(3,0));      
        
        System.out.println("eta");
        System.out.println("0,0: " + mg.calculateNormalizedCentralMoment(0,0));
        System.out.println("0,1: " + mg.calculateNormalizedCentralMoment(0,1));
        System.out.println("0,2: " + mg.calculateNormalizedCentralMoment(0,2));
        System.out.println("0,3: " + mg.calculateNormalizedCentralMoment(0,3));
        System.out.println("1,0: " + mg.calculateNormalizedCentralMoment(1,0));
        System.out.println("1,1: " + mg.calculateNormalizedCentralMoment(1,1));
        System.out.println("1,2: " + mg.calculateNormalizedCentralMoment(1,2));
        System.out.println("2,0: " + mg.calculateNormalizedCentralMoment(2,0));
        System.out.println("2,1: " + mg.calculateNormalizedCentralMoment(2,1));
        System.out.println("3,0: " + mg.calculateNormalizedCentralMoment(3,0));       
        
        double[] moments = mg.calculateMomentInvariants();
        for (int i = 0; i < moments.length; i++){
            System.out.println(moments[i]);
        }
    }    
    
}
