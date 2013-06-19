/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdd;

import java.util.*;
import bdd.Util.*;
import java.util.regex.*;
import org.mvel2.MVEL;
import graphvizjava.GraphViz;

/**
 * ROBDD based in the text An Introduction to Binary Decision Diagrams by Henrik Reif Andersen
 * And the Python implementation of e-mux (https://github.com/e-mux/python-bdd)
 * @author diegoj
 */
public class BDD {

    //! Function variable names (used to construct the dot graph)
    ArrayList<String> variable_names;

    //! Name of the boolean function (defaults to function.func_name if that exists)
    String name = null;

    //! Original boolean function that represents this BDD
    String function = null;

    //! Number of boolean variables
    int n = 0;

    //! Root vertex of the BDD
    Vertex root = null;

    //! Is the BDD reduced
    boolean is_reduced = false;

    //! Vertices in an array
    ArrayList<Vertex> vertices;

    
    /**
     * Evaluate the function inside the BDD (in its variable function).
     * It uses as arguments of that function the boolean values in path.
     * NOTE: uses the Apache Licensed MVEL library https://github.com/mvel/mvel
     * @param bdd BDD that contains the function and the variable_names used in that formula.
     * @param path List of boolean values used as arguments of the function.
     */
    protected static boolean evaluateFunction(BDD bdd, ArrayList<Boolean> path)
    {
        String function = bdd.function;
        int path_size = path.size();
        //System.out.println(function);
        //function.replaceAll("x(\\d+)",bdd.variable_names.get("$1"));
        Pattern pattern = Pattern.compile("x(\\d+)");
        Matcher matcher = pattern.matcher(function);
        //System.out.println(matcher.groupCount());
        //int[] variableIndices = new int[matcher.groupCount()];
        int i = 0;
        while(matcher.find())
        {
            String group = matcher.group().substring(1);
            int variableIndex = Integer.parseInt(group)-1;
            if(variableIndex < path_size){
                String variable = bdd.variable_names.get(variableIndex);
                String replacement = path.get(variableIndex)?"true":"false";
                //System.out.println(variable +"="+replacement);
                function = function.replaceAll(variable, replacement);
            }
            i++;
            //System.out.println(group);
        }
        //System.out.println("Función: "+bdd.function);
        //System.out.println("Función final: "+function);
        return (Boolean)MVEL.eval(function);
    }

    
    /**
     * Evaluate the function given the values of the variables in path.
     * @return Value of the function evaluated using the path values.
     */
    protected boolean evaluate(ArrayList<Boolean> path){
        // Evaluate function_str with path variables in variable_names order
        return BDD.evaluateFunction(this, path);
    }

    
    /**
     * Function used to generate BDD from a function.
     * NOTE: this is a recursive function.
     * @param path List of boolean values given to compute leaf nodes.
     */
    private Vertex generateTreeFunction(ArrayList<Boolean> path){
            int path_len = path.size();
            if (path_len<this.n)
            {
                //System.out.println(path);
                ArrayList<Boolean> path_low = new ArrayList<>(path);
                path_low.add(false);
                Vertex v_low = this.generateTreeFunction(path_low);

                ArrayList<Boolean> path_high = new ArrayList<>(path);
                path_high.add(true);
                Vertex v_high = this.generateTreeFunction(path_high);
                //System.out.println("Parent of "+v_low.id+" y "+v_high.id);
                // Create a new vertex
                return new Vertex(path_len+1, v_low, v_high);
            }
            else if(path_len==this.n)
            {
                // reached leafes
                boolean value = this.evaluate(path);
                return new Vertex(value);
            }
            return null;
     }

    
    /**
     * Prints the BDD as a table. Useful for validation of the BDD generated.
     */
    void printAsTable()
    {
        this.reset();
        System.out.println("");
        System.out.println("-------------------------------------------------");
        System.out.print("Vertices: ");
        System.out.println(this.vertices);
        System.out.print("Variables: ");
        System.out.println(this.variable_names);
        System.out.println("-------------------------------------------------");
        System.out.println("pos.\tindex\tv_name\tlow\thigh\tmark");
        for (Vertex v : this.vertices){
            String v_name = null;
            //System.out.println(v);
            if(v.isLeaf()){
                v_name = Boolean.toString(v.value);
            } else if (v.i < this.variable_names.size()){
                v_name = this.variable_names.get(v.i);
            }
            String low_id = v.low!=null?Integer.toString(v.low.id):"null";
            String high_id = v.high!=null?Integer.toString(v.high.id):"null";

            System.out.println(Integer.toString(v.id)+"\t"+Integer.toString(v.i)+"\t"+v_name+"\t"+low_id+"\t"+high_id+"\t"+Boolean.toString(v.visited));
        }
        System.out.println("-------------------------------------------------");
        System.out.println("");
    }


