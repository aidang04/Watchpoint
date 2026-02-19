package buddywatch.v1;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GuideDAO {

    @Update
    void updateGuide(Guide guide);

    @Insert
    void insertGuide(Guide guide);

    @Insert
    void insertAll(List<Guide> guides);

    @Query("SELECT * FROM Guide WHERE id = :id")
    Guide getGuideById(int id);

    @Query("SELECT filepath FROM Guide WHERE id = :id")
    String getGuideFilepathById(int id);

    @Query("SELECT * FROM Guide ORDER BY id ASC")
    List<Guide> getAllGuides();

    @Query("SELECT * FROM Guide WHERE markedFavourite = TRUE")
    List<Guide> getFavourites();

    @Query("UPDATE Guide SET markedFavourite = NOT markedFavourite WHERE id = :id")
    void toggleFav(int id);

    @Query("SELECT * FROM Guide WHERE markedDaily = TRUE")
    List<Guide> getDailys();

    @Query("UPDATE Guide SET markedDaily = NOT markedDaily WHERE id = :id")
    void toggleDaily(int id);

    @Query("SELECT markedFavourite FROM Guide WHERE id = :id")
    Boolean isFavourite(int id);

    @Query("SELECT markedDaily FROM Guide WHERE id = :id")
    Boolean isDaily(int id);

    @Query("SELECT COUNT(*) FROM Guide")
    int countGuides();

}
