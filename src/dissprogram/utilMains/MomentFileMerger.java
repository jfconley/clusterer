/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author jconley
 */
public class MomentFileMerger {

    public static void main(String[] args) {
        String[] types = {"inf", "river", "road", "wind", "airport"};
        String[] methods = {"GAM"};
        for (int h = 0; h < methods.length; h++){
            String method = methods[h];
            String out = "";            
            System.out.println("creating the file for " + method);
            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                for (int j = 0; j < 50; j++) {
                    File next = new File("D:/synthLandscan/randomStarts/" + type + "/run" + j + "/" + method + "Moments.csv");
                    try{
                        if (next.exists()){
                            FileReader reader = new FileReader(next);
                            CSVParser csvp = new CSVParser(reader);
                            String[][] values = csvp.getAllValues(); 
                            for (int x = 0; x < values.length; x++){
                                out = out + processLine(values[x], type);                           
                            }
                        } else { 
                            for (int x = 0; x < 40; x++){
                                next = new File("D:/synthLandscan/randomStarts/" + type + "/run" + j + "/" + method + "Moments" + x + ".csv");
                                FileReader reader = new FileReader(next);
                                CSVParser csvp = new CSVParser(reader);
                                String[][] values = csvp.getAllValues(); 
                                out = out + processLine(values[0], type);
                            }
                        }
                    } catch (FileNotFoundException fnfe){
                        System.out.println("File " + next.getAbsolutePath() + " apparently doesn't exist.");
                        System.out.println(fnfe.getMessage());
                    } catch (IOException ioe){
                        System.out.println("IOE: " + ioe.getMessage());
                    }               
                }   //for j <-- 0-9
            }   //for i <-- types
            System.out.println(out);
            try{
                File outFile = new File("D:/synthLandscan/randomStarts/momentData/" + method + "testData.csv");
                FileWriter momentWriter = new FileWriter(outFile);
                momentWriter.write("log inv 1, log inv 2, log inv 3, log inv 4, log inv 5, log inv 6, log inv 7, class" + '\n');
                momentWriter.write(out);
                momentWriter.close();
            } catch (IOException ioe){
                System.out.println("IOException writing moment file.  " + ioe.getMessage());
            } 
            
        }   //for h <-- methods
    }
    
    private static String processLine(String[] values, String type){
        if (values[5].equals("NaN")) {
            return "";
        }
        String ret = "";
        for (int i = 0; i < values.length; i++) {
            double d = Double.parseDouble(values[i]);
            if (d == 0){
                ret = ret + "-50,";
            } else {
                ret = ret + Math.log(Math.abs(d)) + ",";
            }
        }
        ret = ret + type + '\n';
        return ret;        
    }
    
}