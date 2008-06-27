/*
 * ImageTreeGenerator.java
 *
 * Created on May 2, 2007, 9:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.image;

import dissprogram.DissUtils;
import dissprogram.classification.ShapeClassifier;
import dissprogram.evidence.BeliefFunction;
import java.awt.RenderingHints;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.coverage.processing.Operations;
import org.opengis.coverage.processing.Operation;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author jfc173
 */
public class ImageTreeGenerator {
    
    private GridCoverage2D coverage;
    private GridCoverage2D[] binaries;
    private double[] thresholds;
    private AbstractProcessor def = DefaultProcessor.getInstance();        
    private ShapeClassifier classifier;
    
    /** Creates a new instance of ImageTreeGenerator */
    public ImageTreeGenerator() {
    }
    
    public void setCoverage(GridCoverage2D gc){
        coverage = gc;
    }
    
    public void setThresholds(double[] d){
        thresholds = d;
    }
    
    public GridCoverage2D[] getBinaries(){
        return binaries;
    }
    
    public void setShapeClassifier(ShapeClassifier sc){
        classifier = sc;
    }
    
    public void createBinaryCoverages(){
        Operations ops = new Operations(new RenderingHints(null));   
        binaries = new GridCoverage2D[thresholds.length];
        for (int i = 0; i < thresholds.length; i++){
            System.out.println("creating binary #" + i + " with threshold " + thresholds[i]);
            Operation binOp = def.getOperation("Binarize");
            ParameterValueGroup pvg = binOp.getParameters();

            GridCoverage2D band0 = (GridCoverage2D) ops.selectSampleDimension(coverage, new int[]{0});
            pvg.parameter("Source").setValue(band0);

            pvg.parameter("Threshold").setValue(new Double(thresholds[i]));
            binaries[i] = (GridCoverage2D) def.doOperation(pvg);              
        }        
    }
    
    public DefaultTreeModel createMomentTree(){
        createBinaryCoverages();
        float[][] raster = DissUtils.coverageToMatrix(binaries[0]);
        classifier.setRaster(raster);
        double[] probs = classifier.classifyProb();
        ClassifiedRaster cr = new ClassifiedRaster();
        cr.setRaster(raster);
        BeliefFunction bf = new BeliefFunction(classifier.getClassNames());
        bf.addSingletons(probs, classifier.getClassNames());
        cr.setBeliefFunction(bf);
        cr.setZInvs(classifier.getZInvs());
        cr.setTempString("root");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(cr);
        DefaultTreeModel model = new DefaultTreeModel(root);
        System.out.println("finished the root.");
        
        //now go and create direct children, grandchildren, etc.        
        for (int i = 0; i < binaries.length; i++){
            ImageBreaker ib = new ImageBreaker();
            System.out.println("working with binary # " + i);
            ib.setImage(binaries[i]);
            ib.isolateClusters();
            float[][][] rasters = ib.getRasterArrays();     
            boolean[] isBackground = ib.getIsBackground();
            GridCoverage2D[] segments = ib.getClusterImages();
            for (int j = 0; j < rasters.length; j++){
                System.out.println("raster # " + j);
                if (!(isBackground[j])){
                    classifier.setRaster(rasters[j]);
                    double[] childProbs = classifier.classifyProb();
                    ClassifiedRaster crx = new ClassifiedRaster();
                    crx.setRaster(rasters[j]);
                    BeliefFunction bfx = new BeliefFunction(classifier.getClassNames());
                    bfx.addSingletons(childProbs, classifier.getClassNames());
                    crx.setBeliefFunction(bfx);
                    crx.setZInvs(classifier.getZInvs()); 
                    crx.setTempString(i + "-" + j);
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(crx);
                    DefaultMutableTreeNode parent = findParent(node, model, root);
                    model.insertNodeInto(node, parent, parent.getChildCount());
                    System.out.println("added another node to the tree");
                }
            }            
        }

        return model;
    }
    
    private DefaultMutableTreeNode findParent(DefaultMutableTreeNode node, DefaultTreeModel model, DefaultMutableTreeNode check){
        DefaultMutableTreeNode ret = check;
        ClassifiedRaster crInNode = (ClassifiedRaster) node.getUserObject();
        
        if (check.isLeaf()){
            return check;
        }
        
        int kiddies = ret.getChildCount();
//        System.out.println(((ClassifiedRaster) ret.getUserObject()).getTempString() + " has " + kiddies + " children.");
        for (int i = 0; i < kiddies; i++){
            DefaultMutableTreeNode kid = (DefaultMutableTreeNode) check.getChildAt(i);
            ClassifiedRaster crInTree = (ClassifiedRaster) kid.getUserObject();
            if (crInTree.contains(crInNode)){
                ret = findParent(node, model, kid);
            }
        }       
//        System.out.println("found parent: " + ((ClassifiedRaster) ret.getUserObject()).getTempString());
        return ret;
    }

    
}
