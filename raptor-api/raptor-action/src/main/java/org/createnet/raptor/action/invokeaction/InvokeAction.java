package org.createnet.raptor.action.invokeaction;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class InvokeAction {

	InvokeAction() {

	}

	public static void invoke(String lwEUI, String action, String payload) {

		String url ="http://loraaction/action/";
		URL obj;
		HttpsURLConnection con;
		
		try {
			
			url = url + "/devEUI=" + lwEUI + "&action=" + action;
			
			obj = new URL(url);
			
			con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			
			con.setDoOutput(true);
			
			String urlParameters = "payload=" + payload;
					
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			//print result
			System.out.println(response.toString());

			
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
