package streamboard.opensource.oscam;

public class ServerSetting {

	private Integer[] refreshtimes = new Integer[]{10000,15000,20000,25000,30000,60000,80000};
	
	private String _profile = "profile1";
	public String getProfile(){
		return _profile;
	}
	public void setProfile(String value){
		_profile = value;
	}
	
	private String _serveraddress;
	public String getServerAddress(){
		return _serveraddress;
	}
	public void setServerAddress(String value){
		_serveraddress = value;
	}
	
	private String _serverport = "80";
	public Integer getServerPort(){
		return Integer.parseInt(_serverport);
	}
	public void setServerPort(Integer value){
		_serverport = value.toString();
	}
	public void setServerPort(String value){
		_serverport = value;
	}
	
	private String _serveruser;
	public String getServerUser(){
		return _serveruser;
	}
	public void setServerUser(String value){
		_serveruser = value;
	}
	
	private String _serverpass;
	public String getServerPass(){
		return _serverpass;
	}
	public void setServerPass(String value){
		_serverpass = value;
	}
	
	private String _serverssl = "0";
	public Boolean getServerSSL(){
		if (_serverssl.equals("1"))
			return true;
		else
			return false;
	}
	public void setServerSSL(Boolean value){
		if (value)
			_serverssl = "1";
		else
			_serverssl = "0";
	}
	public void setServerSSL(String value){
		_serverssl = value;
	}
	
	private String _serverrefresh;
	public Integer getServerRefreshIndex(){
		Integer idx = 0;
		try {
			idx = Integer.parseInt(_serverrefresh);
		}catch (Exception e){
			return 0;
		}
		if (idx > refreshtimes.length-1)
			return 0;
		else
			return idx;
	}
	public void setServerRefreshIndex(Integer value){
		_serverrefresh = value.toString();
	}
	public void setServerRefreshIndex(String value){
		_serverrefresh = value;
	}
	public Integer getServerRefreshValue(){
		Integer idx = 0;
		try {
			idx = Integer.parseInt(_serverrefresh);
		}catch (Exception e){
			return refreshtimes[0];
		}
		if (idx > refreshtimes.length-1)
			return refreshtimes[0];
		else
			return refreshtimes[idx];
	}
	
	public ServerSetting() {
		// TODO Auto-generated constructor stub
	}

}
