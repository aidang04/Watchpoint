package buddywatch.v1.ui;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
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
    private static final int PADDING = 50;
    private static final int MARGIN = 5;
    private static final int TEXT_SIZE_TITLE = 15;
    private static final int TEXT_SIZE_BPM = 30;
    private static final int TEXT_SIZE_BPM_EXTRA = 20;

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

        HeartEventDAO hedao = db.hedao();
        findViewById(R.id.deleteData).setOnClickListener(v -> new Thread(hedao::deleteAllData).start());

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

        int margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN, disp));
        int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING, disp));

        cardP.setMargins(margin, margin, margin, margin);
        card.setLayoutParams(cardP);
        card.setRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, disp));

        // Create TextView and set Parameters
        TextView textView = new TextView(context);
        textView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        textView.setText(title);
        textView.setTextSize(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_TITLE, disp)));
        textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, padding, 0, padding);

        // Create TextView for bpm and set parameters.
        TextView bpm = new TextView(context);

        FrameLayout.LayoutParams bpmP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bpmP.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        bpm.setLayoutParams(bpmP);

        String bpmValue = String.valueOf(event.avgBPM);

        // Sets up the BPM string to have different parts of the string be different sizes for a neat display.
        SpannableString spanBPM = new SpannableString(event.avgBPM + "\n avg. bpm");
        spanBPM.setSpan(new AbsoluteSizeSpan(TEXT_SIZE_BPM, true), 0, bpmValue.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanBPM.setSpan(new AbsoluteSizeSpan(TEXT_SIZE_BPM_EXTRA, true), bpmValue.length(), spanBPM.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        bpm.setText(spanBPM);
        bpm.setTextColor(ContextCompat.getColor(context, R.color.grey));
        bpm.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        // Checks if an heartevent has a concern that has not been addressed.
        // If it does, recolours the CardView to an eye-catching red and adds a onClickListener to address the concern in the user's own time.
        if (!event.severity.equals("None") && !event.addressed){
            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));
            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
            bpm.setTextColor(ContextCompat.getColor(context, R.color.white));

            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, ConcernActivity.class);
                intent.putExtra("name", title);
                intent.putExtra("level", event.severity);
                intent.putExtra("id", event.id);
                intent.putExtra("aid", event.aid);
                startActivity(intent);
            });
        }

        card.addView(textView);
        card.addView(bpm);
        addTo.addView(card);

    }
}
