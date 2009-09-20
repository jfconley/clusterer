/*
 * LandscanTrainingDataGenerator.java
 *
 * Created on February 21, 2008, 10:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.DissUtils;
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
import java.io.FileReader;
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
 * @author jfc173
 */
public class LandscanTrainingDataGenerator {
    
    /**
     * Creates a new instance of LandscanTrainingDataGenerator
     */
    public LandscanTrainingDataGenerator() {
    }
     
    public static void main(String[] args){
//        String[] types = new String[]{"inf", "road", "wind"};  
        String[] types = new String[]{"airport"};
        MomentGenerator mg = new MomentGenerator(); 
        
        //for the first ten tracks of each type (or whichever ones, really
        for (int run = 40; run < 50; run++){                  
            //for each of the three types            
            for (int type = 0; type < types.length; type++){  
//                if ((run != 2) || (type != 0)){
                String thisType = types[type];  
                double[][] moments = new double[40][];                         
                //get the track length from the summary
                int length = 0;
                try{
                    File summary = new File("D:/synthLandscan/" + thisType + "/run" + run + "/summary.csv");
                    FileReader in = new FileReader(summary);
                    CSVParser csvp = new CSVParser(in);
                    String[][] values = csvp.getAllValues();   
                    length = (values.length - 2) * 5;                    
                } catch (IOException ioe){
                    System.out.println("what the? " + ioe.getMessage());
                }
                
                //for 200 evenly-spaced time steps (reduce to 100 in case of time issues, 40 for test data)
                for (int step = 0; step < 40; step++){
                    double interval = (double) length / 40d;
                    int time = nearestMultipleOfFive((int) Math.round(interval * step));
//                    if ((run != 2) || (type != 1) || (step > 81)){
                    File file = new File("D:/synthLandscan/" + thisType + "/run" + run + "/" + thisType + "PAtime" + time + ".csv");
                    float[][] raster;
                    //run GAM & get a coverage out of it (or use the base raster)
                    raster = FZRasterFromFile(file);
                    mg.setRaster(raster);
                    //calculate moments
                    double[] temp = mg.calculateMomentInvariants();
                    moments[step] = new double[temp.length];
                    for (int blob = 0; blob < temp.length; blob++){
                        moments[step][blob] = temp[blob];
                    }
                    System.out.println("finished moments for type " + thisType + ", run " + run + ", step " + step); 
                    try{
                        File momentFile = new File("D:/synthLandscan/" + thisType + "/run" + run + "/FZMoments" + step + ".csv");
                        FileWriter momentWriter = new FileWriter(momentFile);
                        momentWriter.write(""+temp[0]);
                        for (int l = 1; l < temp.length; l++){
                            momentWriter.write("," + temp[l]);
                        }
                        momentWriter.write('\n'); 
                        momentWriter.close();
                    } catch (IOException ioe){
                        System.out.println("IOException writing moment file.  " + ioe.getMessage());
                    }       
//                    }
                }  //end for (int step...
                //stash away for safekeeping and repeat, repeat, repeat!                 
                try{
                    File momentFile = new File("D:/synthLandscan/" + thisType + "/run" + run + "/FZMoments.csv");
                    FileWriter momentWriter = new FileWriter(momentFile);
                    for (int k = 0; k < moments.length; k++){
                        momentWriter.write(""+moments[k][0]);
                        for (int l = 1; l < moments[k].length; l++){
                            momentWriter.write("," + moments[k][l]);
                        }
                        momentWriter.write('\n');
                    }   
                    momentWriter.close();
                } catch (IOException ioe){
                    System.out.println("IOException writing moment file.  " + ioe.getMessage());
                }        
//                }
            }  //end for (int type...                
        }  //end for (int run...
    }
    
    private static int nearestMultipleOfFive(int in){
        int mod = in % 5;
        switch (mod){
            case 0:
                return in;
            case 1:
                return in - 1;
            case 2:
                return in - 2;
            case 3:
                return in + 2;
            case 4:
                return in + 1;
            default:
                System.out.println("this shouldn't happen.");
                return in;
        }  
    }
        
