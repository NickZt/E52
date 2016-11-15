package ua.zt.mezon.e52;

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
    public CompositeSubscription subscription = new CompositeSubscription();
    public Observable<Boolean> validator;

    public RxWear rxWear;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        mainActivityTimePicker.setHour(0);
        mainActivityTimePicker.setMinute(0);
        lSelTimeAlarm=0;

        subscription.add(RxView.clicks( mainActivitySetAlarmButton)
              //  .doOnNext(click -> hideKeyboard())
              //  .flatMap(click2 -> validate())

               // .filter(isValid -> isValid)
                .flatMap(valid ->  allData.rxWear.message().sendDataMapToAllRemoteNodes("/message")
                        .putLong("alarm",  lSelTimeAlarm)
                       // .putString("message", messageEditText.getText().toString())
                        .toObservable()
                ).subscribe(requestId -> Toast.makeText(view.getContext(), "Sent message", Toast.LENGTH_SHORT).show(),//.make(coordinatorLayout, "Sent message", Snackbar.LENGTH_LONG).show()
                        throwable -> {
                            Log.e("MainActivity", "Error on sending message", throwable);

                            if(throwable instanceof GoogleAPIConnectionException) {
                                Toast.makeText(view.getContext(), "Android Wear app is not installed", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(view.getContext(), "Could not send message", Toast.LENGTH_LONG).show();
                            }
                        })
        );





        return view;
    }

    @Override
    public void onTimeChanged(TimePicker timePicker,int hourOfDay, int minute) {
        lSelTimeAlarm = TimeUnit.HOURS.toMillis(hourOfDay)+TimeUnit.HOURS.toMillis(minute);;
    }
}