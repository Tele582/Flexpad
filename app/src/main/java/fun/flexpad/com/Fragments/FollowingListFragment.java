package fun.flexpad.com.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import fun.flexpad.com.Adapters.UserAdapter;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.R;
import fun.flexpad.com.databinding.FragmentFollowerListBinding;
import fun.flexpad.com.databinding.FragmentFollowingBinding;
import fun.flexpad.com.databinding.FragmentFollowingListBinding;

public class FollowingListFragment extends Fragment {

    private FirebaseUser fuser;
    private UserAdapter userAdapter;
    FragmentFollowingListBinding followingListBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        followingListBinding = FragmentFollowingListBinding.inflate(inflater, container, false);

        followingListBinding.followingListing.setHasFixedSize(true);
        followingListBinding.followingListing.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        showFollowingList(fuser.getUid());

        return followingListBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        followingListBinding.titleBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    public void showFollowingList (String currentUserId) {
        final DatabaseReference followRef = FirebaseDatabase.getInstance().getReference()
                .child("FollowList").child(currentUserId);

        ArrayList<String> underFollowingList = new ArrayList<>();
        followRef.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                underFollowingList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String following = dataSnapshot.getKey();
                    underFollowingList.add(following);
                }
                ArrayList<User> followingNamesList = new ArrayList<>();
                FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followingNamesList.clear();
                        int followingNumber = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            assert user != null;
                            if (underFollowingList.contains(user.getId())) {
                                followingNamesList.add(user);
                                followingNumber++;
                            }
                        }

                        followingListBinding.title.setText("Following: " + followingNumber);
                        userAdapter = new UserAdapter(getContext(), followingNamesList, false);
                        followingListBinding.followingListing.setAdapter(userAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}