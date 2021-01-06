package fun.flexpad.com;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

import fun.flexpad.com.Adapters.VoiceAdapter;
import fun.flexpad.com.Fragments.APIService;
import fun.flexpad.com.Fragments.RoomAPIService;
import fun.flexpad.com.Model.Room;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.Model.Voice;
import fun.flexpad.com.Notifications.Client;
import fun.flexpad.com.Notifications.Data;
import fun.flexpad.com.Notifications.MyResponse;
import fun.flexpad.com.Notifications.RoomData;
import fun.flexpad.com.Notifications.RoomSender;
import fun.flexpad.com.Notifications.Sender;
import fun.flexpad.com.Notifications.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomChatActivity extends AppCompatActivity {

    ImageButton mic_live, mic_live_on, btnSend, btnRecording;
    private MediaRecorder mediaRecorder;
    //    private MediaPlayer mediaPlayer;
    private String fileName = "";
    MaterialTextView cancelRecord;
    private static final String LOG_TAG = "Record_log";
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    private Chronometer recordTime;
    private boolean running;

    final int REQUEST_PERMISSION_CODE = 1000;
    TextView roomTextview, liveTextFromSpeech;
    FirebaseUser firebaseUser;
    RecyclerView recyclerVoice;
    VoiceAdapter voiceAdapter;
    List<Voice> mVoice;
    long record_start_time, record_end_time;
    ValueEventListener seenListener, showVoiceListener, sendNotifListener;
    ValueEventListener sendListener;
//    RoomAPIService roomAPIService;
    APIService roomAPIService;
    boolean notify = false;
    DatabaseReference openedRef, v_reference;
    DatabaseReference tokens, referenceNotify;
    StorageReference filePath;
    DatabaseReference voiceRef;
    String roomId, roomTitle;

//    static {
//        System.loadLibrary("cpp_code");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_chat);

//        roomAPIService = Client.getClient("https://fcm.googleapis.com/").create(RoomAPIService.class);
        roomAPIService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerVoice = findViewById(R.id.recycler_voice);
        recyclerVoice.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerVoice.setLayoutManager(linearLayoutManager);

        // Example of a call to a native method --C++
        TextView tv = findViewById(R.id.sample_text);
        tv.setText("Click for random number");
        tv.setOnClickListener(v -> {
            Random r = new Random();
            int lowRange = 0;
            int highRange = 99;
            int result = r.nextInt(highRange - lowRange );
            tv.setText(Integer.toString(result));
        });

        roomTitle = getIntent().getStringExtra("Room_Name");
        roomTextview = findViewById(R.id.room_title);
        roomTextview.setText(roomTitle);
//        final String randomRoomTitle = getIntent().getStringExtra("Random_Room_Name");
//        roomTextview.setText(randomRoomTitle);
        roomId = getIntent().getStringExtra("Room_ID"); ////////////////////////////////////////////////

        mStorage = FirebaseStorage.getInstance().getReference("Room Audio Clips");
        mProgress = new ProgressDialog(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            //might cause app to crash;
//            startActivity(new Intent(RoomChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            navigateUpTo(new Intent(RoomChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        });

        liveTextFromSpeech = findViewById(R.id.live_spoken_text);
        mic_live = findViewById(R.id.mic_live);
        mic_live_on = findViewById(R.id.mic_live_on);
        btnSend = findViewById(R.id.btn_send);
        btnRecording = findViewById(R.id.record_blink);
        cancelRecord = findViewById(R.id.cancel_record);
        recordTime = findViewById(R.id.chronometer); //recordTime.setFormat("Time: %s");
//        mediaPlayer = new MediaPlayer();

        showVoices(roomId);
        recordVoice();
        seenMessage(roomId);
    }

    public native String stringFromJNI();

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(fileName);
    }

    private void manageBlinkEffect() {
//        ObjectAnimator animator = ObjectAnimator.ofInt(btnRecording, "src",
//                R.id.record_blink, R.color.colorWhite, R.id.record_blink);
//        animator.setDuration(750);
//        animator.setEvaluator(new ArgbEvaluator());
//        animator.setRepeatMode(ValueAnimator.REVERSE);
//        animator.setRepeatCount(Animation.INFINITE);
//        animator.start();
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
        navigateUpTo(new Intent(RoomChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void recordVoice() {

        mic_live.setOnClickListener(view -> {



            try {
                if(checkPermissionFromDevice()){
                    fileName = Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + "/"
                            + UUID.randomUUID().toString() + "_audio_record.3gp";
                    setupMediaRecorder();

                    try { mediaRecorder.prepare();
                    } catch (IOException e){ e.printStackTrace(); }
                    mediaRecorder.start();
                    mic_live_on.setVisibility(View.VISIBLE);
                    btnSend.setVisibility(View.VISIBLE);
                    cancelRecord.setVisibility(View.VISIBLE);
                    btnRecording.setVisibility(View.VISIBLE);
                    manageBlinkEffect();

                    if (!running) {
                        recordTime.setBase(SystemClock.elapsedRealtime());
                        recordTime.start();
                        running = true;
                    }
                    recordTime.setVisibility(View.VISIBLE);

                    Toast.makeText(RoomChatActivity.this, "Recording started...", Toast.LENGTH_SHORT).show();
                }
                else{
                    requestPermission();
                }
            } catch (Exception e) {e.printStackTrace();}

            record_start_time = Calendar.getInstance().getTimeInMillis();
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
            cancelRecord.setVisibility(View.INVISIBLE);
            btnRecording.setVisibility(View.INVISIBLE);

            if (running) {
                recordTime.stop();
                running = false;
            }
            recordTime.setVisibility(View.INVISIBLE);
        });

        btnSend.setOnClickListener(vSend -> {
            notify = true;
            try{
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            mic_live_on.setVisibility(View.INVISIBLE);
            btnSend.setVisibility(View.INVISIBLE);
            cancelRecord.setVisibility(View.INVISIBLE);
            btnRecording.setVisibility(View.INVISIBLE);

            if (running) {
                recordTime.stop();
                running = false;
            }
            recordTime.setVisibility(View.INVISIBLE);

            mProgress.setMessage("Sending...");
            mProgress.show();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

//            final String roomTitle = getIntent().getStringExtra("Room_Name");
            String messagelabel = firebaseUser.getUid() + roomTitle;
//            final String roomId = getIntent().getStringExtra("Room_ID"); ////////////////////////////////////////

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss, dd MMM, yyyy");
            Calendar currentCal = Calendar.getInstance();
            final String sendingTime = dateFormat.format(currentCal.getTime());

            record_end_time = Calendar.getInstance().getTimeInMillis();
            SimpleDateFormat date4mat = new SimpleDateFormat("mm:ss");

            final Uri uri = Uri.fromFile(new File(fileName));
            filePath = mStorage.child(uri.getLastPathSegment());
//            StorageReference filePath = mStorage.child(sendingTime + messagelabel + "_clip.3gp");

            filePath.putFile(uri).continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUrl = task.getResult();
                        assert downloadUrl != null;
                        final String voiceUrl = downloadUrl.toString();

                        final DatabaseReference voice_reference = reference.child("VoiceClips").push();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", firebaseUser.getUid());
                        hashMap.put("roomname", roomTitle);
                        hashMap.put("roomID", roomId);
                        hashMap.put("time", sendingTime);
                        hashMap.put("messagelabel", messagelabel);
                        hashMap.put("type", "audio (3gp)");
                        hashMap.put("message", voiceUrl); //or leave as myUrl
                        hashMap.put("name", uri.getLastPathSegment());
                        hashMap.put("messagekey", voice_reference.getKey());
                        hashMap.put("duration", date4mat.format(record_end_time - record_start_time));
                        voice_reference.setValue(hashMap);

                        voiceRef = FirebaseDatabase.getInstance().getReference("RoomVoiceList")
                                .child(firebaseUser.getUid()).child(roomTitle);

                        voiceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    voiceRef.child("roomId").setValue(roomId);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        mProgress.dismiss();

                        referenceNotify = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        sendNotifListener = referenceNotify.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                if (notify) {

//                                final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    DatabaseReference followReference = FirebaseDatabase.getInstance().getReference();
                                    followReference.child("FollowList").child(firebaseUser.getUid()).child("followers")
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    ArrayList<String> followersList = new ArrayList<>();
                                                    followersList.clear();
                                                    for (DataSnapshot dataSnapshot2 : snapshot.getChildren()) {
                                                        String follower = dataSnapshot2.getKey();
                                                        followersList.add(follower);
                                                        sendNotification(follower, roomId, user.getUsername() + " [ROOM -> " + roomTitle + "]", "Duration: " + date4mat.format(record_end_time - record_start_time), follower);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                }
                                notify = false;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        reference.child("Rooms").child(roomId).child("lastMessageTime").setValue(sendingTime);
                        reference.child("Rooms").child(roomId).child("lastMsgTimeStamp").setValue(System.currentTimeMillis());
                    }
                }
            });
        });
    }

    private void sendNotification(String receiver, String roomid, final String username, final String message_time, String userid) {
        tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
//                    RoomData roomData = new RoomData(firebaseUser.getUid(), R.mipmap.flexpad_fourth_actual_icon, message_time, username, userid);// roomid,
//
//                    RoomSender roomSender = new RoomSender(roomData, token.getToken());
//
//                    roomAPIService.sendNotification(roomSender)

                    Data data = new Data(firebaseUser.getUid(), roomid, roomTitle, R.mipmap.flexpad_fourth_actual_icon_foreground, message_time, username, userid, "roomNotification");//

                    Sender sender = new Sender(data, token.getToken());

                    roomAPIService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1) {
//                                            Toast.makeText(RoomChatActivity.this, "Notification Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
//                                        Toast.makeText(RoomChatActivity.this, "Notification Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
//                                    Toast.makeText(RoomChatActivity.this, "Notification Failed!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage(final String roomID) {
        openedRef = FirebaseDatabase.getInstance().getReference("VoiceClips");
        seenListener = openedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Voice voice = snapshot.getValue(Voice.class);
                    assert voice != null;
                    if ((voice.getRoomID() != null) && voice.getRoomID().equals(roomID)) {
                        openedRef.child(voice.getMessagekey()).child("seenBy").child(firebaseUser.getUid()).setValue(true);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showVoices(final String roomID) {
        mVoice = new ArrayList<>();

        v_reference = FirebaseDatabase.getInstance().getReference("VoiceClips");
        showVoiceListener = v_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mVoice.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    try {
                        Voice voice = snapshot.getValue(Voice.class);
                        assert voice != null;
                        if ((voice.getRoomID() != null) && voice.getRoomID().equals(roomID)){
                            mVoice.add(voice);
                        }
                    } catch (Exception e) {e.printStackTrace();}
                    voiceAdapter = new VoiceAdapter (RoomChatActivity.this, mVoice);
                    recyclerVoice.setAdapter(voiceAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (mediaPlayer.isPlaying()) mediaPlayer.pause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        openedRef.removeEventListener(seenListener);
        v_reference.removeEventListener(showVoiceListener);
//        referenceNotify.removeEventListener(sendNotifListener);
//        voiceRef.removeValue();
    }
}

