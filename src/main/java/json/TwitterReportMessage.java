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
public class TwitterReportMessage {
    
    Header header;
    TwitterReportBody body;
    
    public TwitterReportMessage(){
        
    }
    
    public TwitterReportMessage(Header header, TwitterReportBody body){
        this.header = header;
        this.body = body;
    }
    
    public TwitterReportBody getBody(){
        return body;
    }
    
    public Header getHeader(){
        return header;
    }
    
}
