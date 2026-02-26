package buddywatch.v1;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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
        ).fallbackToDestructiveMigration(true).build();

        viewHome(db);

    }

    private void viewHome(GuideDatabase db){

        setContentView(R.layout.activity_main);

        // Creates data access objects to interact with Guide table.
        GuideDAO gDAO = db.gdao();
        ActivityDAO aDAO = db.adao();

        Thread dbCheckDatabase = new Thread(() -> {
            if(gDAO.countGuides() == 0){
                fillDatabase(gDAO);
            }
        });

        // Prepares atomic reference object.
        AtomicReference<List<Guide>> atomicGuides = new AtomicReference<>(new ArrayList<>());

        // Uses atomic object to store all guides.
        Thread dbGetAllGuides = new Thread(() -> atomicGuides.set(gDAO.getAllGuides()));

        try{
            // Checks if database is empty, if so, fills with guides.
            dbCheckDatabase.start();
            dbCheckDatabase.join();

            // Returns a list of all guides.
            dbGetAllGuides.start();
            dbGetAllGuides.join();
        } catch (InterruptedException e) {
            // TODO: Handle Gracefully
            throw new RuntimeException(e);
        }

        // Passes atomic object into actual object for use.
        List<Guide> guides = atomicGuides.get();

        LinearLayout guideContainer = findViewById(R.id.guides);

        // Loops through all guides and adds them to the UI.
        for(int i = 0; i < guides.size(); i++) {
            Guide guide = guides.get(i);

            Thread dbCheckCompleted = new Thread(() -> createDailyBox(db, guideContainer, guide.filepath, guide.guideName, aDAO.checkIfComplete(guide.filepath)));

            try {
                dbCheckCompleted.start();
                dbCheckCompleted.join();
            }catch (InterruptedException e){
                throw new RuntimeException();
            }

        }

        CardView heartCard = findViewById(R.id.heartRate);
        heartCard.setOnClickListener(v -> heartData(db));

    }

    private void viewGuide(String filepath, String name, GuideDatabase db){

        // Sets view to a template for the guide launch view.
        setContentView(R.layout.guideview);

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
            // TODO: Handle Gracefully
            throw new RuntimeException(e);
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

    private void fillDatabase(GuideDAO gDAO){

        List<Guide> guides = new ArrayList<>();
        guides.add(new Guide("Test 1","tut1.txt"));
        guides.add(new Guide("Test 2","tut2.txt"));
        guides.add(new Guide("Test 3","tut3.txt"));
        guides.add(new Guide("Test 4","tut4.txt"));

        Thread dbPopulate = new Thread(() -> gDAO.insertAll(guides));

        try{
            dbPopulate.start();
            dbPopulate.join();
        }
        catch (InterruptedException e){
            // TODO : Handle Gracefully
            throw new RuntimeException();
        }

    }

    private void createDailyBox(GuideDatabase db, LinearLayout addTo, String filepath, String title, int completed){

        Context context = addTo.getContext();
        DisplayMetrics disp = context.getResources().getDisplayMetrics();

        // Create CardView and set Parameters
        CardView card = new CardView(context);
        LinearLayout.LayoutParams cardP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, disp));
        int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13, disp));

        cardP.setMargins(0,0,0,margin);
        card.setLayoutParams(cardP);
        card.setRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, disp));

        // Create TextView and set Parameters
        TextView textView = new TextView(context);
        textView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        textView.setText(title);
        textView.setTextAlignment(ViewGroup.TEXT_ALIGNMENT_CENTER);
        textView.setPadding(0, padding, 0, padding);

        // Create CheckBox and set Parameters
        CheckBox check = new CheckBox(context);
        FrameLayout.LayoutParams checkP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        checkP.gravity = Gravity.END;
        check.setLayoutParams(checkP);
        check.setClickable(false);
        check.setChecked(completed > 0);

        // Assemble TextView and CheckBox into CardView
        card.addView(textView);
        card.addView(check);

        // Sets a listener on the card which when clicked displays the launch page for the selected guide.
        card.setOnClickListener(v -> viewGuide(filepath, title, db));

        // Add CardView to parent view
        addTo.addView(card);

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

    private void heartData(GuideDatabase db){

        setContentView(R.layout.heart_view);

        ImageView home = findViewById(R.id.home);
        home.setOnClickListener(v -> viewHome(db));



    }

    private void createHeartBox(LinearLayout addTo, String title, int avgBPM, boolean concern){

        Context context = addTo.getContext();
        DisplayMetrics disp = context.getResources().getDisplayMetrics();

        // Create CardView and set Parameters
        CardView card = new CardView(context);
        LinearLayout.LayoutParams cardP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, disp));
        int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13, disp));

        cardP.setMargins(0,0,0,margin);
        card.setLayoutParams(cardP);
        card.setRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, disp));

        // Create TextView and set Parameters
        TextView textView = new TextView(context);
        textView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        textView.setText(title);
        textView.setTextAlignment(ViewGroup.TEXT_ALIGNMENT_CENTER);
        textView.setPadding(0, padding, 0, padding);

        TextView bpm = new TextView(context);
        bpm.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        String bpmToDisplay = avgBPM + " bpm";
        bpm.setText(bpmToDisplay);
        bpm.setTextAlignment(ViewGroup.TEXT_ALIGNMENT_VIEW_END);

        if (concern){
            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));
            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
            bpm.setTextColor(ContextCompat.getColor(context, R.color.white));
        }

    }

}