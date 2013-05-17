package streamboard.opensource.oscam;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ControlTabpage extends Activity {
	
	
	private Boolean serverIsReadonly = false;
	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.controltabpage);
        
		// Set listener for Shutdown button in controls
		final Button buttonshutdown = (Button) findViewById(R.id.ctrlServerShutdown);
		buttonshutdown.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendcontrol(0);
			}
		});
		
		// Set listener for Restart button in controls
		final Button buttonrestart = (Button) findViewById(R.id.ctrlServerRestart);
		buttonrestart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendcontrol(1);
			}
		});
		
		if(MainApp.instance.getServerInfo() != null){
			if (MainApp.instance.getServerInfo().getReadonly() == 1)
				this.serverIsReadonly = true;
		}
		
		if(this.serverIsReadonly){
			buttonrestart.setEnabled(false);
			buttonshutdown.setEnabled(false);
		} else {
			buttonrestart.setEnabled(true);
			buttonshutdown.setEnabled(true);
		}

    }
    
	private void sendcontrol(Integer value){

		String parameter = "";
		switch(value){

		case 0:
			//Shutdown
			parameter = "/shutdown.html?action=Shutdown";
			break;
		case 1:
			//Restart
			parameter = "/shutdown.html?action=Restart";
			break;
		}

		Log.i("Controlpage","send: " + parameter);
		MainApp.instance.getServerResponse(parameter);

	}
}
