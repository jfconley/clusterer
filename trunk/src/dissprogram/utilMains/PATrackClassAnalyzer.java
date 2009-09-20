/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dissprogram.utilMains;

import edu.psu.geovista.io.csv.CSVParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author jconley
 */
public class PATrackClassAnalyzer {

    static int[][][][] DWGAMClasses = new int[300][7][6][6];
    static int[][][][] DWFZClasses = new int[300][7][6][6];
    static int[][][][] DWBaseClasses = new int[300][7][6][6];
    static double[][][][] DWGAMMasses = new double[300][7][6][6];
    static double[][][][] DWFZMasses = new double[300][7][6][6];
    static double[][][][] DWBaseMasses = new double[300][7][6][6];
    static int[][][][] simpleGAMClasses = new int[300][7][6][6];
    static int[][][][] simpleFZClasses = new int[300][7][6][6];
    static int[][][][] simpleBaseClasses = new int[300][7][6][6];
    static double[][][][] simpleGAMMasses = new double[300][7][6][6];
    static double[][][][] simpleFZMasses = new double[300][7][6][6];
    static double[][][][] simpleBaseMasses = new double[300][7][6][6];        
    static String[] classNames = new String[7];
    static String[] operators = {"mode", "average", "simple DS", "prop bel trans", "comb. ave.", "mod. ave.", "tree prop bel trans"};
    
    
    public static void main(String[] args) {        
        for (int k = 2; k < 301; k++) {
            try{
                File DWGAMFile = new File("D:/synthLandscan/randomStarts/momentData/GAM/DWGAMClassifications" + k + ".csv");
                File simpleGAMFile = new File("D:/synthLandscan/randomStarts/momentData/GAM/simpleGAMClassifications" + k + ".csv");
//                File DWFZFile = new File("D:/synthLandscan/momentData/fiveClassPACorrected/FZ/DWFZClassifications" + k + ".csv");
//                File simpleFZFile = new File("D:/synthLandscan/momentData/fiveClassPACorrected/FZ/simpleFZClassifications" + k + ".csv");
//                File DWBaseFile = new File("D:/synthLandscan/momentData/fiveClassPACorrected/base/DWbaseClassifications" + k + ".csv");
//                File simpleBaseFile = new File("D:/synthLandscan/momentData/fiveClassPACorrected/base/simplebaseClassifications" + k + ".csv");
            
                FileReader DWGAMReader = new FileReader(DWGAMFile);
                FileReader simpleGAMReader = new FileReader(simpleGAMFile);
//                FileReader DWFZReader = new FileReader(DWFZFile);
//                FileReader simpleFZReader = new FileReader(simpleFZFile);
//                FileReader DWBaseReader = new FileReader(DWBaseFile);
//                FileReader simpleBaseReader = new FileReader(simpleBaseFile);
                
                CSVParser DWGAMParser = new CSVParser(DWGAMReader);
                CSVParser simpleGAMParser = new CSVParser(simpleGAMReader);
//                CSVParser DWFZParser = new CSVParser(DWFZReader);
//                CSVParser simpleFZParser = new CSVParser(simpleFZReader);
//                CSVParser DWBaseParser = new CSVParser(DWBaseReader);
//                CSVParser simpleBaseParser = new CSVParser(simpleBaseReader);
                
                String[][] DWGAMValues = DWGAMParser.getAllValues();
                String[][] simpleGAMValues = simpleGAMParser.getAllValues();
//                String[][] DWFZValues = DWFZParser.getAllValues();
//                String[][] simpleFZValues = simpleFZParser.getAllValues();
//                String[][] DWBaseValues = DWBaseParser.getAllValues();
//                String[][] simpleBaseValues = simpleBaseParser.getAllValues();
                
                //each of the values arrays should be the same size.
                //row 0 is column headers
                classNames = DWGAMValues[0];
                
//                for (int debug = 0; debug < classNames.length; debug++){
//                    System.out.println(debug + ": " + classNames[debug]);
//                }
                
                for (int j = 1; j < DWGAMValues.length; j++){
                    String[] DWGAMThisRowStrings = DWGAMValues[j];
                    double[] DWGAMThisRowDoubles = new double[DWGAMThisRowStrings.length - 1];  //-1 because first column is the operator
                    for (int i = 0; i < DWGAMThisRowDoubles.length; i++){
                        DWGAMThisRowDoubles[i] = Double.parseDouble(DWGAMThisRowStrings[i+1]);
                    }
                    
                    String[] simpleGAMThisRowStrings = simpleGAMValues[j];
                    double[] simpleGAMThisRowDoubles = new double[simpleGAMThisRowStrings.length - 1];  //-1 because first column is the operator
                    for (int i = 0; i < simpleGAMThisRowDoubles.length; i++){
                        simpleGAMThisRowDoubles[i] = Double.parseDouble(simpleGAMThisRowStrings[i+1]);
                    }
                    
//                    String[] DWFZThisRowStrings = DWFZValues[j];
//                    double[] DWFZThisRowDoubles = new double[DWFZThisRowStrings.length - 1];  //-1 because first column is the operator
//                    for (int i = 0; i < DWFZThisRowDoubles.length; i++){
//                        DWFZThisRowDoubles[i] = Double.parseDouble(DWFZThisRowStrings[i+1]);
//                    }
//                    
//                    String[] simpleFZThisRowStrings = simpleFZValues[j];
//                    double[] simpleFZThisRowDoubles = new double[simpleFZThisRowStrings.length - 1];  //-1 because first column is the operator
//                    for (int i = 0; i < simpleFZThisRowDoubles.length; i++){
//                        simpleFZThisRowDoubles[i] = Double.parseDouble(simpleFZThisRowStrings[i+1]);
//                    }
//                    
//                    String[] DWBaseThisRowStrings = DWBaseValues[j];
//                    double[] DWBaseThisRowDoubles = new double[DWBaseThisRowStrings.length - 1];  //-1 because first column is the operator
//                    for (int i = 0; i < DWBaseThisRowDoubles.length; i++){
//                        DWBaseThisRowDoubles[i] = Double.parseDouble(DWBaseThisRowStrings[i+1]);
//                    }
//                    
//                    String[] simpleBaseThisRowStrings = simpleBaseValues[j];
//                    double[] simpleBaseThisRowDoubles = new double[simpleBaseThisRowStrings.length - 1];  //-1 because first column is the operator
//                    for (int i = 0; i < simpleBaseThisRowDoubles.length; i++){
//                        simpleBaseThisRowDoubles[i] = Double.parseDouble(simpleBaseThisRowStrings[i+1]);
//                    }   
                    
                    //figure out the winner for each row of doubles.
                    String[] DWGAMWinners = new String[DWGAMValues.length];
                    String[] simpleGAMWinners = new String[simpleGAMValues.length];
//                    String[] DWFZWinners = new String[DWFZValues.length];
//                    String[] simpleFZWinners = new String[simpleFZValues.length];
//                    String[] DWBaseWinners = new String[DWBaseValues.length];
//                    String[] simpleBaseWinners = new String[simpleBaseValues.length];
                    
                    int[] topIndexes = new int[6];
                    double[] topMasses = new double[6];
                    Arrays.fill(topIndexes, -1);
                    Arrays.fill(topMasses, -1);
                    
                    for (int i = 0; i < DWGAMThisRowDoubles.length-1; i++){  //stop early because I don't want THETA to win
                        if (DWGAMThisRowDoubles[i] > topMasses[0]){
                            topIndexes[0] = i;
                            topMasses[0] = DWGAMThisRowDoubles[i];
                        }
                        if (simpleGAMThisRowDoubles[i] > topMasses[1]){
                            topIndexes[1] = i;
                            topMasses[1] = simpleGAMThisRowDoubles[i];
                        }
//                        if (DWFZThisRowDoubles[i] > topMasses[2]){
//                            topIndexes[2] = i;
//                            topMasses[2] = DWFZThisRowDoubles[i];
//                        }
//                        if (simpleFZThisRowDoubles[i] > topMasses[3]){
//                            topIndexes[3] = i;
//                            topMasses[3] = simpleFZThisRowDoubles[i];
//                        }
//                        if (DWBaseThisRowDoubles[i] > topMasses[4]){
//                            topIndexes[4] = i;
//                            topMasses[4] = DWBaseThisRowDoubles[i];
//                        }
//                        if (simpleBaseThisRowDoubles[i] > topMasses[5]){
//                            topIndexes[5] = i;
//                            topMasses[5] = simpleBaseThisRowDoubles[5];
//                        }                        
                    }
                    for (int p = 0; p < topIndexes.length; p++){
                        if (topIndexes[p] == -1){
                            topIndexes[p] = 5;  //this means unclassified, probably NaN's across the board from a simple DS conflict.
                        }
                    }                                        
                                                           
                    //note that the first 280 are inf, next 280 are road, next 280 are wind, next 280 are river, and last 280 are airport
                    //IS THIS STILL VALID?!?!?!?!  Now it's 1554
                    int actual = -1;
                    if (j < 1555){
                        actual = 0;
                    } else if (j < 3109){
                        actual = 2;
                    } else if (j < 4663){
                        actual = 3;
                    } else if (j < 6217){
                        actual = 1;
                    } else if (j < 7771){
                        actual = 4;
                    }
                                        
                    //Add it to the confusion matrix quadarrays                    
                    DWGAMClasses[k-2][(j-1)%7][actual][topIndexes[0]]++;
                    simpleGAMClasses[k-2][(j-1)%7][actual][topIndexes[1]]++;
//                    DWFZClasses[k-2][(j-1)%7][actual][topIndexes[2]]++;
//                    simpleFZClasses[k-2][(j-1)%7][actual][topIndexes[3]]++;
//                    DWBaseClasses[k-2][(j-1)%7][actual][topIndexes[4]]++;
//                    simpleBaseClasses[k-2][(j-1)%7][actual][topIndexes[5]]++;
                                        
                    //Add the probabilities to the prob quadarrays                   
                    for (int d = 0; d < DWGAMThisRowDoubles.length; d++){
                        if (Double.isNaN(DWGAMThisRowDoubles[d])){
                            //???
                        } else {
                            DWGAMMasses[k-2][(j-1)%7][actual][d] += DWGAMThisRowDoubles[d];
                        }
                        if (Double.isNaN(simpleGAMThisRowDoubles[d])){
                            //???
                        } else {
                            simpleGAMMasses[k-2][(j-1)%7][actual][d] += simpleGAMThisRowDoubles[d];
                        }
//                        if (Double.isNaN(DWFZThisRowDoubles[d])){
//                            //???
//                        } else {                        
//                            DWFZMasses[k-2][(j-1)%7][actual][d] += DWFZThisRowDoubles[d];
//                        }
//                        if (Double.isNaN(simpleFZThisRowDoubles[d])){
//                            //???
//                        } else {                        
//                            simpleFZMasses[k-2][(j-1)%7][actual][d] += simpleFZThisRowDoubles[d];
//                        }
//                        if (Double.isNaN(DWBaseThisRowDoubles[d])){
//                            //???
//                        } else {                        
//                            DWBaseMasses[k-2][(j-1)%7][actual][d] += DWBaseThisRowDoubles[d];
//                        }
//                        if (Double.isNaN(simpleBaseThisRowDoubles[d])){
//                            //???
//                        } else {                        
//                            simpleBaseMasses[k-2][(j-1)%7][actual][d] += simpleBaseThisRowDoubles[d];                            
//                        }
                    }  //end for d                                           
                }  //end for j
            } catch (FileNotFoundException fnfe){
                throw new RuntimeException("No file!  " + fnfe.getMessage());
            } catch (IOException ioe){
                throw new RuntimeException("IO Exception!  " + ioe.getMessage());
            }
            //divide the prob quadarrays by 40(222) so they're actually probs again.             
            for (int v = 0; v < 7; v++){
                for (int v1 = 0; v1 < 6; v1++){
                    for (int v2 = 0; v2 < 6; v2++){
                        DWGAMMasses[k-2][v][v1][v2] = DWGAMMasses[k-2][v][v1][v2]/222;
                        simpleGAMMasses[k-2][v][v1][v2] = simpleGAMMasses[k-2][v][v1][v2]/222;
//                        DWFZMasses[k-2][v][v1][v2] = DWFZMasses[k-2][v][v1][v2]/40;
//                        simpleFZMasses[k-2][v][v1][v2] = simpleFZMasses[k-2][v][v1][v2]/40;
//                        DWBaseMasses[k-2][v][v1][v2] = DWBaseMasses[k-2][v][v1][v2]/40;
//                        simpleBaseMasses[k-2][v][v1][v2] = simpleBaseMasses[k-2][v][v1][v2]/40;
                    }
                }
            }
            System.out.println("finished collating k = " + k);
        }  //end for k      
        
        writeGeneralSummaries();
        writeClassSummaries();
        writeConfusionMatrices();
        
    }
    
