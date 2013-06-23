/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdd;

import java.util.*;
import java.io.*;

/**
 *
 * @author diegoj
 */
public class ProbComputator {

    public static long startTime = 0;
    public static long endTime = 0;
    
    protected static void initStartTime(){
        ProbComputator.startTime = System.nanoTime();
    }
    
    protected static void initEndTime(){
        ProbComputator.endTime = System.nanoTime();
        ProbComputator.elapsedTime = ProbComputator.endTime - ProbComputator.startTime;
    }
    
    public static long elapsedTime = 0;
    
    /**
     * Converts the elapsed time to a human readable format.
     * @return String Time elapsed in human-friendly form.
     */
    protected static String getElapsedTimeAsHumanText(){
        long ns = elapsedTime;
        long us = elapsedTime / 1_000;
        ns = elapsedTime % 1_000;
        long ms = us / 1_000;
        us = us % 1_000;
        long s = ms / 1_000;
        ms = ms % 1_000;
        long m = s / 60;
        s = s % 60;
        long h = m / 60;
        m = m % 60;
        return h+" h, "+m+" m, "+s+" s, "+ms+" ms, "+us+"µs";
    } 
    
    /**
     * Reads a dimacs file an builds a BDD containing the CNF.
     * This method creates a BDD at once, it does not use the operator apply of the BDD.
     * See http://people.sc.fsu.edu/~jburkardt/data/cnf/cnf.html for a dimacs format description.
     * @see BDD
     * @param filename Name of the file containing the CNF in dimacs format.
     */
    protected static void runBulkLoadDimacsFile(String filename){
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
          while ((line = br.readLine()) != null)
          {
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
         }catch (Exception e){//Catch exception if any
             System.err.println("Error: " + e.getMessage());
        }
      
        ArrayList<String> variables = new ArrayList<>();
        for(int i=1; i<=numVariables; i++){
            variables.add("x"+i);
            formula = formula.replaceAll(""+i, "x"+i);
        }
        
        System.out.println(formula);
        BDD bdd = new BDD(formula, variables);
        //System.out.println(bdd.toString());
        // Evaluamos la función
        bdd.printAsTable();
        double[] P = bdd.P_xi();
        System.out.println("Probabilidades");
       
        for(double p : P)
            System.out.print(p+", ");
        System.out.println("");
    }

    /**
     * Reads a dimacs file an builds a BDD containing the CNF.
     * This method creates a BDD at once, it DOES use the operator apply of the BDD.
     * See http://people.sc.fsu.edu/~jburkardt/data/cnf/cnf.html for a dimacs format description.
     * @see BDD
     * @param filename Name of the file containing the CNF in dimacs format.
     * @return BDD BDD tree with the formula contained in the filename.
     */
    protected static BDD loadDimacsFile(String filename){
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
                    //System.out.println(formulaI);
                    BDD bddI = new BDD(formulaI, variables);
                    //System.out.println("patas");
                    if(formulaIndex==1)
                        bdd = bddI;
                    else
                        bdd = bdd.apply(bddI,"and");
                    //System.out.println("-- > " + formulaIndex + " cláusulas de "+ numClausules);
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
     */
    protected static void runLoadDimacsFile(String filename){
        BDD bdd = ProbComputator.loadDimacsFile(filename);
        System.out.println("Función " + bdd.function);
        //System.out.println(bdd.toString());
        // Evaluamos la función
        bdd.printAsTable();
        double[] P = bdd.P_xi();
        System.out.println("Probabilities");
        for(double p : P)
            System.out.print(p+", ");
        System.out.println("");    
    }

     /**
     * Reads a dimacs file an prints the BDD contained in the file.
     * This method DOES NOT creates a BDD at once, it DOES use the operator apply of the BDD.
     * See http://people.sc.fsu.edu/~jburkardt/data/cnf/cnf.html for a dimacs format description.
     * @see BDD
     * @param filename Name of the file containing the CNF in dimacs format.
     */
    protected static void printDimacsFile(String filename){
        BDD bdd = ProbComputator.loadDimacsFile(filename);
        System.out.println("PRINT DIMACS FILE");
        System.out.flush();
        System.out.println(bdd.vertices);
        BDDPrinter printer = new BDDPrinter(bdd);
        printer.print("./"+filename);
    }
    
     /**
     * Creates a BDD from a formula.
     * This method DOES NOT creates a BDD at once, it DOES use the operator apply of the BDD.
     * See http://people.sc.fsu.edu/~jburkardt/data/cnf/cnf.html for a dimacs format description.
     * @see BDD
     * @param String fmla Logic formula.
     * @param ArrayList<String> variables Variables used in the formula. Note that this parameter gives the order of them.
     */
    protected static void printFmla(String fmla, ArrayList<String>variables){
        BDD bdd = new BDD(fmla, variables);
        BDDPrinter printer = new BDDPrinter(bdd);
        printer.print("./"+bdd.name);
    }
    
    /**
     * Generate the probabilities and shows them to user.
     */
    protected static void runFormula(String formula, String[] _variables){
        ArrayList<String> variables = new ArrayList<>(Arrays.asList(_variables));
        BDD bdd = new BDD(formula, variables);
        System.out.println(bdd.toString());
        // Evaluamos la función
        bdd.printAsTable();
        double[] P = bdd.P_xi();
        System.out.println("Probabilities");
        System.out.println(Arrays.toString(P));
    }
    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Si no hay 1 argumento, mostramos 
        if(args.length<1)
        {
            System.out.println("uso java BDD.jar --runtests | --dimacsfile <file> | --formula <boolean_fmla> <comma separated variables>");
            return;
        }
        
        String option = args[0];

        ProbComputator.initStartTime();
        switch (option) {
            case "--runtests":
                int t = 0;
                if (args.length == 2)
                    t = Integer.parseInt(args[1]);
                Tester.run(t);
                break;
            case "--dimacsfile":
                if (args.length == 3 && args[1].equals("bulk"))
                    runBulkLoadDimacsFile(args[2]);
                else
                    runLoadDimacsFile(args[1]);
                break;
            case "--formula":
                runFormula(args[1],args[2].split(","));
                break;
            case "--print":
                //System.out.println(args.length);
                //System.out.println(args[2]);
                if (args.length == 3 && (args[1].equals("dimacs") || args[1].equals("dimacsfile"))){
                    printDimacsFile(args[2]);
                    return;
                }
                else if(args.length == 4 && args[1].equals("fmla")){
                    ArrayList<String> variables = new ArrayList<String>( Arrays.asList(args[3].split(",\\s*") ));
                    printFmla(args[2], variables);
                }
        }
        ProbComputator.initEndTime();
        System.out.println("\nTime passed:\n" + ProbComputator.getElapsedTimeAsHumanText());
    }
}
