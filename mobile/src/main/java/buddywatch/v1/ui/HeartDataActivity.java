package buddywatch.v1.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import buddywatch.v1.R;
import buddywatch.v1.dao.HeartEventDAO;
import buddywatch.v1.database.GuideDatabase;
import buddywatch.v1.model.HeartEvent;
import buddywatch.v1.model.HeartEventWithGuideTitle;
import buddywatch.v1.util.ErrorHandler;
import buddywatch.v1.util.GuideDatabaseConnection;

public class HeartDataActivity extends AppCompatActivity {

    private static final int MAX_HEART_EVENT_TO_DISPLAY = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heart_view);
        GuideDatabase db = GuideDatabaseConnection.getInstance(getApplicationContext()).getDb();

        ImageView home = findViewById(R.id.home);
        home.setOnClickListener(v -> finish());

        List<HeartEventWithGuideTitle> heartEvents = fetchHeartEvents(db);
        LinearLayout heartContainer = findViewById(R.id.dataContainer);

        int limit = Math.min(MAX_HEART_EVENT_TO_DISPLAY, heartEvents.size());
        for(int i = 0; i < limit; i++){
            HeartEventWithGuideTitle event = heartEvents.get(i);
            createHeartBox(heartContainer, event.guideName, event.heartEvent);
        }

    }

    private List<HeartEventWithGuideTitle> fetchHeartEvents(GuideDatabase db){

        HeartEventDAO hedao = db.hedao();
        AtomicReference<List<HeartEventWithGuideTitle>> atomicHeartEventList = new AtomicReference<>(new ArrayList<>());

        Thread dbGetHeartEvents = new Thread(() -> atomicHeartEventList.set(hedao.getAllHeartEventPlusTitle()));

        try{
            dbGetHeartEvents.start();
            dbGetHeartEvents.join();
        } catch (InterruptedException e) {
            ErrorHandler.handle(e, getApplicationContext(), "Database Error. \n Please contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk.");
        }

        return atomicHeartEventList.get();

    }

    private void createHeartBox(LinearLayout addTo, String title, HeartEvent event){

        Context context = HeartDataActivity.this;
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
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, padding, 0, padding);

        TextView bpm = new TextView(context);
        bpm.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        String bpmToDisplay = event.avgBPM + " bpm";
        bpm.setText(bpmToDisplay);
        bpm.setGravity(Gravity.END);

        if (!event.severity.equals("None") && !event.addressed){
            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));
            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
            bpm.setTextColor(ContextCompat.getColor(context, R.color.white));

            card.setOnClickListener(v -> Log.d("debug", "bleh"));
        }

        card.addView(textView);
        card.addView(bpm);

        addTo.addView(card);

    }
}
