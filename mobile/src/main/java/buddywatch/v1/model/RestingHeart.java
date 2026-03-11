package buddywatch.v1.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class RestingHeart {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int BPM;

    public RestingHeart(int BPM){
        this.BPM = BPM;
    }

}
