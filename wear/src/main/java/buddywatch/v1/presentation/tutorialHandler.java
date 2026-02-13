package buddywatch.v1.presentation;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.health.services.client.HealthServicesClient;
import androidx.health.services.client.HealthServices;
import androidx.health.services.client.MeasureCallback;
import androidx.health.services.client.MeasureClient;
import androidx.health.services.client.data.Availability;
import androidx.health.services.client.data.DataType;
import androidx.health.services.client.data.DataPointContainer;
import androidx.health.services.client.data.DeltaDataType;
import androidx.health.services.client.data.SampleDataPoint;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import buddywatch.v1.R;

public class tutorialHandler extends Activity {

    MeasureCallback  callback;
    MeasureClient mClient;
    int curLine;
    private TextView textView;
    private TextView bpmView;

    double curbpm;
    ArrayList<Double> bpms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        // populates guide view
        setContentView(R.layout.activity_tutorial);
        textView = findViewById(R.id.textView);     // guide text display
        bpmView = findViewById(R.id.bpmView);       // heartrate display

        // passed in string from mobile app, points to guide
        String tutorial = getIntent().getStringExtra("TUTORIAL_PATH");
        checkPerms(tutorial);
    }

    public void checkPerms(String filename){

        if (checkSelfPermission(android.Manifest.permission.BODY_SENSORS)
                == PackageManager.PERMISSION_GRANTED) {
            trackHeartrate();

            startTut(filename);
        }
        else
        {
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 1001);
        }

    }

    public void startTut(String filename){

        String[] lines;
        try{
            lines = readFile(filename);
            curLine = 0;

            if(lines.length > 0){
                String out = "Step 1. " + lines[curLine];
                textView.setText(out);
            }

            findViewById(R.id.root).setOnClickListener(v -> {
                curLine++;
                if (curLine >= lines.length){
                    String out = "The Tutorial is over.";
                    textView.setText(out);


                    v.postDelayed(this::finish, 1000);
                    stopTracking();
                    return;
                }
                String out = "Step " + (curLine + 1) + ". " + lines[curLine];
                textView.setText(out);
            });

        }
        catch(IOException e){
            textView.setText("Error reading file.");
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

                trackHeartrate();

                startTut(getIntent().getStringExtra("TUTORIAL_PATH"));
            }
            else if(grantResults[0] == PackageManager.PERMISSION_DENIED){

                startTut(getIntent().getStringExtra("TUTORIAL_PATH"));

            }
        }
        else{
            Log.d("reqPerm", "line 130");
        }
    }

    public void trackHeartrate(){

        HealthServicesClient hClient = HealthServices.getClient(this);
        mClient = hClient.getMeasureClient();

        callback = new MeasureCallback() {
            @Override
            public void onAvailabilityChanged(@NonNull DeltaDataType<?, ?> deltaDataType, @NonNull Availability availability) {
                Log.d("tutorialHandler", "The availability has changed.");
            }

            @Override
            public void onDataReceived(DataPointContainer dataPointContainer) {

                Log.d("HR_DATA", "Received HR data");

                for(SampleDataPoint<Double> dp : dataPointContainer.getData(DataType.HEART_RATE_BPM)){
                    curbpm = dp.getValue();
                    bpms.add(curbpm);

                    runOnUiThread(() -> bpmView.setText(Double.toString(curbpm) + " bpm"));

                }

            }
        };

        mClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback);

    }

    public void stopTracking(){
        if(mClient != null && callback != null){
             mClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback);
        }
    }

    // Breaks down the file into displayable lines for startTut.
    public String[] readFile(String filename) throws IOException {

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

}
