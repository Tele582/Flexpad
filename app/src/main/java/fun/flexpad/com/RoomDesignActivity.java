package fun.flexpad.com;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import fun.flexpad.com.Model.Room;
import fun.flexpad.com.Model.User;

public class RoomDesignActivity extends AppCompatActivity {

    private CircleImageView room_image;
    EditText room_name;
    Button ok;

    private DatabaseReference reference;
    private FirebaseUser fuser;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_design);

        Toolbar toolbar = findViewById(R.id.room_design_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Room Design");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            //might cause app to crash;
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        ok = findViewById(R.id.ok);
        //room_image = findViewById(R.id.room_image);
        room_name = findViewById(R.id.room_name);

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("room_uploads");

        ok.setOnClickListener(view -> {

            String room = room_name.getText().toString();

            if (!room.trim().isEmpty()){
                saveroom(room, fuser.getUid());
                Toast.makeText(RoomDesignActivity.this, "Creating..", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RoomDesignActivity.this, RoomChatActivity.class);
                intent.putExtra("room_key", room);
                startActivity(intent);
            }
            else {
                Toast.makeText(RoomDesignActivity.this, "Can't create room with no name.", Toast.LENGTH_SHORT).show();
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

    //Create and save the room
    public void saveroom(String room, String creator) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Rooms");

        HashMap<String, Object> nmap = new HashMap<>();
        nmap.put("roomname", room);
        nmap.put("creator", creator);

        DatabaseReference dataref = reference.push(); //.child(fuser.getUid())
        String ama = dataref.getKey();
        dataref.setValue(nmap);
    }

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