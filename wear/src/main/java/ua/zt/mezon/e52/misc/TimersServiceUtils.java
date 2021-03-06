package ua.zt.mezon.e52.misc;

import java.util.ArrayList;

/**
 * Created by MezM on 30.11.2016.
 */

public class TimersServiceUtils {
    public static int getIdXbyId_alTimersCategories(ArrayList<TimerWorkspace> alTmpTimersCategories, int id) {

        for (int i = 0; i <alTmpTimersCategories.size() ; i++) {
            if (alTmpTimersCategories.get(i).id==id) {
                return i;
            }
        }

        return -1;
    }
    public static int getIdXbyId_timersTimes(ArrayList<TimersTime> alTmp, int id) {

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

    public boolean isValidId_alTimersCategories (ArrayList<TimerWorkspace> alTmpTimersCategories, int id) {

        for (TimerWorkspace z: alTmpTimersCategories
                ) {
            if (z.id==id) {
                return false;
            }
        }

        return true;
    }
    public static int getIdXbyId_alTimersCategoryInWorkspace(ArrayList<TimersCategoryInWorkspace> alTmp, int id) {

        for (int i = 0; i <alTmp.size() ; i++) {
            if (alTmp.get(i).id==id) {
                return i;
            }
        }

        return -1;
    }
    public boolean isValidId_alTimersCategoryInWorkspace (ArrayList <TimersCategoryInWorkspace> alTmp, int id) {

        for (TimersCategoryInWorkspace z: alTmp
                ) {
            if (z.id==id) {
                return false;
            }
        }

        return true;
    }
}
