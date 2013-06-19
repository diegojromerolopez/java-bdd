/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdd;


/**
 *
 * @author diegoj
 */
abstract class LogicFunction {
    
    public String name;
    
    public abstract boolean run(boolean a, boolean b);  
    
    public abstract String getOp();
}

class And extends LogicFunction{
    String name = "AND";
    public final String SYMBOL = "&&";
    
    /**
     * @override 
     */
    @Override
    public boolean run(boolean a, boolean b){
        return a && b;
    }
    
    @Override
    public String getOp(){ return "&&"; }
}

class Or extends LogicFunction{
    String name = "OR";
    
    /**
     * @override 
     */
    @Override
    public boolean run(boolean a, boolean b){
        return a || b;
    }

    @Override
    public String getOp(){ return "||"; }
}

class Implication extends LogicFunction{
    String name = "Implies";
    
    /**
     * @override 
     */
    @Override
    public boolean run(boolean a, boolean b){
        return !a || b;
    }
    
    @Override
    public String getOp(){ return "=>"; }
}

class Equivalence extends LogicFunction{
    String name = "Equivalent";
    public String SYMBOL = "<=>";
    
    /**
     * @override 
     */
    @Override
    public boolean run(boolean a, boolean b){
        return (!a||b) && (!b||a);
    }

    @Override
    public String getOp(){ return "<=>"; }

}
