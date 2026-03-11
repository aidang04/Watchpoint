package buddywatch.v1.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import buddywatch.v1.model.RestingHeart;

@Dao
public interface RestingHeartDAO {

    @Insert
    void insertAll(List<RestingHeart> rhs);

    @Query("SELECT AVG(BPM) FROM RestingHeart")
    float getAverageBPM();

    @Query("DELETE FROM RestingHeart")
    void deleteAllData();

}
