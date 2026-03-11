package buddywatch.v1.presentation;

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

import java.util.ArrayList;

public class HeartRateManager {

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

    public void stopTracking(){
        if(mClient != null && callback != null){
            mClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback);
        }
    }

    public ArrayList<HeartRateRecord> getRecorder(){
        return recorder;
    }

}
