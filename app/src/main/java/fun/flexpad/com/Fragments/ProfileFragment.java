package fun.flexpad.com.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.R;
import fun.flexpad.com.databinding.FragmentFollowerListBinding;
import fun.flexpad.com.databinding.FragmentProfileBinding;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private DatabaseReference reference;
    private FirebaseUser fuser;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;
    FragmentProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);

//        Button group_payment = view.findViewById(R.id.group_pay);
//        binding.groupPay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(getContext(), PaymentActivity.class));
//            }
//        });

        storageReference = FirebaseStorage.getInstance().getReference("Profile Uploads");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                binding.username.setText(user.getUsername());
                if (user.getImageURI().equals("default")){
                    binding.profileImage.setImageResource(R.mipmap.ic_launcher);
                } else {
                    if (isAdded()) {
                        Glide.with(requireContext()).load(user.getImageURI()).into(binding.profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        showFollowerShip(fuser.getUid());
        binding.profileImage.setOnClickListener(v -> openImage());
        return binding.getRoot();
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);

    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = requireContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading...");
        pd.show();

        if (imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if (!task.isSuccessful()){
                    throw task.getException();
                }


                return fileReference.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    assert downloadUri != null;
                    String mUri = downloadUri.toString();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageURI", mUri);
                    reference.updateChildren(map);

                    pd.dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    public void showFollowerShip (String currentUserId) {
        final DatabaseReference followRef = FirebaseDatabase.getInstance().getReference()
                .child("FollowList").child(currentUserId);

        ArrayList<String> underFollowersList = new ArrayList<>();
        followRef.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                underFollowersList.clear();
                if (snapshot.child("unfollowed").exists()) {
                    final String followersNumber = Long.toString(snapshot.getChildrenCount() - 1);
                    binding.followersList.setText("Followers: " + followersNumber);
                } else {
                    final String followersNumber = Long.toString(snapshot.getChildrenCount());
                    binding.followersList.setText("Followers: " + followersNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ArrayList<String> underFollowingList = new ArrayList<>();
        followRef.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("unfollowed").exists()) {
                    final String followingNumber = Long.toString(snapshot.getChildrenCount() - 1 );
                    binding.followingList.setText("Following: " + followingNumber);
                } else {
                    final String followingNumber = Long.toString(snapshot.getChildrenCount());
                    binding.followingList.setText("Following: " + followingNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.followersList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//NavHostFragment.findNavController(requireParentFragment()).navigate(R.id.action_profileFragment_to_followerListFragment);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.profile_details, new FollowerListFragment()).addToBackStack(null).commit();
            }
        });
        binding.followingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.profile_details, new FollowingListFragment()).addToBackStack(null).commit();
            }
        });
    }
}
