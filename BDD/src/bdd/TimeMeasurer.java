/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdd;

/**
 *
 * @author diegoj
 */
public class TimeMeasurer {
    public String context = "";
    public long startTime = 0;
    public  long endTime = 0;
    public  long elapsedTime = 0;
    
        /**
     * Converts the elapsed time to a human readable format.
     * @return String Time elapsed in human-friendly form.
     */
    protected String getElapsedTimeAsHumanText(){
        long ns = this.elapsedTime;
        long us = this.elapsedTime / 1_000;
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
    
    
    public TimeMeasurer(String context){
        this.context = context;
        this.startTime = System.nanoTime();
    }
    
    public long end(){
        this.endTime = System.nanoTime();
        this.elapsedTime = this.endTime - this.startTime;
        System.out.println(context+" "+this.getElapsedTimeAsHumanText());
        return this.elapsedTime;
    }
    
}
