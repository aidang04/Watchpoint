package buddywatch.v1.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import buddywatch.v1.model.Guide;

@Dao
public interface GuideDAO {

    @Update
    void updateGuide(Guide guide);

    @Insert
    void insertGuide(Guide guide);

    @Insert
    void insertAll(List<Guide> guides);

    @Query("SELECT * FROM Guide WHERE filepath = :fpath")
    Guide getGuideByFilepath(String fpath);

    @Query("SELECT * FROM Guide")
    List<Guide> getAllGuides();

    @Query("SELECT * FROM Guide WHERE markedFavourite = TRUE")
    List<Guide> getFavourites();

    @Query("UPDATE Guide SET markedFavourite = NOT markedFavourite WHERE filepath = :fpath")
    void toggleFav(String fpath);

    @Query("SELECT * FROM Guide WHERE markedDaily = TRUE")
    List<Guide> getDailys();

    @Query("UPDATE Guide SET markedDaily = NOT markedDaily WHERE filepath = :fpath")
    void toggleDaily(String fpath);

    @Query("SELECT markedFavourite FROM Guide WHERE filepath = :fpath")
    Boolean isFavourite(String fpath);

    @Query("SELECT markedDaily FROM Guide WHERE filepath = :fpath")
    Boolean isDaily(String fpath);

    @Query("SELECT COUNT(*) FROM Guide")
    int countGuides();

}
