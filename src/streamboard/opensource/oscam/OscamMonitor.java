package streamboard.opensource.oscam;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;


public class OscamMonitor extends TabActivity {
	
	protected static final int CONTEXTMENU_DETAILS = 0;
	protected static final int CONTEXTMENU_RESTART = 1; 
	protected static final int CONTEXTMENU_DISABLE = 2; 
	protected static final int CONTEXTMENU_ENABLE = 3; 
	
	// define the wakeLock as attribute
	PowerManager.WakeLock wakeLock ;
	PowerManager pm;
	Boolean wakeIsEnabled = false;
	
	public static final String PREFS_NAME = "OscamMonitorPreferences";
	private TabHost tabHost;
	private ListView lv1;
	
	private String filter[];
	
	private Runnable status;
	boolean running = false;
	
	private Thread thread; 
	private Handler handler = new Handler();

	private Integer statusbar_set = 0;
	private String lasterror = "";
	private SubMenu mnu_profiles;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Handler to write Stacktrace on SDcard
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler("/OscamMonitor/trace"));
		
		setContentView(R.layout.main);
		
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "tag");
		wakeIsEnabled = false;
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		MainApp.instance.setProfiles(new ServerProfiles(settings));
		
		// must be after loading profiles
		setAppTitle();
		
		// prepare thread
		status = new Runnable(){
			@Override
			public void run() {	
				getStatus();
				handler.postDelayed(this, 
						MainApp.instance.getProfiles().getActiveProfile()
						.getServerRefreshValue());
			}
		};

		Resources res = getResources(); // Resource object to get Drawables
		tabHost = getTabHost( );  // The activity TabHost
		TabHost.TabSpec spec;  // Resusable TabSpec for each tab
		
		Intent intent;  // Reusable Intent for each tab
		
		//intent = new Intent().setClass(this, StatusClientTabpage.class);
		spec = tabHost.newTabSpec("clients").setIndicator("Clients",
				res.getDrawable(R.drawable.ic_tab_clients))
				.setContent(R.id.ListViewClients);
		tabHost.addTab(spec);

		//intent = new Intent().setClass(this, StatusReaderTabpage.class);
		spec = tabHost.newTabSpec("reader").setIndicator("Reader",
				res.getDrawable(R.drawable.ic_tab_reader))
				.setContent(R.id.ListViewReader);
		tabHost.addTab(spec);

		//intent = new Intent().setClass(this, StatusServerTabpage.class);
		spec = tabHost.newTabSpec("server").setIndicator("Server",
				res.getDrawable(R.drawable.ic_tab_server))
				.setContent(R.id.ListViewServer);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, LogTabpage.class);
		spec = tabHost.newTabSpec("log").setIndicator("Log",
				res.getDrawable(R.drawable.ic_tab_log))
				.setContent(intent);
		tabHost.addTab(spec);

		
		intent = new Intent().setClass(this, ControlTabpage.class);
		spec = tabHost.newTabSpec("controls").setIndicator("Control", 
				res.getDrawable(R.drawable.ic_tab_control))
				.setContent(intent);
		
		tabHost.addTab(spec);
		

		// Set listener for tabchange
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String arg0) {
				//startRunning();
				MainApp.instance.setActiveTab(tabHost.getCurrentTab());
				switchViews(tabHost.getCurrentTab());
			}     
		}); 
		
		if (MainApp.instance.getProfiles().noProfileAvail() == false){
			// if settings filled - clienttab on start
			tabHost.setCurrentTab(0);
			switchViews(0);
		} else {
			// if settings not filled - settingstab on start
			intent = new Intent().setClass(this, SettingsPage.class);
        	startActivity(intent);
		}
		
			
	}
	
	BroadcastReceiver onScreenON = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("BroadcastReceiver", "Screen ON");
			if(!running)
				startRunning();
		}
	}; 
	
	BroadcastReceiver onScreenOFF = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("BroadcastReceiver", "Screen OFF");
			if(running)
				stopRunning();
		}
	}; 
	
	@Override
	public void onPause(){
		super.onPause();
		Log.i(this.getLocalClassName(),"onPause");
		unregisterReceiver(onScreenON); 
		unregisterReceiver(onScreenOFF); 
		if(wakeIsEnabled){
			wakeLock.release();
			wakeIsEnabled = false;
		}
		
		stopRunning();
		MainApp.instance.getProfiles().saveSettings();
		
	}
	
	@Override
	public void onDestroy(){
		Log.i(this.getLocalClassName(),"onDestroy");
		if(wakeIsEnabled){
			wakeLock.release();
			wakeIsEnabled = false;
		}
		
		MainApp.instance.getProfiles().saveSettings();
		super.onDestroy();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.i(this.getLocalClassName(),"onResume");
		
		IntentFilter filterON = new IntentFilter(Intent.ACTION_SCREEN_ON);
		registerReceiver(onScreenON, filterON); 
		IntentFilter filterOFF = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(onScreenOFF, filterOFF);
		
		setAppTitle();
		if(!running)
			switchViews(tabHost.getCurrentTab());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mainmenu, menu);
	    
		mnu_profiles = menu.addSubMenu(0, 3, 0, "Profiles");
		mnu_profiles.setIcon(getResources().getDrawable(R.drawable.ic_menu_profiles));
		mnu_profiles.setHeaderIcon(getResources().getDrawable(R.drawable.ic_menu_profiles));
		
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		mnu_profiles.clear();
		
		ArrayList<String> pnames = MainApp.instance.getProfiles().getProfileNamesArray();
		for(int i = 0; i < pnames.size(); i++){
			mnu_profiles.add(0, i + 4, 0, pnames.get(i));
		}

		if(wakeIsEnabled){
			menu.getItem(1).setTitleCondensed("Auto");
			menu.getItem(1).setTitle("Auto");
		} else {
			menu.getItem(1).setTitleCondensed("Wake");
			menu.getItem(1).setTitle("Wake");
		}
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.mnu_exit:     
	        	finish();
	            break;
	            
	        case R.id.mnu_settings: 
	        	stopRunning();
	        	Intent intent = new Intent().setClass(this, SettingsPage.class);
	        	startActivity(intent);
	            break;
	            
	        case R.id.mnu_run:     
	        	startRunning();
	            break;
	            
	        case R.id.mnu_wake: 
	        	if(item.getTitle().equals("Wake")){
	        		wakeLock.acquire();
	        		wakeIsEnabled = true;
	        	} else {
	        		wakeLock.release();
	        		wakeIsEnabled = false;
	        	}
	            break;
	            
	        case 3:     
	        	// do nothing
	            break;
	            
	        default:
	        	if((item.getItemId() - 4) != MainApp.instance.getProfiles().getActualIdx()){
	        		this.stopRunning();
	        		if (MainApp.instance.getClients() != null)
	        			MainApp.instance.getClients().clear();
	        		MainApp.instance.getProfiles().setActiveProfile(item.getItemId() - 4);
	        		tabHost.setCurrentTab(0);
	        		this.setAppTitle();
	        		this.switchViews(0);
	        	}
	
	    }
	    
	    return true;
	}

	/*
	@Override
	public void onCreateContextMenu (ContextMenu menu, View v, ContextMenuInfo menuInfo){
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		StatusClient o = (StatusClient) lv1.getAdapter().getItem(info.position);

		menu.setHeaderTitle("Options for " + o.name);
		
		if ( o.type.equals("r") ) {
			
		} else if (o.type.equals("p")) {
			
		} else if (o.type.equals("s")) {
			menu.findItem(CONTEXTMENU_RESTART).setEnabled(false);
			menu.findItem(CONTEXTMENU_DISABLE).setEnabled(false);
			menu.findItem(CONTEXTMENU_ENABLE).setEnabled(false);
		} else if (o.type.equals("h")) {
			menu.findItem(CONTEXTMENU_RESTART).setEnabled(false);
			menu.findItem(CONTEXTMENU_DISABLE).setEnabled(false);
			menu.findItem(CONTEXTMENU_ENABLE).setEnabled(false);
		} else if (o.type.equals("m")) {
			menu.findItem(CONTEXTMENU_RESTART).setEnabled(false);
			menu.findItem(CONTEXTMENU_DISABLE).setEnabled(false);
			menu.findItem(CONTEXTMENU_ENABLE).setEnabled(false);
		} else if (o.type.equals("a")) {
			menu.findItem(CONTEXTMENU_RESTART).setEnabled(false);
			menu.findItem(CONTEXTMENU_DISABLE).setEnabled(false);
			menu.findItem(CONTEXTMENU_ENABLE).setEnabled(false);
		} else if (o.type.equals("c")) {
			
		}
			
		
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem aItem) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();

		switch (aItem.getItemId()) {
		case 0:
			StatusClient client = (StatusClient) lv1.getAdapter().getItem(menuInfo.position);
			Intent intent = new Intent().setClass(tabHost.getContext(), InfoPage.class);
			
			Log.i("Details", "Requested position " + client.name + " ID ");
			intent.putExtra("clientid", client.name);
			startActivity(intent);
			return true; 
		}
		return false;
	} 
	
	*/
	public void setAppTitle(){
		this.setTitle("Oscam Monitor: " + MainApp.instance.getProfiles().getActiveProfile().getProfile());
	}
	
	private void startRunning(){
		if(running == false){
			thread = new Thread(null, status, "MagentoBackground");
			thread.start();
		}
		statusbar_set = 4;
		setStatusbar();
		running = true;
	}

	private void stopRunning(){
		handler.removeCallbacks(status);
		if(thread != null){
			if (thread.isAlive()) {
				thread.interrupt();
			}
		}
		running = false;
		statusbar_set = 3;
		this.setStatusbar();
	}
	
	/*
	 * switch views depending on given tab index
	 */
	private void switchViews(int tabidx) {
		
		
		stopRunning();
		
		switch (tabidx){

		case 0:
			lv1 = (ListView)findViewById(R.id.ListViewClients);
			filter = new String[]{"c"};
			break;
		case 1:
			lv1 = (ListView)findViewById(R.id.ListViewReader);
			filter = new String[]{"p","r"};
			break;
		case 2:
			lv1 = (ListView)findViewById(R.id.ListViewServer);
			filter = new String[]{"s","m","a","h"};
			break;
		case 3:
			// Logpage
			break;
		case 4:
			// controlpage
			break;
		}

		lv1.setAdapter(null);
		
		// Settingspage doesn't need connect to server
		if (tabidx < 3) {
			/*
			lv1.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
				public void  onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)  {

					//Let the menu appear
					menu.setHeaderTitle("Options");
					menu.add(Menu.NONE, CONTEXTMENU_DETAILS,1, "Show Details");
					menu.add(Menu.NONE, CONTEXTMENU_RESTART,2, "Restart");
					menu.add(Menu.NONE, CONTEXTMENU_DISABLE,3, "Lock");
					menu.add(Menu.NONE, CONTEXTMENU_ENABLE,4, "Settings");  
					
				
				}

			});
			*/
			TextView st = (TextView) findViewById(R.id.serverstatus);
			st.setVisibility(0);
			statusbar_set = 0;
			startRunning();

		} else {
			TextView st = (TextView) findViewById(R.id.serverstatus);
			Animation a = st.getAnimation();
			if (a != null)
				a.cancel();
			st.setVisibility(8);
		}

		

	}
	
	private void setStatusbar(){
		
		TextView st = (TextView) findViewById(R.id.serverstatus);
		Animation a_in = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
		
		a_in.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				TextView st = (TextView) findViewById(R.id.serverstatus);
				st.setText("");
			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
	
		ServerInfo serverinfo = MainApp.instance.getServerInfo();
		
		Log.i("Statusbar" , "value: " + statusbar_set + " version:  " + serverinfo.getVersion());
		switch(statusbar_set){
			
		case 0:
			st.setText("Server Version: " + serverinfo.getVersion());
			statusbar_set++;
			break;
		case 1:
			
			st.setText("Server Start: " + MainApp.sdf.format(serverinfo.getStartdate()));
			statusbar_set++;
			break;
		case 2:
			st.setText("Server Uptime: " + MainApp.sec2time(serverinfo.getUptime()));
			statusbar_set=0;
			break;
		case 3:
			st.setText("");
			break;
		case 4:
			st.setText("connecting ...");
			break;
		}
		  
	    a_in.reset();
	    //st.clearAnimation();
	    st.startAnimation(a_in);
	  
	}
	
	/*
	 * returns from thread to UI thread
	 */
	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if (MainApp.instance.getClients() != null){

				

				if (lv1.getAdapter() == null){
					lv1.setAdapter(new ClientAdapter(tabHost.getContext(), R.layout.listview_row1 , MainApp.instance.getClients()));
				} else {

					ClientAdapter ad = (ClientAdapter) lv1.getAdapter();
					ad.refreshItems(MainApp.instance.getClients());
					ad.notifyDataSetChanged();
				}
				
				
				
				Log.i("returnRes" , "value: " + statusbar_set);
				
				if(statusbar_set == 4){
					statusbar_set = 0;
				}
				
				setStatusbar();
			}
		}
	};

	/*
	 * Thread
	 */
	private void getStatus(){
		MainApp.instance.setClients(getStatusClients(filter));
		runOnUiThread(returnRes);
	}
	
	/*
	 * returns an arraylist of clients depending of types given in array
	 */
	public ArrayList<StatusClient> getStatusClients(String type[]){
		ArrayList<StatusClient> rc = new ArrayList<StatusClient>();
		StatusClient sc;
		NodeList nl = getNodes();

		try {
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					Node item = nl.item(i);
					sc = new StatusClient(item);
					if (sc != null){
						// check all given types in array
						for (int j = 0; j < type.length; j++){
							if (sc.type.equals(type[j])){
								rc.add(sc);
							}
						}
					} else {
						Log.i(" Loop = " , " null sc  -> " + i);
					}
				} 
			} else {
				return null;
			}
			return rc;

		} catch (Exception e) {
			Log.i("XML Arraylist Excpetion = " , e.getMessage());
			return null;
		}
	}
	
	public NodeList getNodes() {
		try {
			String httpresponse = MainApp.instance.getServerResponse("/oscamapi.html?part=status");
			if(httpresponse.length() > 0){
				// Create XML-DOM
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new InputSource(new StringReader(httpresponse.toString())));
				doc.getDocumentElement().normalize();			

				// check serverinfo and exit on error
				
				ServerInfo serverinfo = new ServerInfo(doc);
				if (serverinfo.hasError()){
					lasterror = serverinfo.getErrorMessage();
					runOnUiThread(showError);
					return null;
				} else {
					MainApp.instance.setServerInfo(serverinfo);
				}

				// return a list of clientnodes
				return doc.getElementsByTagName("client");
				
			} else {
				// get errormessage from main App
				lasterror = MainApp.instance.getLastError();
				runOnUiThread(showError);
				return null;
			}

		} catch (Exception e) {
			lasterror = e.getMessage();
			runOnUiThread(showError);
			Log.i(getClass().getName() , "XML Download Exception", e);
			return null;
		}

	}

	/*
	 * Because if we want to show errors from thread we must come back to
	 * UI context before. Using this by setting lasterror first and call
	 * this runnable with runOnUiThread(showError); then
	 */
	private Runnable showError = new Runnable() {
		@Override
		public void run() {
			// stop update loop
			handler.removeCallbacks(status);
			//show message
			Toast.makeText(tabHost.getContext(), lasterror, Toast.LENGTH_LONG).show();
			lasterror = "";
		}
	};



		
	public class ClientAdapter extends ArrayAdapter<StatusClient> {
		private String _srvids[];
		private ArrayList<Bitmap> _logos;
		private ArrayList<StatusClient> items;

		public ClientAdapter(Context context, int textViewResourceId, ArrayList<StatusClient> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			_srvids = new String[255];
			_logos = new ArrayList<Bitmap>(); //Logocache
		}
		
		public void refreshItems(ArrayList<StatusClient> items){
			this.items = items;
			while (this.items.size() < this.getCount()){
				this.remove( this.getItem( this.getCount() - 1));
			}
			while (this.items.size() > this.getCount()){
				this.add(new StatusClient());
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			StatusClient o = items.get(position);
			boolean isServer = false;
			
			if (o.type.equals("s") || o.type.equals("h") || o.type.equals("m") || o.type.equals("a")){
				isServer = true;
			}
			

			LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if(!isServer){
				v = vi.inflate(R.layout.listview_row1, null);
			} else {
				v = vi.inflate(R.layout.listview_row, null);
			}

			v.setTag(o.name);
			
			v.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent().setClass(tabHost.getContext(), InfoPage.class);
					
					Log.i("Details", "Requested position " + v.getTag() + " ID ");
					intent.putExtra("clientid", (String)v.getTag());
					startActivity(intent);
				}
			
			});
			
			Log.i("Refresh", "Refresh " + position);
			
			if (o != null) {

				ImageView icon =(ImageView) v.findViewById(R.id.icon);
				

				if ( o.type.equals("r") ) {
					icon.setImageResource(R.drawable.readericon);
				} else if (o.type.equals("p")) {
					icon.setImageResource(R.drawable.proxyicon);
				} else if (o.type.equals("s")) {
					icon.setImageResource(R.drawable.servericon);
				} else if (o.type.equals("h")) {
					icon.setImageResource(R.drawable.servericon);
				} else if (o.type.equals("m")) {
					icon.setImageResource(R.drawable.servericon);
				} else if (o.type.equals("a")) {
					icon.setImageResource(R.drawable.servericon);
				} else if (o.type.equals("c")) {
					icon.setImageResource(R.drawable.clienticon);
				}

				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);

				if(isServer){
					tt.setText("User: " + o.name);
					bt.setText(o.protocol + " (type '" + o.type + "')");
				} else {


					if (tt != null) {
						if (o.request_answered.length()>0){
							tt.setText(o.name  + " --> " + o.request_answered);
						} else {
							tt.setText(o.name);
						}
					}
					if(bt != null){
						if ((o.request_ecmtime > 0) || !(o.request_caid.equals("0000")) ) {
							bt.setVisibility(0);
							if (o.request.equals("unknown")) {
								bt.setText(o.request + " [" + o.request_caid + ":" + o.request_srvid +"]");
							} else {
								bt.setText(o.request);
							}
							icon.setAlpha(255);
						} else {
							icon.setAlpha(70);
							bt.setVisibility(8);
						}
					}
				}
				
				if(!isServer){
					// Timebar
					ImageView bar =(ImageView) v.findViewById(R.id.bar);
					Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bar);
					int width = 50;

					width = (o.request_ecmtime / 50) + 1; // +1 to avoid 0 and error
					if(width > 50){
						width = 50;
					}

					try{
						Bitmap resizedbitmap = Bitmap.createBitmap(bmp, 0, 0, width, 2);
						bar.setImageBitmap(resizedbitmap);
					} catch (Exception e){

					}

					// Channellogo
					ImageView channellogo =(ImageView) v.findViewById(R.id.channellogo);
					String caidsrvid[] = new String[2];
					caidsrvid[0] = o.request_caid;
					caidsrvid[1] = o.request_srvid;
					if(this._srvids[position] == null){
						this._srvids[position] = caidsrvid[1];
						this._logos.add(MainApp.instance.getLogos().getLogo(caidsrvid, 0)); // add logo to cache
					}
					if(!this._srvids[position].equals(caidsrvid[1])){
						this._srvids[position] = caidsrvid[1];
						this._logos.set(position, MainApp.instance.getLogos().getLogo(caidsrvid, 0)); // change logo on cache position
					}
					channellogo.setImageBitmap(this._logos.get(position));

					// Protocol icon
					ImageView icon1 =(ImageView) v.findViewById(R.id.icon1);

					if (o.protocol.contains("camd35")){
						icon1.setImageResource(R.drawable.ic_status_c3);
						icon1.setAlpha(255);
					} else if (o.protocol.contains("newcamd")){
						icon1.setImageResource(R.drawable.ic_status_nc);
						icon1.setAlpha(255);
					} else if (o.protocol.contains("cccam")){
						icon1.setImageResource(R.drawable.ic_status_cc);
						icon1.setAlpha(255);
					} else {
						icon1.setImageResource(R.drawable.ic_status_empty);
						icon1.setAlpha(70);
					}

					// AU icon
					ImageView icon3 =(ImageView) v.findViewById(R.id.icon3);

					if (o.au.equals("1")){
						icon3.setImageResource(R.drawable.ic_status_au);
					} else if (o.au.equals("-1")){
						icon3.setImageResource(R.drawable.ic_status_au_fail);
					} else {
						icon3.setImageResource(R.drawable.ic_status_au_no);
					}
				}
				this.notifyDataSetChanged();
			}
			return v;
		}
	}
	
	
}

