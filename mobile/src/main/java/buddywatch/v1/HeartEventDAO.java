package buddywatch.v1;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HeartEventDAO {

    @Insert
    void insertHeartEvent(HeartEvent he);

    @Query("UPDATE HeartEvent SET addressed = TRUE WHERE id = :id")
    void addressEvent(int id);

    @Query("SELECT * FROM HeartEvent WHERE addressed = FALSE")
    List<HeartEvent> checkUnaddressed();

}
