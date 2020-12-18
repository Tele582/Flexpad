package fun.flexpad.com.Fragments;

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

import fun.flexpad.com.Adapters.RoomAdapter;
import fun.flexpad.com.Model.Room;
import fun.flexpad.com.R;

public class FollowingFragment extends Fragment {

    private RecyclerView recyclerView;
    private RoomAdapter roomAdapter;

    private FirebaseUser fuser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_following, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        readFollowedRooms();
        return view;
    }

    private void readFollowedRooms() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("FollowList").child(firebaseUser.getUid()).child("following")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<String> followingList = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String followed = dataSnapshot.getKey();
                        followingList.add(followed);
                    }
                    reference.child("Rooms").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ArrayList<Room> roomFollowList = new ArrayList<>();
                            roomFollowList.clear();
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                Room room = dataSnapshot1.getValue(Room.class);
                                String roomCreator = snapshot.child(room.getRoomKey()).child("creator").getValue(String.class); // or //String roomCreator = snapshot.child(room.getRoomKey()).child("creator").getValue(String.class);

                                if (followingList.contains(roomCreator) || roomCreator.equals(firebaseUser.getUid()))
                                    roomFollowList.add(room);
                            }
                            roomAdapter = new RoomAdapter(getContext(), roomFollowList);
                            recyclerView.setAdapter(roomAdapter);
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