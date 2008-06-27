/*
 * KMLReader.java
 *
 * Created on October 18, 2007, 3:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 *
 * @author jfc173
 */
public class KMLReaderTest {
    
    /** Creates a new instance of KMLReaderTest */
    public KMLReaderTest() {
    }
    
    public static void main (String[] args){
        try{
            File f = new File("E:/OH_stuff/forArc/FatalCrashes2.kml");
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            boolean deleteMe = false;
            String outString = "";
            boolean notDone = true;
            while (notDone){
                try{
                    String s = fr.readLine();
                    if (s == null){
                        notDone = false;
                        System.out.println("null string");
                    }
                    
                    if ((s != null) && (s.contains("<description>"))){                    
                        deleteMe = true;                   
                    }
                    if (!(deleteMe)){
                        outString = outString + s + '\n';
                    }
                    if ((s != null) && (s.contains("</description>"))){ 
                        deleteMe = false;
                    }                    
                } catch (IOException ioe){
                    System.out.println("must be done");
                    notDone = false;
                }
            }
            
            //write out a data file 
            File outFile = new File("E:/OH_stuff/forArc/2005FatalCrashes3.kml");
            FileWriter outWriter = new FileWriter(outFile);
            outWriter.write(outString);
            outWriter.close();
        } catch (FileNotFoundException fnfe){
            System.out.println("whinge");
        } catch (IOException ioe){
            System.out.println("whinge again");
        }
    }
    
}
