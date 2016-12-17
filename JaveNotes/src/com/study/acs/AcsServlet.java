package com.study.acs;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import com.study.acs.Message;

import com.study.acs.InformResponse;




//import src.java.org.openacs.HttpSession;



/**
 * Servlet implementation class AcsServlet
 */
@WebServlet("/acs")
public class AcsServlet extends HttpServlet {
    protected static final String ATTR_LASTINFORM = "lastInform";
 //   private static final String ATTR_CONFIGURATOR = "cfgrun";
       
    public AcsServlet() {
        super();
    }
 
 
    private class xmlFilterInputStream extends InputStream {

        /** Creates a new instance of xmlFilterInputStream */
        public xmlFilterInputStream(InputStream is, int l) {
            //      System.out.println("Stream length is "+l);
            len = l;
            istream = is;
        }

        public int read() throws IOException {
            if (lastchar == '>' && lvl == 0) {
                //        System.err.println ("return EOF");
                return -1;
            }
            int l = lastchar;
            if (nextchar != -1) {
                lastchar = nextchar;
                nextchar = -1;
            } else {
                if (buff.length() > 0) {
                    //                  System.out.println("buff len="+buff.length());
                    lastchar = buff.charAt(0);
                    buff.deleteCharAt(0);
                    return lastchar;
                } else {
                    lastchar = istream.read();
                }
            }
            if (lastchar == '<') {
                intag = true;
            } else if (lastchar == '>') {
                intag = false;
            }

            if (!intag && lastchar == '&') {
                int amppos = buff.length();
                // fix up broken xml not encoding &
                buff.append((char) lastchar);
//                System.out.println("Appended buff len="+buff.length());
                for (int c = 0; c < 10; c++) {
                    int ch = istream.read();
                    if (ch == -1) {
                        break;
                    }
                    if (ch == '&') {
                        nextchar = ch;
                        break;
                    }
                    buff.append((char) ch);
//                System.out.println("Appended buff len="+buff.length());
                }
//                System.out.println ("xmlFilterInputStream: buff="+buff.substring(0, buff.length()));
                String s = buff.substring(amppos);
                if (!s.startsWith("&amp;") && !s.startsWith("&lt;") && !s.startsWith("&gt;") && !s.startsWith("&apos;") && !s.startsWith("&quot;") && !s.startsWith("&#")) {
                    buff.replace(amppos, amppos + 1, "&amp;");
                }
                return read();
            }

            if (l == '<') {
                intag = true;
                if (lastchar == '/') {
                    lvl--;
                } else {
                    lvl++;
                }
            }
            //           System.err.println ("return char="+(char)lastchar+" lvl="+lvl);
            //System.err.print ((char)lastchar);
            len--;
            return lastchar;
        }
        private InputStream istream;
        private int lvl;
        private int lastchar;
        private int len;
        private int nextchar;
        private boolean intag = false;
        private StringBuffer buff = new StringBuffer(16);

        public boolean next() throws IOException {
            while ((nextchar = istream.read()) != -1) {
                if (!Character.isWhitespace(nextchar)) {
                    break;
                }
            }
            //        System.out.println ("Next char is "+nextchar);
            lvl = 0;
            lastchar = 0;
            return (nextchar != -1);
        }
    }
 
    private class charsetConverterInputStream extends InputStream {

        private InputStream in;
        private PipedInputStream pipein;
        private OutputStream pipeout;
        private Reader r;
        private Writer w;

        public charsetConverterInputStream(String csFrom, String csTo, InputStream in) throws UnsupportedEncodingException, IOException {
            this.in = in;
            r = new InputStreamReader(in, csFrom);
            pipein = new PipedInputStream();
            pipeout = new PipedOutputStream(pipein);
            w = new OutputStreamWriter(pipeout, csTo);
        }

        @Override
        public int read() throws IOException {
            if (pipein.available() > 0) {
                return pipein.read();
            }
            int c = r.read();
            if (c == -1) {
                return -1;
            }
            w.write(c);
            w.flush();
            return pipein.read();
        }
    }
    
