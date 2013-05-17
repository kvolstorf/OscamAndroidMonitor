package streamboard.opensource.oscam;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class InfoPage extends Activity {

	private Runnable moredetail;
	private Thread thread; 
	private StatusClient _client;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		// prepare thread
		moredetail = new Runnable(){
			@Override
			public void run() {	
				getMoreDetail();
			}
		};
		
		// prepare Channellogos
		setContentView(R.layout.infopage);
		

		// got selected item index from calling activity
		String idx = this.getIntent().getStringExtra("clientid");
		
		// get clientarray from global MainApp
		ArrayList<StatusClient> clients = MainApp.instance.getClients();
		Log.i("Details", "Array size " + clients.size() + " ID " + idx);
		
		if(!(clients.size() == 0)){
			
			for (int i =0; i< clients.size(); i++){
				if (clients.get(i).name.equals(idx)){
					_client = clients.get(i);
					break;
				}
			}
			
			Boolean isServer = false;
			
			// identify the server types
			if (_client.type.equals("s") || 
					_client.type.equals("h") || 
					_client.type.equals("m") || 
					_client.type.equals("a")){
				isServer = true;
			}

			ImageView icon = (ImageView)findViewById(R.id.infopage_icon);
			if ( _client.type.equals("r") ) {
				icon.setImageResource(R.drawable.readericon);
			} else if (_client.type.equals("p")) {
				icon.setImageResource(R.drawable.proxyicon);
			} else if (_client.type.equals("s")) {
				icon.setImageResource(R.drawable.servericon);
			} else if (_client.type.equals("h")) {
				icon.setImageResource(R.drawable.servericon);
			} else if (_client.type.equals("m")) {
				icon.setImageResource(R.drawable.servericon);
			} else if (_client.type.equals("a")) {
				icon.setImageResource(R.drawable.servericon);
			} else if (_client.type.equals("c")) {
				icon.setImageResource(R.drawable.clienticon);
			}
			
			TextView headline = (TextView)findViewById(R.id.infopage_headline);
			headline.setText("Details for " + _client.name);

			TableLayout table = (TableLayout)findViewById(R.id.infopage_table);
			
			addTableRow(table, "Name:",_client.name);
			addTableRow(table, "Protocol:", _client.protocol);
			if(!isServer){
				addTableRow(table, "Request:", _client.request_caid + ":" + _client.request_srvid);
				addTableRow(table, "Channel:", _client.request);
			}
			addTableRow(table, "Login:", MainApp.sdf.format(_client.times_login));
			addTableRow(table, "Online:", MainApp.sec2time(_client.times_online));
			addTableRow(table, "Idle:", MainApp.sec2time(_client.times_idle));
			addTableRow(table, "Connect:", _client.connection_ip); 
			addTableRow(table, "Status:", _client.connection);
		
			String caidsrvid[] = new String[2];
			caidsrvid[0] = _client.request_caid;
			caidsrvid[1] = _client.request_srvid;
			Bitmap logo = MainApp.instance.getLogos().getLogo(caidsrvid, 0);
			
			if(_client.request_ecmhistory.length() > 0 ){
				String ecmvalues[] = _client.request_ecmhistory.split(",");
				if(ecmvalues.length > 0) {
					Integer arr[] = new Integer[ecmvalues.length]; 
					int total = 0;
					int numvalues = 0;
					for(int i=0; i<ecmvalues.length;i++){
						arr[i] = Integer.parseInt(ecmvalues[i]);
						total += arr[i];
						if(arr[i] > 0)
							numvalues++;
					}

					if(numvalues > 0 && total > 0){
						Integer average = total / numvalues;
						addTableRow(table, "Average:", average.toString() + " ms");
					}

					View chart = null;
					RelativeLayout container = (RelativeLayout)findViewById(R.id.infopage_chartlayout);
					if(!isServer && !_client.request_srvid.equals("0000")){
						chart = new ChartView(container.getContext(), arr, logo);
					} else {
						chart = new ChartView(container.getContext(), arr, null);
					}
					container.addView(chart);
				}
			}

			// Request more details in thread to avoid blocking UI
			if(MainApp.instance.getServerInfo().getRevision() >= 4835 && (_client.type.equals("c") || _client.type.equals("r") || _client.type.equals("p"))){
				thread = new Thread(null, moredetail, "MagentoBackground");
				thread.start();
			}
			
		}

	}
	
	private ClientDetail c_detail;
	private ReaderDetail r_detail;
	
	private void getMoreDetail(){
		try {
			String parameter = "";
			if(_client.type.equals("c")){
				parameter = ClientDetail.getUriParameter(_client.name);
			} else if(_client.type.equals("r")){
				parameter = ReaderDetail.getUriParameter(_client.name);
			} else if(_client.type.equals("p")){
				parameter = ReaderDetail.getUriParameter(_client.name);
			}
			
			String httpresponse = MainApp.instance.getServerResponse(parameter);

			// Create XML-DOM
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(httpresponse.toString())));
			doc.getDocumentElement().normalize();

			if(_client.type.equals("c")){
				c_detail = new ClientDetail(doc);
			} else if(_client.type.equals("r")){
				r_detail = new ReaderDetail(doc);
			} else if(_client.type.equals("p")){
				r_detail = new ReaderDetail(doc);
			}
	
			runOnUiThread(returnRes);


		} catch (Exception e) {
			Log.i("Infopage more Details", "Error " + e.getMessage());
		}
	}
	
	private Runnable returnRes = new Runnable() {
		@Override
		public void run() {
			if (c_detail != null){
				TableLayout table = (TableLayout)findViewById(R.id.infopage_detail);
				addTableRow(table, "CW OK:", c_detail.getCWOK().toString()); 
				addTableRow(table, "CW not OK:", c_detail.getCWNOK().toString()); 
				addTableRow(table, "CW ignored:", c_detail.getCWIGNORE().toString()); 
				addTableRow(table, "CW timeout:", c_detail.getCWTIMEOUT().toString()); 
				addTableRow(table, "CW cache:", c_detail.getCWCACHE().toString()); 
				addTableRow(table, "CW tunneled:", c_detail.getCWTUNNEL().toString()); 
				addTableRow(table, "CW rate:", c_detail.getCWRATE().toString());
				addTableRow(table, "EMM OK:", c_detail.getEMMOK().toString()); 
				addTableRow(table, "EMM not OK:", c_detail.getEMMNOK().toString());
				Log.i("Infopage more Details", "getCWOK: " + c_detail.getCWOK());
			} else {
				Log.i("Infopage more Details", "detail is null");
			}
			if (r_detail != null){
				if(r_detail.getEcmList() != null){
					TableLayout table = (TableLayout)findViewById(R.id.infopage_detail);
					
					addTableRow(table, "EMM written:", r_detail.getEmmWritten().toString());
					addTableRow(table, "EMM skipped:", r_detail.getEmmSkipped().toString());
					addTableRow(table, "EMM blocked:", r_detail.getEmmBlocked().toString());
					addTableRow(table, "EMM error:", r_detail.getEmmError().toString());
					addTableRow(table, "ECM total:", r_detail.getEcmTotal().toString());
					addTableRow(table, "Last Request:", MainApp.sdf.format(r_detail.getEcmLastRequest()));
								
					table = (TableLayout)findViewById(R.id.infopage_detail1);
					for(int i = 0; i < r_detail.getEcmList().size(); i++){
						if (r_detail.getEcmList().get(i).getRc() > 0){
							addTableRow(table, r_detail.getEcmList().get(i).getChannelName(), r_detail.getEcmList().get(i).getRcs());
						} else {
							addTableRow(table, r_detail.getEcmList().get(i).getChannelName(), r_detail.getEcmList().get(i).getCount().toString());
						}
					}
				}
			}
		}
	};
	
	private void addTableRow(TableLayout table, String parameter, String value){

		TableRow row = new TableRow(table.getContext());
		
		TableRow.LayoutParams trParams = new TableRow.LayoutParams();
		trParams.setMargins(2, 2, 2, 1);
		row.setLayoutParams(trParams);
		row.setBackgroundColor(Color.rgb(0x00, 0x00, 0x00));
		
		
		TextView text1 = new TextView(row.getContext());
		text1.setTextSize(18);
		text1.setText(parameter);
		text1.setBackgroundColor(Color.rgb(0x18, 0x18, 0x18));
		text1.setLayoutParams(trParams);
		text1.setPadding(5, 0, 0, 0);
		row.addView(text1);
		
		TextView text2 = new TextView(row.getContext());
		text2.setTextSize(18);
		text2.setText(value);
		text2.setBackgroundColor(Color.rgb(0x18, 0x18, 0x18));
		text2.setLayoutParams(trParams);
		text2.setPadding(5, 0, 0, 0);
		row.addView(text2);

		table.addView(row);

	}

	private class ChartView extends View{
		
		private Integer[] _values;
		private boolean _valid = false;
		private Bitmap _logo;
		
		public ChartView(Context context, Integer[] values, Bitmap logo){
			super(context);
			_values = values;
			_logo = logo;

			if (_values != null){
				if (_values.length > 0){
					int checksum = 0;
					for(int i = 0; i <_values.length; i++)
						checksum += _values[i];

					if( checksum > 0)
						_valid = true;

				}
			}
		}
	
		
		@Override protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			if(_valid) {

				DrawFilter drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG);
				canvas.setDrawFilter(drawFilter);
				
				float density = getContext().getResources().getDisplayMetrics().density; 
				int height = getHeight();
				int width = getWidth();
				int number_bars = _values.length;
				int space = 1;
				
				//Log.i("Draw","Density " +  density );
				//Log.i("Draw","Width " +  height );
				//Log.i("Draw","Height " +  width );

				float thickness = ((width - number_bars + 1) / number_bars);
				float border = (width - (number_bars * space) - (thickness * (number_bars+1)))/2 ;
				
				float startX;
				float startY;
				float stopX;
				float stopY;
				
				Paint paint = new Paint();
				paint.setStyle(Paint.Style.FILL);
				paint.setColor(Color.rgb(0x00, 0x66, 0xff));
				paint.setStrokeWidth(thickness);
				
				int i;
				Integer highestvalue = 0;
				float textposition = 0;
				for(i = 1; i < number_bars + 1; i++){
					int value = _values[i-1];
					
					float barheight = (value / 100) * density;
					
					startX = border + (i * (thickness + space)) ;
					stopX = border + (i * (thickness + space));
					
					if (highestvalue < value){
						highestvalue = value;
						textposition = stopX;
					}
					
					startY = height;
					stopY = height - barheight;
					if (barheight > 30){
						paint.setColor(Color.rgb(0xff, 0x66, 0x00));
						canvas.drawLine(startX, startY, stopX, stopY, paint);
						paint.setColor(Color.rgb(0x00, 0x66, 0xff));
						stopY = height - (30 * density);
						canvas.drawLine(startX, startY, stopX, stopY, paint);
					} else {
						canvas.drawLine(startX, startY, stopX, stopY, paint);
					}
					//Log.i("Draw","stopY " +  stopY + " Height " + height + " value " + ((Integer.parseInt(_ecmvalues[i-1]) / 100) * density));
				}
				
				paint.setStrokeWidth(1);
				paint.setColor(Color.rgb(0x18, 0x18, 0x18));
				float j;
				for(j = (10 * density); j < height; j += (10 * density)){
					canvas.drawLine(0, j, getWidth(), j, paint);
				}
				
				paint.setColor(Color.rgb(0xff, 0xff, 0xff));
				paint.setTextSize((10 * density));
				paint.setTextAlign(Paint.Align.CENTER);
				canvas.drawText(highestvalue.toString(), textposition, 50 * density, paint);
				
				//_logo = Bitmap.createScaledBitmap(_logo, 40, 30, true);
				if(_logo != null)
					canvas.drawBitmap(_logo, 2, 2, paint);
				
				
			}
		}
	}


}
