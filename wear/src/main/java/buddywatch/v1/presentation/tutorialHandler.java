package buddywatch.v1.presentation;

import android.app.Activity;
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

    double curbpm;
    ArrayList<Double> bpms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_tutorial);
        textView = findViewById(R.id.textView);

        String tutorial = getIntent().getStringExtra("TUTORIAL_PATH");
        startTut(tutorial);
    }

    public void startTut(String filename){

        // Call trackHeartrate method to begin tracking the heartrate of the user.
        trackHeartrate();

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

                    // Stop tracking the user's heartrate.
                    stopTracking();

                    v.postDelayed(this::finish, 1000);
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

                for(SampleDataPoint<Double> dp : dataPointContainer.getData(DataType.HEART_RATE_BPM)){
                    curbpm = dp.getValue();
                    bpms.add(curbpm);

                    // TODO: runOnUiThread(() -> );

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
