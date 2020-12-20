package fun.flexpad.com;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
//import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import fun.flexpad.com.Adapters.MessageAdapter;
import fun.flexpad.com.Fragments.APIService;
import fun.flexpad.com.Model.Chat;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.Notifications.Client;
import fun.flexpad.com.Notifications.Data;
import fun.flexpad.com.Notifications.MyResponse;
import fun.flexpad.com.Notifications.Sender;
import fun.flexpad.com.Notifications.Token;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image, verification_image;
    TextView username;
    TextView userstatus;
    FirebaseUser fuser;
    DatabaseReference reference;

    ImageButton btn_send, btn_send_files;
    EditText text_send;
    MessageAdapter messageAdapter;
    List<Chat> mChat;
    RecyclerView recyclerView;

    String userid;
    Intent intent;
    ValueEventListener seenListener;
    APIService apiService;

    private String checker = "", myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;
    boolean notify = false;

    int message_number = 0;
    int image_message_number = 0;
    int doc_message_number = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        String textFromMessageToMainActivity = "messageToMain";

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            //might cause app to crash;
            Intent intent = new Intent(MessageActivity.this, MainActivity.class);
            intent.putExtra("textFromMessageToMainActivity", textFromMessageToMainActivity);
            startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        verification_image = findViewById(R.id.verification_image);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send); // text_send here is the EditText box, supposed to be "type_message" or so..
        username = findViewById(R.id.username);
        userstatus = findViewById(R.id.userstatus);
        btn_send_files = findViewById(R.id.files_btn_send);

        loadingBar = new ProgressDialog(this);

        intent = getIntent();
        final String userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        btn_send.setOnClickListener(v -> {
            notify = true;
            String msg = text_send.getText().toString();

            if (!msg.trim().isEmpty()){
                sendMessage(fuser.getUid(), userid, msg);
            } else {
                Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
            }
            text_send.setText("");
        });

        text_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    checkTypingStatus("noOne");
                } else {
                    checkTypingStatus(fuser.getUid()); //uid of receiver
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_send_files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] options = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                builder.setTitle("Select File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if  (which == 0){
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }
                        if  (which == 1){
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);
                        }
                        if  (which == 2){
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select Ms Word File"), 438);
                        }
                    }
                });
                builder.show();
            }
        });

        assert userid != null;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                String status = "" + dataSnapshot.child("status").getValue();
                String typingStatus = "" + dataSnapshot.child("typingTo").getValue();

                assert user != null;
                username.setText(user.getUsername());

                // might honestly have f***ed this up (logically), not clean enough for my liking.
                if (typingStatus.equals("noOne")) {
                    if (status.equals("online")) {
                        userstatus.setText("is online.."); //user.getStatus()
                    } else {
                        //convert timestamp to proper time date: last seen @... (maybe,..hmm)
                        userstatus.setText("");
                    }
                } else {
                    userstatus.setText("is typing..");
                }

                if (user.getImageURI().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURI()).into(profile_image);
                }
                readMessages(fuser.getUid(), userid, user.getImageURI());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenMessage(userid);
        showVerification(userid);
    }

    //for selecting file from storage
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Sending..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (!checker.equals("image")){

                // sending document
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files"); //create separate document folder

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                final String userid = intent.getStringExtra("userid");

                reference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) { //just added @NotNull..check if issues
                        doc_message_number++;
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {}
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {}
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError){}
                });

                String message_num = Integer.toString(doc_message_number);
                String messagelabel = fuser.getUid() + userid + message_num;

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,dd.MM.yyyy");
                Calendar currentCal = Calendar.getInstance();
                final String currentTime = dateFormat.format(currentCal.getTime());

                //supposed to generate random keys to prevent replacement instead of fixed userid
                assert userid != null;
                StorageReference filePath = storageReference.child(fileUri.getLastPathSegment());
