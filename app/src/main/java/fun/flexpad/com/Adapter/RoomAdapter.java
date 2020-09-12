package fun.flexpad.com.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fun.flexpad.com.Model.Room;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.R;

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
        return new RoomAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Room room = mRooms.get(position);
        holder.roomname.setText(room.getRoomname());

    }

    @Override
    public int getItemCount() {
        return mRooms.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView roomname;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            roomname = itemView.findViewById(R.id.roomIname);
        }
    }

}
