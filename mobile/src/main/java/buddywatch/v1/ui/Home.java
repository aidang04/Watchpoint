package buddywatch.v1.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import buddywatch.v1.R;
import buddywatch.v1.dao.ActivityDAO;
import buddywatch.v1.dao.GuideDAO;
import buddywatch.v1.database.GuideDatabase;
import buddywatch.v1.model.Guide;
import buddywatch.v1.util.ErrorHandler;
import buddywatch.v1.util.GuideDatabaseConnection;

public class Home extends AppCompatActivity {

    private static final int PADDING = 13;
    private static final int MARGIN_UP_DOWN = 5;
    private static final int MARGIN_LEFT_RIGHT = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls a singleton GuideDatabase instance.
        GuideDatabase db = GuideDatabaseConnection.getInstance(getApplicationContext()).getDb();

        viewHome(db);

    }

    /**
     *
     * Home Page Methods Start Here.
     *
     */

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
            ErrorHandler.handle(e, getApplicationContext(), "Database Error. \n Please contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk.");
        }

        // Passes atomic object into actual object for use.
        List<Guide> guides = atomicGuides.get();

        LinearLayout guideContainer = findViewById(R.id.guides);

        // Loops through all guides and adds them to the UI.
        for(Guide guide : guides) {

            Thread dbCheckCompleted = new Thread(() -> {
                int completed = aDAO.checkIfComplete(guide.filepath);
                runOnUiThread(() -> createDailyBox(guideContainer, guide.filepath, guide.guideName, completed));
            });

            try {
                dbCheckCompleted.start();
                dbCheckCompleted.join();
            }catch (InterruptedException e){
                throw new RuntimeException();
            }

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

    private void createDailyBox(LinearLayout addTo, String filepath, String title, int completed){

        Context context = Home.this;
        DisplayMetrics disp = context.getResources().getDisplayMetrics();

        // Create CardView and set Parameters
        CardView card = new CardView(context);
        LinearLayout.LayoutParams cardP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int margin_up_down = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN_UP_DOWN, disp));
        int margin_left_right = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN_LEFT_RIGHT, disp));
        int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING, disp));

        cardP.setMargins(margin_left_right, margin_up_down, margin_left_right, margin_up_down);
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
        addTo.addView(card);

    }

}