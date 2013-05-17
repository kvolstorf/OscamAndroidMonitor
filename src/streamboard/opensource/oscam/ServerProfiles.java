package streamboard.opensource.oscam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;


public class ServerProfiles {

	public ArrayList<ServerSetting> profiles;
	private ServerSetting actualprofile;
	private SharedPreferences settings;
	private Integer actualprofile_idx;
	private Boolean noprofile = true;
	
	public Boolean noProfileAvail(){
		return noprofile;
	}
	
	public ServerSetting getActiveProfile(){
		return actualprofile;
	}
	
	public Integer getLastIdx(){
		return profiles.size() - 1;
	}
	
	public void setActiveProfile(Integer index){
		if(index > profiles.size()-1){
			actualprofile_idx = profiles.size()-1;
			actualprofile = profiles.get(actualprofile_idx);
		} else {
			actualprofile_idx = index;
			actualprofile = profiles.get(actualprofile_idx);
		}
	}
	
	public void createProfile(){
		ServerSetting set = new ServerSetting();
		set.setProfile("profile" + profiles.size());
		profiles.add(set);
		actualprofile_idx = profiles.size() -1;
		actualprofile = profiles.get(actualprofile_idx);
	}
	
	public Integer getActualIdx(){
		return actualprofile_idx;
	}
	
	public ServerSetting getProfile(Integer index){
		return profiles.get(index);
	}

	public void addProfile(ServerSetting profile){
		profiles.add(profile);
		saveSettings();
	}
	
	public void removeProfileAt(Integer index){
		if(index < profiles.size() && index >= 0){
			profiles.remove(profiles.get(index));
			actualprofile_idx = index -1;
			if(actualprofile_idx >= 0){
				actualprofile = profiles.get(actualprofile_idx);
			}else{
				actualprofile_idx = 0;
				actualprofile = profiles.get(actualprofile_idx);
			}
		}
	}
	
	// Returns string array to fill e.g. spinner or menu
	public ArrayList<String> getProfileNamesArray(){
			
		ArrayList<String> array = new ArrayList<String>();
		for (int i = 0; i < profiles.size(); i++){
			array.add(profiles.get(i).getProfile());
		}
		return array;
	}

	//Constructor
	public ServerProfiles(SharedPreferences settings) {
		
		this.settings = settings;
		profiles = new ArrayList<ServerSetting>();
		actualprofile_idx = Integer.parseInt(settings.getString("lastprofile", "0"));
		loadSettings();
		

	}
	
	public void loadSettings() {

		if (settings.getString("serverprofilename", "").length() == 0){
			Log.i("Profiles" , "Settings Empty - try load from SD Card");
			if(readSettingsFromFile()){
				Log.i("Profiles" , "Found and import Settings from SD Card");
			}
		}
		
		String[] profile;
		String[] serveraddress;
		String[] serverport;
		String[] serveruser;
		String[] serverpass;
		String[] serverssl;
		String[] serverrefresh;

		if (settings.getString("serverprofilename", "").length() > 0){
			if(settings.getString("serverprofilename", "").contains(";")){
				profile = TextUtils.split(settings.getString("serverprofilename", ""), ";");
				serveraddress = TextUtils.split(settings.getString("serveraddress", ""), ";");
				serverport = TextUtils.split(settings.getString("serverport", ""), ";");
				serveruser = TextUtils.split(settings.getString("serveruser", ""), ";");
				serverpass = TextUtils.split(settings.getString("serverpass", ""), ";");
				serverssl = TextUtils.split(settings.getString("serverssl", ""), ";");
				serverrefresh = TextUtils.split(settings.getString("serverrefresh", ""), ";");
			} else {
				profile = new String[]{settings.getString("serverprofilename", "")};
				serveraddress = new String[]{settings.getString("serveraddress", "")};
				serverport = new String[]{settings.getString("serverport", "")};
				serveruser = new String[]{settings.getString("serveruser", "")};
				serverpass = new String[]{settings.getString("serverpass", "")};
				serverssl = new String[]{settings.getString("serverssl", "0")};
				serverrefresh = new String[]{settings.getString("serverrefresh", "0")};
				
			}

			for(int i = 0; i < profile.length; i++){
				ServerSetting set = new ServerSetting();
				set.setProfile(profile[i]);
				set.setServerAddress(serveraddress[i]);
				set.setServerPort(serverport[i]);
				set.setServerUser(serveruser[i]);
				set.setServerPass(serverpass[i]);
				set.setServerSSL(serverssl[i]);
				set.setServerRefreshIndex(serverrefresh[i]);
				profiles.add(set);
			}
			noprofile = false;
			actualprofile = profiles.get(actualprofile_idx);

		} else {
			ServerSetting set = new ServerSetting();
			profiles.add(set);
			actualprofile_idx = 0;
			actualprofile=profiles.get(actualprofile_idx);
			noprofile = true;
		}

	}

