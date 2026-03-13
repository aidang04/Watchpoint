package buddywatch.v1.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.sql.Date;
import java.util.List;

import buddywatch.v1.model.Activity;

@Dao
public interface ActivityDAO {

    @Insert
    void insertActivity(Activity activity);

    @Delete
    void deleteActivity(Activity activity);

    @Query("SELECT * FROM Activity WHERE id = :id")
    Activity getActivityById(int id);

    @Query("SELECT dateCompleted FROM Activity WHERE id = :id")
    Date getDateById(int id);

    @Query("SELECT * FROM Activity WHERE dateCompleted = :today")
    List<Activity> getTodaysActivities(Date today);

    @Query("SELECT * FROM Activity WHERE id = (SELECT MAX(id) FROM Activity)")
    Activity getRecentActivity();

    @Query("SELECT COUNT(*) FROM Activity WHERE guidePath = :gPath AND date(dateCompleted / 1000, 'unixepoch') = date('now')")
    int checkIfComplete(String gPath);

}
