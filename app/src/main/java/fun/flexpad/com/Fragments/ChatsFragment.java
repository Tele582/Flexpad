package fun.flexpad.com.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import fun.flexpad.com.Adapter.UserAdapter;
import fun.flexpad.com.FeedbackActivity;
import fun.flexpad.com.Model.Chat;
import fun.flexpad.com.Model.Chatlist;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.Notifications.Token;
import fun.flexpad.com.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;



import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView recyclerSenderView;

    private UserAdapter userAdapter;
    private List<User> mUsers;

    private FirebaseUser fuser;
    private DatabaseReference reference;
//    private DatabaseReference ref2;

    private List<Chatlist> usersList;
//    private List<Chatlist> usersListSender;

    private EditText search_chats;

    String userid;
    Intent intent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.chats_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//        recyclerSenderView = view.findViewById(R.id.chats_recycler_view_sender);
//        recyclerSenderView.setHasFixedSize(true);
//        recyclerSenderView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();
//        usersListSender = new ArrayList<>();

        search_chats = view.findViewById(R.id.search_chats);
        search_chats.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    assert chatlist != null;
                    usersList.add(chatlist);

                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        ref2 = FirebaseDatabase.getInstance().getReference("ChatSenderlist").child(fuser.getUid());
//        ref2.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                usersListSender.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
//                    assert chatlist != null;
//                    usersListSender.add(chatlist);
//
//                }
//                chatListSenders();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        updateToken(FirebaseInstanceId.getInstance().getToken());
        return view;
    }

    private void searchUsers(String s) {
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    for (Chatlist chatlist : usersList){
                        assert user != null;
                        if (user.getId().equals(chatlist.getId())){
                            mUsers.add(user);
                        }
                    } //duplicate for usersListSender
                }

                userAdapter = new UserAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token();
        reference.child(fuser.getUid()).setValue(token1);
    }

    private void chatList() {
//        fuser = FirebaseAuth.getInstance().getCurrentUser();
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            private static final String TAG = "Tagg"; ///////

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        assert user != null;
                        Log.d(TAG, "onDataChange: (QUERY METHOD) found user: " + user.toString()); //temporary

                        for (Chatlist chatlist : usersList) {
                            try {
                                if (user.getId().equals(chatlist.getId())) {
                                    mUsers.add(user);

                                }
                            } catch (Exception e) {
                                e.getStackTrace();
                            }

                        }
                }
                userAdapter = new UserAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

//    private void chatListSenders() {
//        fuser = FirebaseAuth.getInstance().getCurrentUser();
//        final List<User> mUsers = new ArrayList<>();
//        ref2 = FirebaseDatabase.getInstance().getReference("Users");
//        ref2.addValueEventListener(new ValueEventListener() {
//            private static final String TAG = "Tagg";
//
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                mUsers.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    User user = snapshot.getValue(User.class);
//                    assert user != null;
//                    Log.d(TAG, "onDataChange: (QUERY METHOD) found user: " + user.toString()); //temporary
//
//                    for (Chatlist chatlist : usersListSender) {
//                        if (user.getId().equals(chatlist.getIdSender())) {
//                            mUsers.add(user);
//
//                        }
//                    }
//                }
//                userAdapter = new UserAdapter(getContext(), mUsers, true);
//                recyclerSenderView.setAdapter(userAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }
}

