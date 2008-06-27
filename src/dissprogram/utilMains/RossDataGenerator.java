/*
 * RossDataGenerator.java
 *
 * Created on May 23, 2008, 10:38 AM
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
public class RossDataGenerator {
    
    /** Creates a new instance of RossDataGenerator */
    public RossDataGenerator() {
    }
    
    public static void main(String[] args){
        try{
            File summary = new File("C:/jconley/rawork/maciejewski.csv");
            FileReader in = new FileReader(summary);
            CSVParser csvp = new CSVParser(in);
            String[][] values = csvp.getAllValues();  
            
            File population = new File("C:/jconley/rawork/IndianaPop.csv");
            FileReader popIn = new FileReader(population);
            CSVParser csvp2 = new CSVParser(popIn);
            String[][] popValues = csvp2.getAllValues();
            
            int day = 0;
            int X = 2;
            int Y = 3;
            int POP = 1;
            for (int i = 0; i < 730; i++){
                String[][] outValues = new String[93][5];
                outValues[0] = new String[]{"lat","long","pop2000","total","infected"};
                for (int j = 1; j < 93; j++){
                    String name = values[i*92+j][1];
                    outValues[j][0] = "-" + find(name, popValues, X);
                    outValues[j][1] = "-" + find(name, popValues, Y);
                    outValues[j][2] = find(name, popValues, POP);
                    outValues[j][3] = values[i*92+j][2];
                    outValues[j][4] = values[i*92+j][3];
                }
                
                File momentFile = new File("C:/jconley/rawork/IndianaDays/day" + i + ".csv");
                FileWriter momentWriter = new FileWriter(momentFile);
                for (int k = 0; k < outValues.length; k++){
                    momentWriter.write(outValues[k][0]);
                    for (int l = 1; l < outValues[k].length; l++){
                        momentWriter.write("," + outValues[k][l]);
                    }
                    momentWriter.write('\n');
                }   
                momentWriter.close();                
            }
                    
        } catch (IOException ioe){
            System.out.println("what the? " + ioe.getMessage());
        }
    }
    
    private static String find(String name, String[][] popValues, int type){
        String ret = "";
        for (int i = 0; i < popValues.length; i++){
            if (popValues[i][0].startsWith(name)){
                ret = popValues[i][type];
            }
        }
        return ret;
    }
    
}
