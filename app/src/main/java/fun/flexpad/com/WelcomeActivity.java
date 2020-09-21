package fun.flexpad.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);

        int FLEX_LOGO_TIME = 2000;
        new Handler().postDelayed(() -> {
            Intent intent = new Intent (WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, FLEX_LOGO_TIME);
    }
}
