package buddywatch.v1;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Guide {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String guideName;
    public String filepath;
    public Boolean markedFavourite;
    public Boolean markedDaily;

    public Guide(){}

    public Guide(String name, String fpath){

        this.guideName = name;
        this.filepath = fpath;
        this.markedFavourite = false;
        this.markedDaily = false;

    }
}
