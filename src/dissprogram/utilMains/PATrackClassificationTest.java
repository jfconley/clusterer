/*
 * PATrackClassificationTest.java
 *
 * Created on February 28, 2008, 2:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import dissprogram.classification.ClassedArray;
import dissprogram.classification.DistanceWeightedKNN;
import dissprogram.classification.ShapeClassifier;
import dissprogram.classification.SimpleKNN;
import dissprogram.evidence.SetClassifier;
import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author jfc173
 */
public class PATrackClassificationTest {
    
    /** Creates a new instance of PATrackClassificationTest */
    public PATrackClassificationTest() {
    }
    
    public static void main(String[] args){
        String[] methods = {"base", "FZ", "GAM"};
        for (int k = 2; k < 302; k++) {
            for (int i = 0; i < methods.length; i++) {
                debug(methods[i], k);                
            } 
        }
    }
    
    private static void debug(String method, int kForKNN){
        //read in the training data
        File f;
        String[][] values;
        try{
            f = new File("D:/synthLandscan/momentData/fiveClassPACorrected/" + method + "testData.csv");
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
        SimpleKNN knn = new SimpleKNN();
//        DistanceWeightedKNN knn = new DistanceWeightedKNN();
        knn.setK(kForKNN);  
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
        String[] modeStrings = {"mode", "average", "simple DS", "prop bel trans", "comb. ave.", "modified ave.", "tree prop bel trans"};
        double[][] theseClassifications = new double[modes.length][];
        double[][][] allClassifications = new double[200][modes.length][];
        int allIndex = 0;
        String[] classNames = shape.getClassNames();
//        for (int i = 0; i < classNames.length; i++) {
//            System.out.println(classNames[i]);            
//        }
        //read in the moment data
        File f2;
        String[][] momentValues;
        String[] types = new String[]{"inf", "road", "wind", "river", "airport"};                
        for (int type = 0; type < types.length; type++){
            System.out.println("type " + types[type]);
            for (int run = 10; run < 50; run++){
                try{
                    f2 = new File("D:/synthLandscan/" + types[type] + "/run"  + run + "/" + method + "Moments.csv");
                    FileReader in = new FileReader(f2);
                    CSVParser csvp = new CSVParser(in);
                    momentValues = csvp.getAllValues();
                } catch (FileNotFoundException fnfe){
                    throw new RuntimeException("No file!  " + fnfe.getMessage());
                } catch (IOException ioe){
                    throw new RuntimeException("IO Exception!  " + ioe.getMessage());
                }
                
                double[][] moments = new double[momentValues.length][momentValues[0].length];
                for (int i = 0; i < momentValues.length; i++){
                    for (int j = 0; j < momentValues[i].length; j++){
                        moments[i][j] = Double.parseDouble(momentValues[i][j]);
                    }
                }
                
                //classify the set using each of the set classification methods
                for (int modeIndex = 0; modeIndex < modes.length; modeIndex++){
                    int mode = modes[modeIndex];
                    classifier.setMode(mode);
                    theseClassifications[modeIndex] = classifier.classifyMomentSet(moments);
//                    System.out.println("classification for mode " + mode + " is:");
//                    for (int t = 0; t < classNames.length; t++){
//                        System.out.print(classNames[t] + ",");
//                    }
//                    System.out.println("THETA");
//
//                    for (int t = 0; t < theseClassifications[modeIndex].length; t++){
//                        System.out.print(theseClassifications[modeIndex][t] + ",");
//                    }
//                    System.out.println();
                }
                for (int blob = 0; blob < theseClassifications.length; blob++){
                    allClassifications[allIndex][blob] = new double[theseClassifications[blob].length];
                    System.arraycopy(theseClassifications[blob], 0, allClassifications[allIndex][blob], 0, theseClassifications[blob].length);
                }
                allIndex++;
//                System.out.println("finished #" + allIndex);
            }
        }
        
        try{
            FileWriter outFile = new FileWriter("D:/synthLandscan/momentData/fiveClassPACorrected/" + method + "/simple" + method + "Classifications" + kForKNN + ".csv");
            
            outFile.write("mode, SIR, river, road, wind, airport, THETA" + '\n');
            for (int i = 0; i < allClassifications.length; i++){
                for (int j = 0; j < allClassifications[i].length; j++){
                    outFile.write("" + modeStrings[j]);
                    for (int k = 0; k < allClassifications[i][j].length; k++){
                        outFile.write("," + allClassifications[i][j][k]);
                    }
                    outFile.write('\n');
                }
            }
            
            outFile.close();
            System.out.println("wrote " + method + " classifications");
            
        } catch (IOException ioe){
            System.out.println("IOException when saving summary file: " + ioe.getMessage());
        }
        
    }
    
}
