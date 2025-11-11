package buddywatch.v1.presentation;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

public class Listener extends WearableListenerService {

    private static final String MESSAGE_PATH = "/trigger_action";

    @Override
    public void onMessageReceived(MessageEvent msgEvent){

        if(MESSAGE_PATH.equals(msgEvent.getPath())){
            String in = new String(msgEvent.getData(), StandardCharsets.UTF_8);
            Log.d("Wear", "Tut received! " + in);

            Log.d("WearApp", "New tut!");

            Intent intent = new Intent(this, tutorialHandler.class);
            intent.putExtra("TUTORIAL_PATH", in);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);

        }

    }

}

