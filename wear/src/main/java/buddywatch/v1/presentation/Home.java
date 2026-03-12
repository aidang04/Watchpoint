package buddywatch.v1.presentation;

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
        Switch bold = findViewById(R.id.bold);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        yellow.setChecked(prefs.getBoolean("yellow_background", false));
        yellow.setOnCheckedChangeListener((buttonView, isChecked) ->
            prefs.edit().putBoolean("yellow_background", isChecked).apply()
        );

    }

}
