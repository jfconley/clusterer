/*
 * MomentGenerator.java
 *
 * Created on January 16, 2007, 2:31 PM
 *
 * The math parts of this are derived from the following article:
 * Tien C. Hsia (1981).  "A Note in Invariant Moments in Image Processing."  IEEE Transactions on Systems, Man, and Cybernetics 11(12):831-834.
 *
 */

package dissprogram.image;

import org.geotools.coverage.grid.GridCoverage2D;

/**
 *
 * @author jfc173
 */
public class MomentGenerator {
    
    GridCoverage2D image;
    double height, width;
    float[][] raster;
    double xBar, yBar;
    double[] invariants = new double[7];
    
    /** Creates a new instance of MomentGenerator */
    public MomentGenerator() {
    }
    
    public void setImage(GridCoverage2D gc){
        image = gc;
        height = gc.getEnvelope2D().getHeight();
        width = gc.getEnvelope2D().getWidth();
        System.out.println("width = " + width + " and height = " + height);
        //TODO: get the raster back out in float[][] form.
        
    }
    
    public void setRaster(float[][] f){
        raster = f;
        xBar = calculateMoment(1, 0) / calculateMoment(0, 0);
        yBar = calculateMoment(0, 1) / calculateMoment(0, 0);
    }
    
    public double calculateMoment(int p, int q){
        double sum = 0;
        for (int x = 0; x < raster.length; x++){
            for (int y = 0; y < raster[0].length; y++){
                sum = sum + Math.pow(x, p) * Math.pow(y, q) * raster[x][y];
            }
        }
        return sum;
    }
    
    public double calculateCentralMoment(int p, int q){
        double sum = 0;
        for (int x = 0; x < raster.length; x++){
            for (int y = 0; y < raster[0].length; y++){
                sum = sum + Math.pow((x - xBar), p) * Math.pow((y - yBar), q) * raster[x][y];
            }
        }
        return sum;        
    }
    
    public double calculateNormalizedCentralMoment(int p, int q){
        double mu_p_q = calculateCentralMoment(p, q);
        double m_0_0 = calculateMoment(0, 0);
        double gamma = ((p + q)/2) + 1;
        return mu_p_q / Math.pow(m_0_0, gamma);
    }
    
    public double[] calculateMomentInvariants(){
        double eta_2_0 = calculateNormalizedCentralMoment(2, 0);
        double eta_0_2 = calculateNormalizedCentralMoment(0, 2);
        double eta_1_1 = calculateNormalizedCentralMoment(1, 1);
        double eta_3_0 = calculateNormalizedCentralMoment(3, 0);
        double eta_0_3 = calculateNormalizedCentralMoment(0, 3);
        double eta_1_2 = calculateNormalizedCentralMoment(1, 2);
        double eta_2_1 = calculateNormalizedCentralMoment(2, 1);
        
        invariants[0] = eta_2_0 + eta_0_2;
        invariants[1] = Math.pow(eta_2_0 - eta_0_2, 2) + 4 * Math.pow(eta_1_1, 2);
        invariants[2] = Math.pow(eta_3_0 - 3 * eta_1_2, 2) + Math.pow(3 * eta_2_1 - eta_0_3, 2);
        invariants[3] = Math.pow(eta_3_0 + eta_1_2, 2) + Math.pow(eta_2_1 + eta_0_3, 2);
        invariants[4] = (eta_3_0 - 3 * eta_1_2) * (eta_3_0 + eta_1_2) * (Math.pow(eta_3_0 + eta_1_2, 2) - 3 * Math.pow(eta_2_1 + eta_0_3, 2)) + 
                        (3 * eta_2_1 - eta_0_3) * (eta_2_1 + eta_0_3) * (3 * Math.pow(eta_3_0 + eta_1_2, 2) - Math.pow(eta_2_1 + eta_0_3, 2));
        invariants[5] = (eta_2_0 - eta_0_2) * (Math.pow(eta_3_0 + eta_1_2, 2) - Math.pow(eta_2_1 + eta_0_3, 2)) +
                        4 * eta_1_1 * (eta_3_0 + eta_1_2) * (eta_2_1 + eta_0_3);
        invariants[6] = (3 * eta_2_1 - eta_0_3) * (eta_3_0 + eta_1_2) * (Math.pow(eta_3_0 + eta_1_2, 2) - 3 * Math.pow(eta_2_1 + eta_0_3, 2)) -
                        (eta_3_0 - 3 * eta_1_2) * (eta_2_1 + eta_0_3) * (3 * Math.pow(eta_3_0 + eta_1_2, 2) - Math.pow(eta_2_1 + eta_0_3, 2));
//        System.out.println("0: " + invariants[0] + ", 1: " + invariants[1] + ", 2: " + invariants[2] + ", 3: " + invariants[3] + ", 4: " + invariants[4] + ", 5: " + invariants[5] + ", 6: " + invariants[6]);
        return invariants;
    }
    
