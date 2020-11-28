package fun.flexpad.com.Adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import fun.flexpad.com.Model.User;
import fun.flexpad.com.Model.Voice;
import fun.flexpad.com.R;

public class VoiceAdapter extends RecyclerView.Adapter<VoiceAdapter.ViewHolder> {

    private Context mContext;
    private final List<Voice> mVoice;
    private String imageuri;
    private FirebaseUser fuser;

    public VoiceAdapter(Context mContext, List<Voice> mVoice, String imageuri) {
        this.mContext = mContext;
        this.mVoice = mVoice;
        this.imageuri = imageuri;
    }

    @NonNull
    @Override
    public VoiceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.voice_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoiceAdapter.ViewHolder holder, int position) {

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        Voice voice = mVoice.get(position);
        final String senderId = voice.getSender();

//        final DatabaseReference user_reference = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference user_reference = FirebaseDatabase.getInstance().getReference("Users").child(senderId);

//        user_reference.child("VoiceClips").child(voice.getMessagekey()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        user_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                holder.userName.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        holder.seekbar.


        if (imageuri.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageuri).into(holder.profile_image);
        }

    }

    @Override
    public int getItemCount() {
        return mVoice.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageButton btn_play;
        private final ImageView profile_image;
        private SeekBar seekbar;
        private MediaPlayer mediaPlayer;
        private Runnable runnable;
        private Handler handler;
        TextView userName;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            btn_play = itemView.findViewById(R.id.btn_play);
            profile_image = itemView.findViewById(R.id.profile_image);
            seekbar = itemView.findViewById(R.id.seekbar);
            userName = itemView.findViewById(R.id.username);
            mediaPlayer = MediaPlayer.create(mContext, R.raw.symphony); //mVoice.get(getAdapterPosition()).getMessage()

//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    seekbar.setMax(mediaPlayer.getDuration());
//                    mediaPlayer.start();
//                    changeSeekbar();
//                }
//            });

            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b) {
                        mediaPlayer.seekTo(i);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            btn_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        btn_play.setImageResource(R.drawable.ic_play_rr);
                    } else {
                        mediaPlayer.start();
                        btn_play.setImageResource(R.drawable.ic_pause_rr_foreground);
                        changeSeekbar();
                    }
                }
            });

        }

        private void changeSeekbar() {
            seekbar.setProgress(mediaPlayer.getCurrentPosition());

            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        changeSeekbar();
                    }
                };
            }

            handler.postDelayed(runnable, 1000);
        }

//        @Override
//        public void onClick(View view) {
//            if (view.getId() == R.id.btn_play) {
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.pause();
//                    btn_play.setImageResource(R.drawable.ic_play_rr);
//                } else {
//                    mediaPlayer.start();
//                    btn_play.setImageResource(R.drawable.ic_pause_rr_foreground);
//                    changeSeekbar();
//                }
//            }
//        }

    }
}
