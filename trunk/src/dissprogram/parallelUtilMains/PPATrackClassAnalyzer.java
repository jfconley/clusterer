/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package parallelUtilMains;

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
public class PPATrackClassAnalyzer {

    public static void main(String[] args) {
        try{
            AnalyzeSome DWHu = new AnalyzeSome("DW", "GAM");   
            AnalyzeSome simpleHu = new AnalyzeSome("simple", "GAM");   
            AnalyzeSome DWLi = new AnalyzeSome("DW", "GAMLi");   
            AnalyzeSome simpleLi = new AnalyzeSome("simple", "GAMLi"); 
            AnalyzeSome DWAffine = new AnalyzeSome("DW", "GAMAffine");   
            AnalyzeSome simpleAffine = new AnalyzeSome("simple", "GAMAffine"); 
            AnalyzeSome DWSluzek = new AnalyzeSome("DW", "GAMFullSluzek");   
            AnalyzeSome simpleSluzek = new AnalyzeSome("simple", "GAMFullSluzek"); 
            
            Thread t0 = new Thread(DWHu);
            Thread t1 = new Thread(simpleHu);
            Thread t2 = new Thread(DWLi);
            Thread t3 = new Thread(simpleLi);
            Thread t4 = new Thread(DWAffine);
            Thread t5 = new Thread(simpleAffine);
            Thread t6 = new Thread(DWSluzek);
            Thread t7 = new Thread(simpleSluzek);
            
            t0.start();
            t1.start();
            t2.start();
            t3.start();
            t4.start();
            t5.start();
            t6.start();
            t7.start();
            
            t0.join();
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            t5.join();
            t6.join();
            t7.join();
            
        } catch (InterruptedException ie){
            System.out.println(ie.getMessage());
            threadMessage("This thread was interrupted");
        }
    }
    
    private static void threadMessage(String message){
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    } 
    
    private static class AnalyzeSome implements Runnable{
        
        String knn, moment;
        int[][][][] myClasses = new int[300][7][6][6];
        double[][][][] myMasses = new double[300][7][6][6];
        String[] operators = {"mode", "average", "simple DS", "prop bel trans", "comb. ave.", "mod. ave.", "tree prop bel trans"};
        int[] counts = new int[5];
        String[] classNames;
        
        public AnalyzeSome(String sKnn, String sMoment){
            knn = sKnn;
            moment = sMoment;
        }
        
