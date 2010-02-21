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
//        String[] methods = {/*"Affine",*/ "FullSluzek", "Li"};
        String[] methods = {""};
        String sluzekHeader = "";
        for (int i = 0; i < 31; i++) {
            sluzekHeader = sluzekHeader + "log I1(" + (i/10) + "), log I2 (" + (i/10) + "), log I8 (" + (i/10) + "), ";            
        }
        sluzekHeader = sluzekHeader + "class";
        String[] headers = {"log I1, log I2, log I3, log I4, class",
                            sluzekHeader,
                            "log M1, log M2, log M3, log M4, log M5, log M6, log M7, log M8, log M9, log M10,log M11, log M12, log M13, log M14, log M15, log M16, log M17, log M18, log M19, log M20, log M21, log M22, log M23, log M24, log M25, log M26, log M27, log M28, log M29, log M30, log M31, log M32, log M33, log M34, log M35, log M36, log M37, log M38, log M39, log M40, log M41, log M42, log M43, log M44, log M45, log M46, log M47, log M48, log M49, log M50, log M51, log M52, class"};
        for (int h = 0; h < methods.length; h++){
            String method = methods[h];          
            System.out.println("creating the file for " + method);
            try{
                File outFile = new File("D:/synthLandscan/randomStarts/momentData/GAM" + method + "testData.csv");
                FileWriter momentWriter = new FileWriter(outFile);
//                momentWriter.write("log inv 1, log inv 2, log inv 3, log inv 4, log inv 5, log inv 6, log inv 7, class" + '\n');
                momentWriter.write(headers[h] + '\n');
                for (int i = 0; i < types.length; i++) {
                    String type = types[i];
                    for (int j = 0; j < 200; j++) {
                        File next = new File("D:/synthLandscan/randomStarts/" + type + "/run" + j + "/GAM" + method + "Moments.csv");
                        try{
                            String nextBatch = "";
                            if (next.exists()){
                                FileReader reader = new FileReader(next);
                                CSVParser csvp = new CSVParser(reader);
                                String[][] values = csvp.getAllValues(); 
                                for (int x = 0; x < values.length; x++){
                                    nextBatch = nextBatch + processLine(values[x], type);                           
                                }
                            } else { 
                                for (int x = 0; x < 40; x++){
                                    next = new File("D:/synthLandscan/randomStarts/" + type + "/run" + j + "/GAM" + method + "Moments" + x + ".csv");
                                    FileReader reader = new FileReader(next);
                                    CSVParser csvp = new CSVParser(reader);
                                    String[][] values = csvp.getAllValues(); 
                                    nextBatch = nextBatch + processLine(values[0], type);
                                }
                            }
                            System.out.println(nextBatch);
                            momentWriter.write(nextBatch);
                        } catch (FileNotFoundException fnfe){
                            System.out.println("File " + next.getAbsolutePath() + " apparently doesn't exist.");
                            System.out.println(fnfe.getMessage());
                        } catch (IOException ioe){
                            System.out.println("IOE: " + ioe.getMessage());
                        } 
                    }   //for j <-- 0-200
                }   //for i <-- types
                momentWriter.close();
            } catch (IOException ioe){
                System.out.println("IOException writing moment file.  " + ioe.getMessage());
            } 
            
        }   //for h <-- methods
    }
    
    private static String processLine(String[] values, String type){
        if (values[0].equals("NaN")) {
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