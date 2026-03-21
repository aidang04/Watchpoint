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
                childColumns = "guidePath",
                onUpdate = ForeignKey.CASCADE,
                onDelete = ForeignKey.CASCADE),
        @ForeignKey(
                entity = Activity.class,
                parentColumns = "id",
                childColumns = "activityId",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        )},
        indices = {@Index("guidePath"), @Index("activityId")}
)
public class HeartEvent {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // guide id where the event occurred.
    public String guidePath;

    // activity id belonging to the session which caused the event
    public int activityId;

    public int avgBPM;
    public String severity;
    public boolean addressed;

    public HeartEvent(String guidePath, int activityId, int avgBPM, String severity){

        this.guidePath = guidePath;
        this.activityId = activityId;
        this.avgBPM = avgBPM;
        this.severity = severity;
        this.addressed = severity.equals("None");

    }

}
