package fun.flexpad.com;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import fun.flexpad.com.Fragments.APIService;
import fun.flexpad.com.Notifications.Client;
import fun.flexpad.com.Notifications.Data;
import fun.flexpad.com.Notifications.MyResponse;
import fun.flexpad.com.Notifications.Sender;
import fun.flexpad.com.Notifications.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText username, email, password;
    Button btn_register;

    //String contact_no;
    MaterialEditText phone_number;

    FirebaseAuth auth;
    DatabaseReference reference;
    APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_register = findViewById(R.id.btn_register);
        phone_number = findViewById(R.id.phone_number);

        auth = FirebaseAuth.getInstance();

        btn_register.setOnClickListener(view -> {

            //sendEmail();

            String txt_username = Objects.requireNonNull(username.getText()).toString();
            String txt_email = Objects.requireNonNull(email.getText()).toString();
            String txt_password = Objects.requireNonNull(password.getText()).toString();
            String contact_no = Objects.requireNonNull(phone_number.getText()).toString(); //Everyone's phone number

            if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(contact_no)){
                Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (txt_password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Password must have at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {

                if (ContextCompat.checkSelfPermission(
                        RegisterActivity.this, Manifest.permission.READ_CONTACTS) ==
                        PackageManager.PERMISSION_GRANTED) {
                    // You can use the API that requires the permission.
                    register(txt_username, txt_email, txt_password, contact_no);
                    sendEmailToContacts();
                } else {
                    // You can directly ask for the permission.
                    ActivityCompat.requestPermissions(RegisterActivity.this,
                            new String[] { Manifest.permission.READ_CONTACTS },
                            Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            }
        });

        FloatingActionButton floatingShareButton = (FloatingActionButton) findViewById(R.id.floatingShareButton2);
        floatingShareButton.setOnClickListener(v -> {
            //add the sharing option to the floating action button

            Intent a = new Intent(Intent.ACTION_SEND);

            //this is to get the app link in the Play Store without launching your app.
            final String appPackageName = getApplicationContext().getPackageName();
            String strAppLink;

            try {
                strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
            }
            catch (android.content.ActivityNotFoundException anfe) {
                strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
            }

            // this is the sharing part
            a.setType("text/plain"); //text/plain for just text
            String shareBody = "Download now to have live conversations at your convenience." +
                    "\n"+""+strAppLink;
            String shareSub = "APP NAME/TITLE";
            a.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            //a.putExtra(android.content.Intent.EXTRA_TITLE, shareSub);
            a.putExtra(Intent.EXTRA_TEXT, shareBody);

            startActivity(Intent.createChooser(a, "Share Using"));

            //Give users incentive to share. Maybe free flexcoins.
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Continue the action or workflow
                // in your app.

                String txt_username = Objects.requireNonNull(username.getText()).toString();
                String txt_email = Objects.requireNonNull(email.getText()).toString();
                String txt_password = Objects.requireNonNull(password.getText()).toString();
                String contact_no = Objects.requireNonNull(phone_number.getText()).toString(); //Everyone's phone number

                register(txt_username, txt_email, txt_password, contact_no);
                sendEmailToContacts();

            } else {
                // Explain to the user that the feature is unavailable because
                // the features requires a permission that the user has denied.
                // At the same time, respect the user's decision. Don't link to
                // system settings in an effort to convince the user to change
                // their decision.
                String txt_username = Objects.requireNonNull(username.getText()).toString();
                String txt_email = Objects.requireNonNull(email.getText()).toString();
                String txt_password = Objects.requireNonNull(password.getText()).toString();
                String contact_no = Objects.requireNonNull(phone_number.getText()).toString(); //Everyone's phone number

                register(txt_username, txt_email, txt_password, contact_no);
                Toast.makeText(this,"Contacts not available'. ", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    static class Constants{
        public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    }

    public void register (final String username, String email, String password, String phone_number){

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        assert firebaseUser != null;
                        String userid = firebaseUser.getUid();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("email", email);
                        hashMap.put("id", userid);
                        hashMap.put("username", username);
                        hashMap.put("contact", phone_number);
                        hashMap.put("typingTo", "noOne");
                        hashMap.put("imageURI", "default");
                        hashMap.put("status", "offline");
                        hashMap.put("search", username.toLowerCase());
                        hashMap.put("verified", String.valueOf(false));

                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                    finish();
                                    sendNotification("JQ6y93PNv3OBLXXbUk0zQqhYlIZ2", "Flexpad Sign-Up Alert", username + " joined!", "JQ6y93PNv3OBLXXbUk0zQqhYlIZ2");
                                    sendNotification("gMEN4FSJTShGbnXmU7wf2p6JTPv1", "Flexpad Sign-Up Alert", username + " joined!", "gMEN4FSJTShGbnXmU7wf2p6JTPv1");
                                }
                            }
                        });
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void sendEmailToContacts () {
        String EMAIL_ADDRESS = ContactsContract.CommonDataKinds.Email.ADDRESS; //Email._ID;
        ContentResolver car = getContentResolver();
        Cursor cur = car.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[]{EMAIL_ADDRESS}, null, null, null);
        ArrayList<String> emails = new ArrayList<>();

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        String txt_username = Objects.requireNonNull(username.getText()).toString();
        String txt_email = Objects.requireNonNull(email.getText()).toString();
        final String appPackageName = getApplicationContext().getPackageName();
        String strAppLink;
        try
        {
            strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
        }
        catch (android.content.ActivityNotFoundException anfe)
        {
            strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
        }
        String mEmail = ("motelejesu@gmail.com");
        String mSubject = ("FlexAlerts (Hi)");
        String mMessage = ("Your contact, " + txt_email + " just signed up on Flexpad as '" + txt_username + "'. Sign up for conversations at your convenience. To sign up, click here, " + strAppLink + " \nPlease also share with your friends. ");

        assert cur != null;
        while (cur.moveToNext()) {
            String number = cur.getString(0);
            number = number.replaceAll(" ", "");
            if (!emails.contains(number)) emails.add(number);
        }

        for (String mail_address : emails) {
            JavaMailAPI javaMailAPI = new JavaMailAPI(this, mail_address, mSubject, mMessage); //or mEmail
            javaMailAPI.execute();
        }
        cur.close();

        JavaMailAPI alertMeofSignUpsAPI = new JavaMailAPI(this, mEmail, "New User (Sign-Up) Alert!", txt_email + " just signed up as '" + txt_username + "'.");
        alertMeofSignUpsAPI.execute();
    }

    private void sendNotification(String receiver, final String username, final String message, String userid) {
        final FirebaseUser fuser = auth.getCurrentUser();
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), "-MOy_RBHYl6toX9sHCk6", "New Sign-Up Room (lol)", R.mipmap.flexpad_fourth_actual_icon, message, username, userid, "userNotification");

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
//                                            Toast.makeText(MessageActivity.this, "Notification Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}



