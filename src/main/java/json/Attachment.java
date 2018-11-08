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
public class Attachment {
    
    String attachmentName;
    String attachmentType;
    String attachmentTimeStampUTC;
    String attachmentURL;
    
    public Attachment(){
        
    }
    
    public Attachment(String attachmentName, String attachmentType,
                        String attachmentTimeStampUTC, String attachmentURL){
        this.attachmentName = attachmentName;
        this.attachmentType = attachmentType;
        this.attachmentTimeStampUTC = attachmentTimeStampUTC;
        this.attachmentURL = attachmentURL;
    }
    
    public String getAttachmentType(){
        return attachmentType;
    }
    
    public String getAttachmentTimeStampUTC(){
        return attachmentTimeStampUTC;
    }
    
    public String getAttachmentURL(){
        return attachmentURL;
    }
    
}
