package com.study.acs;

import java.io.BufferedReader;
import java.io.IOException;

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
	private static final long serialVersionUID = 1L;
       
    public AcsServlet() {
        super();
    }
    
    /* 
     * ���ĺ���������ACS�յ���GET/POST request
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	//����յ�������
    	BufferedReader br = request.getReader();
    	String s=br.readLine();  
    	while(null!=s){
    		System.out.println(s);
    		s=br.readLine();  
    	}
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
		processRequest(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

}
