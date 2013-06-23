/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdd;

import graphvizjava.*;
import java.util.*;
import bdd.Util.*;
import java.io.File;

/**
 *
 * @author diegoj
 */
public class BDDPrinter {

    /* Output file type */
    static String FILE_TYPE = "gif";
    
    /** BDD tree to print */
    BDD bdd = null;
    
    class Node {

        public BDD bdd = null;
        public Vertex v = null;
        public boolean visited = false;
        public Node low = null;
        public Node high = null;
        String name = "";
        String label = "";

        public Node(Vertex v, BDD bdd) {
            this.v = v;
            this.bdd = bdd;
        }

        /**
         * Obtains the node name: if BDD is reduced it gets the id, else returns the node path-from-root name
         */
        public String setName(String pathName) {
            if (v.index != -1) {
                this.name = bdd.variables.get(v.index) + " (" + pathName + ")";
                return this.name;
            }
            if (v.isLeaf()) {
                this.name = v.value + " (" + pathName + ")";
                return this.name;
            }
            if (v.id != -1) {
                this.name = "" + v.id;
                return this.name;
            }
            this.name = pathName;
            return this.name;
        }

        /**
         * Obtains the node label that is the variable name that the node represents
         */
        private String setLabel() {
            // Obtains the node variable name
            // If it's not a leaf, get the variable name associated
            if (!v.isLeaf()) {
                System.out.println(v.index+" "+bdd.variables.get(v.index - 1));
                System.out.flush();
                this.label = bdd.variables.get(v.index - 1);
                return this.label;
            }
            // If it's a leaf, get 1/0 from True/False, resp.
            if (v.value) {
                this.label = "1";
            }
            this.label = "0";
            return this.label;
        }
    }

    /**  Generates the graph */
    private void _create_graph(GraphViz graph, Node n, String pathName) {
        // Traverse all nodes and creates a dot graph"""
        //System.out.println(n);
        //System.out.println(n.v);
        if (n.v.index != -1) {
            this._create_graph(graph, n.low, pathName + "L");
            this._create_graph(graph, n.high, pathName + "H");
            if (!n.visited) {
                n.visited = true;
                n.setName(pathName);
                graph.addln("\""+n.name + "\" -> \"" + n.low.name + "\" [dir=\"forward\" arrowtype=\"normal\" style=\"dashed\"];");
                graph.addln("\""+n.name + "\" -> \"" + n.high.name + "\" [dir=\"forward\" arrowtype=\"normal\" style=\"normal\"];");
            }
        } else if (n.v.isLeaf()) {
                if (!n.visited) {
                    n.visited = true;
                    n.setName(pathName);
                    n.setLabel();
                }
            }
        }
    
    /**
     * Prints the BDD.
     */
    public void print(String path) {

        ArrayList<Node> nodes = new ArrayList<Node>();
       
        for (Vertex v : bdd.vertices) {
            Node n = new Node(v, bdd);
            nodes.add(n);
        }
        Node root = nodes.get(nodes.size()-1);

        for (int i = 0; i < bdd.vertices.size(); i++) {
            Vertex v = bdd.vertices.get(i);
            if (v.low != null) {
                nodes.get(i).low = nodes.get(v.low.id);
            }
            if (v.high != null) {
                nodes.get(i).high = nodes.get(v.high.id);
            }
        }
        
        for (Node n : nodes){
            if(n.low!=null && n.low!=null && n.low.v != n.v.low)
                System.out.println("El nodo "+n.v.id+" está petao");
            if(n.high!=null && n.high!=null && n.high.v != n.v.high)
                System.out.println("El nodo "+n.v.id+" está petao");
        }

        GraphViz gv = new GraphViz();
        //GraphViz gv = null;
        gv.addln(gv.start_graph());

        String pathName = "R";
        this._create_graph(gv, root, pathName);

        //gv.addln("A -> B;");
        //gv.addln("A -> C;");
        gv.addln(gv.end_graph());
        System.out.println(gv.getDotSource());
        
        String type = FILE_TYPE;
//      String type = "gif";
//      String type = "dot";
//      String type = "fig";    // open with xfig
//      String type = "pdf";
//      String type = "ps";
//      String type = "svg";    // open with inkscape
//      String type = "png";
//      String type = "plain";
        if(!path.contains("\\"+ FILE_TYPE))
            path += "."+FILE_TYPE;
        File out = new File(path);   // Linux
        gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type), out);
    }
    
    
    public BDDPrinter(BDD bdd){
        this.bdd = bdd;
    }
}
