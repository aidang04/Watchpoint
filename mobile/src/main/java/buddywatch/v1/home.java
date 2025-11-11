package buddywatch.v1;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;

public class home extends AppCompatActivity {

    private static final String MESSAGE_PATH = "/trigger_action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button send = findViewById(R.id.send);
        send.setOnClickListener(v -> {
            Log.d("PhoneApp", "Button clicked!");
            sendCommand("tut1.txt");
        });
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