package devapp.callrecorder;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import java.io.File;

public class FileProcessService extends Service {

    String NUMBER = "number";
    String OPTIONS = "options";
    String PATH = "pathName";
    boolean[] options = null;
    boolean allCall = false, serviceCall = false, blackListCall = false;
    String phoneNumber = null;
    String name = null;
    String path = null;
    String newPath = null;
    boolean save=false;

    public FileProcessService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            phoneNumber = intent.getStringExtra(NUMBER);
            path = intent.getStringExtra(PATH);
            options = intent.getBooleanArrayExtra(OPTIONS);
            allCall = options[0];
            serviceCall = options[1];
            blackListCall = options[2];

            if (allCall) {
                comparePhoneBoook(phoneNumber);
                newFileName();
                save=true;
            }

            else if (serviceCall || blackListCall) {

                if (serviceCall &&
                        (phoneNumber.startsWith("0800")||phoneNumber.startsWith("0850")||phoneNumber.startsWith("0888")||
                                phoneNumber.startsWith("444") || phoneNumber.length()==4 || phoneNumber.length()==3 ) ) {
                    comparePhoneBoook(phoneNumber);
                    newFileName();
                    save = true;
                }
                if (blackListCall && (compareBlackList())) {
                    newFileName();
                    save=true;
                }
            }

            if (!save){
                deleteRecordFile(path);
            }
            stopSelf();
        }
        return Service.START_NOT_STICKY;
    }

    public void newFileName (){
        File file = new File(path);
        if (name==null){
            name="Untitled ";
        }
        newPath = path.replace("NUMBER= "  +phoneNumber,"NAME= " +name +" NUMBER= " +phoneNumber);
        File newFile = new File (newPath);
        if (file.isFile()){
            file.renameTo(newFile);
        }
    }

    public void deleteRecordFile (String path){
        File deleteFile = new File(path);
        deleteFile.delete();
    }

    public boolean compareBlackList() {

        boolean here = false;
        File directoryRead = new File(getApplicationInfo().dataDir + "/app_BlackList");
        String[] blackListNumber = directoryRead.list();

        for (int i = 0; i < blackListNumber.length; i++) {
            if (blackListNumber[i].endsWith(phoneNumber)) {
                name = blackListNumber[i];
                name = name.replace("\nNUMBER= " +phoneNumber, "");
                name=name.replace("NAME= ","");
                here=true;
            }
        }
        return here;
    }

    //Uyarlama yapılan kısım. Bu satırdan sonrasına çalışmak gerek.
    public void comparePhoneBoook(String number) {
        String id = null;
        String displayName = null;
        int hasPhoneNumber = 0;
        String phoneNumber = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PHONE_CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                id = cursor.getString(cursor.getColumnIndex(ID));
                displayName = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {
                    Cursor telefon_imlec = contentResolver.query(PHONE_CONTENT_URI, null, PHONE_CONTACT_ID + " = ?", new String[]{id}, null);
                    while (telefon_imlec.moveToNext()) {
                        phoneNumber = telefon_imlec.getString(telefon_imlec.getColumnIndex(PHONE_NUMBER));
                        phoneNumber = phoneNumber.replace(" ", "");
                        phoneNumber = phoneNumber.replace("(","");
                        phoneNumber = phoneNumber.replace(")","");
                        if (phoneNumber.startsWith("+9")) {
                            phoneNumber = phoneNumber.replace("+9", "");
                        }
                        if (phoneNumber.startsWith("90")) {
                            phoneNumber = phoneNumber.replace("9", "");
                        }
                        if (phoneNumber.equals(number)) {
                            name = displayName;
                        }
                    }
                    telefon_imlec.close();
                }
            }
        }
    }
}
