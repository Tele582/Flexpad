package fun.flexpad.com.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import fun.flexpad.com.GamesActivity;
import fun.flexpad.com.Model.Room;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.Model.Voice;
import fun.flexpad.com.R;
import fun.flexpad.com.RoomChatActivity;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<Room> mRooms;
    private String theLastMessage, lastMessageTime;

    public RoomAdapter(Context mContext, ArrayList<Room> mRooms) {
        this.mContext = mContext;
        this.mRooms = mRooms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.room_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Room room = mRooms.get(position);
        holder.roomname.setText(room.getRoomname());
        holder.mineShowView(holder.mine, holder.creator);
        holder.showPopupAndVerification();
        holder.unseenMessageNo(holder.unseen_msg_no);

        holder.lastMessage(holder.lastMsgTime);

        try {
            holder.itemView.setOnClickListener(v -> {
                final Intent intent = new Intent(mContext, RoomChatActivity.class);
                intent.putExtra("Room_Name", room.getRoomname());
                intent.putExtra("Room_ID", room.getRoomKey());
                intent.putExtra("Room_Creator", room.getCreatorId());
                mContext.startActivity(intent);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            });
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mRooms.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {
        public TextView roomname;
        public TextView mine;
        public TextView creator;
        public CircleImageView verification;
        private final TextView unseen_msg_no;
        private TextView lastMsgTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            roomname = itemView.findViewById(R.id.roomIname);
            mine = itemView.findViewById(R.id.mine_view);
            creator = itemView.findViewById(R.id.creator_view);
            verification = itemView.findViewById(R.id.verification_image);
            unseen_msg_no = itemView.findViewById(R.id.unseen_msg_no);
            lastMsgTime = itemView.findViewById(R.id.last_msg_time);
        }

        private void showPopup(View view) {
            PopupMenu popup = new PopupMenu(mContext.getApplicationContext(), view);
            popup.setOnMenuItemClickListener(this);
            popup.inflate(R.menu.room_popup_menu);
            popup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.item1) {
                final DatabaseReference room_delete_reference = FirebaseDatabase.getInstance().getReference().child("Rooms");
                room_delete_reference.child(mRooms.get(getAdapterPosition()).getRoomKey()).removeValue().addOnSuccessListener(aVoid ->
                        Toast.makeText(mContext.getApplicationContext(), "Room Successfully Deleted!", Toast.LENGTH_SHORT).show());
                //this opens random roomChatActivity, which it should not
                mContext.startActivity(new Intent(mContext, GamesActivity.class));
            }
            return false;
        }

        public void mineShowView (TextView mineView, TextView creatorView) {
            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Rooms");
            final Room roomN = mRooms.get(getAdapterPosition()); //for roomN.getCreatorId();

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<String> usersList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        assert user != null;
                        usersList.add(user.getId());
                    }
                    String roomCreator = (String) snapshot.child(roomN.getRoomKey()).child("creator").getValue();
//                    //or, String roomCreator = snapshot.child(roomN.getRoomKey()).child("creator").getValue(String.class);
                    if (roomCreator != null && roomCreator.equals(Objects.requireNonNull(firebaseUser).getUid())) {
                        mineView.setVisibility(View.VISIBLE);
                    } else {
                        mineView.setVisibility(View.GONE);
                        if (usersList.contains(roomN.getCreatorId())) {
                            creatorView.setText(roomN.getCreatorUsername());
                            creatorView.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        public void showPopupAndVerification () {
            final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Rooms");
            final Room roomP = mRooms.get(getAdapterPosition());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final String boundRoomCreator = (String) snapshot.child(roomP.getRoomKey()).child("creator").getValue();
                    final String creatorVerifiedStatus = (String) snapshot.child(roomP.getRoomKey()).child("creatorVerified").getValue();
                    itemView.setOnLongClickListener(v -> {
                        if ((boundRoomCreator != null)) {
                            assert fUser != null;
                            if (boundRoomCreator.equals(fUser.getUid())) {showPopup(v);}
                        }
                        return false;
                    });
                    if (creatorVerifiedStatus != null && creatorVerifiedStatus.equals("true")) {verification.setVisibility(View.VISIBLE);}
                    else {verification.setVisibility(View.GONE);}
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void unseenMessageNo (final TextView unseen_msg_nos) {
            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("VoiceClips");
            final Room roomS = mRooms.get(getAdapterPosition());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int unread = 0;
                    ArrayList<String> seenUsersList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Voice voice = snapshot.getValue(Voice.class);
                        assert voice != null;
                        //assert firebaseUser != null;
                        for (DataSnapshot dataSnapshot1 : snapshot.child("seenBy").getChildren()) {
                            seenUsersList.add(dataSnapshot1.getKey());
                        }

                        try {
                            if ((!seenUsersList.contains(firebaseUser.getUid())) && (roomS.getRoomKey().equals(voice.getRoomID()))) {
                                unseen_msg_nos.setVisibility(View.VISIBLE);
                                unread++;
                                unseen_msg_nos.setText(Integer.toString(unread));
                            }
                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                        seenUsersList.clear();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        private void lastMessage(final TextView lastMsgTime) {
            theLastMessage = "default";
            lastMessageTime = "";
            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("VoiceClips");
            final Room roomL = mRooms.get(getAdapterPosition());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Voice voice = snapshot.getValue(Voice.class);
                        assert voice != null;
                        try {
                            if (voice != null && (roomL.getRoomKey().equals(voice.getRoomID()))) {
                                theLastMessage = voice.getMessagekey();
                                if (voice.getTime() != null ) {
                                    @SuppressLint("SimpleDateFormat")
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
                                    Calendar currentCal = Calendar.getInstance();
                                    final String currentDay = dateFormat.format(currentCal.getTime());
                                    final String ttt = voice.getTime();
                                    if (currentDay.equals(ttt.substring(ttt.length() - 12))) {
                                        lastMessageTime = (String.format("%.5s", ttt));
                                    } else if (((ttt.substring(ttt.length() - 9)).equals(currentDay.substring(currentDay.length() - 9))) &&
                                            ((Integer.toString(Integer.parseInt(String.format("%.2s", currentDay)) - 1))
                                                    .equals(Integer.toString(Integer.parseInt(String.format("%.2s", ttt.substring(ttt.length() - 12))))))) {
                                        lastMessageTime = ("Yesterday");
                                    } else {
                                        lastMessageTime = String.format("%.6s", ttt.substring(ttt.length() - 12));
                                    }
                                } else {
                                    lastMessageTime = "";
                                }
                            }
                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                    }
                    if ("default".equals(theLastMessage)) {
                        lastMsgTime.setText("");
                    } else {
                        lastMsgTime.setText(lastMessageTime);
                    }
                    theLastMessage = "default";
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
