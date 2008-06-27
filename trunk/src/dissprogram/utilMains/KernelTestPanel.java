/*
 * KernelTestPanel.java
 *
 * Created on September 26, 2006, 11:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;
import javax.swing.JPanel;

/**
 *
 * @author jfc173
 */
public class KernelTestPanel extends JPanel {
    
    float[][] arr;
    
    /** Creates a new instance of KernelTestPanel */
    public KernelTestPanel() {
    }
    
    public void setArray(float[][] dA){
        arr = dA;        
    }
    
    public void paintComponent(Graphics g){
        for(int x = 0; x < arr.length; x++){
            for (int y = 0; y < arr[x].length; y++){  
                int value = (int) Math.round(arr[x][y]);
                g.setColor(new Color(value, value, value));
                g.fillRect(x, y, 1, 1);  //surely there's a faster way to do this than pixel-by-pixel
            }
        }         
    }
    
}