    /**
     * Constructor of BDD.
     * @param function_str String containing the boolean formula. Use Java representation of the formula. Don't forget using parentheses.
     * @param variables Name of the variables and order of them in the BDD.
     */
    BDD(String function_str, ArrayList<String> variables){
        this.function = function_str;
        this.name = function_str;
        this.variable_names = variables;
        this.n = variables.size();
        ArrayList<Boolean> path = new ArrayList<>();
        this.root = this.generateTreeFunction(path);
        //System.out.println("BDD Cnstructor");
        // For our own safety, we reduce every BDD built
        this.reduce();
    }

    
    /**
     * Mark all vertices as non-visited.
     * Useful to restart the transversion.
     */
    protected void unvisitVertices(){
        for(Vertex v : this.vertices)
            v.visited = false;
    }

    
    /**
     * Take all measures to make the BDD ready for the algoritms.
     * You MUST call this method before executing them.
     * @return Informs if there has been taken measures to restart the BDD.
     */
    protected boolean reset(){

        if(this.vertices.size()<2){
            this.unvisitVertices();
            return false;
        }
        // We need in this position this values
        // 0 False
        // 1 True
        if(this.vertices.get(1).value == false){
            Collections.swap(this.vertices,0,1);
            this.vertices.get(0).id = 0;
            this.vertices.get(1).id = 1;
        }
        this.vertices.get(0).i = this.n;
        this.vertices.get(1).i = this.n;
        // Everty other vertex is in its position
        for (int i=2; i<this.vertices.size(); i++){
            Vertex v = this.vertices.get(i);
            v.i = v.index - 1;
        }

        this.unvisitVertices();
        return true;
    }

    
    /**
     * Evaluate the boolean function using truth arguments.
     * @param args Arguments of the function. Remember, in the order given by variable_names.
     * @return boolean value given of evaluating the BDD given the args arguments.
     */
    public boolean eval(ArrayList<Boolean> args)
    {
        // Private function that recursively evaluates one tree
        class EvaluateVertex{
            public boolean run(Vertex v, ArrayList<Boolean> args)
            {
                if(!v.isLeaf()){
                    if(!args.get(v.index-1))
                        return this.run(v.low, args);
                    else
                        return this.run(v.high, args);
                }
                return v.value;
            }
        }

        if (args.size() != this.n)
            throw new ArrayIndexOutOfBoundsException("This number of variables is different than the BDD's one");
        
        // First call
        EvaluateVertex e = new EvaluateVertex();
        return e.run(this.root, args);
    }

    /**
     * Traverses all the BDD and groups the vertices by levels.
     * @return Vertex of each level of the BDD.
     */
    public ArrayList<ArrayList<Vertex>> traverse()
    {
        class Traversor{
            public void run(Vertex v, ArrayList<ArrayList<Vertex>> levels)
            {
                if(!v.isLeaf())
                {
                    levels.get(v.index-1).add(v);
                    this.run(v.low, levels);
                    this.run(v.high, levels);
                }
                else{
                    levels.get(levels.size()-1).add(v);
                }
            }
        }
        ArrayList<ArrayList<Vertex>> levels = new ArrayList<ArrayList<Vertex>>();
        for(int i=0; i<this.n+1; i++)
            levels.add(new ArrayList<Vertex>());
        Traversor traversor = new Traversor();
        traversor.run(this.root,levels);
        return levels;
    }

