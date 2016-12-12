package com.study.acs;

import java.io.*;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.soap.*;


abstract public class Message implements Serializable {

    /** Creates a new instance of Message */
    public Message() {
        //id = "intrnl.unset.id."+((name!=null) ? name : "") +(Calendar.getInstance().getTimeInMillis()+3600*1000);
    }

    abstract protected void createBody(SOAPBodyElement body, SOAPFactory spf) throws SOAPException;

    abstract protected void parseBody(SOAPBodyElement body, SOAPFactory f) throws SOAPException;

    static public SOAPBodyElement getRequest(SOAPMessage msg) throws SOAPException {
        SOAPBodyElement request = null;
        Iterator i1 = msg.getSOAPBody().getChildElements();
        while (i1.hasNext()) {
            Node n = (Node) i1.next();
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                request = (SOAPBodyElement) n;
            }
        }
        return request;
    }

    static private String getRequestName(SOAPMessage msg) throws SOAPException {
        if (msg.getSOAPBody().hasFault()) {
            return "Fault";
        }
        String name = getRequest(msg).getNodeName();
        if (name.startsWith("cwmp:")) {
            name = name.substring(5);
        } else if (name.startsWith("cwmp_x:")) {
            name = name.substring(7);
        }
        return name;
    }

    static public Message Parse(SOAPMessage soapMsg) throws SOAPException, ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        String reqname = Message.getRequestName(soapMsg);

        Message msg = null;
        msg = (Message) Class.forName("com.study.acs." + reqname).newInstance();
        msg = msg.parse(soapMsg);
        return msg;
    }

    private Message parse(SOAPMessage soapMsg) throws SOAPException, Exception {
        SOAPEnvelope env = soapMsg.getSOAPPart().getEnvelope();
       // System.out.println("URI " + env.getNamespaceURI(""));

        Iterator<String> pfxs = (Iterator<String>) env.getNamespacePrefixes();
        while (pfxs.hasNext()) {
            String pfx = pfxs.next();
            String uri = env.getNamespaceURI(pfx);
            if (uri.startsWith("urn:dslforum-org:cwmp-")) {
                URN_CWMP = uri;
             //   System.out.println("cwmp NS =" + uri);
            }
        }
        SOAPFactory spf = SOAPFactory.newInstance();
        SOAPBodyElement soaprequest = getRequest(soapMsg);
        SOAPHeader hdr = soapMsg.getSOAPHeader();
        id = "device_did_not_send_id"; // or make it null?...
        noMore = false;
        if (hdr != null) {
            try {
                id = getHeaderElement(spf, hdr, "ID");
            } catch (NoSuchElementException e) {
            }
            try {
                noMore = getHeaderElement(spf, hdr, "NoMoreRequests").equals("1");
            } catch (NoSuchElementException e) {
            }
        }
        name = getRequestName(soapMsg);
        if (soaprequest != null) {
            try {
                parseBody(soaprequest, spf);
            } catch (Exception e) {
                SOAPElement se = getRequestChildElement(spf, soaprequest, "FaultCode");
                String FaultCode = (se != null) ? se.getValue() : "0";
                SOAPElement se2 = getRequestChildElement(spf, soaprequest, "FaultString");
                String FaultString = (se2 != null) ? se2.getValue() : "0";

                if (se != null || se2 != null) {
                   // return new Fault(FaultCode, FaultString, id);
                }
                throw e;
            }
        }
        return this;
    }


    protected SOAPElement getRequestChildElement(SOAPFactory f, SOAPElement req, String name) throws SOAPException {
        Iterator i = req.getChildElements();
        while (i.hasNext()) {
            Object o = i.next();
            try {
                Node nn = (Node) o;
//                if (nn.getNodeName().equals(name)) {
                String n = nn.getLocalName();
                if (n != null && n.equals(name)) {
                    return (SOAPElement) o;
                }
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage() + " " + e.getClass().getName());
            }
        }
        return null;
    }


    protected String getRequestElement(SOAPFactory f, SOAPElement req, String name) throws SOAPException {
        return getRequestChildElement(f, req, name).getValue();
    }



    protected SOAPElement getRequestChildElement(SOAPElement req, Name name) throws SOAPException {
        return (SOAPElement) req.getChildElements(name).next();
    }

    protected String getRequestElement(SOAPElement req, Name name) throws SOAPException {
        return getRequestChildElement(req, name).getValue();
    }

    protected String getHeaderElement(SOAPFactory f, SOAPHeader hdr, String name) throws SOAPException {
        return ((SOAPHeaderElement) hdr.getChildElements(f.createName(name, CWMP, URN_CWMP)).next()).getValue();
    }



    protected String name;

    public String getName() {
        return name;
    }
    protected String id;

    public String getId() {
        if (id == null) {
            id = "ID:intrnl.unset.id." + ((name != null) ? name : "") + (Calendar.getInstance().getTimeInMillis() + 3600 * 1000) + "." + hashCode();
        } /*else {
        if (!id.startsWith("ID:")) {
        id = "ID:"+id;
        }
        }*/
        return id;
    }

    protected void println(StringBuilder b, String n, String v) {
        b.append(n);
        b.append(": ");
        b.append(v);
        b.append("\n");
    }

    protected void println(StringBuilder b, String n, String n2, String v) {
        b.append(n);
        println(b, n2, v);
    }
    public boolean noMore;
    protected String URN_CWMP = "urn:dslforum-org:cwmp-1-0";
    protected static final String CWMP = "cwmp";
    protected static final String PARAMETER_KEY = "ParameterKey";
    protected static final String COMMAND_KEY = "CommandKey";
    protected static final String XSI_TYPE = "xsi:type";
    protected static final String XSD_STRING = "xsd:string";
    protected static final String XSD_UNSIGNEDINT = "xsd:unsignedInt";
    protected static final String XSD_INT = "xsd:int";
    protected static final String XSD_BOOLEAN = "xsd:boolean";
    protected static final String XSD_DATETIME = "xsd:dateTime";
    protected static final String XSD_BASE64 = "xsd:base64";
    protected static final String SOAP_ARRAY_TYPE = "SOAP-ENC:arrayType";
    public static final String FAULT_CODE = "FaultCode";
    public static final String FAULT_STRING = "FaultString";
    static public final String TYPE_OBJECT = "object";
    static public final String TYPE_STRING = "string";
    static public final String TYPE_BOOLEAN = "boolean";
    static public final String TYPE_DATETIME = "dateTime";
    static public final String TYPE_UNSIGNEDINT = "unsignedInt";
    static public final String TYPE_INT = "int";
    static public final String TYPE_BASE64 = "base64";
}
