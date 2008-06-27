/*
 * ImageBreaker.java
 *
 * Created on November 10, 2006, 1:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.image;

import dissprogram.*;
import dissprogram.operations.AverageFilter;
import jaistuff.algorithms.segmentation.regiongrowing.SimpleRegionGrowing;
import java.awt.Image;
import java.util.Arrays;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.RenderedOp;
import javax.media.jai.WritableRenderedImageAdapter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
//import javax.swing.JScrollPane;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.spatialschema.geometry.Envelope;

/**
 *
 * @author jfc173
 */
public class ImageBreaker {
    
    private GridCoverage2D image, segments;
    private GridCoverage2D[] clusters;
    private SimpleRegionGrowing segmenter;
    private float[][][] rasters;
    private boolean[] isBackground;
    private int[] pixelCounts;
    private boolean batch;
    
    /** Creates a new instance of ImageBreaker */
    public ImageBreaker() {
        batch = false;
    }
    
    public void setImage(GridCoverage2D coverage){
        image = coverage;        
    }
    
    public void setBatch(boolean b){
        batch = b;
    }
    
    public void isolateClusters(){
        //assuming a binary image for now, with black clusters on a white background.
        PlanarImage planar = new RenderedImageAdapter(image.getRenderedImage());
        segmenter = new SimpleRegionGrowing(planar, false);  //false can cause out of memory errors, but the filtering in true may be too coarse
        segmenter.run();
        int[][] segmentLabels = segmenter.getLabelMatrix();
        PlanarImage output = segmenter.getOutput();
        int numSegments = segmenter.getNumberOfRegions();        
        
        //I have no idea why I should flip the i and j between floats and segmentLabels, but it works this way.
        float[][] floats = new float[segmentLabels[0].length][segmentLabels.length];
        rasters = new float[numSegments][segmentLabels[0].length][segmentLabels.length];
        isBackground = new boolean[numSegments];
        boolean[] checked = new boolean[numSegments];
        pixelCounts = new int[numSegments];
        for (int l = 0; l < numSegments; l++){
            pixelCounts[l] = segmenter.getPixelCount(l+1);
        }
        Arrays.fill(checked, false);
        for (int i = 0; i < segmentLabels.length; i++){
            for (int j = 0; j < segmentLabels[0].length; j++){
                floats[j][i] = (float) segmentLabels[i][j];
                //One (or more) of these is the background.  Need to ignore it at some point in time.
                for (int k = 0; k < numSegments; k++){
                    if (floats[j][i] == (k+1)){
                        rasters[k][j][i] = 1;
                        if (!(checked[k])){
                            checked[k] = true;
//                            System.out.println("checking (" + i + ", " + j + ") and I get " + image.getRenderedImage().getData().getSampleFloat(i, j, 0));
                            if (image.getRenderedImage().getData().getSampleFloat(i, j, 0) == 0){
//                                System.out.println("background");
                                isBackground[k] = true;
                            } else {
                                isBackground[k] = false;
                            }
                        }
                    } else {                        
                        rasters[k][j][i] = 0;
                    }
                }
            }
        }
        
        //all this is for debugging
//        if (!(batch)){
//            Envelope bounds = new GeneralEnvelope(output.getBounds());
//            segments = DissUtils.matrixToCoverage(floats, bounds);
//
//            clusters = new GridCoverage2D[numSegments];
//            for (int i = 0; i < numSegments; i++){
//                clusters[i] = DissUtils.matrixToCoverage(rasters[i], bounds);
//            }
//        }
//        debug();

    }
    
    public GridCoverage2D[] getClusterImages(){
        return clusters;
    }
    
    public float[][][] getRasterArrays(){
        return rasters;
    }
    
    public boolean[] getIsBackground(){
        return isBackground;
    }
    
    public int[] getPixelCounts(){
        return pixelCounts;
    }
    
    private void debug(){
/*
        Image image = null;
        if (segments.getRenderedImage() instanceof WritableRenderedImageAdapter){
            image = ((WritableRenderedImageAdapter) segments.getRenderedImage()).getAsBufferedImage();
        } else if (segments.getRenderedImage() instanceof RenderedOp){
            image = ((RenderedOp) segments.getRenderedImage()).getAsBufferedImage();
        }
        JLabel imageLabel = new JLabel(new ImageIcon(image));         
        JFrame frame = new JFrame();
        frame.setTitle("All clusters");
        frame.getContentPane().add(imageLabel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
 */

/*        
        JLabel[] individualLabels = new JLabel[clusters.length];
        for (int i = 0; i < clusters.length; i++){
            Image next = null;
            if (clusters[i].getRenderedImage() instanceof WritableRenderedImageAdapter){
                next = ((WritableRenderedImageAdapter) clusters[i].getRenderedImage()).getAsBufferedImage();
            } else if (clusters[i].getRenderedImage() instanceof RenderedOp){
                next = ((RenderedOp) clusters[i].getRenderedImage()).getAsBufferedImage();
            }
            individualLabels[i] = new JLabel(new ImageIcon(next));             
        }
        
        JFrame individuals = new JFrame();
        individuals.setTitle("individual clusters.");
        JPanel images = new JPanel();
        for (int i = 0; i < clusters.length; i++){
            images.add(individualLabels[i]);
        }  
        JScrollPane scroll = new JScrollPane(images);
        individuals.getContentPane().add(scroll);
        individuals.setMaximumSize(new java.awt.Dimension(1000, 1000));
        individuals.pack();
        individuals.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        individuals.setVisible(true);
*/
        //this debugging part is from jaistuff.algorithms.segmentation.regiongrowing.DemoSimpleRegionGrowing.java        
        // Let's see some textual data about the segmentation.
        System.out.println("Number of regions: "+segmenter.getNumberOfRegions());
        for(int c=1;c<=segmenter.getNumberOfRegions();c++){
            System.out.println("Region "+c+": "+segmenter.getPixelCount(c)+" pixels");
        }
    }
    
}
