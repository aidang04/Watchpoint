package buddywatch.v1;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class Guide {

    @PrimaryKey
    @NotNull
    public String filepath;

    public String guideName;
    public Boolean markedFavourite;
    public Boolean markedDaily;

    public Guide(){}

    public Guide(String name, @NonNull String fpath){

        this.guideName = name;
        this.filepath = fpath;
        this.markedFavourite = false;
        this.markedDaily = false;

    }
}