    /**
     * Reduces the BDD tree deleting redundant nodes.
     */
    final public void reduce()
    {
        if (this.is_reduced)
            return;

        ArrayList<Vertex> result = new ArrayList<>();
        int nextid = 0;

        //Traverse tree, so that level[i] contains all vertices of level i
        ArrayList<ArrayList<Vertex>> levels = this.traverse();
        Collections.reverse(levels);
        //System.out.println(levels);

        // Bottom-up reduction
        for(ArrayList<Vertex> level : levels)
        {
            HashMap<String,ArrayList<Vertex>> isoMap = new HashMap<>();
            for(Vertex v : level)
            {
                // Generate key
                String key = "";
                if (v.isLeaf()){
                    key = Boolean.toString(v.value);
                }else if( v.low.id == v.high.id ){
                    v.id = v.low.id;
                    continue;
                }else{
                    key = Integer.toString(v.low.id) + " " + Integer.toString(v.high.id);
                }
                // Append under key to iso_map
                if (isoMap.containsKey(key)){
                    isoMap.get(key).add(v);
                }else{
                    ArrayList<Vertex> key_array = new ArrayList<>();
                    key_array.add(v);
                    isoMap.put(key, key_array);
                }
            }
            for(String key : isoMap.keySet())
            {
                // Set same id for isomorphic vertices
                ArrayList<Vertex> isomorphicVertices = isoMap.get(key);
                for (Vertex v : isomorphicVertices)
                    v.id = nextid;
                nextid += 1;

                // Store one isomorphic vertex in result
                Vertex x = isomorphicVertices.get(0);
                result.add(x);

                // Update references
                if (!x.isLeaf()){
                    // Here's the core of the reduction method
                    // If there is a redundant node whose id is equal to one of its descendants' id
                    // we get the descendant, so we skip the redundant node
                    x.low = result.get(x.low.id);
                    x.high = result.get(x.high.id);
                }
            }


            //

        }
        // Update reference to root vertex (alwayes the last entry in result)
        this.root = result.get(result.size()-1);
        this.vertices = result;
        this.is_reduced = true;
        //this.reset();
    }

    
    /**
     * Overloading of apply method.
     * @param bdd BDD that's going to be operated with ours.
     * @param operation String that contains the name of the operation to compute between the two BDDs.
     * @return A new BDD that's the result of the computation between these two.
     */
    public BDD apply(BDD bdd, String operation){
        LogicFunction op = null;
        if(operation.equals("or"))
            op = new Or();
        else if(operation.equals("and"))
            op = new And();
        else if(operation.equals("=>"))
            op = new Implication();
        //System.out.println(op.getOp());
        //System.out.println("APPLY");
        return this.apply(bdd, op);
    }

    
    /**
     * Apply method.
     * @param bdd BDD that's going to be operated with ours.
     * @param function Object that contains the name of the operation to compute between the two BDDs.
     * @return A new BDD that's the result of the computation between these two.
     */
    public BDD apply(BDD bdd, LogicFunction function)
    {
        // If the trees are not reduced, we reduce them first
        // This algorithm needs this reduction don't delete it
        if (!bdd.is_reduced)
            bdd.reduce();
        if (!this.is_reduced)
            this.reduce();

        // Hash that contains each vertex once
        HashMap<String,Vertex> cache = new HashMap<>();
        final Vertex True = new Vertex(true);
        final Vertex False = new Vertex(false);

        class Applicator{
            /** Cache of vertices */
            HashMap<String,Vertex> cache;
            /** Constructor */
            Applicator(HashMap<String,Vertex> cache){
                this.cache = cache;
            }
            /**
             * Executes the apply algorithm.
             */
            // TODO: almacenar los vértices generados de alguna forma eficiente
            public Vertex run(Vertex v1, Vertex v2, LogicFunction function)
            {
                // Check if v1 and v2 have already been calculated
                String key = Integer.toString(v1.id) + " " + Integer.toString(v2.id);
                if (cache.containsKey(key)){
                    System.out.println(key+" "+cache.get(key));
                    return cache.get(key);
                }

                // Result vertex
                Vertex u = null;

                // If the vertices are both leafs,
                // apply the boolean function to them
                if (v1.isLeaf() && v2.isLeaf()){
                    if(function.run(v1.value, v2.value))
                        return True;
                    return False;
                   //u = new Vertex(function.run(v1.value,v2.value));
                   //cache.put(key, u);
                   //return u;
                }
                
                int index = -1;
                Vertex low = null;
                Vertex high = null;
                // v1.index < v2.index
                if(!v1.isLeaf() && (v2.isLeaf() || v1.index<v2.index))
                {
                    index = v1.index;
                    low = this.run(v1.low, v2, function);
                    high = this.run(v1.high, v2, function);
                }
                else if(v1.isLeaf() || v1.index>v2.index)
                {
                    index = v2.index;
                    low = this.run(v1, v2.low, function);
                    high = this.run(v1, v2.high, function);
                }
                else
                {
                    index = v1.index;
                    low = this.run(v1.low, v2.low, function);
                    high = this.run(v1.high, v2.high, function);
                }
                
                // Create the resulting vertex
                u = new Vertex(index,low,high);
                cache.put(key,u);
                return u;
            }
        }

        // First call
        String functionString = "("+this.function+") "+function.getOp()+" ("+bdd.function+")";
        //System.out.println(functionString);
        BDD newBdd = new BDD(functionString, this.variable_names);
        newBdd.n = this.n;
        Applicator applicator = new Applicator(cache);
        newBdd.root = applicator.run(this.root, bdd.root, function);
        newBdd.reduce();
        return newBdd;
    }

    
    /**
     * Informs if the BDD represents a given boolean function.
     * @param function Boolean function in String representation.
     * @return True if our BDD contains a tree with the function, False otherwise.
     */
    public boolean represents(String function){
        class Represents{
            BDD bdd;
            String function;
            public boolean run(ArrayList<Boolean> args){
                int argsLen = args.size();
                if (argsLen == this.bdd.n)
                    return this.bdd.evaluate(args) && this.bdd.eval(args);
                ArrayList<Boolean> argsFalse = new ArrayList<>(args);
                argsFalse.add(false);
                ArrayList<Boolean> argsTrue = new ArrayList<>(args);
                argsTrue.add(true);
                return this.run(argsFalse) && this.run(argsTrue);
            }
            Represents(String function, BDD bdd){ this.bdd = bdd; this.function=function; }
        }
        Pattern argPattern = Pattern.compile("[a-zA-Z]");
        int argCount = 0;
        Matcher m = argPattern.matcher(function);         // Create Matcher
        while (m.find()) { argCount++;  }

        if(argCount==this.n){
            Represents r = new Represents(function,this);
            return r.run(new ArrayList<Boolean>());
        }
        return false;
    }

