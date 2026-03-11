package buddywatch.v1.util;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import buddywatch.v1.dao.RestingHeartDAO;
import buddywatch.v1.database.GuideDatabase;
import buddywatch.v1.model.RestingHeart;

public class RestingRateListener extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent msgEvent) {

        Log.d("RestingRateListener", "Readings Received.");
        GuideDatabase db = GuideDatabaseConnection.getInstance(getApplicationContext()).getDb();

        if(msgEvent.getPath().equals("/resting_data")){

            byte[] payload = msgEvent.getData();
            ByteBuffer buffer = ByteBuffer.wrap(payload);

            ArrayList<RestingHeart> records = new ArrayList<>();
            while(buffer.hasRemaining()){
                // skip timestamp
                buffer.getLong();
                // store bpm
                records.add(new RestingHeart( (int) buffer.getDouble()));
            }

            RestingHeartDAO rhdao = db.rhdao();
            Thread dbInsertAllData = new Thread(() -> rhdao.insertAll(records));
            dbInsertAllData.start();

        }

    }

}
