package buddywatch.v1.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.ComponentActivity;

import buddywatch.v1.R;

public class home extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_home);

        BuddyWatchApp app = (BuddyWatchApp) getApplication();

    }

}
