package ua.zt.mezon.e52.spclayout;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ua.zt.mezon.e52.R;

/**
 * Created by MezM on 24.11.2016.
 */
public class FragmentListTimersCategory extends Fragment implements WearableListView.ClickListener {
    public static final int NUMBER_OF_TIMES = 10;
    public static final String TAG = "SetTimerActivity";

    private ListViewItem[] mTimeOptions = new ListViewItem[NUMBER_OF_TIMES];
    private WearableListView mWearableListView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view=inflater.inflate(R.layout.list1_fragment, container, false);
        for (int i = 0; i < NUMBER_OF_TIMES; i++) {
            mTimeOptions[i] = new ListViewItem(
                    "next item " + Integer.toString(i),
                    (i + 1) * 60 * 1000);
        }

        mWearableListView = (WearableListView) view.findViewById(R.id.listView1);
        mWearableListView.setAdapter(new TimerWearableListViewAdapter(FragmentListTimersCategory.this.getActivity().getApplicationContext()));
        mWearableListView.setClickListener(this);





        return view;
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        long duration = mTimeOptions[viewHolder.getPosition()].duration;
//        setupTimer(duration);
     //   Toast.makeText(getActivity().getApplicationContext(), String.format("You selected item #%s", viewHolder.getPosition()), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity().getApplicationContext(), ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, String.format("You set active item #%s", viewHolder.getPosition()));
        startActivity(intent);
       // getActivity().finish();

    }

    @Override
    public void onTopEmptyRegionClick() {
    }








    /** Model class for the listview. */
    private static class ListViewItem {

        // Duration in milliseconds.
        long duration;
        // Label to display.
        private String label;

        public ListViewItem(String label, long duration) {
            this.label = label;
            this.duration = duration;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final class TimerWearableListViewAdapter extends WearableListView.Adapter {
        private final Context mContext;
        private final LayoutInflater mInflater;

        private TimerWearableListViewAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WearableListView.ViewHolder(
                    mInflater.inflate(R.layout.timer_list_item, null));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            TextView view = (TextView) holder.itemView.findViewById(R.id.time_text);
            view.setText(mTimeOptions[position].label);
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return NUMBER_OF_TIMES;
        }
    }






















}