package ua.zt.mezon.e52.spclayout;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ua.zt.mezon.e52.AllData;
import ua.zt.mezon.e52.R;
import ua.zt.mezon.e52.core.MySpcIntentService;
import ua.zt.mezon.e52.misc.TimerWorkspace;
import ua.zt.mezon.e52.misc.TimersCategoryInWorkspace;
import ua.zt.mezon.e52.misc.TimersTime;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

/**
 * Created by MezM on 23.11.2016.
 */
public class FragmentListTimersTime extends Fragment implements WearableListView.ClickListener{
    private static final String TAG = "FragmentListTimersTime";
    private AllData allData = AllData.getInstance();
    private ArrayList<TimerWorkspace> alTimersCategories;
    private  int SHOWLEVEL=2; // level to show & select 0-workspace 1 CategoryInWorkspace 2 timerstime

    private int[] ialTimersCategoriesActiveLvls = new int[3];
    private WearableListView mListView;
    private MyListAdapter mAdapter;

    private float mDefaultCircleRadius;
    private float mSelectedCircleRadius;




    private String string_TIMER; //symbol

    public void setUpInternalTimers() {

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
                               // lTimerSetTimeMls = tmp2.time;
                            }
                        }

                    }
                }
            }
        }
//
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (this.getArguments() != null) {


            if (this.getArguments().containsKey("messageSHOWLEVEL")) {
                SHOWLEVEL = this.getArguments().getInt("messageSHOWLEVEL");
                Log.d(TAG, "Создан Фрагмент с SHOWLEVEL ->" + Integer.toString(SHOWLEVEL));
            }
        }
        final View view=inflater.inflate(R.layout.list_fragment, container, false);
        alTimersCategories = allData.getAlTimersCategories();

        setUpInternalTimers();

        mDefaultCircleRadius = getResources().getDimension(R.dimen.default_settings_circle_radius);
        mSelectedCircleRadius = getResources().getDimension(R.dimen.selected_settings_circle_radius);
        mAdapter = new MyListAdapter();



        mListView = (WearableListView) view.findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);
        mListView.setClickListener(FragmentListTimersTime.this);

        return view;




    }
    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        MyItemView itemView = (MyItemView) viewHolder.itemView;
        if (itemView.isActive){

        } else {
            switch (SHOWLEVEL) {
                case 0:{
                    alTimersCategories.get(itemView.mindx).active=true;
                    alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).active=false;

                }
                break;
                case 1:{
                    alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(itemView.mindx).active=true;
                    alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1]).active=false;


                    // return alTimersCategories.get(ialTimersCategoriesActiveLvls[1]).alTimersCategoryInWorkspace.size();
                }
                break;
                case 2:{
//                    for (TimersTime tmp2 :
//                            alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1]).timersTimes) {
//                        tmp2.active=false;
//                    }


                    alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1]).timersTimes.get(itemView.mindx).active=true;
                    alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1]).timersTimes.get(ialTimersCategoriesActiveLvls[2]).active=false;




                    // return alTimersCategories.get(ialTimersCategoriesActiveLvls[1]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[2]).timersTimes.size();
                }
                break;
            }




            //   Toast.makeText(getActivity().getApplicationContext(), String.format("You selected item #%s", viewHolder.getPosition()), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity().getApplicationContext(), ConfirmationActivity.class);
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,getString(R.string.TimerActive)+itemView.mname );//String.format("You set active item #%s", viewHolder.getPosition())
            startActivity(intent);
            //Write data changes
            allData.setAlTimersCategories(alTimersCategories);
            //changeTimerSetaction
            Intent changeTimerIntent =
                    new Intent(getActivity().getApplicationContext(), MySpcIntentService.class);//MyIntentServiceMySpcIntentService
            changeTimerIntent.setAction("changeTimerSetaction");
            changeTimerIntent.putExtra("extra", "changeTimerSetE52StateTimer");
            PendingIntent pendingIntentchangeTimerSet = PendingIntent.getService(
                    getActivity().getApplicationContext(),
                    999,
                    changeTimerIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pendingIntentchangeTimerSet.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

            getActivity().finish();
        }
    }

    @Override
    public void onTopEmptyRegionClick() {
        Toast.makeText(getActivity().getApplicationContext(), "You tapped Top empty area", Toast.LENGTH_SHORT).show();
    }

    public class MyListAdapter extends WearableListView.Adapter {

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new WearableListView.ViewHolder(new MyItemView(FragmentListTimersTime.this.getActivity().getApplicationContext()));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int i) {
            MyItemView itemView = (MyItemView) viewHolder.itemView;
//            itemView. txtView
//            TextView txtView = (TextView) itemView.findViewById(R.id.text);
            switch (SHOWLEVEL) {
                case 0:{
                    itemView.mindx=i;
                    itemView.mid=alTimersCategories.get(i).id;
                    itemView.mname=alTimersCategories.get(i).name;
                    itemView.msSymbol=String.valueOf(alTimersCategories.get(i).name.charAt(0));
                    itemView.bisSymbolSpc =false;
                    itemView.isActive=alTimersCategories.get(i).active;

                }
                break;
                case 1:{
                    itemView.mindx=i;
                    itemView.mid=alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(i).id;
                    itemView.mname=alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(i).name;
                    itemView.msSymbol=alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(i).sTmrCategorySymbol;
                    itemView.bisSymbolSpc =true;
                    itemView.isActive=alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(i).active;
                   // return alTimersCategories.get(ialTimersCategoriesActiveLvls[1]).alTimersCategoryInWorkspace.size();
                }
                break;
                case 2:{
                    itemView.mindx=i;
                    itemView.mid=alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1]).timersTimes.get(i).id;
                    itemView.mname=alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1]).timersTimes.get(i).name;
                    itemView.msSymbol=String.valueOf(alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1]).timersTimes.get(i).name.charAt(0));
                    itemView.bisSymbolSpc =false;
                    itemView.isActive=alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1]).timersTimes.get(i).active;

                   // return alTimersCategories.get(ialTimersCategoriesActiveLvls[1]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[2]).timersTimes.size();
                }
                break;
            }




            itemView.txtView.setText(itemView.mname);
if (itemView.isActive) {
    itemView.txtView.setBackgroundColor(Color.GREEN);
}
          //  Integer resourceId = listItems.get(i);
//            CircledImageView imgView = (CircledImageView) itemView.findViewById(image);
           // itemView.imgView.te;
        }

        @Override
        public int getItemCount() {
            switch (SHOWLEVEL) {
                case 0:{
                    return alTimersCategories.size();
                }

                case 1:{
                    return alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.size();
                }

                case 2:{
                    return alTimersCategories.get(ialTimersCategoriesActiveLvls[0]).alTimersCategoryInWorkspace.get(ialTimersCategoriesActiveLvls[1]).timersTimes.size();
                }

            }
            return 0;
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
        public   int mid;
        public   int mindx;
        public String mname;
        public  String msSymbol;
        public boolean bisSymbolSpc =false;
        public boolean isActive =false;
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