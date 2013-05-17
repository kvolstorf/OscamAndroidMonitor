package streamboard.opensource.oscam;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class LogTabpage extends Activity {
	
	private LogInfo _loginfo = new LogInfo();
	private Runnable receivelog;
	private Thread thread; 
	private boolean running = false;
	
	@Override
	public void onPause(){
		stopRunning();
		Log.i(this.getLocalClassName(),"onPause");
		super.onPause();
	}
	
	@Override
	public void onDestroy(){
		stopRunning();
		Log.i(this.getLocalClassName(),"onDestroy");
		super.onDestroy();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.logtabpage);
		Log.i(this.getLocalClassName(),"started");
		
		// prepare thread
		receivelog = new Runnable(){
			@Override
			public void run() {	
				while(running){
					if(MainApp.instance.getActiveTab() == 3){
						getLog();
						try {
							Thread.sleep(MainApp.instance.getProfiles().getActiveProfile().getServerRefreshValue());
						} catch (InterruptedException e) {}
					} else {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {}
					}
				}

			}
		};

		
		startRunning();
		
	}
	
	private void startRunning(){
		if(running == false){
			running = true;
			thread = new Thread(null, receivelog, "MagentoBackground");
			thread.start();
		}
	}

	private void stopRunning(){
		running = false;
		if(thread != null){
			if (thread.isAlive()) {
				thread.interrupt();
			}
		}		
	}
	
	private void getLog(){
		try {
			String parameter = "";
			parameter = LogInfo.getUriParameter(null);
			String httpresponse = MainApp.instance.getServerResponse(parameter);

			// Create XML-DOM
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(httpresponse.toString())));
			doc.getDocumentElement().normalize();

			_loginfo.parseLogContent(doc);
			
			runOnUiThread(returnRes);


		} catch (Exception e) {
			Log.i("Infopage more Details", "Error " + e.getMessage());
		}
	}
	
	private Runnable returnRes = new Runnable() {
		@Override
		public void run() {
			TextView log = (TextView)findViewById(R.id.logtext);
			log.setText(_loginfo.getLogContent());
		};
	};
		
}