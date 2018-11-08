/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package json;

/**
 *
 * @author andreadisst
 */
public class TextAnalysedBody {
    
    String incidentOriginator;
    String language;
    String incidentID;
    String startTimeUTC;
    /*Data data*/
    
    public TextAnalysedBody(){
        
    }
    
    public TextAnalysedBody(String incidentOriginator, String language, String incidentID, String startTimeUTC){
        this.incidentOriginator = incidentOriginator;
        this.language = language;
        this.incidentID = incidentID;
        this.startTimeUTC = startTimeUTC;
    }
    
    public String getIncidentID(){
        return this.incidentID;
    }
    
}
