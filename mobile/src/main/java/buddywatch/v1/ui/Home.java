package buddywatch.v1.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
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
    AlertDialog dialog;


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

        Thread dbCheckUnaddressed = new Thread(() -> {
            if(db.hedao().checkUnaddressed() > 0){
                heartRateNotify();
            }
        });

        dbCheckUnaddressed.start();
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

    private void heartRateNotify(){

        CardView heartCard = findViewById(R.id.heartRate);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(heartCard.getRadius());
        drawable.setColor(Color.WHITE);
        heartCard.setBackground(drawable);

        ObjectAnimator notify = ObjectAnimator.ofArgb(drawable, "color", Color.WHITE, Color.RED);
        notify.setDuration(1000);
        notify.setRepeatCount(ObjectAnimator.INFINITE);
        notify.setRepeatMode(ObjectAnimator.REVERSE);
        runOnUiThread(notify::start);

    }

    private void fillDatabase(GuideDAO gDAO){

        List<Guide> guides = new ArrayList<>();
        guides.add(new Guide("Create Origami Fox","fox.txt"));
        guides.add(new Guide("Create Origami Egg","egg.txt"));
        guides.add(new Guide("Create Origami Swan","swan.txt"));
        guides.add(new Guide("Administering Pills","administeringPills.txt"));

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
        View dialogView = getLayoutInflater().inflate(R.layout.settings_sheet, null);

        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setView(dialogView);

        Button begin = dialogView.findViewById(R.id.begin);
        begin.setVisibility(View.VISIBLE);
        begin.setOnClickListener(v -> startRestingSession());

        Button deleteGuideData = dialogView.findViewById(R.id.deleteGuideData);
        deleteGuideData.setOnClickListener(v -> deleteData("Guide"));
        deleteGuideData.setVisibility(View.VISIBLE);

        Button deleteResting = dialogView.findViewById(R.id.deleteResting);
        deleteResting.setOnClickListener(v -> deleteData("Resting"));
        deleteResting.setVisibility(View.VISIBLE);

        Button refreshGuideList = dialogView.findViewById(R.id.refreshGuideList);
        refreshGuideList.setOnClickListener(v -> fillDailyTasks(GuideDatabaseConnection.getInstance(this).getDb()));
        refreshGuideList.setVisibility(View.VISIBLE);

        dialog = build.create();
        dialog.show();

    }

    private void startRestingSession(){

        NodeClient nodeClient = Wearable.getNodeClient(this);
        nodeClient.getConnectedNodes().addOnSuccessListener(nodes -> {
            for(Node n : nodes){
                Wearable.getMessageClient(this).sendMessage(n.getId(), START_RESTING, null).addOnSuccessListener(aVoid ->
                        Log.d("Home", "Sent Request."));
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

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}