    /**
     * Evaluates if these two BDDs are equals.
     * @param other The other BDD to compare ours with.
     * @return True if they represent the same function.
     */
    public boolean equals(BDD other)
    {
        return this.apply(other, new Equivalence()).root.equals(new Vertex(true));
    }

    
    /**
     * Gets the number of nodes of this BDD
     * @return Number of nodes of this BDD.
     */
    public int size()
    {
        class SizeCalculator{
            public int run(Vertex v)
            {
                if(v.isLeaf())
                    return 1;
                return 1 + this.run(v.low) + this.run(v.high);
            }
        }
        SizeCalculator s = new SizeCalculator();
        return s.run(this.root);
    }

    
    /**
     * Gets the amount of memory this BDD is using.
     * TODO: it gives the number of nodes
     * @return memory this BDD has.
     */
    public int memorySize()
    {
        class MemorySizeCalculator{
            private int sizeOf(Vertex v){
                return 1;// TODO: How can I compute the memory used by a Vertex?
            }

            public int run(Vertex v)
            {
                if(v.isLeaf())
                    return this.sizeOf(v);
                return this.sizeOf(v) + this.run(v.low) + this.run(v.high);
            }
        }
        MemorySizeCalculator s = new MemorySizeCalculator();
        return s.run(this.root);
    }


    /**
     * Convert the BDD to a String.
     * @return String that has the BDD in a so-so representation.
     */
    @Override
    public String toString()
    {
        class StringConversor{
            public String run(Vertex v)
            {
                if(v.isLeaf())
                    return v.toString();
                return v.toString() + "\n" + this.run(v.low)+"\n" + this.run(v.high)+">";
            }
        }
        StringConversor s = new StringConversor();
        return "<BDD n = "+Integer.toString(this.n)+"\n"+s.run(this.root)+">";
    }


    /**************************************************************************/
    /**************************************************************************/
    /* Algorithms of R. Heradio D. Amorós */

