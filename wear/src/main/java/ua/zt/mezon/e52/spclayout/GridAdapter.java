package ua.zt.mezon.e52.spclayout;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ua.zt.mezon.e52.R;

/**
 * Created by MezM on 24.11.2016.
 */
public class GridAdapter extends FragmentGridPagerAdapter {

    private static final int TRANSITION_DURATION_MILLIS = 100;
    private final Context mContext;
    private List<Row> mRows;
    private ColorDrawable mDefaultBg;
    private ColorDrawable mClearBg;

    public GridAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;

        mRows = new ArrayList<>();

        mRows.add(new Row(
                new FragmentListTimersTime(),
                new FragmentListTimersCategory()));
        mDefaultBg = new ColorDrawable(ctx.getResources().getColor(R.color.dark_grey));
        mClearBg = new ColorDrawable(ctx.getResources().getColor(android.R.color.transparent));
    }

    LruCache<Integer, Drawable> mRowBackgrounds = new LruCache<Integer, Drawable>(3) {
        @Override
        protected Drawable create(final Integer row) {
//            int resid = R.drawable.preview_digital; //BG_IMAGES[row % BG_IMAGES.length];
            int resid =mContext.getResources().getColor(android.R.color.transparent);
//            new DrawableLoadingTask(mContext) {
//                @Override
//                protected void onPostExecute(Drawable result) {
//                    TransitionDrawable background = new TransitionDrawable(new Drawable[] {mDefaultBg, result });
//                    mRowBackgrounds.put(row, background);
//                    notifyRowBackgroundChanged(row);
//                    background.startTransition(TRANSITION_DURATION_MILLIS);
//                }
//            }.execute(resid);
            return mDefaultBg;
        }
    };

    LruCache<Point, Drawable> mPageBackgrounds = new LruCache<Point, Drawable>(3) {
        @Override
        protected Drawable create(final Point page) {
//            int resId = R.drawable.image0;
//            if (page.x == 1) {
//                if (page.y == 1) {
//                    resId = R.drawable.image4;
//                }
//                if (page.y == 2) {
//                    resId = R.drawable.image3;
//                }
//                drawableTask(page, resId);
//            } else if (page.x == 2) {
//                if (page.y == 2) {
//                    resId = R.drawable.image5;
//                }
//                drawableTask(page, resId);
//            }
            return GridPagerAdapter.BACKGROUND_NONE;
        }
    };

    private void drawableTask(final Point page, int resId) {
        new DrawableLoadingTask(mContext) {
            @Override
            protected void onPostExecute(Drawable result) {
                TransitionDrawable background = new TransitionDrawable(new Drawable[] {mClearBg, result });
                mPageBackgrounds.put(page, background);
                notifyPageBackgroundChanged(page.y, page.x);
                background.startTransition(TRANSITION_DURATION_MILLIS);
            }
        }.execute(resId);
    }

    private Fragment cardFragment(int titleRes, int textRes) {
        Resources res = mContext.getResources();
        CardFragment fragment = CardFragment.create(res.getText(titleRes), res.getText(textRes));
        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginBottom(res.getDimensionPixelSize(R.dimen.diag_button_padding_bottom));
        return fragment;
    }

//    static final int[] BG_IMAGES = new int[] {R.drawable.image0, R.drawable.image1, R.drawable.image2,
//            R.drawable.image3, R.drawable.image4, R.drawable.image5 };

    /** A convenient container for a row of fragments. */
    private class Row {
        final List<Fragment> columns = new ArrayList<>();

        public Row(Fragment... fragments) {
            for (Fragment f : fragments) {
                add(f);
            }
        }

        public void add(Fragment f) {
            columns.add(f);
        }

        Fragment getColumn(int i) {
            return columns.get(i);
        }

        public int getColumnCount() {
            return columns.size();
        }
    }

    @Override
    public Fragment getFragment(int row, int col) {
        Row adapterRow = mRows.get(row);
        return adapterRow.getColumn(col);
    }

    @Override
    public Drawable getBackgroundForRow(final int row) {
        return mRowBackgrounds.get(row);
    }

    @Override
    public Drawable getBackgroundForPage(final int row, final int column) {
        return mPageBackgrounds.get(new Point(column, row));
    }

    @Override
    public int getRowCount() {
        return mRows.size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        return mRows.get(rowNum).getColumnCount();
    }

    class DrawableLoadingTask extends AsyncTask<Integer, Void, Drawable> {
        private static final String TAG = "Loader";
        private Context context;

        DrawableLoadingTask(Context context) {
            this.context = context;
        }

        @Override
        protected Drawable doInBackground(Integer... params) {
            Log.d(TAG, "Loading asset 0x" + Integer.toHexString(params[0]));
            return context.getResources().getDrawable(params[0]);
        }
    }
}