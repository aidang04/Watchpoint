package buddywatch.v1.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import buddywatch.v1.model.HeartEvent;
import buddywatch.v1.model.HeartEventWithGuideTitle;

@Dao
public interface HeartEventDAO {

    @Insert
    void insertHeartEvent(HeartEvent he);

    @Query("SELECT * FROM HeartEvent ORDER BY id DESC")
    List<HeartEvent> getAllHeartEvent();

    @Query("SELECT HeartEvent.*, Guide.guideName FROM HeartEvent INNER JOIN Guide ON HeartEvent.gpath = Guide.filepath")
    List<HeartEventWithGuideTitle> getAllHeartEventPlusTitle();

    @Query("UPDATE HeartEvent SET addressed = TRUE WHERE id = :id")
    void addressEvent(int id);

    @Query("SELECT * FROM HeartEvent WHERE addressed = FALSE AND severity != 'None'")
    List<HeartEvent> checkUnaddressed();

    @Query("DELETE FROM HeartEvent")
    void deleteAllData();

}