    public double[] calculateLiInvariants(){
        double mu_2_0 = calculateNormalizedCentralMoment(2, 0);
        double mu_0_2 = calculateNormalizedCentralMoment(0, 2);
        double mu_1_1 = calculateNormalizedCentralMoment(1, 1);
        double mu_3_0 = calculateNormalizedCentralMoment(3, 0);
        double mu_0_3 = calculateNormalizedCentralMoment(0, 3);
        double mu_1_2 = calculateNormalizedCentralMoment(1, 2);
        double mu_2_1 = calculateNormalizedCentralMoment(2, 1);
        double mu_4_0 = calculateNormalizedCentralMoment(4, 0);
        double mu_0_4 = calculateNormalizedCentralMoment(0, 4);
        double mu_1_3 = calculateNormalizedCentralMoment(1, 3);
        double mu_3_1 = calculateNormalizedCentralMoment(3, 1);
        double mu_2_2 = calculateNormalizedCentralMoment(2, 2);
        
        double mu_0_5 = calculateNormalizedCentralMoment(0, 5);
        double mu_1_4 = calculateNormalizedCentralMoment(1, 4);
        double mu_2_3 = calculateNormalizedCentralMoment(2, 3);
        double mu_3_2 = calculateNormalizedCentralMoment(3, 2);
        double mu_4_1 = calculateNormalizedCentralMoment(4, 1);
        double mu_5_0 = calculateNormalizedCentralMoment(5, 0);
        
        double mu_0_6 = calculateNormalizedCentralMoment(0, 6);
        double mu_1_5 = calculateNormalizedCentralMoment(1, 5);
        double mu_2_4 = calculateNormalizedCentralMoment(2, 4);
        double mu_3_3 = calculateNormalizedCentralMoment(3, 3);
        double mu_4_2 = calculateNormalizedCentralMoment(4, 2);
        double mu_5_1 = calculateNormalizedCentralMoment(5, 1);
        double mu_6_0 = calculateNormalizedCentralMoment(6, 0);
        
        double mu_0_7 = calculateNormalizedCentralMoment(0, 7);
        double mu_1_6 = calculateNormalizedCentralMoment(1, 6);
        double mu_2_5 = calculateNormalizedCentralMoment(2, 5);
        double mu_3_4 = calculateNormalizedCentralMoment(3, 4);
        double mu_4_3 = calculateNormalizedCentralMoment(4, 3);
        double mu_5_2 = calculateNormalizedCentralMoment(5, 2);
        double mu_6_1 = calculateNormalizedCentralMoment(6, 1);
        double mu_7_0 = calculateNormalizedCentralMoment(7, 0);
        
        double mu_0_8 = calculateNormalizedCentralMoment(0, 8);
        double mu_1_7 = calculateNormalizedCentralMoment(1, 7);
        double mu_2_6 = calculateNormalizedCentralMoment(2, 6);
        double mu_3_5 = calculateNormalizedCentralMoment(3, 5);
        double mu_4_4 = calculateNormalizedCentralMoment(4, 4);
        double mu_5_3 = calculateNormalizedCentralMoment(5, 3);
        double mu_6_2 = calculateNormalizedCentralMoment(6, 2);
        double mu_7_1 = calculateNormalizedCentralMoment(7, 1);
        double mu_8_0 = calculateNormalizedCentralMoment(8, 0);
        
        double mu_0_9 = calculateNormalizedCentralMoment(0, 9);
        double mu_1_8 = calculateNormalizedCentralMoment(1, 8);
        double mu_2_7 = calculateNormalizedCentralMoment(2, 7);
        double mu_3_6 = calculateNormalizedCentralMoment(3, 6);
        double mu_4_5 = calculateNormalizedCentralMoment(4, 5);
        double mu_5_4 = calculateNormalizedCentralMoment(5, 4);
        double mu_6_3 = calculateNormalizedCentralMoment(6, 3);
        double mu_7_2 = calculateNormalizedCentralMoment(7, 2);
        double mu_8_1 = calculateNormalizedCentralMoment(8, 1);        
        double mu_9_0 = calculateNormalizedCentralMoment(9, 0);
        
        double a_4_0 = mu_2_0 + mu_0_2;
        double a_4_2 = mu_2_0 - mu_0_2;
        double b_4_2 = 2*mu_1_1;
        
        double a_5_1 = mu_3_0 + mu_1_2;
        double b_5_1 = mu_2_1 + mu_0_3;
        double a_5_3 = mu_3_0 - 3*mu_1_2;
        double b_5_3 = 3*mu_2_1 - mu_0_3;
        
        double a_6_0 = mu_4_0 + 2*mu_2_2 + mu_0_4;
        double a_6_2 = mu_4_0 - mu_0_4;
        double b_6_2 = 2*(mu_3_1 + mu_1_3);
        double a_6_4 = mu_4_0 - 6*mu_2_2 + mu_0_4;
        double b_6_4 = 4*(mu_3_1 - mu_1_3);
        
        double a_7_1 = mu_5_0 + 2*mu_3_2 + mu_1_4;
        double b_7_1 = mu_4_1 + 2*mu_2_3 + mu_0_5;
        double a_7_3 = mu_5_0 - 2*mu_3_2 - 3*mu_1_4;       
        double b_7_3 = 3*mu_4_1 + 2*mu_2_3 - mu_0_5;
        double a_7_5 = mu_5_0 - 10*mu_3_2 + 5*mu_1_4;
        double b_7_5 = 5*mu_4_1 - 10*mu_2_3 + mu_0_5;
        
        double a_8_0 = mu_6_0 + 3*mu_4_2 + 3*mu_2_4 + mu_0_6;       //the last term is mu_6_0 in the paper, but I think that's a misprint.
        double a_8_2 = mu_6_0 + mu_4_2 - mu_2_4 - mu_0_6;
        double b_8_2 = 2*(mu_5_1 + 2 * mu_3_3 + mu_1_5);
        double a_8_4 = mu_6_0 - 5*mu_4_2 - 5*mu_2_4 + mu_0_6;
        double b_8_4 = 4*(mu_5_1 - mu_1_5);
        double a_8_6 = mu_6_0 - 15*mu_4_2 + 15*mu_2_4 + mu_0_6;
        double b_8_6 = 2*(3*mu_5_1 - 10*mu_3_3 + 3*mu_1_5);
        
        double a_9_1 = mu_7_0 + 3*mu_5_2 + 3*mu_3_4 + mu_1_6;
        double b_9_1 = mu_6_1 + 3*mu_4_3 + 3*mu_2_5 + mu_0_7;
        double a_9_3 = mu_7_0 - mu_5_2 - 5*mu_3_4 -3*mu_1_6;
        double b_9_3 = 3*mu_6_1 + 5*mu_4_3 + mu_2_5 - mu_0_7;
        double a_9_5 = mu_7_0 - 9*mu_5_2 - 5*mu_3_4 + 5*mu_1_6;
        double b_9_5 = 5*mu_6_1 - 5*mu_4_3 - 9*mu_2_5 + mu_0_7;
        double a_9_7 = mu_7_0 - 21*mu_5_2 + 35*mu_3_4 - 7*mu_1_6;
        double b_9_7 = 7*mu_6_1 - 35*mu_4_3 + 21*mu_2_5 - mu_0_7;
        
        double a_10_0 = mu_8_0 + 4*mu_6_2 + 6 *mu_4_4 + 4*mu_2_6 + mu_0_8;
        double a_10_2 = mu_8_0 + 2*mu_6_2 - 2*mu_2_6 - mu_0_8;
        double b_10_2 = 2*(mu_7_1 + 3*mu_5_3 + 3*mu_3_5 + mu_1_7);
        double a_10_4 = mu_8_0 - 4*mu_6_2 - 10*mu_4_4 - 4*mu_2_6 + mu_0_8;
        double b_10_4 = 4*(mu_7_1 + mu_5_3 - mu_3_5 - mu_1_7);
        double a_10_6 = mu_8_0 - 14* mu_6_2 + 14*mu_2_6 - mu_0_8;
        double b_10_6 = 2*(3*mu_7_1 - 7*mu_5_3 - 7*mu_3_5 + 3*mu_1_7);
        double a_10_8 = mu_8_0 - 28*mu_6_2 + 70*mu_4_4 - 28*mu_2_6 + mu_0_8;
        double b_10_8 = 8*(mu_7_1 - 7*mu_5_3 + 7*mu_3_5 - mu_1_7);
        
        double a_11_1 = mu_9_0 + 4*mu_7_2 + 6*mu_5_4 + 4*mu_3_6 + mu_1_8;
        double b_11_1 = mu_8_1 + 4*mu_6_3 + 6*mu_4_5 + 4*mu_2_7 + mu_0_9;
        double a_11_3 = mu_9_0 - 6*mu_5_4 - 8*mu_3_6 - 3*mu_1_8;
        double b_11_3 = 3*mu_8_1 + 8*mu_6_3 + 6*mu_4_5 - mu_0_9;
        double a_11_5 = mu_9_0 - 8*mu_7_2 - 14*mu_5_4 + 5*mu_1_8;
        double b_11_5 = 5*mu_8_1 - 14*mu_4_5 - 8*mu_2_7 + mu_0_9;
        double a_11_7 = mu_9_0 - 20*mu_7_2 + 14*mu_5_4 + 28*mu_3_6 - 7*mu_1_8;
        double b_11_7 = 7*mu_8_1 - 28*mu_6_3 - 14*mu_4_5 + 20*mu_2_7 - mu_0_9;
        double a_11_9 = mu_9_0 - 36*mu_7_2 + 126*mu_5_4 - 84*mu_3_6 + 9*mu_1_8;
        double b_11_9 = 9*mu_8_1 - 84*mu_6_3 + 126*mu_4_5 - 36*mu_2_7 + mu_0_9;
        
        double[] ret = new double[52];
        ret[0] = a_4_0;
        ret[1] = a_4_2*a_4_2 + b_4_2*b_4_2;
        ret[2] = a_4_2*(a_5_1*a_5_1 - b_5_1*b_5_1) + 2*a_5_1*b_5_1*b_4_2;
        ret[3] = a_5_1*a_5_1 + b_5_1*b_5_1;
        ret[4] = a_5_3*a_5_3 + b_5_3*b_5_3;
        
        ret[5] = a_5_1*a_5_3*(a_5_1*a_5_1 - 3*b_5_1*b_5_1) + b_5_1*b_5_3*(3*a_5_1*a_5_1 - b_5_1*b_5_1);
        ret[6] = a_5_1*b_5_3*(a_5_1*a_5_1 - 3*b_5_1*b_5_1) - b_5_1*a_5_3*(3*a_5_1*a_5_1 - b_5_1*b_5_1);
        ret[7] = a_6_0;
        ret[8] = a_6_2*a_6_2 + b_6_2*b_6_2;
        
        ret[9] = a_6_4*a_6_4 + b_6_4*b_6_4;
        ret[10] = a_6_4*(a_6_2*a_6_2 - b_6_2*b_6_2) + 2*a_6_2*b_6_2*b_6_4;
        ret[11] = b_6_4*(a_6_2*a_6_2 - b_6_2*b_6_2) - 2*a_6_2*b_6_2*a_6_4;
        
        ret[12] = a_6_2*(a_7_1*a_7_1 - b_7_1*b_7_1) + 2*a_7_1*b_7_1*b_6_2;
        ret[13] = a_7_1*a_7_1 + b_7_1*b_7_1;
        ret[14] = a_7_3*a_7_3 + b_7_3*b_7_3;
        
        ret[15] = a_7_5*a_7_5 + b_7_5*b_7_5;
        ret[16] = a_7_1*a_7_3*(a_7_1*a_7_1 - 3*b_7_1*b_7_1) + b_7_1*b_7_3*(3*a_7_1*a_7_1 - b_7_1*b_7_1);
        ret[17] = a_7_1*b_7_3*(a_7_1*a_7_1 - 3*b_7_1*b_7_1) - b_7_1*a_7_3*(3*a_7_1*a_7_1 - b_7_1*b_7_1);  //the first a_7_1 is squared in the article, but I suspect that's another misprint
        ret[18] = a_8_0;
        ret[19] = a_8_2*a_8_2 + b_8_2*b_8_2;
        ret[20] = a_8_4*a_8_4 + b_8_4*b_8_4;
        
        ret[21] = a_8_6*a_8_6 + b_8_6*b_8_6;
        ret[22] = a_8_4*(a_8_2*a_8_2 - b_8_2*b_8_2) + 2*a_8_2*b_8_2*b_8_4;
        ret[23] = b_8_4*(a_8_2*a_8_2 - b_8_2*b_8_2) - 2*a_8_2*b_8_2*a_8_4;
        
        ret[24] = a_8_2*(a_9_1*a_9_1 - b_9_1*b_9_1) + 2*a_9_1*b_9_1*b_8_2;
        ret[25] = a_9_1*a_9_1 + b_9_1*b_9_1;
        ret[26] = a_9_3*a_9_3 + b_9_3*b_9_3;
        ret[27] = a_9_5*a_9_5 + b_9_5*b_9_5;
        
        ret[28] = a_9_7*a_9_7 + b_9_7*b_9_7;
        ret[29] = a_9_1*a_9_3*(a_9_1*a_9_1 - 3*b_9_1*b_9_1) + b_9_1*b_9_3*(3*a_9_1*a_9_1 - b_9_1*b_9_1);
        ret[30] = a_9_1*b_9_3*(a_9_1*a_9_1 - 3*b_9_1*b_9_1) - b_9_1*a_9_3*(3*a_9_1*a_9_1 - b_9_1*b_9_1);
        ret[31] = a_9_1*a_9_5*(Math.pow(a_9_1, 4) - 10*Math.pow(a_9_1, 2)*Math.pow(b_9_1, 2) + 5*Math.pow(b_9_1, 4)) + b_9_1*b_9_5*(5*Math.pow(a_9_1, 4) - 10*Math.pow(a_9_1, 2)*Math.pow(b_9_1, 2) + Math.pow(b_9_1, 4));
        ret[32] = a_9_1*b_9_5*(Math.pow(a_9_1, 4) - 10*Math.pow(a_9_1, 2)*Math.pow(b_9_1, 2) + 5*Math.pow(b_9_1, 4)) - b_9_1*a_9_5*(5*Math.pow(a_9_1, 4) - 10*Math.pow(a_9_1, 2)*Math.pow(b_9_1, 2) + Math.pow(b_9_1, 4));
        ret[33] = a_10_0;
        ret[34] = a_10_2*a_10_2 + b_10_2*b_10_2;
        ret[35] = a_10_4*a_10_4 + b_10_4*b_10_4;
        ret[36] = a_10_6*a_10_6 + b_10_6*b_10_6;
        
        ret[37] = a_10_8*a_10_8 + b_10_8*b_10_8;
        ret[38] = a_10_4*(a_10_2*a_10_2 - b_10_2*b_10_2) + 2*a_10_2*b_10_2*b_10_4;
        ret[39] = b_10_4*(a_10_2*a_10_2 - b_10_2*b_10_2) - 2*a_10_2*b_10_2*b_10_4;
        ret[40] = a_10_8*(a_10_4*a_10_4 - b_10_4*b_10_4) + 2*a_10_4*b_10_4*b_10_8;
        ret[41] = b_10_8*(a_10_4*a_10_4 - b_10_4*b_10_4) - 2*a_10_4*b_10_4*a_10_8;
        
        ret[42] = a_10_2*(a_11_1*a_11_1 - b_11_1*b_11_1) + 2*a_11_1*b_11_1*b_10_2;
        ret[43] = a_11_1*a_11_1 + b_11_1*b_11_1;
        ret[44] = a_11_3*a_11_3 + b_11_3*b_11_3;
        ret[45] = a_11_5*a_11_5 + b_11_5*b_11_5;
        
        ret[46] = a_11_7*a_11_7 + b_11_7*b_11_7;        
        ret[47] = a_11_9*a_11_9 + b_11_9*b_11_9;
        ret[48] = a_11_1*a_11_3*(a_11_1*a_11_1 - 3*b_11_1*b_11_1) + b_11_1*b_11_3*(3*a_11_1*a_11_1 - b_11_1*b_11_1);
        ret[49] = a_11_1*b_11_3*(a_11_1*a_11_1 - 3*b_11_1*b_11_1) - b_11_1*a_11_3*(3*a_11_1*a_11_1 - b_11_1*b_11_1);
        ret[50] = a_11_1*a_11_5*(Math.pow(a_11_1,4) - 10*Math.pow(a_11_1, 2)*Math.pow(b_11_1, 2 + 5*Math.pow(b_11_1, 4))) + b_11_1*b_11_5*(5*Math.pow(a_11_1,4) - 10*Math.pow(a_11_1, 2)*Math.pow(b_11_1, 2 + Math.pow(b_11_1, 4)));
        ret[51] = a_11_1*b_11_5*(Math.pow(a_11_1,4) - 10*Math.pow(a_11_1, 2)*Math.pow(b_11_1, 2 + 5*Math.pow(b_11_1, 4))) - b_11_1*a_11_5*(5*Math.pow(a_11_1,4) - 10*Math.pow(a_11_1, 2)*Math.pow(b_11_1, 2 + Math.pow(b_11_1, 4)));    //accidentally said ret[50] instead of ret[51], overwriting previous line, but I don't want to go back and recompute all of that just now....
        
        return ret;
    }
    
