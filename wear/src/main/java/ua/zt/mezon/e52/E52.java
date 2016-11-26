/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ua.zt.mezon.e52;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.AlarmClock;
import android.support.v4.app.NotificationCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.wearable.MessageApi;
import com.patloew.rxwear.RxWear;
import com.patloew.rxwear.transformers.MessageEventGetDataMap;

import java.lang.ref.WeakReference;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import rx.subscriptions.CompositeSubscription;
import ua.zt.mezon.e52.core.MySpcIntentService;
import ua.zt.mezon.e52.core.ServiceConnector;
import ua.zt.mezon.e52.misc.TimerWorkspace;
import ua.zt.mezon.e52.misc.TimersCategoryInWorkspace;
import ua.zt.mezon.e52.misc.TimersTime;
import ua.zt.mezon.e52.spclayout.TimerTypeSelectActivity;

import static android.app.PendingIntent.getService;
import static java.util.concurrent.TimeUnit.MILLISECONDS;


/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class E52 extends CanvasWatchFaceService {
    private AlarmManager mSpcStateAlarmManager;
    private PendingIntent mSpcStateAlarmPendingIntent,mSpcStateChimePendingIntent, mSpcStateButtTimerPendingIntent, mSpcStateTimerPendingIntent;
    // Left and right dial supported types.
    private CompositeSubscription subscription = new CompositeSubscription();

    private RxWear rxWear;

    private static final String TAG = "E52WatchFace";
    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);
    private static final long INTERACTIVE_UPDATE_RATE_MS_IN_STOPWACH_MODE = MILLISECONDS.toMillis(10);
    private static final int BACKGROUND_COLOR = Color.BLACK;
    private static final int TEXT_HOURS_MINS_COLOR = Color.BLACK; //DKGRAY
    private static final int TEXT_SECONDS_COLOR = Color.BLACK;
    private static final int TEXT_AM_PM_COLOR = Color.BLACK;
    private static final int TEXT_COLON_COLOR = Color.BLACK;
    private static final int TEXT_STEP_COUNT_COLOR = Color.WHITE;
    private static final int STATUS_SYMB_COLOR = Color.BLACK;
    private static final String STRING_COLON = ":";
    private static final String STRING_CLOCK_BATT =Character.toString((char) 94); // батарея часы
    private static final String STRING_PHONE_BATT =Character.toString((char) 59); // батарея телефон
    private static final String STRING_FIT_PEDESTRIAN =Character.toString((char) 40); // Fit пешеход шаги
    private static final String STRING_FIT_PEDESTRIAN_REACH_TARGET =Character.toString((char) 711); // Fit пешеход шаги дошагал
    private  String string_TIMER = Character.toString((char) 82); // Таймер пес часы
    private static final String STRING_POMODORO_TIMER = Character.toString((char) 731); // Таймер помидоро
    private static final String STRING_TABATA_TIMER = Character.toString((char) 711); // Таймер табата спарта
    private static final String STRING_BUTT_TIMER =Character.toString((char) 225); // // сидячий таймер Character.toString((char) 337)
    private static final String STRING_CHIME_TIMER =Character.toString((char) 61);// Кукушка
    private static final String STRING_STOPWATCH_STOP =Character.toString((char) 63);// Секундомер стоит
    private static final String STRING_STOPWATCH_RUN =Character.toString((char) 246);// Секундомер бежит
    private static final String STRING_ALARM_CLOCK =Character.toString((char) 83); // будильник
    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;
    private Typeface NORMAL_TYPEFACE1;
    private Typeface NORMAL_TYPEFACE2, NORMAL_TYPEFACE3/*, NORMAL_TYPEFACE4, NORMAL_TYPEFACE5*/;
    private int iInFacesCount = 1;



    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<E52.Engine> mWeakReference;

        public EngineHandler(E52.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            E52.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            ResultCallback<DailyTotalResult>,
            ServiceConnector.ServiceListener<MySpcIntentService>

    {

        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint;
        boolean mAmbient;
        Calendar mCalendar;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        final BroadcastReceiver mSpcStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive in mSpcStateReceiver");
                Log.d(TAG, "action = " + intent.getAction());
                Log.d(TAG, "extra = " + intent.getStringExtra("extra"));
                Log.d(TAG, "mSpcStateReceiver onHandleIntent called with intent: " + intent);
                switch (intent.getAction())
                {
                    case "Chimeaction":{

                    }
                    break;
                    case "Timeraction":{
                        Log.d(TAG, "action = " + intent.getAction());
                        //  sendVibroMessage(5);
                        //  sendClockMessage("String s1","String  s2");
                    }
                    break;
                    case "ButtTimeraction":{

                    }
                    break;

                }
            }
        };
//        final SpcStateReceiver  = new abstract SpcStateReceiver() ;

        long lCurrPosmenuEndTimeMls = 0;
        long lStopwatchTimeMls = 0;
        long lStopwatchInbetweenTimeMls = 0;
        long lAlarmTimeMls = 0;
        int[] ialTimersCategoriesActiveLvls = new int[3];

        long lTimerSetTimeMls = TimeUnit.SECONDS.toMillis(20) + TimeUnit.MINUTES.toMillis(3);
        long lCurrentTimerEndTimeMls = 0;
        long lCurrentTimerInbetweenEndTimeMls = 0;
        long lButtTimerSetTimeMls = TimeUnit.SECONDS.toMillis(20) + TimeUnit.MINUTES.toMillis(30);
        long lButtCurrentTimerEndTimeMls = 0;
        long lButtCurrentTimerInbetweenEndTimeMls = 0;
        int iChimeCurrHours;
        boolean bStopwatchTimeStartStop = false;
        boolean bAlarmTimeStartStop = false;
        boolean bTimerTimeStartStop = false;
        boolean bChime = false;
        int mMultiplic = 0;
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;
        float mXOffset;
        float mXOffsetMinute, mXOffsetColonHM, mXOffsetCalendarPaintOffset, mYOffsetStatusSymb, mXFitOffsetStatusSymb, mXFitOffsetBattLev;
        float mYOffset;
        float mXCalendDayOffset;
        float mYCalendDayOffset;
        float mXCalendarPaintOffset;
        float mYCalendarPaintOffset;
        private boolean bButtTimerStartStop = false;
        private int mWidthX; //max size
        private int mHeightY;
        private int mHCoefButt;
        private float mCenterX;
        private float mCenterY;
        private float mScale = 1;
        private Paint mHourPaint;
        private Paint mMinutePaint;
        private Paint mSecondPaint;
        private Paint mColonPaint;
        private Paint mStepCountPaint;
        private Paint mCalendarPaint;
        private Paint mCalendarDatePaint;
        private Paint mStatusSymbPaint;
        private Paint mClockButtonsBoundingRect;
        private Paint mBattLevPaint;
        private Paint mFitStatusSymbPaint;
        private float mColonWidth;
        private String mAmString;
        private String mPmString;
        private String tmpString;
        private String battLevString;
        ServiceConnection sConn;
        private MySpcIntentService mySpcIntentService;
        Boolean servONduty = false;

        final BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                battLevString = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));// + "%"
                //BatteryManager.EXTRA_LEVEL
            }
        };
        private long tmpMillis;
        private float mLineHeight;
        private Bitmap mBackgroundBitmap;
        /*
                 * Google API Client used to make Google Fit requests for step data.
                 */
        private GoogleApiClient mGoogleApiClient;
        private boolean mStepsRequested;
        private int mStepsTotal = 0;
        private int mStepsPlannedDaily = 0;
        private float mXStepsOffset;
        private float mYFitOffset;
        private AllData allData = AllData.getInstance();
        private ArrayList<TimerWorkspace> alTimersCategories;

        //




        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            MySpcIntentService.bindService(MySpcIntentService.class, getApplicationContext(), Engine.this);
            mStepsRequested = false;
            mStepsRequested = false;
            mCalendar = Calendar.getInstance();
            mCalendar.setFirstDayOfWeek(Calendar.MONDAY);
//            timerssssssss

//            alarmManager = (AlarmManager) E52.this.getSystemService(Context.ALARM_SERVICE);
            mSpcStateAlarmManager = (AlarmManager) E52.this.getSystemService(Context.ALARM_SERVICE);
