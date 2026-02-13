package buddywatch.v1;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity(
        tableName = "Activity",
        foreignKeys = @ForeignKey(
                entity = Guide.class,
                parentColumns = "id",
                childColumns = "guideID",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        ),
        indices = {@Index("guideID")}
)
public class Activity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int guideID;

    public Date dateCompleted;

    // TRUE if the system has detected a concern with the heartrate data collected in a session.
    // FALSE if no concern identified.
    public boolean concernIdentified;

    // FALSE if a concern has been identified in this session, but the system has not alerted the user to it yet or the user closed the prompt without answering it.
    // TRUE if there was a concern identified and it has been addressed.
    // NULL if no concern identified
    public Boolean concernAddressed;

    public Activity(){}

    public Activity(int guideID, Date completed, boolean concernIdentified){

        this.guideID = guideID;
        this.dateCompleted = completed;
        this.concernIdentified = concernIdentified;

        if(concernIdentified == true){
            this.concernAddressed = false;
        }
        else{
            this.concernAddressed = null;
        }

    }

}
