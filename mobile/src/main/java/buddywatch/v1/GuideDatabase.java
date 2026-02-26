package buddywatch.v1;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


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
