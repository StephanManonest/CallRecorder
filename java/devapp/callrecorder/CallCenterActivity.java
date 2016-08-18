package devapp.callrecorder;

import android.app.Activity;
import android.os.Environment;
import android.os.Bundle;
import android.widget.ListView;
import java.io.File;
import java.util.Arrays;


public class CallCenterActivity extends Activity {

    ListView listViewCallCenter = null;
    String [] fileNames = null;
    CustomAdapterCallCenter customAdapterCallCenter = null;
    File directory = null;
    String directoryPath = null;
    File noteDirectory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_center);
        listViewCallCenter = (ListView)findViewById(R.id.listViewCallCenter);
        noteDirectory=getDir("Note",MODE_APPEND);
        createCustomAdapterCallCenter();
    }

    protected void onResume() {
        super.onResume();
        createCustomAdapterCallCenter();
    }

    protected void onRestart() {
        super.onRestart();
        createCustomAdapterCallCenter();
    }

    public void createCustomAdapterCallCenter(){
        directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        directoryPath+="/"+"CallRecorderRecords";
        directory = new File(directoryPath);
        fileNames = directory.list();
        if (directory.length()!=0) {
            Arrays.sort(fileNames);
            customAdapterCallCenter = new CustomAdapterCallCenter(this, fileNames, directory,getApplicationContext(), noteDirectory);
            listViewCallCenter.setAdapter(customAdapterCallCenter);
        }
    }
}
