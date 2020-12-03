package fun.flexpad.com.Adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;

import fun.flexpad.com.Model.Chat;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.Model.Voice;
import fun.flexpad.com.R;

public class VoiceAdapter extends RecyclerView.Adapter<VoiceAdapter.ViewHolder> {

    private Context mContext;
    private final List<Voice> mVoice;
    private FirebaseUser fuser;

    public VoiceAdapter(Context mContext, List<Voice> mVoice) {
        this.mContext = mContext;
        this.mVoice = mVoice;
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
        final DatabaseReference user_reference = FirebaseDatabase.getInstance().getReference("Users").child(voice.getSender());

        user_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                holder.userName.setText(user.getUsername());

                if (user.getImageURI().equals("default")){
                    holder.profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(mContext).load(user.getImageURI()).into(holder.profile_image);
                }

                holder.duration.setText(voice.getDuration());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        holder.mediaPlayer = MediaPlayer.create(mContext, Uri.parse(voice.getMessage())); ...

        holder.btn_play.setOnClickListener(v -> {

            holder.mediaPlayer = MediaPlayer.create(mContext, Uri.parse(voice.getMessage()));

            holder.mediaPlayer.setOnPreparedListener(mp -> {
                holder.seekbar.setMax(holder.mediaPlayer.getDuration());
                holder.changeSeekbar();
            });

            holder.mediaPlayer.setOnCompletionListener(mc -> {
                holder.mediaPlayer.start();
                holder.btn_play.setVisibility(View.INVISIBLE);
                holder.btn_pause.setVisibility(View.VISIBLE);
                holder.changeSeekbar();
            });

            holder.mediaPlayer.start();
            holder.btn_play.setVisibility(View.INVISIBLE);
            holder.btn_pause.setVisibility(View.VISIBLE);
            holder.changeSeekbar();
        });

        holder.btn_pause.setOnClickListener(v -> {
            holder.mediaPlayer.pause();
            holder.btn_pause.setVisibility(View.INVISIBLE);
            holder.btn_play.setVisibility(View.VISIBLE);
        });

        holder.voiceLayout.setOnClickListener(v -> {
            try {
                if (voice.getType().equals("audio (3gp)")) {holder.showNonTextPopup(v);}
            }
            catch (Exception exception) {exception.getStackTrace();}
        });
    }

    @Override
    public int getItemCount() {
        return mVoice.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

        ImageButton btn_play, btn_pause;
        private final ImageView profile_image;
        private SeekBar seekbar;
        public MediaPlayer mediaPlayer;
        private Runnable runnable;
        private Handler handler;
        TextView userName, duration;
        LinearLayout voiceLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            btn_play = itemView.findViewById(R.id.btn_play);
            btn_pause = itemView.findViewById(R.id.btn_pause);
            profile_image = itemView.findViewById(R.id.profile_image);
            handler = new Handler();
            seekbar = itemView.findViewById(R.id.seekbar);
            userName = itemView.findViewById(R.id.username);
            duration = itemView.findViewById(R.id.duration);
            voiceLayout = itemView.findViewById(R.id.voice_layout);
            mediaPlayer = new MediaPlayer(); //Uri.parse(mVoice.get(getAdapterPosition()).getMessage())
//            mediaPlayer = MediaPlayer.create(mContext, R.raw.symphony);

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
        }

        private void showNonTextPopup(View view) {
            PopupMenu popup = new PopupMenu(mContext.getApplicationContext(), view);
            popup.setOnMenuItemClickListener(this);
            popup.inflate(R.menu.non_text_msg_popup_menu);
            popup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final DatabaseReference message_delete_reference = FirebaseDatabase.getInstance().getReference().child("VoiceClips");
            final Voice voice = mVoice.get(getAdapterPosition());
            switch (item.getItemId()) {
                case R.id.item1:
                    message_delete_reference.child(voice.getMessagekey()).removeValue().addOnSuccessListener(aVoid ->
                            Toast.makeText(mContext.getApplicationContext(), "Voice Message Deleted!", Toast.LENGTH_SHORT).show());
                    voiceLayout.setVisibility(View.GONE);
                    return true;

                default:
                    return false;
            }
        }

        private void changeSeekbar() {
            seekbar.setProgress(mediaPlayer.getCurrentPosition());

            if (mediaPlayer.isPlaying()) {
                runnable = this::changeSeekbar;
            }

            handler.postDelayed(runnable, 1000);
        }
    }
}