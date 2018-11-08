/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import org.apache.commons.math3.ml.clustering.DoublePoint;

/**
 *
 * @author andreadisst
 */
public class Tweet {
    
    private String id;
    private DoublePoint dp;
    
    public Tweet(String id, double latitude, double longitude){
        this.id = id;
        double[] d = new double[2];
        d[0] = latitude;
        d[1] = longitude;
        dp = new DoublePoint(d);
    }
    
    public Tweet(String id, DoublePoint dp){
        this.id = id;
        this.dp = dp;
    }
    
    public String getId(){
        return id;
    }
    
    public DoublePoint getPosition(){
        return dp;
    }
    
}
