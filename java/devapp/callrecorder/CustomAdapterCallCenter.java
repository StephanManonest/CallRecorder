package devapp.callrecorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.StreamHandler;

public class CustomAdapterCallCenter extends BaseAdapter {

    Dialog dialog = null;
    String noteFilePath = null;


    private LayoutInflater layoutInflater = null;
    private String[] fileNames = null;
    private File directory = null;
    Context context = null;
    File noteDirectory = null;

    public CustomAdapterCallCenter(Activity activity, String[] fileNames, File directory, Context context, File noteDirectory) {
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.fileNames = fileNames;
        this.directory = directory;
        this.context = context;
        this.noteDirectory = noteDirectory;
    }

    @Override
    public int getCount() {
        return fileNames.length;
    }

    @Override
    public String getItem(int position) {
        return fileNames[position].toString();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        final View rowView;
        rowView = layoutInflater.inflate(R.layout.listview_call_center, null);
        RelativeLayout relativeLayout = (RelativeLayout) rowView.findViewById(R.id.relativeLayoutCallCenter);
        final Spinner spinner = (Spinner) rowView.findViewById(R.id.spinnerListViewCallCenter);
        String[] spinnerContent = {fileNames[position], "Play", "Note", "Delete"};
        ArrayAdapter<CharSequence> adapterSpinner = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_expandable_list_item_1, spinnerContent);
        spinner.setAdapter(adapterSpinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int positionItem, long id) {

                spinner.setSelection(0);
                File fileProcess = new File(directory.getPath() + "/" + fileNames[position]);

                switch (positionItem) {
                    case 1:
                        play(fileProcess);
                        break;
                    case 2:
                        dialog = getNoteDialog(fileNames[position]);
                        dialog.show();
                        break;
                    case 3:
                        dialog = getDeleteDialog(fileProcess);
                        dialog.show();
                        break;
                    default:
                        return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return rowView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void play(File file) {
        Uri uri = Uri.fromFile(file);
        Intent intentPlay = new Intent(Intent.ACTION_VIEW, uri);
        intentPlay.setDataAndType(uri, "audio/*");
        intentPlay.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentPlay);
    }

    public Dialog getNoteDialog(String fileName) {
        View view = layoutInflater.inflate(R.layout.not_dialog, null);
        final EditText editText = (EditText)view.findViewById(R.id.editTextNoteDialog);
        Button buttonSave = (Button) view.findViewById(R.id.buttonSaveNoteDialog);
        Button buttonDelete = (Button) view.findViewById(R.id.buttonDeleteNoteDialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setView(view);
        builder.setCancelable(true);
        noteFilePath = noteDirectory.getPath();
        noteFilePath += "/" +fileName +".txt";
        File controlFile = new File(noteFilePath);
        if (controlFile.exists()){
            editText.setText(readNote(noteFilePath));
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userInput = editText.getText().toString();
                writeNote(noteFilePath, userInput);
                dialog.dismiss();
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote(noteFilePath);
                editText.setText("");
            }
        });
        return builder.create();
    }

    public void deleteNote(String noteFilePath){
        File file = new File(noteFilePath);
        if (file.exists()){
            file.delete();
        }
    }

    public String readNote (String noteFilePath) {
        String outString = null;
        try {
            FileReader fileReader = new FileReader(noteFilePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String bufferRead = "";
            outString = "";
            while ((bufferRead = bufferedReader.readLine()) != null) {
                outString += bufferRead + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outString;
    }

    public void writeNote (String noteFilePath, String text){
        try {
            FileWriter writer = new FileWriter(noteFilePath);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(text);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Dialog getDeleteDialog (final File fileDel){
        View view = layoutInflater.inflate(R.layout.delete_record_dialog, null);
        Button buttonDelete = (Button)view.findViewById(R.id.buttonDeleteDeleteDialog);
        Button buttonCancel = (Button)view.findViewById(R.id.buttonCancelDeleteDialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setView(view);
        builder.setCancelable(true);

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileDel.delete();
                fileNames = directory.list();
                Arrays.sort(fileNames);
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