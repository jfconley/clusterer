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
    
}
