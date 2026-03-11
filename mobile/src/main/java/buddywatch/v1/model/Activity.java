package buddywatch.v1.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.sql.Date;
import java.time.LocalDate;

@Entity(
        tableName = "Activity",
        foreignKeys = @ForeignKey(
                entity = Guide.class,
                parentColumns = "filepath",
                childColumns = "guidePath",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        ),
        indices = {@Index("guidePath")}
)
public class Activity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String guidePath;

    public Date dateCompleted;

    public Activity(String guidePath, Date dateCompleted){

        this.guidePath = guidePath;
        this.dateCompleted = dateCompleted;

    }

}
