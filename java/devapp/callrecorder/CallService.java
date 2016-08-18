package devapp.callrecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.Locale;

public class CallService extends Service {

    String NUMBER = "number";
    String RECORD = "record";
    String OPTIONS = "options";
    String PATH="pathName";
    String recordFilePath = null;
    String phoneNumber = null;
    String MODE_CODE = "mode";
    int modeCode = 0;
    boolean [] options = null;
    boolean record = false;
    Intent fileProcessIntent = null;
    MediaRecorder recorder=null;
    boolean recording = false;
    private static final String CALL_RECORDS_FOLDER_NAME = "CallRecorderRecords";

    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent!=null){

            if (intent.getStringExtra(NUMBER)!=null){
                phoneNumber = intent.getStringExtra(NUMBER);
                phoneNumber = phoneNumber.replace(" ","");
                phoneNumber = phoneNumber.replace("(","");
                phoneNumber = phoneNumber.replace(")","");
                if (phoneNumber.startsWith("+9")) {
                    phoneNumber = phoneNumber.replace("+9", "");
                }
                if (phoneNumber.startsWith("90")){
                    phoneNumber.replace("9","");
                }
                options = intent.getBooleanArrayExtra(OPTIONS);
                fileProcessIntent = new Intent(getApplicationContext(), FileProcessService.class);
                fileProcessIntent.putExtra(OPTIONS, options);
                fileProcessIntent.putExtra(NUMBER,phoneNumber);
            }
            else {
                modeCode = intent.getIntExtra(MODE_CODE,0);
                record = intent.getBooleanExtra(RECORD, false);
                try {
                    onRecord(record);
                }catch (Exception e){
                    e.getMessage();
                }
            }
        }
        return Service.START_NOT_STICKY;
    }

    private void startRecording() {
        recorder = new MediaRecorder();

        switch (modeCode){
            case 0:
                recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                break;
            case 1:
                recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
                break;
            default:
                return;
        }

        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recordFilePath=getRecordFilePath();
        recorder.setOutputFile(recordFilePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        try {
            recorder.prepare();
        } catch (Exception e) {
            e.getMessage();
        }
        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void onRecord (boolean record){

        if (record){
            startRecording();
            recording = true;
            fileProcessIntent.putExtra(PATH, recordFilePath);
            getApplicationContext().startService(fileProcessIntent);
            recordFilePath=null;
        }
        else {
            if (recording) {
                stopRecording();
            }
            stopSelf();
        }
    }

    public String getRecordFilePath() {

        String path = Environment.getExternalStorageDirectory().getPath();
        File directory = new File(path,CALL_RECORDS_FOLDER_NAME);
        if (!directory.exists()) {
            directory.mkdir();
        }
        directory.setExecutable(true); directory.setReadable(true); directory.setWritable(true);
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("kk.mm.ss", Locale.getDefault());
        Date callDate = new Date();

        String recordName = "DATE= " +dateFormat.format(callDate) +" TIME= " +timeFormat.format(callDate) +"\nNUMBER= "  +phoneNumber +" CallRecorder.mp3";
        phoneNumber=null;
        return (directory.getPath() +"/" +recordName);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}