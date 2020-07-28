package com.microsoft.azure.springsecuritywebapp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BMCRestTestController {

	private final Logger restLogger = LoggerFactory.getLogger(this.getClass());

	public String login(String baseURL, String userName, String password) {
		String output = "";

		if (!baseURL.endsWith("/")) {
			baseURL = baseURL + "/";
		}
		HttpURLConnection urlConnection = null;
		try {
			URL urltorequest = new URL(baseURL + "api/jwt/login");
			urlConnection = (HttpURLConnection) urltorequest.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);

			String str = "username=" + userName + "&password=" + password;
			byte[] outputInBytes = str.getBytes("UTF-8");
			OutputStream os = urlConnection.getOutputStream();
			os.write(outputInBytes);
			os.close();
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			output = "AR-JWT " + getResponseText(in);

		} catch (MalformedURLException ex) {
			restLogger.debug(ex.getMessage());
		} catch (IOException ex) {
			restLogger.debug(ex.getMessage());
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return output;
	}

	public int logout(String baseURL, String token) {
		HttpURLConnection urlConnection = null;
		int statusCode = 0;

		if (!baseURL.endsWith("/")) {
			baseURL = baseURL + "/";
		}

		try {
			URL urltorequest = new URL(baseURL + "api/jwt/logout");
			urlConnection = (HttpURLConnection) urltorequest.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			urlConnection.setRequestProperty("Authorization", token);
			statusCode = urlConnection.getResponseCode();

		} catch (MalformedURLException ex) {
			restLogger.debug(ex.getMessage());
		} catch (IOException ex) {
			restLogger.debug(ex.getMessage());
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return statusCode;
	}

	public String getEntry(String token, String incidentNumber) throws URISyntaxException {
		HttpURLConnection urlConnection = null;

		CloseableHttpClient httpClient = HttpClients.createDefault();

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("VTRVITSTP-03:8008").setPath("/api/arsys/v1/entry/HPD:IncidentInterface")
				.setParameter("q", ""+"'Incident"+" "+"Number"+"'"+"="+"\""+""+incidentNumber+"\"");
		URI uri;

		uri = builder.build();
		String entryId = null;
			HttpGet httpGet = new HttpGet(uri);
			httpGet.addHeader("Authorization", token);
			try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
				StatusLine status = httpResponse.getStatusLine();
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						httpResponse.getEntity().getContent()));

				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = reader.readLine()) != null) {
					response.append(inputLine);
				}
				reader.close();
				

				JSONObject jsonObject2 = new JSONObject(response.toString());
				JSONArray channelArray1 =	jsonObject2.getJSONArray("entries");
				if (jsonObject2.has("entries")) {
					JSONArray jsonArray = jsonObject2.getJSONArray("entries");

					for (int i = 0, size = jsonArray.length(); i < size; i++) {
						JSONObject objectInArray = jsonArray.getJSONObject(i);

						String[] elementNames = JSONObject.getNames(objectInArray);

						for (String elementName : elementNames) {
							if (elementName.equals("_links")) {
								JSONObject jsonObject3 = objectInArray.getJSONObject("_links");
								JSONArray jsonArray1=jsonObject3.getJSONArray("self");
								entryId=jsonArray1.getJSONObject(0).get("href").toString();
								break;
							}
						}

					}

				}
			
				
		} catch (MalformedURLException ex) {
			restLogger.debug(ex.getMessage());
		} catch (IOException ex) {
			restLogger.debug(ex.getMessage());
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
			return entryId;
	
	}
	private static String getResponseText(InputStream inStream) {
		return new Scanner(inStream).useDelimiter("\\A").next();
	}

	public void updateIncident(String token, String msg, String incidentNumber, String etryUrl){
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPut httpPut = new HttpPut(etryUrl);
		String json = "{ \"values\" : {\n ";
		json += "\"z1D_Details\" : \"" + msg + "\",\n";
		json += "\"z1D_WorklogDetails\" : \"updated via REST\",\n";
		json += "\"z1D Action\" : \"MODIFY\",\n";
		json += "\"z1D_View_Access\" : \"Internal\",\n";
		json += "\"z1D_Secure_Log\" : \"Yes\",\n";
		json += "\"z1D_Activity_Type\" : \"Incident Task/Action\",\n";
		json += "\"Resolution\" : \"User Request has been serviced\",\n";
		json += "\"Urgency\" : \"3-Medium\",\n";
		json += "\"Detailed Decription\" : \"Updated description\"";
		json += " } }";

		httpPut.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
		httpPut.addHeader("Authorization", token);

		try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
			StatusLine status = response.getStatusLine();
			System.out.println("update");
			System.out.println("update status:" + status);
		} catch (ClientProtocolException e) {
			restLogger.debug(e.getMessage());
		} catch (IOException e) {
			restLogger.debug(e.getMessage());
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					restLogger.debug(e.getMessage());
				}

			}
		}
	}

	public String createIncident(String token, String firstName, String lastName, String desription,String loginId,String serviceId,String templateId) {
		String requestId = "";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPut = new HttpPost("http://VTRVITSTP-03:8008/api/arsys/v1/entry/HPD:IncidentInterface_Create");
		String json = "{ \"values\" : {\n ";
		json += "\"First_Name\" : \"" + firstName + "\",\n";
		json += "\"Last_Name\" : \""+ lastName + "\",\n";
		json += "\"Description\" : \""+desription+"\",\n";
		json += "\"TemplateID\" : \""+templateId+"\",\n";
		json += "\"Login_ID\" : \""+loginId+"\",\n";
		json += "\"Service_Type\" : \""+serviceId+"\",\n";
		json += "\"z1D_Action\" : \"CREATE\"";
		json += " } }";
		httpPut.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
		httpPut.addHeader("Authorization", token);
		try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
			StatusLine status = response.getStatusLine();
			//String message=response.getHeaders("Location").toString();
	        Header[] header = response.getHeaders("Location");
	        String content = header[0].getValue();
	        System.out.println(content);
	        String requestIds[]=content.split("/");
	        requestId=requestIds[8];
	        System.out.println(requestId);  
			System.out.println("update");
			System.out.println("update status:" + status);
		} catch (ClientProtocolException e) {
			restLogger.debug(e.getMessage());
		} catch (IOException e) {
			restLogger.debug(e.getMessage());
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					restLogger.debug(e.getMessage());
				}

			}
		}

		return requestId;
	}

	public String getMoreIncident(String requestId, String token) {

		String incidentNumber = "";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(
				"http://vtrvitstp-03:8008/api/arsys/v1/entry/HPD:IncidentInterface_Create/" + requestId);
		httpGet.addHeader("Authorization", token);
		try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
			StatusLine status = httpResponse.getStatusLine();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpResponse.getEntity().getContent()));

			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = reader.readLine()) != null) {
				response.append(inputLine);
			}
			reader.close();

			// print result
			System.out.println(response.toString());
			
			
			
			
			  JSONObject jsonObject2 = new JSONObject(response.toString());
			  JSONObject jsonObject3=  (JSONObject) jsonObject2.get("values");
			  
				if (jsonObject2.has("values")) {

					//JSONArray channelArray1 = jsonObject2.getJSONArray("values");
					//JSONObject channelObjectInArray = channelArray1.getJSONObject(0);

					
					

					//String[] elementNames = JSONObject.getNames(channelObjectInArray);
                    incidentNumber = jsonObject3.getString("Incident Number");

					

				
			
			
		}
		}catch (ClientProtocolException e) {
			restLogger.debug(e.getMessage());
		} catch (IOException e) {
			restLogger.debug(e.getMessage());
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					restLogger.debug(e.getMessage());
				}

			}
		}
		return incidentNumber;
	}
	
	public static void main(String args[])
	{
		BMCRestTestController bMCRestTestController=new BMCRestTestController();
		
		String First_Name="Rasmita";
		String Last_Name="Jena";
		String Description= "Rasmita-Test Incident for BMC-Teams Integration POC";
		String TemplateID= "AG00123F73CF5EKnsTSQ5rvrAAZfQA";
		String Login_ID="Rasmita.Jena";
		String Service_Type="User Service Restoration";
		

	 	String baseURL ="http://vtrvitstp-03:8008"; //sc.next();
	 	String username="rasmita.jena";
	    String password="remedy";

		String token= bMCRestTestController.login(baseURL, username, password);
		
		String requestId=bMCRestTestController.createIncident(token, First_Name, Last_Name, Description, Login_ID, Service_Type, TemplateID);
		
		String incidentNumber=bMCRestTestController.getMoreIncident(requestId, token);
		
		System.out.println(incidentNumber);
		
		try {
			bMCRestTestController.getEntry(token,incidentNumber);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
								
		}
	
	


}
