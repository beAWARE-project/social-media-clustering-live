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
public class SocialMediaTextMessage {
    
    Header header;
    SocialMediaTextBody body;
    
    public SocialMediaTextMessage(){
        
    }
    
    public SocialMediaTextMessage(Header header, SocialMediaTextBody body){
        this.header = header;
        this.body = body;
    }
    
    public SocialMediaTextBody getBody(){
        return body;
    }
    
    public Header getHeader(){
        return header;
    }
    
}
