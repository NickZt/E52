package ua.zt.mezon.e52;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.patloew.rxwear.RxWear;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import ua.zt.mezon.e52.misc.TimerWorkspace;
import ua.zt.mezon.e52.misc.TimersCategoryInWorkspace;
import ua.zt.mezon.e52.misc.TimersTime;

/**
 * Created by MezM on 03.11.2016.
 */
public class AllData {

    private static AllData ourInstance = new AllData();

    public Typeface Symbol_TYPEFACE;

    //***********************************************************************************************************************
    public CompositeSubscription subscription = new CompositeSubscription();
    public Observable<Boolean> validator;

    public RxWear rxWear;

    //***********************************************************************************************************************
    public Boolean readFromPrefs;
    // Shared preferences file name
    private static final String PREF_NAME = "e52st";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String IS_TIMERCATEGORIES = "IsTimersCategories";

    SharedPreferences.Editor editor;
    Context _context;
    SharedPreferences pref;
    // shared pref mode
    int PRIVATE_MODE = 0;
    private ArrayList<TimerWorkspace> alTimersCategories = new ArrayList<>();
    //***********************************************************************************************************************
    private AllData() {
    }

    public static AllData getInstance() {
        return ourInstance;
    }

    public void iniPrefManager(Context context) {
        this._context = context;
        rxWear= new RxWear(_context);
        this.Symbol_TYPEFACE =Typeface.createFromAsset( context.getAssets(), "fonts/Entypomod.ttf");
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        if (pref.contains(IS_TIMERCATEGORIES)) {
            loadTimersFromPrefs();
        } else {
            iniTimersFirstTime();
            setAlTimersCategories(alTimersCategories);
        }
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    // // TODO: 05.11.2016 del xml ini
//private void iniTimersFromXml(){
//    for (TypedArray item : getMultiTypedArray(_context, "timerworkspace")) {
//        TimerWorkspace category = new TimerWorkspace();
//        category.id = item.getInt(0, 0);
//        category.name = item.getString(1);
//        alTimersCategories.add(category);
//    }
//}
    private void iniTimersFirstTime() {
        TimerWorkspace tmpItem = new TimerWorkspace();
        tmpItem.name = _context.getString(R.string.timerworkspace);
        tmpItem.sTmrWspaceSymbol = Character.toString((char) 116);
        tmpItem.id = 0;
        tmpItem.active=true;
        tmpItem.alTimersCategoryInWorkspace = new ArrayList<>();

        TimersCategoryInWorkspace tmpCItem = new TimersCategoryInWorkspace();
        tmpCItem.name = _context.getString(R.string.categoryinworkspace0);
        tmpCItem.sTmrCategorySymbol = Character.toString((char) 82); // Таймер пес часы
        tmpCItem.id = 0;
        tmpCItem.active=true;
        tmpCItem.timersTimes = new ArrayList<>();
        TimersTime tmp = new TimersTime();
        tmp.id = 0;
        tmp.name = _context.getString(R.string.categoryinworkspace0);
        tmp.maxrepeats = 0;
        tmp.nextDo = 0;//id 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
        tmp.nextid = 0;
        tmp.repeats = 0;
        tmp.time = TimeUnit.SECONDS.toMillis(20) + TimeUnit.MINUTES.toMillis(3);
        tmp.active = true;
        tmpCItem.timersTimes.add(tmp);
        tmpItem.alTimersCategoryInWorkspace.add(tmpCItem);

        tmpCItem = new TimersCategoryInWorkspace();
        tmpCItem.name = _context.getString(R.string.categoryinworkspace1);
        tmpCItem.sTmrCategorySymbol = Character.toString((char) 731); // Таймер помидоро
        tmpCItem.id = 1;
        tmpCItem.active=false;
        tmpCItem.timersTimes = new ArrayList<>();
        tmp = createTimersTime(_context.getString(R.string.timers0categoryinworkspace1),
                0,
                TimeUnit.SECONDS.toMillis(20) + TimeUnit.MINUTES.toMillis(30), //TimeUnit. ##### .toMillis(1)
                0,
                1,
                0, //id 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
                1,
                true);
//         name,
//        id,
//        time, //TimeUnit. ##### .toMillis(1)
//        repeats,
//         maxrepeats,
//         nextDo, //id 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
//         nextid,
//        boolean active
        tmpCItem.timersTimes.add(tmp);
        tmp = createTimersTime(_context.getString(R.string.timers1categoryinworkspace1),
                1,
                TimeUnit.MINUTES.toMillis(6),
                0,
                1,
                0,
                0,
                false);

        tmpCItem.timersTimes.add(tmp);
        tmpItem.alTimersCategoryInWorkspace.add(tmpCItem);

        tmpCItem = new TimersCategoryInWorkspace();
        tmpCItem.name = _context.getString(R.string.categoryinworkspace2);
        tmpCItem.sTmrCategorySymbol = Character.toString((char) 711); // Таймер табата спарта
        tmpCItem.id = 2;
        tmpCItem.active=false;
        tmpCItem.timersTimes = new ArrayList<>();
        tmp = new TimersTime();
        tmp.id = 0;
        tmp.name = _context.getString(R.string.timers0categoryinworkspace2);
        tmp.maxrepeats = 1;
        tmp.nextDo = 0;//id 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
        tmp.nextid = 1;
        tmp.repeats = 0;
        tmp.time = TimeUnit.SECONDS.toMillis(60);
        tmp.active = true;
        tmpCItem.timersTimes.add(tmp);
        tmp = new TimersTime();
        tmp.id = 1;
        tmp.name = _context.getString(R.string.timers1categoryinworkspace2);
        tmp.maxrepeats = 1;
        tmp.nextDo = 0;//id 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
        tmp.nextid = 2;
        tmp.repeats = 0;
        tmp.time = TimeUnit.SECONDS.toMillis(10);
        tmp.active = false;
        ;
        tmpCItem.timersTimes.add(tmp);
        tmp = new TimersTime();
        tmp.id = 2;
        tmp.name = _context.getString(R.string.timers2categoryinworkspace2);
        tmp.maxrepeats = 1;
        tmp.nextDo = 0;//id 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
        tmp.nextid = 0;
        tmp.repeats = 0;
        tmp.time = TimeUnit.SECONDS.toMillis(10);
        tmp.active = false;
        tmpCItem.timersTimes.add(tmp);


        tmpItem.alTimersCategoryInWorkspace.add(tmpCItem);


        alTimersCategories.add(tmpItem);
    }

    @NonNull
    private TimersTime createTimersTime(
            String name,
            int id,
            long time, //TimeUnit. ##### .toMillis(1)
            int repeats,
            int maxrepeats,
            int nextDo, //id 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
            // String nextname,
            int nextid,
            boolean active
    ) {
        TimersTime tmp;
        tmp = new TimersTime();
        tmp.id = id; //0
        tmp.name = name;//_context.getString(R.string.timers0categoryinworkspace1);
        tmp.maxrepeats = maxrepeats;//1;
        tmp.nextDo = nextDo;//0type 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
        tmp.nextid = nextid;//1;
        tmp.repeats = repeats;//0;
        tmp.time = time; //TimeUnit.SECONDS.toMillis(20) + TimeUnit.MINUTES.toMillis(30);
        tmp.active = active;
        return tmp;
    }
public String convertALTimerWorkspace (ArrayList<TimerWorkspace> tmp){
    Type listOfConvertObject = new TypeToken<ArrayList<TimerWorkspace>>(){}.getType();
    Gson gson = new Gson();
    return gson.toJson(tmp,listOfConvertObject);

}
    public ArrayList<TimerWorkspace> convertStringToALTimerWorkspace (String tmp){
        // load timer tasks from preference
        Type listOfTestObject = new TypeToken<ArrayList<TimerWorkspace>>(){}.getType();
        Gson gson = new Gson();
        return ( ArrayList<TimerWorkspace>)  gson.fromJson(tmp, listOfTestObject);

    }




    private void saveTimersToPrefs() {
// save timer tasks to preference

        editor.putString(IS_TIMERCATEGORIES, convertALTimerWorkspace(alTimersCategories));
        editor.commit();


//        try {
//            editor.putString(IS_TIMERCATEGORIES, ObjectSerializer.serialize(alTimersCategories));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        editor.commit();

    }

    private void loadTimersFromPrefs() {
        // load timer tasks from preference

        String json = pref.getString(IS_TIMERCATEGORIES, "");
        alTimersCategories= convertStringToALTimerWorkspace(json) ;

    }


    public ArrayList<TimerWorkspace> getAlTimersCategories() {
        return alTimersCategories;
    }

    //getter & setter
//    // TODO: 04.11.2016 Observer to setter
    public void setAlTimersCategories(ArrayList<TimerWorkspace> alTimersCategories) {
        this.alTimersCategories = alTimersCategories;
        saveTimersToPrefs();

        rxWear.message().sendDataMapToAllRemoteNodes("/dataMap")
                .putString("timers", "TimersCategories")
                .putString("alTimersCategories", convertALTimerWorkspace(alTimersCategories))
                .toObservable()
                .subscribe(requestId -> {
            /* do something */
                });

    }
    public void setAlTimersCategoriesFromWear(ArrayList<TimerWorkspace> alTimersCategories) {
        this.alTimersCategories = alTimersCategories;
        saveTimersToPrefs();

    }
}
