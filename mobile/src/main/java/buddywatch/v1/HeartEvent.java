package buddywatch.v1;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "HeartEvent",
        foreignKeys = {
                @ForeignKey(
                entity = Guide.class,
                parentColumns = "filepath",
                childColumns = "gPath",
                onUpdate = ForeignKey.CASCADE,
                onDelete = ForeignKey.CASCADE),
        @ForeignKey(
                entity = Activity.class,
                parentColumns = "id",
                childColumns = "aid",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        )},
        indices = {@Index("gPath"), @Index("aid")}
)
public class HeartEvent {

    @PrimaryKey(autoGenerate = true)
    int id;

    // guide id where the event occured.
    String gPath;

    // activity id belonging to the session which caused the event
    int aid;

    int avgBPM;
    String severity;
    boolean rapid;
    boolean addressed;

    public HeartEvent(){}

    public HeartEvent(String gPath, int aid, int avgBPM, String severity, boolean rapid){

        this.gPath = gPath;
        this.aid = aid;
        this.avgBPM = avgBPM;
        this.severity = severity;
        this.rapid = rapid;
        this.addressed = severity.equals("None");

    }

}