    private static float[][] rasterFromFile(File inFile){
        //most of this stuff is from BatchMain
        //run GAM on this file
        InitGAMFile igf = new InitGAMFile();
        try{
            igf.processTextFileWithoutDialog(inFile);
        } catch (FileNotFoundException fnfe){
            System.out.println("why isn't " + inFile + " found?  " + fnfe.getMessage());
        } catch (IOException ioe){
            System.out.println("io exception: " + ioe.getMessage());
        }

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
        return DissUtils.coverageToMatrix(binarized);                    
    }    
    
    private static float[][] baseRasterFromFile(File inFile){
//        int rasterSum = 0;
        float[][] raster = new float[1105][1105];
        float[][] smoothed = new float[1105][1105];
        String[][] values;
        try{
            FileReader in = new FileReader(inFile);
            CSVParser csvp = new CSVParser(in);
            values = csvp.getAllValues();         

            for (int i = 1; i < values.length; i++){
                int x = Integer.parseInt(values[i][0]);
                int y = Integer.parseInt(values[i][1]);
                int cases = Integer.parseInt(values[i][3]);
                int rasterMinX = 13*x;
                int rasterMinY = 13*y;
                int rasterMaxX = 13*x + 12;
                int rasterMaxY = 13*y + 12;

                if (cases == 0){
                    for (int j = rasterMinX; j <= rasterMaxX; j++){
                        for (int k = rasterMinY; k <= rasterMaxY; k++){
                            raster[j][k] = 0;
                        }
                    }                        
                } else {
                    for (int j = rasterMinX; j <= rasterMaxX; j++){
                        for (int k = rasterMinY; k <= rasterMaxY; k++){
                            raster[j][k] = 1;
//                            rasterSum++;
                        }
                    }                                
                }
            }

            //raster is a bit pixellated right now, so smooth it.
            for (int i = 0; i < raster.length; i++){
                for (int j = 0; j < raster[i].length; j++){
                    int zeros = 0;
                    int ones = 0;

                    int rasterMinX = Math.max(0, i-6);
                    int rasterMaxX = Math.min(i+6, raster.length-1);
                    int rasterMinY = Math.max(0, j-6);
                    int rasterMaxY = Math.min(j+6, raster[i].length-1);

                    for (int k = rasterMinX; k <= rasterMaxX; k++){
                        for (int l = rasterMinY; l <= rasterMaxY; l++){
                            if (raster[k][l] == 0){
                                zeros++;
                            } else{
                                ones++;
                            }
                        }
                    }

                    if (ones >= zeros){
                        smoothed[i][j] = 1;
                    } else {
                        smoothed[i][j] = 0;
                    }
                }
            }
            return smoothed;
        } catch (IOException ioe){
            System.out.println("what the? " + ioe.getMessage());
            return null;  //deliberately returning null instead of smoothed to generate an error instead of chugging along with a raster full of zeros.
        }            
         
    }
    
    private static float[][] FZRasterFromFile(File inFile){
        //most of this stuff is from BatchMain
        //run GAM on this file
        InitGAMFile igf = new InitGAMFile();
        try{
            igf.processTextFileWithoutDialog(inFile);
        } catch (FileNotFoundException fnfe){
            System.out.println("why isn't " + inFile + " found?  " + fnfe.getMessage());
        } catch (IOException ioe){
            System.out.println("io exception: " + ioe.getMessage());
        }

        RandomGam gam = new RandomGam();
        double largeDimension = Math.max(igf.getMaxX() - igf.getMinX(),
                                         igf.getMaxY() - igf.getMinY());              
        gam.setMaxRadius(0.05 * largeDimension);
        gam.setMinRadius(0.005 * largeDimension);
        gam.setMinPoints(3);
        gam.setMinAccepted(1.5);
        gam.setNumTests(7500);
        gam.setFitnessFunction(new FitnessRelativePct(igf.getDataSet()));  
        gam.setInitializer(igf);  

        ImageProcessor ip = new ImageProcessor(); 
        ip.setInitializer(igf);
        ip.setGam(gam);                 

        //get the image
        GridCoverage2D coverage = ip.generateCoverage();

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
        return DissUtils.coverageToMatrix(binarized);                    
    }       
    
}
