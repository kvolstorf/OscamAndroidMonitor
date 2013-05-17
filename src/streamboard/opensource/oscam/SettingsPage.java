package streamboard.opensource.oscam;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingsPage extends Activity {
	
	private Integer lastindex;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.settingsmenu, menu);
	    
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		ServerProfiles profiles = MainApp.instance.getProfiles();
		
	    switch (item.getItemId()) {
	        case R.id.mnu_nextprofile:     
	        	if (profiles.getActualIdx() < profiles.getLastIdx()){
	        		profiles.setActiveProfile(profiles.getActualIdx() +1 );
	        		loadSettings();
	        	}
	            break;
	        case R.id.mnu_prevprofile: 
	        	if (profiles.getActualIdx() > 0){
	        		profiles.setActiveProfile(profiles.getActualIdx()-1);
	        		loadSettings();
	        	}
	            break;
	        case R.id.mnu_removeprofile: 
	        	profiles.removeProfileAt(profiles.getActualIdx());
	        	profiles.saveSettings();
	        	loadSettings();
	            break;
	        case R.id.mnu_addprofile: 
	        	profiles.createProfile();
	        	loadSettings();
	            break;
	        case R.id.mnu_exitsettings: 
	        	profiles.setActiveProfile(lastindex);
	        	finish();
	            break;
	
	    }
	    return true;
	}
	

	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        lastindex = MainApp.instance.getProfiles().getActualIdx();
    	
    	
        setContentView(R.layout.settingspage);
        loadSettings();
        
	
        
        
    	//Set listener for button in settings
		final Button buttonsave = (Button) findViewById(R.id.saveButton1);
		buttonsave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveSettings();
				//Toast.makeText(this, "Profile saved", Toast.LENGTH_LONG).show();
			}
		});
		

    }
    
	private void loadSettings(){
		ServerProfiles profiles = MainApp.instance.getProfiles();
		
		EditText profilefield = (EditText)findViewById(R.id.editProfileName);
		profilefield.setText(profiles.getActiveProfile().getProfile());
		EditText urlfield = (EditText)findViewById(R.id.editUri1);
		urlfield.setText(profiles.getActiveProfile().getServerAddress());
		EditText portfield = (EditText)findViewById(R.id.editPort1);
		portfield.setText(profiles.getActiveProfile().getServerPort().toString());
		EditText userfield = (EditText)findViewById(R.id.editUser1);
		userfield.setText(profiles.getActiveProfile().getServerUser());
		EditText passfield = (EditText)findViewById(R.id.editPass1);
		passfield.setText(profiles.getActiveProfile().getServerPass());
		CheckBox checkssl = (CheckBox)findViewById(R.id.checkSSL1);
		checkssl.setChecked(profiles.getActiveProfile().getServerSSL());
		Spinner selectrefresh = (Spinner)findViewById(R.id.selectRefresh1);
		selectrefresh.setSelection(profiles.getActiveProfile().getServerRefreshIndex());
	}
	
	private void saveSettings(){
		ServerProfiles profiles = MainApp.instance.getProfiles();
		
		EditText profilefield = (EditText)findViewById(R.id.editProfileName);
		profiles.getActiveProfile().setProfile(profilefield.getText().toString());
		EditText urlfield = (EditText)findViewById(R.id.editUri1);
		profiles.getActiveProfile().setServerAddress(urlfield.getText().toString());
		EditText portfield = (EditText)findViewById(R.id.editPort1);
		profiles.getActiveProfile().setServerPort(portfield.getText().toString());
		EditText userfield = (EditText)findViewById(R.id.editUser1);
		profiles.getActiveProfile().setServerUser(userfield.getText().toString());
		EditText passfield = (EditText)findViewById(R.id.editPass1);
		profiles.getActiveProfile().setServerPass(passfield.getText().toString());
		CheckBox checkssl = (CheckBox)findViewById(R.id.checkSSL1);
		profiles.getActiveProfile().setServerSSL(checkssl.isChecked());
		Spinner selectrefresh = (Spinner)findViewById(R.id.selectRefresh1);
		profiles.getActiveProfile().setServerRefreshIndex(selectrefresh.getSelectedItemPosition());
		profiles.saveSettings();
		
	}
}