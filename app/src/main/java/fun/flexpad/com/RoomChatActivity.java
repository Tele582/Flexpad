package fun.flexpad.com;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class RoomChatActivity extends AppCompatActivity {

    Button mic_live;
    MediaRecorder mediaRecorder;
    String pathSave = "";

    final int REQUEST_PERMISSION_CODE = 1000;
    TextView roomTextview;

    static {
        System.loadLibrary("cpp_code");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_chat);

        // Example of a call to a native method --C++
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        final String roomtitle = getIntent().getStringExtra("room_key");
        roomTextview = findViewById(R.id.room_title);
        roomTextview.setText(roomtitle);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            //might cause app to crash;
            startActivity(new Intent(RoomChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        mic_live = findViewById(R.id.mic_live);

        mic_live.setOnClickListener(view -> {
            if(checkPermissionFromDevice()){
                pathSave = Environment.getExternalStorageDirectory()
                        .getAbsolutePath()+"/"
                        + UUID.randomUUID().toString()+"_audio_record.3gp";
                setupMediaRecorder();
                //mediaRecorder = new MediaRecorder();
                try{
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e){
                    e.printStackTrace();
                }
                //btnPlay.setEnabled(false);
                //btnStop.setEnabled(false);
                //btnStopRecord.setEnabled(true);

                Toast.makeText(RoomChatActivity.this, "Recording...", Toast.LENGTH_SHORT).show();
            }
            else{
                requestPermission();
            }
        });
    }

    public native String stringFromJNI();

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSave);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO

        }, REQUEST_PERMISSION_CODE);
    }

    //Press Ctrl+O

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermissionFromDevice(){
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

}



