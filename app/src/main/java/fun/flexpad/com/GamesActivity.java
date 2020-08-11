package fun.flexpad.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class GamesActivity extends AppCompatActivity {

    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);

        listView = findViewById(R.id.gamelist);
        ArrayList<String> arrayList = new ArrayList<>();

        arrayList.add("Fact or Fake?");
        arrayList.add("Classifie");

        ArrayAdapter arrayAdapter = new ArrayAdapter (this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    startActivity(new Intent(GamesActivity.this, FactOrFakeActivity.class));
                } //else if (position == 1) {
                    //startActivity(new Intent(GamesActivity.this, FactOrFakeActivity.class));

                //}
            }
        });

    }

}
