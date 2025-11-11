package buddywatch.v1.presentation;

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

            buddyWatchApp app = (buddyWatchApp) getApplication();
            TutViewModel tutView = app.getTutViewModel();
            tutView.setFilePath(in);

        }

    }

}

