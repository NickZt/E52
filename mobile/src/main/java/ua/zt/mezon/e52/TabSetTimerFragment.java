package ua.zt.mezon.e52;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

import ua.zt.mezon.e52.misc.TimerWorkspace;
import ua.zt.mezon.e52.timerspslay.Tmr2lvlExpandableListAdapter;

/**
 * Created by MezM on 03.11.2016.
 */

public class TabSetTimerFragment extends Fragment implements TimePickerDialog.OnTimeSetListener {

    ArrayList<TimerWorkspace> alTimersCategories = new ArrayList<>();
    private int iActiveWorkSpaceindex;
    //TimePickerDialog  mTimerPickerDialog;
    Button btAddTimerWorkSpspButton;
    private AllData allData = AllData.getInstance();
    private TextView timeTextView;
    private Spinner tmrworkspspinner;
    public static final String TYPE = "TYPE";
//    private TmrDataSource mDataSource;
    private RecyclerView recyclerview;;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tabsettimer, container, false);
        btAddTimerWorkSpspButton = (Button) view.findViewById(R.id.AddTimerWorkSpspButton);
        alTimersCategories = allData.getAlTimersCategories();
        timeTextView = (TextView) view.findViewById(R.id.textView);
//        set Spinner
        tmrworkspspinner = (Spinner) view.findViewById(R.id.tmrworkspspinner);
        ArrayList<String> tmp = new ArrayList<>();
        for (TimerWorkspace z : alTimersCategories) {
            if (z.active) {
                tmp.add("+ " + "id " + Integer.toString(z.id) + " " + z.name);
                iActiveWorkSpaceindex =alTimersCategories.indexOf(z);
            } else {
                tmp.add("- " + "id " + Integer.toString(z.id) + " " + z.name);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(),
                R.layout.tmr_wrk_spc_spinner_item  , R.id.texttmrwrk, tmp.toArray(new String[tmp.size()])); //android.R.layout.simple_spinner_item R.layout.tmr_wrk_spc_spinner_item
      //  adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//    TimerWorkspaceSpinnAdapter wrkspc_spinadapter = new TimerWorkspaceSpinnAdapter(this.getContext(),
//            R.layout.tmr_wrk_spc_spinner_item  , alTimersCategories); //android.R.layout.simple_spinner_item R.layout.tmr_wrk_spc_spinner_item
//    wrkspc_spinadapter.setDropDownViewResource(R.layout.tmr_wrk_spc_spinner_item);


        tmrworkspspinner.setAdapter(adapter);
        tmrworkspspinner.setPrompt("Select Timer Work Space");


//        set Spinner

        btAddTimerWorkSpspButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        TabSetTimerFragment.this,
                        0, //hour now.get(Calendar.HOUR_OF_DAY)
                        0, //MINUTE now.get(Calendar.MINUTE)
                        0,//second
                        true //is24HourMode
                );
                tpd.vibrate(false);
                tpd.enableSeconds(true);
                tpd.enableMinutes(true);
                tpd.setTitle("TimePicker Title");
//                if (limitTimes.isChecked()) {
//                    tpd.setTimeInterval(2, 5, 10);
//                }
                tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Log.d("TimePicker", "Dialog was cancelled");
                    }
                });
                tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
            }
        });
//        // TODO: 10.11.2016  move  Recycler to on view created  A 5.0 known bug
//// Recycler


        recyclerview = (RecyclerView) view.findViewById(R.id.tmrrecyclerview);

       recyclerview.setLayoutManager(new LinearLayoutManager( view.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerview.setAdapter(new Tmr2lvlExpandableListAdapter(alTimersCategories.get( iActiveWorkSpaceindex).alTimersCategoryInWorkspace));


//        end recycler

        return view;

    }

//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        // Recycler
////        mLayoutManager = new LinearLayoutManager(getActivity());
////        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        final LinearLayoutManager  mLayoutManager = new LinearLayoutManager(getActivity());
//        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//
//        recyclerview = (RecyclerView) view.findViewById(R.id.tmrrecyclerview);
//        recyclerview.setItemAnimator(new DefaultItemAnimator());
//     //   recyclerview.setLayoutManager(mLayoutManager);
//        recyclerview.setLayoutManager(new LinearLayoutManager( recyclerview.getContext()));
//     //   final LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
////        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
////        recyclerview.setLayoutManager(layoutManager);
//
////        recyclerview.setLayoutManager(new LinearLayoutManager( view.getContext(), LinearLayoutManager.VERTICAL, false));
//        recyclerview.setAdapter(new TmrExpandableListAdapter(alTimersCategories.get( iActiveWorkSpaceindex).alTimersCategoryInWorkspace));
//
//    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String secondString = second < 10 ? "0" + second : "" + second;
        String time = "You picked the following time: " + hourString + "h" + minuteString + "m" + secondString + "s";
        timeTextView.setText(time);
    }


}
