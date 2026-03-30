package buddywatch.v1.ui;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import buddywatch.v1.util.HeartRateManager;
import buddywatch.v1.R;

public class RestingHeartRateTracker extends Activity implements HeartRateManager.HeartRateListener {

    private HeartRateManager heartRateManager;
    private Chronometer timer;
    private TextView bpmView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resting);

        timer = findViewById(R.id.timer);
        bpmView = findViewById(R.id.bpmView);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        if(prefs.getBoolean("yellow_background", false)){
            findViewById(R.id.background).setBackground(ContextCompat.getDrawable(this, R.drawable.rect_border_yellow));
        }

        checkPerms();

    }

    private void checkPerms(){

        if (checkSelfPermission(android.Manifest.permission.BODY_SENSORS)
                == PackageManager.PERMISSION_GRANTED) {
            heartRateManager = new HeartRateManager(this, this);
            heartRateManager.startTracking();
            startTimer();
        }
        else
        {
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 1001);
        }

    }

    private void startTimer(){

        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        findViewById(R.id.stop).setOnClickListener(V -> {
            timer.stop();
            heartRateManager.sendData("/resting_data");
            finish();
        });

    }

    @Override
    public void onHeartRateUpdate(int bpm){
        runOnUiThread(() -> bpmView.setText(getResources().getString(R.string.bpm, bpm)));
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(heartRateManager != null) heartRateManager.stopTracking();
    }


}
