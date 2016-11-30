package ua.zt.mezon.e52.misc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
    public static boolean isValidId_alTimersCategoryInWorkspace(ArrayList<TimersCategoryInWorkspace> alTmp, int id) {

        for (TimersCategoryInWorkspace z: alTmp
                ) {
            if (z.id==id) {
                return false;
            }
        }

        return true;
    }
    public static String convertTimersCategoryInWorkspace(TimersCategoryInWorkspace tmp) {
        Type listOfConvertObject = new TypeToken<TimersCategoryInWorkspace>(){}.getType();
        Gson gson = new Gson();
        return gson.toJson(tmp,listOfConvertObject);
    }
}
