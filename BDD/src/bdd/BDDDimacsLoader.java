/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdd;

import java.util.*;
import java.io.*;

/**
 * Loads a DIMACS file in a BDD.
 * @author diegoj
 */
public class BDDDimacsLoader {
    
    /** Path of the DIMACS file */
    String filename;
    
    /**
     * Constructor: builds a BDDDimacsLoader from the DIMACS file path.
     * @param String filename Path of the dimacs file.
     */
    public BDDDimacsLoader(String filename){
        this.filename = filename;
    }
    
     /**
     * Reads a dimacs file an builds a BDD containing the CNF.
     * This method creates a BDD at once, it does not use the operator apply of the BDD.
     * See http://people.sc.fsu.edu/~jburkardt/data/cnf/cnf.html for a dimacs format description.
     * @see BDD
     * @param filename Name of the file containing the CNF in dimacs format.
     */
    public BDD loadFile(){
        int numVariables = 0;
        int numClausules = 0;
        String formula = "";
        
        try{
          // Open the file that is the first 
          // command line parameter
          FileInputStream fstream = new FileInputStream(filename);
          // Get the object of DataInputStream
          DataInputStream in = new DataInputStream(fstream);
          BufferedReader br = new BufferedReader(new InputStreamReader(in));
          String line;
          
          //Read File Line By Line
          while ((line = br.readLine()) != null && line.length()>0)
          {
              if(line.length()==0)
                  break;
              if (line.charAt(0)!='c'){
                  if(line.charAt(0)=='p')
                  {
                      String[] content = line.split(" ");
                      numVariables = Integer.parseInt(content[2]);
                      numClausules = Integer.parseInt(content[3]);
                  }
                  else
                  {
                    String content = line.substring(0, line.length()-2).trim();
                    content = content.replaceAll(" ", " || ");
                    content = content.replaceAll("-", " !");
                    content = content.replaceAll("  ", " ");
                    formula += "("+content +")"+ "&&";
                  }
              }
          }
          formula = formula.substring(0, formula.length()-2);
          //Close the input stream
          in.close();
         }
        catch (Exception e)
         {
             //Catch exception if any
             System.err.println("Error: " + e.getMessage());
             e.printStackTrace();
        }
      
        ArrayList<String> variables = new ArrayList<>();
        
        formula = formula.replaceAll("(\\d+)", "x$1_");
        formula = formula.trim();
        
        for(int i=1; i<=numVariables; i++)
            variables.add("x"+i+"_");
        
        BDD bdd = new BDD(formula, variables);
        return bdd;
    }
   
    
    /**
     * Reads a dimacs file an builds a BDD containing the CNF.
     * This method creates a BDD at once, it DOES use the operator apply of the BDD.
     * See http://people.sc.fsu.edu/~jburkardt/data/cnf/cnf.html for a dimacs format description.
     * @see BDD
     * @param filename Name of the file containing the CNF in dimacs format.
     * @return BDD BDD tree with the formula contained in the filename.
     */
    public BDD loadFileUsingApplyAlgorithm(int cnfs_by_bdd){
        //System.out.println("runLoadDimacsFile");
        int numVariables = 0;
        int numClausules = 0;
        String formula = "";
        ArrayList<String> variables = new ArrayList<>();
        BDD bdd = null;
        try{
          // Open the file that is the first 
          // command line parameter
          FileInputStream fstream = new FileInputStream(filename);
          // Get the object of DataInputStream
          DataInputStream in = new DataInputStream(fstream);
          BufferedReader br = new BufferedReader(new InputStreamReader(in));
          String line;

          int formulaIndex = 1;
          String formulaBDD = "";
          
          //Read File Line By Line
          while ((line = br.readLine()) != null && line.length()>0)
          {
              if(line.length()==0)
                  break;
              //System.out.println("Otra línea");
              if (line.charAt(0)!='c')
              {
                  if(line.charAt(0)=='p')
                  {
                      String[] content = line.split(" ");
                      numVariables = Integer.parseInt(content[2]);
                      numClausules = Integer.parseInt(content[3]);
                      for(int i=1; i<=numVariables; i++){
                        variables.add("x"+i+"_");
                        //System.out.println(variables.get(i-1));
                      }
                  }
                  else
                  {
                    String formulaI = line.substring(0, line.length()-2).trim();
                    formulaI = formulaI.replaceAll(" ", " || ");
                    formulaI = formulaI.replaceAll("-", " !");
                    formulaI = formulaI.replaceAll("  ", " ");
                    formulaI += " ";
                    //System.out.println(formulaI);
                    formulaI = formulaI.replaceAll("(\\d+)", "x$1_");
                    formulaI = formulaI.trim();
                    
                    BDD bddI = null;
                    if(formulaIndex==1)
                    {
                        formulaBDD = "("+formulaI+")";
                        bddI = new BDD(formulaBDD, variables);
                        bdd = bddI;
                    }
                    else
                    {
                        if(formulaIndex % cnfs_by_bdd == 0){
                            bddI = new BDD(formulaBDD, variables);
                            bdd = bdd.apply(bddI,"and");
                            formulaBDD = "";
                        }
                        else{
                            if(formulaBDD.equals(""))
                                formulaBDD = "("+formulaI+")";
                            else
                                formulaBDD = formulaBDD+ " && "+ "("+formulaI+")";
                        }
                    }
                    System.out.println("-- > " + formulaIndex + " cláusulas de "+ numClausules);
                    //System.out.println(formulaBDD);
                    // Avanzamos la CNF
                    formulaIndex++;
                    //System.out.println(formulaIndex+"/"+numClausules);
                  }
              }
          }
          //Close the input stream
          in.close();
         }catch (Exception e){//Catch exception if any
             System.err.println("Error: "+e.getMessage());
             e.printStackTrace();
        }
        return bdd;    
    }

     /**
     * Reads a dimacs file an builds a BDD containing the CNF.
     * This method creates a BDD at once, it DOES use the operator apply of the BDD.
     * See http://people.sc.fsu.edu/~jburkardt/data/cnf/cnf.html for a dimacs format description.
     * @see BDD
     * @param filename Name of the file containing the CNF in dimacs format.
     * @return BDD BDD tree with the formula contained in the filename.
     */
    public BDD loadFileUsingApplyAlgorithm(){
        return this.loadFileUsingApplyAlgorithm(3);
    }
}
