/*
 * KernelFigurePanel.java
 *
 * Created on November 1, 2007, 5:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author jfc173
 */
public class KernelFigurePanel extends JPanel{
    
    /** Creates a new instance of KernelFigurePanel */
    public KernelFigurePanel(){        
    }
    
    public void paintComponent(Graphics g){
        int height1 = 33;
        int left1 = 158;
        int right1 = 158+125;
        
        int height2 = 100;
        int left2 = 242;
        int right2 = 242+83;
        
        int height3 = 50;
        int left3 = 250;
        int right3 = 250+100;   
        
        int base = 50;

        for (int i = 158; i < 351; i++){
            int value1 = valueAt(i, height1, left1, right1);
            int value2 = valueAt(i, height2, left2, right2);
            int value3 = valueAt(i, height3, left3, right3);
            
            if (value1 > 0){
                g.setColor(Color.BLUE);            
                g.drawLine(base + i, base, base + i, base + value1);
            }
            if (value2 > 0){
                g.setColor(Color.RED);
                g.drawLine(base + i, base + value1 + value3, base + i, base + value1 + value2 + value3);
            }
            if (value3 > 0){
                g.setColor(Color.GREEN);
                g.drawLine(base + i, base + value1, base + i, base + value1 + value3);
            }

            
        }        
    }    
    
    public static int valueAt(int x, int height, int left, int right){
        int radius = (right - left)/2;
        int middle = left + radius;
        double dist = Math.abs(middle - x);
        double scale = (radius - dist)/radius;
        double k;
        if (scale > 0){
            k = height * (Math.sqrt(scale));            
            System.out.println("dist = " + dist);
            System.out.println("scale = " + scale);
            System.out.println("k = " + k);
            System.out.println();
        } else {
            k = 0;
        } 
        return (int) Math.round(k);
    }    
    
}
