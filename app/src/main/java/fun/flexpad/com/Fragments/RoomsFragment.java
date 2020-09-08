package fun.flexpad.com.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fun.flexpad.com.R;
import fun.flexpad.com.RoomChatActivity;
import fun.flexpad.com.RoomDesignActivity;

public class RoomsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseUser fuser;

    Button create_room;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_rooms, container, false);

        create_room = view.findViewById(R.id.create_room);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        create_room.setOnClickListener(view1 -> startActivity(new Intent(getContext(), RoomDesignActivity.class)));





        return view;
    }
}
