/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdd;

import java.util.*;

/**
 * 
 * @author diegoj
 */
public class Main {
    
    /**
     * Reads a dimacs file an builds a BDD containing the CNF.
     * This method creates a BDD at once, it DOES use the operator apply of the BDD.
     * See http://people.sc.fsu.edu/~jburkardt/data/cnf/cnf.html for a dimacs format description.
     * @see BDD
     * @param filename Name of the file containing the CNF in dimacs format.
     */
    protected static void runLoadDimacsFile(String filename, boolean useApplyAlgorithm){
        BDDDimacsLoader loader = new BDDDimacsLoader(filename);
        BDD bdd = null;
        if(useApplyAlgorithm)
            bdd = loader.loadFileUsingApplyAlgorithm();
        else
            bdd = loader.loadFile();
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
    protected static void printDimacsFile(String filename, boolean useApplyAlgorithm){
        BDDDimacsLoader loader = new BDDDimacsLoader(filename);
        BDD bdd = null;
        if(useApplyAlgorithm)
            bdd = loader.loadFileUsingApplyAlgorithm(15);
        else
            bdd = loader.loadFile();
        //System.out.println("PRINT DIMACS FILE");
        //System.out.flush();
        //System.out.println(bdd.vertices);
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
    protected static void printFmla(String fmla, ArrayList<String> variables){
        BDD bdd = new BDD(fmla, variables);
        BDDPrinter printer = new BDDPrinter(bdd);
        printer.print("./"+bdd.name);
    }
    
    /**
     * Generate the probabilities and shows them to user.
     */
    protected static void runFormula(String formula, ArrayList<String> variables){
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
            System.out.println("Main program of BDD in Java");
            System.out.println("1. Probability computation:");
            System.out.println("\tFrom formula: java -jar BDD.jar --prob fmla <logic formula in java code> <variables in 'x1,x2,...,xN' format>");
            System.out.println("\tFrom dimacs file: java -jar BDD.jar --prob dimacs <filepath of dimacs file> [apply]");
            System.out.println("2. BDD printing:");
            System.out.println("\tFrom formula: java -jar BDD.jar --print fmla <logic formula in java code> <variables in 'x1,x2,...,xN' format>");
            System.out.println("\tFrom dimacs file: java -jar BDD.jar --print dimacs <filepath of dimacs file> [apply]");
            return;
        }
        String text = "";
        String option = args[0];
        TimeMeasurer t = new TimeMeasurer(option);
        switch (option) {
            case "--runtests":
                int testIndex = 0;
                if (args.length == 2)
                    testIndex = Integer.parseInt(args[1]);
                Tester.run(testIndex);
                break;
            case "--print":
                text = "Printing a formula";
                if(args[1].equalsIgnoreCase("fmla")){
                    text += " get from commandline";
                    System.out.println(text);
                    ArrayList<String> variables = new ArrayList<String>( Arrays.asList(args[3].split(",\\s*") ));
                    Main.printFmla(args[2], variables);
                }
                else if(args[1].equalsIgnoreCase("dimacs")){
                    boolean useApply = args.length==4 && args[3].equals("apply");
                    text += " get from dimacs file";
                    if(useApply)
                        text += " use apply operator in each CNF";
                    System.out.println(text);
                    Main.printDimacsFile(args[2], args.length==4 && args[3].equals("apply"));
                }
                else{
                    System.out.println("You are not using this software correctly");
                }
            break;
            case "--prob":
                text = "Computing probabilities";
                if(args[1].equalsIgnoreCase("fmla")){
                    text += " get from commandline";
                    System.out.println(text);
                    ArrayList<String> variables = new ArrayList<String>( Arrays.asList(args[3].split(",\\s*") ));
                    Main.runFormula(args[2], variables);
                }
                else if(args[1].equalsIgnoreCase("dimacs")){
                    boolean useApply = args.length==4 && args[3].equals("apply");
                    text += " get from dimacs file";
                    if(useApply)
                        text += " use apply operator in each CNF";
                    System.out.println(text);
                    Main.runLoadDimacsFile(args[2], useApply);
                }
                else{
                    System.out.println("You are not using this software correctly");
                }
            break;
        }
        t.end();
        t.show();
    }
}