        public void run(){
            for (int k = 2; k < 301; k++) {
                counts = new int[5];                
                try{
                    File classificationFile = new File("D:/synthLandscan/randomStarts/momentData/" + moment + "/" + knn + moment + "Classifications" + k + ".csv");
                    FileReader classificationReader = new FileReader(classificationFile);
                    CSVParser classificationParser = new CSVParser(classificationReader);

                    String[][] classificationValues = classificationParser.getAllValues();

                    //each of the values arrays should be the same size.
                    //row 0 is column headers
                    classNames = classificationValues[0];

    //                for (int debug = 0; debug < classNames.length; debug++){
    //                    System.out.println(debug + ": " + classNames[debug]);
    //                }

                    for (int j = 1; j < classificationValues.length; j++){
                        String[] thisRowStrings = classificationValues[j];
                        double[] thisRowDoubles = new double[thisRowStrings.length - 2];  //-1 because first column is the operator and the last is the real class
                        for (int i = 0; i < thisRowDoubles.length; i++){
                            thisRowDoubles[i] = Double.parseDouble(thisRowStrings[i+1]);
                        }

                        //figure out the winner for each row of doubles.
                        String[] winners = new String[classificationValues.length];

                        int[] topIndexes = new int[6];
                        double[] topMasses = new double[6];
                        Arrays.fill(topIndexes, -1);
                        Arrays.fill(topMasses, -1);

                        for (int i = 0; i < thisRowDoubles.length-1; i++){  //stop early because I don't want THETA to win
                            if (thisRowDoubles[i] > topMasses[0]){
                                topIndexes[0] = i;
                                topMasses[0] = thisRowDoubles[i];
                            }
                        }
                        for (int p = 0; p < topIndexes.length; p++){
                            if (topIndexes[p] == -1){
                                topIndexes[p] = 5;  //this means unclassified, probably NaN's across the board from a simple DS conflict.
                            }
                        }                                        

                        int actual = -1;
                        String corr = thisRowStrings[thisRowStrings.length-1];                        
                        if ((corr.equalsIgnoreCase(classNames[1]) || (corr.equalsIgnoreCase("inf")))){
                            actual = 0;
                            counts[0]++;
                        } else if (corr.equalsIgnoreCase(classNames[2])){
                            actual = 1;
                            counts[1]++;
                        } else if (corr.equalsIgnoreCase(classNames[3])){
                            actual = 2;
                            counts[2]++;
                        } else if (corr.equalsIgnoreCase(classNames[4])){
                            actual = 3;
                            counts[3]++;
                        } else if (corr.equalsIgnoreCase(classNames[5])){
                            actual = 4;
                            counts[4]++;
                        }

                        //Add it to the confusion matrix quadarrays                    
                        myClasses[k-2][(j-1)%7][actual][topIndexes[0]]++;

                        //Add the probabilities to the prob quadarrays                   
                        for (int d = 0; d < thisRowDoubles.length; d++){
                            if (Double.isNaN(thisRowDoubles[d])){
                                //???
                            } else {
                                myMasses[k-2][(j-1)%7][actual][d] += thisRowDoubles[d];
                            }
                        }  //end for d                                           
                    }  //end for j
                } catch (FileNotFoundException fnfe){
                    throw new RuntimeException("No file!  " + fnfe.getMessage());
                } catch (IOException ioe){
                    throw new RuntimeException("IO Exception!  " + ioe.getMessage());
                }
                //divide the prob quadarrays by the right count so they're actually probs again.             
                for (int v = 0; v < 7; v++){
                    for (int v1 = 0; v1 < 6; v1++){
                        for (int v2 = 0; v2 < 6; v2++){
                            myMasses[k-2][v][v1][v2] = myMasses[k-2][v][v1][v2]/(counts[0]/7);
                        }
                    }
                }
                System.out.println("finished collating k = " + k);
            }  //end for k      

            writeGeneralSummaries();
            writeClassSummaries();
            writeConfusionMatrices();

        }    
        
