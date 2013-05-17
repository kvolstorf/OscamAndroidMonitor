package streamboard.opensource.oscam;

import java.io.DataInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.w3c.dom.Node;

import streamboard.opensource.oscam.http.CustomSSLSocketFactory;

import android.app.Application;
import android.util.Log;

public class MainApp extends Application{

	private ArrayList<StatusClient> _clients;
	private ServerProfiles _profiles;
	private String _lasterror;
	private ServerInfo _serverinfo = new ServerInfo();
	private LogoFactory _logos;
	private Integer _activetab = 0;

	
	public static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.GERMAN);
	public static SimpleDateFormat dateparser = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZ"); 
	public static MainApp instance;
	
	public MainApp(){
		instance = this;
	}
	
	public Integer getActiveTab(){
		return _activetab;
	}
	
	public void setActiveTab(Integer activetab){
		_activetab = activetab;
	}
	
	public LogoFactory getLogos(){
		if (_logos == null)
			_logos = new LogoFactory(this.getApplicationContext());
		return _logos;
	}
	
	public String getLastError(){
		return _lasterror;
	}
	
	public ArrayList<StatusClient> getClients(){
		return _clients;
	}

	public void setClients(ArrayList<StatusClient> clients){
		if(clients != null)
			_clients = clients;
	}

	public ServerProfiles getProfiles(){
		return _profiles;
	}

	public ServerInfo getServerInfo(){
		return _serverinfo;
	}
	
	public void setServerInfo(ServerInfo serverinfo){
		_serverinfo = serverinfo;
	}
	
	public void setProfiles(ServerProfiles profiles){
		_profiles = profiles;
	}
	
	
	public String getServerResponse(String parameter){
		try {
			
			String server = _profiles.getActiveProfile().getServerAddress();
		

			if(server.length() > 0){
				int port = 80;
				try{
					port = _profiles.getActiveProfile().getServerPort();
				} catch (Exception e) {}
				
				String host = "";
				String[] uriparts = null;
				if (server.contains("/")){
					uriparts = server.split("/");
					host = uriparts[0];
				} else {
					host = server;
				}
				
				if(port != 80){
					server = host + ":" + port;
				}
				
				if(uriparts != null){
					for(int i = 1; i< uriparts.length; i++){
						server = server + "/" + uriparts[i];
					}
				}
				
				String user = _profiles.getActiveProfile().getServerUser();
				String password = _profiles.getActiveProfile().getServerPass();
				
				StringBuilder uri = new StringBuilder();
				if (_profiles.getActiveProfile().getServerSSL() == true) {
					uri.append("https://").append(server);
				} else {
					uri.append("http://").append(server);
				}
	
				uri.append(parameter);
				Log.i( "Loader ", uri.toString() + " user: " + user + " pass: " + password + " SSL: " + _profiles.getActiveProfile().getServerSSL().toString());

				HttpParams httpParameters = new BasicHttpParams();
				//HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
				//HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				
				DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
				if (_profiles.getActiveProfile().getServerSSL()  == true )
					httpclient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", new CustomSSLSocketFactory(), port));
				HttpProtocolParams.setUseExpectContinue(httpclient.getParams(), false);	 

				// Set password
				if(user.length() > 0) httpclient.getCredentialsProvider().setCredentials(
						new AuthScope(host, port, null, "Digest"), 
						new UsernamePasswordCredentials(user, password));

				// Execute HTTP request
				HttpGet httpget = new HttpGet(uri.toString());
				HttpResponse response = httpclient.execute(httpget);		    

				// Retrieve content
				HttpEntity r_entity = response.getEntity();
				byte[] result = new byte[2048];
				StringBuilder httpresponse = new StringBuilder();
				int len;
				if( r_entity != null ) {
					DataInputStream is = new DataInputStream(r_entity.getContent()); 
					while ((len = is.read(result)) != -1) httpresponse.append(new String(result).substring(0, len));
				}
				httpclient.getConnectionManager().shutdown();

				if(httpresponse.length() > 0){
					//Log.i( "Loader ", httpresponse.toString());
					return httpresponse.toString();
				}
			}
			return "";
		} 
		catch (SSLException sex) {
			_lasterror = sex.getMessage();
			Log.i(getClass().getName() , "XML Download SSL Exception", sex);
			return "";
		}

		catch (Exception e) {
			_lasterror = e.getMessage();
			Log.i(getClass().getName() , "XML Download Exception", e);
			return "";
		}
	}
	
	
	// some staic functions used globally
	
	public static String getNodeValue(Node node){
		if (node.getFirstChild() != null)
			return node.getFirstChild().getNodeValue();
		else
			return "";
	}
	
	public static String chkNull(String value) {
		if (value == null) return "na";
		if (value.length() == 0)return "";
		return value;
	}
	
	public static Integer chkIntNull(String value) {
		if (value == null) return 0;
		if (value.length() == 0)return 0;
		return Integer.parseInt(value);
	}
	
	public static Float chkFloatNull(String value) {
		if (value == null) value = "0";
		if (value.length() == 0) value = "0";
		return Float.parseFloat(value);
	}
	
	public static Date chkDateNull(String value){
		try {
			return dateparser.parse(value);
		} catch (ParseException e) {
			return new Date();
		}
	}
	
	/*
	 * convert seconds to (0 days) 00:00:00 format
	 */
	public static String sec2time(long elapsedTime) {       
		String format = String.format("%%0%dd", 2); 
		String seconds = String.format(format, elapsedTime % 60);  
		String minutes = String.format(format, (elapsedTime % 3600) / 60);  
		
		long hrs = elapsedTime / 3600;
		String hours = "";
		String days = "";
		
		if(hrs < 24){
			hours = String.format(format, hrs);
		} else {
			if (hrs / 24 > 1)
				days = String.format("%d days ", hrs / 24);
			else
				days = String.format("%d day ", hrs / 24);
			hours = String.format(format, hrs % 24);
		}
		
		String time =  days + hours + ":" + minutes + ":" + seconds;  
		return time;  
	}
	
}
