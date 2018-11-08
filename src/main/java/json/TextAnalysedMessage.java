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
public class TextAnalysedMessage {
    
    Header header;
    TextAnalysedBody body;
    
    public TextAnalysedMessage(){
        
    }
    
    public TextAnalysedMessage(Header header, TextAnalysedBody body){
        this.header = header;
        this.body = body;
    }
    
    public TextAnalysedBody getBody(){
        return body;
    }
    
    public Header getHeader(){
        return header;
    }
    
}
