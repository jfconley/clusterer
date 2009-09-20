/*
 * PCAMain.java
 *
 * Created on February 27, 2007, 10:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 * NOTE: IN THE LIBRARIES, GV/JDM, GV/GEOTOOLS, COLT, GV/SDA, JTS-1.6, GT2/API-2.2.x, AND GT2/MAIN-2.2.x ARE ONLY FOR THIS CLASS!
 */

package dissprogram.utilMains;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import edu.psu.geovista.io.csv.CSVParser;
import edu.psu.geovista.sda.PCAMethod;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;

/**
 *
 * @author jfc173
 */
public class PCAMain {
    
    /** Creates a new instance of PCAMain */
    public PCAMain() {
    }
    
    public static void main(String[] args) {
        //input file
        File f;
        String[][] values;
        try{
            f = new File("D:/synthLandscan/momentData/GAMtestData.csv");
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
        
        //convert to feature collection
        FeatureCollection fc = FeatureCollections.newCollection();
        com.vividsolutions.jts.geom.GeometryFactory geomFac = new com.vividsolutions.jts.geom.GeometryFactory();
        java.util.ArrayList features = new java.util.ArrayList();
        try{
            org.geotools.feature.AttributeType[] pointAttribute = new org.geotools.feature.AttributeType[9];
            pointAttribute[0] = AttributeTypeFactory.newAttributeType("where", Point.class);
            pointAttribute[1] = AttributeTypeFactory.newAttributeType("inv_1", Double.class);
            pointAttribute[2] = AttributeTypeFactory.newAttributeType("inv_2", Double.class);
            pointAttribute[3] = AttributeTypeFactory.newAttributeType("inv_3", Double.class);
            pointAttribute[4] = AttributeTypeFactory.newAttributeType("inv_4", Double.class);
            pointAttribute[5] = AttributeTypeFactory.newAttributeType("inv_5", Double.class);
            pointAttribute[6] = AttributeTypeFactory.newAttributeType("inv_6", Double.class);
            pointAttribute[7] = AttributeTypeFactory.newAttributeType("inv_7", Double.class);
            pointAttribute[8] = AttributeTypeFactory.newAttributeType("class", String.class);

            org.geotools.feature.FeatureType pointType = org.geotools.feature.FeatureTypeFactory.newFeatureType(pointAttribute,"Feature");

            for(int j = 1; j < values.length; j++){
                Point point = geomFac.createPoint(new Coordinate(j, j));
                double inv_1 = Double.parseDouble(values[j][0]);
                double inv_2 = Double.parseDouble(values[j][1]);
                double inv_3 = Double.parseDouble(values[j][2]);
                double inv_4 = Double.parseDouble(values[j][3]);
                double inv_5 = Double.parseDouble(values[j][4]);
                double inv_6 = Double.parseDouble(values[j][5]);
                double inv_7 = Double.parseDouble(values[j][6]);
                org.geotools.feature.Feature pointFeature = pointType.create(new Object[]{point,
                                                                                          new Double(inv_1),
                                                                                          new Double(inv_2),
                                                                                          new Double(inv_3),
                                                                                          new Double(inv_4),
                                                                                          new Double(inv_5),
                                                                                          new Double(inv_6),
                                                                                          new Double(inv_7),
                                                                                          values[j][7]},
                                                                             ""+j);
                features.add(pointFeature);
            }
            fc.addAll(features);
        }
        catch (SchemaException se){
            throw new RuntimeException("Schema exception!  " + se.getMessage());
        }
        catch (IllegalAttributeException iae){
            throw new RuntimeException("Illegal attribute!  " + iae.getMessage());
        }
        
        System.out.println("schema: " + fc.getSchema());
        System.out.println("FType:  " + fc.getFeatureType());
        System.out.println("count:  " + fc.getNumberOfAttributes());
        
        //run PCAMethod
        PCAMethod pca = new PCAMethod();
        pca.setDataset(fc);
//        pca.setNumComponents(5);
        FeatureCollection withPCA = pca.calculate();
        
        //write out results
        System.out.println("inv_1, inv_2, inv_3, inv_4, inv_5, inv_6, inv_7, class, pc1, pc2, pc3, pc4, pc5");
        FeatureIterator it = withPCA.features();
        while (it.hasNext()){
            Feature next = it.next();
            System.out.print(next.getAttribute("inv_1") + ", ");
            System.out.print(next.getAttribute("inv_2") + ", ");
            System.out.print(next.getAttribute("inv_3") + ", ");
            System.out.print(next.getAttribute("inv_4") + ", ");
            System.out.print(next.getAttribute("inv_5") + ", ");
            System.out.print(next.getAttribute("inv_6") + ", ");
            System.out.print(next.getAttribute("inv_7") + ", ");
            System.out.print(next.getAttribute("class") + ", ");
            System.out.print(next.getAttribute("pca0") + ", ");
            System.out.print(next.getAttribute("pca1") + ", ");
            System.out.print(next.getAttribute("pca2") + ", ");
            System.out.print(next.getAttribute("pca3") + ", ");
            System.out.println(next.getAttribute("pca4"));
        }
    }
    
}
