/*
 * RasterAggregator.java
 *
 * Created on February 16, 2008, 5:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;


import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author jfc173
 */
public class RasterAggregator {
    
    /** Creates a new instance of RasterAggregator */
    public RasterAggregator() {
    }
    
    public static void main (String[] args){
        try{
            File f = new File("E:/landscan/namerica06/export/namerfullcsv.csv");        
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            String[][] values = csvp.getAllValues();   
            
            int[][] aggregated = new int[84][84];
            for (int i = 0; i < values.length; i++){
                for (int j = 0; j < values[i].length; j++){
                    int x = (int) Math.floor(i/10);
                    int y = (int) Math.floor(j/10);
                    aggregated[x][y] = aggregated[x][y] + Integer.parseInt(values[i][j]);
                }
            }
            
            System.out.println("aggregated is " + aggregated.length + "x" + aggregated[0].length);           
            
            FileWriter outFile = new FileWriter("E:/landscan/namerica06/export/nameraggcsv.csv");
            
            for (int i = 0; i < aggregated.length; i++){
                for (int j = 0; j < aggregated[i].length; j++){                    
                    outFile.write("," + aggregated[i][j]);
                }
                outFile.write('\n');
            }
            
            outFile.close();            
        } catch (IOException ioe){
            System.out.println("IOException: " + ioe.getMessage());
        }
    }
    
}