//                    (AlarmManager)  getSystemService(Context.ALARM_SERVICE);


            Intent spcStateChimeIntent =
                    new Intent(getApplicationContext(), MySpcIntentService.class);//getApplicationContext(), MyIntentService.class
            spcStateChimeIntent.setAction("Chimeaction");
            spcStateChimeIntent.putExtra("extra","E52Chimeaction");
            mSpcStateChimePendingIntent = PendingIntent.getService(
                    getApplicationContext(),
                    0,
                    spcStateChimeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);


            Intent spcStateAlarmIntent =
                    new Intent(getApplicationContext(), MySpcIntentService.class);//getApplicationContext(), MyIntentService.class
            spcStateAlarmIntent.setAction("Alarmaction");
            spcStateAlarmIntent.putExtra("extra","E52Alarmaction");
            mSpcStateAlarmPendingIntent = PendingIntent.getService(
                    getApplicationContext(),
                    1,
                    spcStateAlarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);


            Intent spcStateTimerIntent =
                    new Intent(getApplicationContext(), MySpcIntentService.class);//MyIntentServiceMySpcIntentService
            spcStateTimerIntent.setAction("Timeraction");
            spcStateTimerIntent.putExtra("extra","E52StateTimer");
            mSpcStateTimerPendingIntent = PendingIntent.getService(
                    getApplicationContext(),
                    2,
                    spcStateTimerIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Intent spcStateButtTimerIntent =
                    new Intent(getApplicationContext(),MySpcIntentService.class);
            spcStateButtTimerIntent.setAction("ButtTimeraction");


            mSpcStateButtTimerPendingIntent = PendingIntent.getService(
                    getApplicationContext(),
                    3,
                    spcStateButtTimerIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);



// end timerss ini
            allData.iniPrefManager(getApplicationContext());
            if (!allData.isFirstTimeLaunch()) {
                alTimersCategories = allData.getAlTimersCategories();

                setUpInternalTimers();

            }
            rxWear = new RxWear(getApplicationContext());
//            // TODO: 15.11.2016 Rx Wear
//            //start rx part
            subscription.add(rxWear.message().listen("/message", MessageApi.FILTER_PREFIX)
                    .compose(MessageEventGetDataMap.noFilter())
                    .subscribe(dataMap -> {
                        if (dataMap.containsKey("alarm")) {
                            lAlarmTimeMls = dataMap.getLong("alarm");
                            Toast.makeText(getApplicationContext(), "Alarm at "+TimeInMilisToStr(lAlarmTimeMls), Toast.LENGTH_SHORT).show();
                        }


                    }, throwable -> Toast.makeText(getApplicationContext(), "Error on message listen", Toast.LENGTH_SHORT).show()));

//            rxWear.message().listen("/dataMap", MessageApi.FILTER_LITERAL)
//                    .compose(MessageEventGetDataMap.noFilter())
//                    .subscribe(dataMap -> {
//                        // String title = dataMap.getString("title", getString(R.string.no_message));
//                        if  (dataMap.containsKey("WearHandleIntent")) {
//                            String tmp = dataMap.getString("WearHandleIntent");
//                            Toast.makeText(getApplicationContext(), "WearHandleIntent", Toast.LENGTH_SHORT).show();
//                            switch (tmp)
//                            {
//                                case "Chimeaction":{
//
//                                }
//                                break;
//                                case "Timeraction":{
//                                    Log.d(TAG, " Timeraction Rx action = " + tmp);
//                                    sendVibroMessage(5);
//                                    sendClockMessage("String s1","String  s2");
//                                }
//                                break;
//                                case "ButtTimeraction":{
//
//                                }
//                                break;
//
//                            }
//
//
//                        }
//                    });








            rxWear.message().listen("/dataMap", MessageApi.FILTER_LITERAL)
                    .compose(MessageEventGetDataMap.noFilter())
                    .subscribe(dataMap -> {
                        // String title = dataMap.getString("title", getString(R.string.no_message));
                        if (dataMap.containsKey("alTimersCategories")) {
                            String json = dataMap.getString("alTimersCategories");
                            alTimersCategories = allData.convertStringToALTimerWorkspace(json);
                            allData.setAlTimersCategories(alTimersCategories);
                            allData.setFirstTimeLaunch(false);
                            Toast.makeText(getApplicationContext(), "Timers arrived", Toast.LENGTH_SHORT).show();
                            setUpInternalTimers();
//                           long lTimerSetTimeMls = TimeUnit.SECONDS.toMillis(20) + TimeUnit.MINUTES.toMillis(3);
//                           long lCurrentTimerEndTimeMls = 0;
//                           long lCurrentTimerInbetweenEndTimeMls = 0;


                        }

            /* do timersss */

                    });
//end rx part
            mGoogleApiClient = new GoogleApiClient.Builder(E52.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.RECORDING_API)
                    // When user has multiple accounts, useDefaultAccount() allows Google Fit to
                    // associated with the main account for steps. It also replaces the need for
                    // a scope request.
                    .useDefaultAccount()
                    .build();
            setWatchFaceStyle(new WatchFaceStyle.Builder(E52.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());
            Resources resources = E52.this.getResources();

            //        String nextAlarm = Settings.System.getString(getContentResolver(), alarmManager.getNextAlarmClock().toString());

            mYOffset = resources.getDimension(R.dimen.digital_y_offset);
            NORMAL_TYPEFACE1 = Typeface.createFromAsset(resources.getAssets(), "fonts/Digitalo.ttf");  //a_RombyGr Crystal DIGIFACE Digitalo LiqCrystMd
            NORMAL_TYPEFACE2 = Typeface.createFromAsset(resources.getAssets(), "fonts/Entypomod.ttf");
            NORMAL_TYPEFACE3 = Typeface.createFromAsset(resources.getAssets(), "fonts/Roboto-Black.ttf"); //PRFV   LCDNOV_9  LCDNOV_3 Serpentine.ttf
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.background));


            // mBackgroundPaint.setColor(Color.BLACK);

            final int backgroundResId = R.drawable.e52wk; //fullfunclect e52wk

            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), backgroundResId);


            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), backgroundResId);


            mYFitOffset = resources.getDimension(R.dimen.fit_y_offset);

            mLineHeight = resources.getDimension(R.dimen.fit_line_height);
            mXCalendDayOffset = resources.getDimension(R.dimen.digital_x_CalendDay_offset);
            mYCalendDayOffset = resources.getDimension(R.dimen.digital_y_CalendDay_offset);
            mXCalendarPaintOffset = resources.getDimension(R.dimen.digital_x_CalendarPaint_offset);
            mYCalendarPaintOffset = resources.getDimension(R.dimen.digital_y_CalendarPaint_offset);


            mAmString = resources.getString(R.string.digital_am);
            mPmString = resources.getString(R.string.digital_pm);

            mHourPaint = createTextPaint(TEXT_HOURS_MINS_COLOR, 1);
            mMinutePaint = createTextPaint(TEXT_HOURS_MINS_COLOR, 1);
            mSecondPaint = createTextPaint(TEXT_SECONDS_COLOR, 1);//TEXT_SECONDS_COLOR

            mCalendarPaint = createTextPaint(TEXT_SECONDS_COLOR, 3); // 5 3
            mCalendarDatePaint = createTextPaint(TEXT_SECONDS_COLOR, 1);

            mColonPaint = createTextPaint(TEXT_COLON_COLOR, 1);


            mStatusSymbPaint = createTextPaint(STATUS_SYMB_COLOR, 2); //4

            mStepCountPaint = createTextPaint(TEXT_STEP_COUNT_COLOR, 1);
            mBattLevPaint = createTextPaint(TEXT_STEP_COUNT_COLOR, 1);
            mFitStatusSymbPaint = createTextPaint(TEXT_STEP_COUNT_COLOR, 2);

            mClockButtonsBoundingRect = new Paint();
            mClockButtonsBoundingRect.setColor(Color.RED);


        }

        public void setUpInternalTimers() {
            if (bTimerTimeStartStop) {
                bTimerTimeStartStop=false;
                deleteTimer();
            }
            for (TimerWorkspace tmp :
                    alTimersCategories) {
                if (tmp.active) {
                    ialTimersCategoriesActiveLvls[0] = allData.getIdXbyId_alTimersCategories(alTimersCategories,tmp.id);
                    if (ialTimersCategoriesActiveLvls[0]==-1) ialTimersCategoriesActiveLvls[0]=0;
                    for (TimersCategoryInWorkspace tmp1 :
                            tmp.alTimersCategoryInWorkspace) {
                        if (tmp1.active) {
                            ialTimersCategoriesActiveLvls[1] = tmp.getIdXbyId_alTimersCategoryInWorkspace(tmp.alTimersCategoryInWorkspace,  tmp1.id);
                            if (ialTimersCategoriesActiveLvls[1]==-1) ialTimersCategoriesActiveLvls[1]=0;
                            string_TIMER = tmp1.sTmrCategorySymbol;
                            for (TimersTime tmp2 :
                                    tmp1.timersTimes) {
                                if (tmp2.active) {

                                    ialTimersCategoriesActiveLvls[2] =tmp1.getIdXbyId_timersTimes( tmp1.timersTimes,tmp2.id);
                                    if (ialTimersCategoriesActiveLvls[2]==-1) ialTimersCategoriesActiveLvls[2]=0;
                                    lTimerSetTimeMls = tmp2.time;
                                }
                            }

                        }
                    }
                }
            }
//
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor, int textFont) {
//            // TODO: 21.09.2016
            Paint paint = new Paint();
            paint.setColor(textColor);
            switch (textFont) {
                case 1:
                    paint.setTypeface(NORMAL_TYPEFACE1);
                    break;
                case 2:
                    paint.setTypeface(NORMAL_TYPEFACE2);
                    break;
                case 3:
                    paint.setTypeface(NORMAL_TYPEFACE3);
                    break;
            }

            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onVisibilityChanged: " + visible);
            }
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();

                registerReceiver();

                // Update time zone and date formats, in case they changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            E52.this.registerReceiver(mTimeZoneReceiver, filter);
            E52.this.registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
           // E52.this.registerReceiver(mSpcStateReceiver, new IntentFilter("E52"));
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            E52.this.unregisterReceiver(mTimeZoneReceiver);
            E52.this.unregisterReceiver(mBatteryInfoReceiver);
        }


        public void onApplyWindowInsets(WindowInsets insets) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onApplyWindowInsets: " + (insets.isRound() ? "round" : "square"));
            }
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = E52.this.getResources();
            boolean isRound = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
                isRound = insets.isRound();
            }
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            mXStepsOffset = resources.getDimension(isRound
                    ? R.dimen.fit_steps_or_distance_x_offset_round : R.dimen.fit_steps_or_distance_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
            float textSecondSize = resources.getDimension(R.dimen.digital_Secondtext_size);
            float textCalendarPaintSize = resources.getDimension(R.dimen.digital_CalendarPaint_size);
            float statusSymbSize = resources.getDimension(R.dimen.digital_StatusSymb_size);
            float amPmSize = resources.getDimension(isRound
                    ? R.dimen.fit_am_pm_size_round : R.dimen.fit_am_pm_size);

            mHourPaint.setTextSize(textSize);
            mMinutePaint.setTextSize(textSize);
            mColonPaint.setTextSize(textSize);

            mSecondPaint.setTextSize(textSecondSize);//textSecondSize

            mCalendarPaint.setTextSize(textCalendarPaintSize);
            mCalendarDatePaint.setTextSize(textSecondSize);
            mBattLevPaint.setTextSize(textSecondSize);
            mStatusSymbPaint.setTextSize(statusSymbSize);
            mFitStatusSymbPaint.setTextSize(statusSymbSize);

            mStepCountPaint.setTextSize(resources.getDimension(R.dimen.fit_steps_or_distance_text_size));

            //   mColonWidth = mColonPaint.measureText(STRING_COLON);
            mColonWidth = mColonPaint.measureText("8");
            mXOffsetColonHM = mHourPaint.measureText("16") + mXOffset;
            mXOffsetMinute = mXOffsetColonHM + mColonWidth / 2;
            mXOffsetCalendarPaintOffset = mXOffsetMinute + mMinutePaint.measureText("88");

            mYOffsetStatusSymb = mYCalendDayOffset - mCalendarDatePaint.measureText("8") / 2;
            mXFitOffsetStatusSymb = mFitStatusSymbPaint.measureText(STRING_CLOCK_BATT); // батарея часы
            mXFitOffsetBattLev = mBattLevPaint.measureText("188");
        }


        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mHourPaint.setAntiAlias(!inAmbientMode);
                    mMinutePaint.setAntiAlias(!inAmbientMode);
                    mSecondPaint.setAntiAlias(!inAmbientMode);
                    mFitStatusSymbPaint.setAntiAlias(!inAmbientMode);
                    mColonPaint.setAntiAlias(!inAmbientMode);
                    mStepCountPaint.setAntiAlias(!inAmbientMode);
                    mStatusSymbPaint.setAntiAlias(!inAmbientMode);

                    mCalendarPaint.setAntiAlias(!inAmbientMode);
                    mCalendarDatePaint.setAntiAlias(!inAmbientMode);
                    mBattLevPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /*
      * Determines if tap inside a buttons area (0,1,2,3) or return 99.
      */
        private int getTappedButtonId(int x, int y) {


            for (int i = 0; i < 4; i++) {

                Rect rClockButtonsBoundingRect = new Rect(0, 0, 0, 0);
                //  mHCoefButt= (int) (mHeightY / 3f);
                switch (i) {
                    case 0:
                        //Left bottom Button Режим
                        rClockButtonsBoundingRect.set(
                                0,                                          // left
                                mHeightY - mHCoefButt,                       // top
                                mHCoefButt,                               // right
                                mHeightY);                            // bottom

                        break;

                    case 1:
                        //Left Upper Button Свет
                        rClockButtonsBoundingRect.set(
                                0,                              // left
                                0,                              // top
                                mHCoefButt,                     // right
                                mHCoefButt);                    // bottom
                        //  canvas.draw
                        break;
                    case 2:
                        //right Upper Button ВКЛ
                        rClockButtonsBoundingRect.set(
                                mWidthX - mHCoefButt,                              // left
                                0,                              // top
                                mWidthX,                     // right
                                mHCoefButt);                    // bottom
                        //  canvas.draw
                        break;
                    case 3:
                        //right bottom Button ВЫБОР
                        rClockButtonsBoundingRect.set(
                                mWidthX - mHCoefButt,                              // left
                                mHeightY - mHCoefButt,                              // top
                                mWidthX,                     // right
                                mHeightY);                    // bottom
                        //  canvas.draw
                        break;


                }

                if (rClockButtonsBoundingRect.width() > 0) {
                    if (rClockButtonsBoundingRect.contains(x, y)) {
                        return i;
                    }
                } else {
                    Log.e(TAG, "Not a recognized button id.");
                }
            }

            return 99;
        }


        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                    //   String ss = Integer.toString(iInFacesCount) + " э x " + Integer.toString(x) + " y " + Integer.toString(y);
