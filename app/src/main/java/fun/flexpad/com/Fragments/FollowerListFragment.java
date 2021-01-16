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

public class FollowerListFragment extends Fragment {

    private FirebaseUser fuser;
    private UserAdapter userAdapter;
    FragmentFollowerListBinding followerListBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        followerListBinding = FragmentFollowerListBinding.inflate(inflater, container, false);

//        recyclerView = view.findViewById(R.id.followers_listing);
        followerListBinding.followersListing.setHasFixedSize(true);
        followerListBinding.followersListing.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        showFollowersList(fuser.getUid());

        return followerListBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        followerListBinding.titleBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    public void showFollowersList (String currentUserId) {
        final DatabaseReference followRef = FirebaseDatabase.getInstance().getReference()
                .child("FollowList").child(currentUserId);

        ArrayList<String> underFollowersList = new ArrayList<>();
        followRef.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                underFollowersList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String follower = dataSnapshot.getKey();
                    underFollowersList.add(follower);
                }
                ArrayList<User> followersNamesList = new ArrayList<>();
                FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followersNamesList.clear();
                        int followersNumber = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            assert user != null;
                            if (underFollowersList.contains(user.getId())) {
                                followersNamesList.add(user);
                                followersNumber++;
                            }
                        }

                        followerListBinding.title.setText("Followers: " + followersNumber);
                        userAdapter = new UserAdapter(getContext(), followersNamesList, false);
                        followerListBinding.followersListing.setAdapter(userAdapter);
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