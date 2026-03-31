package buddywatch.v1.util;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

import buddywatch.v1.dao.ActivityDAO;
import buddywatch.v1.dao.HeartEventDAO;
import buddywatch.v1.dao.RestingHeartDAO;
import buddywatch.v1.database.GuideDatabase;
import buddywatch.v1.model.Activity;
import buddywatch.v1.model.HeartEvent;

public class HeartRateListener extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent msgEvent){

        GuideDatabase db = GuideDatabaseConnection.getInstance(getApplicationContext()).getDb();

        if(msgEvent.getPath().equals("/guide_data")) {

            byte[] payload = msgEvent.getData();
            ByteBuffer buffer = ByteBuffer.wrap(payload);

            int pathLen = buffer.getInt();
            byte[] pathPayload = new byte[pathLen];
            buffer.get(pathPayload);
            String path = new String(pathPayload, StandardCharsets.UTF_8);

            ArrayList<HeartRateRecord> records = new ArrayList<>();
            while (buffer.hasRemaining()) {
                // gets timestamp in seconds
                long timestamp = buffer.getLong() / 1000;
                double bpm = buffer.getDouble();

                records.add(new HeartRateRecord(timestamp, bpm));
            }

            ActivityDAO adao = db.adao();
            RestingHeartDAO rhdao = db.rhdao();
            Thread dbInsertActivity = new Thread(() -> {
                adao.insertActivity(new Activity(path, Date.valueOf(LocalDate.now().toString())));

                float averageResting = rhdao.getAverageBPM();

                // Checks if averageResting has anything in it. If it does, uses that, if it doesn't gets the average bpm from the session.
                double baseline = averageResting > 0 ? averageResting : records.stream().mapToDouble(r -> r.bpm).average().orElse(0);

                assessHeartRate(records, db, baseline);
            });

            dbInsertActivity.start();
        }
    }

    private void assessHeartRate(ArrayList<HeartRateRecord> records, GuideDatabase db, double average){

        // Thresholds of stress, relative to resting heart-rate multiplied by each variable.
        final double MILD_STRESS = 1.1;         // 10 % increase
        final double MODERATE_STRESS = 1.2;     // 20 % increase
        final double SEVERE_STRESS = 1.3;       // 30 % increase

        // Tracks last 2 heart-rate entries for rapid increase checking.
        HeartRateRecord minus1 = null;
        HeartRateRecord minus2 = null;

        String severity = "None";

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
                minus2 = minus1;
                minus1 = hr;

            }

        }

        HeartEventDAO hedao = db.hedao();
        ActivityDAO adao = db.adao();
        final int faverage = (int) average;
        final String fseverity = severity;

        Activity recent = adao.getRecentActivity();

        if(recent != null){
            hedao.insertHeartEvent(new HeartEvent(recent.guidePath, recent.id, faverage, fseverity));
        }

    }
}
