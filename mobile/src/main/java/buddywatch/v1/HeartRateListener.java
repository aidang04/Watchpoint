package buddywatch.v1;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class HeartRateListener extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent msgEvent){

        if(msgEvent.getPath().equals("/heart_rate_data")){

            byte[] payload = msgEvent.getData();
            ArrayList<HeartRateRecord> records = new ArrayList<>();

            ByteBuffer buffer = ByteBuffer.wrap(payload);
            while(buffer.hasRemaining()){
                long timestamp = buffer.getLong();
                double bpm = buffer.getDouble();

                records.add(new HeartRateRecord(timestamp, bpm));
            }

        }

    }
}
