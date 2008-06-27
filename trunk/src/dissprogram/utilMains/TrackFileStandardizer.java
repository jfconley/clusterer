/*
 * TrackFileStandardizer.java
 *
 * Created on August 6, 2007, 10:07 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author z4x
 */
public class TrackFileStandardizer {
    
    /** Creates a new instance of TrackFileStandardizer */
    public TrackFileStandardizer() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File f;
        String[][] values;
        try{
            f = new File("C:/z4xNoSpaces/synthData/comparison/GAM/GAMMomentsNoZMerge3.csv");
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
        
        for (int i = 0; i < values.length; i=i+2){
            for (int j = 0; j < values[i].length; j++){
                System.out.print(values[i][j] + ",");
            }
            System.out.println();
        }
    }
    
}
