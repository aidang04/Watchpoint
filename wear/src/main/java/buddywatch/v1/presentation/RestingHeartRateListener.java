package buddywatch.v1.presentation;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.health.services.client.HealthServices;
import androidx.health.services.client.HealthServicesClient;
import androidx.health.services.client.MeasureCallback;
import androidx.health.services.client.MeasureClient;
import androidx.health.services.client.data.Availability;
import androidx.health.services.client.data.DataPointContainer;
import androidx.health.services.client.data.DataType;
import androidx.health.services.client.data.DeltaDataType;
import androidx.health.services.client.data.SampleDataPoint;

import java.util.ArrayList;

import buddywatch.v1.R;

public class RestingHeartRateListener extends Activity {

    MeasureCallback callback;
    MeasureClient mClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resting);

        // TODO: Finish

        ArrayList<HeartRateRecord> recorder = new ArrayList<>();

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

            }
        }
    };

        mClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback);



}

}
