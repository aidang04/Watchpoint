package buddywatch.v1.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import androidx.activity.ComponentActivity;

import buddywatch.v1.R;

public class Home extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_home);

        Switch yellow = findViewById(R.id.yellow);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        yellow.setChecked(prefs.getBoolean("yellow_background", false));
        yellow.setOnCheckedChangeListener((buttonView, isChecked) ->
            prefs.edit().putBoolean("yellow_background", isChecked).apply()
        );

    }

}
