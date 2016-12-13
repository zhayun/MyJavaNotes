package com.study.acs;

import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;


public class InformResponse extends Message{
	
    public InformResponse() {
    	name = "InformResponse";
    }
	
    protected void createBody(SOAPBodyElement body, SOAPFactory spf) throws SOAPException {
    }
    
    protected void parseBody(SOAPBodyElement body, SOAPFactory f) throws SOAPException{
    }
    
}
