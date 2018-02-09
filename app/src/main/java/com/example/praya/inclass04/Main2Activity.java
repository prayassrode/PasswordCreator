package com.example.praya.inclass04;
/*
InClass04
Prayas Rode and Jacob Stern
*/
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main2Activity extends AppCompatActivity {
    Handler handler;
    static final int STATUS_START = 0x00;
    static final int STATUS_STOP = 0x01;
    static final int STATUS_PROGRESS = 0x02;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        final SeekBar seekBarCount = findViewById(R.id.passwordCountSeekBar);
        seekBarCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView passwordCount = findViewById(R.id.passwordCount);
                passwordCount.setText(seekBar.getProgress()+ 1 +"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final SeekBar seekBarLength = findViewById(R.id.passwordLengthSeekbar);
        seekBarLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView passwordLength = findViewById(R.id.passwordLength);
                passwordLength.setText(seekBar.getProgress()+8+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Generating passwords");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what){
                    case STATUS_START:
                        progressDialog.setMax(seekBarCount.getProgress()+1);
                        progressDialog.show();
                        progressDialog.setProgress(0);
                        break;
                    case STATUS_PROGRESS:
                        progressDialog.incrementProgressBy(1);
                        break;
                    case STATUS_STOP:
                        progressDialog.dismiss();
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
                        builder.setTitle("Passwords");
                        ArrayList<String> pw = (ArrayList<String>) message.obj;
                        final String[] passwords = pw.toArray(new String[pw.size()]);
                        builder.setItems(passwords, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                TextView password = findViewById(R.id.password);
                                password.setText(passwords[i]);

                            }
                        });
                        builder.create().show();

                        break;
                }
                return false;
            }
        });

        Button generatePasswordThread = findViewById(R.id.generatePasswordThreadButton);
        generatePasswordThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExecutorService threadPool = Executors.newFixedThreadPool(2);
                threadPool.execute(new Runnable() {

                    @Override
                    public void run() {
                        ArrayList<String> passwords = new ArrayList<>();
                        Message message = new Message();
                        message.what = STATUS_START;
                        handler.sendMessage(message);
                        for (int i=0; i < seekBarCount.getProgress()+1; i++){
                            passwords.add(Util.getPassword(seekBarLength.getProgress() + 8));
                            Message progress = new Message();
                            progress.what = STATUS_PROGRESS;
                            progress.obj = i+1;
                            handler.sendMessage(progress);
                        }
                        Message finalMessage = new Message();
                        finalMessage.what = STATUS_STOP;
                        finalMessage.obj = passwords;
                        handler.sendMessage(finalMessage);
                    }
                });

            }
        });
        Button passwordAsync = findViewById(R.id.generatePasswordAsyncButton);
        passwordAsync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DoWork(seekBarCount.getProgress()+1).execute(seekBarLength.getProgress()+8);
            }
        });


    }

    class DoWork extends AsyncTask<Integer, Integer, ArrayList<String>>{
        int count;
        ProgressDialog progressDialog;

        public DoWork(int count) {
            this.count = count;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(Main2Activity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Generating passwords");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(count);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            progressDialog.dismiss();
            final AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
            builder.setTitle("Passwords");
            final String[] passwords = s.toArray(new String[s.size()]);
            builder.setItems(passwords, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    TextView password = findViewById(R.id.password);
                    password.setText(passwords[i]);
                }
            });
            builder.create().show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.incrementProgressBy(1);
        }

        @Override
        protected ArrayList<String> doInBackground(Integer... integers) {
            ArrayList<String> passwords = new ArrayList<>();
            for (int i = 0; i < count; i++){
                passwords.add(Util.getPassword(integers[0]));
                publishProgress();
            }
            return passwords;
        }
    }




}
