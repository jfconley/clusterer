/*
 * TimeSeriesMomentTest.java
 *
 * Created on May 31, 2007, 10:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.DissUtils;
import dissprogram.image.ImageBreaker;
import dissprogram.image.ImageProcessor;
import dissprogram.image.MomentGenerator;
import edu.psu.geovista.gam.FitnessRelativePct;
import edu.psu.geovista.gam.InitGAMFile;
import edu.psu.geovista.gam.RandomGam;
import edu.psu.geovista.gam.SystematicGam;
import edu.psu.geovista.io.csv.CSVParser;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.coverage.processing.Operations;
import org.opengis.coverage.processing.Operation;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author z4x
 */
public class TimeSeriesMomentTest {
    
    /** Creates a new instance of TimeSeriesMomentTest */
    public TimeSeriesMomentTest() {
    }
    
    public static void main(String[] args){
//        int[] lengths = new int[]{461, 445, 470, 430, 462, 443, 421, 477, 468, 438};  //wind (finished 0-9)       
//        int[] lengths = new int[]{597, 537, 431, 467, 461, 391, 440, 467, 461, 589};  //road (finished 0-9)
        int[] lengths = new int[]{754, 1059, 886, 832, 903, 883, 830, 805, 832, 982};  //infect (finished 0-9)
        
        for (int folder = 8; folder < 10; folder++){
            int time = 0;
            double[][] GAMmoments = new double[lengths[folder]][7];  
            double[][] FZmoments = new double[lengths[folder]][7];
            boolean notDone = true;
            while (notDone){
                try{
                    File f = new File("C:/z4xNoSpaces/synthData/infect/run" + folder + "/inf75time" + time + ".csv");

                    //most of this stuff is from BatchMain
                    //run GAM on this file
                    InitGAMFile igf = new InitGAMFile();
                    igf.processTextFileWithoutDialog(f);

    //                RandomGam gam = new RandomGam();
    //                ((RandomGam) gam).setNumTests(500);                
                    SystematicGam gam = new SystematicGam();
                    double largeDimension = Math.max(igf.getMaxX() - igf.getMinX(),
                                                     igf.getMaxY() - igf.getMinY());              
                    gam.setMaxRadius(0.05 * largeDimension);
                    gam.setMinRadius(0.005 * largeDimension);
                    gam.setMinPoints(3);
                    gam.setMinAccepted(1.5);
                    gam.setFitnessFunction(new FitnessRelativePct(igf.getDataSet()));  
                    gam.setInitializer(igf);  

                    ImageProcessor ip = new ImageProcessor(); 
                    ip.setInitializer(igf);
                    ip.setGam(gam);                 

                    //get the image
                    GridCoverage2D coverage = ip.generateCoverageGAMOpt();

                    //binarize it
                    int floor = (int) Math.floor(ip.getMinHeight());
                    int ceiling = (int) Math.ceil(ip.getMaxHeight());             

                    AbstractProcessor def = DefaultProcessor.getInstance(); 
                    Operations ops = new Operations(new RenderingHints(null));
                    Operation binOp = def.getOperation("Binarize");
                    ParameterValueGroup pvg = binOp.getParameters();

                    GridCoverage2D band0 = (GridCoverage2D) ops.selectSampleDimension(coverage, new int[]{0});
                    pvg.parameter("Source").setValue(band0);

                    pvg.parameter("Threshold").setValue(new Double(floor + (ceiling - floor)/4));
                    GridCoverage2D binarized = (GridCoverage2D) def.doOperation(pvg);                 

                    //calculate the moments
                    MomentGenerator mgf = new MomentGenerator();
                    mgf.setRaster(DissUtils.coverageToMatrix(binarized));
                    GAMmoments[time/5] = mgf.calculateMomentInvariants();                               

    //                System.out.println("finished time step # " + time);
                    System.out.print(time);
                    for (int a = 0; a < 7; a++){
                        System.out.print(", " + GAMmoments[time/5][a]);
                    }
                    System.out.println();
/*
                    //repeat for FZ
                    InitGAMFile igf2 = new InitGAMFile();
                    igf2.processTextFileWithoutDialog(f);

                    RandomGam fz = new RandomGam();
                    ((RandomGam) fz).setNumTests(500);     
                    double largeDimension2 = Math.max(igf2.getMaxX() - igf2.getMinX(),
                                                      igf2.getMaxY() - igf2.getMinY());              
                    fz.setMaxRadius(0.05 * largeDimension2);
                    fz.setMinRadius(0.005 * largeDimension2);
                    fz.setMinPoints(3);
                    fz.setMinAccepted(1.5);
                    fz.setFitnessFunction(new FitnessRelativePct(igf2.getDataSet()));  
                    fz.setInitializer(igf2);  

                    ImageProcessor ip2 = new ImageProcessor(); 
                    ip2.setInitializer(igf2);
                    ip2.setGam(fz);                 

                    //get the image
                    GridCoverage2D coverage2 = ip2.generateCoverage();

                    //binarize it
                    int floor2 = (int) Math.floor(ip2.getMinHeight());
                    int ceiling2 = (int) Math.ceil(ip2.getMaxHeight());             

                    AbstractProcessor def2 = DefaultProcessor.getInstance(); 
                    Operations ops2 = new Operations(new RenderingHints(null));
                    Operation binOp2 = def2.getOperation("Binarize");
                    ParameterValueGroup pvg2 = binOp2.getParameters();

                    GridCoverage2D band0_2 = (GridCoverage2D) ops2.selectSampleDimension(coverage2, new int[]{0});
                    pvg2.parameter("Source").setValue(band0_2);

                    pvg2.parameter("Threshold").setValue(new Double(floor2 + (ceiling2 - floor2)/4));
                    GridCoverage2D binarized2 = (GridCoverage2D) def2.doOperation(pvg2);                 

                    //calculate the moments
                    MomentGenerator mgf2 = new MomentGenerator();
                    mgf2.setRaster(DissUtils.coverageToMatrix(binarized2));
                    FZmoments[time/5] = mgf2.calculateMomentInvariants();                               

                    System.out.println("finished time step # " + time);
                    System.out.print(time);
                    for (int a = 0; a < 7; a++){
                        System.out.print(", " + FZmoments[time/5][a]);
                    }
                    System.out.println();
*/
                    time = time + 5;                                
                } catch (FileNotFoundException fnfe){
                    //done.
                    notDone = false;
                } catch (IOException ioe){
                    System.out.println("exception when loading file: " + ioe.getMessage());
                }
            }

            //write out the arrays
            try{
                FileWriter outFile = new FileWriter("C:/z4xNoSpaces/synthData/infect/run" + folder + "/GAMmoments.csv");

                outFile.write("time, gam1_raw, gam2_raw, gam3_raw, gam4_raw, gam5_raw, gam6_raw, gam7_raw" + '\n');
                for (int i = 0; i < GAMmoments.length; i++){
                    outFile.write(i*5 + "");
                    for (int j = 0; j < GAMmoments[i].length; j++){
                        outFile.write(", " + GAMmoments[i][j]); 
                    }
                    outFile.write('\n');
                }

                outFile.close();
                System.out.println("wrote GAM moment summary");

            } catch (IOException ioe){
                System.out.println("IOException when saving summary file: " + ioe.getMessage());
            }       
/*
            try{
                FileWriter outFile = new FileWriter("C:/z4xNoSpaces/synthData/infect/run" + folder + "/FZmoments.csv");

                outFile.write("time, fz1_raw, fz2_raw, fz3_raw, fz4_raw, fz5_raw, fz6_raw, fz7_raw" + '\n');
                for (int i = 0; i < FZmoments.length; i++){
                    outFile.write(i*5 + "");
                    for (int j = 0; j < FZmoments[i].length; j++){
                        outFile.write(", " + FZmoments[i][j]); 
                    }
                    outFile.write('\n');
                }

                outFile.close();
                System.out.println("wrote FZ moment summary");

            } catch (IOException ioe){
                System.out.println("IOException when saving summary file: " + ioe.getMessage());
            }   
*/      }
    }
    
}