//                    Toast.makeText(getApplicationContext(), ss, Toast.LENGTH_SHORT)
//                            .show();
                    int backgroundResId;

                    switch (getTappedButtonId(x, y)) {


                        case 1:
                            //Left Upper Button Свет
                            if (mHourPaint.getColor() == TEXT_HOURS_MINS_COLOR) {
                                mHourPaint.setColor(TEXT_STEP_COUNT_COLOR);
                                mMinutePaint.setColor(TEXT_STEP_COUNT_COLOR);
                                mSecondPaint.setColor(TEXT_STEP_COUNT_COLOR);//TEXT_SECONDS_COLOR

                                mCalendarPaint.setColor(TEXT_STEP_COUNT_COLOR); // 5 3
                                mCalendarDatePaint.setColor(TEXT_STEP_COUNT_COLOR);

                                mColonPaint.setColor(TEXT_STEP_COUNT_COLOR);
                                mStatusSymbPaint.setColor(TEXT_STEP_COUNT_COLOR);

                            } else {
                                mHourPaint.setColor(TEXT_HOURS_MINS_COLOR);
                                mMinutePaint.setColor(TEXT_HOURS_MINS_COLOR);
                                mSecondPaint.setColor(TEXT_SECONDS_COLOR);//TEXT_SECONDS_COLOR

                                mCalendarPaint.setColor(TEXT_SECONDS_COLOR); // 5 3
                                mCalendarDatePaint.setColor(TEXT_SECONDS_COLOR);

                                mColonPaint.setColor(TEXT_COLON_COLOR);
                                mStatusSymbPaint.setColor(STATUS_SYMB_COLOR);
                            }
//                            switch (iInFacesCount) {
//                                case 1:
//                                    break;
//
//                            }


                            break;
                        case 2:
                            //right Upper Button ВКЛ
                            switch (iInFacesCount) {
                                case 1: //основной режим
                                    if (bChime) {
                                        bChime = false;
                                        deleteChime();
                                    } else {
                                        bChime = true;
                                        iChimeCurrHours = (int) TimeUnit.MILLISECONDS.toHours(mCalendar.getTimeInMillis()) % 24;
                                        refreshChimeAndSetNextUpdate();
                                    }
                                    break;
                                case 2: // режим будильник
                                    if (bAlarmTimeStartStop) {
                                        bAlarmTimeStartStop = false;
                                       // bAl2Req();
                                        deleteAlarm();
                                    } else {
                                        bAlarmTimeStartStop = true;
                                      //  alWorkDaysSetter((int) (MILLISECONDS.toHours(lAlarmTimeMls) % 24), (int) (MILLISECONDS.toMinutes(lAlarmTimeMls) % 60));
                                        refreshsetAlarmAndSetNextUpdate((int) (MILLISECONDS.toHours(lAlarmTimeMls) % 24), (int) (MILLISECONDS.toMinutes(lAlarmTimeMls) % 60));
                                    }
                                    break;


                                case 3:
//                                    секундомер
                                    if (bStopwatchTimeStartStop) {
                                        // lStopwatchInbetweenTimeMls
                                        bStopwatchTimeStartStop = false;
                                        lStopwatchInbetweenTimeMls = mCalendar.getTimeInMillis() - lStopwatchTimeMls;

                                    } else {
                                        bStopwatchTimeStartStop = true;
                                        if (lStopwatchTimeMls != 0) {
                                            lStopwatchTimeMls = mCalendar.getTimeInMillis() - lStopwatchInbetweenTimeMls;
                                            lStopwatchInbetweenTimeMls = 0;
                                        } else {
//                                            Запускаем секундомер
                                            lStopwatchTimeMls = mCalendar.getTimeInMillis();
                                        }
                                    }
                                    break;

                                case 4:
//                                    таймер
                                    if (bTimerTimeStartStop) {
                                        // lStopwatchInbetweenTimeMls
                                        bTimerTimeStartStop = false;
                                        lCurrentTimerInbetweenEndTimeMls = lCurrentTimerEndTimeMls - mCalendar.getTimeInMillis();
                                        deleteTimer();
// // TODO: 08.10.2016 stopTimerd
//                                        bTimer2Req();
                                    } else {
                                        //                                            Запускаем таймер из промежуточного положения
                                        bTimerTimeStartStop = true;
                                        if (lCurrentTimerEndTimeMls != 0) {
                                            lCurrentTimerEndTimeMls = mCalendar.getTimeInMillis() + lCurrentTimerInbetweenEndTimeMls;
                                            //  startTimer(getString(R.string.timer_set_str), (int) MILLISECONDS.toSeconds(lCurrentTimerInbetweenEndTimeMls));
                                            refreshTimerAndSetNextUpdate(lCurrentTimerInbetweenEndTimeMls);
                                            lCurrentTimerInbetweenEndTimeMls = 0;

                                        } else {
//                                            Запускаем таймер
//                                            startTimer(getString(R.string.timer_set_str), (int) MILLISECONDS.toSeconds(lTimerSetTimeMls));
                                            lCurrentTimerEndTimeMls = mCalendar.getTimeInMillis() + lTimerSetTimeMls;
                                            refreshTimerAndSetNextUpdate(lTimerSetTimeMls);
                                        }


                                    }
                                    break;

                            }
                            break;


                        case 3:
                            //right bottom Button ВЫБОР
                            //  canvas.draw
                            switch (iInFacesCount) {
                                case 1: // режим основной
                                    // в фит на 3 сек
                                    iInFacesCount = 5;
                                    lCurrPosmenuEndTimeMls = mCalendar.getTimeInMillis() + TimeUnit.SECONDS.toMillis(3);

                                    break;
                                case 2: // режим будильник
                                    if (bAlarmTimeStartStop) {
                                        bAlarmTimeStartStop = false;
                                       // bAl2Req();
                                        deleteAlarm();
                                    } else {
                                        bAlarmTimeStartStop = true;
                                      //  setAlarm((int) (MILLISECONDS.toHours(lAlarmTimeMls) % 24), (int) (MILLISECONDS.toMinutes(lAlarmTimeMls) % 60));
                                        refreshsetAlarmAndSetNextUpdate((int) (MILLISECONDS.toHours(lAlarmTimeMls) % 24), (int) (MILLISECONDS.toMinutes(lAlarmTimeMls) % 60));
                                    }


                                    break;
                                case 3:
//                                    секундомер
                                    if (bStopwatchTimeStartStop) {


                                    } else {

                                        if (lStopwatchTimeMls != 0) {
//                                            Обнуляем секундомер
                                            lStopwatchTimeMls = 0;
                                            lStopwatchInbetweenTimeMls = 0;
                                        }


                                    }
                                    break;
                                case 4:
//                                    таймер
                                    if (bTimerTimeStartStop) {

                                    } else {
                                        if (lCurrentTimerEndTimeMls == 0) {
                                            Intent intent = new Intent(getApplicationContext(), TimerTypeSelectActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                        if (lCurrentTimerEndTimeMls != 0) {
//                                            Обнуляем таймер
                                            lCurrentTimerEndTimeMls = 0;
                                            lCurrentTimerInbetweenEndTimeMls = 0;
                                        }


                                    }
                                    break;
                            }

                            //end right bottom Button ВЫБОР
                            break;
                        case 0:
                            //Left bottom Button Режим совп с 99

                            //break;

                        case 99:
                            //Left bottom Button Режим совп с 99
                            if (iInFacesCount == 4) {
                                iInFacesCount = 99;
                            } else if (iInFacesCount >= 4) { //f
                                iInFacesCount = 1;

                            } else {
                                iInFacesCount++;
                            }

                            switch (iInFacesCount) {
                                // // TODO: 28.09.2016 Background change & life time
                                case 1:
                                    // mTextPaint.setTypeface(NORMAL_TYPEFACE);
                                    lCurrPosmenuEndTimeMls = 0;
                                    break;

                                case 2:
                                    // // TODO: 28.09.2016 Background change
//                                    backgroundResId = R.drawable.fullfunclect; //fullfunclect e52wk felect
//
//                                    mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), backgroundResId);
//
//                                    mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
//                                            (int) (mBackgroundBitmap.getWidth() * mScale),
//                                            (int) (mBackgroundBitmap.getHeight() * mScale), true);
//                                    // end background change
                                    lCurrPosmenuEndTimeMls = mCalendar.getTimeInMillis() + TimeUnit.SECONDS.toMillis(15);


                                    break;
                                case 3:
//                                    backgroundResId = R.drawable.felect; //fullfunclect e52wk felect
//
//                                    mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), backgroundResId);
//
//                                    mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
//                                            (int) (mBackgroundBitmap.getWidth() * mScale),
//                                            (int) (mBackgroundBitmap.getHeight() * mScale), true);
//                                    // end background change
                                    lCurrPosmenuEndTimeMls = mCalendar.getTimeInMillis() + TimeUnit.SECONDS.toMillis(15);

                                    break;
                                case 4:
//                                    backgroundResId = R.drawable.e52wk; //fullfunclect e52wk felect
//
//                                    mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), backgroundResId);
//
//                                    mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
//                                            (int) (mBackgroundBitmap.getWidth() * mScale),
//                                            (int) (mBackgroundBitmap.getHeight() * mScale), true);
//                                    // end background change
                                    lCurrPosmenuEndTimeMls = mCalendar.getTimeInMillis() + TimeUnit.SECONDS.toMillis(15);

                                    break;
                                case 5:
                                    // mTextPaint.setTypeface(NORMAL_TYPEFACE5);
                                    // end background change
                                    lCurrPosmenuEndTimeMls = mCalendar.getTimeInMillis() + TimeUnit.SECONDS.toMillis(5);


                                    break;
                                case 99: //f

                                    lCurrPosmenuEndTimeMls = 0;
                                    break;
                                default:
                                    if (iInFacesCount >= 4) {
                                        iInFacesCount = 1;
                                    }
                                    break;

                            }

                            break;
                    }


            }
            invalidate();
        }

        //// TODO: 05.10.2016 alarm create
        void setRefreshServiceAlarm(Context context, Class<? extends IntentService> cls) {
            int alarmType = AlarmManager.RTC_WAKEUP;
            int refreshInterval = (int) TimeUnit.MINUTES.toMillis(60); // milliseconds

            Intent intent = new Intent(context, cls);
            PendingIntent refreshIntent = getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(alarmType, System.currentTimeMillis(), refreshInterval, refreshIntent);
        }

        private void setAlarm(int hours, int minutes) {
            // Set one Alarm
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, hours);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.alarm_set_done));

            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }

        private void alWorkDaysSetter(int iHOUR, int iMINUTES) {
            ArrayList<Integer> days = new ArrayList<Integer>();
            days.add(Calendar.TUESDAY);
            days.add(Calendar.MONDAY);
            days.add(Calendar.WEDNESDAY);
            days.add(Calendar.THURSDAY);
            days.add(Calendar.FRIDAY);
            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
            i.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.alarm_set_done));
            i.putExtra(AlarmClock.EXTRA_HOUR, iHOUR);
            i.putExtra(AlarmClock.EXTRA_MINUTES, iMINUTES);
            i.putExtra(AlarmClock.EXTRA_DAYS, days);
            i.putExtra(AlarmClock.EXTRA_SKIP_UI, false);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

        private void bAl2Req() {

            Intent i = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
            i.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.alarm_set_done));
            i.putExtra(AlarmClock.ALARM_SEARCH_MODE_LABEL, getString(R.string.alarm_set_done));
            i.putExtra(AlarmClock.EXTRA_SKIP_UI, false); //true
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivity(i);
            } else {
                Toast.makeText(getApplicationContext(), "Не могу удалить будильник", Toast.LENGTH_SHORT);
            }
        }

        private void bTimer2Req() {

            Intent i = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
            i.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.timer_set_str));
            i.putExtra(AlarmClock.ALARM_SEARCH_MODE_LABEL, getString(R.string.timer_set_str));
            i.putExtra(AlarmClock.EXTRA_SKIP_UI, false); //true
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivity(i);
            } else {
                Toast.makeText(getApplicationContext(), "Не могу удалить таймер", Toast.LENGTH_SHORT);
            }
        }

        private boolean bAlReq() {

            Intent i = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
            i.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.alarm_set_done));
            i.putExtra(AlarmClock.ALARM_SEARCH_MODE_LABEL, getString(R.string.alarm_set_done));
            i.putExtra(AlarmClock.EXTRA_SKIP_UI, false);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivity(i);
            }

            return true;
        }

        public void startTimer(String message, int seconds) {
            Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
                    .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                    .putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                    .putExtra(AlarmClock.EXTRA_SKIP_UI, false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }

        //        private void registerTimer(long duration) {
//            // Get the alarm manager.
//            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//
//            // Create intent that gets fired when timer expires.
//            Intent intent = new Intent(Constants.ACTION_SHOW_ALARM, null, this,
//                    TimerNotificationService.class);
//            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT);
//
//            // Calculate the time when it expires.
//            long wakeupTime = System.currentTimeMillis() + duration;
//
//            // Schedule an alarm.
//            alarm.setExact(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent);
//        }
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mWidthX = width;
            mHeightY = height;
            /*
             * Find the coordinates of the center point on the screen.
             * Ignore the window insets so that, on round watches
             * with a "chin", the watch face is centered on the entire screen,
             * not just the usable portion.
             */
            mCenterX = mWidthX / 2f;
            mCenterY = mHeightY / 2f;
            mScale = ((float) width) / (float) mBackgroundBitmap.getWidth();
            /*
             * Calculate the lengths of the watch hands and store them in member variables.
             */
            mHCoefButt = (int) (mHeightY / 3f);

            mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                    (int) (mBackgroundBitmap.getWidth() * mScale),
                    (int) (mBackgroundBitmap.getHeight() * mScale), true);
        }

        private String formatTwoDigitNumber(int hour) {
            return String.format("%02d", hour);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
//            if (isInAmbientMode()) {
//                canvas.drawColor(Color.BLACK);
//            } else {
//                // canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
//
//                canvas.drawBitmap(mBackgroundBitmap, 0, 0, mBackgroundPaint);
//            }
            canvas.drawBitmap(mBackgroundBitmap, 0, 0, mBackgroundPaint);
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            boolean is24Hour = DateFormat.is24HourFormat(E52.this);


            switch (iInFacesCount) {
                case 1:
                    // Draw the hours.
                    String hourString;
                    hourString = formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY));
                    canvas.drawText(hourString, mXOffset, mYOffset, mHourPaint);
                    //   x += mHourPaint.measureText("88")+mColonWidth/2;
                    // Draw first colon (between hour and minute).
                    //  canvas.drawText(STRING_COLON, x, mYOffset, mColonPaint);

                    if (bChime) {
                        canvas.drawText(STRING_CHIME_TIMER, mXOffsetMinute + mColonWidth / 3, mYOffsetStatusSymb, mStatusSymbPaint);// Кукушка
                    }
                    if (!bStopwatchTimeStartStop && lStopwatchTimeMls != 0) {
                        canvas.drawText(STRING_STOPWATCH_STOP, mXOffsetColonHM - mColonWidth / 4, mYOffsetStatusSymb, mStatusSymbPaint);// Секундомер
                    }
