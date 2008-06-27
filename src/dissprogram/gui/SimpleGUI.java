/*
 * SimpleGUI.java
 *
 * Created on May 2, 2006, 2:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.gui;

import dissprogram.*;
import dissprogram.classification.ClassedArray;
import dissprogram.classification.DistanceWeightedKNN;
import dissprogram.classification.ShapeClassifier;
import dissprogram.classification.SimpleKNN;
import dissprogram.image.ClassifiedRaster;
import dissprogram.image.ImageBreaker;
import dissprogram.image.ImageProcessor;
import dissprogram.image.ImageTreeGenerator;
import dissprogram.image.TreeAnalyzer;
import dissprogram.operations.AverageFilter;
import edu.psu.geovista.gam.AbstractGam;
import edu.psu.geovista.gam.BesagNewellGAM;
import edu.psu.geovista.gam.CrossMidLine;
import edu.psu.geovista.gam.FitnessRelativePct;
import edu.psu.geovista.gam.GeneticGAM;
import edu.psu.geovista.gam.InitGAMFile;
import edu.psu.geovista.gam.MutateLinearAmount;
import edu.psu.geovista.gam.RandomGam;
import edu.psu.geovista.gam.RelocateDifference;
import edu.psu.geovista.gam.SelectRandomElite;
import edu.psu.geovista.gam.StopAtNGens;
import edu.psu.geovista.gam.SurviveEliteN;
import edu.psu.geovista.gam.SystematicGam;
import edu.psu.geovista.gam.TextFilter;
import edu.psu.geovista.io.csv.CSVParser;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import javax.media.jai.RenderedOp;
import javax.media.jai.WritableRenderedImageAdapter;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
public class SimpleGUI extends JPanel implements ActionListener, ChangeListener{
    
    JButton loadButton, goButton, undoButton, segmentButton, loadResultsButton, analyzeButton, loadCoverageButton;
    JComboBox methods, operations;
    JLabel imageLabel; 
    InitGAMFile initializer;
    AbstractGam gam;
    ImageProcessor ip;
    GridCoverage2D coverage;
    GridCoverage2D[] undo;
    JSlider thresholder;
    JPanel top;
    AbstractProcessor def = DefaultProcessor.getInstance(); 
    int floor;
    double scale;
    double[] means, stdevs;  //used in loading the training data for the shape classifier.  These probably should live elsewhere, but I can't think of a good place.
    ShapeClassifier classifier;
    
    /** Creates a new instance of SimpleGUI */
    public SimpleGUI() {
        loadButton = new JButton("LOAD DATA");
        loadButton.setActionCommand("LOAD");
        loadButton.addActionListener(this);
        
        loadCoverageButton = new JButton("LOAD RASTER");
        loadCoverageButton.setActionCommand("COVERAGE");
        loadCoverageButton.addActionListener(this);
        
        loadResultsButton = new JButton("LOAD GAM RESULTS");
        loadResultsButton.setActionCommand("RESULTS");
        loadResultsButton.addActionListener(this);
        
        methods = new JComboBox(new String[]{"Systematic", "Besag-Newell", "Random", "Genetic"});
        methods.setActionCommand("METHOD");
        methods.addActionListener(this);
        
        goButton = new JButton("GO");
        goButton.setActionCommand("GO");
        goButton.addActionListener(this);
        
        operations = new JComboBox(new String[]{"exp", "log", "binarize", "gradient magnitude", "abs value", "no data filter", "smooth"});
        operations.setActionCommand("OPERATION");
        operations.addActionListener(this);
        
        undoButton = new JButton("UNDO LAST OP");
        undoButton.setActionCommand("UNDO");
        undoButton.addActionListener(this);
        undoButton.setEnabled(false);
        
        segmentButton = new JButton("SEGMENT");
        segmentButton.setActionCommand("SEGMENT");
        segmentButton.addActionListener(this);
        segmentButton.setEnabled(false);
        
        analyzeButton = new JButton("ANALYZE");
        analyzeButton.setActionCommand("ANALYZE");
        analyzeButton.addActionListener(this);
        analyzeButton.setEnabled(false);
        
        thresholder = new JSlider();
        thresholder.setMinimum(0);
        thresholder.setMaximum(255);
        thresholder.setValue(170);
        thresholder.setMajorTickSpacing(50);
        thresholder.setMinorTickSpacing(10);
        thresholder.setPaintTicks(true);
        thresholder.setVisible(false);
        thresholder.addChangeListener(this);
        
        top = new JPanel();
        top.add(loadButton);
        top.add(loadCoverageButton);
        top.add(goButton);
        top.add(methods);
        top.add(loadResultsButton);
        top.add(operations);
        top.add(undoButton);
        top.add(segmentButton);
        top.add(analyzeButton);
        top.add(thresholder);
        this.setLayout(new BorderLayout());
        this.add(top, BorderLayout.NORTH);             
        
        ip = new ImageProcessor();
        undo = new GridCoverage2D[5];
        initShapeClassifier();
    }
    
    private void initShapeClassifier(){
        classifier = new ShapeClassifier();
//        DistanceWeightedKNN knn = new DistanceWeightedKNN();
        SimpleKNN knn = new SimpleKNN();
        knn.setK(35);  //35 is one of the better k values from the leave-one-out validation
        //somehow get the training data in (read from a file, dummy)
        //read in file
        File f;
        String[][] values;
        try{
//            f = new File("E:/endOfSummer/momentData/logAggVPSNoZ.csv");  //for road-river-soil-urban
            f = new File("E:/endOfSummer/synthData/comparison/balanced/GAMMomentsNoZBalMerged.csv");  //for infect-road-wind
            FileReader in = new FileReader(f);
            CSVParser csvp = new CSVParser(in);
            values = csvp.getAllValues();             
        }
        catch (FileNotFoundException fnfe){
            throw new RuntimeException("No file!  " + fnfe.getMessage());            
        }       
        catch (IOException ioe){
            throw new RuntimeException("IO Exception!  " + ioe.getMessage());
        }
        
        //create classedArray[]
        //to see if leaving one or two invariants out improves classification (Auroop from ORNL's suggestion)
        boolean[] include = new boolean[]{true, true, true, true, true, true, true};
        int numInvsUsed = 7;  //this int must be the number of true's in the include array.
        
        ClassedArray[] matrix = new ClassedArray[values.length-1];
        for (int i = 1; i < values.length; i++){
            double[] data = new double[numInvsUsed]; //was values[i].length - 1
            int index = 0;  //because I can't assume it will be data[j]--some values of j might be skipped by the include array
            for (int j = 0; j < values[i].length - 1; j++){
                if (include[j]){
                    data[index] = Double.parseDouble(values[i][j]);                
                    index++;
                }
            }
            ClassedArray ca = new ClassedArray();
            ca.setArray(data);
            ca.setKlass(values[i][values[i].length - 1]);  
            matrix[i-1] = ca;
        }         
        matrix = standardizeMatrix(matrix);
        
        knn.setTrainingData(matrix);        
        classifier.setClassifier(knn);
        classifier.setMeans(means);
        classifier.setStDevs(stdevs);
        classifier.setIncludeArray(include);
    }
    
    public void actionPerformed(ActionEvent e){
        String command = e.getActionCommand();
        if (command.equals("LOAD")){
            loadPointsAction();
        } else if (command.equals("METHOD")){
            setMethodAction();
        } else if (command.equals("GO")){
            runGamAction();
        } else if (command.equals("OPERATION")){
            operationAction();
        } else if (command.equals("UNDO")){
            undoAction();
        } else if (command.equals("SEGMENT")){
            segmentAction();
        } else if (command.equals("RESULTS")){
            loadResultsAction();
        } else if (command.equals("ANALYZE")){
            analyzeAction();
        } else if (command.equals("COVERAGE")){
            loadCoverageAction();           
        } else {
            System.out.println("snuh?" + command);
        }
    }
    
    private void loadPointsAction(){
        JFileChooser jfc = new JFileChooser();
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileFilter(new TextFilter());
        try{
            int returnVal = jfc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                File f = jfc.getSelectedFile();
                initializer = new InitGAMFile();
                initializer.processTextFile(f);                
            }
        }
        catch (FileNotFoundException fnfe){
            JOptionPane.showMessageDialog(this,
                                          fnfe.getMessage(),
                                          "File Not Found Exception",
                                          JOptionPane.ERROR_MESSAGE);                
        }
        catch (IOException ioe){
            JOptionPane.showMessageDialog(this,
                                          ioe.getMessage(),
                                          "I/O Exception",
                                          JOptionPane.ERROR_MESSAGE);                
        }        
    }
    
    private void setMethodAction(){
        String s = (String) methods.getSelectedItem();   
        double largeDimension = Math.max(initializer.getMaxX() - initializer.getMinX(),
                                         initializer.getMaxY() - initializer.getMinY());            
        if (s.equals("Systematic")){
            gam = new SystematicGam();
        } else if (s.equals("Besag-Newell")){
            gam = new BesagNewellGAM();
        } else if (s.equals("Random")){
            gam = new RandomGam();
            ((RandomGam) gam).setNumTests(5000);
        } else if (s.equals("Genetic")){
            //Whee!  A lot of parameters.  These values work reasonably well, in my experience.                
            gam = new GeneticGAM();
            StopAtNGens halt = new StopAtNGens(75);
            ((GeneticGAM) gam).setHaltCondition(halt);
            ((GeneticGAM) gam).setPopSize(100);
            ((GeneticGAM) gam).setProbMut(0.05);
            ((GeneticGAM) gam).setRandomGenes(0);
            ((GeneticGAM) gam).setFirstAdd(4);
            ((GeneticGAM) gam).setUpdateOften(2);
            ((GeneticGAM) gam).setSelectPairs(false);
            ((GeneticGAM) gam).setBannedList(true);
            ((GeneticGAM) gam).setSolutionList(false);
            ((GeneticGAM) gam).setAntiConvergence(true);
            SelectRandomElite selection = new SelectRandomElite(0.2);
            ((GeneticGAM) gam).setSelectMethod(selection);
            SurviveEliteN survive = new SurviveEliteN(0.2);
            ((GeneticGAM) gam).setSurviveMethod(survive);
            CrossMidLine crossover = new CrossMidLine();
            ((GeneticGAM) gam).setCrossoverMethod(crossover);
            MutateLinearAmount mutation = new MutateLinearAmount(0.05 * largeDimension);
            ((GeneticGAM) gam).setMutationMethod(mutation);
            RelocateDifference relocation = new RelocateDifference(3, 1.5);
            ((GeneticGAM) gam).setRelocationMethod(relocation);
        }
        gam.setMaxRadius(0.05 * largeDimension);
        gam.setMinRadius(0.005 * largeDimension);
        gam.setMinPoints(3);
        gam.setMinAccepted(1.2);
        gam.setFitnessFunction(new FitnessRelativePct(initializer.getDataSet()));  
        gam.setInitializer(initializer);          
    }
    
    private void runGamAction(){
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        ip.setInitializer(initializer);
        ip.setGam(gam);
        coverage = ip.generateCoverage();
        replaceImage();
        analyzeButton.setEnabled(true);
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));        
    }
    
    private void operationAction(){
        String s = (String) operations.getSelectedItem();
        try{
            if (thresholder.isVisible()){
                thresholder.setVisible(false);
            }
            addToUndoList();
            undoButton.setEnabled(true);
            if (coverage != null){
                Operations ops = new Operations(new RenderingHints(null));
                if (s.equals("exp")){
                    segmentButton.setEnabled(false);
                    GridCoverage2D exp = (GridCoverage2D) ops.exp(coverage);
                    coverage = exp;
                } else if (s.equals("binarize")){
                    try{   
                        segmentButton.setEnabled(true);
                        int ceiling = (int) Math.ceil(coverage.getSampleDimension(0).getMaximumValue());
                        floor = (int) Math.floor(coverage.getSampleDimension(0).getMinimumValue());
//                        floor = (int) Math.floor(ip.getMinHeight());
//                        int ceiling = (int) Math.ceil(ip.getMaxHeight());
                        scale = (double) 256 / (ceiling - floor);
                        int increment = (Math.max(1, (int) Math.round((ceiling - floor) / 10)));

                        System.out.println("floor = " + floor);
                        System.out.println("ceiling = " + ceiling);
                        System.out.println("scale = " + scale);
                        System.out.println("increment = " + increment);

                        thresholder.setMinimum(floor);
                        thresholder.setMaximum(ceiling);

                        thresholder.setVisible(true);
                        thresholder.setLabelTable(thresholder.createStandardLabels(increment, floor));
                        thresholder.setPaintLabels(true);
                        thresholder.setMajorTickSpacing(increment);
                        thresholder.setValue(floor + (ceiling - floor)/2);

                        Operation binOp = def.getOperation("Binarize");
                        ParameterValueGroup pvg = binOp.getParameters();

                        GridCoverage2D band0 = (GridCoverage2D) ops.selectSampleDimension(coverage, new int[]{0});
                        pvg.parameter("Source").setValue(band0);

                        pvg.parameter("Threshold").setValue(new Double(floor + (ceiling - floor)/2));
                        GridCoverage2D binarized = (GridCoverage2D) def.doOperation(pvg);  
                        coverage = binarized;
                    } catch (org.opengis.parameter.ParameterNotFoundException cause){
                        throw new RuntimeException("No threshold parameter: " + cause);
                    }
                } else if (s.equals("log")){
                    segmentButton.setEnabled(false);
                    GridCoverage2D log = (GridCoverage2D) ops.log(coverage);
                    coverage = log;  
//                    } else if (s.equals("invert")){
//                        GridCoverage2D inv = (GridCoverage2D) ops.invert(coverage);
//                        coverage = inv;      
                } else if (s.equals("abs value")){
                    segmentButton.setEnabled(false);
                    GridCoverage2D abs = (GridCoverage2D) ops.absolute(coverage);
                    coverage = abs;
                } else if (s.equals("no data filter")){
                    segmentButton.setEnabled(false);
                    GridCoverage2D no = (GridCoverage2D) ops.nodataFilter(coverage);
                    coverage = no;                        
                } else if (s.equals("gradient magnitude")){
                    segmentButton.setEnabled(false);
                    GridCoverage2D gradient = (GridCoverage2D) ops.gradientMagnitude(coverage);
                    coverage = gradient;
                } else if (s.equals("smooth")){                    
                    AverageFilter filter = new AverageFilter();
                    filter.setType(AverageFilter.MEDIAN);
                    if (filter.getType() == AverageFilter.MEAN){
                        segmentButton.setEnabled(false);                        
                    }
                    GridCoverage2D filtered = filter.filter(coverage);
                    coverage = filtered;
                }
                replaceImage();
            }   
        } catch (IllegalArgumentException iae){
            JOptionPane.showMessageDialog(this,
                                          iae.getMessage(),
                                          "Illegal Argument Exception",
                                          JOptionPane.ERROR_MESSAGE); 
        }            
    }
    
    private void undoAction(){
        segmentButton.setEnabled(false);
        if (undo[4] != null){
            coverage = undo[4];
            undo[4] = null;
        } else if (undo[3] != null){
            coverage = undo[3];
            undo[3] = null;
        } else if (undo[2] != null){
            coverage = undo[2];
            undo[2] = null;
        } else if (undo[1] != null){
            coverage = undo[1];
            undo[1] = null;
        } else if (undo[0] != null){
            coverage = undo[0];
            undo[0] = null;
            undoButton.setEnabled(false);
        } else {
            System.out.println("whinge horribly.");
        }
        replaceImage();         
    }
    
    private void segmentAction(){
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        ImageBreaker ib = new ImageBreaker();
        ib.setImage(coverage);
        ib.isolateClusters();

        float[][][] rasters = ib.getRasterArrays();      
        boolean[] isBackground = ib.getIsBackground();
        String[] classes = new String[rasters.length];
        double[][] classProbs = new double[rasters.length][];
        for (int i = 0; i < rasters.length; i++){
            if (!(isBackground[i])){
                classifier.setRaster(rasters[i]);
                classes[i] = classifier.classify();
                classProbs[i] = classifier.classifyProb();
            } else {
                classes[i] = "background";
                classProbs[i] = new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
            }
        }

        String[] classNames = classifier.getClassNames();            
        for (int j = 0; j < rasters.length; j++){
            if (ib.getPixelCounts()[j] > 25){
                System.out.println();
                System.out.println("Cluster " + j + "(size = " + ib.getPixelCounts()[j] + ")");
                System.out.println("Winner take all class: " + classes[j]);
                System.out.println("Probabilities:");
                for (int k = 0; k < classNames.length; k++){
                    System.out.print(classNames[k] + ",");
                }
                System.out.println();
                for (int l = 0; l < classProbs[j].length; l++){
                    System.out.print(classProbs[j][l] + ",");
                }
                System.out.println();
            }
        }
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));        
    }
    
    private void loadResultsAction(){
        JFileChooser jfc = new JFileChooser();
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileFilter(new TextFilter());

        int returnVal = jfc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION){
            File f = jfc.getSelectedFile();
            coverage = ip.generateCoverageFromFile(f);
            replaceImage();                    
        }
        analyzeButton.setEnabled(true);

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));        
    }
    
    private void analyzeAction(){
        ImageTreeGenerator itg = new ImageTreeGenerator();
        itg.setCoverage(coverage);
        int max = (int) Math.ceil(coverage.getSampleDimension(0).getMaximumValue());
        int min = (int) Math.floor(coverage.getSampleDimension(0).getMinimumValue());
        double increment = (double) (max - min) / 5d;
        double[] thresholds = new double[5];
        for (int i = 0; i < 5; i++){
            thresholds[i] = min + i * increment;
        }
        itg.setThresholds(thresholds);
        itg.setShapeClassifier(classifier);
        DefaultTreeModel tree = itg.createMomentTree();
        TreeAnalyzer analyzer = new TreeAnalyzer();
        analyzer.setTree(tree);
        analyzer.setBinaryCoverages(itg.getBinaries());
        analyzer.setShapeClassifier(classifier);
        ClassifiedRaster[] rasters = analyzer.analyze();  //this method also changes the user objects of the tree.  Just so you know.
        for (int i = 0; i < rasters.length; i++){
            System.out.println(rasters[i].toFileString());
        }
        
        //how about displaying the tree in a tree viewer?  Need to put a sensible toString() method into ClassifiedRaster, then.
        JTree jtree = new JTree(tree);
        JFrame frame = new JFrame();
        frame.setPreferredSize(new java.awt.Dimension(400, 200));
        frame.getContentPane().add(new JScrollPane(jtree));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);        
    }
    
    private void loadCoverageAction(){
        JFileChooser jfc = new JFileChooser();
        jfc.setMultiSelectionEnabled(false);
        try{
            int returnVal = jfc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                File f = jfc.getSelectedFile();
                org.geotools.gce.arcgrid.ArcGridReader agr = new org.geotools.gce.arcgrid.ArcGridReader(f);
                org.opengis.coverage.grid.GridCoverage gc = agr.read(null);
                System.out.println("It worked!!!  I've got a coverage!!!");
                
//                org.opengis.coverage.grid.GridCoverageExchange gce = new org.geotools.data.coverage.grid.stream.StreamGridCoverageExchange();
//                org.opengis.coverage.grid.GridCoverageReader reader = gce.getReader(f.toURI().toURL());
//                org.opengis.coverage.grid.Format format = reader.getFormat();
//                org.opengis.parameter.ParameterValueGroup params = format.getReadParameters();
//                java.util.Iterator whatDoYouHave = params.values().iterator();
//                org.opengis.parameter.GeneralParameterValue[] values = new org.opengis.parameter.GeneralParameterValue[params.values().size()];
//                int index = 0;
//                while (whatDoYouHave.hasNext()){
//                   org.opengis.parameter.GeneralParameterValue next = (org.opengis.parameter.GeneralParameterValue) whatDoYouHave.next();
//                   System.out.println(next.toString());
//                   values[index] = next;
//                }                           
//                org.opengis.coverage.grid.GridCoverage gc = reader.read(values);
                
                if (gc instanceof GridCoverage2D){
                    coverage = (GridCoverage2D) gc;
                    ip.processLoadedGridCoverage(coverage);
                    replaceImage();
                    analyzeButton.setEnabled(true);
                } else {
                    System.out.println("Then what kind of coverage are you? " + gc.getClass().getName());
                }
            }
        }
        catch (FileNotFoundException fnfe){
            JOptionPane.showMessageDialog(this,
                                          fnfe.getMessage(),
                                          "File Not Found Exception",
                                          JOptionPane.ERROR_MESSAGE);                
        }
        catch (IOException ioe){
            JOptionPane.showMessageDialog(this,
                                          ioe.getMessage(),
                                          "I/O Exception",
                                          JOptionPane.ERROR_MESSAGE);                
        }           
    }
    
    private void replaceImage(){
        if (imageLabel != null){
            this.remove(imageLabel);
        }        
        Image image = null;
        if (coverage.getRenderedImage() instanceof WritableRenderedImageAdapter){
            image = ((WritableRenderedImageAdapter) coverage.getRenderedImage()).getAsBufferedImage();
        } else if (coverage.getRenderedImage() instanceof RenderedOp){
            image = ((RenderedOp) coverage.getRenderedImage()).getAsBufferedImage();
        }
        imageLabel = new JLabel(new ImageIcon(image));            
        this.add(imageLabel, BorderLayout.CENTER);
        this.validate();
        this.repaint();        
    }
    
    private void addToUndoList(){
        if (undo[0] == null){
            undo[0] = coverage;
        } else if (undo[1] == null){
            undo[1] = coverage;
        } else if (undo[2] == null){
            undo[2] = coverage;
        } else if (undo[3] == null){
            undo[3] = coverage;
        } else if (undo[4] == null){
            undo[4] = coverage;
        } else {  //undo list is full.
            undo[0] = undo[1];  //for performance sake, I hope Java is smart enough to just rearrange some pointers.
            undo[1] = undo[2];
            undo[2] = undo[3];
            undo[3] = undo[4];
            undo[4] = coverage;
        }
    }
    
    public void stateChanged(ChangeEvent e){
        if (e.getSource() != thresholder){
            throw new RuntimeException("Where did you come from?!" + e.getSource());
        } else {
            try{    
                GridCoverage2D backup;
                if (undo[4] != null){
                    backup = undo[4];
                } else if (undo[3] != null){
                    backup = undo[3];
                } else if (undo[2] != null){
                    backup = undo[2];
                } else if (undo[1] != null){
                    backup = undo[1];
                } else {
                    backup = undo[0];               
                }
                Operations ops = new Operations(new RenderingHints(null));
                Operation binOp = def.getOperation("Binarize");
                ParameterValueGroup pvg = binOp.getParameters();

                GridCoverage2D band0 = (GridCoverage2D) ops.selectSampleDimension(backup, new int[]{0});
                pvg.parameter("Source").setValue(band0);

                double val = thresholder.getValue();
//                System.out.println("val = " + val + " and scale = " + scale + ", so val * scale = " + (val * scale));
                double thresh = floor + (double) (val * scale);
                System.out.println("Slider gives me " + val + " so I use " + thresh);
                pvg.parameter("Threshold").setValue(new Double(val));  //val was thresh
                GridCoverage2D binarized = (GridCoverage2D) def.doOperation(pvg);  
                coverage = binarized;
                replaceImage();
            } catch (org.opengis.parameter.ParameterNotFoundException cause){
                throw new RuntimeException("No threshold parameter: " + cause);
            }            
        }
    }
    
    public static String[] extractClassNames(ClassedArray[] array){
        Vector v = new Vector();
        for (int i = 0; i < array.length; i++){
            String klass = array[i].getKlass();
            if (!(v.contains(klass))){
                v.add(klass);
            }            
        }
        
        System.out.println("I found " + v.size() + " classes.");
        
        String[] ret = new String[v.size()];
        for (int i = 0; i < v.size(); i++){
            ret[i] = (String) v.get(i);
        }
        return ret;
    }        
    
    public static double euclideanDist(double[] a, double[] b){
        if (a.length != b.length){
            throw new RuntimeException("arrays must be same length: a.length = " + a.length + " and b.length = " + b.length);
        }
        double sum = 0;
        for (int i = 0; i < a.length; i++){
            sum = sum + ((a[i] - b[i]) * (a[i] - b[i]));
        }
        return Math.sqrt(sum);
    }     
    
    public ClassedArray[] standardizeMatrix(ClassedArray[] in){
        //this is not in DissUtils because I want access to the means and standard deviations later, too.
        ClassedArray[] out = new ClassedArray[in.length];
        means = new double[in[0].getArray().length];
        stdevs = new double[in[0].getArray().length];
        double tolerance = 0.001;  //if the mean is within tolerance of 0 and the standard deviation is within tolerance of 1, then assume it's already standardized.
        
        //compute means
        double[] sums = new double[means.length];
        for (int i = 0; i < in.length; i++){
            double[] snuh = in[i].getArray();
            for (int j = 0; j < snuh.length; j++){
                sums[j] = sums[j] + snuh[j];
            }
        }
        for (int k = 0; k < means.length; k++){
            means[k] = sums[k] / in.length;
        }
        
        //compute stdevs
        Arrays.fill(sums, 0);
        for (int i = 0; i < in.length; i++){
            double[] snuh = in[i].getArray();
            for (int j = 0; j < snuh.length; j++){
                sums[j] = sums[j] + ((snuh[j] - means[j]) * (snuh[j] - means[j]));
            }
        }
        for (int k = 0; k < stdevs.length; k++){
            stdevs[k] = Math.sqrt(sums[k] / in.length);
        }
                      
        //compute standardized values (if needed)
        double[][] matrix = new double[in.length][means.length];
        
        for (int i = 0; i < means.length; i++){
            boolean standardized = (Math.abs(means[i]) > tolerance) && (Math.abs(stdevs[i] - 1) > tolerance);
            for (int j = 0; j < in.length; j++){
                if (standardized){
                    matrix[j][i] = in[j].getArray()[i];
                } else {
                    matrix[j][i] = (in[j].getArray()[i] - means[i])/stdevs[i];
                }
            }
        }
        
        for (int k = 0; k < in.length; k++){
            ClassedArray ca = new ClassedArray();
            ca.setArray(matrix[k]);
            ca.setKlass(in[k].getKlass());
            out[k] = ca;
        }
        
        return out;
        
    }        
    
}
