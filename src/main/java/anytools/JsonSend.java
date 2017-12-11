package anytools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class JsonSend {
	static final int READ_TIMEOUT = 5000;
	String logUrl = System.getProperty("rsys.logurl", "https://msk-hibtest.corp.icba.biz:8181/rsys/startPoint/log");
	JsonLog json;
	int curTimeOut;
	
	public JsonSend(JsonLog Json) {
	  json = Json;
	  curTimeOut = READ_TIMEOUT;
	}

	public JsonSend(JsonLog Json, String Url) {
		  json = Json;
		  logUrl = Url;
	}

	public JsonSend(JsonLog Json, int TimeOut) {
		  json = Json;
		  TimeOut = curTimeOut;
	}

//	public  SendContaner(){
//		
//	}
	public int Send(boolean needResponse){
//	  HttpsURLConnection con;
	  BufferedReader in = null;
	  StringBuffer response = new StringBuffer();
	  int responseCode = JsonLog.RET_ERROR;
		
	try {
		String out = json.obj2json().toJSONString();
//		System.out.println("logUrl = " + logUrl);
//		con = (HttpsURLConnection) new URL(logUrl).openConnection();
		HttpURLConnection con = (HttpURLConnection) new URL(logUrl).openConnection();
		con.setRequestMethod("POST");
  	    con.setReadTimeout(needResponse? curTimeOut: 1);
		con.setDoOutput(true);
//		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
		con.setRequestProperty("Content-Length", "" + out.length()*2);
		
		String loginPassword = "syshedshdl:itlekkth2";
		String encoded = new sun.misc.BASE64Encoder().encode (loginPassword.getBytes());
		con.setRequestProperty ("Authorization", "Basic " + encoded);
		
		con.connect();
	    OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
	    wr.write(out);
		wr.flush();
		wr.close();
		con.getOutputStream().close();

//		 DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//		 wr.writeBytes(json.obj2json().toJSONString());
//		 wr.flush();
//		 wr.close();
		 
//		 if (needResponse){
		    responseCode = con.getResponseCode();
		    in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		    response.append(in.readLine());
//		 }else responseCode = JsonLog.RET_WITHOUTRESP;
		 
//		 String inputLine = in.readLine();
//			while ((inputLine = in.readLine()) != null) {
//				response.append(in.readLine());
//			}
		 if (response.length() != 0) responseCode = new Integer(response.toString()).intValue();
	}catch (java.net.SocketTimeoutException e) {
		responseCode = JsonLog.RET_TIMEOUT;
	} catch (MalformedURLException e) {
//		responseCode = JsonLog.RET_NOSERVLET;
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return responseCode;
	}
	
	public JsonLog getJson() {
		return json;
	}

	public void setJson(JsonLog json) {
		this.json = json;
	}

}
