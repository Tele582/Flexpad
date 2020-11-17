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
import java.util.List;

import fun.flexpad.com.Adapter.RoomAdapter;
import fun.flexpad.com.Adapter.UserAdapter;
import fun.flexpad.com.Model.Room;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.R;

public class ForYouFragment extends Fragment {

    private RecyclerView recyclerView;
    private RoomAdapter roomAdapter;
    private ArrayList<Room> mRooms;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_for_you, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        //mRooms = new ArrayList<>();
        readRooms();
        return view;
    }

    private void readRooms() {
        mRooms = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Rooms");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    mRooms.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Room room = snapshot.getValue(Room.class);
                        assert room != null;
                        mRooms.add(room);
                    }
                    roomAdapter = new RoomAdapter(getContext(), mRooms);
                    recyclerView.setAdapter(roomAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}




