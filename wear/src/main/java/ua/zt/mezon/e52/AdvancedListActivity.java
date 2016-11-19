package ua.zt.mezon.e52;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static ua.zt.mezon.e52.R.id.image;

public class AdvancedListActivity extends Activity implements WearableListView.ClickListener  {

    private WearableListView mListView;
    private MyListAdapter mAdapter;

    private float mDefaultCircleRadius;
    private float mSelectedCircleRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        mDefaultCircleRadius = getResources().getDimension(R.dimen.default_settings_circle_radius);
        mSelectedCircleRadius = getResources().getDimension(R.dimen.selected_settings_circle_radius);
        mAdapter = new MyListAdapter();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mListView = (WearableListView) stub.findViewById(R.id.listView1);
                mListView.setAdapter(mAdapter);
                mListView.setClickListener(AdvancedListActivity.this);
            }
        });
    }

    private static ArrayList<Integer> listItems;
    static {
        listItems = new ArrayList<Integer>();
        listItems.add(android.R.drawable.presence_offline);
        listItems.add(android.R.drawable.sym_action_call);
        listItems.add(android.R.drawable.sym_action_email);
        listItems.add(android.R.drawable.ic_partial_secure);
        listItems.add(android.R.drawable.ic_delete);
        listItems.add(android.R.drawable.stat_sys_download_done);
        listItems.add(android.R.drawable.ic_menu_edit);
        listItems.add(android.R.drawable.ic_menu_mylocation);
        listItems.add(android.R.drawable.ic_dialog_email);
        listItems.add(android.R.drawable.ic_menu_add);
        listItems.add(android.R.drawable.ic_popup_reminder);
        listItems.add(android.R.drawable.presence_video_busy);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Toast.makeText(this, String.format("You selected item #%s", viewHolder.getPosition()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTopEmptyRegionClick() {
        Toast.makeText(this, "You tapped Top empty area", Toast.LENGTH_SHORT).show();
    }

    public class MyListAdapter extends WearableListView.Adapter {

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new WearableListView.ViewHolder(new MyItemView(AdvancedListActivity.this));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int i) {
            MyItemView itemView = (MyItemView) viewHolder.itemView;
//            itemView. txtView
//            TextView txtView = (TextView) itemView.findViewById(R.id.text);
            itemView.txtView.setText(String.format("Item %d", i));

            Integer resourceId = listItems.get(i);
//            CircledImageView imgView = (CircledImageView) itemView.findViewById(image);
            itemView.imgView.setImageResource(resourceId);
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }
    }

    public final class MyItemView extends FrameLayout implements WearableListView.OnCenterProximityListener {
        /** The duration of the expand/shrink animation. */
        private static final int ANIMATION_DURATION_MS = 750;
        /** The ratio for the size of a circle in shrink state. */
        private static final float SHRINK_CIRCLE_RATIO = .75f;

        private static final float SHRINK_LABEL_ALPHA = .5f;
        private static final float EXPAND_LABEL_ALPHA = 1.5f;

        final CircledImageView imgView;
        final TextView txtView;
        private  int mFadedCircleColor;
        private  int mChosenCircleColor;
        private final float mExpandCircleRadius;
        private final float mShrinkCircleRadius;

        private final ObjectAnimator mExpandCircleAnimator;
        private final ObjectAnimator mExpandLabelAnimator;
        private final AnimatorSet mExpandAnimator;

        private final ObjectAnimator mShrinkCircleAnimator;
        private final ObjectAnimator mShrinkLabelAnimator;
        private final AnimatorSet mShrinkAnimator;

        public MyItemView(Context context) {
            super(context);
            View.inflate(context, R.layout.row_advanced_item_layout, this);
            imgView = (CircledImageView) findViewById(R.id.image);
            txtView = (TextView) findViewById(R.id.name);
            mFadedCircleColor = getResources().getColor(android.R.color.darker_gray);
            mChosenCircleColor = getResources().getColor(android.R.color.holo_blue_dark);

            mExpandCircleRadius = imgView.getCircleRadius();
            mShrinkCircleRadius = mExpandCircleRadius * SHRINK_CIRCLE_RATIO;

            mShrinkCircleAnimator = ObjectAnimator.ofFloat(imgView, "circleRadius",
                    mExpandCircleRadius, mShrinkCircleRadius);
            mShrinkLabelAnimator = ObjectAnimator.ofFloat(txtView, "alpha",
                    EXPAND_LABEL_ALPHA, SHRINK_LABEL_ALPHA);
            mShrinkAnimator = new AnimatorSet().setDuration(ANIMATION_DURATION_MS);
            mShrinkAnimator.playTogether(mShrinkCircleAnimator, mShrinkLabelAnimator);

            mExpandCircleAnimator = ObjectAnimator.ofFloat(imgView, "circleRadius",
                    mShrinkCircleRadius, mExpandCircleRadius);
            mExpandLabelAnimator = ObjectAnimator.ofFloat(txtView, "alpha",
                    SHRINK_LABEL_ALPHA, EXPAND_LABEL_ALPHA);
            mExpandAnimator = new AnimatorSet().setDuration(ANIMATION_DURATION_MS);
            mExpandAnimator.playTogether(mExpandCircleAnimator, mExpandLabelAnimator);








            imgView.setCircleRadius(mExpandCircleRadius);
            imgView.setCircleRadiusPressed(mShrinkCircleRadius);
        }



        @Override
        public void onCenterPosition(boolean animate) {

            //Animation example to be ran when the view becomes the centered one

            imgView.setCircleColor(mChosenCircleColor);
            // imgView.setCircleRadius(12.2f);

            if (animate) {
                mShrinkAnimator.cancel();
                if (!mExpandAnimator.isRunning()) {
                    mExpandCircleAnimator.setFloatValues(imgView.getCircleRadius(), mExpandCircleRadius);
                    mExpandLabelAnimator.setFloatValues(txtView.getAlpha(), EXPAND_LABEL_ALPHA);
                    mExpandAnimator.start();
                }
            } else {
                mExpandAnimator.cancel();
                imgView.setCircleRadius(mExpandCircleRadius);
                txtView.setAlpha(EXPAND_LABEL_ALPHA);
            }
            imgView.animate().scaleX(1f).scaleY(1f).alpha(1);
            txtView.animate().scaleX(1.4f).scaleY(1.4f).alpha(1);
            imgView.setAlpha(1f);
            txtView.setAlpha(1f);
        }

        @Override
        public void onNonCenterPosition(boolean animate) {

            //Animation example to be ran when the view is not the centered one anymore


            imgView.setCircleColor(mFadedCircleColor);
            if (animate) {
                mExpandAnimator.cancel();
                if (!mShrinkAnimator.isRunning()) {
                    mShrinkCircleAnimator.setFloatValues(imgView.getCircleRadius(), mShrinkCircleRadius);
                    mShrinkLabelAnimator.setFloatValues(txtView.getAlpha(), SHRINK_LABEL_ALPHA);
                    mShrinkAnimator.start();
                }
            } else {
                mShrinkAnimator.cancel();
                imgView.setCircleRadius(mShrinkCircleRadius);
                txtView.setAlpha(SHRINK_LABEL_ALPHA);
            }
            imgView.animate().scaleX(0.7f).scaleY(0.7f).alpha(0.5f);
            txtView.animate().scaleX(0.7f).scaleY(0.7f).alpha(0.5f);
        }
    }


}
