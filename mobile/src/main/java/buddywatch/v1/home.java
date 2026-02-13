package buddywatch.v1;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class home extends AppCompatActivity {

    private static final String MESSAGE_PATH = "/trigger_action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Builds our database connection, for storing guides and user data.
        GuideDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                GuideDatabase.class,
                "guide_database"
        ).build();

        viewHome(db);

    }

    private void viewHome(GuideDatabase db){

        setContentView(R.layout.activity_main);

        // Creates data access object to interact with Guide table.
        GuideDAO gDAO = db.gdao();

        // Prepares atomic reference object.
        AtomicReference<List<Guide>> guides = new AtomicReference<>(new ArrayList<>());

        // Uses atomic object to store all guides.
        Thread dbGetAllGuides = new Thread(() -> guides.set(gDAO.getAllGuides()));

        try{
            dbGetAllGuides.start();
            dbGetAllGuides.join();
        } catch (InterruptedException e) {
            // TODO: Handle Gracefully
            throw new RuntimeException(e);
        }

        // Passes atomic object into actual object for use.
        List<Guide> guidez = guides.get();

        LinearLayout guideContainer = findViewById(R.id.guides);

        // Loops through all guides and adds them to the UI.
        for(int i = 0; i < guidez.size(); i++) {
            Guide guide = guidez.get(i);

            Button button = new Button(this);
            button.setText(guide.guideName);
            button.setId(guide.id);
            button.setOnClickListener(v -> viewGuide(button, db));

            guideContainer.addView(button);

        }

    }

    private void viewGuide(Button info, GuideDatabase db){

        setContentView(R.layout.guideview);
        Button start = findViewById(R.id.send);
        TextView title = findViewById(R.id.title);
        ImageView favourite = findViewById(R.id.favourite);
        ImageView daily = findViewById(R.id.daily);
        ImageView back = findViewById(R.id.back);

        AtomicReference<Guide> results = new AtomicReference<>();

        GuideDAO gDAO = db.gdao();

        Thread dbGetGuide = new Thread(() -> results.set(gDAO.getGuideById(info.getId())));

        try{
            dbGetGuide.start();
            dbGetGuide.join();
        } catch (InterruptedException e) {
            // TODO: Handle Gracefully
            throw new RuntimeException(e);
        }

        Guide current = results.get();

        // Runs UI updates to customise guide screen to selected guide.
        runOnUiThread(() -> {
            title.setText(info.getText());

            if(current.markedDaily){
                toggleDaily(daily);
            }
            if(current.markedFavourite){
                toggleFav(favourite);
            }

        });

        // Sets listener on send button to send filepath for requested guide.
        start.setOnClickListener(v -> {

            AtomicReference<String> filepath = new AtomicReference<>();
            Thread dbGetFilepath = new Thread(() -> filepath.set(gDAO.getGuideFilepathById(info.getId())));

            try{
                dbGetFilepath.start();
                dbGetFilepath.join();
            } catch (InterruptedException e) {
                // TODO: Handle Gracefully
                throw new RuntimeException(e);
            }

            String fp = filepath.get();
            Log.d("Debug", "Sent: " + fp);
            sendCommand(fp);

        });

        // Sets listeners to allow user to toggle options on each guide.
        favourite.setOnClickListener(v -> {
            toggleFav(favourite);
            Thread dbToggleFav = new Thread(() -> gDAO.toggleFav(info.getId()));
            dbToggleFav.start();
        });
        daily.setOnClickListener(v -> {
            toggleDaily(daily);
            Thread dbToggleDaily = new Thread(() -> gDAO.toggleDaily(info.getId()));
            dbToggleDaily.start();
        });

        back.setOnClickListener(v -> viewHome(db));

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