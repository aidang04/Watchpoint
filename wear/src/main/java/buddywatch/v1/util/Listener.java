package buddywatch.v1.util;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

import buddywatch.v1.ui.RestingHeartRateTracker;
import buddywatch.v1.ui.TutorialHandler;

public class Listener extends WearableListenerService {

    private static final String MESSAGE_PATH = "/trigger_action";
    private static final String START_RESTING = "/trigger_resting";

    @Override
    public void onMessageReceived(MessageEvent msgEvent){

        Log.d("Wear", "aa");

        if(MESSAGE_PATH.equals(msgEvent.getPath())){
            String in = new String(msgEvent.getData(), StandardCharsets.UTF_8);
            Log.d("Wear", "Tut received! " + in);

            Intent intent = new Intent(this, TutorialHandler.class);
            intent.putExtra("TUTORIAL_PATH", in);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            startActivity(intent);

        }
        else if(START_RESTING.equals(msgEvent.getPath())){

            Intent intent = new Intent(this, RestingHeartRateTracker.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            startActivity(intent);

        }

    }

}