//                StorageReference filePath = storageReference.child(currentTime + messagelabel + "." + checker);

                filePath.putFile(fileUri).continueWithTask((Continuation) task -> {
                    if (!task.isSuccessful()){
                        throw Objects.requireNonNull(task.getException());
                    }

                    return filePath.getDownloadUrl();
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            final Uri downloadUrl = task.getResult();
                            assert downloadUrl != null;
                            final String docUrl = downloadUrl.toString();

                            final DatabaseReference message_reference = reference.child("Chats").push();

                            HashMap<String, Object> hashMap = new HashMap<>();

                            hashMap.put("sender", fuser.getUid());
                            hashMap.put("receiver", userid);
                            hashMap.put("messagelabel", messagelabel);
                            hashMap.put("type", checker);
                            hashMap.put("message", docUrl); //or leave as myUrl
                            hashMap.put("name", fileUri.getLastPathSegment());
                            hashMap.put("isseen", false);
                            hashMap.put("messagekey", message_reference.getKey());
                            message_reference.setValue(hashMap);

                            final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(fuser.getUid())
                                    .child(userid);

                            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        chatRef.child("id").setValue(userid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            final DatabaseReference chatSenderRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(userid)
                                    .child(fuser.getUid());

                            chatSenderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        chatSenderRef.child("id").setValue(fuser.getUid());
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            loadingBar.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
//                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//                        double p = (100.0*taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                        loadingBar.setMessage((int) p + "%  Uploading..."); //..also allow download in Message Adapter..
//
//                    }
//                })
                ;


            } else if (checker.equals("image")){ //sending image
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                final String userid = intent.getStringExtra("userid");

                reference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) { //just added @NotNull..check if issues
                        image_message_number++;
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {}
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {}
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError){}
                });

                String message_num = Integer.toString(image_message_number);
                String messagelabel = fuser.getUid() + userid + message_num;

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,dd.MM.yyyy");
                Calendar currentCal = Calendar.getInstance();
                final String currentTime = dateFormat.format(currentCal.getTime());

                //supposed to generate random keys to prevent replacement instead of fixed userid
                assert userid != null;
                StorageReference filePath = storageReference.child(fileUri.getLastPathSegment());
//                StorageReference filePath = storageReference.child(currentTime + messagelabel + "." + "jpg"); //or say "." + checker

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask((Continuation) task -> {
                    if (!task.isSuccessful()){
                        throw Objects.requireNonNull(task.getException());
                    }

                    return filePath.getDownloadUrl();
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            assert downloadUrl != null;
                            myUrl = downloadUrl.toString();

                            loadingBar.dismiss();
                            final DatabaseReference message_reference = reference.child("Chats").push();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", fuser.getUid());//check, sender might be userid, or not..probably is though
                            hashMap.put("receiver", userid);
                            hashMap.put("messagelabel", messagelabel);
                            hashMap.put("type", checker);
                            hashMap.put("message", myUrl);
                            hashMap.put("name", fileUri.getLastPathSegment());
                            hashMap.put("isseen", false);
                            hashMap.put("messagekey", message_reference.getKey());

                            message_reference.setValue(hashMap);

                            final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(fuser.getUid())
                                    .child(userid);

                            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        chatRef.child("id").setValue(userid);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            final DatabaseReference chatSenderRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(userid)
                                    .child(fuser.getUid());

                            chatSenderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        chatSenderRef.child("id").setValue(fuser.getUid());
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            loadingBar.dismiss();
                        }
                    }
                });

            } else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected!", Toast.LENGTH_SHORT).show();
            }
        }
    }//

    // for message seen
    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // for sent messages
    private void sendMessage(String sender, final String receiver, String message) {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final String userid = intent.getStringExtra("userid");

        assert userid != null;
        //reference.child("Chats").child(fuser.getUid()).child(userid).push(); // comment this line maybe

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) { //just added @NotNull..check if issues
                message_number++;
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){}
        });

        String message_num = Integer.toString(message_number);
        String messagelabel = sender + message_num + receiver; //Still needs a fix, message_number starts from zero at every app restart..

        DatabaseReference message_reference = reference.child("Chats").push();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("messagelabel", messagelabel);
        hashMap.put("type", "text");
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("messagekey", message_reference.getKey());

        message_reference.setValue(hashMap);

        //assert userid != null;

        //might need to add this line to onActivityResult
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatSenderRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());

        chatSenderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatSenderRef.child("id").setValue(fuser.getUid());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String msg = message;

        final DatabaseReference referenceNotify = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        referenceNotify.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //to send notifications (not working yet)
    private void sendNotification(String receiver, final String username, final String message) {

        final FirebaseUser notiFuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    //String token = snapshot.getValue(String.class);
                    Data data = new Data(notiFuser.getUid(), R.mipmap.flexpad_fourth_actual_icon, username + ": " + message, "New Message", userid);

                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    //Sender sender = new Sender(data, token);

                    // I don't even fully yet grasp how the f*** this apiService works exactly
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1){
                                    Toast.makeText(MessageActivity.this, "Notification Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //for read messages
    private void readMessages(final String myid, final String userid, final String imageuri){
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mChat.add(chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageuri);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showVerification (String chatReceiver) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(chatReceiver);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                if (user.getVerified().equals("true")) {verification_image.setVisibility(View.VISIBLE);}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // ..fix current user identity, I think
    private void currentUser (String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    //online or offline Status
    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        //hashMap.put("onlineStatus", status);
        reference.updateChildren(hashMap);
    }

    // for typing (a bit messy though)
    private void checkTypingStatus (String typing) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (seenListener != null && reference != null) {
            reference.removeEventListener(seenListener);
            status("offline");
            checkTypingStatus("noOne");
            currentUser("none");
        }
    }
}

// you can't see image picture yet because i had issues making the view gone,
// and also issues displaying picture, even though messages are actually sent, as shown in firebase.


