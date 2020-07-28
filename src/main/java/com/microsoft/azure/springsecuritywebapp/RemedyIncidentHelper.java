package com.microsoft.azure.springsecuritywebapp;

import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class RemedyIncidentHelper {
	
	
	@Autowired
	BMCRestTestController bMCRestTestController;
	
	/** process Folder **/
	@Value("${remedy.username}")
	private String userName;
	
	
	/** process Folder **/
	@Value("${remedy.password}")
	private String  password;
	
	 String updateIncident(String msg,String incidentNumber)
	{
		 	String entryUrl="";
		 	String baseURL ="http://vtrvitstp-03:8008"; //sc.next();
		
			String token= bMCRestTestController.login(baseURL, userName, password);
			
			System.out.println("*************** Getting JWT Login *****************");
			System.out.println("Login result :"+token);
			System.out.println();
			
			try {
			entryUrl=bMCRestTestController.getEntry(token, incidentNumber);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("*************** Updating  Incident "+incidentNumber+" *****************");
			bMCRestTestController.updateIncident(token,msg,incidentNumber,entryUrl);
		    
		    
		    return "updateInc";
		    
		    
	}
	 
	 String createIncident(String first_name,
			 String last_name,
			 String incidentDescription,
			String templateId,String serviceType,String loginId)
		{
	 	String entryUrl="";
	 	String baseURL ="http://vtrvitstp-03:8008"; //sc.next();
	
		String token= bMCRestTestController.login(baseURL, userName, password);
		String requestId=bMCRestTestController.createIncident(token, first_name, last_name, incidentDescription, loginId, serviceType, templateId);
		String incidentNumber=bMCRestTestController.getMoreIncident(requestId, token);
		System.out.println("*************** Getting JWT Login *****************");
		System.out.println("Login result :"+token);
		System.out.println();
		
		return incidentNumber;
		
		}

}
