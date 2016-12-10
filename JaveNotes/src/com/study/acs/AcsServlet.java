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
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AcsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    /* 
     * 核心函数，处理ACS收到的GET/POST request
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	//输出收到的内容
    	BufferedReader br = request.getReader();
    	String s=br.readLine();  
    	while(null!=s){
    		System.out.println(s);
    		s=br.readLine();  
    	}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		processRequest(request, response);
	}

}
