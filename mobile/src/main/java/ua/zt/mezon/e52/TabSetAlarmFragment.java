package ua.zt.mezon.e52;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.patloew.rxwear.GoogleAPIConnectionException;
import com.patloew.rxwear.RxWear;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by MezM on 03.11.2016.
 */

public class TabSetAlarmFragment extends Fragment implements TimePicker.OnTimeChangedListener{
    TimePicker mainActivityTimePicker;
    Button mainActivitySetAlarmButton;
    private AllData allData = AllData.getInstance();
    long lSelTimeAlarm=0;
    private CompositeSubscription subscription = new CompositeSubscription();
    private Observable<Boolean> validator;

    public RxWear rxWear;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tabsetalarm, container, false);
        rxWear= new RxWear(container.getContext());
//        view.findViewById(R.id.btn_play_again).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // We normally won't show the welcome slider again in real app
//                // but this is for testing
//                PrefManager prefManager = new PrefManager(v.getContext().getApplicationContext());
//
//                // make first time launch TRUE
//                prefManager.setFirstTimeLaunch(true);
//
//                startActivity(new Intent(v.getContext(), WelcomeActivity.class));
//             //  finish();
//            }
//        });
//
//
        mainActivityTimePicker = (TimePicker) view.findViewById(R.id.mainActivityTimePicker );
        mainActivityTimePicker.setIs24HourView(true);
        mainActivitySetAlarmButton = (Button)  view.findViewById(R.id.mainActivitySetAlarmButton);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityTimePicker.setHour(0);
        } else
        {
            mainActivityTimePicker.setCurrentHour(0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityTimePicker.setMinute(0);
        }else
        {
            mainActivityTimePicker.setCurrentMinute(0);
        }
        lSelTimeAlarm=0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            subscription.add(RxView.clicks( mainActivitySetAlarmButton)
                  //  .doOnNext(click -> hideKeyboard())
                  //  .flatMap(click2 -> validate())

                   // .filter(isValid -> isValid)
                    .flatMap(valid ->  rxWear.message().sendDataMapToAllRemoteNodes("/message")
                            .putLong("alarm",  lSelTimeAlarm = TimeUnit.HOURS.toMillis(mainActivityTimePicker.getHour())+TimeUnit.MINUTES.toMillis(mainActivityTimePicker.getMinute()))
                           // .putString("message", messageEditText.getText().toString())
                            .toObservable()
                    ).subscribe(requestId -> Toast.makeText(view.getContext(), "Sent message "+String.valueOf(lSelTimeAlarm), Toast.LENGTH_SHORT).show(),//.make(coordinatorLayout, "Sent message", Snackbar.LENGTH_LONG).show()
                            throwable -> {
                                Log.e("MainActivity", "Error on sending message", throwable);

                                if(throwable instanceof GoogleAPIConnectionException) {
                                    Toast.makeText(view.getContext(), "Android Wear app is not installed", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(view.getContext(), "Could not send message", Toast.LENGTH_LONG).show();
                                }
                            })
            );
        }


        return view;
    }

    @Override
    public void onTimeChanged(TimePicker timePicker,int hourOfDay, int minute) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            lSelTimeAlarm = TimeUnit.HOURS.toMillis(timePicker.getHour())+TimeUnit.MINUTES.toMillis(timePicker.getMinute());
        }else
        {
            lSelTimeAlarm = TimeUnit.HOURS.toMillis(timePicker.getCurrentHour())+TimeUnit.MINUTES.toMillis(timePicker.getCurrentMinute());
          //  mainActivityTimePicker.setCurrentMinute(0);
        }
    }
}