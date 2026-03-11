package buddywatch.v1.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

import buddywatch.v1.R;
import buddywatch.v1.dao.GuideDAO;
import buddywatch.v1.database.GuideDatabase;
import buddywatch.v1.model.Guide;
import buddywatch.v1.util.ErrorHandler;
import buddywatch.v1.util.GuideDatabaseConnection;

public class Home extends AppCompatActivity {

    private static final int PADDING = 13;
    private static final int MARGIN_UP_DOWN = 5;
    private static final int MARGIN_LEFT_RIGHT = 10;
    private static final String START_RESTING = "/trigger_resting";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Calls a singleton GuideDatabase instance.
        GuideDatabase db = GuideDatabaseConnection.getInstance(getApplicationContext()).getDb();
        viewHome(db);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GuideDatabase db = GuideDatabaseConnection.getInstance(getApplicationContext()).getDb();
        fillDailyTasks(db);
    }

    private void viewHome(GuideDatabase db){

        // Creates a data access object to interact with Guide table.
        GuideDAO gDAO = db.gdao();

        Thread dbCheckDatabase = new Thread(() -> {
            if(gDAO.countGuides() == 0){
                fillDatabase(gDAO);
            }
        });

        try{
            // Checks if database is empty, if so, fills with guides.
            dbCheckDatabase.start();
            dbCheckDatabase.join();
        } catch (InterruptedException e) {
            ErrorHandler.handle(e, getApplicationContext(), "Database Error. \n Please contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk.");
        }

        CardView heartCard = findViewById(R.id.heartRate);
        heartCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, HeartDataActivity.class);
            startActivity(intent);
        });

        CardView guideCard = findViewById(R.id.allGuides);
        guideCard.setOnClickListener(v -> {
            Intent intent =  new Intent(this, AllGuidesActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.settings).setOnClickListener(v -> callSettings());
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
            ErrorHandler.handle(e, getApplicationContext(), "Database Error. \n Please contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk.");
        }
    }

    private void fillDailyTasks(GuideDatabase db) {

        new Thread(() -> {
            List<Guide> guides = db.gdao().getDailys();

            runOnUiThread(() -> updateUI(db, guides));

        }).start();

    }

    private void updateUI(GuideDatabase db, List<Guide> guides){

        LinearLayout guideContainer = findViewById(R.id.guides);
        guideContainer.removeAllViews();

        // Loops through all guides and adds them to the UI.
        for(Guide guide : guides) {

            Thread dbCheckCompleted = new Thread(() -> {
                int completed = db.adao().checkIfComplete(guide.filepath);
                runOnUiThread(() -> guideContainer.addView(createDailyBox(guide.filepath, guide.guideName, completed)));
            });

            try {
                dbCheckCompleted.start();
                dbCheckCompleted.join();
            }catch (InterruptedException e){
                ErrorHandler.handle(e, getApplicationContext(), "Database Error. \nPlease contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk.");
            }

        }

    }

    private CardView createDailyBox(String filepath, String title, int completed){

        Context context = Home.this;
        DisplayMetrics disp = context.getResources().getDisplayMetrics();

        // Create CardView and set Parameters
        CardView card = new CardView(context);
        LinearLayout.LayoutParams cardP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int margin_up_down = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN_UP_DOWN, disp));
        int marginLeftRight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN_LEFT_RIGHT, disp));
        int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING, disp));

        cardP.setMargins(marginLeftRight, margin_up_down, marginLeftRight, margin_up_down);
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
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuidePageActivity.class);
            intent.putExtra("filepath", filepath);
            intent.putExtra("name", title);
            startActivity(intent);
        });

        // Add CardView to parent view
        return card;

    }

    private void callSettings(){

        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setView(R.layout.settings_sheet);

        AlertDialog dialog = build.create();
        dialog.show();

        findViewById(R.id.start).setOnClickListener(v -> startRestingSession());
        findViewById(R.id.deleteGuideData).setOnClickListener(v -> deleteData("Guide"));
        findViewById(R.id.deleteResting).setOnClickListener(v -> deleteData("Resting"));

    }

    private void startRestingSession(){

        NodeClient nodeClient = Wearable.getNodeClient(this);
        nodeClient.getConnectedNodes().addOnSuccessListener(nodes -> {
            for(Node n : nodes){
                Wearable.getMessageClient(this).sendMessage(n.getId(), START_RESTING, null);
            }
        });

    }

    private void deleteData(String toDelete){

        GuideDatabase db = GuideDatabaseConnection.getInstance(this).getDb();

        if(toDelete.equals("Resting")){

            Thread dbDeleteData = new Thread(() -> db.rhdao().deleteAllData());
            dbDeleteData.start();

        }
        else if(toDelete.equals("Guide")){

            Thread dbDeleteData = new Thread(() -> db.hedao().deleteAllData());
            dbDeleteData.start();

        }

        Toast.makeText(this, "All " + toDelete + " data successfully deleted.", Toast.LENGTH_SHORT).show();

    }

}