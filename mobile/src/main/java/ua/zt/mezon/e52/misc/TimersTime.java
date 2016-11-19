package ua.zt.mezon.e52.misc;

/**
 * Created by MezM on 05.11.2016.
 */
public class TimersTime {
    public String name;
    public int id;
    public long time; //TimeUnit. ##### .toMillis(1)
    public int repeats;
    public int maxrepeats;
    public int nextDo; //type 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
    // String nextname;
    public int nextid;
    public boolean active;
    public int typeTimerBehavior=0; // 0-normal 1 vibrate every second 2silent)
    public int idDescription=0; //
}
