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

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import fun.flexpad.com.Fragments.ChatsFragment;
import fun.flexpad.com.Fragments.ContactsFragment;
import fun.flexpad.com.Fragments.ProfileFragment;
import fun.flexpad.com.Fragments.RoomsFragment;
import fun.flexpad.com.Fragments.UsersFragment;
import fun.flexpad.com.Model.Chat;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupWithNavController();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

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
            String shareBody = "Download now to have live conversations at your convenience." +
                    "\n"+""+strAppLink;
            String shareSub = "APP NAME/TITLE";
            a.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            //a.putExtra(android.content.Intent.EXTRA_TITLE, shareSub);
            a.putExtra(Intent.EXTRA_TEXT, shareBody);

            startActivity(Intent.createChooser(a, "Share Using"));

            //Give users incentive to share. Maybe free flexcoins.

        });

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

    //@Override
    //protected void onStop() {
    //    super.onStop() ;
    //    status("offline");
    //}
}
