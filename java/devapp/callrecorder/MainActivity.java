package devapp.callrecorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends Activity {

    File blackListDirectory = null;
    File blackListFile = null;
    String[] dosyaAdlari = null;

    static final int CONTACT_REQUEST_CODE = 1;
    private SharedPreferences sharedPreferences = null;
    SharedPreferences.Editor editor = null;
    private String ON_OF = "onOff", ALL_NUMBER_CALL = "allcall", SERVICE_NUMBER_CALL = "serviceCall", BLACK_LIST_NUMBER_CALL = "blackListCall", SHOW_INFO = "showInfo", MODE_CODE = "mode";
    private String name = "", number = "", blackListLabel = "";
    private Switch switchOnOff = null, switchAll = null, switchService = null, switchBlack = null;
    private ImageButton imageButtonCallCenter = null, imageButtonKeyboard = null, imageButtonPhoneBook = null;
    private ListView listViewBlackList = null;
    Dialog dialog = null;
    boolean showInfo = true;
    Spinner spinnerMode = null;
    String [] spinnerContent = null;
    ArrayAdapter<CharSequence> adapterSpinner=null;

    /*View kontrollerinin olusturulmasi ve event handle işlemleri onCreate metodu dışında ayrı ayrı fonksiyonlarla yapılmalıydı. Bu işlemlerin onCreate içerisinde yapılması Single Responsobility
    prensibine aykırı. Bu şekilde kodlamayla activity God Object'e dönüşüyor. (Aykut Taşdelen - Android Programlama Eğitimi)*/
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchOnOff = (Switch) findViewById(R.id.switchOnOff);
        imageButtonCallCenter = (ImageButton) findViewById(R.id.imageButtonCallCenter);
        switchAll = (Switch) findViewById(R.id.switchAllCalls);
        switchService = (Switch) findViewById(R.id.switchServiceNumber);
        switchBlack = (Switch) findViewById(R.id.switchBlackList);
        imageButtonKeyboard = (ImageButton) findViewById(R.id.imageButtonKeyboard);
        imageButtonPhoneBook = (ImageButton) findViewById(R.id.imageButtonPhoneBook);
        listViewBlackList = (ListView) findViewById(R.id.listViewBlackList);
        spinnerMode = (Spinner)findViewById(R.id.spinnerMode);

        switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (showInfo){
                        dialog=getInfoDialog();
                        dialog.show();
                    }
                    editor.putBoolean(ON_OF, true);
                    switchAll.setChecked(true);
                    switchAll.setClickable(true);
                    switchService.setClickable(false);
                    switchBlack.setClickable(false);
                    chechkPermission();
                } else {
                    editor.putBoolean(ON_OF, false);
                    switchAll.setChecked(false);
                    switchService.setChecked(false);
                    switchBlack.setChecked(false);
                    switchAll.setClickable(false);
                    switchService.setClickable(false);
                    switchBlack.setClickable(false);
                }
                editor.apply();
            }
        });

        spinnerContent = new String[]{"MODE 1", "MODE 2"};
        adapterSpinner = new ArrayAdapter<CharSequence>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, spinnerContent);
        spinnerMode.setAdapter(adapterSpinner);
        spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt(MODE_CODE, position);
                editor.apply();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        imageButtonCallCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCallCenter = new Intent(getApplicationContext(), CallCenterActivity.class);
                startActivity(intentCallCenter);
            }
        });

        switchAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    editor.putBoolean(ALL_NUMBER_CALL, true);
                    editor.putBoolean(SERVICE_NUMBER_CALL, false);
                    editor.putBoolean(BLACK_LIST_NUMBER_CALL, false);
                    switchService.setClickable(false);
                    switchService.setChecked(false);
                    switchBlack.setClickable(false);
                    switchBlack.setChecked(false);
                } else {
                    editor.putBoolean(ALL_NUMBER_CALL, false);
                    switchService.setChecked(true);
                    switchBlack.setChecked(true);
                    switchService.setClickable(true);
                    switchBlack.setClickable(true);
                }
                editor.apply();
            }
        });

        switchService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    editor.putBoolean(SERVICE_NUMBER_CALL, true);
                } else {
                    editor.putBoolean(SERVICE_NUMBER_CALL, false);
                }
                editor.apply();
            }
        });

        switchBlack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    editor.putBoolean(BLACK_LIST_NUMBER_CALL, true);
                } else {
                    editor.putBoolean(BLACK_LIST_NUMBER_CALL, false);
                }
                editor.apply();
            }
        });

        imageButtonKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = getBlackListDialog();
                dialog.show();
            }
        });

        imageButtonPhoneBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Amaç: Sonuç döndürebileceğimiz bir aktivite başlatmak.
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);  //Show user only contacts w/ phone numbers
                startActivityForResult(pickContactIntent, CONTACT_REQUEST_CODE);
            }
        });

        //Sistem belleği içerisinde uygulamanın kayıtlı olduğu yerde skorlar adında bir alt klasör oluşturdum.
        blackListDirectory = getDir("BlackList", MODE_APPEND);
        setBlackListAdapter();
        //Tercihler için dosya oluşturan metotlar: getSharedPreferences("string",Mode int), getPrefences(Mode int)=tek dosya ise bunu kullanmak yeterli.
        sharedPreferences = getSharedPreferences("OPTIONS", MODE_PRIVATE);
        //sharedPrefences tipinde editor nesnesi oluşturuldu.
        editor = sharedPreferences.edit();
        tercihleriOku(sharedPreferences, switchOnOff, spinnerMode, switchAll, switchService, switchBlack);
    }
    protected void onResume() {
        super.onResume();
        setBlackListAdapter();
    }

    protected void onRestart() {
        super.onRestart();
        setBlackListAdapter();
    }

    private void setBlackListAdapter() {
        dosyaAdlari = blackListDirectory.list();
        Arrays.sort(dosyaAdlari);
        CustomAdapter customAdapter = new CustomAdapter(this, dosyaAdlari, blackListDirectory);
        listViewBlackList.setAdapter(customAdapter);
    }

    public void tercihleriOku(SharedPreferences preferences, CompoundButton v_0, Spinner v, CompoundButton v_1, CompoundButton v_2, CompoundButton v_3 ) {
        boolean onOff = preferences.getBoolean(ON_OF, false);
        boolean allCall = preferences.getBoolean(ALL_NUMBER_CALL, false);
        boolean serviceCall = preferences.getBoolean(SERVICE_NUMBER_CALL, false);
        boolean blackListCall = preferences.getBoolean(BLACK_LIST_NUMBER_CALL, false);
        showInfo = preferences.getBoolean(SHOW_INFO, true);
        int modeCode = preferences.getInt(MODE_CODE,0);
        v.setSelection(modeCode);


        if (!onOff) {
            v_1.setClickable(false);
            v_2.setClickable(false);
            v_3.setClickable(false);
        }
        if (allCall) {
            v_2.setClickable(false);
            v_3.setClickable(false);
        }
        v_0.setChecked(onOff);
        v_1.setChecked(allCall);
        v_2.setChecked(serviceCall);
        v_3.setChecked(blackListCall);
    }

    public void addBlackList() {
        number = number.replace(" ", "");
        number = number.replace("(","");
        number = number.replace(")","");
        if (number.startsWith("+9")) {
            number = number.replace("+9", "");
        }
        if (number.startsWith("90")) {
            number = number.replace("9", "");
        }
        if (blackListControl(number)) {
            Toast.makeText(getApplicationContext(), "Number was added Black List before.", Toast.LENGTH_SHORT).show();
        } else {
            blackListLabel = "NAME= " +name + "\nNUMBER= " +number;
            blackListFile = new File(blackListDirectory.getPath(), "" + blackListLabel);
            if (!blackListFile.exists()) {
                try {
                    blackListFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        setBlackListAdapter();
        name = "";
        number = "";
        blackListLabel = "";
    }

    public boolean blackListControl(String number) {
        boolean here = false;
        String numberFromBlackList;
        setBlackListAdapter();
        for (int i = 0; i < dosyaAdlari.length; i++) {
            numberFromBlackList = dosyaAdlari[i];
            if (numberFromBlackList.endsWith(number)) {
                here = true;
            }
        }
        return here;
    }

    private Dialog getInfoDialog(){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.info_dialog, null);
        CheckBox checkBox = (CheckBox)view.findViewById(R.id.checkBoxInfo);
        Button button = (Button)view.findViewById(R.id.buttonOkInfo);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    editor.putBoolean(SHOW_INFO, false);
                }
                else {
                    editor.putBoolean(SHOW_INFO, true);
                }
                editor.apply();
                tercihleriOku(sharedPreferences, switchOnOff, spinnerMode, switchAll, switchService, switchBlack);
            }
        });

        return builder.create();
    }

    private Dialog getBlackListDialog() {
        //Argüman olarak mevcut contexti alıyor. Amaç kaynak layout.xml dosyasndan gerekli nesneleri oluşturmak.
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        //Dialog penceresinin oluşturulacağı kaynak xml belirtiliyor.
        View view = layoutInflater.inflate(R.layout.black_list_dialog, null);
        final EditText editTextName = (EditText) view.findViewById(R.id.editTextName);
        final EditText editTextNumber = (EditText) view.findViewById(R.id.editTextNumber);
        Button buttonAdd = (Button) view.findViewById(R.id.buttonAdd);
        Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
        //Builder nesnesi oluşturuldu
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Dialog penceresinin hangi view ile oluşturulacağı belirtildi.
        builder.setView(view);
        builder.setCancelable(true);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = editTextName.getText().toString();
                number = editTextNumber.getText().toString();
                if (name.equals("") || number.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please fill in all required entry fields.", Toast.LENGTH_SHORT).show();
                    if (!name.equals("") && number.equals("")) {
                        //Diğer satıra otomatik geçiş
                        editTextNumber.requestFocus();
                    }
                } else {
                    addBlackList();
                    dialog.dismiss();
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    //Uyarlama yapılan kısım. Bu satırdan sonrasına çalışmak gerek.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == CONTACT_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();
                // We only need the NUMBER column, because there will be only one row in the result
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using CursorLoader to perform the query.
                Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
                cursor.moveToFirst();
                // Retrieve the phone number from the NUMBER column
                int columnNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int columnName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                number = cursor.getString(columnNumber);
                name = cursor.getString(columnName);
                addBlackList();
            }
        }
    }


    private void chechkPermission() {

        //Manifestte aldığımız izinleri listeliyoruz.
        String[] permission = {
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.PROCESS_OUTGOING_CALLS,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE    };

        //Aldığımız izinler için sabit belirliyoruz. Eğer uygulama içinde farklı yerlerde izin alınacaksa her izin isteme için farklı tamsayı değeri
        // ile konrol sağlanacak.
        int permissionCode = 23;

        //Versiyon kontrolü Api:23 altıysa bu checkPermission metotu çalışmayacak.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (
                    ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_CONTACTS ) == PackageManager.PERMISSION_GRANTED
                &&  ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE ) == PackageManager.PERMISSION_GRANTED
                &&  ContextCompat.checkSelfPermission(this,android.Manifest.permission.PROCESS_OUTGOING_CALLS ) == PackageManager.PERMISSION_GRANTED
                &&  ContextCompat.checkSelfPermission(this,android.Manifest.permission.RECORD_AUDIO ) == PackageManager.PERMISSION_GRANTED
                &&  ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED
                &&  ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_CONTACTS ) == PackageManager.PERMISSION_GRANTED           ) {

                //-- Eğer almak istediğimiz izinler daha önceden kullanıcı tarafından onaylanmış ise bu kısımda istediğimiz işlemleri yapabiliriz..
                //-- Mesela uygulama açılışında SD Kart üzerindeki herhangi bir dosyaya bu kısımda erişebiliriz.
            }
            else {
                //-- Almak istediğimiz izinler daha öncesinde kullanıcı tarafından onaylanmamış ise bu kod bloğu harekete geçecektir.
                //-- Burada requestPermissions() metodu ile kullanıcıdan ilgili Manifest izinlerini onaylamasını istiyoruz.
                requestPermissions(permission, permissionCode);
            }
        }
    }

    //İzin işleminin nasıl sonuçlandığını yönetebilmek için onRequestPermissionsResult() metodunu implement/Override etmemiz gerekmektedir.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 23: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //-- Eğer kullanıcı istemiş olduğunuz izni onaylarsa bu kod bloğu çalışacaktır.

                } else {
                    //-- Kullanıcı istemiş olduğunuz izni reddederse bu kod bloğu çalışacaktır.
                    switchOnOff.setChecked(false);
                }
            }
        }
    }
}

