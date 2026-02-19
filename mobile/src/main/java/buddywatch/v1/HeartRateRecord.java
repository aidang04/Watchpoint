package buddywatch.v1;

public class HeartRateRecord {
    public long timestamp;
    public double bpm;
    public HeartRateRecord(long t, double b){
        timestamp = t;
        bpm = b;
    }

}
