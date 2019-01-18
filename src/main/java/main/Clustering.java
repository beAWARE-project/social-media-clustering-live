/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.Arrays;
import mykafka.BusReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import json.*;
import static main.TwitterReport.*;
import mykafka.Bus;
import static utils.DBSCAN.getClusters;

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
    
    public static void main(String[] args)  {
        
        KafkaConsumer<String, String> kafkaConsumer = busReader.getKafkaConsumer();
        kafkaConsumer.subscribe(Arrays.asList(Configuration.TOP001_SOCIAL_MEDIA_TEXT));
        
        long startTime = System.currentTimeMillis();
        ArrayList<Tweet> collected = new ArrayList<>();
        
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
                                TwitterReportMessage message = generateMessage(groupedTweets.get(0), "Vicenza", center.getLatitude(), center.getLongitude(), TwitterReportURL, "it-IT", "Twitter Report","");
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
                        Type type = new TypeToken<SocialMediaTextMessage>() {}.getType();
                        SocialMediaTextMessage message = gson.fromJson(message_str, type);

                        String incidentID = message.getBody().getIncidentID();
                        if(incidentID.contains("_")){
                            if(incidentID.split("_").length == 3){
                                String id = incidentID.split("_")[2];
                                Position position = message.getBody().getPosition();
                                if(position!=null){
                                    System.out.println(id + " " + position.getLatitude() + " " + position.getLongitude());
                                    if(collected.isEmpty()){
                                        startTime = System.currentTimeMillis();
                                    }
                                    collected.add(new Tweet(id, position.getLatitude(), position.getLongitude()));
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
    
}
