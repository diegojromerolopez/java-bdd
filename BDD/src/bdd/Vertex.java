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
public class Vertex {
    /** Position in vector vertices of BDD class */
    int id = -1;
    /** Variable that is associated with this vertex. Position in vector variables of BDD class  */
    int index = -1;
    /** Probability computator index */
    int i = -1;
    
    boolean value = false;
    boolean is_leaf = false;
    Vertex low = null;
    Vertex high = null;

    boolean visited = false;

    
    /**
     * Basic constructor.
     */
    Vertex(int index, boolean value, Vertex low, Vertex high)
    {
        this.index = index;
        this.value = value;
        this.is_leaf = this.index==-1 && low==null && high==null;
        if(this.is_leaf && (low!=null || high!=null))
        {
            System.out.println("Este vértice está petado");
        }
        this.low = low;
        this.high = high;
    }

    /**
     * Internal node constructor.
     */
    Vertex(int index, Vertex low, Vertex high)
    {
        this(index,false,low,high);
    }

    /**
     * Leaf constructor.
     */
    Vertex(boolean value)
    {
        this(-1,value,null,null);
    }
    
    /**
     * Informs if this vertex is a leaf. That is, has no children.
     * @return true if this node has no children, false if has children.
     */
    public boolean isLeaf(){
        return (this.is_leaf);
    }

    public boolean equals(Vertex other)
    {
       boolean thisIsLeaf = this.isLeaf();
       boolean otherIsLeaf = other.isLeaf();
        if (!thisIsLeaf && !otherIsLeaf)
            return this.index==other.index;
        else if(thisIsLeaf && otherIsLeaf)
            return this.value==other.value;
        return false;
    }
    
    /**
     * Convert this Vertex to string.
     */
    public String toString(){
        boolean thisIsLeaf = this.isLeaf();
        if (!thisIsLeaf){
            //System.out.println( this.value +" " + this.index );
            return "<Vertex "+Integer.toString(this.index)+">";
        }else if(thisIsLeaf){
            return "<Vertex "+(this.value?"T":"F")+">";
        }
        return "<Vertex is not leaf and has values. It is Wrong>";
    }

    /**
     * Convert this Vertex to string.
     */
    public String toString(ArrayList<String> variables){
        boolean thisIsLeaf = this.isLeaf();
        if (!thisIsLeaf)
            return "<Vertex var="+variables.get(this.index-1)+" index="+Integer.toString(this.index)+">";
        else if(thisIsLeaf){
            return "<Vertex "+(this.value?"1":"0")+">";
        }
        return "<Vertex is not leaf and has values. It is Wrong>";
    }
    
}
