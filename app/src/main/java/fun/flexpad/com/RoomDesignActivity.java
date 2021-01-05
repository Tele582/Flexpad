package fun.flexpad.com;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import fun.flexpad.com.Model.Room;
import fun.flexpad.com.Model.User;

public class RoomDesignActivity extends AppCompatActivity {

    private CircleImageView room_image;
    private EditText room_name;
    private Button createBtn;
    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_design);

        final Toolbar toolbar = findViewById(R.id.room_design_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Design and Create Room");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            //might cause app to crash;
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        createBtn = findViewById(R.id.create_btn);
        room_name = findViewById(R.id.room_name);
        //room_image = findViewById(R.id.room_image);

        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("room_uploads");
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                createBtn.setOnClickListener(view -> {
                    final User user = snapshot.getValue(User.class);
                    final String room = room_name.getText().toString();
                    if (!room.trim().isEmpty()){
                        saveroom(room, user.getId(), user.getUsername(), user.getVerified());
                    } else {
                        Toast.makeText(RoomDesignActivity.this, "Can't create room with no name.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*reference = FirebaseDatabase.getInstance().getReference().child("Rooms");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //User user = dataSnapshot.getValue(User.class);
                Room room = dataSnapshot.getValue(Room.class);
                //assert user != null;
                assert room != null;
                //username.setText(user.getUsername());
                if (room.getImageURI().equals("default")){
                    room_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    //if (isAdded()) {
                        Glide.with(RoomDesignActivity.this).load(room.getImageURI()).into(room_image);
                    //}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        //room_image.setOnClickListener(v -> openImage());
    }

//    Create and save room
    public void saveroom(String roomname, String creatorId, String creatorName, String creatorVerified) {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference room_reference = reference.child("Rooms").push();
        String roomKey = room_reference.getKey();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss, dd MMM, yyyy");
        Calendar currentCal = Calendar.getInstance();
        final String timeCreated = dateFormat.format(currentCal.getTime());

        HashMap<String, Object> nmap = new HashMap<>();
        nmap.put("roomname", roomname);
        nmap.put("creator", creatorId);
        nmap.put("creatorUsername", creatorName);
        nmap.put("roomKey", roomKey);
        nmap.put("creatorVerified", creatorVerified);
        nmap.put("dateCreated", timeCreated);
        nmap.put("lastMessageTime", timeCreated);
        nmap.put("lastMsgTimeStamp", System.currentTimeMillis());
        room_reference.setValue(nmap);

        final Intent intent = new Intent(RoomDesignActivity.this, RoomChatActivity.class);
        intent.putExtra("Room_Name", roomname);
        intent.putExtra("Room_ID", roomKey);
        Toast.makeText(RoomDesignActivity.this, "Creating room..", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    /*private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = RoomDesignActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.show();

        if (imageUri != null){
            final StorageReference fileRef = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    assert downloadUri != null;
                    String mUri = downloadUri.toString();

                    reference = FirebaseDatabase.getInstance().getReference("Rooms").child(fuser.getUid());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageURI", mUri);
                    reference.updateChildren(map);

                    pd.dismiss();
                } else {
                    Toast.makeText(RoomDesignActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(RoomDesignActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        } else {
            Toast.makeText(RoomDesignActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }*/

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(this, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }*/

}