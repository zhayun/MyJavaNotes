package com.study.acs;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class AcsServlet
 */
@WebServlet("/acs")
public class AcsServlet extends HttpServlet {
       
    public AcsServlet() {
        super();
    }
 
    /* 
     * ���ĺ���������ACS�յ���GET/POST request
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response){

    	System.out.println("-------------------------------------------HTTP Header Information-------------------------------------------------------------------- ");
    	 Enumeration em =request.getHeaderNames();//ͨ��ö�����ͻ�ȡ�����ļ���ͷ����Ϣ��
    	 //����ͷ����Ϣ��
    	 while(em.hasMoreElements()){          
	    	//ȡ����Ϣ��                   
	    	 String name=(String)em.nextElement();
	    	//ȡ����Ϣֵ              
	    	 String value=request.getHeader(name);
	    	  System.out.println(name+"="+value);
    	 }
    	 
    	 System.out.println("-------------------------------------------HTTP SOAP Body Information-------------------------------------------------------------------- ");
     	BufferedReader br = null;
		try {
			br = request.getReader();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
     	String s = null;
		try {
			s = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		//����յ�������
     	while(null!=s){
     		System.out.println(s);
     		try {
				s=br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}  
     	}     
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		processRequest(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		processRequest(request, response);
	}
}
