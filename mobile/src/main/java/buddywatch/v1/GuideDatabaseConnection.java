package buddywatch.v1;

import android.content.Context;

import androidx.room.Room;

// A singleton used to store a connection to the locally stored guide database.
public class GuideDatabaseConnection {

    private static GuideDatabaseConnection instance;
    private final GuideDatabase db;

    private GuideDatabaseConnection(Context context){

        db = Room.databaseBuilder(
                context.getApplicationContext(),
                GuideDatabase.class,
                "guide_database"
        ).fallbackToDestructiveMigration(true).build();

    }

    public static GuideDatabaseConnection getInstance(Context context){

        if (instance == null){
            instance = new GuideDatabaseConnection(context);
        }
        return instance;
    }

    public GuideDatabase getDb(){
        return db;
    }

}