    /**
     * Algorithm 2: P(ψ)
     */
    protected double[] P(){
        /*
        Gets the probability of having False or True in the BDD.
        Returns a list whose
        - First element (index 0) is the probability of getting False in the function.
        - Second element (index 1) is the probability of getting True in the function.
        */

        int numVertices = this.vertices.size();
        double[] formulaSatProb = new double[numVertices];
        for(int i=0; i<numVertices; i++){
            formulaSatProb[i] = 0.0;
        }

        int i = numVertices-1;
        formulaSatProb[numVertices-1] = 1.0;   // root vertex
        while(i > 1){
            double increment = formulaSatProb[i]/2.0;
            Vertex v_i = this.vertices.get(i);
            formulaSatProb[v_i.low.id] += increment;
            formulaSatProb[v_i.high.id] += increment;
            i -= 1;
        }

        // Técnicamente los valores > 1 no valen para nada
        // formula_sat_prob=[formula_sat_prob[0],formula_sat_prob[1]]
        return formulaSatProb;
        /*
        ArrayList<Double> objFormulaSatProb = new ArrayList<Double>(numVertices);
        for(int k=0; k<numVertices; k++)
            objFormulaSatProb.set(i,formulaSatProb[i]);
        return objFormulaSatProb;
         *
         */
    }

    /**
     * Marginal probabilites.
     */
    protected void MP_xi(int v, double[] total_prob, double[] formula_sat_prob, double[] prob){
        double prob_low = 0.0;
        double prob_high = 0.0;
        //_bdd = bdd
        ArrayList<Vertex> bdd = this.vertices;
        ArrayList<String> names = this.variable_names;
        // Avoid visited nodes
        Vertex w = bdd.get(v);
        bdd.get(v).visited = !bdd.get(v).visited;

        // Base case 1
        if (bdd.get(v).low.id==1)
            prob_low = formula_sat_prob[v]/2.0;
        else if (bdd.get(v).low.id!=0){
            if (bdd.get(v).visited != bdd.get(bdd.get(v).low.id).visited)
                this.MP_xi(bdd.get(v).low.id, total_prob, formula_sat_prob, prob);
            prob_low = (total_prob[bdd.get(v).low.id] * formula_sat_prob[v]/2.0) / formula_sat_prob[bdd.get(v).low.id];
        }
        // Base case 2
        if (bdd.get(v).high.id==1)
            prob_high = formula_sat_prob[v]/2.0;
        else if (bdd.get(v).high.id!=0){
            if (bdd.get(v).visited != bdd.get(bdd.get(v).high.id).visited)
                this.MP_xi(bdd.get(v).high.id, total_prob, formula_sat_prob, prob);
            prob_high = (total_prob[bdd.get(v).high.id] * formula_sat_prob[v]/2.0) / formula_sat_prob[bdd.get(v).high.id];
        }

        total_prob[v] = prob_low + prob_high;
        // #print "{0} in prob ({1})".format(bdd[v].id,(prob))
        prob[bdd.get(v).i] += prob_high;

        // Trasversal
        int i = bdd.get(v).i+1;
        while (i<bdd.get(bdd.get(v).low.id).i){
            prob[i] += prob_low/2.0;
            i += 1;
        }
        i = bdd.get(v).i+1;
        while (i<bdd.get(bdd.get(v).high.id).i){
            prob[i] += prob_high/2.0;
            i += 1;
        }
    }

    public double[] P_xi(){
        //    int number_of_literals = bdd.n
        //var_ordering = bdd.variable_names
        //bdd_vertices = bdd.vertices
        //bdd_length = len(bdd_vertices)
        int numLiterals = this.n;
        int numVertices = this.vertices.size();

        // Incialización
        double [] totalProb = new double[numVertices];
        Arrays.fill(totalProb, 0.0);
        double [] prob = new double[numLiterals];
        Arrays.fill(totalProb, 0.0);

        // Total probabilities
        double[] formulaSatProb = this.P();

        // if debug:
        //for(int i=0; i<numVertices; i++)
        //    System.out.print(formulaSatProb[i]+", ");

        // Marginal probabilities
        this.MP_xi(numVertices-1, totalProb, formulaSatProb, prob);

        for(int i=0; i<numLiterals; i++)
            prob[i] = prob[i]/formulaSatProb[1];
        return prob;
    }
    
    
    /**************************************************************************/
    // <editor-fold defaultstate="open" desc="Dot file creation zone">
    
 
    // </editor-fold>
}// from class BDD
