/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package parallelUtilMains;

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
 * @author jconley
 */
public class PLandscanTrainingDataGenerator {
    
    public PLandscanTrainingDataGenerator(){      
    }
    
    public static void main(String[] args){
        PartialDataGenerator infFZ = new PartialDataGenerator("inf", "FZ");
        PartialDataGenerator roadFZ = new PartialDataGenerator("road", "FZ");
        PartialDataGenerator riverFZ = new PartialDataGenerator("river", "FZ");
        PartialDataGenerator windFZ = new PartialDataGenerator("wind", "FZ");
        PartialDataGenerator airportFZ = new PartialDataGenerator("airport", "FZ");
        
        PartialDataGenerator infGAM = new PartialDataGenerator("inf", "GAM");
        PartialDataGenerator roadGAM = new PartialDataGenerator("road", "GAM");
        PartialDataGenerator riverGAM = new PartialDataGenerator("river", "GAM");
        PartialDataGenerator windGAM = new PartialDataGenerator("wind", "GAM");
        PartialDataGenerator airportGAM = new PartialDataGenerator("airport", "GAM");
        
        PartialDataGenerator infbase = new PartialDataGenerator("inf", "base");
        PartialDataGenerator roadbase = new PartialDataGenerator("road", "base");
        PartialDataGenerator riverbase = new PartialDataGenerator("river", "base");
        PartialDataGenerator windbase = new PartialDataGenerator("wind", "base");
        PartialDataGenerator airportbase = new PartialDataGenerator("airport", "base");    
        
        try{
            Thread infFZThread = new Thread(infFZ);
            Thread roadFZThread = new Thread(roadFZ);
            Thread riverFZThread = new Thread(riverFZ);
            Thread windFZThread = new Thread(windFZ);
            Thread airportFZThread = new Thread(airportFZ);

            Thread infGAMThread = new Thread(infGAM);
            Thread roadGAMThread = new Thread(roadGAM);
            Thread riverGAMThread = new Thread(riverGAM);
            Thread windGAMThread = new Thread(windGAM);
            Thread airportGAMThread = new Thread(airportGAM);

            Thread infbaseThread = new Thread(infbase);
            Thread roadbaseThread = new Thread(roadbase);
            Thread riverbaseThread = new Thread(riverbase);
            Thread windbaseThread = new Thread(windbase);
            Thread airportbaseThread = new Thread(airportbase);   
            
            //batch 1
            infFZThread.start();
            roadFZThread.start();
            riverFZThread.start();
            windFZThread.start();
            airportFZThread.start();
            infbaseThread.start();
            roadbaseThread.start();
            riverbaseThread.start();    
            
            infFZThread.join();
            roadFZThread.join();
            riverFZThread.join();
            windFZThread.join();
            airportFZThread.join();
            infbaseThread.join();
            roadbaseThread.join();
            riverbaseThread.join();           

            //batch 2
            windbaseThread.start();
            airportbaseThread.start();
            infGAMThread.start();
            roadGAMThread.start();
            riverGAMThread.start();
            windGAMThread.start();
            airportGAMThread.start();
            
            windbaseThread.join();
            airportbaseThread.join();            
            infGAMThread.join();
            roadGAMThread.join();
            riverGAMThread.join();
            windGAMThread.join();
            airportGAMThread.join();  
            roadFZThread.join();
            airportFZThread.join();
        
        } catch (InterruptedException ie){
            System.out.println(ie.getMessage());
            threadMessage("This thread was interrupted");
        }        
    }
    
    private static void threadMessage(String message){
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }    
    
    private static class PartialDataGenerator implements Runnable{
        
        String type, method;
        
        public PartialDataGenerator(String typ, String meth){
            type = typ;
            method = meth;
        }
        
        public void run(){        
            MomentGenerator mg = new MomentGenerator(); 
            int start = 0;
            if ((type.equals("road")) && (method.equals("FZ"))){
                start = 48;
            } else if ((type.equals("airport")) && (method.equals("FZ"))){
                start = 28;
            }
            
            //for the tracks of each type
            for (int run = start; run < 50; run++){                  
                //for each of the three types             
                double[][] moments = new double[40][];                         
                //get the track length from the summary
                int length = 0;
                try{
                    File summary = new File("D:/synthLandscan/" + type + "/run" + run + "/summary.csv");
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
                    File file = new File("D:/synthLandscan/" + type + "/run" + run + "/" + type + "PAtime" + time + ".csv");
                    float[][] raster;
                    //run GAM & get a coverage out of it (or use the base raster)
                    if (method.equals("FZ")){
                        raster = FZRasterFromFile(file);
                    } else if (method.equals("GAM")){
                        raster = GAMRasterFromFile(file);
                    } else {
                        raster = baseRasterFromFile(file);
                    }
                    mg.setRaster(raster);
                    //calculate moments
                    double[] temp = mg.calculateMomentInvariants();
                    moments[step] = new double[temp.length];
                    for (int blob = 0; blob < temp.length; blob++){
                        moments[step][blob] = temp[blob];
                    }
                    System.out.println("finished moments for method" + method + ", type " + type + ", run " + run + ", step " + step); 
                    try{
                        File momentFile = new File("D:/synthLandscan/" + type + "/run" + run + "/" + method + "Moments" + step + ".csv");
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
                    File momentFile = new File("D:/synthLandscan/" + type + "/run" + run + "/" + method + "Moments.csv");
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
            }  //end for (int run...
        }
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
        
    private static float[][] GAMRasterFromFile(File inFile){
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
        //I have the space, so use it!
        String noExtension = inFile.getPath().substring(0, inFile.getPath().length() - 4);        
        String filePath = noExtension + "GAMRaster.csv";
        saveRaster(DissUtils.coverageToMatrix(band0), filePath);
        
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
            //I have the space, so use it!
            String noExtension = inFile.getPath().substring(0, inFile.getPath().length() - 4);
            String filePath = noExtension + "baseRaster.csv";
            saveRaster(smoothed, filePath);              
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
        //I have the space, so use it!
        String noExtension = inFile.getPath().substring(0, inFile.getPath().length() - 4);        
        String filePath = noExtension + "FZRaster.csv";
        saveRaster(DissUtils.coverageToMatrix(band0), filePath);        
        
        pvg.parameter("Source").setValue(band0);

        pvg.parameter("Threshold").setValue(new Double(floor + (ceiling - floor)/4));
        GridCoverage2D binarized = (GridCoverage2D) def.doOperation(pvg);                 
        return DissUtils.coverageToMatrix(binarized);                    
    }           
    
    private static void saveRaster(float[][] raster, String filePath){
        File f = new File(filePath);
        try{
            FileWriter out = new FileWriter(f);
            for (int i = 0; i < raster.length; i++){
                for (int j = 0; j < raster[i].length; j++){
                    out.write(raster[i][j] + ",");
                }
                out.write('\n');
            }
            out.close();
            System.out.println("raster saved to " + filePath);
        } catch (IOException ioe){
            System.out.println("IOException when saving raster to " + filePath);
        }
    }
    
}
