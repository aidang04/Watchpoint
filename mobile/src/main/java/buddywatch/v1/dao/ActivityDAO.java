package buddywatch.v1.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.sql.Date;

import buddywatch.v1.model.Activity;

@Dao
public interface ActivityDAO {

    @Insert
    void insertActivity(Activity activity);

    @Query("DELETE FROM Activity")
    void deleteAllActivity();

    @Query("SELECT dateCompleted FROM Activity WHERE id = :id")
    Date getDateById(int id);

    @Query("SELECT * FROM Activity WHERE id = (SELECT MAX(id) FROM Activity)")
    Activity getRecentActivity();

    @Query("SELECT COUNT(*) FROM Activity WHERE guidePath = :gPath AND date(dateCompleted / 1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    int checkIfComplete(String gPath);

}