//                    if (bButtTimerStartStop && lButtCurrentTimerEndTimeMls != 0) {
//                        canvas.drawText(STRING_BUTT_TIMER, mXCalendDayOffset + mColonWidth / 4, mYOffset - mColonWidth , mStatusSymbPaint);// сидячий таймер
//                    }
                    if (bAlarmTimeStartStop) {
                        canvas.drawText(STRING_ALARM_CLOCK, mXOffset + mColonWidth / 3, mYOffsetStatusSymb, mStatusSymbPaint); // будильник
                    }
// blinking symbols start
                    if (mMultiplic > 1) {
                        mMultiplic = 0;
                    }
                    switch (mMultiplic) {
                        case 0:
                            canvas.drawText(STRING_COLON, mXOffsetColonHM, mYOffset, mColonPaint);
                            if (bStopwatchTimeStartStop && lStopwatchTimeMls != 0) {
                                canvas.drawText(STRING_STOPWATCH_STOP, mXOffsetColonHM, mYOffsetStatusSymb, mStatusSymbPaint);
                            }
                            if (bTimerTimeStartStop) {
                                canvas.drawText(string_TIMER, mXOffsetMinute + 2 * mColonWidth - mColonWidth / 2, mYOffsetStatusSymb, mStatusSymbPaint);
                            }
                            if (bButtTimerStartStop && lButtCurrentTimerEndTimeMls != 0) {
                                canvas.drawText(STRING_BUTT_TIMER, mXCalendDayOffset + mColonWidth / 4, mYOffset - mColonWidth, mStatusSymbPaint);// сидячий таймер
                            }
                            mMultiplic++;
                            break;
                        case 1:
                            mMultiplic = 0;
                            break;

                    }
