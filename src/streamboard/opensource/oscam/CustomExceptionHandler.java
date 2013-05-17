package streamboard.opensource.oscam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;


public class CustomExceptionHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler _defaultUEH;
    private String _localPath;
    private File _root;
    private SimpleDateFormat stacktraceformat = new SimpleDateFormat("dd_MM_yy_HH_mm", Locale.GERMAN);
    
    public CustomExceptionHandler(String localPath) {
    	this._root = Environment.getExternalStorageDirectory();
    	this._localPath = localPath;
    	this._defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();
		printWriter.close();
		String filename = "OscamMonitor_" + this.stacktraceformat.format(new Date()) + ".stacktrace";

		if (this._localPath != null) {
			writeToFile(stacktrace, filename);
		}

		this._defaultUEH.uncaughtException(t, e);
	}
	
	private void writeToFile(String stacktrace, String filename) {
		try {
			File stactracedir = new File(this._root + this._localPath + "/");
			stactracedir.mkdirs();
			BufferedWriter bos = new BufferedWriter(new FileWriter(this._root + this._localPath + "/" + filename));
			bos.write(stacktrace);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
