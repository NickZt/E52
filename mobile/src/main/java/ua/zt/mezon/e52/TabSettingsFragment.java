package ua.zt.mezon.e52;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by MezM on 03.11.2016.
 */

public class TabSettingsFragment extends Fragment {
    private AllData allData = AllData.getInstance();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tabset1, container, false);

        view.findViewById(R.id.btn_play_again).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We normally won't show the welcome slider again in real app
                // but this is for testing
               /// PrefManager prefManager = new PrefManager(v.getContext().getApplicationContext());

                // make first time launch TRUE
                allData.setFirstTimeLaunch(true);

                startActivity(new Intent(v.getContext(), WelcomeActivity.class));
                //  finish();
            }
        });




        return view;
    }

}