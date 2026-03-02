package buddywatch.v1.ui;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import buddywatch.v1.R;
import buddywatch.v1.dao.GuideDAO;
import buddywatch.v1.database.GuideDatabase;
import buddywatch.v1.model.Guide;
import buddywatch.v1.util.ErrorHandler;
import buddywatch.v1.util.GuideDatabaseConnection;

public class AllGuidesActivity extends AppCompatActivity {

    private static final int PADDING = 13;
    private static final int MARGIN = 10;
    private static final int CARD_RADIUS = 12;
    private static final int TEXT_SIZE = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_view);
        GuideDatabase db = GuideDatabaseConnection.getInstance(getApplicationContext()).getDb();

        GuideDAO gdao = db.gdao();
        AtomicReference<List<Guide>> atomicGuideList = new AtomicReference<>(new ArrayList<>());

        Thread dbGetAllGuides = new Thread(() -> atomicGuideList.set(gdao.getAllGuides()));

        try{
            dbGetAllGuides.start();
            dbGetAllGuides.join();
        } catch (InterruptedException e) {
            ErrorHandler.handle(e, getApplicationContext(), "Database Error. \nPlease contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk.");
        }

        displayGuides(atomicGuideList.get());

        EditText searchBox = findViewById(R.id.search);

        findViewById(R.id.home).setOnClickListener(v -> finish());
        findViewById(R.id.send).setOnClickListener(v -> displayGuides(searchGuides(gdao, searchBox.getText().toString())));

    }

    private void displayGuides(List<Guide> guides){

        LinearLayout display = findViewById(R.id.guideFrame);
        display.removeAllViews();

        if(guides.isEmpty()){

            TextView noResults = new TextView(getApplicationContext());
            LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textP.gravity = Gravity.CENTER_VERTICAL;
            noResults.setLayoutParams(textP);

            noResults.setText(getString(R.string.noResults));
            noResults.setTextSize(TEXT_SIZE);
            noResults.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey));
            noResults.setTextAlignment(TEXT_ALIGNMENT_CENTER);

            display.addView(noResults);


        } else {
            for (Guide guide : guides) {

                display.addView(createGuideBox(guide));

            }
        }

    }

    private CardView createGuideBox(Guide guide){

        Context context = getApplicationContext();
        DisplayMetrics disp = context.getResources().getDisplayMetrics();

        // Create CardView and set Parameters
        CardView card = new CardView(context);
        LinearLayout.LayoutParams cardP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, MARGIN, disp));
        int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, PADDING, disp));

        cardP.setMargins(0,0,0,margin);
        card.setLayoutParams(cardP);
        card.setRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, CARD_RADIUS, disp));

        // Create TextView and set Parameters
        TextView textView = new TextView(context);
        textView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        textView.setText(guide.guideName);
        textView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE, disp));
        textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        textView.setPadding(0, padding, 0, padding);

        card.addView(textView);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuidePageActivity.class);
            intent.putExtra("filepath", guide.filepath);
            intent.putExtra("name", guide.guideName);
            startActivity(intent);
        });

        return card;

    }

    private List<Guide> searchGuides(GuideDAO gdao, String searchFor){

        AtomicReference<List<Guide>> atomicResults = new AtomicReference<>(new ArrayList<>());
        Thread dbSearchForText = new Thread(() -> atomicResults.set(gdao.searchAll(searchFor)));

        try{
            dbSearchForText.start();
            dbSearchForText.join();
        }catch (InterruptedException e){
            ErrorHandler.handle(e, getApplicationContext(), "Database Error. \n Please contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk");
        }

        return atomicResults.get();

    }

}