// blinking symbols end

                    // Draw the minutes.
                    String minuteString = formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));
                    canvas.drawText(minuteString, mXOffsetMinute, mYOffset, mMinutePaint);

//              // TODO: 24.09.2016 дата и день в позициях
                    canvas.drawText(formatTwoDigitNumber(mCalendar.get(Calendar.SECOND)), mXCalendDayOffset, mYOffset, mSecondPaint);

                    canvas.drawText(formatTwoDigitNumber(mCalendar.get(Calendar.DATE)), mXCalendDayOffset, mYCalendDayOffset, mCalendarDatePaint);

                    int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
                    String weekday = new DateFormatSymbols().getShortWeekdays()[dayOfWeek];
                    dayOfWeek--;
                    if (dayOfWeek == 0) {
                        dayOfWeek = 7;
                    }
                    float xc = (mXOffset - mColonWidth / 2) + dayOfWeek * ((mXOffsetCalendarPaintOffset - mColonWidth) / 7);
                    canvas.drawText(weekday, xc, mYCalendarPaintOffset, mCalendarPaint);

                    break;
                case 2:
//                    Alarm draw start
                    // Draw the hours.
                    tmpMillis = lAlarmTimeMls;
                    if (tmpMillis != 0) {
                        hourString = formatTwoDigitNumber((int) MILLISECONDS.toHours(tmpMillis) % 24);
                        canvas.drawText(hourString, mXOffset, mYOffset, mHourPaint);
                    } else {
                        canvas.drawText("00", mXOffset, mYOffset, mHourPaint);
                    }

                    // Draw the minutes.
                    if (tmpMillis != 0) {
                        hourString = formatTwoDigitNumber((int) MILLISECONDS.toMinutes(tmpMillis) % 60);
                        canvas.drawText(hourString, mXOffsetMinute, mYOffset, mMinutePaint);
                    } else {
                        canvas.drawText("00", mXOffsetMinute, mYOffset, mMinutePaint);
                    }


                    if (bAlarmTimeStartStop) {
//                         blinking symbols start
                        if (mMultiplic > 1) {
                            mMultiplic = 0;
                        }
                        switch (mMultiplic) {
                            case 0:
                                canvas.drawText(STRING_COLON, mXOffsetColonHM, mYOffset, mColonPaint);
                                canvas.drawText(STRING_ALARM_CLOCK, mXOffset + mColonWidth / 3, mYOffsetStatusSymb, mStatusSymbPaint); // будильник
                                mMultiplic++;
                                break;
                            case 1:
                                mMultiplic = 0;
                                break;

                        }
//                         blinking symbols end
                    } else {
                        canvas.drawText(STRING_ALARM_CLOCK, mXOffset + mColonWidth / 3, mYOffsetStatusSymb, mStatusSymbPaint); // будильник
                    }
//                    Alarm draw end
                    break;
                case 3:
