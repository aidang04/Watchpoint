package buddywatch.v1.ui;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import buddywatch.v1.util.HeartRateManager;
import buddywatch.v1.R;

public class TutorialHandler extends Activity implements HeartRateManager.HeartRateListener {

    private HeartRateManager heartRateManager;
    private int curLine;
    private TextView textView;
    private TextView bpmView;

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // populates guide view
        setContentView(R.layout.activity_guide);
        textView = findViewById(R.id.textView);     // guide text display
        bpmView = findViewById(R.id.bpmView);       // heartrate display

        if(prefs.getBoolean("yellow_background", false)){
            findViewById(R.id.background).setBackground(ContextCompat.getDrawable(this, R.drawable.rect_border_yellow));
        }

        // passed in string from mobile app, points to guide
        String tutorial = getIntent().getStringExtra("TUTORIAL_PATH");
        checkPerms(tutorial);
    }

    private void checkPerms(String filename){

        if (checkSelfPermission(android.Manifest.permission.BODY_SENSORS)
                == PackageManager.PERMISSION_GRANTED) {
            heartRateManager = new HeartRateManager(this, this);
            heartRateManager.startTracking();
            startTut(filename);
        }
        else
        {
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 1001);
        }

    }

    private void startTut(String filename){

        String[] lines;
        try{
            lines = readFile(filename);
            curLine = 0;

            if(lines.length > 0){
                String out = "Step 1. " + lines[curLine];
                textView.setText(out);
            }

            findViewById(R.id.forward).setOnClickListener(v -> {
                curLine++;
                Log.d("Debug", String.valueOf(curLine) + "hi");
                if (curLine == lines.length){

                    String out = "The Tutorial is over.";
                    textView.setText(out);

                    heartRateManager.stopTracking();
                    heartRateManager.sendData("/guide_data", getIntent().getStringExtra("TUTORIAL_PATH"));

                    v.postDelayed(this::finish, 1000);
                    return;
                }

                if(curLine > 0 && findViewById(R.id.chevronLeft).getVisibility() != View.VISIBLE){
                    // Sets the chevron indicating it is possible to go back to visible.
                    findViewById(R.id.chevronLeft).setVisibility(View.VISIBLE);
                }

                String out = "Step " + (curLine + 1) + ". " + lines[curLine];
                textView.setText(out);
            });

            findViewById(R.id.back).setOnClickListener(v -> {
                if (!(curLine < 1)){
                curLine--;
                String out = "Step " + (curLine + 1) + ". " + lines[curLine];
                textView.setText(out);

                if(curLine == 0){
                    findViewById(R.id.chevronLeft).setVisibility(View.INVISIBLE);
                }
                }
            });

        }
        catch(IOException e){
            textView.setText(R.string.fileError);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                heartRateManager = new HeartRateManager(this, this);
                heartRateManager.startTracking();

                startTut(getIntent().getStringExtra("TUTORIAL_PATH"));
            }
            else if(grantResults[0] == PackageManager.PERMISSION_DENIED){

                startTut(getIntent().getStringExtra("TUTORIAL_PATH"));

            }
        }
    }

    // Breaks down the file into displayable lines for startTut.
    private String[] readFile(String filename) throws IOException {

        InputStream is = getAssets().open(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String ln;

        while ((ln = reader.readLine()) != null){
            sb.append(ln).append("\n");
        }
        reader.close();
        is.close();

        return sb.toString().split("\n");

    }

    @Override
    public void onHeartRateUpdate(int bpm){
        runOnUiThread(() -> bpmView.setText(getResources().getString(R.string.bpm, bpm)));
    }

}
