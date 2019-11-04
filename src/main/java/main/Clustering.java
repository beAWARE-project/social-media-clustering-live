/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.io.IOException;
import java.util.Arrays;
import mykafka.BusReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import json.*;
import static main.TwitterReport.*;
import mykafka.Bus;
import static utils.DBSCAN.getClusters;
import org.json.*;
import utils.MongoAPI;

/**
 *
 * @author andreadisst
 */
public class Clustering {
    
    private static Bus bus = new Bus();
    private static BusReader busReader = new BusReader();
    private static Gson gson = new Gson();
    
    private static final int TIME_LIMIT = 60000;
    private static final int NUMBER_LIMIT = 10;
    private static final boolean USE_MTA = true;
    
    public static void main(String[] args)  {
        
        
        KafkaConsumer<String, String> kafkaConsumer = busReader.getKafkaConsumer();
        if(USE_MTA){
            kafkaConsumer.subscribe(Arrays.asList(Configuration.TOP028_TEXT_ANALYSED));
        }else{
            kafkaConsumer.subscribe(Arrays.asList(Configuration.TOP001_SOCIAL_MEDIA_TEXT));
        }
        
        long startTime = System.currentTimeMillis();
        ArrayList<Tweet> collected = new ArrayList<>();
        String district = "", language = "";
        
        try {
            while (true) {
                if( ( (System.currentTimeMillis() - startTime >= TIME_LIMIT) && (collected.size() > 0) ) || (collected.size() >= NUMBER_LIMIT)){
                    Map<Integer, List<String>> clusters = getClusters(collected);
                    for (Map.Entry<Integer, List<String>> entry : clusters.entrySet()){
                        List<String> groupedTweets = entry.getValue();
                        if(groupedTweets.size() > 0){
                            String TwitterReportURL = generateReport(groupedTweets);
                            if(!TwitterReportURL.equals("")){
                                Position center = getCenterPoint(collected,groupedTweets);
                                TwitterReportMessage message = generateMessage(groupedTweets.get(0), district, center.getLatitude(), center.getLongitude(), TwitterReportURL, language, "Twitter Report","");
                                String messageJSON = gson.toJson(message);
                                try{
                                    bus.post(Configuration.TOP003_SOCIAL_MEDIA_REPORT, messageJSON);
                                }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
                                    System.out.println("Error on send: " + e);
                                }
                            }
                        }

                    }
                    startTime = System.currentTimeMillis();
                    collected = new ArrayList<>();
                }else{
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
                    for (ConsumerRecord<String, String> record : records)
                    {
                        String message_str = record.value();
                        if(USE_MTA){
                            JSONObject obj = new JSONObject(message_str);
                            if(obj.getJSONObject("body").getString("incidentOriginator").equals("SMA")){
                                String incidentID = obj.getJSONObject("body").getString("incidentID");
                                if(incidentID.contains("_")){
                                    if(incidentID.split("_").length == 3){
                                        String id = incidentID.split("_")[2];
                                        ArrayList<Position> positions = getPositionFromJSON(message_str);
                                        if(positions.isEmpty()){
                                            if(obj.getJSONObject("body").has("position")){
                                                if(collected.isEmpty()){
                                                    startTime = System.currentTimeMillis();
                                                }
                                                collected.add(new Tweet(id, obj.getJSONObject("body").getJSONObject("position").getDouble("latitude"), obj.getJSONObject("body").getJSONObject("position").getDouble("longitude")));
                                                district = obj.getJSONObject("header").getString("district");
                                                language = obj.getJSONObject("body").getString("language");
                                                storeLocation(id, obj.getJSONObject("body").getJSONObject("position").getDouble("latitude"),obj.getJSONObject("body").getJSONObject("position").getDouble("longitude"));
                                            }
                                        }else{
                                            for(Position position : positions){
                                                if(collected.isEmpty()){
                                                    startTime = System.currentTimeMillis();
                                                }
                                                collected.add(new Tweet(id, position.getLatitude(), position.getLongitude()));
                                                district = obj.getJSONObject("header").getString("district");
                                                language = obj.getJSONObject("body").getString("language");
                                                storeLocation(id, position.getLatitude(),position.getLongitude());
                                            }
                                        }
                                    }
                                }
                            }
                        }else{
                            Type type = new TypeToken<SocialMediaTextMessage>() {}.getType();
                            SocialMediaTextMessage message = gson.fromJson(message_str, type);
                            String incidentID = message.getBody().getIncidentID();
                            if(incidentID.contains("_")){
                                if(incidentID.split("_").length == 3){
                                    String id = incidentID.split("_")[2];
                                    Position position = message.getBody().getPosition();
                                    if(position!=null){
                                        if(collected.isEmpty()){
                                            startTime = System.currentTimeMillis();
                                        }
                                        collected.add(new Tweet(id, position.getLatitude(), position.getLongitude()));
                                        district = message.getHeader().getDistrict();
                                        language = message.getBody().getLanguage();
                                        storeLocation(id, position.getLatitude(),position.getLongitude());
                                    }
                                }
                            }
                        }
                            
                    }
                }
            }
        } finally {
          kafkaConsumer.close(); 
        }
        
    }
    
    private static ArrayList<Position> getPositionFromJSON(String message_str){
                
        ArrayList<Position> positions = new ArrayList<>();
        
        try{
            JSONObject obj = new JSONObject(message_str);

            JSONObject data = obj.getJSONObject("body").getJSONObject("data");
            JSONArray concepts = data.toJSONArray(data.names());
            for (int i = 0; i < concepts.length(); i++){
                if(!concepts.getJSONObject(i).isNull("location")){
                    double latitude = concepts.getJSONObject(i).getJSONObject("location").getDouble("latitude");
                    double longitude = concepts.getJSONObject(i).getJSONObject("location").getDouble("longitude");
                    positions.add(new Position(latitude,longitude));
                }
            }
        }catch(JSONException ex){
            return positions;
        }
        
        return positions;
        
    }
    
    private static void storeLocation(String id, Double latitude, Double longitude){
        try{
            MongoClient mongoClient = MongoAPI.connect();
            DB db = mongoClient.getDB("BeAware");
            DBCollection collection = db.getCollection("Consumer");

            Double[] coordinates = {latitude,longitude};
            BasicDBObject newDocument = new BasicDBObject().append("$set", new BasicDBObject().append("coordinates", new BasicDBObject().append("type", "Point").append("coordinates",coordinates)));
            BasicDBObject searchQuery = new BasicDBObject().append("id_str", id);
            collection.updateMulti(searchQuery, newDocument);

            mongoClient.close();
        }catch(UnknownHostException | KeyManagementException | NoSuchAlgorithmException e){
            System.out.println("Error while storing a location: " + e);
        }
    }
}
