/*
 * TrackClassificationTest.java
 *
 * Created on July 31, 2007, 9:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.DissUtils;
import dissprogram.classification.ClassedArray;
import dissprogram.classification.DistanceWeightedKNN;
import dissprogram.classification.ShapeClassifier;
import dissprogram.classification.SimpleKNN;
import dissprogram.evidence.SetClassifier;
import dissprogram.image.ImageProcessor;
import dissprogram.image.MomentGenerator;
import edu.psu.geovista.gam.FitnessRelativePct;
import edu.psu.geovista.gam.InitGAMFile;
import edu.psu.geovista.gam.SystematicGam;
import edu.psu.geovista.io.csv.CSVParser;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;
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
public class TrackClassificationTest {
    
    /** Creates a new instance of TrackClassificationTest */
    public TrackClassificationTest() {
    }
    
    public static void main (String[] args){
        boolean debug = false;
        if (debug){
            debug();
        } else {
            nonDebug();
        }
    }        
    
    private static void debug(){
        //read in the training data
        File f;
        String[][] values;
        try{
            f = new File("C:/z4xNoSpaces/synthData/comparison/balanced/GAMMomentsNoZBalMerged.csv");
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            values = csvp.getAllValues(); 
        }
        catch (FileNotFoundException fnfe){
            throw new RuntimeException("No file!  " + fnfe.getMessage());            
        }       
        catch (IOException ioe){
            throw new RuntimeException("IO Exception!  " + ioe.getMessage());
        }
        
        //to see if leaving one or two invariants out improves classification (Auroop from ORNL's suggestion)
        boolean[] include = new boolean[]{true, true, true, true, true, true, true};
        int numInvsUsed = 0;
        for (int i = 0; i < include.length; i++){
            if (include[i]){
                numInvsUsed++;
            }
        }
        
        ClassedArray[] matrix = new ClassedArray[values.length-1];
        for (int i = 1; i < values.length; i++){
            double[] data = new double[numInvsUsed]; //was values[i].length - 1
            int index = 0;  //because I can't assume it will be data[j]--some values of j might be skipped by the include array
            for (int j = 0; j < values[i].length - 1; j++){
                if (include[j]){
                    data[index] = Double.parseDouble(values[i][j]);                
                    index++;
                }
            }
            ClassedArray ca = new ClassedArray();
            ca.setArray(data);
            ca.setKlass(values[i][values[i].length - 1]);  
            matrix[i-1] = ca;
        }                 
        
        ClassedArray[] z = new ClassedArray[matrix.length];
        double[] means = new double[matrix[0].getArray().length];
        double[] stdevs = new double[matrix[0].getArray().length];
        double[] logMeans = new double[matrix[0].getArray().length];
        double[] logStDevs = new double[matrix[0].getArray().length];               
        double tolerance = 0.001;  //if the mean is within tolerance of 0 and the standard deviation is within tolerance of 1, then assume it's already standardized.
        
        //compute means
        double[] sums = new double[means.length];
        double[] logSums = new double[logMeans.length];
        for (int i = 0; i < matrix.length; i++){
            double[] snuh = matrix[i].getArray();
            for (int j = 0; j < snuh.length; j++){
                sums[j] = sums[j] + snuh[j];           
                logSums[j] = logSums[j] + Math.log(Math.abs(snuh[j]));
            }
        }
        for (int k = 0; k < means.length; k++){
            means[k] = sums[k] / matrix.length;
            logMeans[k] = logSums[k] / matrix.length;
        }
        
        //compute stdevs
        Arrays.fill(sums, 0);
        Arrays.fill(logSums, 0);
        for (int i = 0; i < matrix.length; i++){
            double[] snuh = matrix[i].getArray();
            for (int j = 0; j < snuh.length; j++){
                sums[j] = sums[j] + ((snuh[j] - means[j]) * (snuh[j] - means[j]));
                logSums[j] = logSums[j] + ((snuh[j] - logMeans[j]) * (snuh[j] - logMeans[j]));
            }
        }
        for (int k = 0; k < stdevs.length; k++){
            stdevs[k] = Math.sqrt(sums[k] / matrix.length);
            logStDevs[k] = Math.sqrt(logSums[k] / matrix.length);
        }
                      
        //compute standardized values (if needed)
        double[][] zMatrix = new double[matrix.length][means.length];
        
        for (int i = 0; i < means.length; i++){
            boolean standardized = (Math.abs(means[i]) < tolerance) && (Math.abs(stdevs[i] - 1) < tolerance);
            for (int j = 0; j < matrix.length; j++){
                if (standardized){
                    zMatrix[j][i] = matrix[j].getArray()[i];
                } else {
                    zMatrix[j][i] = (matrix[j].getArray()[i] - logMeans[i])/logStDevs[i];
                }
            }
        }
        
        for (int k = 0; k < matrix.length; k++){
            ClassedArray ca = new ClassedArray();
            ca.setArray(zMatrix[k]);
            ca.setKlass(matrix[k].getKlass());
            z[k] = ca;
        }        
        
        SetClassifier classifier = new SetClassifier();
        SimpleKNN knn = new SimpleKNN();
        knn.setK(26);  //26 is the best value of k for GAM
        knn.setTrainingData(z);
        ShapeClassifier shape = new ShapeClassifier();
        shape.setClassifier(knn);
        shape.setMeans(means);
        shape.setStDevs(stdevs);
        shape.setIncludeArray(include);
        classifier.setShapeClassifier(shape);
        //didn't think the initialization would take 100 lines...  But the training data
        //and classifier should now be ready.
        
        int[] modes = new int[]{SetClassifier.MODE, SetClassifier.AVERAGE, SetClassifier.SIMPLE_DS, SetClassifier.PROP_BEL_TRANS, SetClassifier.COMBINED_AVERAGE};  //add MODIFIED_AVERAGE if/when it is completed.
        double[][] windClassifications = new double[modes.length][];
        
        String[] classNames = shape.getClassNames();        

        //read in the moment data
        File f2;
        String[][] momentValues;
        try{
            f2 = new File("C:/z4xNoSpaces/synthData/comparison/balanced/debugMoments.csv");
            FileReader in = new FileReader(f2);
            CSVParser csvp = new CSVParser(in);
            momentValues = csvp.getAllValues(); 
        }
        catch (FileNotFoundException fnfe){
            throw new RuntimeException("No file!  " + fnfe.getMessage());            
        }       
        catch (IOException ioe){
            throw new RuntimeException("IO Exception!  " + ioe.getMessage());
        }
        
        double[][] moments = new double[momentValues.length-1][momentValues[0].length];
        for (int i = 1; i < momentValues.length; i++){
            for (int j = 0; j < momentValues[i].length; j++){
                moments[i-1][j] = Double.parseDouble(momentValues[i][j]);
            }
        }

        //classify the set using each of the set classification methods
        for (int modeIndex = 0; modeIndex < modes.length; modeIndex++){
            int mode = modes[modeIndex];
            classifier.setMode(mode);
            windClassifications[modeIndex] = classifier.classifyMomentSet(moments);
            System.out.println("classification for mode " + mode + " is:");
            for (int t = 0; t < classNames.length; t++){
                System.out.print(classNames[t] + ",");
            }
            System.out.println("THETA");

            for (int t = 0; t < windClassifications[modeIndex].length; t++){
                System.out.print(windClassifications[modeIndex][t] + ",");
            }
            System.out.println();
        }      
    }
    
    private static void nonDebug(){
        //read in the training data
        File f;
        String[][] values;
        try{
            f = new File("C:/z4xNoSpaces/synthData/comparison/balanced/GAMMomentsNoZBigBalMerged.csv");
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            values = csvp.getAllValues(); 
        }
        catch (FileNotFoundException fnfe){
            throw new RuntimeException("No file!  " + fnfe.getMessage());            
        }       
        catch (IOException ioe){
            throw new RuntimeException("IO Exception!  " + ioe.getMessage());
        }
        
        //to see if leaving one or two invariants out improves classification (Auroop from ORNL's suggestion)
        boolean[] include = new boolean[]{true, true, true, true, true, true, true};
        int numInvsUsed = 0;
        for (int i = 0; i < include.length; i++){
            if (include[i]){
                numInvsUsed++;
            }
        }
        
        ClassedArray[] matrix = new ClassedArray[values.length-1];
        for (int i = 1; i < values.length; i++){
            double[] data = new double[numInvsUsed]; //was values[i].length - 1
            int index = 0;  //because I can't assume it will be data[j]--some values of j might be skipped by the include array
            for (int j = 0; j < values[i].length - 1; j++){
                if (include[j]){
                    data[index] = Double.parseDouble(values[i][j]);                
                    index++;
                }
            }
            ClassedArray ca = new ClassedArray();
            ca.setArray(data);
            ca.setKlass(values[i][values[i].length - 1]);  
            matrix[i-1] = ca;
        }                 
        
        ClassedArray[] z = new ClassedArray[matrix.length];
        double[] means = new double[matrix[0].getArray().length];
        double[] stdevs = new double[matrix[0].getArray().length];
//        double[] logMeans = new double[matrix[0].getArray().length];
//        double[] logStDevs = new double[matrix[0].getArray().length];               
        double tolerance = 0.001;  //if the mean is within tolerance of 0 and the standard deviation is within tolerance of 1, then assume it's already standardized.
        
        //compute means
        double[] sums = new double[means.length];
//        double[] logSums = new double[logMeans.length];
        for (int i = 0; i < matrix.length; i++){
            double[] snuh = matrix[i].getArray();
            for (int j = 0; j < snuh.length; j++){
                sums[j] = sums[j] + snuh[j];           
//                logSums[j] = logSums[j] + Math.log(Math.abs(snuh[j]));
            }
        }
        for (int k = 0; k < means.length; k++){
            means[k] = sums[k] / matrix.length;
//            logMeans[k] = logSums[k] / matrix.length;
        }
        
        //compute stdevs
        Arrays.fill(sums, 0);
//        Arrays.fill(logSums, 0);
        for (int i = 0; i < matrix.length; i++){
            double[] snuh = matrix[i].getArray();
            for (int j = 0; j < snuh.length; j++){
                sums[j] = sums[j] + ((snuh[j] - means[j]) * (snuh[j] - means[j]));
//                logSums[j] = logSums[j] + ((snuh[j] - logMeans[j]) * (snuh[j] - logMeans[j]));
            }
        }
        for (int k = 0; k < stdevs.length; k++){
            stdevs[k] = Math.sqrt(sums[k] / matrix.length);
//            logStDevs[k] = Math.sqrt(logSums[k] / matrix.length);
        }
                      
        //compute standardized values (if needed)
        double[][] zMatrix = new double[matrix.length][means.length];
        
        for (int i = 0; i < means.length; i++){
            boolean standardized = (Math.abs(means[i]) < tolerance) && (Math.abs(stdevs[i] - 1) < tolerance);
            for (int j = 0; j < matrix.length; j++){
                if (standardized){
                    zMatrix[j][i] = matrix[j].getArray()[i];
                } else {
                    zMatrix[j][i] = (matrix[j].getArray()[i] - means[i])/stdevs[i];
                }
            }
        }
        
        for (int k = 0; k < matrix.length; k++){
            ClassedArray ca = new ClassedArray();
            ca.setArray(zMatrix[k]);
            ca.setKlass(matrix[k].getKlass());
            z[k] = ca;
        }        
        
        SetClassifier classifier = new SetClassifier();
//        SimpleKNN knn = new SimpleKNN();
        DistanceWeightedKNN knn = new DistanceWeightedKNN();
        knn.setK(26);  //26 is the best value of k for GAM
        knn.setTrainingData(z);
        ShapeClassifier shape = new ShapeClassifier();
        shape.setClassifier(knn);
        shape.setMeans(means);
        shape.setStDevs(stdevs);
        shape.setIncludeArray(include);
        classifier.setShapeClassifier(shape);
        //didn't think the initialization would take 100 lines...  But the training data
        //and classifier should now be ready.
        
        int max = 32;
        int[] modes = new int[]{SetClassifier.MODE, SetClassifier.AVERAGE, SetClassifier.SIMPLE_DS, SetClassifier.PROP_BEL_TRANS, SetClassifier.COMBINED_AVERAGE, SetClassifier.MODIFIED_AVERAGE, SetClassifier.TREE_PBT};
        String[] modeNames = new String[]{"MODE", "AVERAGE", "SIMPLE_DS", "PROP_BEL_TRANS", "COMBINED_AVERAGE", "MODIFIED_AVERAGE", "TREE_PBT"};
        double[][][] infClassifications = new double[modes.length][max][];
        double[][][] roadClassifications = new double[modes.length][max][];
        double[][][] windClassifications = new double[modes.length][max][];
        
        String[] classNames = shape.getClassNames();        
        
        //iterate through this process until I feel like stopping
        for (int i = 0; i < max; i++){
            File folder = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i);
            folder.mkdir();
             
            //for each model run
            //(forty should be enough to capture the trends and still not take an eternity)
            double[][] infMoments = new double[40][];
            double[][] roadMoments = new double[40][];
            double[][] windMoments = new double[40][];
            
            MomentGenerator mg = new MomentGenerator();

            File momentFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/moments.csv");
            if (momentFile.exists()){
                try{
                    FileReader momentReader = new FileReader(momentFile);
                    CSVParser momentParser = new CSVParser(momentReader);
                    String[][] moments = momentParser.getAllValues();
                    boolean next = false;
                    int fileIndex = 1;  //1, not 0, because of the "inf" header in the first row of moments
                    int arrayIndex = 0;
                    while (!(next)){
                        String[] aRow = moments[fileIndex];
                        infMoments[arrayIndex] = new double[aRow.length];
                        for (int k = 0; k < aRow.length; k++){
                            infMoments[arrayIndex][k] = Double.parseDouble(aRow[k]);
                        }
                        fileIndex++;
                        arrayIndex++;
                        try{
                            Double.parseDouble(moments[fileIndex][0]);
                        } catch (NumberFormatException nfe){
                            //couldn't parse it, which means I'm done with the infMoments and on to the road
                            next = true;
                            arrayIndex = 0;
                            fileIndex++;
                        }
                    }
                    next = false;
                    while (!(next)){
                        String[] aRow = moments[fileIndex];
                        roadMoments[arrayIndex] = new double[aRow.length];
                        for (int k = 0; k < aRow.length; k++){
                            roadMoments[arrayIndex][k] = Double.parseDouble(aRow[k]);
                        }
                        fileIndex++;
                        arrayIndex++;
                        try{
                            Double.parseDouble(moments[fileIndex][0]);
                        } catch (NumberFormatException nfe){
                            //couldn't parse it, which means I'm done with the roadMoments and on to the wind
                            next = true;
                            arrayIndex = 0;
                            fileIndex++;
                        }
                    }                        
                    next = false;
                    while (!(next)){
                        String[] aRow = moments[fileIndex];
                        windMoments[arrayIndex] = new double[aRow.length];
                        for (int k = 0; k < aRow.length; k++){
                            windMoments[arrayIndex][k] = Double.parseDouble(aRow[k]);
                        }
                        fileIndex++;
                        arrayIndex++;
                        next = (fileIndex == moments.length);
                    }                         
                } catch (FileNotFoundException fnfe){
                    System.out.println("I already checked to see if the file exists, so you'd better darn well find it!  " + fnfe.getMessage());
                } catch (IOException ioe){
                    System.out.println("IOException reading moment file.  " + ioe.getMessage());
                }                                                           
            } else {  //momentFile.exists() = false  
                //generate one model run of each kind of process
                //I have 100+ GB of space on this machine, so save the data for now
                //generate infect if needed
                int minAcceptableLength = 1500;
                File testFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/inf75raster0.csv");                
                int infTimeSteps = 0;
                if (testFile.exists()){
                    testFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/inf75time" + infTimeSteps + ".csv");
                    while (testFile.exists()){
                        infTimeSteps = infTimeSteps + 5;
                        testFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/inf75time" + infTimeSteps + ".csv");
                    }
                    infTimeSteps = infTimeSteps - 5;
                } else {
                    while (infTimeSteps < minAcceptableLength){
                        infTimeSteps = generateInfectData(i);
                    }
                }
                
                //generate road if needed
                testFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/road75raster0.csv");                
                int roadTimeSteps = 0;
                if (testFile.exists()){
                    testFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/road75time" + roadTimeSteps + ".csv");
                    while (testFile.exists()){
                        roadTimeSteps = roadTimeSteps + 5;
                        testFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/road75time" + roadTimeSteps + ".csv");
                    }
                    roadTimeSteps = roadTimeSteps - 5;
                } else {
                    while (roadTimeSteps < minAcceptableLength){
                        roadTimeSteps = generateRoadData(i);
                    }
                }

                //generate wind
                testFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/wind75raster0.csv");                
                int windTimeSteps = 0;
                if (testFile.exists()){
                    testFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/wind75time" + windTimeSteps + ".csv");
                    while (testFile.exists()){
                        windTimeSteps = windTimeSteps + 5;
                        testFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/wind75time" + windTimeSteps + ".csv");
                    }
                    windTimeSteps = windTimeSteps - 5;
                } else {
                    while (windTimeSteps < minAcceptableLength){
                        windTimeSteps = generateWindData(i);
                    }
                }
                
                double infInterval = (double) infTimeSteps / 40d;                
                double roadInterval = (double) roadTimeSteps / 40d;
                double windInterval = (double) windTimeSteps / 40d;            
                
                for (int j = 0; j < 40; j++){            
                    int infStep = nearestMultipleOfFive((int) (Math.round(j * infInterval)));
                    File infFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/inf75time" + infStep + ".csv");
                    File infRasterFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/inf75raster" + infStep + ".csv");
                    float[][] infRaster;
                    if (infRasterFile.exists()){
                        infRaster = readRasterFile(infRasterFile);
                    } else {
                        infRaster = rasterFromFile(infFile);
                        writeRasterFile(infRasterFile, infRaster);
                    }
                    mg.setRaster(infRaster);
                    double[] temp = mg.calculateMomentInvariants();
                    infMoments[j] = new double[temp.length];
                    System.arraycopy(temp, 0, infMoments[j], 0, temp.length);
                    System.out.println("finished rasterizing infect run " + i + " time " + infStep);

                    int roadStep = nearestMultipleOfFive((int) (Math.round(j * roadInterval)));
                    File roadFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/road75time" + roadStep + ".csv");
                    File roadRasterFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/road75raster" + roadStep + ".csv");
                    float[][] roadRaster;
                    if (roadRasterFile.exists()){
                        roadRaster = readRasterFile(roadRasterFile);
                    } else {
                        roadRaster = rasterFromFile(roadFile);
                        writeRasterFile(roadRasterFile, roadRaster);
                    }
                    mg.setRaster(roadRaster);
                    temp = mg.calculateMomentInvariants();
                    roadMoments[j] = new double[temp.length];
                    System.arraycopy(temp, 0, roadMoments[j], 0, temp.length);              
                    System.out.println("finished rasterizing road run " + i + " time " + roadStep);

                    int windStep = nearestMultipleOfFive((int) (Math.round(j * windInterval)));
                    File windFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/wind75time" + windStep + ".csv");
                    File windRasterFile = new File("C:/z4xNoSpaces/synthData/comparison/balanced/run" + i + "/wind75raster" + windStep + ".csv");
                    float[][] windRaster;
                    if (windRasterFile.exists()){
                        windRaster = readRasterFile(windRasterFile);
                    } else {
                        windRaster = rasterFromFile(windFile);
                        writeRasterFile(windRasterFile, windRaster);
                    }
                    mg.setRaster(windRaster);
                    temp = mg.calculateMomentInvariants(); 
                    windMoments[j] = new double[temp.length];  
                    System.arraycopy(temp, 0, windMoments[j], 0, temp.length);
                    System.out.println("finished rasterizing wind run " + i + " time " + windStep);                     
                }
                    
                try{
                    FileWriter momentWriter = new FileWriter(momentFile);
                    momentWriter.write("inf" + '\n');
                    for (int k = 0; k < infMoments.length; k++){
                        momentWriter.write(""+infMoments[k][0]);
                        for (int l = 1; l < infMoments[k].length; l++){
                            momentWriter.write("," + infMoments[k][l]);
                        }
                        momentWriter.write('\n');
                    }

                    momentWriter.write("road" + '\n');
                    for (int k = 0; k < roadMoments.length; k++){
                        momentWriter.write(""+roadMoments[k][0]);
                        for (int l = 1; l < roadMoments[k].length; l++){
                            momentWriter.write("," + roadMoments[k][l]);
                        }
                        momentWriter.write('\n');
                    }       

                    momentWriter.write("wind" + '\n');
                    for (int k = 0; k < windMoments.length; k++){
                        momentWriter.write(""+windMoments[k][0]);
                        for (int l = 1; l < windMoments[k].length; l++){
                            momentWriter.write("," + windMoments[k][l]);
                        }
                        momentWriter.write('\n');
                    }              

                    momentWriter.close();
                } catch (IOException ioe){
                    System.out.println("IOException writing moment file.  " + ioe.getMessage());
                }                
            }
            
            //classify the set using each of the set classification methods
            for (int modeIndex = 0; modeIndex < modes.length; modeIndex++){
                int mode = modes[modeIndex];
                String name = modeNames[modeIndex];
                classifier.setMode(mode);
                infClassifications[modeIndex][i] = classifier.classifyMomentSet(infMoments);                
                roadClassifications[modeIndex][i] = classifier.classifyMomentSet(roadMoments);
                windClassifications[modeIndex][i] = classifier.classifyMomentSet(windMoments);

                System.out.println("inf classification for mode " + name + " is:");
                for (int t = 0; t < classNames.length; t++){
                    System.out.print(classNames[t] + ",");
                }
                System.out.println("THETA");
                
                for (int t = 0; t < infClassifications[modeIndex][i].length; t++){
                    System.out.print(infClassifications[modeIndex][i][t] + ",");
                }
                System.out.println();                
                
                System.out.println("road classification for mode " + name + " is:");
                for (int t = 0; t < classNames.length; t++){
                    System.out.print(classNames[t] + ",");
                }
                System.out.println("THETA");
                
                for (int t = 0; t < roadClassifications[modeIndex][i].length; t++){
                    System.out.print(roadClassifications[modeIndex][i][t] + ",");
                }
                System.out.println();                
                
                System.out.println("wind classification for mode " + name + " is:");
                for (int t = 0; t < classNames.length; t++){
                    System.out.print(classNames[t] + ",");
                }
                System.out.println("THETA");
                
                for (int t = 0; t < windClassifications[modeIndex][i].length; t++){
                    System.out.print(windClassifications[modeIndex][i][t] + ",");
                }
                System.out.println();
            }            
        }
        
        try{
            FileWriter infFile = new FileWriter("C:/z4xNoSpaces/synthData/comparison/balanced/infSummary.csv");            
            
            for (int k = 0; k < infClassifications.length; k++){
                for (int j = 0; j < infClassifications[k].length; j++){
                    infFile.write("mode " + modeNames[k] + ": track " + j + '\n');
                    for (int i = 0; i < classNames.length; i++){
                        infFile.write(classNames[i] + ", ");
                    } 
                    infFile.write("THETA" + '\n');
                    for (int i = 0; i < infClassifications[k][j].length; i++){
                        infFile.write(infClassifications[k][j][i] + ",");
                    }
                    infFile.write('\n');
                }    
                infFile.write('\n');
            }            
            infFile.close();
            System.out.println("wrote infSummary");
            
            FileWriter roadFile = new FileWriter("C:/z4xNoSpaces/synthData/comparison/balanced/roadSummary.csv");             
            for (int k = 0; k < roadClassifications.length; k++){
                for (int j = 0; j < roadClassifications[k].length; j++){
                    roadFile.write("mode " + modeNames[k] + ": track " + j + '\n');
                    for (int i = 0; i < classNames.length; i++){
                        roadFile.write(classNames[i] + ", ");
                    } 
                    roadFile.write("THETA" + '\n');
                    for (int i = 0; i < roadClassifications[k][j].length; i++){
                        roadFile.write(roadClassifications[k][j][i] + ",");
                    }
                    roadFile.write('\n');
                } 
                roadFile.write('\n');
            }            
            roadFile.close();
            System.out.println("wrote roadSummary");
            
            FileWriter windFile = new FileWriter("C:/z4xNoSpaces/synthData/comparison/balanced/windSummary.csv");             
            for (int k = 0; k < windClassifications.length; k++){
                for (int j = 0; j < windClassifications[k].length; j++){
                    windFile.write("mode " + modeNames[k] + ": track " + j + '\n');
                    for (int i = 0; i < classNames.length; i++){
                        windFile.write(classNames[i] + ", ");
                    } 
                    windFile.write("THETA" + '\n');
                    for (int i = 0; i < windClassifications[k][j].length; i++){
                        windFile.write(windClassifications[k][j][i] + ",");
                    }
                    windFile.write('\n');
                } 
                windFile.write('\n');
            }            
            windFile.close();
            System.out.println("wrote windSummary");
            
        } catch (IOException ioe){
            System.out.println("IOException when writing summaries: " + ioe.getMessage());
        }         
    }
    
    private static float[][] readRasterFile(File rasterFile){
        float[][] raster = new float[1][1];
        try{
            FileReader reader = new FileReader(rasterFile);
            CSVParser parser = new CSVParser(reader);
            String[][] rasterStrings = parser.getAllValues();  
            raster = new float[rasterStrings.length][rasterStrings[0].length-1];
            for (int i = 0; i < rasterStrings.length; i++){
                for (int j = 0; j < rasterStrings[i].length-1; j++){
                    raster[i][j] = Float.parseFloat(rasterStrings[i][j]);
                }
            }
        } catch (FileNotFoundException fnfe){
            System.out.println("Whaddya mean file not found?  I checked it's existence before calling this method!  " + fnfe.getMessage());
        } catch (IOException ioe){
            System.out.println("IOException reading raster file.  " + ioe.getMessage());
        }
        return raster;
    }
    
    private static void writeRasterFile(File rasterFile, float[][] raster){
        try{
            FileWriter writer = new FileWriter(rasterFile);             
            for (int i = 0; i < raster.length; i++){
                for (int j = 0; j < raster[i].length; j++){
                    writer.write(raster[i][j] + ",");
                }
                writer.write('\n');
            }            
            writer.close();        
        } catch (IOException ioe){
            System.out.println("IOException writing raster file:  " + ioe.getMessage());
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
    
    //the return value of this is the number of time steps in this track.
    //do this because I want to ensure that the track has enough time steps to be worth evaluating
    private static int generateInfectData(int index){
        int size = 75;
        int[][] susceptible = new int[size][size];
        int[][] infectious = new int[size][size];
        int[][] recovered = new int[size][size];
        int[][] dead = new int[size][size];
        //this assumes people recover or die after exactly 10 days.
        int[][][] recoverNow = new int[10][size][size];
        int totalInfected = 0;     
        int totalRecovered = 0;
        int totalDead = 0;
        
        //may want to tweak the rateOfInfection.  With a recovery time of 10, 
        //the theoretical reproductive rate is 10*rateOfInfection.  Because of how
        //I handle edge cases and cells where everyone's already infected, the actual
        //reproductive rate will be slightly smaller.
        double rateOfInfection = 0.105;
        double mortalityRate = 0.02;
        
        for (int i = 0; i < size; i++){
            Arrays.fill(susceptible[i], 1000);
            Arrays.fill(infectious[i], 0);
            Arrays.fill(recovered[i], 0);
            Arrays.fill(dead[i], 0);            
        }
        
        //seed with patient zero
        infectious[30][30]++;
        recoverNow[0][30][30]++;
        susceptible[30][30]--;
        totalInfected++;
        
        //loop through time steps
        int time = 0;
        while(totalInfected > 0){
            //if the time has been long enough, start recovering people.
            //this has to be a separate loop to avoid interference in the recoverNow variable.
            if (time > 9){
                for (int i = 0; i < size; i++){
                    for (int j = 0; j < size; j++){
                        for (int k = 0; k < recoverNow[time % 10][i][j]; k++){
                            infectious[i][j]--;
                            totalInfected--;        
                            double r = Math.random();
                            if (r < mortalityRate){
                                dead[i][j]++;
                                totalDead++;
                            } else {
                                recovered[i][j]++;
                                totalRecovered++;
                            }
                        }  //end for(int k...
                        recoverNow[time % 10][i][j] = 0;                        
                    }   //end for(int j...
                }
            }
            
            for (int i = 0; i < size; i++){
                for (int j = 0; j < size; j++){                                       
                    //for each infectious person, infect others, dividing the new cases between the current cell and the neighboring cells.
                    for (int k = 0; k < infectious[i][j]; k++){
                        double r = Math.random();
                        if (r < rateOfInfection){
                            //we have a winner!  another person gets infected.
                            r = Math.random();
//                            System.out.println("random " + r);
                            if (r < 0.5){
                                //half the time, infection is in same cell
                                if (susceptible[i][j] > 0){
                                    //can only start a new case here if someone's susceptible
//                                    System.out.println("infecting someone here");
                                    susceptible[i][j]--;
                                    infectious[i][j]++;
                                    totalInfected++;
                                    recoverNow[time % 10][i][j]++;
                                }
                            } else if (r < 0.625){
                                if (i+1 < size){
                                    if (susceptible[i+1][j] > 0){
                                        //can only start a new case here if someone's susceptible
//                                        System.out.println("infecting someone north");
                                        susceptible[i+1][j]--;
                                        infectious[i+1][j]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i+1][j]++;
                                    }               
                                }                 
                            } else if (r < 0.75){
                                if (i-1 >= 0){
                                    if (susceptible[i-1][j] > 0){
                                        //can only start a new case here if someone's susceptible
//                                        System.out.println("infecting someone south");
                                        susceptible[i-1][j]--;
                                        infectious[i-1][j]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i-1][j]++;
                                    }    
                                }
                            } else if (r < 0.875){
                                if (j+1 < size){
                                    if (susceptible[i][j+1] > 0){
                                        //can only start a new case here if someone's susceptible
//                                        System.out.println("infecting someone east");
                                        susceptible[i][j+1]--;
                                        infectious[i][j+1]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i][j+1]++;
                                    }  
                                }
                            } else {
                                if (j-1 >= 0){
                                    if (susceptible[i][j-1] > 0){
                                        //can only start a new case here if someone's susceptible
//                                        System.out.println("infecting someone west");
                                        susceptible[i][j-1]--;
                                        infectious[i][j-1]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i][j-1]++;
                                    }    
                                }
                            }
                        }  //end if (r < rateOfInfection)
                    }  //end for (int k = 0; k < infectious[i][j]                                        
                }  //end for (int j...
            }   //end for (int i...
            
            if (time % 5 == 0){
                try{
                    String outFileName = "inf" + size + "time" + time + ".csv";
                    FileWriter outFile = new FileWriter("C:/z4xNoSpaces/synthData/comparison/balanced/run" + index + "/" + outFileName);

                    outFile.write("x, y, pop, cases" + '\n');
                    for (int i = 0; i < size; i++){
                        for (int j = 0; j < size; j++){
                            int background = susceptible[i][j] + infectious[i][j] + recovered[i][j];
                            outFile.write(i + ", " + j + ", " + background + ", " + infectious[i][j] + '\n');
                        }
                    }

                    outFile.close();
                    System.out.println("wrote " + outFileName);
                } catch (IOException ioe){
                    System.out.println("IOException when saving file: " + ioe.getMessage());
                }  
                System.out.println("there are " + totalInfected + " infected.");
                System.out.println(totalRecovered + " have recovered.");
                System.out.println(totalDead + " have died.");
            }
            
            time++;
        }  //end while(totalInfected > 0)
        
        //return time-1 instead of time to reverse the last time++ call
        return time-1;
    }
      
    private static int generateRoadData(int index){
        //this is very similar to the infectious data generator, with the change that infections travel
        //faster along roads (e.g., mosquitoes in cars).  There are two highways at x=10 and x=65.
        //There are two orthogonal highways at y=10 and y=65.  Finally, there are smaller roads at x=37 and
        //y=37.
        int size = 75;
        int[][] susceptible = new int[size][size];
        int[][] infectious = new int[size][size];
        int[][] recovered = new int[size][size];
        int[][] dead = new int[size][size];
        //this assumes people recover or die after exactly 10 time steps.
        int[][][] recoverNow = new int[10][size][size];
        int totalInfected = 0;     
        int totalRecovered = 0;
        int totalDead = 0;
        int[] xRoads = new int[]{37};
        int[] yRoads = new int[]{37};
        int[] xHighways = new int[]{10, 65};
        int[] yHighways = new int[]{10, 65};
        
        //may want to tweak the rateOfInfection.  With a recovery time of 10, 
        //the theoretical reproductive rate is 10*rateOfInfection.  Because of how
        //I handle edge cases and cells where everyone's already infected, the actual
        //reproductive rate will be slightly smaller.  With the way I'm dealing with 
        //transportation routes, it will be considerably smaller (a little above half).
        double rateOfInfection = 0.20;
        double mortalityRate = 0.02;
        
        for (int i = 0; i < size; i++){
            Arrays.fill(susceptible[i], 1000);
            Arrays.fill(infectious[i], 0);
            Arrays.fill(recovered[i], 0);
            Arrays.fill(dead[i], 0);            
        }
        
        //seed with patient zero
        infectious[37][37]++;
        recoverNow[0][37][37]++;
        susceptible[37][37]--;
        totalInfected++;
        
        //loop through time steps
        int time = 0;
        while(totalInfected > 0){
            //if the time has been long enough, start recovering people.
            //this has to be a separate loop to avoid interference in the recoverNow variable.
            if (time > 9){
                for (int i = 0; i < size; i++){
                    for (int j = 0; j < size; j++){
                        for (int k = 0; k < recoverNow[time % 10][i][j]; k++){
                            infectious[i][j]--;
                            totalInfected--;        
                            double r = Math.random();
                            if (r < mortalityRate){
                                dead[i][j]++;
                                totalDead++;
                            } else {
                                recovered[i][j]++;
                                totalRecovered++;
                            }
                        }  //end for(int k...
                        recoverNow[time % 10][i][j] = 0;                        
                    }   //end for(int j...
                }
            }
            
            for (int i = 0; i < size; i++){
                for (int j = 0; j < size; j++){                                       
                    //for each infectious person, infect others, dividing the new cases between the current cell and the neighboring cells.
                    for (int k = 0; k < infectious[i][j]; k++){
                        double r = Math.random();
                        if (r < rateOfInfection){
                            //we may have a winner!    
                            //on the roads and highways, these are preferentially located along the transport routes.
                            boolean xRoad = onRoadX(i, xRoads);
                            boolean yRoad = onRoadY(j, yRoads);
                            boolean xHighway = onHighwayX(i, xHighways);
                            boolean yHighway = onHighwayY(j, yHighways);
                            double incXProb, decXProb, incYProb, decYProb;
                            double hereProb = 0.3;
                            if (xRoad){
                                if (yRoad){
                                    //at intersection of two roads, all directions equally likely, prob. of movement is 0.4
                                    incXProb = 0.10;
                                    decXProb = 0.10;
                                    incYProb = 0.10;
                                    decYProb = 0.10;
                                } else if (yHighway) {
                                    //intersection of highway and road, highway more likely, prob. of movement is 0.5
                                    //travel along Y highway changes X value and vice versa, so X is more likely to be changed along a Y highway
                                    incXProb = 0.15;
                                    decXProb = 0.15;
                                    incYProb = 0.10;
                                    decYProb = 0.10;                                        
                                } else {
                                    //just along a X road, prob. of movement is 0.3
                                    incXProb = 0.05;
                                    decXProb = 0.05;
                                    incYProb = 0.10;
                                    decYProb = 0.10;
                                }  
                            } else if (xHighway){
                                if (yRoad){
                                    //intersection of highway and road, highway more likely, prob. of movement is 0.5
                                    //travel along Y highway changes X value and vice versa, so X is more likely to be changed along a Y highway
                                    incXProb = 0.10;
                                    decXProb = 0.10;
                                    incYProb = 0.15;
                                    decYProb = 0.15;
                                } else if (yHighway) {
                                    //at intersection of two highways, all directions equally likely, prob. of movement is 0.6
                                    incXProb = 0.15;
                                    decXProb = 0.15;
                                    incYProb = 0.15;
                                    decYProb = 0.15;                                      
                                } else {
                                    //just along a X highway, prob. of movement is 0.4
                                    incXProb = 0.05;
                                    decXProb = 0.05;
                                    incYProb = 0.15;
                                    decYProb = 0.15;
                                }                                      
                            } else {
                                if (yRoad){
                                    //just a y Road, prob. of movement is 0.3
                                    incXProb = 0.10;
                                    decXProb = 0.10;
                                    incYProb = 0.05;
                                    decYProb = 0.05;                                        
                                } else if (yHighway){
                                    //just along a Y highway, prob. of movement is 0.4
                                    incXProb = 0.15;
                                    decXProb = 0.15;
                                    incYProb = 0.05;
                                    decYProb = 0.05;
                                } else {
                                    //no transportation route, all directions equally likely, prob. of movement is 0.2
                                    incXProb = 0.05;
                                    decXProb = 0.05;
                                    incYProb = 0.05;
                                    decYProb = 0.05;                                         
                                }
                            }
                            //now that I know what the probability of moving in each direction is...
                            r = Math.random();
                            if (r < incXProb){
                                //increment x
                                if (i+1 < size){
                                    if (susceptible[i+1][j] > 0){
                                        //can only start a new case here if someone's susceptible
                                        susceptible[i+1][j]--;
                                        infectious[i+1][j]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i+1][j]++;
                                    }               
                                }                                    
                            } else if (r < incXProb + decXProb){
                                //decrement x
                                if (i-1 >= 0){
                                    if (susceptible[i-1][j] > 0){
                                        //can only start a new case here if someone's susceptible
                                        susceptible[i-1][j]--;
                                        infectious[i-1][j]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i-1][j]++;
                                    }    
                                }
                            } else if (r < incXProb + decXProb + incYProb){
                                //increment y
                                if (j+1 < size){
                                    if (susceptible[i][j+1] > 0){
                                        //can only start a new case here if someone's susceptible
                                        susceptible[i][j+1]--;
                                        infectious[i][j+1]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i][j+1]++;
                                    }  
                                }
                            } else if (r < incXProb + decXProb + incYProb + decYProb){
                                //decrement y
                                if (j-1 >= 0){
                                    if (susceptible[i][j-1] > 0){
                                        //can only start a new case here if someone's susceptible
                                        susceptible[i][j-1]--;
                                        infectious[i][j-1]++;
                                        totalInfected++;
                                        recoverNow[time % 10][i][j-1]++;
                                    }    
                                }
                            } else if (r < incXProb + decXProb + incYProb + decYProb + hereProb){
                                //new case is here.
                                if (susceptible[i][j] > 0){
                                    //can only start a new case here if someone's susceptible
                                    susceptible[i][j]--;
                                    infectious[i][j]++;
                                    totalInfected++;
                                    recoverNow[time % 10][i][j]++;
                                }                                    
                            } else {
                                //no new case after all.  This is done so that the reproductive rate
                                //is higher along the transportation routes.  Without a higher RR there,
                                //the shape doesn't change much.  (Cases build up in isolated areas, and 
                                //this allows transmission about as fast as the roads with fewer cases.)
                            }   //end if (r < incXProb)...else...else...else...else...else         
                        }  //end if (r < rateOfInfection)
                    }  //end for (int k...
                }  //end for (int j...
            }   //end for (int i...
            
            if (time % 5 == 0){
                try{
                    String outFileName = "road" + size + "time" + time + ".csv";
                    FileWriter outFile = new FileWriter("C:/z4xNoSpaces/synthData/comparison/balanced/run" + index + "/" + outFileName);

                    outFile.write("x, y, pop, cases" + '\n');
                    for (int i = 0; i < size; i++){
                        for (int j = 0; j < size; j++){
                            int background = susceptible[i][j] + infectious[i][j] + recovered[i][j];
                            outFile.write(i + ", " + j + ", " + background + ", " + infectious[i][j] + '\n');
                        }
                    }

                    outFile.close();
                    System.out.println("wrote " + outFileName);
                } catch (IOException ioe){
                    System.out.println("IOException when saving file: " + ioe.getMessage());
                }   
                System.out.println("there are " + totalInfected + " infected.");
                System.out.println(totalRecovered + " have recovered.");
                System.out.println(totalDead + " have died.");
            }
            
            time++;
        }  //end while(totalInfected > 0)
                
        //return time-1 instead of time to reverse the last time++ call
        return time-1;        
    }
    
    private static boolean onRoadX(int x, int[] xRoads){
        boolean ret = false;
        for (int i = 0; i < xRoads.length; i++){
            ret = ret || (xRoads[i] == x);
        }
        return ret;
    }
    
    private static boolean onRoadY(int y, int[] yRoads){
        boolean ret = false;
        for (int i = 0; i < yRoads.length; i++){
            ret = ret || (yRoads[i] == y);
        }
        return ret;
    }    
    
    private static boolean onHighwayX(int x, int[] xHighways){
        boolean ret = false;
        for (int i = 0; i < xHighways.length; i++){
            ret = ret || (xHighways[i] == x);
        }
        return ret;
    }    
    
    private static boolean onHighwayY(int y, int[] yHighways){
        boolean ret = false;
        for (int i = 0; i < yHighways.length; i++){
            ret = ret || (yHighways[i] == y);
        }
        return ret;
    }        
    
    private static int generateWindData(int index){
        int size = 75;
        int initTotalPollution = 2500;
        int[][] concentration = new int[size][size];        
        double depositionProbability = 0.002;
        double motionProbability = 0.08;
        
        //assumptions here: start with all the pollution at a single source; wind constant
        //and strong in a single direction from the northwest; constant rate of deposition
        //(or rather, probability of a unit of pollution being deposited at each time step)
        
        for (int i = 0; i < size; i++){
            Arrays.fill(concentration[i], 0);
        }
        
        //start with all the pollution at a source at [10][10]
        concentration[10][10] = initTotalPollution;
        
        int[][] nextStepConcentration = new int[size][size];
                
        int time = 0;
        int airbornePollution = initTotalPollution;
        //for each time step
        while (airbornePollution > 0){
            //make a copy to store the next time step without interfering with the current one.
            for (int i = 0; i < size; i++){
                nextStepConcentration[i] = new int[size];
                System.arraycopy(concentration[i], 0, nextStepConcentration, 0, size);  //java 5
//               nextStepConcentration[i] = Arrays.copyOf(concentration[i], size);  //java 6
            }               
            for (int i = 0; i < size; i++){
                for (int j = 0; j < size; j++){
                    //for each particle at each location, it has a motionProbability chance of moving,
                    //a depositionProbability chance of being deposited, and if it moves, it is most likely
                    //but not guaranteed to move to the south or east.
                    for (int k = 0; k < concentration[i][j]; k++){
                        double r = Math.random();                        
                        if (r < depositionProbability){
                            //deposition is a simple decrement
                            nextStepConcentration[i][j]--;
                            airbornePollution--;
                        } else if (r < depositionProbability + motionProbability){
                            //motion, decrement from here and increment in one of the neighbors
                            nextStepConcentration[i][j]--;
                            double s = Math.random();
                            if (s < 0.05){
                                if (i > 0){
                                    nextStepConcentration[i-1][j]++;
                                } else {
                                    airbornePollution--;
                                }                                       
                            } else if (s < 0.1){
                                if (j > 0){
                                    nextStepConcentration[i][j-1]++;                                
                                } else {
                                    airbornePollution--;
                                } 
                            } else if (s < 0.55){
                                if (i < size-1){
                                    nextStepConcentration[i+1][j]++;
                                } else {
                                    airbornePollution--;
                                } 
                            } else {
                                if (j < size-1){
                                    nextStepConcentration[i][j+1]++;
                                } else {
                                    airbornePollution--;
                                } 
                            }
                        }  //end if (r < depositionProbability)
                    }  //end for (int k...
                }  //end for (int j...
            }  //end for (int i...   
            concentration = nextStepConcentration;
            if (time % 5 == 0){
                try{
                    String outFileName = "wind" + size + "time" + time + ".csv";
                    FileWriter outFile = new FileWriter("C:/z4xNoSpaces/synthData/comparison/balanced/run" + index + "/" + outFileName);

                    outFile.write("x, y, pop, cases" + '\n');
                    for (int i = 0; i < size; i++){
                        for (int j = 0; j < size; j++){
                            //background population is initTotalPollution, which is the maximum possible value for concentration[i][j]                    
                            outFile.write(i + ", " + j + ", " + initTotalPollution + ", " + concentration[i][j] + '\n');
                        }
                    }

                    outFile.close();
                    System.out.println("wrote " + outFileName);
                } catch (IOException ioe){
                    System.out.println("IOException when saving file: " + ioe.getMessage());
                } 
            }
            
            time++;
        }  //end while (airbornePollution > 0)                
        
        //return time-1 instead of time to reverse the last time++ call
        return time-1;  
    }                     
    
}
