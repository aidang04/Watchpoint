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
        TutViewModel tutView = app.getTutViewModel();

        tutView.getFilePath().observe(this, tutorial -> {
            if (tutorial != null){
                Log.d("WearApp", "New tut!");

                Intent intent = new Intent(this, tutorialHandler.class);
                intent.putExtra("TUTORIAL_PATH", tutorial);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);

            }
        });

    }

}
