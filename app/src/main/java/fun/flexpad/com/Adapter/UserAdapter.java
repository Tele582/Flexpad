package fun.flexpad.com.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import fun.flexpad.com.MessageActivity;
import fun.flexpad.com.Model.Chat;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.R;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;
    private String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean ischat) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURI().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURI()).into(holder.profile_image);
        }

        if (user.getVerified().equals("true")) {
            holder.verification_image.setVisibility(View.VISIBLE);
        }

        if (ischat){
            lastMessage(user.getId(), holder.last_msg);
        } else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (ischat){
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else{
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        if (ischat) {
            unreadMessageNo(user.getId(), holder.unread_msg_no);
        } else {
            holder.unread_msg_no.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, MessageActivity.class);
            intent.putExtra("userid", user.getId());
            mContext.startActivity(intent);
        });

        checkFollowingStatus(user.getId(), holder.btnFollow);

        updateFollowList(holder.btnFollow, user.getId());

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class  ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public CircleImageView profile_image;
        public CircleImageView verification_image;
        private final ImageView img_on;
        private final ImageView img_off;
        private final TextView last_msg;
        private final TextView unread_msg_no;
        public Button btnFollow;

        ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            verification_image = itemView.findViewById(R.id.verification_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
            unread_msg_no = itemView.findViewById(R.id.unread_msg_no);
            btnFollow = itemView.findViewById(R.id.btn_follow);

        }
    }

    private void lastMessage(final String userid, final TextView last_msg){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;

                    try {
                        //assert firebaseUser != null; //causes crash on logout
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)
                                || chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = chat.getMessage();
                        }

                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                }
                if ("default".equals(theLastMessage)) {
                    last_msg.setText("");
                } else {
                    last_msg.setText(theLastMessage);
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void unreadMessageNo (final String userId, final TextView unread_msg_nos) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    //assert firebaseUser != null;

                    if (ischat) {
                        try {
                            if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId) && !chat.isIsseen()) {
                                unread_msg_nos.setVisibility(View.VISIBLE);
                                unread++;
                                unread_msg_nos.setText(Integer.toString(unread));
                            }
                            if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId) && chat.isIsseen()) {
                                unread_msg_nos.setVisibility(View.GONE);
                            }
                            if (chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())){
                                unread_msg_nos.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void checkFollowingStatus(String uid, Button followButton) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        assert firebaseUser != null;
        try {
            DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference()
                    .child("FollowList").child(firebaseUser.getUid()).child("following");

            followingRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(uid).exists()) {
                        followButton.setText(R.string.following);
                        Drawable dr = followButton.getContext().getResources().getDrawable(R.drawable.ic_baseline_check_circle_24);
                        followButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, dr,null);
                    } else {
                        followButton.setText(R.string.follow);
                        followButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void updateFollowList (Button followButton, String userString) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        followButton.setOnClickListener(view -> {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm,\ndd MMMM");
            Calendar currentCal = Calendar.getInstance();
            String currentDate = dateFormat.format(currentCal.getTime());
            if (followButton.getText().equals("Follow")) {
                FirebaseDatabase.getInstance().getReference()
                        .child("FollowList")
                        .child(firebaseUser.getUid())
                        .child("following")
                        .child(userString)
                        .setValue(currentDate).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("FollowList")
                                .child(userString)
                                .child("followers")
                                .child(firebaseUser.getUid())
                                .setValue(currentDate).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("FollowList")
                                        .child(firebaseUser.getUid())
                                        .child("following").child("unfollowed")
                                        .child(userString)
                                        .removeValue().addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("FollowList")
                                                .child(userString)
                                                .child("followers").child("unfollowed")
                                                .child(firebaseUser.getUid())
                                                .removeValue().addOnCompleteListener(task3 -> {
                                            if (task3.isSuccessful()) {

                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            } else {
                FirebaseDatabase.getInstance().getReference()
                        .child("FollowList")
                        .child(firebaseUser.getUid())
                        .child("following")
                        .child(userString)
                        .removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("FollowList")
                                .child(userString)
                                .child("followers")
                                .child(firebaseUser.getUid())
                                .removeValue().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("FollowList")
                                        .child(firebaseUser.getUid())
                                        .child("following").child("unfollowed")
                                        .child(userString)
                                        .setValue(currentDate).addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("FollowList")
                                                .child(userString)
                                                .child("followers").child("unfollowed")
                                                .child(firebaseUser.getUid())
                                                .setValue(currentDate).addOnCompleteListener(task3 -> {
                                            if (task3.isSuccessful()) {

                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}
