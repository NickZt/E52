package ua.zt.mezon.e52;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ua.zt.mezon.e52.servsubtps.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {
   /// timePicker.setIs24HourView(true);
    private AllData allData = AllData.getInstance();
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (allData._context == null) {
            allData.iniPrefManager(this);
        };
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        

    }


    public void setupViewPager(ViewPager upViewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new TabSettingsFragment(), "Settings");
        adapter.addFragment(new TabSetAlarmFragment(), "Alarm");
        adapter.addFragment(new TabSetTimerFragment(), "Timers");
        upViewPager.setAdapter(adapter);

    }
}
