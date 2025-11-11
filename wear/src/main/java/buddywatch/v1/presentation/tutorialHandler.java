package buddywatch.v1.presentation;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import buddywatch.v1.R;

public class tutorialHandler extends Activity {

    int curLine;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_tutorial);
        textView = findViewById(R.id.textView);

        String tutorial = getIntent().getStringExtra("TUTORIAL_PATH");
        startTut(tutorial);
    }

    public void startTut(String filename){

        String[] lines;
        try{
            lines = readFile(filename);
            curLine = 0;

            if(lines.length > 0){
                String out = "Step 1. " + lines[curLine];
                textView.setText(out);
            }

            findViewById(R.id.root).setOnClickListener(v -> {
                curLine++;
                if (curLine >= lines.length){
                    String out = "The Tutorial is over.";
                    try {
                        wait(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    finish();
                }
                String out = "Step " + (curLine + 1) + ". " + lines[curLine];
                textView.setText(out);
            });

        }
        catch(IOException e){
            textView.setText("Error reading file.");
        }
    }


    // Breaks down the file into displayable lines for startTut.
    public String[] readFile(String filename) throws IOException {

        InputStream is = getAssets().open(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String ln;

        while ((ln = reader.readLine()) != null){
            sb.append(ln).append("\n");
        }
        reader.close();
        is.close();

        return sb.toString().split("\n");

    }

}
