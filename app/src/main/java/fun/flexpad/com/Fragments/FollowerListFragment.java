package fun.flexpad.com.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

public class FollowerListFragment extends Fragment {

    private FirebaseUser fuser;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follower_list, container, false);

        recyclerView = view.findViewById(R.id.followers_listing);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        showFollowersList(fuser.getUid());

        return view;
    }


    public void showFollowersList (String currentUserId) {
        final DatabaseReference followRef = FirebaseDatabase.getInstance().getReference()
                .child("FollowList").child(currentUserId);

        ArrayList<String> underFollowersList = new ArrayList<>();
        followRef.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                underFollowersList.clear();
                if (snapshot.child("unfollowed").exists()) {
                    final String followersNumber = Long.toString(snapshot.getChildrenCount() - 1);

                } else {
                    final String followersNumber = Long.toString(snapshot.getChildrenCount());

                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String follower = dataSnapshot.getKey();
                    underFollowersList.add(follower);
                }
                ArrayList<User> followersNamesList = new ArrayList<>();
                FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followersNamesList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            assert user != null;
                            if (underFollowersList.contains(user.getId())) {
                                followersNamesList.add(user);
                            }
                        }

                        userAdapter = new UserAdapter(getContext(), followersNamesList, false);
                        recyclerView.setAdapter(userAdapter);
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