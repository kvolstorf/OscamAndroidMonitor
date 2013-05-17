package streamboard.opensource.oscam;

import java.io.File;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;


public class LogoFactory {
	
	private Context _context;
	private File _root;
	private String _basepath = "/OscamMonitor/logos/";
	private String _logopath;
	private Bitmap _nologo;
	
	// constructor
	public LogoFactory(Context context){
		_context = context;
		_root = Environment.getExternalStorageDirectory();
		_logopath = _root.getAbsolutePath() + _basepath;
		_nologo = BitmapFactory.decodeResource( _context.getResources(), R.drawable.lg_no_logo);
	}
	
	public Bitmap getLogo(String name[], int type){
		
		if (!this.hasStorage(false)) {
			return _nologo;
		}
		
		switch(type){
			
			case 0:
				// channel logo
				return this.generateChannelLogo(name[0], name[1]);
					
			case 1:
				// user logo
				return this.generateUserLogo(name[0]);
		
			default:
				// case else
				return _nologo;
		}

	}
	
	private Bitmap generateUserLogo(String name){
		
		StringBuilder filename = new StringBuilder();
		filename.append(_logopath);
		filename.append(name);
		filename.append(".pn");
		
		return this.getBitmapFromPath(filename.toString());
	}
	
	private Bitmap generateChannelLogo(String caid, String srvid){
		
		StringBuilder filename = new StringBuilder();
		filename.append(_logopath);
		filename.append(caid);
		filename.append("_");
		filename.append(srvid);
		filename.append(".pn");
		
		return this.getBitmapFromPath(filename.toString());
	}

	private Bitmap getBitmapFromPath(String path){
		try {
			 
			File lp = new File(path);
			
			if (lp.exists()){
				
				Log.i("Pfad", path);
				Bitmap bitmap = BitmapFactory.decodeFile(path);
				return bitmap;
				
			} else {
								
				return _nologo;
				
			}
		} 
		
		catch (Exception e) {
			return _nologo;
		}
	}

	public boolean hasStorage(boolean requireWriteAccess) {
	    String state = Environment.getExternalStorageState();

	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    } else if (!requireWriteAccess
	            && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	} 
	

	public void generateFolders(){
		
		// should folder structure
	}
	
}