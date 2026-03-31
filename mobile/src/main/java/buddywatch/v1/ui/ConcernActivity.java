package buddywatch.v1.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Date;
import java.util.concurrent.atomic.AtomicReference;

import buddywatch.v1.R;
import buddywatch.v1.dao.ActivityDAO;
import buddywatch.v1.dao.HeartEventDAO;
import buddywatch.v1.database.GuideDatabase;
import buddywatch.v1.util.ErrorHandler;
import buddywatch.v1.util.GuideDatabaseConnection;

public class ConcernActivity extends AppCompatActivity {

    // responsible for setting up UI
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.concern_view);

        // Uses the intent from HeartDataActivity to obtain important information to display.
        String name = getIntent().getStringExtra("name");
        String level = getIntent().getStringExtra("level");
        int id = getIntent().getIntExtra("id", -1);
        int aid = getIntent().getIntExtra("aid", -1);

        // Checks that id and aid are not their default values, returns if so.
        if(id == -1 || aid == -1){
            ErrorHandler.handle(new RuntimeException(), getApplicationContext(), "Intent Error. \nPlease contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk.");
            finish();
            return;
        }

        // Sets up database interaction to obtain the date that the activity in question was completed.
        GuideDatabase db = GuideDatabaseConnection.getInstance(getApplicationContext()).getDb();
        ActivityDAO adao = db.adao();
        AtomicReference<Date> atomicDate = new AtomicReference<>();

        Thread dbGetActivityDateById = new Thread(() -> atomicDate.set(adao.getDateById(aid)));

        try{
            dbGetActivityDateById.start();
            dbGetActivityDateById.join();
        }
        catch (InterruptedException e){
            ErrorHandler.handle(e, getApplicationContext(), "Database Error. \nPlease contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk.");
            finish();
            return;
        }

        Date concernDate = atomicDate.get();

        // Checks that the date did not return a null value, if it didn't customises the UI to the specific concern.
        if(concernDate == null){
            ErrorHandler.handle(new RuntimeException(), getApplicationContext(), "Database Error. \nPlease contact the maintainer at aidan.gowdy.2022@uni.strath.ac.uk.");
            finish();
            return;
        }
        else {
            customiseUI(name, level, concernDate);
        }

        // Sets up database interaction to set the concern as addressed.
        HeartEventDAO hedao = db.hedao();
        Thread dbAddress = new Thread(() -> hedao.addressEvent(id));
        dbAddress.start();

        // Sets URL Listeners on buttons to link to helpful resources.
        setURLListener(R.id.carerCentre, "https://www.careinfoscotland.scot/topics/support-for-carers/carer-centres/");
        setURLListener(R.id.carerUK, "https://www.carersuk.org/scotland/help-and-advice/practical-support/adult-carer-support-plans/");
        setURLListener(R.id.carersScotland, "https://carers.org/our-work-in-scotland/our-work-in-scotland");

        findViewById(R.id.home).setOnClickListener(v -> finish());

    }

    // customises on screen text
    public void customiseUI(String name, String level, Date date){

        // Formats java.sql.Date class into a String to display of the format dd/mm/yyyy
        String[] dateParts = date.toString().split("-");
        String dateDisplay = dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];

        // Finds TextViews for customising their content.
        TextView concernDuring = findViewById(R.id.concernDuring);
        TextView concernLevel = findViewById(R.id.concernLevel);
        TextView concernDate = findViewById(R.id.concernDate);

        // Customises content.
        concernDuring.setText(getString(R.string.concernDuring, name));
        concernLevel.setText(getString(R.string.concernLevel, level));
        concernDate.setText(getString(R.string.concernDate, dateDisplay));

    }

    // method to set URL listeners for each link
    private void setURLListener(int view, String url){

        findViewById(view).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
            startActivity(intent);
        });

    }
}
