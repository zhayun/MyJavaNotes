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
     * 核心函数，处理ACS收到的GET/POST request
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response){

    	System.out.println("-------------------------------------------HTTP Header Information-------------------------------------------------------------------- ");
    	 Enumeration em =request.getHeaderNames();//通过枚举类型获取请求文件的头部信息集
    	 //遍历头部信息集
    	 while(em.hasMoreElements()){          
	    	//取出信息名                   
	    	 String name=(String)em.nextElement();
	    	//取出信息值              
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
		
		//输出收到的内容
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
