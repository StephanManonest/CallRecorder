package devapp.callrecorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.Arrays;

public class CustomAdapter extends BaseAdapter {
    Dialog dialog = null;

    private LayoutInflater layoutInflater = null;
    private String[] dosyaAdlari=null;
    private File file = null;

    public CustomAdapter(Activity activity, String[] dosyaAdlari, File file){
        //XML'i alıp View'a çevirecek inflater'ı örnekleyelim
        layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.dosyaAdlari=dosyaAdlari;
        this.file = file;
    }

    @Override
    public int getCount() {
        return dosyaAdlari.length;
    }

    @Override
    public String getItem(int position) {
        return dosyaAdlari[position].toString();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        View view;
        view = layoutInflater.inflate(R.layout.listview_blacklist, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        TextView textView = (TextView) view.findViewById(R.id.textViewNames);
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.imageButtonKullan);
        String string = dosyaAdlari[position].toString();
        textView.setText(string);

        imageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String deleteFileName = dosyaAdlari[position];
                String directoryPath = file.getAbsolutePath();
                String deleteFilePath=directoryPath+"/"+deleteFileName;
                File deleteFile = new File(deleteFilePath);
                dialog = getDeleteDialog(deleteFile);
                dialog.show();
            }
        });
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public Dialog getDeleteDialog (final File fileDel){
        View view = layoutInflater.inflate(R.layout.delete_blacklist_dialog, null);
        Button buttonDelete = (Button)view.findViewById(R.id.buttonDeleteBlackListDeleteDialog);
        Button buttonCancel = (Button)view.findViewById(R.id.buttonCancelBlackListDeleteDialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setView(view);
        builder.setCancelable(true);

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileDel.delete();
                dosyaAdlari=file.list();
                Arrays.sort(dosyaAdlari);
                notifyDataSetChanged();
                dialog.dismiss();
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
}


