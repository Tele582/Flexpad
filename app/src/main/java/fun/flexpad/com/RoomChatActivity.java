package fun.flexpad.com;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import fun.flexpad.com.Adapter.RoomAdapter;
import fun.flexpad.com.Adapter.VoiceAdapter;
import fun.flexpad.com.Model.Room;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.Model.Voice;

public class RoomChatActivity extends AppCompatActivity {

    ImageButton mic_live, mic_live_on, btnSend, btnRecording;
    private MediaRecorder mediaRecorder;
    private String fileName = "";
    MaterialTextView recordTime, cancelRecord;
    private static final String LOG_TAG = "Record_log";
    private StorageReference mStorage;
    private ProgressDialog mProgress;

    final int REQUEST_PERMISSION_CODE = 1000;
    TextView roomTextview;
    FirebaseUser firebaseUser;
    RecyclerView recyclerVoice;
    VoiceAdapter voiceAdapter;
    List<Voice> mVoice;

    static {
        System.loadLibrary("cpp_code");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_chat);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerVoice = findViewById(R.id.recycler_voice);
        recyclerVoice.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerVoice.setLayoutManager(linearLayoutManager);

        // Example of a call to a native method --C++
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        tv.setOnClickListener(v -> {
            Random r = new Random();
            int lowRange = 0;
            int highRange = 99;
            int result = r.nextInt(highRange - lowRange );
            tv.setText(Integer.toString(result));
        });

        final String roomtitle = getIntent().getStringExtra("Room_Name");
        roomTextview = findViewById(R.id.room_title);
        roomTextview.setText(roomtitle);
//        final String randomRoomTitle = getIntent().getStringExtra("Random_Room_Name");
//        roomTextview.setText(randomRoomTitle);
        final String roomId = getIntent().getStringExtra("Room_ID");

        mStorage = FirebaseStorage.getInstance().getReference("Room Audio Clips");
        mProgress = new ProgressDialog(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            //might cause app to crash;
            startActivity(new Intent(RoomChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        mic_live = findViewById(R.id.mic_live);
        mic_live_on = findViewById(R.id.mic_live_on);
        btnSend = findViewById(R.id.btn_send);
        btnRecording = findViewById(R.id.record_blink);
        recordTime = findViewById(R.id.record_time);
        cancelRecord = findViewById(R.id.cancel_record);


        playVoices(firebaseUser.getUid(), roomId);
        recordVoice();
    }

    public native String stringFromJNI();

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(fileName);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO

        }, REQUEST_PERMISSION_CODE);
    }

    //Ctrl+O

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RoomChatActivity.this, MainActivity.class));
    }

    public void recordVoice() {
        mic_live.setOnClickListener(view -> {
            if(checkPermissionFromDevice()){
                fileName = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/"
                        + UUID.randomUUID().toString() + "_audio_record.3gp";
                setupMediaRecorder();

                try{
                    mediaRecorder.prepare();
                } catch (IOException e){
                    e.printStackTrace();
                }
                mediaRecorder.start();
                mic_live_on.setVisibility(View.VISIBLE);
                btnSend.setVisibility(View.VISIBLE);
                btnRecording.setVisibility(View.VISIBLE);
                recordTime.setVisibility(View.VISIBLE);
                cancelRecord.setVisibility(View.VISIBLE);

                Toast.makeText(RoomChatActivity.this, "Recording started...", Toast.LENGTH_SHORT).show();
            }
            else{
                requestPermission();
            }
        });

        cancelRecord.setOnClickListener(v -> {
            try{
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(RoomChatActivity.this, "Recording stopped!", Toast.LENGTH_SHORT).show();
            mic_live_on.setVisibility(View.INVISIBLE);
            btnSend.setVisibility(View.INVISIBLE);
            btnRecording.setVisibility(View.INVISIBLE);
            recordTime.setVisibility(View.INVISIBLE);
            cancelRecord.setVisibility(View.INVISIBLE);
        });

        btnSend.setOnClickListener(vSend -> {
            try{
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            mic_live_on.setVisibility(View.INVISIBLE);
            btnSend.setVisibility(View.INVISIBLE);
            btnRecording.setVisibility(View.INVISIBLE);
            recordTime.setVisibility(View.INVISIBLE);
            cancelRecord.setVisibility(View.INVISIBLE);

            mProgress.setMessage("Sending...");
            mProgress.show();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            final String roomTitle = getIntent().getStringExtra("Room_Name");
            String messagelabel = firebaseUser.getUid() + roomTitle;
            final String roomId = getIntent().getStringExtra("Room_ID");

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,dd.MM.yyyy");
            Calendar currentCal = Calendar.getInstance();
            final String currentTime = dateFormat.format(currentCal.getTime());

            StorageReference filePath = mStorage.child(currentTime + messagelabel + "_clip.3gp"); // = uri.getLastPathSegment()..ish;

            Uri uri = Uri.fromFile(new File(fileName));
            filePath.putFile(uri).addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> {

                final DatabaseReference voice_reference = reference.child("VoiceClips").push();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", firebaseUser.getUid());
                hashMap.put("roomname", roomTitle);
                hashMap.put("roomID", roomId);
                hashMap.put("time", currentTime);
                hashMap.put("messagelabel", messagelabel);
                hashMap.put("type", "audio(3gp)");
                hashMap.put("message", Objects.requireNonNull(taskSnapshot).toString()); //or leave as myUrl
                hashMap.put("name", uri.getLastPathSegment());
                hashMap.put("messagekey", voice_reference.getKey());
                voice_reference.setValue(hashMap);

                final DatabaseReference voiceRef = FirebaseDatabase.getInstance().getReference("RoomVoiceList")
                        .child(firebaseUser.getUid()).child(roomTitle);

                voiceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            voiceRef.child("roomId").setValue(roomId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mProgress.dismiss();
            });
        });
    }

    private void playVoices(final String myId, final String roomID){
        mVoice = new ArrayList<>();

        final DatabaseReference v_reference = FirebaseDatabase.getInstance().getReference("VoiceClips");
        v_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mVoice.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Voice voice = snapshot.getValue(Voice.class);
                    assert voice != null;
                    if ((voice.getRoomID() != null) && voice.getRoomID().equals(roomID)){
                            mVoice.add(voice);
                    }
                    voiceAdapter = new VoiceAdapter (RoomChatActivity.this, mVoice);
                    recyclerVoice.setAdapter(voiceAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}



