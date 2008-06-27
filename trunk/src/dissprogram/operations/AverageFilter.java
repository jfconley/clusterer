/*
 * AverageFilter.java
 *
 * Created on June 14, 2007, 9:27 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.operations;

import dissprogram.DissUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import org.geotools.coverage.grid.GridCoverage2D;

/**
 *
 * @author z4x
 */
public class AverageFilter {
    
    public static final int MEDIAN = 0;
    public static final int MEAN = 1;
    public static final int MODE = 2;
    
    private int window = 3;
    private int type = MEDIAN;
    
    /** Creates a new instance of AverageFilter */
    public AverageFilter() {
    }
    
    public void setWindowSize(int i){
        if (i % 2 == 1){
            window = i;
        } else {
            System.out.println("window size must be odd: " + i);
        }
    }
    
    public void setType(int i){
        if ((i == MEDIAN) || (i == MEAN) || (i == MODE)){
            type = i;
        } else {
            System.out.println("invalid type: " + i);
        }
    }
    
    public int getType(){
        return type;
    }
    
    public GridCoverage2D filter(GridCoverage2D coverage){
        //I'd like to think there's a better way of doing this than coverage-->raster-->coverage, but oh well
        float[][] matrix = DissUtils.coverageToMatrix(coverage);
        float[][] smoothed = new float[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++){
            for (int j = 0; j < matrix[i].length; j++){
                int windowWidth = (int) Math.floor(window / 2);
                int windowMinX = Math.max(0, i - windowWidth);
                int windowMaxX = Math.min(matrix.length - 1, i + windowWidth);
                int windowMinY = Math.max(0, j - windowWidth);
                int windowMaxY = Math.min(matrix[i].length - 1, j + windowWidth);
                float[][] windowValues = new float[windowMaxX - windowMinX + 1][windowMaxY - windowMinY + 1];
                for (int k = windowMinX; k <= windowMaxX; k++){
                    for (int l = windowMinY; l <= windowMaxY; l++){
                        windowValues[k - windowMinX][l - windowMinY] = matrix[k][l];
                    }
                }
                
                switch (type){
                    case MEDIAN:
                        smoothed[i][j] = calculateMedian(windowValues);
                        break;
                    case MEAN:
                        smoothed[i][j] = calculateMean(windowValues);
                        break;
                    case MODE:
                        smoothed[i][j] = calculateMode(windowValues);
                        break;
                }                    
            }
        }
        return DissUtils.matrixToCoverage(smoothed, coverage.getEnvelope());
    }
    
    private float calculateMedian(float[][] windowValues){
        float[] array = new float[windowValues.length * windowValues[0].length];
        int medianIndex = (int) Math.floor(array.length / 2);
        for (int i = 0; i < windowValues.length; i++){
            for (int j = 0; j < windowValues[i].length; j++){
                array[(i * windowValues[0].length) + j] = windowValues[i][j];
            }
        }
//        System.out.print("input array is [" + array[0]);
//        for (int i = 1; i < array.length; i++){
//            System.out.print(", " + array[i]);
//        }
//        System.out.println("]");
        Arrays.sort(array);
//        System.out.println("Median is " + array[medianIndex]);
        return array[medianIndex];
    }
    
    private float calculateMean(float[][] windowValues){
        float sum = 0;
        for (int i = 0; i < windowValues.length; i++){
            for (int j = 0; j < windowValues[i].length; j++){
                sum = sum + windowValues[i][j];
            }
        }
        return sum / (windowValues.length * windowValues[0].length);
    }
    
    private float calculateMode(float[][] windowValues){
        HashMap map = new HashMap();
        for (int i = 0; i < windowValues.length; i++){
            for (int j = 0; j < windowValues[i].length; j++){
                if (map.containsKey("" + windowValues[i][j])){
                    Integer count = (Integer) map.get("" + windowValues[i][j]);
                    map.put("" + windowValues[i][j], new Integer(count.intValue() + 1));
                } else {
                    map.put("" + windowValues[i][j], new Integer(1));
                }               
            }
        }
        
        String most = "";
        int mostCount = 0;
        Iterator it = map.keySet().iterator();
        while (it.hasNext()){
            String next = (String) it.next();
            Integer innt = (Integer) map.get(next);
            if (innt.intValue() > mostCount){
                mostCount = innt.intValue();
                most = next;
            }
        }
        
        return Float.parseFloat(most);        
    }
    
}
