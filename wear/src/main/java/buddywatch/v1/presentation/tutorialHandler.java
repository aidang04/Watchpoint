package buddywatch.v1.presentation;

import static java.time.MonthDay.now;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
    ArrayList<HeartRateRecord> recorder = new ArrayList<>();

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

            findViewById(R.id.forward).setOnClickListener(v -> {
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

            findViewById(R.id.back).setOnClickListener(v -> {
                if (!(curLine < 1)){
                curLine--;
                String out = "Step " + (curLine + 1) + ". " + lines[curLine];
                textView.setText(out);
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

                trackHeartrate();

                startTut(getIntent().getStringExtra("TUTORIAL_PATH"));
            }
            else if(grantResults[0] == PackageManager.PERMISSION_DENIED){

                startTut(getIntent().getStringExtra("TUTORIAL_PATH"));

            }
        }
    }

    public void trackHeartrate(){

        HealthServicesClient hClient = HealthServices.getClient(this);
        mClient = hClient.getMeasureClient();
        Resources res = getResources();

        callback = new MeasureCallback() {
            @Override
            public void onAvailabilityChanged(@NonNull DeltaDataType<?, ?> deltaDataType, @NonNull Availability availability) {
                Log.d("tutorialHandler", "The availability has changed.");
            }

            @Override
            public void onDataReceived(DataPointContainer dataPointContainer) {

                for(SampleDataPoint<Double> dp : dataPointContainer.getData(DataType.HEART_RATE_BPM)){

                    recorder.add(new HeartRateRecord(System.currentTimeMillis(), dp.getValue()));
                    runOnUiThread(() -> bpmView.setText(res.getString(R.string.bpm, dp.getValue().intValue())));

                }

            }
        };

        mClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback);

    }

    public void stopTracking(){
        if(mClient != null && callback != null){
             mClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback);
             sendData();
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

    private void sendData(){

        String path = getIntent().getStringExtra("TUTORIAL_PATH");

        // Creates a buffer with enough memory to hold all entries in the Heartrate + Timestamp recorder.
        ByteBuffer buffer = ByteBuffer.allocate(recorder.size() * 16);

        // Loops through recorder and adds each element to the buffer.
        for(HeartRateRecord r : recorder){
            buffer.putLong(r.timestamp);
            buffer.putDouble(r.bpm);
        }

        byte[] pathPayload = path != null ? path.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] payload = buffer.array();

        Wearable.getNodeClient(this).getConnectedNodes().addOnSuccessListener(nodes -> {
            for(Node node : nodes){
                Wearable.getMessageClient(this).sendMessage(node.getId(), "/path", pathPayload);
                Wearable.getMessageClient(this).sendMessage(node.getId(), "/heart_rate_data", payload);
            }
        });

    }

}
