package buddywatch.v1.model;

import androidx.room.Embedded;

public class HeartEventWithGuideTitle {
    @Embedded
    public HeartEvent heartEvent;

    public String guideName;
}
