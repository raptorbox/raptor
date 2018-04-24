package org.createnet.raptor.action.invokeaction;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class InvokeAction {

	InvokeAction() {

	}

	public static void invoke(String url, String lwEUI, String action, String payload) {

		final String uri = url + "/loraaction/action?devEUI=" + lwEUI + "&action=" + action;
		System.out.println(uri);
	    RestTemplate restTemplate = new RestTemplate();
	    
	    if (payload.startsWith("\"") && payload.endsWith("\"")) {
	    	payload = payload.substring(1, payload.length()-1);
	    }
	    
	    String requestJson = "{\"payload\":\"" + payload + "\"}";
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    System.out.println(requestJson);
	    HttpEntity<String> entity = new HttpEntity<String>(requestJson,headers);
	    
	    String result = restTemplate.postForObject( uri, entity, String.class);
	 
	    System.out.println(result);
	}

}
