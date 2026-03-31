package buddywatch.v1.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.health.services.client.HealthServices;
import androidx.health.services.client.MeasureCallback;
import androidx.health.services.client.MeasureClient;
import androidx.health.services.client.data.Availability;
import androidx.health.services.client.data.DataPointContainer;
import androidx.health.services.client.data.DataType;
import androidx.health.services.client.data.DeltaDataType;
import androidx.health.services.client.data.SampleDataPoint;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class HeartRateManager {

    // interface that defines what happens to the UI when a heart-rate update is received.
    public interface HeartRateListener{
        void onHeartRateUpdate(int bpm);
    }

    private MeasureCallback callback;
    private MeasureClient mClient;
    private final ArrayList<HeartRateRecord> recorder = new ArrayList<>();
    private final Context context;
    private final HeartRateListener listener;

    public HeartRateManager(Context context, HeartRateListener listener){
        this.context = context;
        this.listener = listener;
    }

    // starts tracking heart-rate
    public void startTracking(){
        mClient = HealthServices.getClient(context).getMeasureClient();

        callback = new MeasureCallback() {
            @Override
            public void onAvailabilityChanged(@NonNull DeltaDataType<?, ?> deltaDataType, @NonNull Availability availability) {
                Log.d("HeartRateManager", "Availability changed.");
            }

            @Override
            public void onDataReceived(@NonNull DataPointContainer dataPointContainer) {
                for(SampleDataPoint<Double> dp : dataPointContainer.getData(DeltaDataType.HEART_RATE_BPM)){
                    recorder.add(new HeartRateRecord(System.currentTimeMillis(), dp.getValue()));
                    if(listener != null){
                        listener.onHeartRateUpdate(dp.getValue().intValue());
                    }
                }

            }
        };
        mClient.registerMeasureCallback(DeltaDataType.HEART_RATE_BPM, callback);
    }

    // stops tracking and unregisters the measure callback
    public void stopTracking(){
        if(mClient != null && callback != null){
            mClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback);
        }
    }

    // overload method for sendData
    public void sendData(String path){
        sendData(path, null);
    }

    // packages heart-rate info and sends it, if provided a guidePath it will send that too. tags message with messagePath for destination
    public void sendData(String messagePath, String guidePath){

        // if guidePath is empty, stays empty, if it isn't it's contents are converted to byte array.
        byte[] pathPayload = (guidePath != null && !guidePath.isEmpty()) ? guidePath.getBytes(StandardCharsets.UTF_8) : new byte[0];

        ByteBuffer buffer = ByteBuffer.allocate(4 + pathPayload.length + recorder.size()* 16);
        buffer.putInt(pathPayload.length);
        buffer.put(pathPayload);

        for(HeartRateRecord r : recorder){
            buffer.putLong(r.timestamp);
            buffer.putDouble(r.bpm);
        }

        byte[] payload = buffer.array();

        Wearable.getNodeClient(context).getConnectedNodes().addOnSuccessListener(nodes -> {
            for(Node node : nodes){
                Wearable.getMessageClient(context).sendMessage(node.getId(), messagePath, payload).addOnFailureListener(e -> Log.d("HeartRateManager", "Failed to send."));
            }
        }).addOnFailureListener(e -> Log.d("HeartRateManager", "No nodes found"));

    }

}
