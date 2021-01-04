package fun.flexpad.com.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import fun.flexpad.com.R;
import fun.flexpad.com.databinding.FragmentUsersBinding;

public class UsersFragment extends Fragment {

    FragmentUsersBinding fragmentUsersBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentUsersBinding = FragmentUsersBinding.inflate(inflater, container, false);

        return fragmentUsersBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());

        viewPagerAdapter.addFragment(new ContactsFragment(), "Contacts");
        viewPagerAdapter.addFragment(new AllUsersFragment(), "All Users");

        fragmentUsersBinding.usersViewPager.setAdapter(viewPagerAdapter);
        fragmentUsersBinding.usersTabLayout.setupWithViewPager(fragmentUsersBinding.usersViewPager);

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