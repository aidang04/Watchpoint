package buddywatch.v1;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


@Database(
        entities = {Guide.class, Activity.class},
        version = 1,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class GuideDatabase extends RoomDatabase {

    public abstract GuideDAO gdao();
    public abstract ActivityDAO adao();

}
