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

    @Query("SELECT HeartEvent.*, Guide.guideName FROM HeartEvent INNER JOIN Guide ON HeartEvent.guidePath = Guide.filepath")
    List<HeartEventWithGuideTitle> getAllHeartEventPlusTitle();

    @Query("UPDATE HeartEvent SET addressed = TRUE WHERE id = :id")
    void addressEvent(int id);

    @Query("SELECT COUNT(*) FROM HeartEvent WHERE addressed = 0 AND severity != 'None'")
    int checkUnaddressed();

    @Query("DELETE FROM HeartEvent")
    void deleteAllData();

}
