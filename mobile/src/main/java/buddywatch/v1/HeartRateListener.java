package buddywatch.v1;

import android.util.Log;

import androidx.room.Room;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class HeartRateListener extends WearableListenerService {

    @Override
    public synchronized void onMessageReceived(MessageEvent msgEvent){

        GuideDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                GuideDatabase.class,
                "guide_database"
        ).build();

        if(msgEvent.getPath().equals(("/path"))){

            byte[] pathPayload = msgEvent.getData();
            String path = new String(pathPayload, StandardCharsets.UTF_8);
            ActivityDAO adao = db.adao();

            Thread dbInsertActivity = new Thread(() -> adao.insertActivity(new Activity(path, LocalDate.now())));

            try{
                dbInsertActivity.start();
                dbInsertActivity.join();
            } catch (InterruptedException e) {
                // TODO: Handle Gracefully
                throw new RuntimeException(e);
            }


        }else if(msgEvent.getPath().equals("/heart_rate_data")){

            byte[] payload = msgEvent.getData();
            ArrayList<HeartRateRecord> records = new ArrayList<>();

            ByteBuffer buffer = ByteBuffer.wrap(payload);
            while(buffer.hasRemaining()){
                // gets timestamp in seconds
                long timestamp = buffer.getLong() / 1000;
                double bpm = buffer.getDouble();

                records.add(new HeartRateRecord(timestamp, bpm));
                Log.d("Debug", "timestamp: " + timestamp + ". bpm: " + bpm);
            }

            assessHeartrate(records, db);

        }

    }

    private void assessHeartrate(ArrayList<HeartRateRecord> records, GuideDatabase db){

        // TODO: Maybe integrate a method that gets the users resting heart-rate by tracking it outside of guides?

        // Thresholds of stress, relative to resting heart-rate multiplied by each variable.
        final double MILD_STRESS = 1.1;         // 10 % increase
        final double MODERATE_STRESS = 1.2;     // 20 % increase
        final double SEVERE_STRESS = 1.3;       // 30 % increase
        final double RAPID_INCREASE = 0.15;     // 15 % rapid increase

        // Tracks last 2 heart-rate entries for rapid increase checking.
        HeartRateRecord minus1 = null;
        HeartRateRecord minus2 = null;

        String severity = "None";
        boolean rapidDetected = false;


        // Gets the user's average heart-rate.
        double average = 0;
        for(HeartRateRecord hr : records){

            average+= hr.bpm;

        }
        average = average / records.size();

        for(HeartRateRecord hr : records){

            // Checks the current bpm against the thresholds.
            if(average * SEVERE_STRESS <= hr.bpm && !severity.equals("Severe")){
                severity = "Severe";
            } else if(average * MODERATE_STRESS <= hr.bpm && severity.equals("None") || severity.equals("Mild")){
                severity = "Moderate";
            } else if (average * MILD_STRESS <= hr.bpm && severity.equals("None")) {
                severity = "Mild";
            }

            // Fills trackers for previous states, if both are full, checks that there is less than 8 seconds of time between all three measures, then checks if the difference in heart-rate measures is bigger than 15% of the original heart-rate.
            if(minus1 == null){
                minus1 = hr;
            }else if(minus2 == null){
                minus2 = minus1;
                minus1 = hr;
            } else{
                if(hr.timestamp - minus2.timestamp < 8 && (hr.bpm - minus2.bpm) > minus2.bpm * RAPID_INCREASE){
                    rapidDetected = true;
                }

                minus2 = minus1;
                minus1 = hr;

            }

            HeartEventDAO hedao = db.hedao();
            ActivityDAO adao = db.adao();
            final String fseverity = severity;
            final boolean frapid = rapidDetected;

            Thread dbInsertHeartEvent = new Thread(() -> {
                Activity recent = adao.getRecentActivity();
                hedao.insertHeartEvent(new HeartEvent(recent.guidePath, recent.id, fseverity, frapid));
            });

            dbInsertHeartEvent.start();


        }

    }
}
