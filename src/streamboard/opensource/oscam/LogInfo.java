package streamboard.opensource.oscam;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import android.util.Log;

public class LogInfo {
	
	public static String URI_PARAMETER = "/oscamapi.html?part=status&appendlog=1";
	public static String getUriParameter(String value){
		return URI_PARAMETER;
	}
	
	private String logcontent;
	
	public LogInfo(){
		resetLogContent();
	}

	public LogInfo(Document doc){
		resetLogContent();
		parseLogContent(doc);
	}
	
	public void parseLogContent(Document doc){
		try{

			NodeList lognodes = doc.getElementsByTagName("log");
			if (lognodes != null){
				Node lognode = lognodes.item(0);
				if (lognode.getFirstChild() != null){
					logcontent = lognode.getFirstChild().getNodeValue();
				}
			}
		
			removeDates();
			
		} catch (Exception e) {
			Log.i("XML Log Parsing Excpetion = " , e.getMessage());
		}
	}
	
	//removing the datepart of logline timestamps
	private void removeDates(){
		logcontent = logcontent.replaceAll("\\d{4}/\\d{2}/\\d{2} ", "");
	}
	
	public String getLogContent(){
		return logcontent.trim();
	}
	
	public void resetLogContent(){
		logcontent = "no log avail";
	}
}
