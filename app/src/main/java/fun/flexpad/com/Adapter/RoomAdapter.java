package fun.flexpad.com.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fun.flexpad.com.MessageActivity;
import fun.flexpad.com.Model.Room;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.R;
import fun.flexpad.com.RoomChatActivity;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private Context mContext;
    private List<Room> mRooms;

    public RoomAdapter(Context mContext, List<Room> mRooms) {
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

        final Room room = mRooms.get(position); //or remove 'final'
        holder.roomname.setText(room.getRoomname());


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, RoomChatActivity.class);
            intent.putExtra("room_key", room.getRoomname());
            mContext.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return mRooms.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView roomname;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            roomname = itemView.findViewById(R.id.roomIname);
        }
    }

}
