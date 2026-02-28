package buddywatch.v1.database;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import buddywatch.v1.model.HeartEvent;
import buddywatch.v1.dao.HeartEventDAO;
import buddywatch.v1.dao.ActivityDAO;
import buddywatch.v1.dao.GuideDAO;
import buddywatch.v1.model.Activity;
import buddywatch.v1.model.Guide;
import buddywatch.v1.util.Converters;


@Database(
        entities = {Guide.class, Activity.class, HeartEvent.class},
        version = 3,
        exportSchema = false
)
@TypeConverters({Converters.class})
@AutoMigration(
        from = 1,
        to = 2
)
public abstract class GuideDatabase extends RoomDatabase {

    public abstract GuideDAO gdao();
    public abstract ActivityDAO adao();
    public abstract HeartEventDAO hedao();

}
