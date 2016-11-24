package ua.zt.mezon.e52.spclayout;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.WatchViewStub;

import ua.zt.mezon.e52.R;

public class TimerTypeSelectActivity extends Activity   {
    private GridOneRowViewPager pager;
    private DotsPageIndicator dotsPageIndicator;
    private GridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                pager = (GridOneRowViewPager) stub.findViewById(R.id.pager);
                dotsPageIndicator = (DotsPageIndicator) stub.findViewById(R.id.page_indicator);
                adapter = new GridAdapter(getBaseContext(), getFragmentManager());
                pager.setAdapter(adapter);

                pager.setBackground(GridPagerAdapter.BACKGROUND_NONE);

                pager.setAdapter(new GridAdapter(getBaseContext(), getFragmentManager()));
                dotsPageIndicator.setPager(pager);
            }
        });
    }

}