    private static void writeGeneralSummaries(){
        //write out summaries for %correct by k
        //write out summaries for certainty by k                               
        
        double[][] DWGAMCorrOpByK = new double[7][300];
        double[][] DWGAMCorrClByK = new double[5][300];
        double[][] DWGAMCertOpByK = new double[7][300];
        double[][] DWGAMCertClByK = new double[5][300];
        
        double[][] simpleGAMCorrOpByK = new double[7][300];
        double[][] simpleGAMCorrClByK = new double[5][300];
        double[][] simpleGAMCertOpByK = new double[7][300];
        double[][] simpleGAMCertClByK = new double[5][300];
        
//        double[][] DWFZCorrOpByK = new double[7][300];
//        double[][] DWFZCorrClByK = new double[5][300];
//        double[][] DWFZCertOpByK = new double[7][300];
//        double[][] DWFZCertClByK = new double[5][300];
//        
//        double[][] simpleFZCorrOpByK = new double[7][300];
//        double[][] simpleFZCorrClByK = new double[5][300];
//        double[][] simpleFZCertOpByK = new double[7][300];
//        double[][] simpleFZCertClByK = new double[5][300];
//        
//        double[][] DWBaseCorrOpByK = new double[7][300];
//        double[][] DWBaseCorrClByK = new double[5][300];
//        double[][] DWBaseCertOpByK = new double[7][300];
//        double[][] DWBaseCertClByK = new double[5][300];
//        
//        double[][] simpleBaseCorrOpByK = new double[7][300];
//        double[][] simpleBaseCorrClByK = new double[6][300];
//        double[][] simpleBaseCertOpByK = new double[7][300];
//        double[][] simpleBaseCertClByK = new double[6][300];        
        
        File DWGAMpctCorrectOperatorByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/DWGAMPctCorrectOperatorByK.csv");
        File DWGAMpctCorrectClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/DWGAMpctCorrectClassByK.csv");
        File DWGAMcertaintyOperatorByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/DWGAMcertaintyOperatorByK.csv");
        File DWGAMcertaintyClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/DWGAMcertaintyClassByK.csv");
        
        File simpleGAMpctCorrectOperatorByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/simpleGAMpctCorrectOperatorByK.csv");
        File simpleGAMpctCorrectClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/simpleGAMpctCorrectClassByK.csv");
        File simpleGAMcertaintyOperatorByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/simpleGAMcertaintyOperatorByK.csv");
        File simpleGAMcertaintyClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/simpleGAMcertaintyClassByK.csv");
        
//        File DWFZpctCorrectOperatorByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/DWFZpctCorrectOperatorByK.csv");
//        File DWFZpctCorrectClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/DWFZpctCorrectClassByK.csv");
//        File DWFZcertaintyOperatorByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/DWFZcertaintyOperatorByK.csv");
//        File DWFZcertaintyClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/DWFZcertaintyClassByK.csv");
//        
//        File simpleFZpctCorrectOperatorByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/simpleFZpctCorrectOperatorByK.csv");
//        File simpleFZpctCorrectClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/simpleFZpctCorrectClassByK.csv");
//        File simpleFZcertaintyOperatorByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/simpleFZcertaintyOperatorByK.csv");
//        File simpleFZcertaintyClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/simpleFZcertaintyClassByK.csv");
//        
//        File DWBasepctCorrectOperatorByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/DWBasepctCorrectOperatorByK.csv");
//        File DWBasepctCorrectClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/DWBasepctCorrectClassByK.csv");
//        File DWBasecertaintyOperatorByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/DWBasecertaintyOperatorByK.csv");
//        File DWBasecertaintyClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/DWBasecertaintyClassByK.csv");
//        
//        File simpleBasepctCorrectOperatorByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/simpleBasepctCorrectOperatorByK.csv");
//        File simpleBasepctCorrectClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/simpleBasepctCorrectClassByK.csv");
//        File simpleBasecertaintyOperatorByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/simpleBasecertaintyOperatorByK.csv");
//        File simpleBasecertaintyClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/simpleBasecertaintyClassByK.csv");
        try{
            FileWriter DWGAMPctCorrectOperatorByKWriter = new FileWriter(DWGAMpctCorrectOperatorByK);
            FileWriter DWGAMPctCorrectClassByKWriter = new FileWriter(DWGAMpctCorrectClassByK);
            FileWriter DWGAMCertaintyOperatorByKWriter = new FileWriter(DWGAMcertaintyOperatorByK);
            FileWriter DWGAMCertaintyClassByKWriter = new FileWriter(DWGAMcertaintyClassByK);

            FileWriter simpleGAMPctCorrectOperatorByKWriter = new FileWriter(simpleGAMpctCorrectOperatorByK);
            FileWriter simpleGAMPctCorrectClassByKWriter = new FileWriter(simpleGAMpctCorrectClassByK);
            FileWriter simpleGAMCertaintyOperatorByKWriter = new FileWriter(simpleGAMcertaintyOperatorByK);
            FileWriter simpleGAMCertaintyClassByKWriter = new FileWriter(simpleGAMcertaintyClassByK);

//            FileWriter DWFZPctCorrectOperatorByKWriter = new FileWriter(DWFZpctCorrectOperatorByK);
//            FileWriter DWFZPctCorrectClassByKWriter = new FileWriter(DWFZpctCorrectClassByK);
//            FileWriter DWFZCertaintyOperatorByKWriter = new FileWriter(DWFZcertaintyOperatorByK);
//            FileWriter DWFZCertaintyClassByKWriter = new FileWriter(DWFZcertaintyClassByK);
//
//            FileWriter simpleFZPctCorrectOperatorByKWriter = new FileWriter(simpleFZpctCorrectOperatorByK);
//            FileWriter simpleFZPctCorrectClassByKWriter = new FileWriter(simpleFZpctCorrectClassByK);
//            FileWriter simpleFZCertaintyOperatorByKWriter = new FileWriter(simpleFZcertaintyOperatorByK);
//            FileWriter simpleFZCertaintyClassByKWriter = new FileWriter(simpleFZcertaintyClassByK);
//
//            FileWriter DWBasePctCorrectOperatorByKWriter = new FileWriter(DWBasepctCorrectOperatorByK);
//            FileWriter DWBasePctCorrectClassByKWriter = new FileWriter(DWBasepctCorrectClassByK);
//            FileWriter DWBaseCertaintyOperatorByKWriter = new FileWriter(DWBasecertaintyOperatorByK);
//            FileWriter DWBaseCertaintyClassByKWriter = new FileWriter(DWBasecertaintyClassByK);
//
//            FileWriter simpleBasePctCorrectOperatorByKWriter = new FileWriter(simpleBasepctCorrectOperatorByK);
//            FileWriter simpleBasePctCorrectClassByKWriter = new FileWriter(simpleBasepctCorrectClassByK);
//            FileWriter simpleBaseCertaintyOperatorByKWriter = new FileWriter(simpleBasecertaintyOperatorByK);
//            FileWriter simpleBaseCertaintyClassByKWriter = new FileWriter(simpleBasecertaintyClassByK);        

            for (int op = 0; op < 7; op++){
                for (int k2 = 0; k2 < 300; k2++){
                    for (int kl = 0; kl < 5; kl++){                    
                        DWGAMCorrOpByK[op][k2] += DWGAMClasses[k2][op][kl][kl]/1110d;  //div by 200 instead of div by 40 because we want the percent across all classes, and this is equivalent to adding up all class-specific percent corrects and dividing by 5.
                        DWGAMCertOpByK[op][k2] += DWGAMMasses[k2][op][kl][kl]/5;     //div by 5 again to average over all classes
                        DWGAMCorrClByK[kl][k2] += DWGAMClasses[k2][op][kl][kl]/1554d;  //div by 280 instead of by 40 to average over all operators
                        DWGAMCertClByK[kl][k2] += DWGAMMasses[k2][op][kl][kl]/7;     //div by 7 to average over all operators.
                 
                        simpleGAMCorrOpByK[op][k2] += simpleGAMClasses[k2][op][kl][kl]/1110d;  //div by 200 instead of div by 40 because we want the percent across all classes, and this is equivalent to adding up all class-specific percent corrects and dividing by 5.
                        simpleGAMCertOpByK[op][k2] += simpleGAMMasses[k2][op][kl][kl]/5;     //div by 5 again to average over all classes
                        simpleGAMCorrClByK[kl][k2] += simpleGAMClasses[k2][op][kl][kl]/1554d;  //div by 280 instead of by 40 to average over all operators
                        simpleGAMCertClByK[kl][k2] += simpleGAMMasses[k2][op][kl][kl]/7; 
                        
//                        DWFZCorrOpByK[op][k2] += DWFZClasses[k2][op][kl][kl]/200d;  //div by 200 instead of div by 40 because we want the percent across all classes, and this is equivalent to adding up all class-specific percent corrects and dividing by 5.
//                        DWFZCertOpByK[op][k2] += DWFZMasses[k2][op][kl][kl]/5;     //div by 5 again to average over all classes
//                        DWFZCorrClByK[kl][k2] += DWFZClasses[k2][op][kl][kl]/280d;  //div by 280 instead of by 40 to average over all operators
//                        DWFZCertClByK[kl][k2] += DWFZMasses[k2][op][kl][kl]/7; 
//                        
//                        simpleFZCorrOpByK[op][k2] += simpleFZClasses[k2][op][kl][kl]/200d;  //div by 200 instead of div by 40 because we want the percent across all classes, and this is equivalent to adding up all class-specific percent corrects and dividing by 5.
//                        simpleFZCertOpByK[op][k2] += simpleFZMasses[k2][op][kl][kl]/5;     //div by 5 again to average over all classes
//                        simpleFZCorrClByK[kl][k2] += simpleFZClasses[k2][op][kl][kl]/280d;  //div by 280 instead of by 40 to average over all operators
//                        simpleFZCertClByK[kl][k2] += simpleFZMasses[k2][op][kl][kl]/7; 
//                        
//                        DWBaseCorrOpByK[op][k2] += DWBaseClasses[k2][op][kl][kl]/200d;  //div by 200 instead of div by 40 because we want the percent across all classes, and this is equivalent to adding up all class-specific percent corrects and dividing by 5.
//                        DWBaseCertOpByK[op][k2] += DWBaseMasses[k2][op][kl][kl]/5;     //div by 5 again to average over all classes
//                        DWBaseCorrClByK[kl][k2] += DWBaseClasses[k2][op][kl][kl]/280d;  //div by 280 instead of by 40 to average over all operators
//                        DWBaseCertClByK[kl][k2] += DWBaseMasses[k2][op][kl][kl]/7; 
//                        
//                        simpleBaseCorrOpByK[op][k2] += simpleBaseClasses[k2][op][kl][kl]/200d;  //div by 200 instead of div by 40 because we want the percent across all classes, and this is equivalent to adding up all class-specific percent corrects and dividing by 5.
//                        simpleBaseCertOpByK[op][k2] += simpleBaseMasses[k2][op][kl][kl]/5;     //div by 5 again to average over all classes
//                        simpleBaseCorrClByK[kl][k2] += simpleBaseClasses[k2][op][kl][kl]/280d;  //div by 280 instead of by 40 to average over all operators
//                        simpleBaseCertClByK[kl][k2] += simpleBaseMasses[k2][op][kl][kl]/7;                                                 
                    }
                }
            }               
            
            for (int k2 = 2; k2 < 302; k2++){                   
                DWGAMPctCorrectOperatorByKWriter.write("," + k2);
                DWGAMPctCorrectClassByKWriter.write("," + k2);                    
                DWGAMCertaintyOperatorByKWriter.write("," + k2);
                DWGAMCertaintyClassByKWriter.write("," + k2);    

                simpleGAMPctCorrectOperatorByKWriter.write("," + k2);
                simpleGAMPctCorrectClassByKWriter.write("," + k2);
                simpleGAMCertaintyOperatorByKWriter.write("," + k2);
                simpleGAMCertaintyClassByKWriter.write("," + k2); 

//                DWFZPctCorrectOperatorByKWriter.write("," + k2);
//                DWFZPctCorrectClassByKWriter.write("," + k2);
//                DWFZCertaintyOperatorByKWriter.write("," + k2);
//                DWFZCertaintyClassByKWriter.write("," + k2); 
//
//                simpleFZPctCorrectOperatorByKWriter.write("," + k2);
//                simpleFZPctCorrectClassByKWriter.write("," + k2);
//                simpleFZCertaintyOperatorByKWriter.write("," + k2);
//                simpleFZCertaintyClassByKWriter.write("," + k2); 
//
//                DWBasePctCorrectOperatorByKWriter.write("," + k2);
//                DWBasePctCorrectClassByKWriter.write("," + k2);
//                DWBaseCertaintyOperatorByKWriter.write("," + k2);
//                DWBaseCertaintyClassByKWriter.write("," + k2); 
//
//                simpleBasePctCorrectOperatorByKWriter.write("," + k2);
//                simpleBasePctCorrectClassByKWriter.write("," + k2);
//                simpleBaseCertaintyOperatorByKWriter.write("," + k2);
//                simpleBaseCertaintyClassByKWriter.write("," + k2);                     
            }            
            
            DWGAMPctCorrectOperatorByKWriter.write('\n');
            DWGAMPctCorrectClassByKWriter.write('\n');                    
            DWGAMCertaintyOperatorByKWriter.write('\n');
            DWGAMCertaintyClassByKWriter.write('\n');    

            simpleGAMPctCorrectOperatorByKWriter.write('\n');
            simpleGAMPctCorrectClassByKWriter.write('\n');
            simpleGAMCertaintyOperatorByKWriter.write('\n');
            simpleGAMCertaintyClassByKWriter.write('\n'); 

//            DWFZPctCorrectOperatorByKWriter.write('\n');
//            DWFZPctCorrectClassByKWriter.write('\n');
//            DWFZCertaintyOperatorByKWriter.write('\n');
//            DWFZCertaintyClassByKWriter.write('\n'); 
//
//            simpleFZPctCorrectOperatorByKWriter.write('\n');
//            simpleFZPctCorrectClassByKWriter.write('\n');
//            simpleFZCertaintyOperatorByKWriter.write('\n');
//            simpleFZCertaintyClassByKWriter.write('\n'); 
//
//            DWBasePctCorrectOperatorByKWriter.write('\n');
//            DWBasePctCorrectClassByKWriter.write('\n');
//            DWBaseCertaintyOperatorByKWriter.write('\n');
//            DWBaseCertaintyClassByKWriter.write('\n'); 
//
//            simpleBasePctCorrectOperatorByKWriter.write('\n');
//            simpleBasePctCorrectClassByKWriter.write('\n');
//            simpleBaseCertaintyOperatorByKWriter.write('\n');
//            simpleBaseCertaintyClassByKWriter.write('\n');               
            
            for (int index = 0; index < 5; index++){
                DWGAMPctCorrectOperatorByKWriter.write(operators[index] + ",");
                DWGAMPctCorrectClassByKWriter.write(classNames[index+1] + ",");                    
                DWGAMCertaintyOperatorByKWriter.write(operators[index] + ",");
                DWGAMCertaintyClassByKWriter.write(classNames[index+1] + ",");    

                simpleGAMPctCorrectOperatorByKWriter.write(operators[index] + ",");
                simpleGAMPctCorrectClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMCertaintyOperatorByKWriter.write(operators[index] + ",");
                simpleGAMCertaintyClassByKWriter.write(classNames[index+1] + ","); 

//                DWFZPctCorrectOperatorByKWriter.write(operators[index] + ",");
//                DWFZPctCorrectClassByKWriter.write(classNames[index+1] + ",");
//                DWFZCertaintyOperatorByKWriter.write(operators[index] + ",");
//                DWFZCertaintyClassByKWriter.write(classNames[index+1] + ","); 
//
//                simpleFZPctCorrectOperatorByKWriter.write(operators[index] + ",");
//                simpleFZPctCorrectClassByKWriter.write(classNames[index+1] + ",");
//                simpleFZCertaintyOperatorByKWriter.write(operators[index] + ",");
//                simpleFZCertaintyClassByKWriter.write(classNames[index+1] + ","); 
//
//                DWBasePctCorrectOperatorByKWriter.write(operators[index] + ",");
//                DWBasePctCorrectClassByKWriter.write(classNames[index+1] + ",");
//                DWBaseCertaintyOperatorByKWriter.write(operators[index] + ",");
//                DWBaseCertaintyClassByKWriter.write(classNames[index+1] + ","); 
//
//                simpleBasePctCorrectOperatorByKWriter.write(operators[index] + ",");
//                simpleBasePctCorrectClassByKWriter.write(classNames[index+1] + ",");
//                simpleBaseCertaintyOperatorByKWriter.write(operators[index] + ",");
//                simpleBaseCertaintyClassByKWriter.write(classNames[index+1] + ",");
                
                for (int k2 = 0; k2 < 300; k2++){                   
                    DWGAMPctCorrectOperatorByKWriter.write(DWGAMCorrOpByK[index][k2] + ",");
                    DWGAMPctCorrectClassByKWriter.write(DWGAMCorrClByK[index][k2] + ",");                    
                    DWGAMCertaintyOperatorByKWriter.write(DWGAMCertOpByK[index][k2] + ",");
                    DWGAMCertaintyClassByKWriter.write(DWGAMCertClByK[index][k2] + ",");    

                    simpleGAMPctCorrectOperatorByKWriter.write(simpleGAMCorrOpByK[index][k2] + ",");
                    simpleGAMPctCorrectClassByKWriter.write(simpleGAMCorrClByK[index][k2] + ",");
                    simpleGAMCertaintyOperatorByKWriter.write(simpleGAMCertOpByK[index][k2] + ",");
                    simpleGAMCertaintyClassByKWriter.write(simpleGAMCertClByK[index][k2] + ","); 

//                    DWFZPctCorrectOperatorByKWriter.write(DWFZCorrOpByK[index][k2] + ",");
//                    DWFZPctCorrectClassByKWriter.write(DWFZCorrClByK[index][k2] + ",");
//                    DWFZCertaintyOperatorByKWriter.write(DWFZCertOpByK[index][k2] + ",");
//                    DWFZCertaintyClassByKWriter.write(DWFZCertClByK[index][k2] + ","); 
//
//                    simpleFZPctCorrectOperatorByKWriter.write(simpleFZCorrOpByK[index][k2] + ",");
//                    simpleFZPctCorrectClassByKWriter.write(simpleFZCorrClByK[index][k2] + ",");
//                    simpleFZCertaintyOperatorByKWriter.write(simpleFZCertOpByK[index][k2] + ",");
//                    simpleFZCertaintyClassByKWriter.write(simpleFZCertClByK[index][k2] + ","); 
//
//                    DWBasePctCorrectOperatorByKWriter.write(DWBaseCorrOpByK[index][k2] + ",");
//                    DWBasePctCorrectClassByKWriter.write(DWBaseCorrClByK[index][k2] + ",");
//                    DWBaseCertaintyOperatorByKWriter.write(DWBaseCertOpByK[index][k2] + ",");
//                    DWBaseCertaintyClassByKWriter.write(DWBaseCertClByK[index][k2] + ","); 
//
//                    simpleBasePctCorrectOperatorByKWriter.write(simpleBaseCorrOpByK[index][k2] + ",");
//                    simpleBasePctCorrectClassByKWriter.write(simpleBaseCorrClByK[index][k2] + ",");
//                    simpleBaseCertaintyOperatorByKWriter.write(simpleBaseCertOpByK[index][k2] + ",");
//                    simpleBaseCertaintyClassByKWriter.write(simpleBaseCertClByK[index][k2] + ",");                     
                }
                DWGAMPctCorrectOperatorByKWriter.write('\n');
                DWGAMPctCorrectClassByKWriter.write('\n');                    
                DWGAMCertaintyOperatorByKWriter.write('\n');
                DWGAMCertaintyClassByKWriter.write('\n');    

                simpleGAMPctCorrectOperatorByKWriter.write('\n');
                simpleGAMPctCorrectClassByKWriter.write('\n');
                simpleGAMCertaintyOperatorByKWriter.write('\n');
                simpleGAMCertaintyClassByKWriter.write('\n'); 

//                DWFZPctCorrectOperatorByKWriter.write('\n');
//                DWFZPctCorrectClassByKWriter.write('\n');
//                DWFZCertaintyOperatorByKWriter.write('\n');
//                DWFZCertaintyClassByKWriter.write('\n'); 
//
//                simpleFZPctCorrectOperatorByKWriter.write('\n');
//                simpleFZPctCorrectClassByKWriter.write('\n');
//                simpleFZCertaintyOperatorByKWriter.write('\n');
//                simpleFZCertaintyClassByKWriter.write('\n'); 
//
//                DWBasePctCorrectOperatorByKWriter.write('\n');
//                DWBasePctCorrectClassByKWriter.write('\n');
//                DWBaseCertaintyOperatorByKWriter.write('\n');
//                DWBaseCertaintyClassByKWriter.write('\n'); 
//
//                simpleBasePctCorrectOperatorByKWriter.write('\n');
//                simpleBasePctCorrectClassByKWriter.write('\n');
//                simpleBaseCertaintyOperatorByKWriter.write('\n');
//                simpleBaseCertaintyClassByKWriter.write('\n');   
                
                System.out.println("finished writing row " + index);
            }
            
            for (int blob = 5; blob < 7; blob++){
                DWGAMPctCorrectOperatorByKWriter.write(operators[blob] + ",");                   
                DWGAMCertaintyOperatorByKWriter.write(operators[blob] + ",");  

                simpleGAMPctCorrectOperatorByKWriter.write(operators[blob] + ",");
                simpleGAMCertaintyOperatorByKWriter.write(operators[blob] + ",");

//                DWFZPctCorrectOperatorByKWriter.write(operators[blob] + ",");
//                DWFZCertaintyOperatorByKWriter.write(operators[blob] + ",");
//
//                simpleFZPctCorrectOperatorByKWriter.write(operators[blob] + ",");
//                simpleFZCertaintyOperatorByKWriter.write(operators[blob] + ",");
//
//                DWBasePctCorrectOperatorByKWriter.write(operators[blob] + ",");
//                DWBaseCertaintyOperatorByKWriter.write(operators[blob] + ",");
//
//                simpleBasePctCorrectOperatorByKWriter.write(operators[blob] + ",");
//                simpleBaseCertaintyOperatorByKWriter.write(operators[blob] + ",");            

                for (int k2 = 0; k2 < 300; k2++){                   
                    DWGAMPctCorrectOperatorByKWriter.write(DWGAMCorrOpByK[blob][k2] + ",");                
                    DWGAMCertaintyOperatorByKWriter.write(DWGAMCertOpByK[blob][k2] + ",");

                    simpleGAMPctCorrectOperatorByKWriter.write(simpleGAMCorrOpByK[blob][k2] + ",");
                    simpleGAMCertaintyOperatorByKWriter.write(simpleGAMCertOpByK[blob][k2] + ",");

//                    DWFZPctCorrectOperatorByKWriter.write(DWFZCorrOpByK[blob][k2] + ",");
//                    DWFZCertaintyOperatorByKWriter.write(DWFZCertOpByK[blob][k2] + ",");
//
//                    simpleFZPctCorrectOperatorByKWriter.write(simpleFZCorrOpByK[blob][k2] + ",");
//                    simpleFZCertaintyOperatorByKWriter.write(simpleFZCertOpByK[blob][k2] + ",");
//
//                    DWBasePctCorrectOperatorByKWriter.write(DWBaseCorrOpByK[blob][k2] + ",");
//                    DWBaseCertaintyOperatorByKWriter.write(DWBaseCertOpByK[blob][k2] + ",");
//
//                    simpleBasePctCorrectOperatorByKWriter.write(simpleBaseCorrOpByK[blob][k2] + ",");
//                    simpleBaseCertaintyOperatorByKWriter.write(simpleBaseCertOpByK[blob][k2] + ",");                  
                }      
                
                DWGAMPctCorrectOperatorByKWriter.write('\n');             
                DWGAMCertaintyOperatorByKWriter.write('\n');

                simpleGAMPctCorrectOperatorByKWriter.write('\n');
                simpleGAMCertaintyOperatorByKWriter.write('\n');

//                DWFZPctCorrectOperatorByKWriter.write('\n');
//                DWFZCertaintyOperatorByKWriter.write('\n');
//
//                simpleFZPctCorrectOperatorByKWriter.write('\n');
//                simpleFZCertaintyOperatorByKWriter.write('\n');
//
//                DWBasePctCorrectOperatorByKWriter.write('\n');
//                DWBaseCertaintyOperatorByKWriter.write('\n');
//
//                simpleBasePctCorrectOperatorByKWriter.write('\n');
//                simpleBaseCertaintyOperatorByKWriter.write('\n');
                
                System.out.println("finished writing row " + blob);                
            }
            
            DWGAMPctCorrectOperatorByKWriter.close();
            DWGAMPctCorrectClassByKWriter.close();                    
            DWGAMCertaintyOperatorByKWriter.close();
            DWGAMCertaintyClassByKWriter.close();    

            simpleGAMPctCorrectOperatorByKWriter.close();
            simpleGAMPctCorrectClassByKWriter.close();
            simpleGAMCertaintyOperatorByKWriter.close();
            simpleGAMCertaintyClassByKWriter.close(); 

//            DWFZPctCorrectOperatorByKWriter.close();
//            DWFZPctCorrectClassByKWriter.close();
//            DWFZCertaintyOperatorByKWriter.close();
//            DWFZCertaintyClassByKWriter.close(); 
//
//            simpleFZPctCorrectOperatorByKWriter.close();
//            simpleFZPctCorrectClassByKWriter.close();
//            simpleFZCertaintyOperatorByKWriter.close();
//            simpleFZCertaintyClassByKWriter.close(); 
//
//            DWBasePctCorrectOperatorByKWriter.close();
//            DWBasePctCorrectClassByKWriter.close();
//            DWBaseCertaintyOperatorByKWriter.close();
//            DWBaseCertaintyClassByKWriter.close(); 
//
//            simpleBasePctCorrectOperatorByKWriter.close();
//            simpleBasePctCorrectClassByKWriter.close();
//            simpleBaseCertaintyOperatorByKWriter.close();
//            simpleBaseCertaintyClassByKWriter.close();              
            
        } catch (IOException ioe){
            System.out.println("ioexception: " + ioe.getMessage());
        }        
    }

