package ua.zt.mezon.e52.misc;

import java.util.ArrayList;

/**
 * Created by MezM on 05.11.2016.
 */
public class TimersCategoryInWorkspace {
    public String name;
    public int id;
    public String sTmrCategorySymbol; // Character.toString((char) 731); // Таймер помидоро
    public ArrayList<TimersTime> timersTimes;
    public int idDescription=0; //
    public boolean active;


    public int getIdXbyId_timersTimes ( ArrayList<TimersTime> alTmp, int id) {

        for (int i = 0; i <alTmp.size() ; i++) {
            if (alTmp.get(i).id==id) {
                return i;
            }
        }

        return -1;
    }
    public boolean isValidId_timersTimes ( ArrayList<TimersTime> alTmp, int id) {

        for (TimersTime z: alTmp
                ) {
            if (z.id==id) {
                return false;
            }
        }

        return true;
    }
}
