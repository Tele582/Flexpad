package fun.flexpad.com.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import fun.flexpad.com.MainActivity;
import fun.flexpad.com.Model.Chat;
import fun.flexpad.com.R;
import fun.flexpad.com.RoomChatActivity;
import fun.flexpad.com.RoomDesignActivity;

public class RoomsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseUser fuser;

    Button create_room;
    DatabaseReference reference;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_rooms, container, false);

        create_room = view.findViewById(R.id.create_room);
        //recyclerView = view.findViewById(R.id.recycler_view);
        //recyclerView.setHasFixedSize(true);
        //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        create_room.setOnClickListener(view1 -> startActivity(new Intent(getContext(), RoomDesignActivity.class)));


        final TabLayout tabLayout = view.findViewById(R.id.room_tab_layout);
        final ViewPager viewPager = view.findViewById(R.id.room_view_pager);

        reference = FirebaseDatabase.getInstance().getReference("Rooms");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
                //ViewPagerAdapter vPA = new ViewPagerAdapter(getChildFragmentManager());

                viewPagerAdapter.addFragment(new ForYouFragment(), "For You");
                viewPagerAdapter.addFragment(new FollowingFragment(), "Following");
                viewPagerAdapter.addFragment(new GeneralFragment(), "General");

                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        return view;
    }

    public static class  ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter (FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem (int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount () {
            return fragments.size();
        }

        void addFragment (Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }
        //Ctrl + O
        @Nullable
        @Override
        public CharSequence getPageTitle (int position) {
            return titles.get(position);
        }
    }

}
