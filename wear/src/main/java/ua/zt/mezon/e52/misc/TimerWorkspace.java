package ua.zt.mezon.e52.misc;

import java.util.ArrayList;

/**
 * Created by MezM on 04.11.2016.
 */
public class TimerWorkspace {

    public ArrayList <TimersCategoryInWorkspace> alTimersCategoryInWorkspace;
    public String name;
    public  String sTmrWspaceSymbol;
    public int id;
    public boolean active;

    public int getIdXbyId_alTimersCategoryInWorkspace (ArrayList <TimersCategoryInWorkspace> alTmp, int id) {

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