//                    Stop watch draw start
                    // Draw the hours.
                    if (lStopwatchTimeMls != 0) {
                        if (bStopwatchTimeStartStop) {
                            tmpMillis = mCalendar.getTimeInMillis() - lStopwatchTimeMls;
                        } else {
                            tmpMillis = lStopwatchInbetweenTimeMls;
                        }


                    } else {
                        tmpMillis = 0;
                    }
                    if (tmpMillis != 0) {
                        hourString = formatTwoDigitNumber((int) MILLISECONDS.toHours(tmpMillis) % 24);
                        canvas.drawText(hourString, mXOffset, mYOffset, mHourPaint);
                    } else {
                        canvas.drawText("00", mXOffset, mYOffset, mHourPaint);
                    }
                    // Draw first colon (between hour and minute).
                    //  canvas.drawText(STRING_COLON, mXOffsetColonHM, mYOffset, mColonPaint);

                    // Draw the minutes.
                    if (tmpMillis != 0) {
                        hourString = formatTwoDigitNumber((int) MILLISECONDS.toMinutes(tmpMillis) % 60);
                        canvas.drawText(hourString, mXOffsetMinute, mYOffset, mMinutePaint);
                    } else {
                        canvas.drawText("00", mXOffsetMinute, mYOffset, mMinutePaint);
                    }
                    // Draw the SECONDS.

                    if (tmpMillis != 0) {
                        hourString = formatTwoDigitNumber((int) MILLISECONDS.toSeconds(tmpMillis) % 60);
                        canvas.drawText(hourString, mXCalendDayOffset, mYOffset, mSecondPaint);
                    } else {
                        canvas.drawText("00", mXCalendDayOffset, mYOffset, mSecondPaint);
                    }

                    // Draw the MILLISECONDS.
                    if (tmpMillis != 0) {
                        hourString = formatTwoDigitNumber((int) MILLISECONDS.toMillis(tmpMillis) % 100);
                        canvas.drawText(hourString, mXCalendDayOffset, mYCalendDayOffset, mCalendarDatePaint);
                    } else {
                        canvas.drawText("00", mXCalendDayOffset, mYCalendDayOffset, mCalendarDatePaint);
                    }

                    if (bStopwatchTimeStartStop) {
//                         blinking symbols start
                        if (mMultiplic >= 100) {
                            canvas.drawText(STRING_COLON, mXOffsetColonHM, mYOffset, mColonPaint);
                            if (bStopwatchTimeStartStop && lStopwatchTimeMls != 0) {
                                canvas.drawText(STRING_STOPWATCH_STOP, mXOffsetColonHM, mYOffsetStatusSymb, mStatusSymbPaint); // 66 кубок
                                if (mMultiplic <= 200) {
                                    mMultiplic++;
                                } else {
                                    mMultiplic = 0;
                                }
                            } else {
                                mMultiplic++;
                            }


//                         blinking symbols end
                        } else {
                            mMultiplic++;
                        }
                    } else {
                        canvas.drawText(STRING_STOPWATCH_STOP, mXOffsetColonHM, mYOffsetStatusSymb, mStatusSymbPaint);
                    }

//                    Stop watch draw  end
                    break;
                case 4:
//                    Timer draw start
                    // Draw the hours.

                    if (bTimerTimeStartStop) {

                        tmpMillis = lCurrentTimerEndTimeMls - mCalendar.getTimeInMillis();


                    } else {
                        if (lCurrentTimerInbetweenEndTimeMls != 0) {
                            tmpMillis = lCurrentTimerInbetweenEndTimeMls;
                        } else {
                            tmpMillis = lTimerSetTimeMls;
                        }

                    }

                    if (tmpMillis != 0) {
                        hourString = formatTwoDigitNumber((int) MILLISECONDS.toHours(tmpMillis) % 24);
                        canvas.drawText(hourString, mXOffset, mYOffset, mHourPaint);
                    } else {
                        canvas.drawText("00", mXOffset, mYOffset, mHourPaint);
                    }
                    // Draw first colon (between hour and minute).
                    //  canvas.drawText(STRING_COLON, mXOffsetColonHM, mYOffset, mColonPaint);

                    // Draw the minutes.
                    if (tmpMillis != 0) {
                        hourString = formatTwoDigitNumber((int) MILLISECONDS.toMinutes(tmpMillis) % 60);
                        canvas.drawText(hourString, mXOffsetMinute, mYOffset, mMinutePaint);
                    } else {
                        canvas.drawText("00", mXOffsetMinute, mYOffset, mMinutePaint);
                    }
                    // Draw the SECONDS.

                    if (tmpMillis != 0) {
                        hourString = formatTwoDigitNumber((int) MILLISECONDS.toSeconds(tmpMillis) % 60);
                        canvas.drawText(hourString, mXCalendDayOffset, mYOffset, mSecondPaint);
                    } else {
                        canvas.drawText("00", mXCalendDayOffset, mYOffset, mSecondPaint);
                    }


                    if (bTimerTimeStartStop) {
//                         blinking symbols start
                        if (mMultiplic > 1) {
                            mMultiplic = 0;
                        }
                        switch (mMultiplic) {
                            case 0:
                                canvas.drawText(STRING_COLON, mXOffsetColonHM, mYOffset, mColonPaint);
                                if (bTimerTimeStartStop && tmpMillis != 0) {
                                    canvas.drawText(string_TIMER, mXOffsetMinute + 2 * mColonWidth - mColonWidth / 2, mYOffsetStatusSymb, mStatusSymbPaint); // таймер
                                }
                                mMultiplic++;
                                break;
                            case 1:
                                mMultiplic = 0;
                                break;

                        }
//                         blinking symbols end
                    } else {
                        canvas.drawText(string_TIMER, mXOffsetMinute + 2 * mColonWidth - mColonWidth / 2, mYOffsetStatusSymb, mStatusSymbPaint); // таймер
                    }

//                    Timer  draw  end
                    break;
                case 5:
//                    Fit  draw start
                    // Only render steps if there is no peek card, so they do not bleed into each other
                    // in ambient mode.
                    if (getPeekCardPosition().isEmpty()) {
                        canvas.drawText(STRING_FIT_PEDESTRIAN, mXOffset, mYOffset - mXFitOffsetStatusSymb / 2, mFitStatusSymbPaint);//mYCalendarPaintOffset
                        canvas.drawText(
                                getString(R.string.fit_steps, mStepsTotal), //Количество прошагано
                                mXOffset + mXFitOffsetStatusSymb / 2,
                                mYOffset,
                                mStepCountPaint);

                    }

                    canvas.drawText(STRING_CLOCK_BATT, mXOffset, mYCalendDayOffset - mXFitOffsetStatusSymb / 4, mFitStatusSymbPaint);
                    canvas.drawText(battLevString, mXOffset + mXFitOffsetStatusSymb / 2, mYCalendDayOffset, mBattLevPaint); //Батарейка с процентами  +mXFitOffsetStatusSymb


//                    Fit  draw end
                    break;
                case 99: // // TODO: 28.09.2016 Проверочный экран для заполнения и теста размещения
                    // Draw the hours.
                    canvas.drawText("88", mXOffset, mYOffset, mHourPaint);
                    // Draw the Status Symb.
                    canvas.drawText(STRING_CHIME_TIMER, mXOffsetMinute + mColonWidth / 3, mYOffsetStatusSymb, mStatusSymbPaint); // Кукушка
                    canvas.drawText(STRING_STOPWATCH_STOP, mXOffsetColonHM - mColonWidth / 4, mYOffsetStatusSymb, mStatusSymbPaint);// Секундомер
                    canvas.drawText(STRING_ALARM_CLOCK, mXOffset + mColonWidth / 3, mYOffsetStatusSymb, mStatusSymbPaint); // будильник
                    canvas.drawText(STRING_BUTT_TIMER, mXCalendDayOffset + mColonWidth / 4, mYOffset - mColonWidth, mStatusSymbPaint);// сидячий таймер mColonWidth/2
//                  Multiplic start
                    // Draw first colon (between hour and minute).
                    canvas.drawText(STRING_COLON, mXOffsetColonHM, mYOffset, mColonPaint);

                    //      canvas.drawText(Character.toString((char) 66), mXOffsetColonHM,  mYCalendDayOffset, mStatusSymbPaint);

                    canvas.drawText(string_TIMER, mXOffsetMinute + 2 * mColonWidth - mColonWidth / 2, mYOffsetStatusSymb, mStatusSymbPaint); // таймер


                    // Draw the minutes.


                    canvas.drawText("88", mXOffsetMinute, mYOffset, mMinutePaint);
//              // TODO: 24.09.2016 дата и день в позициях
                    canvas.drawText("88", mXCalendDayOffset, mYOffset, mSecondPaint);
                    canvas.drawText("88", mXCalendDayOffset, mYCalendDayOffset, mCalendarDatePaint);

                    for (int i = 1; i < 8; i++) {
                        dayOfWeek = i;
                        weekday = new DateFormatSymbols().getShortWeekdays()[dayOfWeek];
                        dayOfWeek--;
                        if (dayOfWeek == 0) {
                            dayOfWeek = 7;
                        }
                        xc = (mXOffset - mColonWidth / 2) + dayOfWeek * ((mXOffsetCalendarPaintOffset - mColonWidth) / 7);
                        canvas.drawText(weekday, xc, mYCalendarPaintOffset, mCalendarPaint);
                    }