    private static void writeClassSummaries(){
        double[][] DWGAMCorrModeClByK = new double[6][300];
        double[][] DWGAMCorrAverageClByK = new double[6][300];
        double[][] DWGAMCorrDSClByK = new double[6][300];
        double[][] DWGAMCorrPBTClByK = new double[6][300];
        double[][] DWGAMCorrCombAveClByK = new double[6][300];
        double[][] DWGAMCorrModAveClByK = new double[6][300];
        double[][] DWGAMCorrTreePBTClByK = new double[6][300];
        
        double[][] DWGAMCertModeClByK = new double[6][300];
        double[][] DWGAMCertAverageClByK = new double[6][300];
        double[][] DWGAMCertDSClByK = new double[6][300];
        double[][] DWGAMCertPBTClByK = new double[6][300];
        double[][] DWGAMCertCombAveClByK = new double[6][300];
        double[][] DWGAMCertModAveClByK = new double[6][300];
        double[][] DWGAMCertTreePBTClByK = new double[6][300];
        
//        double[][] DWBaseCorrModeClByK = new double[6][300];
//        double[][] DWBaseCorrAverageClByK = new double[6][300];
//        double[][] DWBaseCorrDSClByK = new double[6][300];
//        double[][] DWBaseCorrPBTClByK = new double[6][300];
//        double[][] DWBaseCorrCombAveClByK = new double[6][300];
//        double[][] DWBaseCorrModAveClByK = new double[6][300];
//        double[][] DWBaseCorrTreePBTClByK = new double[6][300];
//        
//        double[][] DWBaseCertModeClByK = new double[6][300];
//        double[][] DWBaseCertAverageClByK = new double[6][300];
//        double[][] DWBaseCertDSClByK = new double[6][300];
//        double[][] DWBaseCertPBTClByK = new double[6][300];
//        double[][] DWBaseCertCombAveClByK = new double[6][300];
//        double[][] DWBaseCertModAveClByK = new double[6][300];
//        double[][] DWBaseCertTreePBTClByK = new double[6][300];
        
        File DWGAMpctCorrectModeClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMPctCorrectModeClassByK.csv");
        File DWGAMpctCorrectAverageClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMPctCorrectAverageClassByK.csv");
        File DWGAMpctCorrectDSClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMPctCorrectDSClassByK.csv");
        File DWGAMpctCorrectPBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMPctCorrectPBTClassByK.csv");
        File DWGAMpctCorrectCombAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMPctCorrectCombAveClassByK.csv");
        File DWGAMpctCorrectModAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMPctCorrectModAveClassByK.csv");
        File DWGAMpctCorrectTreePBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMPctCorrectTreePBTClassByK.csv");

        File DWGAMcertaintyModeClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMCertaintyModeClassByK.csv");
        File DWGAMcertaintyAverageClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMCertaintyAverageClassByK.csv");
        File DWGAMcertaintyDSClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMCertaintyDSClassByK.csv");
        File DWGAMcertaintyPBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMCertaintyPBTClassByK.csv");
        File DWGAMcertaintyCombAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMCertaintyCombAveClassByK.csv");
        File DWGAMcertaintyModAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMCertaintyModAveClassByK.csv");
        File DWGAMcertaintyTreePBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/DWGAMCertaintyTreePBTClassByK.csv");        
        
//        File DWBasepctCorrectModeClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBasePctCorrectModeClassByK.csv");
//        File DWBasepctCorrectAverageClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBasePctCorrectAverageClassByK.csv");
//        File DWBasepctCorrectDSClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBasePctCorrectDSClassByK.csv");
//        File DWBasepctCorrectPBTClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBasePctCorrectPBTClassByK.csv");
//        File DWBasepctCorrectCombAveClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBasePctCorrectCombAveClassByK.csv");
//        File DWBasepctCorrectModAveClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBasePctCorrectModAveClassByK.csv");
//        File DWBasepctCorrectTreePBTClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBasePctCorrectTreePBTClassByK.csv");
//
//        File DWBasecertaintyModeClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBaseCertaintyModeClassByK.csv");
//        File DWBasecertaintyAverageClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBaseCertaintyAverageClassByK.csv");
//        File DWBasecertaintyDSClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBaseCertaintyDSClassByK.csv");
//        File DWBasecertaintyPBTClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBaseCertaintyPBTClassByK.csv");
//        File DWBasecertaintyCombAveClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBaseCertaintyCombAveClassByK.csv");
//        File DWBasecertaintyModAveClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBaseCertaintyModAveClassByK.csv");
//        File DWBasecertaintyTreePBTClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/DWBaseCertaintyTreePBTClassByK.csv");          
        
        double[][] simpleGAMCorrModeClByK = new double[6][300];
        double[][] simpleGAMCorrAverageClByK = new double[6][300];
        double[][] simpleGAMCorrDSClByK = new double[6][300];
        double[][] simpleGAMCorrPBTClByK = new double[6][300];
        double[][] simpleGAMCorrCombAveClByK = new double[6][300];
        double[][] simpleGAMCorrModAveClByK = new double[6][300];
        double[][] simpleGAMCorrTreePBTClByK = new double[6][300];
        
        double[][] simpleGAMCertModeClByK = new double[6][300];
        double[][] simpleGAMCertAverageClByK = new double[6][300];
        double[][] simpleGAMCertDSClByK = new double[6][300];
        double[][] simpleGAMCertPBTClByK = new double[6][300];
        double[][] simpleGAMCertCombAveClByK = new double[6][300];
        double[][] simpleGAMCertModAveClByK = new double[6][300];
        double[][] simpleGAMCertTreePBTClByK = new double[6][300];
        
//        double[][] simpleBaseCorrModeClByK = new double[6][300];
//        double[][] simpleBaseCorrAverageClByK = new double[6][300];
//        double[][] simpleBaseCorrDSClByK = new double[6][300];
//        double[][] simpleBaseCorrPBTClByK = new double[6][300];
//        double[][] simpleBaseCorrCombAveClByK = new double[6][300];
//        double[][] simpleBaseCorrModAveClByK = new double[6][300];
//        double[][] simpleBaseCorrTreePBTClByK = new double[6][300];
//        
//        double[][] simpleBaseCertModeClByK = new double[6][300];
//        double[][] simpleBaseCertAverageClByK = new double[6][300];
//        double[][] simpleBaseCertDSClByK = new double[6][300];
//        double[][] simpleBaseCertPBTClByK = new double[6][300];
//        double[][] simpleBaseCertCombAveClByK = new double[6][300];
//        double[][] simpleBaseCertModAveClByK = new double[6][300];
//        double[][] simpleBaseCertTreePBTClByK = new double[6][300];
        
        File simpleGAMpctCorrectModeClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMPctCorrectModeClassByK.csv");
        File simpleGAMpctCorrectAverageClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMPctCorrectAverageClassByK.csv");
        File simpleGAMpctCorrectDSClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMPctCorrectDSClassByK.csv");
        File simpleGAMpctCorrectPBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMPctCorrectPBTClassByK.csv");
        File simpleGAMpctCorrectCombAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMPctCorrectCombAveClassByK.csv");
        File simpleGAMpctCorrectModAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMPctCorrectModAveClassByK.csv");
        File simpleGAMpctCorrectTreePBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMPctCorrectTreePBTClassByK.csv");

        File simpleGAMcertaintyModeClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMCertaintyModeClassByK.csv");
        File simpleGAMcertaintyAverageClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMCertaintyAverageClassByK.csv");
        File simpleGAMcertaintyDSClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMCertaintyDSClassByK.csv");
        File simpleGAMcertaintyPBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMCertaintyPBTClassByK.csv");
        File simpleGAMcertaintyCombAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMCertaintyCombAveClassByK.csv");
        File simpleGAMcertaintyModAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMCertaintyModAveClassByK.csv");
        File simpleGAMcertaintyTreePBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/simpleGAMCertaintyTreePBTClassByK.csv");        
        
//        File simpleBasepctCorrectModeClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBasePctCorrectModeClassByK.csv");
//        File simpleBasepctCorrectAverageClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBasePctCorrectAverageClassByK.csv");
//        File simpleBasepctCorrectDSClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBasePctCorrectDSClassByK.csv");
//        File simpleBasepctCorrectPBTClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBasePctCorrectPBTClassByK.csv");
//        File simpleBasepctCorrectCombAveClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBasePctCorrectCombAveClassByK.csv");
//        File simpleBasepctCorrectModAveClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBasePctCorrectModAveClassByK.csv");
//        File simpleBasepctCorrectTreePBTClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBasePctCorrectTreePBTClassByK.csv");
//
//        File simpleBasecertaintyModeClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBaseCertaintyModeClassByK.csv");
//        File simpleBasecertaintyAverageClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBaseCertaintyAverageClassByK.csv");
//        File simpleBasecertaintyDSClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBaseCertaintyDSClassByK.csv");
//        File simpleBasecertaintyPBTClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBaseCertaintyPBTClassByK.csv");
//        File simpleBasecertaintyCombAveClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBaseCertaintyCombAveClassByK.csv");
//        File simpleBasecertaintyModAveClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBaseCertaintyModAveClassByK.csv");
//        File simpleBasecertaintyTreePBTClassByK = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/byClass/simpleBaseCertaintyTreePBTClassByK.csv");          
        
        
        try{
            FileWriter DWGAMPctCorrectModeClassByKWriter = new FileWriter(DWGAMpctCorrectModeClassByK);
            FileWriter DWGAMPctCorrectAverageClassByKWriter = new FileWriter(DWGAMpctCorrectAverageClassByK);
            FileWriter DWGAMPctCorrectDSClassByKWriter = new FileWriter(DWGAMpctCorrectDSClassByK);
            FileWriter DWGAMPctCorrectPBTClassByKWriter = new FileWriter(DWGAMpctCorrectPBTClassByK);
            FileWriter DWGAMPctCorrectCombAveClassByKWriter = new FileWriter(DWGAMpctCorrectCombAveClassByK);
            FileWriter DWGAMPctCorrectModAveClassByKWriter = new FileWriter(DWGAMpctCorrectModAveClassByK);
            FileWriter DWGAMPctCorrectTreePBTClassByKWriter = new FileWriter(DWGAMpctCorrectTreePBTClassByK);

            FileWriter DWGAMCertaintyModeClassByKWriter = new FileWriter(DWGAMcertaintyModeClassByK);
            FileWriter DWGAMCertaintyAverageClassByKWriter = new FileWriter(DWGAMcertaintyAverageClassByK);
            FileWriter DWGAMCertaintyDSClassByKWriter = new FileWriter(DWGAMcertaintyDSClassByK);
            FileWriter DWGAMCertaintyPBTClassByKWriter = new FileWriter(DWGAMcertaintyPBTClassByK);
            FileWriter DWGAMCertaintyCombAveClassByKWriter = new FileWriter(DWGAMcertaintyCombAveClassByK);
            FileWriter DWGAMCertaintyModAveClassByKWriter = new FileWriter(DWGAMcertaintyModAveClassByK);
            FileWriter DWGAMCertaintyTreePBTClassByKWriter = new FileWriter(DWGAMcertaintyTreePBTClassByK);            
            
//            FileWriter DWBasePctCorrectModeClassByKWriter = new FileWriter(DWBasepctCorrectModeClassByK);
//            FileWriter DWBasePctCorrectAverageClassByKWriter = new FileWriter(DWBasepctCorrectAverageClassByK);
//            FileWriter DWBasePctCorrectDSClassByKWriter = new FileWriter(DWBasepctCorrectDSClassByK);
//            FileWriter DWBasePctCorrectPBTClassByKWriter = new FileWriter(DWBasepctCorrectPBTClassByK);
//            FileWriter DWBasePctCorrectCombAveClassByKWriter = new FileWriter(DWBasepctCorrectCombAveClassByK);
//            FileWriter DWBasePctCorrectModAveClassByKWriter = new FileWriter(DWBasepctCorrectModAveClassByK);
//            FileWriter DWBasePctCorrectTreePBTClassByKWriter = new FileWriter(DWBasepctCorrectTreePBTClassByK);
//
//            FileWriter DWBaseCertaintyModeClassByKWriter = new FileWriter(DWBasecertaintyModeClassByK);
//            FileWriter DWBaseCertaintyAverageClassByKWriter = new FileWriter(DWBasecertaintyAverageClassByK);
//            FileWriter DWBaseCertaintyDSClassByKWriter = new FileWriter(DWBasecertaintyDSClassByK);
//            FileWriter DWBaseCertaintyPBTClassByKWriter = new FileWriter(DWBasecertaintyPBTClassByK);
//            FileWriter DWBaseCertaintyCombAveClassByKWriter = new FileWriter(DWBasecertaintyCombAveClassByK);
//            FileWriter DWBaseCertaintyModAveClassByKWriter = new FileWriter(DWBasecertaintyModAveClassByK);
//            FileWriter DWBaseCertaintyTreePBTClassByKWriter = new FileWriter(DWBasecertaintyTreePBTClassByK);                
            
            FileWriter simpleGAMPctCorrectModeClassByKWriter = new FileWriter(simpleGAMpctCorrectModeClassByK);
            FileWriter simpleGAMPctCorrectAverageClassByKWriter = new FileWriter(simpleGAMpctCorrectAverageClassByK);
            FileWriter simpleGAMPctCorrectDSClassByKWriter = new FileWriter(simpleGAMpctCorrectDSClassByK);
            FileWriter simpleGAMPctCorrectPBTClassByKWriter = new FileWriter(simpleGAMpctCorrectPBTClassByK);
            FileWriter simpleGAMPctCorrectCombAveClassByKWriter = new FileWriter(simpleGAMpctCorrectCombAveClassByK);
            FileWriter simpleGAMPctCorrectModAveClassByKWriter = new FileWriter(simpleGAMpctCorrectModAveClassByK);
            FileWriter simpleGAMPctCorrectTreePBTClassByKWriter = new FileWriter(simpleGAMpctCorrectTreePBTClassByK);

            FileWriter simpleGAMCertaintyModeClassByKWriter = new FileWriter(simpleGAMcertaintyModeClassByK);
            FileWriter simpleGAMCertaintyAverageClassByKWriter = new FileWriter(simpleGAMcertaintyAverageClassByK);
            FileWriter simpleGAMCertaintyDSClassByKWriter = new FileWriter(simpleGAMcertaintyDSClassByK);
            FileWriter simpleGAMCertaintyPBTClassByKWriter = new FileWriter(simpleGAMcertaintyPBTClassByK);
            FileWriter simpleGAMCertaintyCombAveClassByKWriter = new FileWriter(simpleGAMcertaintyCombAveClassByK);
            FileWriter simpleGAMCertaintyModAveClassByKWriter = new FileWriter(simpleGAMcertaintyModAveClassByK);
            FileWriter simpleGAMCertaintyTreePBTClassByKWriter = new FileWriter(simpleGAMcertaintyTreePBTClassByK);            
            
//            FileWriter simpleBasePctCorrectModeClassByKWriter = new FileWriter(simpleBasepctCorrectModeClassByK);
//            FileWriter simpleBasePctCorrectAverageClassByKWriter = new FileWriter(simpleBasepctCorrectAverageClassByK);
//            FileWriter simpleBasePctCorrectDSClassByKWriter = new FileWriter(simpleBasepctCorrectDSClassByK);
//            FileWriter simpleBasePctCorrectPBTClassByKWriter = new FileWriter(simpleBasepctCorrectPBTClassByK);
//            FileWriter simpleBasePctCorrectCombAveClassByKWriter = new FileWriter(simpleBasepctCorrectCombAveClassByK);
//            FileWriter simpleBasePctCorrectModAveClassByKWriter = new FileWriter(simpleBasepctCorrectModAveClassByK);
//            FileWriter simpleBasePctCorrectTreePBTClassByKWriter = new FileWriter(simpleBasepctCorrectTreePBTClassByK);
//
//            FileWriter simpleBaseCertaintyModeClassByKWriter = new FileWriter(simpleBasecertaintyModeClassByK);
//            FileWriter simpleBaseCertaintyAverageClassByKWriter = new FileWriter(simpleBasecertaintyAverageClassByK);
//            FileWriter simpleBaseCertaintyDSClassByKWriter = new FileWriter(simpleBasecertaintyDSClassByK);
//            FileWriter simpleBaseCertaintyPBTClassByKWriter = new FileWriter(simpleBasecertaintyPBTClassByK);
//            FileWriter simpleBaseCertaintyCombAveClassByKWriter = new FileWriter(simpleBasecertaintyCombAveClassByK);
//            FileWriter simpleBaseCertaintyModAveClassByKWriter = new FileWriter(simpleBasecertaintyModAveClassByK);
//            FileWriter simpleBaseCertaintyTreePBTClassByKWriter = new FileWriter(simpleBasecertaintyTreePBTClassByK);                  
            
//            for (int op = 0; op < 7; op++){
                for (int k2 = 0; k2 < 300; k2++){
                    for (int kl = 0; kl < 6; kl++){                    
                        DWGAMCorrModeClByK[kl][k2] += DWGAMClasses[k2][0][kl][kl]/40d;     
                        DWGAMCorrAverageClByK[kl][k2] += DWGAMClasses[k2][1][kl][kl]/40d;
                        DWGAMCorrDSClByK[kl][k2] += DWGAMClasses[k2][2][kl][kl]/40d;
                        DWGAMCorrPBTClByK[kl][k2] += DWGAMClasses[k2][3][kl][kl]/40d;
                        DWGAMCorrCombAveClByK[kl][k2] += DWGAMClasses[k2][4][kl][kl]/40d;
                        DWGAMCorrModAveClByK[kl][k2] += DWGAMClasses[k2][5][kl][kl]/40d;
                        DWGAMCorrTreePBTClByK[kl][k2] += DWGAMClasses[k2][6][kl][kl]/40d;
                        
                        DWGAMCertModeClByK[kl][k2] += DWGAMMasses[k2][0][kl][kl];     
                        DWGAMCertAverageClByK[kl][k2] += DWGAMMasses[k2][1][kl][kl];
                        DWGAMCertDSClByK[kl][k2] += DWGAMMasses[k2][2][kl][kl];
                        DWGAMCertPBTClByK[kl][k2] += DWGAMMasses[k2][3][kl][kl];
                        DWGAMCertCombAveClByK[kl][k2] += DWGAMMasses[k2][4][kl][kl];
                        DWGAMCertModAveClByK[kl][k2] += DWGAMMasses[k2][5][kl][kl];
                        DWGAMCertTreePBTClByK[kl][k2] += DWGAMMasses[k2][6][kl][kl];              
                        
//                        DWBaseCorrModeClByK[kl][k2] += DWBaseClasses[k2][0][kl][kl]/40d;     
//                        DWBaseCorrAverageClByK[kl][k2] += DWBaseClasses[k2][1][kl][kl]/40d;
//                        DWBaseCorrDSClByK[kl][k2] += DWBaseClasses[k2][2][kl][kl]/40d;
//                        DWBaseCorrPBTClByK[kl][k2] += DWBaseClasses[k2][3][kl][kl]/40d;
//                        DWBaseCorrCombAveClByK[kl][k2] += DWBaseClasses[k2][4][kl][kl]/40d;
//                        DWBaseCorrModAveClByK[kl][k2] += DWBaseClasses[k2][5][kl][kl]/40d;
//                        DWBaseCorrTreePBTClByK[kl][k2] += DWBaseClasses[k2][6][kl][kl]/40d;
//                        
//                        DWBaseCertModeClByK[kl][k2] += DWBaseMasses[k2][0][kl][kl];     
//                        DWBaseCertAverageClByK[kl][k2] += DWBaseMasses[k2][1][kl][kl];
//                        DWBaseCertDSClByK[kl][k2] += DWBaseMasses[k2][2][kl][kl];
//                        DWBaseCertPBTClByK[kl][k2] += DWBaseMasses[k2][3][kl][kl];
//                        DWBaseCertCombAveClByK[kl][k2] += DWBaseMasses[k2][4][kl][kl];
//                        DWBaseCertModAveClByK[kl][k2] += DWBaseMasses[k2][5][kl][kl];
//                        DWBaseCertTreePBTClByK[kl][k2] += DWBaseMasses[k2][6][kl][kl];         
                        
                        simpleGAMCorrModeClByK[kl][k2] += simpleGAMClasses[k2][0][kl][kl]/40d;     
                        simpleGAMCorrAverageClByK[kl][k2] += simpleGAMClasses[k2][1][kl][kl]/40d;
                        simpleGAMCorrDSClByK[kl][k2] += simpleGAMClasses[k2][2][kl][kl]/40d;
                        simpleGAMCorrPBTClByK[kl][k2] += simpleGAMClasses[k2][3][kl][kl]/40d;
                        simpleGAMCorrCombAveClByK[kl][k2] += simpleGAMClasses[k2][4][kl][kl]/40d;
                        simpleGAMCorrModAveClByK[kl][k2] += simpleGAMClasses[k2][5][kl][kl]/40d;
                        simpleGAMCorrTreePBTClByK[kl][k2] += simpleGAMClasses[k2][6][kl][kl]/40d;
                        
                        simpleGAMCertModeClByK[kl][k2] += simpleGAMMasses[k2][0][kl][kl];     
                        simpleGAMCertAverageClByK[kl][k2] += simpleGAMMasses[k2][1][kl][kl];
                        simpleGAMCertDSClByK[kl][k2] += simpleGAMMasses[k2][2][kl][kl];
                        simpleGAMCertPBTClByK[kl][k2] += simpleGAMMasses[k2][3][kl][kl];
                        simpleGAMCertCombAveClByK[kl][k2] += simpleGAMMasses[k2][4][kl][kl];
                        simpleGAMCertModAveClByK[kl][k2] += simpleGAMMasses[k2][5][kl][kl];
                        simpleGAMCertTreePBTClByK[kl][k2] += simpleGAMMasses[k2][6][kl][kl];              
                        
//                        simpleBaseCorrModeClByK[kl][k2] += simpleBaseClasses[k2][0][kl][kl]/40d;     
//                        simpleBaseCorrAverageClByK[kl][k2] += simpleBaseClasses[k2][1][kl][kl]/40d;
//                        simpleBaseCorrDSClByK[kl][k2] += simpleBaseClasses[k2][2][kl][kl]/40d;
//                        simpleBaseCorrPBTClByK[kl][k2] += simpleBaseClasses[k2][3][kl][kl]/40d;
//                        simpleBaseCorrCombAveClByK[kl][k2] += simpleBaseClasses[k2][4][kl][kl]/40d;
//                        simpleBaseCorrModAveClByK[kl][k2] += simpleBaseClasses[k2][5][kl][kl]/40d;
//                        simpleBaseCorrTreePBTClByK[kl][k2] += simpleBaseClasses[k2][6][kl][kl]/40d;
//                        
//                        simpleBaseCertModeClByK[kl][k2] += simpleBaseMasses[k2][0][kl][kl];     
//                        simpleBaseCertAverageClByK[kl][k2] += simpleBaseMasses[k2][1][kl][kl];
//                        simpleBaseCertDSClByK[kl][k2] += simpleBaseMasses[k2][2][kl][kl];
//                        simpleBaseCertPBTClByK[kl][k2] += simpleBaseMasses[k2][3][kl][kl];
//                        simpleBaseCertCombAveClByK[kl][k2] += simpleBaseMasses[k2][4][kl][kl];
//                        simpleBaseCertModAveClByK[kl][k2] += simpleBaseMasses[k2][5][kl][kl];
//                        simpleBaseCertTreePBTClByK[kl][k2] += simpleBaseMasses[k2][6][kl][kl];                                   
                    }
                }
//            }               
            
            for (int k2 = 2; k2 < 302; k2++){                   
                DWGAMPctCorrectModeClassByKWriter.write("," + k2);
                DWGAMPctCorrectAverageClassByKWriter.write("," + k2);
                DWGAMPctCorrectDSClassByKWriter.write("," + k2);
                DWGAMPctCorrectPBTClassByKWriter.write("," + k2);
                DWGAMPctCorrectCombAveClassByKWriter.write("," + k2);
                DWGAMPctCorrectModAveClassByKWriter.write("," + k2);
                DWGAMPctCorrectTreePBTClassByKWriter.write("," + k2);
                
                DWGAMCertaintyModeClassByKWriter.write("," + k2);
                DWGAMCertaintyAverageClassByKWriter.write("," + k2);
                DWGAMCertaintyDSClassByKWriter.write("," + k2);
                DWGAMCertaintyPBTClassByKWriter.write("," + k2);
                DWGAMCertaintyCombAveClassByKWriter.write("," + k2);
                DWGAMCertaintyModAveClassByKWriter.write("," + k2);
                DWGAMCertaintyTreePBTClassByKWriter.write("," + k2);       
                
//                DWBasePctCorrectModeClassByKWriter.write("," + k2);
//                DWBasePctCorrectAverageClassByKWriter.write("," + k2);
//                DWBasePctCorrectDSClassByKWriter.write("," + k2);
//                DWBasePctCorrectPBTClassByKWriter.write("," + k2);
//                DWBasePctCorrectCombAveClassByKWriter.write("," + k2);
//                DWBasePctCorrectModAveClassByKWriter.write("," + k2);
//                DWBasePctCorrectTreePBTClassByKWriter.write("," + k2);
//                
//                DWBaseCertaintyModeClassByKWriter.write("," + k2);
//                DWBaseCertaintyAverageClassByKWriter.write("," + k2);
//                DWBaseCertaintyDSClassByKWriter.write("," + k2);
//                DWBaseCertaintyPBTClassByKWriter.write("," + k2);
//                DWBaseCertaintyCombAveClassByKWriter.write("," + k2);
//                DWBaseCertaintyModAveClassByKWriter.write("," + k2);
//                DWBaseCertaintyTreePBTClassByKWriter.write("," + k2);  
                
                simpleGAMPctCorrectModeClassByKWriter.write("," + k2);
                simpleGAMPctCorrectAverageClassByKWriter.write("," + k2);
                simpleGAMPctCorrectDSClassByKWriter.write("," + k2);
                simpleGAMPctCorrectPBTClassByKWriter.write("," + k2);
                simpleGAMPctCorrectCombAveClassByKWriter.write("," + k2);
                simpleGAMPctCorrectModAveClassByKWriter.write("," + k2);
                simpleGAMPctCorrectTreePBTClassByKWriter.write("," + k2);
                
                simpleGAMCertaintyModeClassByKWriter.write("," + k2);
                simpleGAMCertaintyAverageClassByKWriter.write("," + k2);
                simpleGAMCertaintyDSClassByKWriter.write("," + k2);
                simpleGAMCertaintyPBTClassByKWriter.write("," + k2);
                simpleGAMCertaintyCombAveClassByKWriter.write("," + k2);
                simpleGAMCertaintyModAveClassByKWriter.write("," + k2);
                simpleGAMCertaintyTreePBTClassByKWriter.write("," + k2);       
                
//                simpleBasePctCorrectModeClassByKWriter.write("," + k2);
//                simpleBasePctCorrectAverageClassByKWriter.write("," + k2);
//                simpleBasePctCorrectDSClassByKWriter.write("," + k2);
//                simpleBasePctCorrectPBTClassByKWriter.write("," + k2);
//                simpleBasePctCorrectCombAveClassByKWriter.write("," + k2);
//                simpleBasePctCorrectModAveClassByKWriter.write("," + k2);
//                simpleBasePctCorrectTreePBTClassByKWriter.write("," + k2);
//                
//                simpleBaseCertaintyModeClassByKWriter.write("," + k2);
//                simpleBaseCertaintyAverageClassByKWriter.write("," + k2);
//                simpleBaseCertaintyDSClassByKWriter.write("," + k2);
//                simpleBaseCertaintyPBTClassByKWriter.write("," + k2);
//                simpleBaseCertaintyCombAveClassByKWriter.write("," + k2);
//                simpleBaseCertaintyModAveClassByKWriter.write("," + k2);
//                simpleBaseCertaintyTreePBTClassByKWriter.write("," + k2);                   
            }            
            
            DWGAMPctCorrectModeClassByKWriter.write('\n');
            DWGAMPctCorrectAverageClassByKWriter.write('\n');
            DWGAMPctCorrectDSClassByKWriter.write('\n');
            DWGAMPctCorrectPBTClassByKWriter.write('\n');
            DWGAMPctCorrectCombAveClassByKWriter.write('\n');
            DWGAMPctCorrectModAveClassByKWriter.write('\n');
            DWGAMPctCorrectTreePBTClassByKWriter.write('\n');

            DWGAMCertaintyModeClassByKWriter.write('\n');
            DWGAMCertaintyAverageClassByKWriter.write('\n');
            DWGAMCertaintyDSClassByKWriter.write('\n');
            DWGAMCertaintyPBTClassByKWriter.write('\n');
            DWGAMCertaintyCombAveClassByKWriter.write('\n');
            DWGAMCertaintyModAveClassByKWriter.write('\n');
            DWGAMCertaintyTreePBTClassByKWriter.write('\n');       

//            DWBasePctCorrectModeClassByKWriter.write('\n');
//            DWBasePctCorrectAverageClassByKWriter.write('\n');
//            DWBasePctCorrectDSClassByKWriter.write('\n');
//            DWBasePctCorrectPBTClassByKWriter.write('\n');
//            DWBasePctCorrectCombAveClassByKWriter.write('\n');
//            DWBasePctCorrectModAveClassByKWriter.write('\n');
//            DWBasePctCorrectTreePBTClassByKWriter.write('\n');
//
//            DWBaseCertaintyModeClassByKWriter.write('\n');
//            DWBaseCertaintyAverageClassByKWriter.write('\n');
//            DWBaseCertaintyDSClassByKWriter.write('\n');
//            DWBaseCertaintyPBTClassByKWriter.write('\n');
//            DWBaseCertaintyCombAveClassByKWriter.write('\n');
//            DWBaseCertaintyModAveClassByKWriter.write('\n');
//            DWBaseCertaintyTreePBTClassByKWriter.write('\n');                
            
            simpleGAMPctCorrectModeClassByKWriter.write('\n');
            simpleGAMPctCorrectAverageClassByKWriter.write('\n');
            simpleGAMPctCorrectDSClassByKWriter.write('\n');
            simpleGAMPctCorrectPBTClassByKWriter.write('\n');
            simpleGAMPctCorrectCombAveClassByKWriter.write('\n');
            simpleGAMPctCorrectModAveClassByKWriter.write('\n');
            simpleGAMPctCorrectTreePBTClassByKWriter.write('\n');

            simpleGAMCertaintyModeClassByKWriter.write('\n');
            simpleGAMCertaintyAverageClassByKWriter.write('\n');
            simpleGAMCertaintyDSClassByKWriter.write('\n');
            simpleGAMCertaintyPBTClassByKWriter.write('\n');
            simpleGAMCertaintyCombAveClassByKWriter.write('\n');
            simpleGAMCertaintyModAveClassByKWriter.write('\n');
            simpleGAMCertaintyTreePBTClassByKWriter.write('\n');       

//            simpleBasePctCorrectModeClassByKWriter.write('\n');
//            simpleBasePctCorrectAverageClassByKWriter.write('\n');
//            simpleBasePctCorrectDSClassByKWriter.write('\n');
//            simpleBasePctCorrectPBTClassByKWriter.write('\n');
//            simpleBasePctCorrectCombAveClassByKWriter.write('\n');
//            simpleBasePctCorrectModAveClassByKWriter.write('\n');
//            simpleBasePctCorrectTreePBTClassByKWriter.write('\n');
//
//            simpleBaseCertaintyModeClassByKWriter.write('\n');
//            simpleBaseCertaintyAverageClassByKWriter.write('\n');
//            simpleBaseCertaintyDSClassByKWriter.write('\n');
//            simpleBaseCertaintyPBTClassByKWriter.write('\n');
//            simpleBaseCertaintyCombAveClassByKWriter.write('\n');
//            simpleBaseCertaintyModAveClassByKWriter.write('\n');
//            simpleBaseCertaintyTreePBTClassByKWriter.write('\n');             
            
            for (int index = 0; index < 6; index++){
                DWGAMPctCorrectModeClassByKWriter.write(classNames[index+1] + ",");  
                DWGAMPctCorrectAverageClassByKWriter.write(classNames[index+1] + ",");
                DWGAMPctCorrectDSClassByKWriter.write(classNames[index+1] + ",");
                DWGAMPctCorrectPBTClassByKWriter.write(classNames[index+1] + ",");
                DWGAMPctCorrectCombAveClassByKWriter.write(classNames[index+1] + ",");
                DWGAMPctCorrectModAveClassByKWriter.write(classNames[index+1] + ",");
                DWGAMPctCorrectTreePBTClassByKWriter.write(classNames[index+1] + ",");
                
                DWGAMCertaintyModeClassByKWriter.write(classNames[index+1] + ",");
                DWGAMCertaintyAverageClassByKWriter.write(classNames[index+1] + ",");
                DWGAMCertaintyDSClassByKWriter.write(classNames[index+1] + ",");
                DWGAMCertaintyPBTClassByKWriter.write(classNames[index+1] + ",");
                DWGAMCertaintyCombAveClassByKWriter.write(classNames[index+1] + ",");
                DWGAMCertaintyModAveClassByKWriter.write(classNames[index+1] + ",");
                DWGAMCertaintyTreePBTClassByKWriter.write(classNames[index+1] + ",");       
                
//                DWBasePctCorrectModeClassByKWriter.write(classNames[index+1] + ",");
//                DWBasePctCorrectAverageClassByKWriter.write(classNames[index+1] + ",");
//                DWBasePctCorrectDSClassByKWriter.write(classNames[index+1] + ",");
//                DWBasePctCorrectPBTClassByKWriter.write(classNames[index+1] + ",");
//                DWBasePctCorrectCombAveClassByKWriter.write(classNames[index+1] + ",");
//                DWBasePctCorrectModAveClassByKWriter.write(classNames[index+1] + ",");
//                DWBasePctCorrectTreePBTClassByKWriter.write(classNames[index+1] + ",");
//                
//                DWBaseCertaintyModeClassByKWriter.write(classNames[index+1] + ",");
//                DWBaseCertaintyAverageClassByKWriter.write(classNames[index+1] + ",");
//                DWBaseCertaintyDSClassByKWriter.write(classNames[index+1] + ",");
//                DWBaseCertaintyPBTClassByKWriter.write(classNames[index+1] + ",");
//                DWBaseCertaintyCombAveClassByKWriter.write(classNames[index+1] + ",");
//                DWBaseCertaintyModAveClassByKWriter.write(classNames[index+1] + ",");
//                DWBaseCertaintyTreePBTClassByKWriter.write(classNames[index+1] + ",");      
                
                simpleGAMPctCorrectModeClassByKWriter.write(classNames[index+1] + ",");  
                simpleGAMPctCorrectAverageClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMPctCorrectDSClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMPctCorrectPBTClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMPctCorrectCombAveClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMPctCorrectModAveClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMPctCorrectTreePBTClassByKWriter.write(classNames[index+1] + ",");
                
                simpleGAMCertaintyModeClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMCertaintyAverageClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMCertaintyDSClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMCertaintyPBTClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMCertaintyCombAveClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMCertaintyModAveClassByKWriter.write(classNames[index+1] + ",");
                simpleGAMCertaintyTreePBTClassByKWriter.write(classNames[index+1] + ",");       
                
//                simpleBasePctCorrectModeClassByKWriter.write(classNames[index+1] + ",");
//                simpleBasePctCorrectAverageClassByKWriter.write(classNames[index+1] + ",");
//                simpleBasePctCorrectDSClassByKWriter.write(classNames[index+1] + ",");
//                simpleBasePctCorrectPBTClassByKWriter.write(classNames[index+1] + ",");
//                simpleBasePctCorrectCombAveClassByKWriter.write(classNames[index+1] + ",");
//                simpleBasePctCorrectModAveClassByKWriter.write(classNames[index+1] + ",");
//                simpleBasePctCorrectTreePBTClassByKWriter.write(classNames[index+1] + ",");
//                
//                simpleBaseCertaintyModeClassByKWriter.write(classNames[index+1] + ",");
//                simpleBaseCertaintyAverageClassByKWriter.write(classNames[index+1] + ",");
//                simpleBaseCertaintyDSClassByKWriter.write(classNames[index+1] + ",");
//                simpleBaseCertaintyPBTClassByKWriter.write(classNames[index+1] + ",");
//                simpleBaseCertaintyCombAveClassByKWriter.write(classNames[index+1] + ",");
//                simpleBaseCertaintyModAveClassByKWriter.write(classNames[index+1] + ",");
//                simpleBaseCertaintyTreePBTClassByKWriter.write(classNames[index+1] + ",");                   
                
                for (int k2 = 0; k2 < 300; k2++){                   
                    DWGAMPctCorrectModeClassByKWriter.write(DWGAMCorrModeClByK[index][k2] + ",");
                    DWGAMPctCorrectAverageClassByKWriter.write(DWGAMCorrAverageClByK[index][k2] + ","); 
                    DWGAMPctCorrectDSClassByKWriter.write(DWGAMCorrDSClByK[index][k2] + ","); 
                    DWGAMPctCorrectPBTClassByKWriter.write(DWGAMCorrPBTClByK[index][k2] + ","); 
                    DWGAMPctCorrectCombAveClassByKWriter.write(DWGAMCorrCombAveClByK[index][k2] + ","); 
                    DWGAMPctCorrectModAveClassByKWriter.write(DWGAMCorrModAveClByK[index][k2] + ","); 
                    DWGAMPctCorrectTreePBTClassByKWriter.write(DWGAMCorrTreePBTClByK[index][k2] + ","); 
                    
                    DWGAMCertaintyModeClassByKWriter.write(DWGAMCertModeClByK[index][k2] + ",");
                    DWGAMCertaintyAverageClassByKWriter.write(DWGAMCertAverageClByK[index][k2] + ","); 
                    DWGAMCertaintyDSClassByKWriter.write(DWGAMCertDSClByK[index][k2] + ","); 
                    DWGAMCertaintyPBTClassByKWriter.write(DWGAMCertPBTClByK[index][k2] + ","); 
                    DWGAMCertaintyCombAveClassByKWriter.write(DWGAMCertCombAveClByK[index][k2] + ","); 
                    DWGAMCertaintyModAveClassByKWriter.write(DWGAMCertModAveClByK[index][k2] + ","); 
                    DWGAMCertaintyTreePBTClassByKWriter.write(DWGAMCertTreePBTClByK[index][k2] + ",");                     
                    
//                    DWBasePctCorrectModeClassByKWriter.write(DWBaseCorrModeClByK[index][k2] + ",");
//                    DWBasePctCorrectAverageClassByKWriter.write(DWBaseCorrAverageClByK[index][k2] + ","); 
//                    DWBasePctCorrectDSClassByKWriter.write(DWBaseCorrDSClByK[index][k2] + ","); 
//                    DWBasePctCorrectPBTClassByKWriter.write(DWBaseCorrPBTClByK[index][k2] + ","); 
//                    DWBasePctCorrectCombAveClassByKWriter.write(DWBaseCorrCombAveClByK[index][k2] + ","); 
//                    DWBasePctCorrectModAveClassByKWriter.write(DWBaseCorrModAveClByK[index][k2] + ","); 
//                    DWBasePctCorrectTreePBTClassByKWriter.write(DWBaseCorrTreePBTClByK[index][k2] + ","); 
//                    
//                    DWBaseCertaintyModeClassByKWriter.write(DWBaseCertModeClByK[index][k2] + ",");
//                    DWBaseCertaintyAverageClassByKWriter.write(DWBaseCertAverageClByK[index][k2] + ","); 
//                    DWBaseCertaintyDSClassByKWriter.write(DWBaseCertDSClByK[index][k2] + ","); 
//                    DWBaseCertaintyPBTClassByKWriter.write(DWBaseCertPBTClByK[index][k2] + ","); 
//                    DWBaseCertaintyCombAveClassByKWriter.write(DWBaseCertCombAveClByK[index][k2] + ","); 
//                    DWBaseCertaintyModAveClassByKWriter.write(DWBaseCertModAveClByK[index][k2] + ","); 
//                    DWBaseCertaintyTreePBTClassByKWriter.write(DWBaseCertTreePBTClByK[index][k2] + ",");      
                    
                    simpleGAMPctCorrectModeClassByKWriter.write(simpleGAMCorrModeClByK[index][k2] + ",");
                    simpleGAMPctCorrectAverageClassByKWriter.write(simpleGAMCorrAverageClByK[index][k2] + ","); 
                    simpleGAMPctCorrectDSClassByKWriter.write(simpleGAMCorrDSClByK[index][k2] + ","); 
                    simpleGAMPctCorrectPBTClassByKWriter.write(simpleGAMCorrPBTClByK[index][k2] + ","); 
                    simpleGAMPctCorrectCombAveClassByKWriter.write(simpleGAMCorrCombAveClByK[index][k2] + ","); 
                    simpleGAMPctCorrectModAveClassByKWriter.write(simpleGAMCorrModAveClByK[index][k2] + ","); 
                    simpleGAMPctCorrectTreePBTClassByKWriter.write(simpleGAMCorrTreePBTClByK[index][k2] + ","); 
                    
                    simpleGAMCertaintyModeClassByKWriter.write(simpleGAMCertModeClByK[index][k2] + ",");
                    simpleGAMCertaintyAverageClassByKWriter.write(simpleGAMCertAverageClByK[index][k2] + ","); 
                    simpleGAMCertaintyDSClassByKWriter.write(simpleGAMCertDSClByK[index][k2] + ","); 
                    simpleGAMCertaintyPBTClassByKWriter.write(simpleGAMCertPBTClByK[index][k2] + ","); 
                    simpleGAMCertaintyCombAveClassByKWriter.write(simpleGAMCertCombAveClByK[index][k2] + ","); 
                    simpleGAMCertaintyModAveClassByKWriter.write(simpleGAMCertModAveClByK[index][k2] + ","); 
                    simpleGAMCertaintyTreePBTClassByKWriter.write(simpleGAMCertTreePBTClByK[index][k2] + ",");                     
                    
//                    simpleBasePctCorrectModeClassByKWriter.write(simpleBaseCorrModeClByK[index][k2] + ",");
//                    simpleBasePctCorrectAverageClassByKWriter.write(simpleBaseCorrAverageClByK[index][k2] + ","); 
//                    simpleBasePctCorrectDSClassByKWriter.write(simpleBaseCorrDSClByK[index][k2] + ","); 
//                    simpleBasePctCorrectPBTClassByKWriter.write(simpleBaseCorrPBTClByK[index][k2] + ","); 
//                    simpleBasePctCorrectCombAveClassByKWriter.write(simpleBaseCorrCombAveClByK[index][k2] + ","); 
//                    simpleBasePctCorrectModAveClassByKWriter.write(simpleBaseCorrModAveClByK[index][k2] + ","); 
//                    simpleBasePctCorrectTreePBTClassByKWriter.write(simpleBaseCorrTreePBTClByK[index][k2] + ","); 
//                    
//                    simpleBaseCertaintyModeClassByKWriter.write(simpleBaseCertModeClByK[index][k2] + ",");
//                    simpleBaseCertaintyAverageClassByKWriter.write(simpleBaseCertAverageClByK[index][k2] + ","); 
//                    simpleBaseCertaintyDSClassByKWriter.write(simpleBaseCertDSClByK[index][k2] + ","); 
//                    simpleBaseCertaintyPBTClassByKWriter.write(simpleBaseCertPBTClByK[index][k2] + ","); 
//                    simpleBaseCertaintyCombAveClassByKWriter.write(simpleBaseCertCombAveClByK[index][k2] + ","); 
//                    simpleBaseCertaintyModAveClassByKWriter.write(simpleBaseCertModAveClByK[index][k2] + ","); 
//                    simpleBaseCertaintyTreePBTClassByKWriter.write(simpleBaseCertTreePBTClByK[index][k2] + ",");                       
                }
                
                DWGAMPctCorrectModeClassByKWriter.write('\n');
                DWGAMPctCorrectAverageClassByKWriter.write('\n');
                DWGAMPctCorrectDSClassByKWriter.write('\n');
                DWGAMPctCorrectPBTClassByKWriter.write('\n');
                DWGAMPctCorrectCombAveClassByKWriter.write('\n');
                DWGAMPctCorrectModAveClassByKWriter.write('\n');
                DWGAMPctCorrectTreePBTClassByKWriter.write('\n');
                
                DWGAMCertaintyModeClassByKWriter.write('\n');
                DWGAMCertaintyAverageClassByKWriter.write('\n');
                DWGAMCertaintyDSClassByKWriter.write('\n');
                DWGAMCertaintyPBTClassByKWriter.write('\n');
                DWGAMCertaintyCombAveClassByKWriter.write('\n');
                DWGAMCertaintyModAveClassByKWriter.write('\n');
                DWGAMCertaintyTreePBTClassByKWriter.write('\n');       
                
//                DWBasePctCorrectModeClassByKWriter.write('\n');
//                DWBasePctCorrectAverageClassByKWriter.write('\n');
//                DWBasePctCorrectDSClassByKWriter.write('\n');
//                DWBasePctCorrectPBTClassByKWriter.write('\n');
//                DWBasePctCorrectCombAveClassByKWriter.write('\n');
//                DWBasePctCorrectModAveClassByKWriter.write('\n');
//                DWBasePctCorrectTreePBTClassByKWriter.write('\n');
//                
//                DWBaseCertaintyModeClassByKWriter.write('\n');
//                DWBaseCertaintyAverageClassByKWriter.write('\n');
//                DWBaseCertaintyDSClassByKWriter.write('\n');
//                DWBaseCertaintyPBTClassByKWriter.write('\n');
//                DWBaseCertaintyCombAveClassByKWriter.write('\n');
//                DWBaseCertaintyModAveClassByKWriter.write('\n');
//                DWBaseCertaintyTreePBTClassByKWriter.write('\n');      
                
                simpleGAMPctCorrectModeClassByKWriter.write('\n');
                simpleGAMPctCorrectAverageClassByKWriter.write('\n');
                simpleGAMPctCorrectDSClassByKWriter.write('\n');
                simpleGAMPctCorrectPBTClassByKWriter.write('\n');
                simpleGAMPctCorrectCombAveClassByKWriter.write('\n');
                simpleGAMPctCorrectModAveClassByKWriter.write('\n');
                simpleGAMPctCorrectTreePBTClassByKWriter.write('\n');
                
                simpleGAMCertaintyModeClassByKWriter.write('\n');
                simpleGAMCertaintyAverageClassByKWriter.write('\n');
                simpleGAMCertaintyDSClassByKWriter.write('\n');
                simpleGAMCertaintyPBTClassByKWriter.write('\n');
                simpleGAMCertaintyCombAveClassByKWriter.write('\n');
                simpleGAMCertaintyModAveClassByKWriter.write('\n');
                simpleGAMCertaintyTreePBTClassByKWriter.write('\n');       
                
//                simpleBasePctCorrectModeClassByKWriter.write('\n');
//                simpleBasePctCorrectAverageClassByKWriter.write('\n');
//                simpleBasePctCorrectDSClassByKWriter.write('\n');
//                simpleBasePctCorrectPBTClassByKWriter.write('\n');
//                simpleBasePctCorrectCombAveClassByKWriter.write('\n');
//                simpleBasePctCorrectModAveClassByKWriter.write('\n');
//                simpleBasePctCorrectTreePBTClassByKWriter.write('\n');
//                
//                simpleBaseCertaintyModeClassByKWriter.write('\n');
//                simpleBaseCertaintyAverageClassByKWriter.write('\n');
//                simpleBaseCertaintyDSClassByKWriter.write('\n');
//                simpleBaseCertaintyPBTClassByKWriter.write('\n');
//                simpleBaseCertaintyCombAveClassByKWriter.write('\n');
//                simpleBaseCertaintyModAveClassByKWriter.write('\n');
//                simpleBaseCertaintyTreePBTClassByKWriter.write('\n');                  
                
                System.out.println("finished writing row " + index);
            }                 
            
            DWGAMPctCorrectModeClassByKWriter.close();
            DWGAMPctCorrectAverageClassByKWriter.close();
            DWGAMPctCorrectDSClassByKWriter.close();
            DWGAMPctCorrectPBTClassByKWriter.close();
            DWGAMPctCorrectCombAveClassByKWriter.close();
            DWGAMPctCorrectModAveClassByKWriter.close();
            DWGAMPctCorrectTreePBTClassByKWriter.close();

            DWGAMCertaintyModeClassByKWriter.close();
            DWGAMCertaintyAverageClassByKWriter.close();
            DWGAMCertaintyDSClassByKWriter.close();
            DWGAMCertaintyPBTClassByKWriter.close();
            DWGAMCertaintyCombAveClassByKWriter.close();
            DWGAMCertaintyModAveClassByKWriter.close();
            DWGAMCertaintyTreePBTClassByKWriter.close();     

//            DWBasePctCorrectModeClassByKWriter.close();
//            DWBasePctCorrectAverageClassByKWriter.close();
//            DWBasePctCorrectDSClassByKWriter.close();
//            DWBasePctCorrectPBTClassByKWriter.close();
//            DWBasePctCorrectCombAveClassByKWriter.close();
//            DWBasePctCorrectModAveClassByKWriter.close();
//            DWBasePctCorrectTreePBTClassByKWriter.close();
//
//            DWBaseCertaintyModeClassByKWriter.close();
//            DWBaseCertaintyAverageClassByKWriter.close();
//            DWBaseCertaintyDSClassByKWriter.close();
//            DWBaseCertaintyPBTClassByKWriter.close();
//            DWBaseCertaintyCombAveClassByKWriter.close();
//            DWBaseCertaintyModAveClassByKWriter.close();
//            DWBaseCertaintyTreePBTClassByKWriter.close();                                  
            
            simpleGAMPctCorrectModeClassByKWriter.close();
            simpleGAMPctCorrectAverageClassByKWriter.close();
            simpleGAMPctCorrectDSClassByKWriter.close();
            simpleGAMPctCorrectPBTClassByKWriter.close();
            simpleGAMPctCorrectCombAveClassByKWriter.close();
            simpleGAMPctCorrectModAveClassByKWriter.close();
            simpleGAMPctCorrectTreePBTClassByKWriter.close();

            simpleGAMCertaintyModeClassByKWriter.close();
            simpleGAMCertaintyAverageClassByKWriter.close();
            simpleGAMCertaintyDSClassByKWriter.close();
            simpleGAMCertaintyPBTClassByKWriter.close();
            simpleGAMCertaintyCombAveClassByKWriter.close();
            simpleGAMCertaintyModAveClassByKWriter.close();
            simpleGAMCertaintyTreePBTClassByKWriter.close();     

//            simpleBasePctCorrectModeClassByKWriter.close();
//            simpleBasePctCorrectAverageClassByKWriter.close();
//            simpleBasePctCorrectDSClassByKWriter.close();
//            simpleBasePctCorrectPBTClassByKWriter.close();
//            simpleBasePctCorrectCombAveClassByKWriter.close();
//            simpleBasePctCorrectModAveClassByKWriter.close();
//            simpleBasePctCorrectTreePBTClassByKWriter.close();
//
//            simpleBaseCertaintyModeClassByKWriter.close();
//            simpleBaseCertaintyAverageClassByKWriter.close();
//            simpleBaseCertaintyDSClassByKWriter.close();
//            simpleBaseCertaintyPBTClassByKWriter.close();
//            simpleBaseCertaintyCombAveClassByKWriter.close();
//            simpleBaseCertaintyModAveClassByKWriter.close();
//            simpleBaseCertaintyTreePBTClassByKWriter.close();              
            
        } catch (IOException ioe){
            System.out.println("ioexception: " + ioe.getMessage());
        }        
            
    }
    
