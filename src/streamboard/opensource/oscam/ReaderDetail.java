package streamboard.opensource.oscam;

import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReaderDetail {

	public static String URI_PARAMETER = "/oscamapi.html?part=readerstats&label=";
	public static String getUriParameter(String value){
		return URI_PARAMETER + value;
	}

	private ArrayList<ECMstat> _ecmlist;
	private Integer _emmtotwritten;
	private Integer _emmtotskipped;
	private Integer _emmtotblocked;
	private Integer _emmtoterror;
	
	private Integer _ecmtotal;
	private Date _ecmlastaccess;
	
	public ArrayList<ECMstat> getEcmList() {
		return _ecmlist;
	}
	public Integer getEmmWritten(){
		return _emmtotwritten;
	}
	public Integer getEmmSkipped(){
		return _emmtotskipped;
	}
	public Integer getEmmBlocked(){
		return _emmtotblocked;
	}
	public Integer getEmmError(){
		return _emmtoterror;
	}
	public Integer getEcmTotal(){
		return _ecmtotal;
	}
	public Date getEcmLastRequest(){
		return _ecmlastaccess;
	}
	
	public ReaderDetail(Document doc) {
		
		NodeList emmnodes = doc.getElementsByTagName("emmstats");
		Element emmmainnode = (Element)emmnodes.item(0);
		_emmtotwritten = MainApp.chkIntNull(emmmainnode.getAttribute("totalwritten"));
		_emmtotskipped = MainApp.chkIntNull(emmmainnode.getAttribute("totalskipped"));
		_emmtotblocked = MainApp.chkIntNull(emmmainnode.getAttribute("totalblocked"));
		_emmtoterror = MainApp.chkIntNull(emmmainnode.getAttribute("totalerror"));
		
		NodeList ecmmainnodes = doc.getElementsByTagName("ecmstats");
		Element ecmmainnode = (Element)ecmmainnodes.item(0);
		_ecmtotal = MainApp.chkIntNull(ecmmainnode.getAttribute("totalecm"));
		_ecmlastaccess = MainApp.chkDateNull(ecmmainnode.getAttribute("lastaccess"));
		
		NodeList ecmnodes = doc.getElementsByTagName("ecm");

		if(ecmnodes.getLength() > 0){
			_ecmlist = new ArrayList<ECMstat>();
			for (int i = 0; i < ecmnodes.getLength(); i++) {
				_ecmlist.add(new ECMstat(ecmnodes.item(i)));
			}
		}
	}

	class ECMstat {

		private String _caid;
		private String _provid;
		private String _srvid;
		private String _channelname;
		private Integer _avgtime;
		private Integer _lasttime;
		private Integer _rc;
		private String _rcs;
		private Date _lastrequest;
		private Integer _count;

		public String getCaid(){
			return _caid;
		}
		public String getProvid(){
			return _provid;
		}
		public String getSrvid(){
			return _srvid;
		}
		public String getChannelName(){
			return _channelname;
		}
		public Integer getAvgTime(){
			return _avgtime;
		}
		public Integer getLastTime(){
			return _lasttime;
		}
		public Integer getRc(){
			return _rc;
		}
		public String getRcs(){
			return _rcs;
		}
		public Date getLastRequest(){
			return _lastrequest;
		}
		public Integer getCount(){
			return _count;
		}

		public ECMstat(Node node){

			Element element = (Element) node;
			_caid = MainApp.chkNull(element.getAttribute("caid"));
			_provid = MainApp.chkNull(element.getAttribute("provid"));
			_srvid = MainApp.chkNull(element.getAttribute("srvid"));
			_channelname = MainApp.chkNull(element.getAttribute("channelname"));
			_avgtime = MainApp.chkIntNull(element.getAttribute("avgtime"));
			_lasttime = MainApp.chkIntNull(element.getAttribute("lasttime"));
			_rc = MainApp.chkIntNull(element.getAttribute("rc"));
			_rcs = MainApp.chkNull(element.getAttribute("rcs"));
			_lastrequest = MainApp.chkDateNull(element.getAttribute("lastrequest"));
			_count = MainApp.chkIntNull(MainApp.getNodeValue(node));

		}

	}
}