        private void writeGeneralSummaries(){
            //write out summaries for %correct by k
            //write out summaries for certainty by k                               

            double[][] corrOpByK = new double[7][300];
            double[][] corrClByK = new double[5][300];
            double[][] certOpByK = new double[7][300];
            double[][] certClByK = new double[5][300];         

            File pctCorrectOperatorByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/" + knn + moment + "PctCorrectOperatorByK.csv");
            File pctCorrectClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/" + knn + moment + "PctCorrectClassByK.csv");
            File certaintyOperatorByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/" + knn + moment + "CertaintyOperatorByK.csv");
            File certaintyClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/" + knn + moment + "CertaintyClassByK.csv");

            try{            
                FileWriter pctCorrectOperatorByKWriter = new FileWriter(pctCorrectOperatorByK);
                FileWriter pctCorrectClassByKWriter = new FileWriter(pctCorrectClassByK);
                FileWriter certaintyOperatorByKWriter = new FileWriter(certaintyOperatorByK);
                FileWriter certaintyClassByKWriter = new FileWriter(certaintyClassByK);

                for (int op = 0; op < 7; op++){
                    for (int k2 = 0; k2 < 300; k2++){
                        for (int kl = 0; kl < 5; kl++){                    
                            corrOpByK[op][k2] += myClasses[k2][op][kl][kl]/(counts[0]*5d);  //div by 200 instead of div by 40 because we want the percent across all classes, and this is equivalent to adding up all class-specific percent corrects and dividing by 5.
                            certOpByK[op][k2] += myMasses[k2][op][kl][kl]/5;     //div by 5 again to average over all classes
                            corrClByK[kl][k2] += myClasses[k2][op][kl][kl]/(counts[0]*7d);  //div by 280 instead of by 40 to average over all operators
                            certClByK[kl][k2] += myMasses[k2][op][kl][kl]/7;     //div by 7 to average over all operators.                                                          
                        }
                    }
                }               

                for (int k2 = 2; k2 < 302; k2++){                   
                    pctCorrectOperatorByKWriter.write("," + k2);
                    pctCorrectClassByKWriter.write("," + k2);                    
                    certaintyOperatorByKWriter.write("," + k2);
                    certaintyClassByKWriter.write("," + k2);      
                }            

                pctCorrectOperatorByKWriter.write('\n');
                pctCorrectClassByKWriter.write('\n');                    
                certaintyOperatorByKWriter.write('\n');
                certaintyClassByKWriter.write('\n');      

                for (int index = 0; index < 5; index++){
                    pctCorrectOperatorByKWriter.write(operators[index] + ",");
                    pctCorrectClassByKWriter.write(classNames[index+1] + ",");                    
                    certaintyOperatorByKWriter.write(operators[index] + ",");
                    certaintyClassByKWriter.write(classNames[index+1] + ",");    

                    for (int k2 = 0; k2 < 300; k2++){                   
                        pctCorrectOperatorByKWriter.write(corrOpByK[index][k2] + ",");
                        pctCorrectClassByKWriter.write(corrClByK[index][k2] + ",");                    
                        certaintyOperatorByKWriter.write(certOpByK[index][k2] + ",");
                        certaintyClassByKWriter.write(certClByK[index][k2] + ",");    
                    }

                    pctCorrectOperatorByKWriter.write('\n');
                    pctCorrectClassByKWriter.write('\n');                    
                    certaintyOperatorByKWriter.write('\n');
                    certaintyClassByKWriter.write('\n');    

                    System.out.println("finished writing row " + index);
                }

                for (int blob = 5; blob < 7; blob++){
                    pctCorrectOperatorByKWriter.write(operators[blob] + ",");                   
                    certaintyOperatorByKWriter.write(operators[blob] + ",");       

                    for (int k2 = 0; k2 < 300; k2++){                   
                        pctCorrectOperatorByKWriter.write(corrOpByK[blob][k2] + ",");                
                        certaintyOperatorByKWriter.write(certOpByK[blob][k2] + ",");           
                    }      

                    pctCorrectOperatorByKWriter.write('\n');             
                    certaintyOperatorByKWriter.write('\n');

                    System.out.println("finished writing row " + blob);                
                }

                pctCorrectOperatorByKWriter.close();
                pctCorrectClassByKWriter.close();                    
                certaintyOperatorByKWriter.close();
                certaintyClassByKWriter.close();      

            } catch (IOException ioe){
                System.out.println("ioexception: " + ioe.getMessage());
            }        
        }

