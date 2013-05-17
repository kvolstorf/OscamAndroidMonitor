package streamboard.opensource.oscam;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class ClientDetail {

	public static String URI_PARAMETER = "/oscamapi.html?part=userstats&label=";
	public static String getUriParameter(String value){
		return URI_PARAMETER + value;
	}
	
	private Integer _cwok = 0;
	private Integer _cwnok = 0;
	private Integer _cwignore = 0;
	private Integer _cwtimeout = 0;
	private Integer _cwcache = 0;
	private Integer _cwtun = 0;
	private Integer _cwlastresptime = 0;
	private Integer _emmok = 0;
	private Integer _emmnok = 0;
	private float _cwrate = 0;
	
	public Integer getCWOK(){
		return _cwok;
	}
	
	public Integer getCWNOK(){
		return _cwnok;
	}
	
	public Integer getCWIGNORE(){
		return _cwignore;
	}
	
	public Integer getCWTIMEOUT(){
		return _cwtimeout;
	}
	
	public Integer getCWCACHE(){
		return _cwcache;
	}
	
	public Integer getCWTUNNEL(){
		return _cwtun;
	}
	
	public Integer getCWLASTRESPONSETIME(){
		return _cwlastresptime;
	}
	
	public Integer getEMMOK(){
		return _emmok;
	}
	
	public Integer getEMMNOK(){
		return _emmnok;
	}
	
	public Float getCWRATE(){
		return _cwrate;
	}
	
	public ClientDetail(){
		
	}
	
	public ClientDetail(Document doc){
		
		try{
		NodeList usernodes = doc.getElementsByTagName("users");
		if (usernodes != null){
			Node usernode = usernodes.item(0);
			
			NodeList statsnodes = ((Element) usernode).getElementsByTagName("stats");
			
			Node statsnode = statsnodes.item(0);
			NodeList content = statsnode.getChildNodes();
			
			for(int i = 0; i < content.getLength(); i++){
				if (content.item(i).getFirstChild() != null){
					//Log.i("ClientDetail", "ID " + i + " Content " + content.item(i).getFirstChild().getNodeValue() );
					switch(i){
					case 1:_cwok = MainApp.chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 3:_cwnok = MainApp.chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 5:_cwignore = MainApp.chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 7:_cwtimeout = MainApp.chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 9:_cwcache = MainApp.chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 11:_cwtun = MainApp.chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 13:_cwlastresptime = MainApp.chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 15:_emmok = MainApp.chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 17:_emmnok = MainApp.chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 19:_cwrate = MainApp.chkFloatNull(content.item(i).getFirstChild().getNodeValue());
					}
				}
			}
		}
		} catch (Exception e){
			Log.i("Clientdetail", "Error " + e.getStackTrace().toString());
		}
	}


}
