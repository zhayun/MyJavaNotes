package com.study.acs;

import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;


public class InformResponse extends Message{
	
    public InformResponse() {
    	name = "InformResponse";
    }
	
    public InformResponse(String _id, int me) {
        name = "InformResponse";
        id = _id;
        MaxEnvelopes = me;
    }
    
    protected void createBody(SOAPBodyElement body, SOAPFactory spf) throws SOAPException {
    	body.addChildElement(spf.createName("MaxEnvelopes")).setValue(String.valueOf(MaxEnvelopes));
    }
    
    protected void parseBody(SOAPBodyElement body, SOAPFactory f) throws SOAPException{
    }
    
    public int MaxEnvelopes;
}
