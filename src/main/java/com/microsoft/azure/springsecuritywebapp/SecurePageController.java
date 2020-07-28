// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.springsecuritywebapp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class SecurePageController {

	@Autowired
	RemedyIncidentHelper remedyIncidentHelper;

	@Autowired
	HttpClientHelper httpClientHelper;

	@RequestMapping(path = "/secure_page") // NOSONAR
	public ModelAndView updateIncident(@RequestParam(value = "indicidentNumber", required = false) String indicidentNumber) { // NOSONAR
		String message = "";

		try {
			message = updateIncidentDetails(indicidentNumber);
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	String successMessage = remedyIncidentHelper.updateIncident(message, indicidentNumber);
		return new ModelAndView(message);
	}

	@GetMapping("/welcome/") // NOSONAR
	public ModelAndView indexPage() { // NOSONAR
		ModelAndView mav = new ModelAndView("index");

		return mav;
	}

	@RequestMapping(path = "/getChatDetails") // NOSONAR
	public String getChatDetailsPage(@RequestParam(value = "code", required = true) String code, Model model) // NOSONAR
			throws JSONException, IOException {

		ResponseEntity<String> response = null;

		System.out.println("Authorization Ccode------" + code);

		String redirectUrl = "https://graph.microsoft.com/v1.0/me";

		String authtoken = httpClientHelper.getAccessToken(OAuthConstants.GRANT_TYPE_AUTHORIZATION_CODE, code);

		String baseurl = "https://graph.microsoft.com/v1.0/me/joinedTeams";

		URL url = new URL(baseurl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestProperty("Authorization", "Bearer " + authtoken);
		conn.setRequestProperty("Accept", "application/json");

		String response1 = httpClientHelper.getResponseStringFromConn(conn);

		int responseCode = conn.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			throw new IOException(response1);
		}

		JSONObject teamResponseObject = HttpClientHelper.processResponse(responseCode, response1);

		String teamId = "";
		JSONObject teamJsonObject1 = teamResponseObject.getJSONObject("responseMsg");

		if (teamJsonObject1.has("value")) {
			JSONArray teamjsonArray = teamJsonObject1.getJSONArray("value");

			for (int i = 0, size = teamjsonArray.length(); i < size; i++) {
				JSONObject teamObjectInArray = teamjsonArray.getJSONObject(i);

				String[] elementNames = JSONObject.getNames(teamObjectInArray);

				for (String elementName : elementNames) {

					if (elementName.equals("displayName") && (teamObjectInArray.get(elementName).equals("CPA_POC"))) {

						teamId = teamObjectInArray.getString("id");

						break;
					}

				}

			}

		}

		String clienttoken = httpClientHelper.getAccessForToken(OAuthConstants.GRANT_TYPE_CLIENT_CREDENTIALS, code);
		String channelUrl = "https://graph.microsoft.com/v1.0/teams/" + teamId + "/channels";

		URL channelurl = new URL(channelUrl);
		HttpURLConnection conn1 = (HttpURLConnection) channelurl.openConnection();

		// Set the appropriate header fields in the request header.
		conn1.setRequestProperty("Authorization", "Bearer " + clienttoken);
		conn1.setRequestProperty("Accept", "application/json");

		String channelResponse = httpClientHelper.getResponseStringFromConn(conn1);

		int channelResponseCode = conn1.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			throw new IOException(channelResponse);
		}

		JSONObject channelresponseObject = HttpClientHelper.processResponse(channelResponseCode, channelResponse);

		String channelId = "";

		JSONObject jsonObject2 = channelresponseObject.getJSONObject("responseMsg");

		if (jsonObject2.has("value")) {

			JSONArray channelArray1 = jsonObject2.getJSONArray("value");
			for (int i = 0, size = channelArray1.length(); i < size; i++) {
				JSONObject channelObjectInArray = channelArray1.getJSONObject(i);

				String[] elementNames = JSONObject.getNames(channelObjectInArray);

				for (String elementName : elementNames) {

					if (elementName.equals("displayName")
							&& (channelObjectInArray.get(elementName).equals("Incident_Query"))) {

						channelId = channelObjectInArray.getString("id");

						break;
					}

				}

			}
		}

		String msgUrl = "https://graph.microsoft.com/beta/teams/" + teamId + "/channels/" + channelId + "/messages";

		URL msgUri = new URL(msgUrl);

		HttpURLConnection conn2 = (HttpURLConnection) msgUri.openConnection();

		// Set the appropriate header fields in the request header.

		// String authtoken2= HttpClientHelper.getAuth(code, redirectUrl);

		conn2.setRequestProperty("Authorization", "Bearer " + authtoken);
		conn2.setRequestProperty("Accept", "application/json");

		String msgResponse = httpClientHelper.getResponseStringFromConn(conn2);

		int msResponseCode = conn2.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			throw new IOException(msgResponse);
		}

		JSONObject msgresponseObject = HttpClientHelper.processResponse(msResponseCode, msgResponse);

		return remedyIncidentHelper.updateIncident(JsonUtil.getMessages(msgresponseObject.toString()), "");

	}

	public String updateIncidentDetails(String incidentNumber) throws JSONException, IOException {

		String clienttoken = httpClientHelper
				.getAccessTokenForClientCredential(OAuthConstants.GRANT_TYPE_CLIENT_CREDENTIALS, "");

		String baseurl = "https://graph.microsoft.com/v1.0/users";

		URL url = new URL(baseurl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		// Set the appropriate header fields in the request header.
		conn.setRequestProperty("Authorization", "Bearer " + clienttoken);
		conn.setRequestProperty("Accept", "application/json");

		String response1 = httpClientHelper.getResponseStringFromConn(conn);

		int responseCode = conn.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			throw new IOException(response1);
		}

		JSONObject responseObject = httpClientHelper.processResponse(responseCode, response1);

		// JSONObject valueTeamObject =responseObject.getJSONObject("value");

		JSONObject jsonObject1 = responseObject.getJSONObject("responseMsg");

		String teamId = "";

		String userId = "";

		if (jsonObject1.has("value")) {
			JSONArray jsonArray = jsonObject1.getJSONArray("value");

			for (int i = 0, size = jsonArray.length(); i < size; i++) {
				JSONObject objectInArray = jsonArray.getJSONObject(i);

				String[] elementNames = JSONObject.getNames(objectInArray);

				for (String elementName : elementNames) {
					if (elementName.equals("mail")
							&& (objectInArray.getString(elementName).equals("rasmiawsact02@gmail.com"))) {
						userId = objectInArray.getString("id");

						System.out.println(userId);
						break;
					}
				}

			}

		}

		String teamUrl = "https://graph.microsoft.com/v1.0/users/" + userId + "/joinedTeams";

		URL teamuri = new URL(teamUrl);
		HttpURLConnection teamconn = (HttpURLConnection) teamuri.openConnection();

		// Set the appropriate header fields in the request header.
		teamconn.setRequestProperty("Authorization", "Bearer " + clienttoken);
		teamconn.setRequestProperty("Accept", "application/json");

		String teamresponse1 = httpClientHelper.getResponseStringFromConn(teamconn);

		int teamresponseCode = teamconn.getResponseCode();
		if (teamresponseCode != HttpURLConnection.HTTP_OK) {
			throw new IOException(teamresponse1);
		}

		JSONObject teamResponseObject = httpClientHelper.processResponse(teamresponseCode, teamresponse1);

		// JSONObject valueTeamObject =responseObject.getJSONObject("value");

		JSONObject teamJsonObject1 = teamResponseObject.getJSONObject("responseMsg");

		if (jsonObject1.has("value")) {
			JSONArray teamjsonArray = teamJsonObject1.getJSONArray("value");

			for (int i = 0, size = teamjsonArray.length(); i < size; i++) {
				JSONObject teamObjectInArray = teamjsonArray.getJSONObject(i);

				String[] elementNames = JSONObject.getNames(teamObjectInArray);

				for (String elementName : elementNames) {

					if (elementName.equals("displayName") && (teamObjectInArray.get(elementName).equals("CPA_POC"))) {

						teamId = teamObjectInArray.getString("id");
						System.out.println(userId);
						break;
					}

				}

			}

		}

		// String clienttoken =
		// httpClientHelper.getAccessTokenForClientCredential(OAuthConstants.GRANT_TYPE_CLIENT_CREDENTIALS,code);
		String channelUrl = "https://graph.microsoft.com/v1.0/teams/" + teamId + "/channels";

		URL channelurl = new URL(channelUrl);
		HttpURLConnection conn1 = (HttpURLConnection) channelurl.openConnection();

		// Set the appropriate header fields in the request header.
		conn1.setRequestProperty("Authorization", "Bearer " + clienttoken);
		conn1.setRequestProperty("Accept", "application/json");

		String channelResponse = httpClientHelper.getResponseStringFromConn(conn1);

		int channelResponseCode = conn1.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			throw new IOException(channelResponse);
		}

		JSONObject channelresponseObject = httpClientHelper.processResponse(channelResponseCode, channelResponse);

		String channelId = "";

		JSONObject jsonObject2 = channelresponseObject.getJSONObject("responseMsg");

		if (jsonObject2.has("value")) {

			JSONArray channelArray1 = jsonObject2.getJSONArray("value");
			for (int i = 0, size = channelArray1.length(); i < size; i++) {
				JSONObject channelObjectInArray = channelArray1.getJSONObject(i);

				String[] elementNames = JSONObject.getNames(channelObjectInArray);

				for (String elementName : elementNames) {

					if (elementName.equals("displayName")
							&& (channelObjectInArray.get(elementName).equals("Incident_Query"))) {

						channelId = channelObjectInArray.getString("id");

						break;
					}

				}

			}
		}

		String msgUrl = "https://graph.microsoft.com/beta/teams/" + teamId + "/channels/" + channelId + "/messages";

		URL msgUri = new URL(msgUrl);

		HttpURLConnection conn2 = (HttpURLConnection) msgUri.openConnection();

		// Set the appropriate header fields in the request header.

		// String authtoken2= httpClientHelper.getAuth(code, redirectUrl);

		conn2.setRequestProperty("Authorization", "Bearer " + clienttoken);
		conn2.setRequestProperty("Accept", "application/json");

		String msgResponse = httpClientHelper.getResponseStringFromConn(conn2);

		int msResponseCode = conn2.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			throw new IOException(msgResponse);
		}

		JSONObject msgresponseObject = httpClientHelper.processResponse(msResponseCode, msgResponse);

		return remedyIncidentHelper.updateIncident(JsonUtil.getMessages(msgresponseObject.toString()), incidentNumber);

	}

	@GetMapping("/create_incident")
	public ModelAndView  createIncident(@RequestParam(value = "first_name", required = false) String first_name,
			@RequestParam(value = "last_name", required = false) String last_name,
			@RequestParam(value = "incidentDescription", required = false) String incidentDescription,
			@RequestParam(value = "templateId", required = false) String templateId,
			@RequestParam(value = "templateId", required = false) String serviceType,Model model) {
		
		String First_Name="Rasmita";
		String Last_Name="Jena";
		String Description= "Rasmita-Test Incident for BMC-Teams Integration POC";
		String TemplateID= "AG00123F73CF5EKnsTSQ5rvrAAZfQA";
		String Login_ID="Rasmita.Jena";
		String Service_Type="User Service Restoration";
		

	 	String baseURL ="http://vtrvitstp-03:8008"; //sc.next();
	 	String username="rasmita.jena";
	    String password="remedy";

		String incidentNumber=remedyIncidentHelper.createIncident(First_Name, Last_Name, Description, TemplateID, Service_Type,Login_ID);
		
		System.out.println(incidentNumber+"++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	    model.addAttribute("incidentId", incidentNumber);
		
		  ModelAndView mav = new ModelAndView("createdInc");
		  
		  mav.addObject("incidentId", incidentNumber); mav.setViewName("createdInc");
		 
	    return mav;
	

	}
	


}
