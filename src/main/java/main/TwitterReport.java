/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import utils.CDR;
import utils.MongoAPI;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import json.*;
import static utils.DBSCAN.getClusters;

/**
 *
 * @author andreadisst
 */
public class TwitterReport {
        
    public static String generateReport(List<String> ids){
        try{
            MongoClient mongoClient = MongoAPI.connect();
            DB db = mongoClient.getDB("BeAware");
            DBCollection collection = db.getCollection("Consumer");
            
            ArrayList<TwitterReportLine> twitterReportLines = new ArrayList<>();
            for(String id : ids){
                BasicDBObject whereQuery = new BasicDBObject();
                whereQuery.put("id_str", id);
                DBObject post = collection.findOne(whereQuery);
                
                if(post!=null){
                    String postText;
                    if(post.containsField("extended_tweet")){
                        DBObject extended_tweet = (DBObject) post.get("extended_tweet");
                        postText = extended_tweet.get("full_text").toString();
                    }else{
                        postText = post.get("text").toString();
                    }

                    twitterReportLines.add(new TwitterReportLine(postText,post.get("created_at").toString(),id));
                }
                
            }
            
            mongoClient.close();
            
            if(!twitterReportLines.isEmpty()){
                String filename = "TwitterReport"+System.currentTimeMillis()+".html";
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8));
                writer.write("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Twitter Report</title></head><body>");
                for(TwitterReportLine twitterReportLine : twitterReportLines){
                    String line = "<div><p><b>"+twitterReportLine.getText()+"</b></p><p><i>Posted at</i> "+twitterReportLine.getDate()+"</p><p><a href=\"https://twitter.com/statuses/"+
                            twitterReportLine.getId()+"\">Show on Twitter</a></div><br>";
                    writer.write(line);
                }
                writer.write("</body></html>");
                writer.close();

                CDR.storeFile(filename, filename, "text/html");
                File file = new File(filename);
                file.delete();

                return Configuration.DATA_STORAGE_URL + filename;
            }else{
                return "";
            }
                  
        }catch(UnknownHostException | KeyManagementException | NoSuchAlgorithmException e){
            System.out.println("Error while generating a report: " + e);
        }catch (IOException e) {
            System.out.println("Error while generating a report: " + e);
        }
        return "";
    }
    
    public static TwitterReportMessage generateMessage(String id, String district, double position_x, double position_y, String attachmentURL, String language, String title, String description){
        LocalDateTime ldt = LocalDateTime.now();
        String now = ldt.withNano(0) + "Z";
        long epoch = ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
        
        Header header = new Header(Configuration.TOP003_SOCIAL_MEDIA_REPORT, 0, 1, "SMA", "sma-c-msg-"+epoch, now, "Actual", "Alert", "", "Public", district, "", 0, "", "");
        Position position = new Position(position_x, position_y);
        Attachment attachment = new Attachment(attachmentURL.split("/")[attachmentURL.split("/").length-1], "webpage", now, attachmentURL);
        List<Attachment> attachments = new ArrayList<>();
        attachments.add(attachment);
        TwitterReportBody body = new TwitterReportBody("SMA", "INC_SMA_C_"+id+"_"+epoch, language, "Other", /*"Twitter Report", */"undefined", "Unknown", "Observed","2020-01-01T12:00:00Z", title, now, description, position, attachments);
        TwitterReportMessage message = new TwitterReportMessage(header, body);
        
        return message;
    }
    
    public static Position getCenterPoint(List<Tweet> tweets, List<String> groupedTweets){
        double latitude = 0.0;
        double longitude = 0.0;
        int count = 0;
        for(Tweet tweet : tweets){
            if(groupedTweets.contains(tweet.getId())){
                latitude += tweet.getPosition().getPoint()[0];
                longitude += tweet.getPosition().getPoint()[1];
                count++;
            }
        }
        return new Position((double) Math.round(latitude/count * 10000) / 10000,(double) Math.round(longitude/count * 10000) / 10000);
    }
}
