package streamboard.opensource.oscam;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class StatusServerTabpage extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is the Status Server tab");
        setContentView(textview);
    }
}