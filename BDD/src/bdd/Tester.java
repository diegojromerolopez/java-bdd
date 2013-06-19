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
public class Tester {
 
    protected static void printHeader(String header){
        System.out.println("\n\n");
        System.out.println("============================================");
        System.out.println("============================================");
        System.out.println(header);
    }
    
    /**
     * Test 1
     */
    protected static void test1(){
        printHeader("Test 1");
        ArrayList<String> variables = new ArrayList<>();
        variables.add("a");
        variables.add("b");
        BDD bdd = new BDD("a || b", variables);
        System.out.println(bdd.toString());
        // Evaluamos la función
        bdd.printAsTable();
       
        boolean[] v = {false, true};
        System.out.println("a\tb\ta||b");
        for(int x1=0; x1<2; x1++)
        {
            for(int x2=0; x2<2; x2++)
            {
                ArrayList<Boolean> values = new ArrayList<>(Arrays.asList(v[x1],v[x2]));
                boolean result = bdd.eval(values);
            
                System.out.println((x1==1?"true":"false")+"\t"+(x2==1?"true":"false")+"\t"+(result?"true":"false"));
            }
        }
        
        double[] P = bdd.P_xi();
        System.out.println("Probabilidades");
       
        for(double p : P) System.out.print(p+", ");
    }
    
    /**
     * Test 2
     */
    protected static void test2(){
        printHeader("Test 2");
        ArrayList<String> variables = new ArrayList<>();
        variables.add("x1");
        variables.add("x2");
        variables.add("x3");
        variables.add("x4");
        variables.add("x5");
        variables.add("x6");
        BDD bdd = new BDD("(x1 && x2) || (x3 && x4) || (x5 && x6)", variables);
        System.out.println(bdd.toString());
        // Evaluamos la función
        bdd.printAsTable();
        double[] P = bdd.P_xi();
        System.out.println("Probabilidades");
       
        for(double p : P)
            System.out.print(p+", ");
        System.out.println("");
        System.out.println("Debería ser "+(23/37.)+", "+(23/37.)+", "+(23/37.)+", "+(23/37.)+", "+(23/37.)+", "+(23/37.));
    }
    
    /**
     * Test 3
     */
    protected static void test3(){
        printHeader("Test 3");
        ArrayList<String> variables = new ArrayList<>();
        variables.add("a");
        variables.add("b");
        variables.add("c");
        BDD bdd = new BDD("(!a) || b || c", variables);
        System.out.println(bdd.toString());
        // Evaluamos la función
        bdd.printAsTable();
        double[] P = bdd.P_xi();
        System.out.println("Probabilidades");
       
        for(double p : P)
            System.out.print(p+", ");
        System.out.println("");
        System.out.println("Debería ser "+(3/7.)+", "+(4/7.)+", "+(4/7.));
    }
    
    protected static void test4(){
        printHeader("Test 4");
        ArrayList<String> variables = new ArrayList<>();
        variables.add("a");
        variables.add("b");
        variables.add("c");
        
        BDD bdd1 = new BDD("(!a) || b", variables);
        System.out.println("BDD1");
        bdd1.printAsTable();

        BDD bdd2 = new BDD("c", variables);
        System.out.println("BDD2");
        bdd2.printAsTable();

        BDD bdd3 = bdd1.apply(bdd2, "or");
        System.out.println("BDD3 (BDD1 or BDD2)");
        bdd3.printAsTable();

        
        BDD bdd4 = new BDD("(!a) || b || c", variables);
        System.out.println("BDD4 ((!a) || b || c)");
        bdd4.printAsTable();
        
        
        
       /*
        double[] P = bdd3.P_xi();
        System.out.println("Probabilidades");
       
        for(double p : P)
            System.out.print(p+", ");
        System.out.println("");
        System.out.println("Debería ser "+(3/7.)+", "+(4/7.)+", "+(4/7.));
        * 
        */
    }
    
    protected static void run(int t){
        if(t==0 || t==1)Tester.test1();
        if(t==0 || t==2)Tester.test2();
        if(t==0 || t==3)Tester.test3();
        if(t==0 || t==4)Tester.test4();
    }
    
    
}
