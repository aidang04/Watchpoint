package buddywatch.v1;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
public class ErrorHandler {
    public static void handle(Exception e, Context context, String userMsg){
        Log.e("Error", e.getMessage(), e);
        Toast.makeText(context, userMsg, Toast.LENGTH_SHORT).show();
    }

}
