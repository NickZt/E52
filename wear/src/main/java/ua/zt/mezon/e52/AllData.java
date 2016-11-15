package ua.zt.mezon.e52;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by MezM on 03.11.2016.
 */
public class AllData {
    private static AllData ourInstance = new AllData();

    public static AllData getInstance() {
        return ourInstance;
    }

    private AllData() {
    }


    public Boolean readFromPrefs;


//    public static final String  PREFS_data= = "data";
//    public static final String  PREFS_poiITEM_MAP= = "poiITEM_MAP";
//    public static final String  PREFS_data_active= = "data_active";
//    public static final String  PREFS_data_selected= = "data_selected";
//***********************************************************************************************************************
    SharedPreferences.Editor editor;
    Context _context;
    SharedPreferences pref;
    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "e52st";


    public void iniPrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

//    public void setFirstTimeLaunch(boolean isFirstTime) {
//        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
//        editor.commit();
//    }

//    public boolean isFirstTimeLaunch() {
//        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
//    }













}
