package buddywatch.v1.util;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

import buddywatch.v1.dao.ActivityDAO;
import buddywatch.v1.dao.HeartEventDAO;
import buddywatch.v1.database.GuideDatabase;
import buddywatch.v1.model.Activity;
import buddywatch.v1.model.HeartEvent;

public class HeartRateListener extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent msgEvent){

        Log.d("Debug", "Received");

        GuideDatabase db = GuideDatabaseConnection.getInstance(getApplicationContext()).getDb();

        if(msgEvent.getPath().equals("/guide_data")){

            byte[] payload = msgEvent.getData();
            ByteBuffer buffer = ByteBuffer.wrap(payload);

            int pathLen = buffer.getInt();
            byte[] pathPayload = new byte[pathLen];
            buffer.get(pathPayload);
            String path = new String(pathPayload, StandardCharsets.UTF_8);

            ArrayList<HeartRateRecord> records = new ArrayList<>();
            while(buffer.hasRemaining()){
                // gets timestamp in seconds
                long timestamp = buffer.getLong() / 1000;
                double bpm = buffer.getDouble();

                records.add(new HeartRateRecord(timestamp, bpm));
            }

            ActivityDAO adao = db.adao();
            Thread dbInsertActivity = new Thread(() -> {
                adao.insertActivity(new Activity(path, Date.valueOf(LocalDate.now().toString())));
                assessHeartrate(records, db);
            });

            try{
                dbInsertActivity.start();
                dbInsertActivity.join();
            } catch (InterruptedException e) {
                ErrorHandler.handle(e, getApplicationContext(), "Database Error. \n Please contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk.");
            }
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

        for(HeartRateRecord hr : records) {

            // Checks the current bpm against the thresholds.
            if (average * SEVERE_STRESS <= hr.bpm && !severity.equals("Severe")) {
                severity = "Severe";
            } else if (average * MODERATE_STRESS <= hr.bpm && (severity.equals("None") || severity.equals("Mild"))) {
                severity = "Moderate";
            } else if (average * MILD_STRESS <= hr.bpm && severity.equals("None")) {
                severity = "Mild";
            }

            // Fills trackers for previous states, if both are full, checks that there is less than 8 seconds of time between all three measures, then checks if the difference in heart-rate measures is bigger than 15% of the original heart-rate.
            if (minus1 == null) {
                minus1 = hr;
            } else if (minus2 == null) {
                minus2 = minus1;
                minus1 = hr;
            } else {
                if (hr.timestamp - minus2.timestamp < 8 && (hr.bpm - minus2.bpm) > minus2.bpm * RAPID_INCREASE) {
                    rapidDetected = true;
                }

                minus2 = minus1;
                minus1 = hr;

            }

        }

        HeartEventDAO hedao = db.hedao();
        ActivityDAO adao = db.adao();
        final int faverage = Math.round(Math.round(average));
        final String fseverity = severity;
        final boolean frapid = rapidDetected;

        Activity recent = adao.getRecentActivity();

        if(recent != null){
            hedao.insertHeartEvent(new HeartEvent(recent.guidePath, recent.id, faverage, fseverity, frapid));
        }

    }
}
