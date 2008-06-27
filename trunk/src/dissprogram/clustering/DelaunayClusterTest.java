/*
 * DelaunayClusterTest.java
 *
 * Created on April 4, 2007, 3:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.clustering;

import java.awt.Dimension;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import javax.swing.JFrame;
import org.geotools.feature.FeatureCollection;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.util.delaunay.AddAPointTriangulator;
import org.geotools.graph.util.delaunay.AutoClustUtils;
import org.geotools.graph.util.delaunay.DelaunayEdge;
import org.geotools.graph.util.delaunay.DelaunayNode;
import org.geotools.graph.util.delaunay.DelaunayTriangulator;
import org.geotools.graph.util.delaunay.GraphViewer;

/**
 *
 * @author jfc173
 */
public class DelaunayClusterTest {
    
    /** Creates a new instance of DelaunayClusterTest */
    public DelaunayClusterTest() {
    }
    
    public static FeatureCollection createNodes(){
        FeatureCollection coll = null;
        boolean useAll = true;
        
        try{
            FileReader dataReader = new FileReader("C:/jconley/diss/synthData/grid/s100bw1bk0l4000p0.csv");           
            edu.psu.geovista.io.csv.CSVParser csvp = new edu.psu.geovista.io.csv.CSVParser(dataReader);
            String[][] data = csvp.getAllValues();
            double[][] all = new double[data.length-1][data[0].length];
            for (int i = 1; i < data.length; i++){
                for (int j = 0; j < data[i].length; j++){
                    all[i-1][j] = Double.parseDouble(data[i][j]);
                }
            } 

            coll = org.geotools.feature.FeatureCollections.newCollection();

            for (int i = 0; i < all.length; i++){         
                com.vividsolutions.jts.geom.Coordinate coord = new com.vividsolutions.jts.geom.Coordinate(all[i][0]*3, all[i][1]*3);
                com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
                com.vividsolutions.jts.geom.Point p = fact.createPoint(coord);

                java.util.ArrayList features = new java.util.ArrayList();
                org.geotools.feature.AttributeType[] pointAttribute = new org.geotools.feature.AttributeType[3];
                pointAttribute[0] = org.geotools.feature.AttributeTypeFactory.newAttributeType("centre", com.vividsolutions.jts.geom.Point.class);
                pointAttribute[1] = org.geotools.feature.AttributeTypeFactory.newAttributeType("population",Double.class);
                pointAttribute[2] = org.geotools.feature.AttributeTypeFactory.newAttributeType("target",Double.class);

                org.geotools.feature.FeatureType pointType = org.geotools.feature.FeatureTypeFactory.newFeatureType(pointAttribute,"testPoint");

                org.geotools.feature.Feature pointFeature = pointType.create(new Object[]{p,
                                                                                          new Double(all[i][2]),
                                                                                          new Double(all[i][3])});
                coll.add(pointFeature);                                
            }
            
 
        } catch (Exception e){
//            e.printStackTrace();
            System.out.println("Error message: " + e.getMessage());
        }
        return coll;
    }        
    
    
    public static void main(String[] args){
        Graph g;
        boolean debug = false;

        JFrame frame = new JFrame();
        DelaunayTriangulator triangulator = new DelaunayTriangulator();
        if (debug){
            AddAPointTriangulator aapt = new AddAPointTriangulator(AutoClustUtils.featureCollectionToNodeArray(createNodes()));            
            frame.getContentPane().add(aapt);
        } else {
            triangulator.setNodeArray(AutoClustUtils.featureCollectionToNodeArray(createNodes()));
            g = triangulator.getTriangulation();  
            try{
                FileWriter nodeWriter = new FileWriter("C:/jconley/diss/synthData/grid/delaunay/s100bw1bk0l4000p0nodes.csv");
                FileWriter edgeWriter = new FileWriter("C:/jconley/diss/synthData/grid/delaunay/s100bw1bk0l4000p0edges.csv");       
                Iterator nodeIt = g.getNodes().iterator();
                while (nodeIt.hasNext()){
                    DelaunayNode next = (DelaunayNode) nodeIt.next();
                    double x = next.getFeature().getDefaultGeometry().getCentroid().getX();
                    double y = next.getFeature().getDefaultGeometry().getCentroid().getY();
                    double pop = ((Double) next.getFeature().getAttribute("population")).doubleValue();
                    double cases = ((Double) next.getFeature().getAttribute("target"));
                    nodeWriter.write(x + "," + y + "," + pop + "," + cases + '\n');
                }
                
                Iterator edgeIt = g.getEdges().iterator();
                while (edgeIt.hasNext()){
                    DelaunayEdge next = (DelaunayEdge) edgeIt.next();
                    double x1 = ((DelaunayNode) next.getNodeA()).getFeature().getDefaultGeometry().getCentroid().getX();
                    double y1 = ((DelaunayNode) next.getNodeA()).getFeature().getDefaultGeometry().getCentroid().getY();
                    double x2 = ((DelaunayNode) next.getNodeB()).getFeature().getDefaultGeometry().getCentroid().getX();
                    double y2 = ((DelaunayNode) next.getNodeB()).getFeature().getDefaultGeometry().getCentroid().getY();
                    edgeWriter.write(x1 + "," + y1 + "," + x2 + "," + y2 + '\n');
                }
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Exception writing files: " + e.getMessage());
            }
            
            
            GraphViewer viewer = new GraphViewer();
            viewer.setGraph(g);
            frame.getContentPane().add(viewer);
        }
        //for debug purposes
//        System.out.println("long edges:");
//        java.util.Iterator edgeIt = g.getEdges().iterator();
//        while (edgeIt.hasNext()){
//            DelaunayEdge next = (DelaunayEdge) edgeIt.next();
//            if (next.getEuclideanDistance() > 50){
//                System.out.println(next.toString());
//            }
//        }
        //end debug        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(400, 400));
        frame.setVisible(true);
                
    }
    
}
