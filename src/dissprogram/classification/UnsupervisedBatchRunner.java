/*
 * UnsupervisedBatchRunner.java
 *
 * Created on January 25, 2007, 1:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.classification;

import dissprogram.DissUtils;
import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author jfc173
 */
public class UnsupervisedBatchRunner {
    
    /** Creates a new instance of UnsupervisedBatchRunner */
    public UnsupervisedBatchRunner() {
    }
    
    public static void main(String[] args) {
        double[][] moments;
        String[][] values;
        try{
            File f = new File("C:/jconley/diss/moment_tests/batchMomentTests.txt");
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            values = csvp.getAllValues(); 
            //skip first row--headers
            //skip first three columns--cluster no., background? and pixel count
            moments = new double[values.length-1][7];
            for (int i = 1; i < values.length; i++){
                for (int j = 3; j < 10; j++){
                    moments[i-1][j-3] = Double.parseDouble(values[i][j]);
                }
            }
        }
        catch (FileNotFoundException fnfe){
            throw new RuntimeException("No file!  " + fnfe.getMessage());            
        }
        catch (IOException ioe){
            throw new RuntimeException("IO Exception!  " + ioe.getMessage());
        }    
        
        double[][] standardized = DissUtils.shiftToZScores(moments);
        KMeans classifier = new KMeans();
        int k = 10;
        classifier.setK(k);
        classifier.setMatrix(standardized);
        classifier.run();
        double[][] means = classifier.getMeans();
        for (int i = 0; i < means.length; i++){
            System.out.print("Mean " + i + ": (");
            for (int j = 0; j < means[i].length; j++){
                System.out.print(means[i][j] + ", ");
            }
            System.out.println(")");
        }
        int[] classes = classifier.getClassArray();
        int[] classCounts = new int[k];
        for (int i = 0; i < classes.length; i++){
            int klass = classes[i];
            classCounts[klass]++;
        }
        for (int i = 0; i < k; i++){
            System.out.println("class " + i + ": " + classCounts[i]);
        }
        for (int j = 0; j < moments.length; j++){
            System.out.print(j + ", ");
            for (int m = 0; m < moments[j].length; m++){
                System.out.print(", " + moments[j][m]);
            }         
            System.out.println(", " + classes[j]);
        }
    }    
    
}
