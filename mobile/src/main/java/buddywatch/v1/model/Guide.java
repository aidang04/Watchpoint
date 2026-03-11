package buddywatch.v1.model;

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

    public Guide(String guideName, @NonNull String filepath){

        this.guideName = guideName;
        this.filepath = filepath;
        this.markedFavourite = false;
        this.markedDaily = false;

    }
}