    private class xmlFilterNS extends InputStream {
        // Dumb class to filter out declaration of default xmlns

        private String pat = "xmlns=\"urn:dslforum-org:cwmp-1-0\"";
        private String pat2 = "xmlns=\"urn:dslforum-org:cwmp-1-1\"";
        private int length = 0;
        private int pos = 0;
        private boolean f = false;
        private byte buff[] = new byte[1024];
        private InputStream is;

        @Override
        public int read() throws IOException {
            if (!f) {
                length = is.read(buff);
                if (length < buff.length) {
                    byte[] b2 = new byte[length];
                    System.arraycopy(buff, 0, b2, 0, length);
                    buff = b2;
                }

                String b = new String(buff);
                b = b.replace(pat, "");
                b = b.replace(pat2, "");
                buff = b.getBytes();
                length = buff.length;
                f = true;
            }

            if (pos < length) {
                return buff[pos++];
            }
            return is.read();
        }

        public xmlFilterNS(InputStream is) {
            this.is = is;
        }
    }
    /* 
     * 核心函数，处理ACS收到的GET/POST request
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, SOAPException {

    	System.out.println("-------------------------------------------HTTP Header Information-------------------------------------------------------------------- ");
    	SOAPMessage soapMsg = null;
    	xmlFilterInputStream f = new xmlFilterInputStream(request.getInputStream(), request.getContentLength());
    	MessageFactory mf;
    	//ByteArrayOutputStream out = new ByteArrayOutputStream();
       

    	
    	try {
			mf = MessageFactory.newInstance();
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			f.close();
			throw new ServletException();
		}
    
    	 Enumeration em =request.getHeaderNames();//通过枚举类型获取请求文件的头部信息集
    	 //遍历头部信息集
    	 while(em.hasMoreElements()){          
	    	//取出信息名                   
	    	 String name=(String)em.nextElement();
	    	//取出信息值              
	    	 String value=request.getHeader(name);
	    	  System.out.println(name+"="+value);
    	 }
    	    
    	String ct = request.getContentType();
    	int csix = (ct != null) ? ct.indexOf("charset=") : -1;
    	System.out.println("csix = "+csix);
    	String csFrom = (csix == -1) ? "ISO-8859-1" : ct.substring(csix + 8).replaceAll("\"", "");
    	System.out.println("csFrom = "+csFrom);
    	HttpSession session = request.getSession();
    	Inform lastInform = (Inform) session.getAttribute(ATTR_LASTINFORM);
    	response.setContentType(ct != null ? ct : "text/xml;charset=UTF-8");
    	 
        while (f.next()) {
        	MimeHeaders hdrs = new MimeHeaders();
        	hdrs.setHeader("Content-Type", "text/xml; charset=UTF-8");
        	 InputStream in = (csFrom.equalsIgnoreCase("UTF-8")) ? new xmlFilterNS(f) : new charsetConverterInputStream(csFrom, "UTF-8", new xmlFilterNS(f));
        	 soapMsg = mf.createMessage(hdrs, in);
        	 
        	 Message msg = null;
             try {
                 msg = Message.Parse(soapMsg);
             } catch (Exception e) {
                 e.printStackTrace();
                 return;
             }
             
             String reqname = msg.getName();
             if (reqname.equals("Inform")) {//如果该消息是inform报文，则输出报文的内容
            	 lastInform = (Inform) msg;
            	 System.out.println("-------------------------------------------CPE Information-------------------------------------------------------------------- ");
            	 System.out.println(lastInform.toString());
            	 
            	 InformResponse resp = new InformResponse(lastInform.getId(), MY_MAX_ENVELOPES);
            	 
            	 response.setStatus(HttpServletResponse.SC_OK);
            	 resp.writeTo(response.getOutputStream());
             }
        }
        
        f.close();     
    	//输出收到的内容
//    	BufferedReader br = request.getReader();
//    	String s=br.readLine();  
//    	while(null!=s){
//    		System.out.println(s);
//    		s=br.readLine();  
//    	}
     
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
		try {
			processRequest(request, response);
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	}
	private final int MY_MAX_ENVELOPES = 1;
}
