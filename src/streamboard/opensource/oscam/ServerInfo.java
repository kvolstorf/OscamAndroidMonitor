package streamboard.opensource.oscam;

import java.text.ParseException;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class ServerInfo {
	private String _version = "Server Version: unknown";
	private Integer _revision = 0;
	private Integer _readonly = 0;
	private Date _startdate;
	private Integer _uptime;
	private Boolean _haserror = false;
	private String _errormessage = "undefined error";
	
	public String getVersion(){
		return this._version;
	}
	
	public Integer getRevision(){
		return this._revision;
	}
	
	public Integer getReadonly(){
		return this._readonly;
	}
	
	public Date getStartdate(){
		return this._startdate;
	}
	
	public Integer getUptime(){
		return this._uptime;
	}
	
	public Boolean hasError(){
		return _haserror;
	}
	
	public String getErrorMessage(){
		if (_haserror){
			return _errormessage;
		} else {
			return "no error";
		}
	}
	
	public ServerInfo(){
		
	}
	
	public ServerInfo(Document doc){
		try {
		
		Node rootnode = doc.getElementsByTagName("oscam").item(0);
		Element rootelement = (Element) rootnode;
		_version = rootelement.getAttribute("version");
		
		if(rootelement.getAttribute("revision") != null && rootelement.getAttribute("revision").length()>0){
			_revision = Integer.parseInt(rootelement.getAttribute("revision"));
		} else {
			_revision = (-1);
		}
		
		if(rootelement.getAttribute("readonly") != null && rootelement.getAttribute("readonly").length()>0){
			_readonly = Integer.parseInt(rootelement.getAttribute("readonly"));
		} else {
			_readonly = (-1);
		}
		
		Log.i("XML Parsing Server Revision = " , _revision.toString());
		
		
		try {
			_startdate = MainApp.dateparser.parse(rootelement.getAttribute("starttime"));
		} catch (ParseException e) {
			_startdate = new Date();
		}
		_uptime = Integer.parseInt(rootelement.getAttribute("uptime")); 
		
		NodeList nl = doc.getElementsByTagName("error");
		if (nl != null) {
			if (nl.getLength() > 0){
				_haserror = true;
				Node errornode = nl.item(0);
				if (errornode.getFirstChild() != null)
					_errormessage = errornode.getFirstChild().getNodeValue();
			}
		}
		} catch (Exception e) {
			Log.i("XML Parsing Excpetion = " , _version + e.getMessage());
		}
		
	}
	
}