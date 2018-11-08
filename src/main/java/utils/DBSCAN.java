/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.Tweet;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

/**
 *
 * @author andreadisst
 */
public class DBSCAN {
    
    private final static double EPS = 0.01;
    
    private final static int MIN_PTS = 0;
    
    /*public static void main(String[] args)  {
        
        List<Tweet> tweets = new ArrayList<>();
        
        tweets.add(new Tweet("1060484417116872705", 40.363, 22.5825));
        tweets.add(new Tweet("1060484417116872705", 40.363, 22.5825));
        tweets.add(new Tweet("1060484417116872705", 40.363, 22.5825));
        tweets.add(new Tweet("1060484417116872705", 40.363, 22.5825));
        tweets.add(new Tweet("1060484417116872705", 40.363, 22.5825));
        tweets.add(new Tweet("1060484450319036416", 40.3714,  22.5753));
        tweets.add(new Tweet("1060484485190504448", 40.6325,  22.9407));
        
        Map<Integer, List<String>> clusters = getClusters(tweets);
        
        System.out.println(clusters);

    }*/
    
    public static Map<Integer, List<String>> getClusters(List<Tweet> tweets){
        
        Map<Integer, List<String>> clusters = new HashMap<>();
        
        List<DoublePoint> points = new ArrayList<>();
        for(Tweet tweet : tweets){
            points.add(tweet.getPosition());
        }
        
        DBSCANClusterer<DoublePoint> clusterer = new DBSCANClusterer<DoublePoint>(EPS,MIN_PTS);
        
        List<Cluster<DoublePoint>> DBSCANClusters = clusterer.cluster(points);
        
        for(Tweet tweet : tweets){
            int clusterID = findCluster(tweet.getPosition(),DBSCANClusters);
            
            if(clusters.containsKey(clusterID)){
                List<String> tweetIDs = clusters.get(clusterID);
                tweetIDs.add(tweet.getId());
                clusters.put(clusterID, tweetIDs);
            }else{
                List<String> tweetIDs = new ArrayList<>();
                tweetIDs.add(tweet.getId());
                clusters.put(clusterID, tweetIDs);
            }
        }
        
        return clusters;
    }
    
    private static int findCluster(DoublePoint dp, List<Cluster<DoublePoint>> DBSCANClusters){
        for (int i = 0; i < DBSCANClusters.size(); i++) {
            for(DoublePoint p : DBSCANClusters.get(i).getPoints()) {
                if(p.getPoint()[0]==dp.getPoint()[0] && p.getPoint()[1]==dp.getPoint()[1]){
                    return i+1;
                }
            }
        }
        return 0;
    }
}