    public double[] calculateAffineInvariants(){    //from ChimKassimIbrahim.pdf
        double mu_0_0 = calculateCentralMoment(0, 0);
        double mu_2_0 = calculateCentralMoment(2, 0);
        double mu_0_2 = calculateCentralMoment(0, 2);
        double mu_1_1 = calculateCentralMoment(1, 1);
        double mu_3_0 = calculateCentralMoment(3, 0);
        double mu_0_3 = calculateCentralMoment(0, 3);
        double mu_1_2 = calculateCentralMoment(1, 2);
        double mu_2_1 = calculateCentralMoment(2, 1);
        
        double[] ret = new double[4];
        ret[0] = (1/Math.pow(mu_0_0, 4)) * (mu_2_0 * mu_0_2 - mu_1_1 * mu_1_1);
        ret[1] = (1/Math.pow(mu_0_0, 10)) * (mu_3_0 * mu_3_0 * mu_0_3 * mu_0_3 - 
                                             6 * mu_3_0 * mu_2_1 * mu_1_2 * mu_0_3 +    
                                             4 * mu_3_0 * Math.pow(mu_1_2, 3) + 
                                             4 * mu_0_3 * Math.pow(mu_2_1, 3) - 
                                             3 * mu_2_1 * mu_2_1 * mu_1_2 * mu_1_2);
        ret[2] = (1/Math.pow(mu_0_0, 7)) * (mu_2_0 * (mu_2_1 * mu_0_3 - mu_1_2 * mu_1_2) -
                                            mu_1_1 * (mu_3_0 * mu_0_3 - mu_2_1 * mu_1_2) +
                                            mu_0_2 * (mu_3_0 * mu_1_2 - mu_2_1 * mu_2_1));
        ret[3] = (1/Math.pow(mu_0_0, 11)) * (Math.pow(mu_2_0, 3) * mu_0_3 * mu_0_3 -
                                             6 * mu_2_0 * mu_2_0 * mu_1_1 * mu_1_2 * mu_0_3 -
                                             6 * mu_2_0 * mu_2_0 * mu_0_2 * mu_2_1 * mu_0_3 +
                                             9 * mu_2_0 * mu_2_0 * mu_0_2 * mu_1_2 * mu_1_2 +
                                             12 * mu_2_0 * mu_1_1 * mu_1_1 * mu_2_1 * mu_0_3 +
                                             6 * mu_2_0 * mu_1_1 * mu_0_2 * mu_3_0 * mu_0_3 -
                                             18 * mu_2_0 * mu_1_1 * mu_0_2 * mu_2_1 * mu_1_2 -
                                             8 * Math.pow(mu_1_1, 3) * mu_3_0 * mu_0_3 -
                                             6 * mu_2_0 * mu_0_2 * mu_0_2 * mu_3_0 * mu_1_2 +
                                             9 * mu_2_0 * mu_0_2 * mu_0_2 * mu_2_1 * mu_2_1 +
                                             12 * mu_1_1 * mu_1_1 * mu_0_2 * mu_3_0 * mu_1_2 -
                                             6 * mu_1_1 * mu_0_2 * mu_0_2 * mu_3_0 * mu_2_1 +
                                             Math.pow(mu_0_2, 3) * mu_3_0 * mu_3_0);

        return ret;
    }    

    public double[] calculateSluzekInvariantTrio(){
        double m_0_0 = calculateMoment(0, 0);
        double mu_1_1 = calculateCentralMoment(1, 1);
        double mu_2_0 = calculateCentralMoment(2, 0);
        double mu_0_2 = calculateCentralMoment(0, 2);
        
        double[] ret = new double[3];
        ret[0] = (mu_2_0 + mu_0_2)/(m_0_0 * m_0_0);                 
        ret[1] = (Math.pow((mu_2_0 - mu_0_2), 2) + 4 * mu_1_1 * mu_1_1)/Math.pow(m_0_0, 4);
        ret[2] = (mu_2_0 * mu_0_2 - mu_1_1 * mu_1_1)/Math.pow(m_0_0, 4);
        return ret;
        
    }
    
}