//                    canvas.drawRect
//// TODO: 29.09.2016 draw interaction bounds
                    Rect rClockButtonsBoundingRect = new Rect(0, 0, 0, 0);
                    rClockButtonsBoundingRect.set(
                            0,                                          // left
                            mHeightY - mHCoefButt,                       // top
                            mHCoefButt,                               // right
                            mHeightY);                            // bottom
                    mClockButtonsBoundingRect.setColor(Color.RED);
                    mClockButtonsBoundingRect.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(rClockButtonsBoundingRect, mClockButtonsBoundingRect);


                    //Left Upper Button Свет
                    rClockButtonsBoundingRect.set(
                            0,                              // left
                            0,                              // top
                            mHCoefButt,                     // right
                            mHCoefButt);                    // bottom
                    canvas.drawRect(rClockButtonsBoundingRect, mClockButtonsBoundingRect);
                    //right Upper Button ВКЛ
                    rClockButtonsBoundingRect.set(
                            mWidthX - mHCoefButt,                              // left
                            0,                              // top
                            mWidthX,                     // right
                            mHCoefButt);                    // bottom
                    //  canvas.draw
                    canvas.drawRect(rClockButtonsBoundingRect, mClockButtonsBoundingRect);
                    //right bottom Button ВЫБОР
                    rClockButtonsBoundingRect.set(
                            mWidthX - mHCoefButt,                              // left
                            mHeightY - mHCoefButt,                              // top
                            mWidthX,                     // right
                            mHeightY);                    // bottom
                    canvas.drawRect(rClockButtonsBoundingRect, mClockButtonsBoundingRect);


                    break;


            }


            //  canvas.drawText( text, mXOffset, mYOffset, mTextPaint);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {

            if (lCurrPosmenuEndTimeMls != 0 && mCalendar.getTimeInMillis() >= lCurrPosmenuEndTimeMls) {
                lCurrPosmenuEndTimeMls = 0;
                iInFacesCount = 1;

            }
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs;
                if (iInFacesCount == 3) {
                    delayMs = INTERACTIVE_UPDATE_RATE_MS_IN_STOPWACH_MODE
                            - (timeMs % INTERACTIVE_UPDATE_RATE_MS_IN_STOPWACH_MODE);
                } else {
                    delayMs = INTERACTIVE_UPDATE_RATE_MS
                            - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                }
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        private void getTotalSteps() {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "getTotalSteps()");
            }

            if ((mGoogleApiClient != null)
                    && (mGoogleApiClient.isConnected())
                    && (!mStepsRequested)) {

                mStepsRequested = true;

                PendingResult<DailyTotalResult> stepsResult =
                        Fitness.HistoryApi.readDailyTotal(
                                mGoogleApiClient,
                                DataType.TYPE_STEP_COUNT_DELTA);

                stepsResult.setResultCallback(this);
            }
        }

        @Override
        public void onConnected(Bundle connectionHint) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "mGoogleApiAndFitCallbacks.onConnected: " + connectionHint);
            }
            mStepsRequested = false;

            // The subscribe step covers devices that do not have Google Fit installed.
            subscribeToSteps();

            getTotalSteps();
        }

        /*
         * Subscribes to step count (for phones that don't have Google Fit app).
         */
        private void subscribeToSteps() {
            Fitness.RecordingApi.subscribe(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                if (status.getStatusCode()
                                        == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                    Log.i(TAG, "Existing subscription for activity detected.");
                                } else {
                                    Log.i(TAG, "Successfully subscribed!");
                                }
                            } else {
                                Log.i(TAG, "There was a problem subscribing.");
                            }
                        }
                    });
        }

        @Override
        public void onConnectionSuspended(int cause) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "mGoogleApiAndFitCallbacks.onConnectionSuspended: " + cause);
            }
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "mGoogleApiAndFitCallbacks.onConnectionFailed: " + result);
            }
        }

        @Override
        public void onResult(DailyTotalResult dailyTotalResult) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "mGoogleApiAndFitCallbacks.onResult(): " + dailyTotalResult);
            }

            mStepsRequested = false;

            if (dailyTotalResult.getStatus().isSuccess()) {

                List<DataPoint> points = dailyTotalResult.getTotal().getDataPoints();
                ;

                if (!points.isEmpty()) {
                    mStepsTotal = points.get(0).getValue(Field.FIELD_STEPS).asInt();

                    //  mStepsTotal = points.get(0).getValue(Field.FIELD_STEPS).asInt();
//                    // TODO: 15.11.2016 -ж.. сидячий таймер переставлен
                    lButtCurrentTimerEndTimeMls = mCalendar.getTimeInMillis() + lButtTimerSetTimeMls; // сделали шаг -ж.. сидячий таймер переставлен

//                    long lButtTimerSetTimeMls = TimeUnit.SECONDS.toMillis(20) + TimeUnit.MINUTES.toMillis(30);
//                    long lButtCurrentTimerEndTimeMls = 0;
//                    long lButtCurrentTimerInbetweenEndTimeMls = 0;

                    Log.d(TAG, "steps updated: " + mStepsTotal);

                }
            } else {
                Log.e(TAG, "onResult() failed! " + dailyTotalResult.getStatus().getStatusMessage());
            }
        }
        public void sendVibroMessage(int s) {

            long[] vibrationPattern1= new long[2*s+1] ;
            //-1 - don't repeat
            int indexInPatternToRepeat = -1;


            Vibrator v = (Vibrator) getBaseContext().getSystemService(VIBRATOR_SERVICE);
            if (v.hasVibrator()) {
                for (int i = 0; i <2*s ; i=i+2) {
                    vibrationPattern1[s]=100;// Length of a Morse Code "dot" in milliseconds 200 /2
                    vibrationPattern1[s+1]=140;// Length of Gap Between dots/dashes 200

                }
                v.vibrate(  vibrationPattern1 ,indexInPatternToRepeat);//

            }
            else {
            }
        }
        private void sendClockMessage(String s1, String  s2) {
            //Define Notification Manager
            NotificationManager notificationManager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);

//Define sound URI
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.preview_digital)
                    .setContentTitle(s1)
                    .setContentText(s2)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setSound(soundUri)
                    .setVibrate(new long[] {50,50,100,50,100}); //This sets the sound to play


