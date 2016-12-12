package com.study.acs;

import com.study.acs.Message;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

public class Inform extends Message {

    public class Event implements Entry<String, String> {

        private String event;
        private String cmdKey;

        public Event(String event, String cmdKey) {
            this.event = event;
            this.cmdKey = cmdKey;
        }

        public String getKey() {
            return event;
        }

        public String getValue() {
            return cmdKey;
        }

        public String setValue(String value) {
            return cmdKey = value;
        }
    }

    /** Creates a new instance of Inform */
    public Inform() {
    }

    protected void createBody(SOAPBodyElement body, SOAPFactory spf) throws SOAPException {
    }

    protected void parseBody(SOAPBodyElement body, SOAPFactory spf) throws SOAPException {
        SOAPElement deviceid = getRequestChildElement(spf, body, "DeviceId");
        defns = deviceid.getNamespaceURI();

        oui = getRequestElement(spf, deviceid, "OUI");
        sn = getRequestElement(spf, deviceid, "SerialNumber");
        Manufacturer = getRequestElement(spf, deviceid, "Manufacturer");
        ProductClass = getRequestElement(spf, deviceid, "ProductClass");
        if (ProductClass == null) {
            ProductClass = "";
        }
        MaxEnvelopes = Integer.parseInt(getRequestElement(spf, body, "MaxEnvelopes"));
        RetryCount = Integer.parseInt(getRequestElement(spf, body, "RetryCount"));
        CurrentTime = getRequestElement(spf, body, "CurrentTime");

        Iterator pi = getRequestChildElement(spf, body, "ParameterList").getChildElements(spf.createName("ParameterValueStruct"));
        /*
        //System.out.println ("pi.hasNext: "+pi.hasNext());
        Iterator pii = getRequestChildElement (spf, body, "ParameterList").getChildElements(new QName("ParameterValueStruct"));
        //System.out.println ("pii.hasNext: "+pii.hasNext());
        pii = getRequestChildElement (spf, body, "ParameterList").getChildElements(spf.createName ("ParameterValueStruct","cwmp",defns));
        System.out.println ("pii.hasNext: "+pii.hasNext());
        pii = getRequestChildElement (spf, body, "ParameterList").getChildElements(spf.createName ("ParameterValueStruct","cwmp",URN_CWMP));
        System.out.println ("pii.hasNext: "+pii.hasNext());
         */
        Name nameKey = spf.createName("Name");
        Name nameValue = spf.createName("Value");
        params = new Hashtable<String, String>();
        while (pi.hasNext()) {
            SOAPElement param = (SOAPElement) pi.next();
            String key = getRequestElement(param, nameKey);
            if (root == null && !key.startsWith(".")) {
                if (key.startsWith("Device.")) {
                    root = "Device";
                } else if (key.startsWith("InternetGatewayDevice.")) {
                    root = "InternetGatewayDevice";
                } else {
                    throw new RuntimeException("Invalid root. Must be InternetGatewayDevice or Device: " + key);
                }
            }
            String value = "";
            try {
                value = getRequestElement(param, nameValue);
            } catch (Exception e) {
            }
            if (value == null) {
                value = "";
            }
            params.put(key, value);
        }

        if (root == null) {
            throw new RuntimeException("Invalid root. Must be InternetGatewayDevice or Device");
        }

        pi = getRequestChildElement(spf, body, "Event").getChildElements(spf.createName("EventStruct"));
        Name eventCode = spf.createName("EventCode");
        Name commandKey = spf.createName(COMMAND_KEY);
        events = new LinkedHashSet<Entry<String, String>>();
        while (pi.hasNext()) {
            SOAPElement param = (SOAPElement) pi.next();
            String event = getRequestElement(param, eventCode);
            String cmdKey = getRequestElement(param, commandKey);
            System.out.println("EVENT: " + event + "[" + cmdKey + "]");
            if (cmdKey == null) {
                cmdKey = "";
            }
            events.add(new Event(event, cmdKey));
        }

    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(1024);
        s.append("Inform:\n");
        println(s, "\toui: ", oui);
        println(s, "\tsn: ", sn);
        println(s, "\tManufacturer: ", Manufacturer);

        s.append("\tEvents:\n");
        for (Entry<String, String> ev : events) {
            println(s, "\t\t", ev.getKey(), ev.getValue());
        }

        s.append("\tParams:\n");
        for (String k : params.keySet()) {
            println(s, "\t\t", k, params.get(k));
        }
        return s.toString();
    }

    private String oui;
    public String sn;
    public String ProductClass;
    public String Manufacturer;
    public int RetryCount;
    public String CurrentTime;
    public Hashtable<String, String> params;
    private Set<Entry<String, String>> events;
    public int MaxEnvelopes;
    public String defns;
    private String root = null;
}
