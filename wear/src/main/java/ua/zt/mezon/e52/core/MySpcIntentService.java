package ua.zt.mezon.e52.core;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MySpcIntentService extends IntentService {
  //  private RxWear rxWear;

    public MySpcIntentService() {
        super("MyIntentService");
    }
    final String LOG_TAG = "MySpcIntentService>";

    private static ServiceConnector.ServiceListener serviceConnection1;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "Служба Bind");
        return super.onBind(intent);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
        wakeLock.setReferenceCounted(true);
        if(! wakeLock.isHeld()) {
            wakeLock.acquire();
        }
        String tmp = " ";
       // wakeLock.acquire();
        if (intent.getAction() != null) {
             tmp=intent.getAction();

        }

//        rxWear= new RxWear(getApplicationContext());
        Log.d(LOG_TAG, "onReceive in  MyIntentService");
        Log.d(LOG_TAG, "action = " + intent.getAction());
        Log.d(LOG_TAG, "extra = " + intent.getStringExtra("extra"));
        Log.d(TAG, "onHandleIntent called with intent in  MyIntentService : " + intent);
//        rxWear.message().sendDataMapToAllRemoteNodes("/dataMap")
//                .putString("WearHandleIntent", intent.getAction())
//                .toObservable()
//                .subscribe(requestId -> {
//            /* do something */
//                });

        serviceConnection1.callAlarmfromService(tmp);

        if(wakeLock != null) {
            if(wakeLock.isHeld()) {
                wakeLock.release();
            }
            wakeLock = null;
        }

    }

    public static <T extends Service> ServiceConnector<T> bindService(final Class<T> serviceClazz, final Context context, final ServiceConnector.ServiceListener<T> listener) {
        ServiceConnector<T> serviceConnection = new ServiceConnector<T>(listener);

        Intent intent = new Intent(context, serviceClazz);
        Intent startServiceIntent = new Intent(context, serviceClazz);
        context.startService(startServiceIntent);
        context.bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        serviceConnection1= serviceConnection.getServiceListener();
        return serviceConnection;
    }
}
