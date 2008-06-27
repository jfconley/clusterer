/*
 * Main.java
 *
 * Created on March 21, 2006, 11:11 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram;

import dissprogram.gui.SimpleGUI;
import javax.swing.JFrame;



/**
 *
 * @author jfc173
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SimpleGUI gui = new SimpleGUI();
        JFrame frame = new JFrame("test");
        frame.setPreferredSize(new java.awt.Dimension(1000, 1000));
        frame.getContentPane().add(gui);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
}
