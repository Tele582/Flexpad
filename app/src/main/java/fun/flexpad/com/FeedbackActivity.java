package fun.flexpad.com;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FeedbackActivity extends AppCompatActivity {

    EditText app_feedback;
    Button send_feedback, view_feedback;
    Intent intent;

    FirebaseUser fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        app_feedback = findViewById(R.id.appfeedback);
        send_feedback = findViewById(R.id.sendfeedback);
        view_feedback = findViewById(R.id.viewfeedback);
        intent = getIntent();
        final String userid = intent.getStringExtra("userid");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        send_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = app_feedback.getText().toString();

                if (!message.trim().isEmpty()){
                    send_feedback.setEnabled(true);
                    sendMessage(fuser.getUid(), message);
                    Toast.makeText(FeedbackActivity.this, "Sending..", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(FeedbackActivity.this, "You can't send an empty message.", Toast.LENGTH_SHORT).show();
                }
                app_feedback.setText("");

                view_feedback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(FeedbackActivity.this)
                                .setTitle("Sent:")
                                .setMessage(message)
                                .show();
                    }
                });
            }
        });
    }

    private void sendMessage(String sender, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        //reference here is RootRef in tut
        //final String userid = intent.getStringExtra("id");
        //assert userid != null;



        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("type", "text");
        hashMap.put("message", message);
        reference.child("Feedback").push().setValue(hashMap);
    }
}

