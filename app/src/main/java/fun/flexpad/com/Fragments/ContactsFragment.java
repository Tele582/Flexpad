package fun.flexpad.com.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fun.flexpad.com.Adapter.UserAdapter;
import fun.flexpad.com.Model.User;
import fun.flexpad.com.R;

public class ContactsFragment extends Fragment {

    private List<User> mUsers;

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    FirebaseUser fuser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            addContacts();
        } else {
            // You can directly ask for the permission.
            ActivityCompat.requestPermissions((Activity) requireContext(),
                    new String[] { Manifest.permission.READ_CONTACTS },
                    Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        return view;
    }

    private void addContacts() {

        mUsers = new ArrayList<>();

        String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        ContentResolver cr = requireContext().getContentResolver();
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

        DatabaseReference contacts_reference = FirebaseDatabase.getInstance().getReference("Users"); //final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        contacts_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (String num : phones) {
                        assert user != null;
                        assert fuser != null;
                        try {
                            if (user.getContact().equals(num) && !user.getId().equals(fuser.getUid())){
                                mUsers.add(user);
                            }
                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), mUsers, false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Permission is granted. Continue the action or workflow in your app.
                addContacts();
            } else {
                Toast.makeText(getContext(), "Contacts not available'. ", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    static class Constants{
        public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    }
}




