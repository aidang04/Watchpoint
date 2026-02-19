package buddywatch.v1;

import androidx.room.TypeConverter;

import java.sql.Date;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long val){
        return  val == null ? null : new Date(val);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date){
        return date == null ? null : date.getTime();
    }

}
