/*
 * TreeAnalyzer.java
 *
 * Created on May 2, 2007, 4:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.image;

import dissprogram.DissUtils;
import dissprogram.classification.ShapeClassifier;
import dissprogram.evidence.BeliefFunction;
import dissprogram.evidence.DSCombine;
import dissprogram.evidence.DSTotalConflictException;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.geotools.coverage.grid.GridCoverage2D;

/**
 *
 * @author jfc173
 */
public class TreeAnalyzer {
    
    DefaultTreeModel model;
    GridCoverage2D[] binaries;
    float[][][] binaryRasters;
    private ShapeClassifier classifier;
    private DSCombine combine = new DSCombine();
    
    /** Creates a new instance of TreeAnalyzer */
    public TreeAnalyzer() {
    }
    
    public void setShapeClassifier(ShapeClassifier sc){
        classifier = sc;
    }
    
    public void setTree(DefaultTreeModel dtm){
        model = dtm;
    }
    
    public void setBinaryCoverages(GridCoverage2D[] bins){
        binaries = bins;
        binaryRasters = new float[binaries.length][][];
        for (int i = 0; i < binaries.length; i++){
            binaryRasters[i] = DissUtils.coverageToMatrix(binaries[i]);
            showMeTheImage(binaries[i], i);
        }
    }
    
    public ClassifiedRaster[] analyze(){
        Vector v = new Vector();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        //For each node, calculate each of the prime rasters, then evaluate the probabilities for them.
        //When all the prime rasters are created, DS them together and use that as the probabilities for the node.
        
        v = processNodeAndChildren(root, v);
        ClassifiedRaster[] ret = new ClassifiedRaster[v.size()];
        for (int i = 0; i < v.size(); i++){
            ret[i] = (ClassifiedRaster) v.get(i);
        }
        return ret;
    }
    
    private Vector processNodeAndChildren(DefaultMutableTreeNode node, Vector v){
        ClassifiedRaster cr = (ClassifiedRaster) node.getUserObject();
        combine.setClassNames(classifier.getClassNames());
        BeliefFunction belief = processThisNode(node, cr);
        cr.setBeliefFunction(belief);
        v.add(cr);
        node.setUserObject(cr);
        System.out.println("finished analyzing another node--" + cr.getTempString());
        System.out.println(cr.toString());
        System.out.println("It has " + node.getChildCount() + " children.");
        //this loop should only execute if the node has at least one child, thus providing a base case.
        for (int i = 0; i < node.getChildCount(); i++){
            v = processNodeAndChildren((DefaultMutableTreeNode) node.getChildAt(i), v);
        }                
        return v;
    }
    
    private BeliefFunction processThisNode(DefaultMutableTreeNode node, ClassifiedRaster cr){
        BeliefFunction bf = cr.getBeliefFunction();         
        String s = cr.getTempString().substring(0,1);
        int depth = 0;
        boolean isRoot = false;
        if (s.equalsIgnoreCase("r")){
            // the "r" comes from the word "root", so this is the root node of the tree.
            depth = -1;
            isRoot = true;
        } else {
            depth = Integer.parseInt(s);
        }
//        System.out.println(cr.getTempString() + " is supposedly at a depth of " + depth);
        float[][] myRaster = cr.getRaster();
        for (int i = depth + 1; i < binaries.length; i++){
            float[][] prime = generatePrimeRaster(myRaster, binaryRasters[i], isRoot);
            if (prime != null){
                classifier.setRaster(prime);
                String[] names = classifier.getClassNames();
                double[] classes = classifier.classifyProb();  
                BeliefFunction bf2 = new BeliefFunction(names);
                bf2.addSingletons(classes, names);                
                try{
                    bf = combine.combineShafer(bf, bf2);
                } catch (DSTotalConflictException dtce){
                    bf = new BeliefFunction(names);
                    double[] nans = new double[names.length];
                    Arrays.fill(nans, Double.NaN);
                    bf.addSingletons(nans, names);
                }
            
                cr.setBeliefFunction(bf);
            }
        }
        return bf;
    }
    
    private float[][] generatePrimeRaster(float[][] cluster, float[][] higherThresh, boolean isRoot){
        float sum = 0;
        float[][] ret = new float[cluster.length][cluster[0].length];
//        System.out.println("cluster dimensions: " + cluster.length + "x" + cluster[0].length);
//        System.out.println("higher thresh dims: " + higherThresh.length + "x" + higherThresh[0].length);
        //Occasionally, the dimensions of the higherThresh get flipped, when it isn't the root.
        //I have no idea why, but the swap variable will kludge around it.
        //I think I solved (or at least moved) it with an array index swap in DissUtils.matrixToCoverage
//        boolean swap = !(isRoot);
        
        float clusterSum = 0;  //only used for debugging
        float higherSum = 0;   //only used for debugging
        for (int i = 0; i < cluster.length; i++){
            for (int j = 0; j < cluster[i].length; j++){
//                if (swap){
//                    ret[i][j] = Math.min(cluster[i][j], higherThresh[j][i]);
//                    higherSum = higherSum + higherThresh[j][i];
//                } else {
                    ret[i][j] = Math.min(cluster[i][j], higherThresh[i][j]);
                    higherSum = higherSum + higherThresh[i][j];
//                }
                sum = sum + ret[i][j];
                clusterSum = clusterSum + cluster[i][j];
            }
        }
//        System.out.println("original cluster has " + clusterSum + " pixels");
//        System.out.println("higher threshold has " + higherSum + " pixels");
//        System.out.println("the prime raster has " + sum + " pixels");
        if (sum == 0){
            System.out.println("prime is null");
            return null;
        } else {
            System.out.println("prime is not null");
            return ret;
        }
    }    
    
    private static void showMeTheImage(GridCoverage2D gc, int i){
        java.awt.Image image = null;
        if (gc.getRenderedImage() instanceof javax.media.jai.WritableRenderedImageAdapter){
            image = ((javax.media.jai.WritableRenderedImageAdapter) gc.getRenderedImage()).getAsBufferedImage();
        } else if (gc.getRenderedImage() instanceof javax.media.jai.RenderedOp){
            image = ((javax.media.jai.RenderedOp) gc.getRenderedImage()).getAsBufferedImage();
        }
        javax.swing.JLabel imageLabel = new javax.swing.JLabel(new javax.swing.ImageIcon(image));         
        javax.swing.JFrame frame = new javax.swing.JFrame();
        frame.setTitle("binary #" + i);
        frame.getContentPane().add(imageLabel);
        frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);    
    }    
    
}
