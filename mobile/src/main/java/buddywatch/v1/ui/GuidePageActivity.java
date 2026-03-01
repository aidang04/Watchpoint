package buddywatch.v1.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import buddywatch.v1.R;
import buddywatch.v1.dao.GuideDAO;
import buddywatch.v1.database.GuideDatabase;
import buddywatch.v1.model.Guide;
import buddywatch.v1.util.ErrorHandler;
import buddywatch.v1.util.GuideDatabaseConnection;

public class GuidePageActivity extends AppCompatActivity {

    private static final String MESSAGE_PATH = "/trigger_action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guideview);
        GuideDatabase db = GuideDatabaseConnection.getInstance(getApplicationContext()).getDb();

        String filepath = getIntent().getStringExtra("filepath");
        String name = getIntent().getStringExtra("name");

        // Obtains references for elements of the view.
        Button start = findViewById(R.id.send);
        TextView title = findViewById(R.id.title);
        ImageView favourite = findViewById(R.id.favourite);
        ImageView daily = findViewById(R.id.daily);
        ImageView back = findViewById(R.id.back);

        AtomicReference<Guide> results = new AtomicReference<>();

        GuideDAO gDAO = db.gdao();

        Thread dbGetGuide = new Thread(() -> results.set(gDAO.getGuideByFilepath(filepath)));

        try{
            dbGetGuide.start();
            dbGetGuide.join();
        } catch (InterruptedException e) {
            ErrorHandler.handle(e, getApplicationContext(), "Database Error. \n Please contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk.");
        }

        Guide current = results.get();

        // Runs UI updates to customise guide screen to selected guide.
        runOnUiThread(() -> {
            title.setText(name);

            if(current.markedDaily){
                toggleDaily(daily);
            }
            if(current.markedFavourite){
                toggleFav(favourite);
            }

        });

        // Sets listener on send button to send filepath for requested guide.
        start.setOnClickListener(v -> sendCommand(filepath));

        // Sets listeners to allow user to toggle options on each guide.
        favourite.setOnClickListener(v -> {
            toggleFav(favourite);
            Thread dbToggleFav = new Thread(() -> gDAO.toggleFav(filepath));
            dbToggleFav.start();
        });
        daily.setOnClickListener(v -> {
            toggleDaily(daily);
            Thread dbToggleDaily = new Thread(() -> gDAO.toggleDaily(filepath));
            dbToggleDaily.start();
        });

        back.setOnClickListener(v -> finish());

    }

    private void toggleFav(ImageView toUpdate){

        if(toUpdate.getTag() == "true"){
            toUpdate.setTag("false");
            toUpdate.setImageResource(R.drawable.unheart);
        }
        else {
            toUpdate.setTag("true");
            toUpdate.setImageResource(R.drawable.heart);

        }

    }

    private void toggleDaily(ImageView toUpdate) {

        if(toUpdate.getTag() == "true"){
            toUpdate.setTag("false");
            toUpdate.setImageResource(R.drawable.unsave);
        }
        else {
            toUpdate.setTag("true");
            toUpdate.setImageResource(R.drawable.save);
        }
    }

    private void sendCommand(String tut) {

        NodeClient nodeClient = Wearable.getNodeClient(this);
        nodeClient.getConnectedNodes().addOnSuccessListener(nodes -> {
            for (Node n : nodes) {
                byte[] toSend = tut.getBytes(StandardCharsets.UTF_8);
                Wearable.getMessageClient(this).sendMessage(n.getId(), MESSAGE_PATH, toSend).addOnSuccessListener(aVoid ->
                        Log.d("PhoneApp", "Sent message: " + tut)).addOnFailureListener(e ->
                        Log.e("PhoneApp", "Failed to send: " + tut));
            }
        });
    }

}
