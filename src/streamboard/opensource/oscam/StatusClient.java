package streamboard.opensource.oscam;

import java.util.Date;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StatusClient {
	
	public String name;
	public String type;
	public String protocol;
	public String protocolext;
	public String au;

	public String request_caid;
	public String request_srvid;
	public Integer request_ecmtime;
	public String request_ecmhistory;
	public String request_answered;
	public String request;

	public Date times_login;
	public Integer times_online;
	public Integer times_idle;

	public String connection_ip;
	public Integer connection_port;
	public String connection;
	
	public String getSummary(){
		
		if(name != null){
		StringBuilder text = new StringBuilder();
		text.append("Name:\t\t\t" + name + "\n");
		text.append("Protocol:\t\t" + protocol + "\n");
		text.append("Request:\t\t" + request_caid + ":" + request_srvid + "\n");
		text.append("Channel:\t\t" + request + "\n");
		text.append("Login:\t\t\t" + MainApp.sdf.format(times_login) + "\n");
		text.append("Online:\t\t" + MainApp.sec2time(times_online) + "\n");
		text.append("Idle:\t\t\t\t" + MainApp.sec2time(times_idle) + "\n");
		text.append("Connect:\t\t" + connection_ip + "\n"); 
		text.append("Status:\t\t\t" + connection + "\n");
		
		return text.toString();
		} else {
			return "please wait for next refresh";
		}
	}
	
	public StatusClient(){
		
	}

	public StatusClient(Node node){
		try {
			
			Element baseelement = (Element) node;
			Element element = (Element) node;

			type = MainApp.chkNull(element.getAttribute("type"));
			name = MainApp.chkNull(element.getAttribute("name"));
			protocol = MainApp.chkNull(element.getAttribute("protocol"));
			protocolext = MainApp.chkNull(element.getAttribute("protocolext"));
			au = MainApp.chkNull(element.getAttribute("au"));

			NodeList nl = baseelement.getElementsByTagName("request");
			Node innernode = nl.item(0);
			element = (Element) innernode;

			request = MainApp.getNodeValue(innernode);
						
			request_caid = MainApp.chkNull(element.getAttribute("caid"));
			request_srvid = MainApp.chkNull(element.getAttribute("srvid"));
			request_ecmtime = MainApp.chkIntNull(element.getAttribute("ecmtime"));
			request_answered = MainApp.chkNull(element.getAttribute("answered"));
			request_ecmhistory = MainApp.chkNull(element.getAttribute("ecmhistory"));

			nl = baseelement.getElementsByTagName("times");
			innernode = nl.item(0);
			element = (Element) innernode;

			times_login = MainApp.dateparser.parse(MainApp.chkNull(element.getAttribute("login")));
			times_online = MainApp.chkIntNull(element.getAttribute("online"));
			times_idle = MainApp.chkIntNull(element.getAttribute("idle"));

			nl = baseelement.getElementsByTagName("connection");
			innernode = nl.item(0);
			element = (Element) innernode;

			connection = MainApp.chkNull(innernode.getFirstChild().getNodeValue());
			connection_ip = MainApp.chkNull(element.getAttribute("ip"));
			connection_port = MainApp.chkIntNull(element.getAttribute("port"));

		} catch (Exception e) {
			//Log.i("XML Pasing Excpetion = " , e.getMessage());
		}
	}
	

}