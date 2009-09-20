/*
 * InfectionDataViewer.java
 *
 * Created on May 30, 2007, 2:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.dataGeneration;

import edu.psu.geovista.io.csv.CSVParser;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author z4x
 */
public class InfectionDataViewer {
    
    /** Creates a new instance of InfectionDataViewer */
    public InfectionDataViewer() {
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setVisible(true);
        frame.setPreferredSize(new Dimension(400, 425));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        int time = 0;
        String[][] values;
        boolean notDone = true;
        boolean scaleSize = true;
        while (notDone){
            try{
                File f = new File("D:/synthLandscan/road/run1/roadPAtime" + time + ".csv");
                FileReader in = new FileReader(f);
                CSVParser csvp = new CSVParser(in);
                values = csvp.getAllValues();
                
                frame.setTitle("Time " + time);
                Graphics g = frame.getContentPane().getGraphics();
                
                if (scaleSize){
                    g.clearRect(0, 0, 1000, 1000);
                    for (int i = 1; i < values.length; i++){
                        int x = 10 + 5*Integer.parseInt(values[i][0]);
                        int y = 10 + 5*Integer.parseInt(values[i][1]);
                        int cases = Integer.parseInt(values[i][3]);
                        if (cases == 0){
                            g.setColor(Color.LIGHT_GRAY);
                            g.fillOval(x-2, y-2, 4, 4);
                        }
                    }
                    for (int i = 1; i < values.length; i++){
                        int x = 10 + 5*Integer.parseInt(values[i][0]);
                        int y = 10 + 5*Integer.parseInt(values[i][1]);
                        int cases = Integer.parseInt(values[i][3]);
                        if (cases != 0){
                            int alpha = (int) Math.round(Math.min(255, 25 + cases/10d));
                            g.setColor(new Color(200, 0, 200, alpha));
                            int size = 4 + (int) Math.floor(cases / 100);
//                            int size = 4;
                            g.fillOval((int) Math.floor(x-(size/2)), (int) Math.floor(y-(size/2)), size, size);
                        }
                    }                    
                } else {
                    for (int i = 1; i < values.length; i++){
                        int x = 10 + 5*Integer.parseInt(values[i][0]);
                        int y = 10 + 5*Integer.parseInt(values[i][1]);
                        int cases = Integer.parseInt(values[i][3]);
                        
                        if (cases == 0){
                            g.setColor(Color.LIGHT_GRAY);
                        } else {
                            g.setColor(Color.MAGENTA.darker());
                        }
                        g.fillOval(x-2, y-2, 4, 4);
                    }                   
                }
                
                time = time + 5;
            } catch (FileNotFoundException fnfe){
                //done.
                notDone = false;
            } catch (IOException ioe){
                System.out.println("exception when loading file: " + ioe.getMessage());
            }
        }
    }
    
    
}
