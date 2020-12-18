package fun.flexpad.com.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import fun.flexpad.com.Model.Voice;
import fun.flexpad.com.R;
import fun.flexpad.com.Model.Room;
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
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        create_room.setOnClickListener(view1 -> startActivity(new Intent(getContext(), RoomDesignActivity.class)));

//        String message_to_main = "messageToMain";
//        final String textFromPrevActivity = Objects.requireNonNull(getActivity()).getIntent().getStringExtra("textFromMessageToMainActivity");
//        if (textFromPrevActivity.equals(message_to_main)) {
//            Navigation.findNavController(requireView()).navigate(R.id.action_roomsFragment_to_chatsFragment);
//        }
        final TabLayout tabLayout = view.findViewById(R.id.room_tab_layout);
        final ViewPager viewPager = view.findViewById(R.id.room_view_pager);

        reference = FirebaseDatabase.getInstance().getReference("VoiceClips");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try { ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager()); //ViewPagerAdapter vPA = new ViewPagerAdapter(getFragmentManager());

                        DatabaseReference followReference = FirebaseDatabase.getInstance().getReference();
                        followReference.child("FollowList").child(fuser.getUid()).child("following")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ArrayList<String> followingList = new ArrayList<>();

                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            String followed = dataSnapshot.getKey();
                                            followingList.add(followed);
                                        }

                                        followReference.child("Rooms").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                ArrayList<String> roomFollowListID = new ArrayList<>();
                                                roomFollowListID.clear();
                                                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                                    Room room = dataSnapshot1.getValue(Room.class);
                                                    String roomCreator = snapshot.child(room.getRoomKey()).child("creator").getValue(String.class); // or //String roomCreator = snapshot.child(room.getRoomKey()).child("creator").getValue(String.class);

                                                    if (followingList.contains(roomCreator) || roomCreator.equals(fuser.getUid()))
                                                        roomFollowListID.add(room.getRoomKey());
                                                }

                                                ArrayList<String> seenUsersList = new ArrayList<>();
                                                followReference.child("VoiceClips").addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        int unread = 0;
                                                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                            Voice voice = snapshot1.getValue(Voice.class);
                                                            assert voice != null;

                                                            for (DataSnapshot dataSnapshot2 : snapshot1.child("seenBy").getChildren()) {
                                                                seenUsersList.add(dataSnapshot2.getKey());
                                                            }

                                                            if (!seenUsersList.contains(fuser.getUid()) && roomFollowListID.contains(voice.getRoomID())) {
                                                                unread++;
                                                            }
                                                            seenUsersList.clear();
                                                        }


                                                        viewPagerAdapter.addFragment(new ForYouFragment(), "For You");
                                                        if (unread == 0) {
                                                            viewPagerAdapter.addFragment(new FollowingFragment(), "Following ("+unread+")");
                                                        } else {
                                                            viewPagerAdapter.addFragment(new FollowingFragment(), "Following ("+unread+")");
                                                        }
                                                        viewPagerAdapter.addFragment(new GeneralFragment(), "General");

                                                        viewPager.setAdapter(viewPagerAdapter);
                                                        tabLayout.setupWithViewPager(viewPager);

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });



                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                } catch (Exception e) {e.getStackTrace();}
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
