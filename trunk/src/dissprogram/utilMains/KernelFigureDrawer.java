/*
 * KernelFigureDrawer.java
 *
 * Created on November 1, 2007, 4:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author jfc173
 */
public class KernelFigureDrawer {
    
    /** Creates a new instance of KernelFigureDrawer */
    public KernelFigureDrawer() {
    }
    
    public static void main(String[] args){
        KernelFigurePanel panel = new KernelFigurePanel();        
        JFrame f = new JFrame();
        f.getContentPane().add(panel);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    
}
