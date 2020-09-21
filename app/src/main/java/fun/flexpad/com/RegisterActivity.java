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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
//import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText username, email, password;
    Button btn_register;

    //String contact_no;
    MaterialEditText phone_number;

    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                    getEmailAddresses();
                } else {
                    // You can directly ask for the permission.
                    ActivityCompat.requestPermissions(RegisterActivity.this,
                            new String[] { Manifest.permission.READ_CONTACTS },
                            Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            }
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
                getEmailAddresses();

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
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username);
                            hashMap.put("contact", phone_number);
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("imageURI", "default");
                            hashMap.put("status", "offline");
                            hashMap.put("search", username.toLowerCase());

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);



                                        finish();
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void getPhoneNumbers() {
        String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{PHONE_NUMBER}, null, null, null);
        ArrayList<String> phones = new ArrayList<>();
        assert cur != null;
        //cur.moveToFirst();
        while (cur.moveToNext()) {
            String number = cur.getString(0);
            number = number.replaceAll(" ", "");
            if (!phones.contains(number)) phones.add(number);
        }
        cur.close();
    }

    public void getEmailAddresses() {
        String EMAIL_ADDRESS = ContactsContract.CommonDataKinds.Email.ADDRESS; //Email._ID;
        ContentResolver car = getContentResolver();
        Cursor cur = car.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[]{EMAIL_ADDRESS}, null, null, null);
        ArrayList<String> emails = new ArrayList<>();

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);

        String txt_username = Objects.requireNonNull(username.getText()).toString();
        String txt_email = Objects.requireNonNull(email.getText()).toString();

        final String appPackageName = getApplicationContext().getPackageName();
        String strAppLink = "";

        try
        {
            strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
        }
        catch (android.content.ActivityNotFoundException anfe)
        {
            strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
        }

        String mEmail = ("telelekan@gmail.com");
        String mSubject = ("FlexAlerts (Hi)");
        String mMessage = ("Your contact, " + txt_email + " just signed up on Flexpad as '" + txt_username + "'. Sign up too to find amazing podcasts, send money with friends, send anonymous messages and more. To sign up, click here, " + strAppLink + " \nPlease share with your friends too. ");

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
    }

    private void sendEmail() {

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);

        String txt_username = Objects.requireNonNull(username.getText()).toString();
        String txt_email = Objects.requireNonNull(email.getText()).toString();

        final String appPackageName = getApplicationContext().getPackageName();
        String strAppLink = "";

        try
        {
            strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
        }
        catch (android.content.ActivityNotFoundException anfe)
        {
            strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
        }

        String mEmail = ("telelekan@gmail.com");
        String mSubject = ("FlexAlerts (Hii)");
        String mMessage = ("Your contact, " + txt_email + " just signed up on Flexpad as '" + txt_username + "'. To sign up too, click here, " + strAppLink);

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, mEmail, mSubject, mMessage);
        javaMailAPI.execute();

    }

}