//Display notification
            notificationManager.notify(0, mBuilder.build());
            //  Notifications.notify(this, 5000, "This text will go away after five seconds.");

            //   Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
        }


        private void refreshsetAlarmAndSetNextUpdate(int hours , int minutes) {


            Calendar tmpCal= Calendar.getInstance();;
            long timeMs = mCalendar.getTimeInMillis();

            // Schedule a new alarm
            if (bAlarmTimeStartStop) {
                // Calculate the next chime trigger time

// время до старта аларма
//                tmpCal.se
                tmpCal.setTimeInMillis(timeMs);
                tmpCal.set(Calendar.HOUR_OF_DAY, hours);
                tmpCal.set(Calendar.MINUTE, minutes);
                tmpCal.set(Calendar.SECOND, 0);
                tmpCal.set(Calendar.MILLISECOND, 0);

               if (tmpCal.after(mCalendar)) {

               }
               else {
                   tmpCal.add(Calendar.DAY_OF_MONTH, 1);

               };



                long triggerTimeMs = tmpCal.getTimeInMillis();
// Schedule an alarm.
                mSpcStateAlarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        mSpcStateAlarmPendingIntent);
                Log.d(TAG, "refreshAlarm->"+ TimeInMilisToStr(triggerTimeMs)+"now> "+TimeInMilisToStr(timeMs));


            }
        }
        private void deleteAlarm() {


            // delete timer alarm

            mSpcStateAlarmManager.cancel(mSpcStateAlarmPendingIntent);



        }



        private void refreshChimeAndSetNextUpdate() {


            Calendar tmpCal= Calendar.getInstance();;
            long timeMs = mCalendar.getTimeInMillis();

            // Schedule a new alarm
            if (bChime) {
                // Calculate the next chime trigger time
                tmpCal.setTimeInMillis(timeMs);
                tmpCal.add(Calendar.HOUR_OF_DAY, 1);
                tmpCal.set(Calendar.MINUTE, 0);
                tmpCal.set(Calendar.SECOND, 0);
                tmpCal.set(Calendar.MILLISECOND, 0);

                long triggerTimeMs = tmpCal.getTimeInMillis();
// Schedule an alarm.
                mSpcStateAlarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        mSpcStateChimePendingIntent);
                Log.d(TAG, "refreshChimeAndSetNextUpdate->"+ TimeInMilisToStr(triggerTimeMs));


            }
        }
        private void deleteChime() {


            // delete timer alarm

            mSpcStateAlarmManager.cancel(mSpcStateChimePendingIntent);



        }


        private void refreshTimerAndSetNextUpdate(long lTimerSetMls) {
//            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//            alarm.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + timeToWakeUp, pendingIntent);

            // Schedule a new timer alarm
            if (bTimerTimeStartStop) {
                // Schedule an alarm.
                long wakeupTime = SystemClock.elapsedRealtime() + lTimerSetMls;///System.currentTimeMillis()
                mSpcStateAlarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP ,//AlarmManager.RTC_WAKEUP
                        wakeupTime , //lCurrentTimerEndTimeMls
                        mSpcStateTimerPendingIntent);
                Log.d(TAG, "refreshTimerAndSetNextUpdate->"+ TimeInMilisToStr(lCurrentTimerEndTimeMls)+" "+TimeInMilisToStr(wakeupTime));

            }
        }
        private void deleteTimer() {


            // delete timer alarm

                mSpcStateAlarmManager.cancel(mSpcStateTimerPendingIntent);



        }

        private void refreshButtTimerAndSetNextUpdate() {


            // Schedule a new Butt timer alarm
            if (bButtTimerStartStop) {
                // Schedule an alarm.
                mSpcStateAlarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        lButtCurrentTimerEndTimeMls,
                        mSpcStateButtTimerPendingIntent);
               // mSpcStateAlarmManager.

            }
        }
        private void deleteButtTimer() {


            //delete Butt timer alarm

                mSpcStateAlarmManager.cancel(mSpcStateButtTimerPendingIntent);


        }

        public PendingIntent getGoNextTimerPendingIntent( ){
//            // TODO: 26.11.2016 intent to set many more data
            Intent restartIntent =
                    new Intent(getApplicationContext(), MySpcIntentService.class);//MyIntentServiceMySpcIntentService
            restartIntent.setAction("nextTimeraction");
            restartIntent.putExtra("extra","nextE52StateTimer");
            PendingIntent pendingIntentRestart = PendingIntent.getService(
                    getApplicationContext(),
                    99,
                    restartIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            return pendingIntentRestart;
        }

        /**
         * Build a notification including different actions and other various setup and return it.
         *
         * @param s2 name the next  timer.
         * @return the notification to display.
         */
        private void sendClockMessagewithstartNextTimer(String s1, String  s2) {
            //             Intent to restart a timer.

            NotificationManager notificationManager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Action actionnext;
            actionnext = new NotificationCompat.Action(android.R.drawable.ic_popup_reminder, "Go Next timer-> \n" +s2, getGoNextTimerPendingIntent());

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.preview_digital)
                    .setContentTitle(s1)
                    .setContentText(s2)
                    .setDefaults(Notification.DEFAULT_SOUND)
                   // .setSound(soundUri)
                    //.setVibrate(new long[] {150,50,100,150,100}) //This sets the sound to play
                    .addAction(actionnext)

                    .setOnlyAlertOnce(true)
                    .setLocalOnly(true);

//Display notification

            notificationManager.notify(99, mBuilder.build());
        }



        /**
         * Cancels an old countdown and deletes the dataItem.
         *
         * @param notifyMgr the notification manager.
         */
        private void cancelCountdown(NotificationManager notifyMgr) {
            notifyMgr.cancel(Constants.NOTIFICATION_TIMER_EXPIRED);
        }


        @Override
        public void onServiceConnected(MySpcIntentService service) {
            this.mySpcIntentService = service;
            servONduty = true;
        }

        @Override
        public void onServiceDisconnected(MySpcIntentService service) {
            servONduty = false;
        }


        @Override
        public void callAlarmfromService(String tmp) {
            Log.d(TAG, "<<<<<<<<<<<<<<<<<callAlarmfromService()->>>>>>>>>>>>>>>>>>>");
            long tmpCurrTim = mCalendar.getTimeInMillis();
            switch (tmp)
            {

                case "Alarmaction":{
                    if (bAlarmTimeStartStop) {

                        extrAlarmFire(tmpCurrTim);
                    }
                }
                break;
                case "Chimeaction":{
                    if (bChime) {

                        extrChimeFire(tmpCurrTim);
                    }
                }
                break;
                case "Timeraction":{
                    extrTimerFire(tmpCurrTim);

                }
                break;
                case "ButtTimeraction":{
                    extrButtTimerFire(tmpCurrTim);
                }
                break;
                case "nextTimeraction" :{
                    extrnextTimerActionFire();
                }
                break;
                case "changeTimerSetaction" :{
                    alTimersCategories = allData.getAlTimersCategories();
                    setUpInternalTimers();
                }
                break;


            }

        }

        public void extrnextTimerActionFire() {
            NotificationManager notificationManager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(99);
            if (!bTimerTimeStartStop) {
                bTimerTimeStartStop = true;
                if (lCurrentTimerEndTimeMls != 0) {
                    lCurrentTimerEndTimeMls = mCalendar.getTimeInMillis() + lCurrentTimerInbetweenEndTimeMls;
                    //  startTimer(getString(R.string.timer_set_str), (int) MILLISECONDS.toSeconds(lCurrentTimerInbetweenEndTimeMls));
                    refreshTimerAndSetNextUpdate(lCurrentTimerInbetweenEndTimeMls);
                    lCurrentTimerInbetweenEndTimeMls = 0;

                } else {
//                                            Запускаем таймер
//                                            startTimer(getString(R.string.timer_set_str), (int) MILLISECONDS.toSeconds(lTimerSetTimeMls));
                    lCurrentTimerEndTimeMls = mCalendar.getTimeInMillis() + lTimerSetTimeMls;
                    refreshTimerAndSetNextUpdate(lTimerSetTimeMls);
                }
            }
        }

        public void  extrAlarmFire (long tmpCurrTim) {
            sendVibroMessage(8);
            sendClockMessage(getString(R.string.my_digital_name),getString(R.string.alarm_set_done) ); //my_digital_name
            bAlarmTimeStartStop=false;
        }
        public void extrChimeFire(long tmpCurrTim) {
//            if (iChimeCurrHours < (TimeUnit.MILLISECONDS.toHours(tmpCurrTim) % 24)) {
//                iChimeCurrHours = (int) TimeUnit.MILLISECONDS.toHours(tmpCurrTim) % 24;
//                sendVibroMessage(iChimeCurrHours);
//
//            }
            sendVibroMessage(1);

        }

        public void extrButtTimerFire(long tmpCurrTim) {
            if (bButtTimerStartStop) {
                if (lButtCurrentTimerEndTimeMls >= tmpCurrTim) {

                }
            }
        }

        public void extrTimerFire(long tmpCurrTim) {
            if (bTimerTimeStartStop) {
                //     sendVibroMessage(1);
                if (alTimersCategories.get(ialTimersCategoriesActiveLvls[0])
                        .alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1])
                        .timersTimes.get(ialTimersCategoriesActiveLvls[2]).typeTimerBehavior==1)
                {
                    sendVibroMessage(1);
                } // for timer
              //  if (lCurrentTimerEndTimeMls <= tmpCurrTim) {
//                        Пора остановить таймер
                Log.d(TAG, "Пора остановить таймер->"+ TimeInMilisToStr(tmpCurrTim)+" "+TimeInMilisToStr(lCurrentTimerEndTimeMls));
                    lCurrentTimerEndTimeMls = 0;
                    lCurrentTimerInbetweenEndTimeMls = 0;
                    bTimerTimeStartStop = false;
                    sendVibroMessage(4);
                    String s = alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1])
                            .timersTimes.get(ialTimersCategoriesActiveLvls[2]).name;

                    // step to next timer

                    alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1])
                            .timersTimes.get(ialTimersCategoriesActiveLvls[2]).active = false;
                    // to next active
                    alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1])
                            .timersTimes.get(ialTimersCategoriesActiveLvls[2]).repeats++;
                    // repeats set ++1
                    switch (alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1])
                            .timersTimes.get(ialTimersCategoriesActiveLvls[2]).nextDo) {
                        //type 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
                        case 0: {
                            int tmpid = alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1])
                                    .timersTimes.get(ialTimersCategoriesActiveLvls[2]).nextid;

                            ialTimersCategoriesActiveLvls[2] =alTimersCategories.get(ialTimersCategoriesActiveLvls[0])
                                                                .alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1])
                                                                 .getIdXbyId_timersTimes(
                                                                         alTimersCategories.get(ialTimersCategoriesActiveLvls[0])
                                                                                 .alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1])
                                                                        .timersTimes, tmpid);

                            //switch to next id
                            lTimerSetTimeMls =  alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1])
                                    .timersTimes.get(ialTimersCategoriesActiveLvls[2]).time;

                            alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1])
                                    .timersTimes.get(ialTimersCategoriesActiveLvls[2]).active = true; //set active new id


                        }
                        break;
                        case 1: {

                        }
                        break;
                    }
                    allData.setAlTimersCategories(alTimersCategories);
//sendClockMessage();
                    sendClockMessagewithstartNextTimer(s,alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1])
                            .timersTimes.get(ialTimersCategoriesActiveLvls[2]).name );//getString(R.string.timer_set_str)


            }
        }
    }

    private String TimeInMilisToStr(long time) {
        long second = (time / 1000) % 60;
        long minute = (time / (1000 * 60)) % 60;
        long hour = (time / (1000 * 60 * 60)) % 24;
        return  String.format("%02d:%02d:%02d", hour, minute, second);
    }


    // am.setRepeating(AlarmManager.RTC, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_DAY, pendingIntent);

}