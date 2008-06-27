/*
 * MomentFileCondenser.java
 *
 * Created on February 28, 2008, 1:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author jfc173
 */
public class MomentFileCondenser {
    
    /** Creates a new instance of MomentFileCondenser */
    public MomentFileCondenser() {
    }
    
    public static void main(String[] args){
        try{
            for (int i = 0; i < 199; i++){
                File f = new File("E:/synthLandscan/inf/run6/baseMoments" + i + ".csv");
                FileReader in = new FileReader(f);
                CSVParser csvp = new CSVParser(in);
                String[][] values = csvp.getAllValues(); 
                System.out.print(values[0][0]);
                for (int j = 1; j < values[0].length; j++){
                    System.out.print("," + values[0][j]);
                }
                System.out.println();
            }
        } catch (IOException ioe){
            System.out.println("IOE? " + ioe.getMessage());
        }
    }
    
}
