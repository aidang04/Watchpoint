package buddywatch.v1.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import buddywatch.v1.model.Guide;

@Dao
public interface GuideDAO {

    @Insert
    void insertAll(List<Guide> guides);

    @Query("SELECT * FROM Guide WHERE filepath = :fpath")
    Guide getGuideByFilepath(String fpath);

    @Query("SELECT * FROM Guide ORDER BY markedFavourite DESC")
    List<Guide> getAllGuides();

    @Query("UPDATE Guide SET markedFavourite = NOT markedFavourite WHERE filepath = :fpath")
    void toggleFav(String fpath);

    @Query("SELECT * FROM Guide WHERE markedDaily = TRUE")
    List<Guide> getDailys();

    @Query("UPDATE Guide SET markedDaily = NOT markedDaily WHERE filepath = :fpath")
    void toggleDaily(String fpath);

    @Query("SELECT * FROM GUIDE WHERE LOWER(guideName) LIKE LOWER('%' || :searchFor || '%')")
    List<Guide> searchAll(String searchFor);

    @Query("SELECT COUNT(*) FROM Guide")
    int countGuides();

}
