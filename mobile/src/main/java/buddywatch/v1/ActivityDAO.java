package buddywatch.v1;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.sql.Date;
import java.util.List;

@Dao
public interface ActivityDAO {

    @Insert
    void insertActivity(Activity activity);

    @Delete
    void deleteActivity(Activity activity);

    @Query("SELECT * FROM Activity WHERE id = :id")
    Activity getActivityById(int id);

    @Query("SELECT * FROM Activity WHERE dateCompleted = :today")
    List<Activity> getTodaysActivities(Date today);

    @Query("SELECT COUNT(*) FROM Activity WHERE guideID = :gid AND dateCompleted = date('now')")
    int checkIfComplete(int gid);

    @Query("SELECT * FROM Activity WHERE concernIdentified = TRUE AND concernAddressed = FALSE")
    List<Activity> checkForUnaddressedConcerns();

    @Query("UPDATE Activity SET concernAddressed = TRUE WHERE id = :id AND concernAddressed = FALSE")
    void addressConcern(int id);

}