        private void writeClassSummaries(){
            double[][] corrModeClByK = new double[6][300];
            double[][] corrAverageClByK = new double[6][300];
            double[][] corrDSClByK = new double[6][300];
            double[][] corrPBTClByK = new double[6][300];
            double[][] corrCombAveClByK = new double[6][300];
            double[][] corrModAveClByK = new double[6][300];
            double[][] corrTreePBTClByK = new double[6][300];

            double[][] certModeClByK = new double[6][300];
            double[][] certAverageClByK = new double[6][300];
            double[][] certDSClByK = new double[6][300];
            double[][] certPBTClByK = new double[6][300];
            double[][] certCombAveClByK = new double[6][300];
            double[][] certModAveClByK = new double[6][300];
            double[][] certTreePBTClByK = new double[6][300];

            File pctCorrectModeClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "PctCorrectModeClassByK.csv");
            File pctCorrectAverageClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "PctCorrectAverageClassByK.csv");
            File pctCorrectDSClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "PctCorrectDSClassByK.csv");
            File pctCorrectPBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "PctCorrectPBTClassByK.csv");
            File pctCorrectCombAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "PctCorrectCombAveClassByK.csv");
            File pctCorrectModAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "PctCorrectModAveClassByK.csv");
            File pctCorrectTreePBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "PctCorrectTreePBTClassByK.csv");

            File certaintyModeClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "CertaintyModeClassByK.csv");
            File certaintyAverageClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "CertaintyAverageClassByK.csv");
            File certaintyDSClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "CertaintyDSClassByK.csv");
            File certaintyPBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "CertaintyPBTClassByK.csv");
            File certaintyCombAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "CertaintyCombAveClassByK.csv");
            File certaintyModAveClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "CertaintyModAveClassByK.csv");
            File certaintyTreePBTClassByK = new File("D:/synthLandscan/randomStarts/momentData/summaries/byClass/" + knn + moment + "CertaintyTreePBTClassByK.csv");        

            try{
                FileWriter pctCorrectModeClassByKWriter = new FileWriter(pctCorrectModeClassByK);
                FileWriter pctCorrectAverageClassByKWriter = new FileWriter(pctCorrectAverageClassByK);
                FileWriter pctCorrectDSClassByKWriter = new FileWriter(pctCorrectDSClassByK);
                FileWriter pctCorrectPBTClassByKWriter = new FileWriter(pctCorrectPBTClassByK);
                FileWriter pctCorrectCombAveClassByKWriter = new FileWriter(pctCorrectCombAveClassByK);
                FileWriter pctCorrectModAveClassByKWriter = new FileWriter(pctCorrectModAveClassByK);
                FileWriter pctCorrectTreePBTClassByKWriter = new FileWriter(pctCorrectTreePBTClassByK);

                FileWriter certaintyModeClassByKWriter = new FileWriter(certaintyModeClassByK);
                FileWriter certaintyAverageClassByKWriter = new FileWriter(certaintyAverageClassByK);
                FileWriter certaintyDSClassByKWriter = new FileWriter(certaintyDSClassByK);
                FileWriter certaintyPBTClassByKWriter = new FileWriter(certaintyPBTClassByK);
                FileWriter certaintyCombAveClassByKWriter = new FileWriter(certaintyCombAveClassByK);
                FileWriter certaintyModAveClassByKWriter = new FileWriter(certaintyModAveClassByK);
                FileWriter certaintyTreePBTClassByKWriter = new FileWriter(certaintyTreePBTClassByK);            

    //            for (int op = 0; op < 7; op++){
                    for (int k2 = 0; k2 < 300; k2++){
                        for (int kl = 0; kl < 6; kl++){                    
                            corrModeClByK[kl][k2] += myClasses[k2][0][kl][kl]/(double) (counts[0]);     
                            corrAverageClByK[kl][k2] += myClasses[k2][1][kl][kl]/(double) (counts[0]);
                            corrDSClByK[kl][k2] += myClasses[k2][2][kl][kl]/(double) (counts[0]);
                            corrPBTClByK[kl][k2] += myClasses[k2][3][kl][kl]/(double) (counts[0]);
                            corrCombAveClByK[kl][k2] += myClasses[k2][4][kl][kl]/(double) (counts[0]);
                            corrModAveClByK[kl][k2] += myClasses[k2][5][kl][kl]/(double) (counts[0]);
                            corrTreePBTClByK[kl][k2] += myClasses[k2][6][kl][kl]/(double) (counts[0]);

                            certModeClByK[kl][k2] += myMasses[k2][0][kl][kl];     
                            certAverageClByK[kl][k2] += myMasses[k2][1][kl][kl];
                            certDSClByK[kl][k2] += myMasses[k2][2][kl][kl];
                            certPBTClByK[kl][k2] += myMasses[k2][3][kl][kl];
                            certCombAveClByK[kl][k2] += myMasses[k2][4][kl][kl];
                            certModAveClByK[kl][k2] += myMasses[k2][5][kl][kl];
                            certTreePBTClByK[kl][k2] += myMasses[k2][6][kl][kl];              

                        }
                    }
    //            }               

                for (int k2 = 2; k2 < 302; k2++){                   
                    pctCorrectModeClassByKWriter.write("," + k2);
                    pctCorrectAverageClassByKWriter.write("," + k2);
                    pctCorrectDSClassByKWriter.write("," + k2);
                    pctCorrectPBTClassByKWriter.write("," + k2);
                    pctCorrectCombAveClassByKWriter.write("," + k2);
                    pctCorrectModAveClassByKWriter.write("," + k2);
                    pctCorrectTreePBTClassByKWriter.write("," + k2);

                    certaintyModeClassByKWriter.write("," + k2);
                    certaintyAverageClassByKWriter.write("," + k2);
                    certaintyDSClassByKWriter.write("," + k2);
                    certaintyPBTClassByKWriter.write("," + k2);
                    certaintyCombAveClassByKWriter.write("," + k2);
                    certaintyModAveClassByKWriter.write("," + k2);
                    certaintyTreePBTClassByKWriter.write("," + k2);                                          
                }            

                pctCorrectModeClassByKWriter.write('\n');
                pctCorrectAverageClassByKWriter.write('\n');
                pctCorrectDSClassByKWriter.write('\n');
                pctCorrectPBTClassByKWriter.write('\n');
                pctCorrectCombAveClassByKWriter.write('\n');
                pctCorrectModAveClassByKWriter.write('\n');
                pctCorrectTreePBTClassByKWriter.write('\n');

                certaintyModeClassByKWriter.write('\n');
                certaintyAverageClassByKWriter.write('\n');
                certaintyDSClassByKWriter.write('\n');
                certaintyPBTClassByKWriter.write('\n');
                certaintyCombAveClassByKWriter.write('\n');
                certaintyModAveClassByKWriter.write('\n');
                certaintyTreePBTClassByKWriter.write('\n');                    

                for (int index = 0; index < 6; index++){
                    pctCorrectModeClassByKWriter.write(classNames[index+1] + ",");  
                    pctCorrectAverageClassByKWriter.write(classNames[index+1] + ",");
                    pctCorrectDSClassByKWriter.write(classNames[index+1] + ",");
                    pctCorrectPBTClassByKWriter.write(classNames[index+1] + ",");
                    pctCorrectCombAveClassByKWriter.write(classNames[index+1] + ",");
                    pctCorrectModAveClassByKWriter.write(classNames[index+1] + ",");
                    pctCorrectTreePBTClassByKWriter.write(classNames[index+1] + ",");

                    certaintyModeClassByKWriter.write(classNames[index+1] + ",");
                    certaintyAverageClassByKWriter.write(classNames[index+1] + ",");
                    certaintyDSClassByKWriter.write(classNames[index+1] + ",");
                    certaintyPBTClassByKWriter.write(classNames[index+1] + ",");
                    certaintyCombAveClassByKWriter.write(classNames[index+1] + ",");
                    certaintyModAveClassByKWriter.write(classNames[index+1] + ",");
                    certaintyTreePBTClassByKWriter.write(classNames[index+1] + ",");                                        

                    for (int k2 = 0; k2 < 300; k2++){                   
                        pctCorrectModeClassByKWriter.write(corrModeClByK[index][k2] + ",");
                        pctCorrectAverageClassByKWriter.write(corrAverageClByK[index][k2] + ","); 
                        pctCorrectDSClassByKWriter.write(corrDSClByK[index][k2] + ","); 
                        pctCorrectPBTClassByKWriter.write(corrPBTClByK[index][k2] + ","); 
                        pctCorrectCombAveClassByKWriter.write(corrCombAveClByK[index][k2] + ","); 
                        pctCorrectModAveClassByKWriter.write(corrModAveClByK[index][k2] + ","); 
                        pctCorrectTreePBTClassByKWriter.write(corrTreePBTClByK[index][k2] + ","); 

                        certaintyModeClassByKWriter.write(certModeClByK[index][k2] + ",");
                        certaintyAverageClassByKWriter.write(certAverageClByK[index][k2] + ","); 
                        certaintyDSClassByKWriter.write(certDSClByK[index][k2] + ","); 
                        certaintyPBTClassByKWriter.write(certPBTClByK[index][k2] + ","); 
                        certaintyCombAveClassByKWriter.write(certCombAveClByK[index][k2] + ","); 
                        certaintyModAveClassByKWriter.write(certModAveClByK[index][k2] + ","); 
                        certaintyTreePBTClassByKWriter.write(certTreePBTClByK[index][k2] + ",");                     

                    }

                    pctCorrectModeClassByKWriter.write('\n');
                    pctCorrectAverageClassByKWriter.write('\n');
                    pctCorrectDSClassByKWriter.write('\n');
                    pctCorrectPBTClassByKWriter.write('\n');
                    pctCorrectCombAveClassByKWriter.write('\n');
                    pctCorrectModAveClassByKWriter.write('\n');
                    pctCorrectTreePBTClassByKWriter.write('\n');

                    certaintyModeClassByKWriter.write('\n');
                    certaintyAverageClassByKWriter.write('\n');
                    certaintyDSClassByKWriter.write('\n');
                    certaintyPBTClassByKWriter.write('\n');
                    certaintyCombAveClassByKWriter.write('\n');
                    certaintyModAveClassByKWriter.write('\n');
                    certaintyTreePBTClassByKWriter.write('\n');                                        

                    System.out.println("finished writing row " + index);
                }                 

                pctCorrectModeClassByKWriter.close();
                pctCorrectAverageClassByKWriter.close();
                pctCorrectDSClassByKWriter.close();
                pctCorrectPBTClassByKWriter.close();
                pctCorrectCombAveClassByKWriter.close();
                pctCorrectModAveClassByKWriter.close();
                pctCorrectTreePBTClassByKWriter.close();

                certaintyModeClassByKWriter.close();
                certaintyAverageClassByKWriter.close();
                certaintyDSClassByKWriter.close();
                certaintyPBTClassByKWriter.close();
                certaintyCombAveClassByKWriter.close();
                certaintyModAveClassByKWriter.close();
                certaintyTreePBTClassByKWriter.close();     

            } catch (IOException ioe){
                System.out.println("ioexception: " + ioe.getMessage());
            }        

        }

        private void writeConfusionMatrices(){
            int[] kValues = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 125, 150, 175, 200, 225, 250, 275, 300};

            File certMatrices = new File("D:/synthLandscan/randomStarts/momentData/summaries/confMatrices/" + knn + moment + "Certainty.csv");
            File confMatrices = new File("D:/synthLandscan/randomStarts/momentData/summaries/confMatrices/" + knn + moment + "Confusion.csv");

            try{

                FileWriter certMatricesWriter = new FileWriter(certMatrices);
                FileWriter confMatricesWriter = new FileWriter(confMatrices);          

                for (int op = 0; op < operators.length; op++){
                    for (int kValue = 0; kValue < kValues.length; kValue++){
                        certMatricesWriter.write(kValues[kValue] + "_" + operators[op]);
                        confMatricesWriter.write(kValues[kValue] + "_" + operators[op]);                  

                        for (int klass = 0; klass < 6; klass++){
                            certMatricesWriter.write("," + classNames[klass+1]);
                            confMatricesWriter.write("," + classNames[klass+1]);                     
                        }

                        certMatricesWriter.write('\n');
                        confMatricesWriter.write('\n');             

                        for (int actual = 0; actual < 6; actual++){
                            certMatricesWriter.write(classNames[actual+1]);
                            confMatricesWriter.write(classNames[actual+1]);                      

                            for (int hypo = 0; hypo < 6; hypo++){
                                certMatricesWriter.write("," + myMasses[kValues[kValue]-2][op][actual][hypo]);
                                confMatricesWriter.write("," + myClasses[kValues[kValue]-2][op][actual][hypo]);                              
                            }

                            certMatricesWriter.write('\n');
                            confMatricesWriter.write('\n');   
                        }

                        certMatricesWriter.write('\n');
                        confMatricesWriter.write('\n');                  
                    }  //end for kValue
                }  //end for op

                certMatricesWriter.close();
                confMatricesWriter.close();           

            } catch (IOException ioe){
                System.out.println("ioexception: " + ioe.getMessage());
            }
            
        }
        
    }            
    
}
