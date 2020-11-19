package fun.flexpad.com.Adapter;

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

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import fun.flexpad.com.MainActivity;
import fun.flexpad.com.Model.Room;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.R;
import fun.flexpad.com.RoomChatActivity;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<Room> mRooms;

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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, RoomChatActivity.class);
            intent.putExtra("Room_Name", room.getRoomname());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mRooms.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {
        public TextView roomname;
        public TextView mine;
        public TextView creator;
        public CircleImageView verification;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            roomname = itemView.findViewById(R.id.roomIname);
            mine = itemView.findViewById(R.id.mine_view);
            creator = itemView.findViewById(R.id.creator_view);
            verification = itemView.findViewById(R.id.verification_image);
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
    }
}
