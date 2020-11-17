package fun.flexpad.com;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import fun.flexpad.com.Fragments.ChatsFragment;
import fun.flexpad.com.Fragments.ContactsFragment;
import fun.flexpad.com.Fragments.ProfileFragment;
import fun.flexpad.com.Fragments.RoomsFragment;
import fun.flexpad.com.Fragments.UsersFragment;
import fun.flexpad.com.Model.Chat;
import fun.flexpad.com.Model.Room;
import fun.flexpad.com.Model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    CircleImageView profile_image, verification_image;
    TextView username;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    ImageView fullScreenContainer, backFromPic;
    LinearLayout backFromPicBg;

    int roomsNumber = 0;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean isAccelerometerSensorAvailable, itIsNotFirstTime = false;
    private float currentX, currentY, currentZ, lastX, lastY, lastZ;
    private float xDifference, yDifference, zDifference;
    private float shakeThreshold = 5f;
    private Vibrator vibrator;

    @Override
    public void onBackPressed() {
        if (fullScreenContainer.getVisibility() == View.VISIBLE) {
            fullScreenContainer.setImageDrawable(null);
            fullScreenContainer.setVisibility(View.GONE);
            backFromPicBg.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupWithNavController();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        showFloatingButton();
        shakeToRoom();

        profile_image = findViewById(R.id.profile_image);
        verification_image = findViewById(R.id.verification_image);
        username = findViewById(R.id.username);
        fullScreenContainer = findViewById(R.id.full_screen_container);
        backFromPic = findViewById(R.id.back_from_pic);
        backFromPicBg = findViewById(R.id.back_from_pic_bg);

        showUserDetails();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()){
                        unread++;
                    }
                }

                viewPagerAdapter.addFragment(new RoomsFragment(), "Rooms");

                if (unread == 0){
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");

                } else {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats ( "+unread+" )");
                }
                viewPagerAdapter.addFragment(new ContactsFragment(), "Contacts");
                viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");
                viewPagerAdapter.addFragment(new UsersFragment(), "Users");

                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            //might cause app to crash;
            startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return true;
        }
        else if (item.getItemId() == R.id.games) {
            startActivity(new Intent(MainActivity.this, GamesActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return true;
        }
        else if (item.getItemId() == R.id.feedbackmessage) {
            startActivity(new Intent(MainActivity.this, FeedbackActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return true;
        }
        return false;
    }

    public void shakeToRoom() {

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelerometerSensorAvailable = true;
        } else {
            Toast.makeText(this, "Accelerometer Sensor Not Available", Toast.LENGTH_SHORT).show();
            isAccelerometerSensorAvailable = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        currentX = event.values[0];
        currentY = event.values[1];
        currentZ = event.values[2];

        if (itIsNotFirstTime) {
            xDifference  = Math.abs(lastX - currentX);
            yDifference  = Math.abs(lastY - currentY);
            zDifference  = Math.abs(lastZ - currentZ);

            if ((xDifference > shakeThreshold && yDifference > shakeThreshold) ||
                    (xDifference > shakeThreshold && zDifference > shakeThreshold) ||
                    (yDifference > shakeThreshold && zDifference > shakeThreshold)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500); //deprecated in API 26
                }
                ArrayList<Room> mRooms = new ArrayList<>();
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Rooms");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Room rm = dataSnapshot.getValue(Room.class);
                            assert rm != null;
                            roomsNumber++;
                            mRooms.add(rm);
                        }
                        Random rand = new Random();
                        int lowRange = 1;
                        int highRange = roomsNumber;
                        final int randomRoomNo = rand.nextInt(highRange - lowRange);
                        final Room room = mRooms.get(randomRoomNo);
                        Intent intent = new Intent(MainActivity.this, RoomChatActivity.class);
                        intent.putExtra("Room_Name", room.getRoomname());
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }

        lastX = currentX;
        lastY = currentY;
        lastZ = currentZ;
        itIsNotFirstTime = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static class  ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        //Ctrl + O

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private  void showFloatingButton () {

        FloatingActionButton floatingShareButton = (FloatingActionButton) findViewById(R.id.floatingShareButton);
        floatingShareButton.setOnClickListener(v -> {
            //add the sharing option to the floating action button

            Intent a = new Intent(Intent.ACTION_SEND);

            //this is to get the app link in the Play Store without launching your app.
            final String appPackageName = getApplicationContext().getPackageName();
            String strAppLink = "";

            try {
                strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
            }
            catch (android.content.ActivityNotFoundException anfe) {
                strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
            }

            // this is the sharing part
            a.setType("text/plain"); //text/plain for just text
            String shareBody = "Download now for conversations at your convenience." +
                    "\n"+""+strAppLink;
            String shareSub = "APP NAME/TITLE";
            a.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            //a.putExtra(android.content.Intent.EXTRA_TITLE, shareSub);
            a.putExtra(Intent.EXTRA_TEXT, shareBody);

            startActivity(Intent.createChooser(a, "Share Using"));

            //Give users incentive to share. Maybe free flexcoins.

        });
    }

    private  void showUserDetails () {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getUsername());
                if (user.getImageURI().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURI()).into(profile_image);
                }
                if (user.getVerified().equals("true")) {
                    verification_image.setVisibility(View.VISIBLE);
                }
                profile_image.setOnClickListener(v -> {
                    if (user.getImageURI().equals("default")){
                        fullScreenContainer.setImageResource(R.mipmap.ic_launcher);
                        fullScreenContainer.setVisibility(View.VISIBLE);
                        backFromPicBg.setVisibility(View.VISIBLE);
                        fullScreenContainer.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    } else {
                        Glide.with(getApplicationContext()).load(user.getImageURI()).into(fullScreenContainer);
                        fullScreenContainer.setVisibility(View.VISIBLE);
                        backFromPicBg.setVisibility(View.VISIBLE);
                        fullScreenContainer.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    }
                });
                backFromPic.setOnClickListener(v -> {
                    fullScreenContainer.setImageDrawable(null);
                    fullScreenContainer.setVisibility(View.GONE);
                    backFromPicBg.setVisibility(View.GONE);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private  void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        if (isAccelerometerSensorAvailable) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
        if (isAccelerometerSensorAvailable) {
            sensorManager.unregisterListener(this);
        }
    }

    //@Override
    //protected void onStop() {
    //    super.onStop() ;
    //    status("offline");
    //}
}
