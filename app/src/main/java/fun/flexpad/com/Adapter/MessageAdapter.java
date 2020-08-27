package fun.flexpad.com.Adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.textclassifier.ConversationActions;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import fun.flexpad.com.MessageActivity;
import fun.flexpad.com.Model.Chat;
import fun.flexpad.com.Model.Chatlist;
import fun.flexpad.com.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageuri;

    public String original_message;

    private FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageuri) {
        this.mChat = mChat;
        this.mContext = mContext;
        this.imageuri = imageuri;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, final int position) {

        //////holder.messageSenderPicture.setImageResource(R.drawable.imag_30); // this mostly works though
        //holder.messageSenderPicture.setVisibility(View.GONE); //causes crash
        //holder.messageReceiverPicture.setVisibility(View.GONE); //causes crash

        Chat chat = mChat.get(position);

        holder.show_message.setText(chat.getMessage());

        //original_message = chat.getMessage();

        if (imageuri.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageuri).into(holder.profile_image);
        }

        if (position == mChat.size()-1){
            if (chat.isIsseen()){
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Delivered");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }

        //holder.messageSenderPicture.setVisibility(View.VISIBLE);
        //Picasso.

        //here, I try delete again (either use holder.show_message or holder.messageLAyout
        holder.messageLAyout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (mChat.get(position).getType().equals("pdf") || mChat.get(position).getType().equals("docx")){
                ///

                if (mChat.get(position).getType().equals("text")){
                    holder.showPopup(v);
                }
            }
        });
    }

    private void deleteSentMessage(final int position, final ViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Chats")
                .child(mChat.get(position).getSender())
                .child(mChat.get(position).getReceiver())
                .child(mChat.get(position).getMessageid())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Message Successfully Deleted!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public void deleteReceivedMessage (final int position, final ViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Chats")
                .child(mChat.get(position).getReceiver())
                .child(mChat.get(position).getSender())
                .child(mChat.get(position).getMessageid())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Message Successfully Deleted!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void deleteMessageForEveryone(final int position, final ViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Chats")
                .child(mChat.get(position).getReceiver())
                .child(mChat.get(position).getSender())
                .child(mChat.get(position).getMessageid())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    rootRef.child("Chats")
                            .child(mChat.get(position).getSender())
                            .child(mChat.get(position).getReceiver())
                            .child(mChat.get(position).getMessageid())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "Message Successfully Deleted!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    private abstract class runnable implements Runnable {

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

        TextView show_message;
        ImageButton btn_play;
        ImageButton btn_stop;
        public ImageView profile_image;
        TextView txt_seen;
        TextToSpeech textToSpeech;
        Translate translate;
        String newmessage;

        //RecyclerView recyclerView;

        RelativeLayout messageLAyout; // for click listener to show delete;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            btn_play = itemView.findViewById(R.id.btn_play);
            btn_stop = itemView.findViewById(R.id.btn_stop);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            messageLAyout = itemView.findViewById(R.id.messageLayout);
            //recyclerView = itemView.findViewById(R.id.recycler_view);

            //messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image);
            //messageSenderPicture = itemView.findViewById(R.id.message_sender_image);

            textToSpeech = new TextToSpeech(mContext.getApplicationContext(), status -> {
                //Set language if no error
                if (status != TextToSpeech.ERROR) {

                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            Log.i("TextToSpeech", "On Start");
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            //Log.i("TextToSpeech", "On Done");
                        }

                        @Override
                        public void onError(String utteranceId) {
                            Log.i("TextToSpeech", "On Error");
                        }
                    });
                }
            });

            //text to speech button
            btn_play.setOnClickListener(v -> {
                //Get text form message bar
                String text = show_message
                        .getText().toString();

                getTranslateService();
                Detection detection = translate.detect(text);
                final String detectedLanguage = detection.getLanguage();

                //convert to speech
                textToSpeech.setLanguage(new Locale(detectedLanguage));
                //textToSpeech.setLanguage(Locale.US);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);

                btn_stop.setVisibility(View.VISIBLE);
                btn_stop.setEnabled(true);
            });



            btn_stop.setOnClickListener(v -> {
                if (textToSpeech != null) {
                    textToSpeech.stop(); //textToSpeech.shutdown();
                    btn_stop.setVisibility(View.INVISIBLE);
                    btn_play.setVisibility(View.VISIBLE);
                    btn_play.setEnabled(true);
                    btn_stop.setEnabled(false);
                }
            });
        }

        private void showPopup(View view) {
            PopupMenu popup = new PopupMenu(mContext.getApplicationContext(), view);
            popup.setOnMenuItemClickListener(this);
            popup.inflate(R.menu.popup_menu);
            popup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item1:

                    //deleteMessageForEveryone();
                    //delete_message();             //////////////////////
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    reference.child("Chats").push().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(mContext.getApplicationContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    return true;
                case R.id.item2:
                    if (checkInternetConnection()) {
                        //If there is internet connection, get translate service and start translation:
                        getTranslateService();
                        translate();
                    } else {
                        //If not, display "no connection" warning:
                        Toast.makeText(mContext.getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }

                    return true;
                case R.id.item3:

                    mChat.clear();

                    //mChat.addAll(...);      //FirebaseDatabase.getInstance().getReference().child("Chats").setValue(mChat);

                default:
                    return false;
            }
        }

        public void getTranslateService() {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try (InputStream is = mContext.getResources().openRawResource(R.raw.flexpadtranslate_1e6ba6d8f137)) {
                //Get credentials:
                final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);

                //Set credentials and get translate service:
                TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
                translate = translateOptions.getService();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        String[] symbolList = new String[]{
                "en", "af", "fr", "de", "it", "ja", "yo", "zh"};
        String[] langList = new String[]{
                "English", "Afrikaans", "French", "German", "Italian", "Japanese", "Yoruba", "Chinese"};

        public void translate() {
            String newmsg = show_message.getText().toString();

            Detection detection = translate.detect(newmsg);
            final String detectedLanguage = detection.getLanguage();

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Select Language");

            builder.setItems(langList, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        String selectedLang = symbolList[which];
                        //Translation translation = translate.translate(newmsg, Translate.TranslateOption.targetLanguage(selectedLang), Translate.TranslateOption.model("base"));
                        Translation translation = translate.translate(newmsg, Translate.TranslateOption.sourceLanguage(detectedLanguage), Translate.TranslateOption.targetLanguage(selectedLang), Translate.TranslateOption.model("base"));

                        newmessage = translation.getTranslatedText();
                        show_message.setText(newmessage);
                    }
                    catch (TranslateException te) {
                        te.getStackTrace();
                    }
                }
            });
            builder.show();

        }
    }

    public void delete_message(final int position, final ViewHolder holder) {

         DatabaseReference rootref = FirebaseDatabase.getInstance().getReference("Chats").child(fuser.getUid());
         rootref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
             @Override
             public void onComplete(@NonNull Task<Void> task) {

                 if (task.isSuccessful()) {
                     Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                 }
             }
         });


    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }


    public boolean checkInternetConnection() {
        //Check internet connection:
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        //Means that we are connected to a network (mobile or wi-fi)
        assert connectivityManager != null;
        boolean connected = Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;

        return connected;
    }
}

//'Chats' represents Messages in ths project
