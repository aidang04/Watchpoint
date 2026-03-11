package buddywatch.v1.model;

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
    public int id;

    // guide id where the event occurred.
    public String gPath;

    // activity id belonging to the session which caused the event
    public int aid;

    public int avgBPM;
    public String severity;
    public boolean rapid;
    public boolean addressed;

    public HeartEvent(String gPath, int aid, int avgBPM, String severity, boolean rapid){

        this.gPath = gPath;
        this.aid = aid;
        this.avgBPM = avgBPM;
        this.severity = severity;
        this.rapid = rapid;
        this.addressed = severity.equals("None");

    }

}
