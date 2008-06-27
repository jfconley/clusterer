/*
 * EpanechnikovKernel.java
 *
 * Created on September 26, 2006, 11:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.image;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 *
 * @author jfc173
 */
public class EpanechnikovKernel {
    
    double height;
    double majorAxis;
    double minorAxis;
    double majorAxisOrientation;
    Point2D.Double centroid;
    Point2D.Double origin = new Point2D.Double(0, 0);
    
    /** Creates a new instance of EpanechnikovKernel */
    public EpanechnikovKernel() {
    }
    
    public void setHeight(double d){
        height = d;
    }
    
    public double getHeight(){
        return height;        
    }
    
    public void setMajorAxis(double d){
        majorAxis = d;
    }
    
    public double getMajorAxis(){
        return majorAxis;
    }
    
    public void setMinorAxis(double d){
        minorAxis = d;
    }
    
    public double getMinorAxis(){
        return minorAxis;
    }
    
    public void setMajorAxisOrientation(double d){
        majorAxisOrientation = d;
    }
    
    public double getMajorAxisOrientation(){
        return majorAxisOrientation;
    }
    
    public void setCentroid(Point2D.Double p){
        centroid = p;
    }
    
    public Point2D getCentroid(){
        return centroid;
    }
    
    public double getValueAt(Point2D.Double p){
        //it'll probably be easier to transform p to the situation of a 
        //circular kernel rather than make an elliptical kernel.
              
        //first, find p's coordinates relative to centroid
        Point2D.Double shifted = new Point2D.Double(p.getX() - centroid.getX(), 
                                                    p.getY() - centroid.getY());
        
        //rotate it so the major axis is north-south
        Point2D.Double rotated = new Point2D.Double();
        AffineTransform at = AffineTransform.getRotateInstance(0-Math.toRadians(majorAxisOrientation));
        at.transform(shifted, rotated);
        
        //move it away from the north-south axis as if minor and major axes were identical
        double yStretch = majorAxis/minorAxis;
        Point2D.Double stretched = new Point2D.Double(rotated.getX(), rotated.getY()*yStretch);
        
        //apply a circular Epanechnikov kernel of height height and radius majorAxis.
        double dist = stretched.distance(origin);
        double scale = (majorAxis - dist)/majorAxis;
        double k;
        if (scale > 0){
            k = height * (Math.sqrt(scale));
        } else {
            k = 0;
        }
//        if (k > 0){
//            System.out.println("k=" + k);
//        }
        return k; 
    }
    
}
