/*
 * RossTrackClassifier.java
 *
 * Created on May 23, 2008, 12:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.DissUtils;
import dissprogram.classification.ClassedArray;
import dissprogram.classification.DistanceWeightedKNN;
import dissprogram.classification.ShapeClassifier;
import dissprogram.evidence.SetClassifier;
import dissprogram.image.ImageProcessor;
import dissprogram.image.MomentGenerator;
import edu.psu.geovista.gam.FitnessRelativePct;
import edu.psu.geovista.gam.InitGAMFile;
import edu.psu.geovista.gam.RandomGam;
import edu.psu.geovista.io.csv.CSVParser;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
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
public class RossTrackClassifier {
    
    /** Creates a new instance of RossTrackClassifier */
    public RossTrackClassifier() {
    }
    
    public static void main(String[] args){
        debug();
    }
    
    private static void debug(){
        //read in the training data
        File f;
        String[][] values;
        try{
            f = new File("E:/synthLandscan/momentData/baseTrainingLogNoZ.csv");
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            values = csvp.getAllValues();
        } catch (FileNotFoundException fnfe){
            throw new RuntimeException("No file!  " + fnfe.getMessage());
        } catch (IOException ioe){
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
        
        int[] modes = new int[]{SetClassifier.MODE, SetClassifier.AVERAGE, SetClassifier.SIMPLE_DS, SetClassifier.PROP_BEL_TRANS, SetClassifier.COMBINED_AVERAGE, SetClassifier.MODIFIED_AVERAGE, SetClassifier.TREE_PBT};
        double[][] classifications = new double[modes.length][];
        int allIndex = 0;
        String[] classNames = shape.getClassNames();
        
        //generate the moment data
        File f2;
        MomentGenerator mg = new MomentGenerator();

        double[][] moments = new double[730][7];
        for (int day = 0; day < 730; day++){
            f2 = new File("C:/jconley/rawork/IndianaDays/day" + day + ".csv");                
            float[][] raster = FZRasterFromFile(f2);
            mg.setRaster(raster);                
            moments[day] = mg.calculateMomentInvariants();
            System.out.println("finished day " + day + " invariants");
        }

        //classify the set using each of the set classification methods
        for (int modeIndex = 0; modeIndex < modes.length; modeIndex++){
            int mode = modes[modeIndex];
            classifier.setMode(mode);
            classifications[modeIndex] = classifier.classifyMomentSet(moments);
            System.out.println("classification for mode " + mode + " is:");
            for (int t = 0; t < classNames.length; t++){
                System.out.print(classNames[t] + ",");
            }
            System.out.println("THETA");

            for (int t = 0; t < classifications[modeIndex].length; t++){
                System.out.print(classifications[modeIndex][t] + ",");
            }
            System.out.println();
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