	public void saveSettings() {
		
		String seperator = "";
		String profile = "";
		String serveraddress = "";
		String serverport = "";
		String serveruser = "";
		String serverpass = "";
		String serverssl = "";
		String serverrefresh = "";
		
		for(int i = 0; i < profiles.size(); i++){
						
			ServerSetting set = profiles.get(i);
			
			profile = profile + seperator + set.getProfile();
			serveraddress = serveraddress + seperator + set.getServerAddress();
			serverport = serverport + seperator + set.getServerPort().toString();
			serveruser = serveruser + seperator + set.getServerUser();
			serverpass = serverpass + seperator + set.getServerPass();
			if (set.getServerSSL())
				serverssl = serverssl + seperator + "1";
			else
				serverssl = serverssl + seperator + "0";
			serverrefresh = serverrefresh + seperator + set.getServerRefreshIndex().toString();
			
			seperator = ";";
			
		}
		
		Editor editor = settings.edit();

		editor.putString("serverprofilename", profile);
		editor.putString("serveraddress", serveraddress);
		editor.putString("serverport", serverport);
		editor.putString("serveruser", serveruser);
		editor.putString("serverpass", serverpass);
		editor.putString("serverssl", serverssl);
		editor.putString("serverrefresh", serverrefresh);
		editor.putString("lastprofile", actualprofile_idx.toString());

		editor.commit();

		if(!saveSettingsToFile()){
			Log.i("Profiles" , "Save to file failed");
		}
		
	}
	
	public Boolean saveSettingsToFile(){

		File root = Environment.getExternalStorageDirectory();
		String localpath = root + "/OscamMonitor/settings_backup/";
		File storragedir = new File(localpath);
		try {
			storragedir.mkdirs();			
			BufferedWriter bos = new BufferedWriter(new FileWriter(localpath + "settings"), 20000);
			bos.write(settings.getString("serverprofilename", "") + "\n");
			bos.write(settings.getString("serveraddress", "") + "\n");
			bos.write(settings.getString("serverport", "") + "\n");
			bos.write(settings.getString("serveruser", "") + "\n");
			bos.write(settings.getString("serverpass", "") + "\n");
			bos.write(settings.getString("serverssl", "") + "\n");
			bos.write(settings.getString("serverrefresh", "") + "\n");
			bos.flush();
			bos.close();
			Log.i("Profiles" ,"Settings written " + localpath + "settings");
			return true;

		} catch (Exception e) {
			Log.i("Profiles" ,"Error write Settings " + localpath + "settings");
			Log.i("Profiles" , e.getStackTrace().toString());
			return false;
		}
	}
	
	public Boolean readSettingsFromFile(){
		File root = Environment.getExternalStorageDirectory();
		String localpath = root + "/OscamMonitor/settings_backup/";
		File storragedir = new File(localpath + "settings");
		if(!storragedir.exists())
			return false;

		try{
			Editor editor = settings.edit();
			BufferedReader br = new BufferedReader(new FileReader(storragedir));

			editor.putString("serverprofilename", br.readLine());
			editor.putString("serveraddress", br.readLine());
			editor.putString("serverport", br.readLine());
			editor.putString("serveruser", br.readLine());
			editor.putString("serverpass", br.readLine());
			editor.putString("serverssl", br.readLine());
			editor.putString("serverrefresh", br.readLine());
			editor.commit();
		
			Log.i("Profiles" ,"Read Settings complete " + localpath + "settings");
			return true;
			
		} catch (Exception e) {
			Log.i("Profiles" ,"Error read Settings " + localpath + "settings");
			Log.i("Profiles" , e.getStackTrace().toString());
			return false;
		}
	}

}