    private static void writeConfusionMatrices(){
        int[] kValues = {5, 25, 50, 100, 150, 200, 250, 300};
        
//        File DWBaseCertMatrices = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/confMatrices/DWBaseCertainty.csv");
//        File DWBaseConfMatrices = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/confMatrices/DWBaseConfusion.csv");
        File DWGAMCertMatrices = new File("D:/synthLandscan/randomStarts/momentData/summaries/confMatrices/DWGAMCertainty.csv");
        File DWGAMConfMatrices = new File("D:/synthLandscan/randomStarts/momentData/summaries/confMatrices/DWGAMConfusion.csv");
//        File DWFZCertMatrices = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/confMatrices/DWFZCertainty.csv");
//        File DWFZConfMatrices = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/confMatrices/DWVZConfusion.csv");
        
//        File simpleBaseCertMatrices = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/confMatrices/simpleBaseCertainty.csv");
//        File simpleBaseConfMatrices = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/confMatrices/simpleBaseConfusion.csv");
        File simpleGAMCertMatrices = new File("D:/synthLandscan/randomStarts/momentData/summaries/confMatrices/simpleGAMCertainty.csv");
        File simpleGAMConfMatrices = new File("D:/synthLandscan/randomStarts/momentData/summaries/confMatrices/simpleGAMConfusion.csv");
//        File simpleFZCertMatrices = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/confMatrices/simpleFZCertainty.csv");
//        File simpleFZConfMatrices = new File("D:/synthLandscan/momentData/fiveClassPACorrected/summaries/confMatrices/simpleVZConfusion.csv");
        
        
        try{
//            FileWriter DWBaseCertMatricesWriter = new FileWriter(DWBaseCertMatrices);
//            FileWriter DWBaseConfMatricesWriter = new FileWriter(DWBaseConfMatrices);
            FileWriter DWGAMCertMatricesWriter = new FileWriter(DWGAMCertMatrices);
            FileWriter DWGAMConfMatricesWriter = new FileWriter(DWGAMConfMatrices);
//            FileWriter DWFZCertMatricesWriter = new FileWriter(DWFZCertMatrices);
//            FileWriter DWFZConfMatricesWriter = new FileWriter(DWFZConfMatrices);
            
//            FileWriter simpleBaseCertMatricesWriter = new FileWriter(simpleBaseCertMatrices);
//            FileWriter simpleBaseConfMatricesWriter = new FileWriter(simpleBaseConfMatrices);
            FileWriter simpleGAMCertMatricesWriter = new FileWriter(simpleGAMCertMatrices);
            FileWriter simpleGAMConfMatricesWriter = new FileWriter(simpleGAMConfMatrices);
//            FileWriter simpleFZCertMatricesWriter = new FileWriter(simpleFZCertMatrices);
//            FileWriter simpleFZConfMatricesWriter = new FileWriter(simpleFZConfMatrices);            
            
            for (int op = 0; op < operators.length; op++){
                for (int kValue = 0; kValue < kValues.length; kValue++){
//                    DWBaseCertMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
//                    DWBaseConfMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
                    DWGAMCertMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
                    DWGAMConfMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
//                    DWFZCertMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
//                    DWFZConfMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
                    
//                    simpleBaseCertMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
//                    simpleBaseConfMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
                    simpleGAMCertMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
                    simpleGAMConfMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
//                    simpleFZCertMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
//                    simpleFZConfMatricesWriter.write(kValues[kValue] + "_" + operators[op]);                    
                    
                    for (int klass = 0; klass < 6; klass++){
//                        DWBaseCertMatricesWriter.write("," + classNames[klass+1]);
//                        DWBaseConfMatricesWriter.write("," + classNames[klass+1]);
                        DWGAMCertMatricesWriter.write("," + classNames[klass+1]);
                        DWGAMConfMatricesWriter.write("," + classNames[klass+1]);
//                        DWFZCertMatricesWriter.write("," + classNames[klass+1]);
//                        DWFZConfMatricesWriter.write("," + classNames[klass+1]);
                        
//                        simpleBaseCertMatricesWriter.write("," + classNames[klass+1]);
//                        simpleBaseConfMatricesWriter.write("," + classNames[klass+1]);
                        simpleGAMCertMatricesWriter.write("," + classNames[klass+1]);
                        simpleGAMConfMatricesWriter.write("," + classNames[klass+1]);
//                        simpleFZCertMatricesWriter.write("," + classNames[klass+1]);
//                        simpleFZConfMatricesWriter.write("," + classNames[klass+1]);                        
                    }
                    
//                    DWBaseCertMatricesWriter.write('\n');
//                    DWBaseConfMatricesWriter.write('\n');
                    DWGAMCertMatricesWriter.write('\n');
                    DWGAMConfMatricesWriter.write('\n');
//                    DWFZCertMatricesWriter.write('\n');
//                    DWFZConfMatricesWriter.write('\n');
                    
//                    simpleBaseCertMatricesWriter.write('\n');
//                    simpleBaseConfMatricesWriter.write('\n');
                    simpleGAMCertMatricesWriter.write('\n');
                    simpleGAMConfMatricesWriter.write('\n');
//                    simpleFZCertMatricesWriter.write('\n');
//                    simpleFZConfMatricesWriter.write('\n');                    
                    
                    for (int actual = 0; actual < 6; actual++){
//                        DWBaseCertMatricesWriter.write(classNames[actual+1]);
//                        DWBaseConfMatricesWriter.write(classNames[actual+1]);
                        DWGAMCertMatricesWriter.write(classNames[actual+1]);
                        DWGAMConfMatricesWriter.write(classNames[actual+1]);
//                        DWFZCertMatricesWriter.write(classNames[actual+1]);
//                        DWFZConfMatricesWriter.write(classNames[actual+1]);
                        
//                        simpleBaseCertMatricesWriter.write(classNames[actual+1]);
//                        simpleBaseConfMatricesWriter.write(classNames[actual+1]);
                        simpleGAMCertMatricesWriter.write(classNames[actual+1]);
                        simpleGAMConfMatricesWriter.write(classNames[actual+1]);
//                        simpleFZCertMatricesWriter.write(classNames[actual+1]);
//                        simpleFZConfMatricesWriter.write(classNames[actual+1]);                        
                        
                        for (int hypo = 0; hypo < 6; hypo++){
//                            DWBaseCertMatricesWriter.write("," + DWBaseMasses[kValues[kValue]-2][op][actual][hypo]);
//                            DWBaseConfMatricesWriter.write("," + DWBaseClasses[kValues[kValue]-2][op][actual][hypo]);
                            DWGAMCertMatricesWriter.write("," + DWGAMMasses[kValues[kValue]-2][op][actual][hypo]);
                            DWGAMConfMatricesWriter.write("," + DWGAMClasses[kValues[kValue]-2][op][actual][hypo]);   
//                            DWFZCertMatricesWriter.write("," + DWFZMasses[kValues[kValue]-2][op][actual][hypo]);
//                            DWFZConfMatricesWriter.write("," + DWFZClasses[kValues[kValue]-2][op][actual][hypo]);    
                            
//                            simpleBaseCertMatricesWriter.write("," + simpleBaseMasses[kValues[kValue]-2][op][actual][hypo]);
//                            simpleBaseConfMatricesWriter.write("," + simpleBaseClasses[kValues[kValue]-2][op][actual][hypo]);
                            simpleGAMCertMatricesWriter.write("," + simpleGAMMasses[kValues[kValue]-2][op][actual][hypo]);
                            simpleGAMConfMatricesWriter.write("," + simpleGAMClasses[kValues[kValue]-2][op][actual][hypo]);   
//                            simpleFZCertMatricesWriter.write("," + simpleFZMasses[kValues[kValue]-2][op][actual][hypo]);
//                            simpleFZConfMatricesWriter.write("," + simpleFZClasses[kValues[kValue]-2][op][actual][hypo]);                              
                        }
                        
//                        DWBaseCertMatricesWriter.write('\n');
//                        DWBaseConfMatricesWriter.write('\n');
                        DWGAMCertMatricesWriter.write('\n');
                        DWGAMConfMatricesWriter.write('\n');                                
//                        DWFZCertMatricesWriter.write('\n');
//                        DWFZConfMatricesWriter.write('\n'); 
                        
//                        simpleBaseCertMatricesWriter.write('\n');
//                        simpleBaseConfMatricesWriter.write('\n');
                        simpleGAMCertMatricesWriter.write('\n');
                        simpleGAMConfMatricesWriter.write('\n');                                
//                        simpleFZCertMatricesWriter.write('\n');
//                        simpleFZConfMatricesWriter.write('\n'); 
                    }
                    
//                    DWBaseCertMatricesWriter.write('\n');
//                    DWBaseConfMatricesWriter.write('\n');
                    DWGAMCertMatricesWriter.write('\n');
                    DWGAMConfMatricesWriter.write('\n');  
//                    DWFZCertMatricesWriter.write('\n');
//                    DWFZConfMatricesWriter.write('\n');                    
                    
//                    simpleBaseCertMatricesWriter.write('\n');
//                    simpleBaseConfMatricesWriter.write('\n');
                    simpleGAMCertMatricesWriter.write('\n');
                    simpleGAMConfMatricesWriter.write('\n');  
//                    simpleFZCertMatricesWriter.write('\n');
//                    simpleFZConfMatricesWriter.write('\n');                        
                }  //end for kValue
            }  //end for op
        
//            DWBaseCertMatricesWriter.close();
//            DWBaseConfMatricesWriter.close();
            DWGAMCertMatricesWriter.close();
            DWGAMConfMatricesWriter.close();   
//            DWFZCertMatricesWriter.close();
//            DWFZConfMatricesWriter.close();       
            
//            simpleBaseCertMatricesWriter.close();
//            simpleBaseConfMatricesWriter.close();
            simpleGAMCertMatricesWriter.close();
            simpleGAMConfMatricesWriter.close();   
//            simpleFZCertMatricesWriter.close();
//            simpleFZConfMatricesWriter.close();              
            
        } catch (IOException ioe){
            System.out.println("ioexception: " + ioe.getMessage());
        }
        
    }
    
}

