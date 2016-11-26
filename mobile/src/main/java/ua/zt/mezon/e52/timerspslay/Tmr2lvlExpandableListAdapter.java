package ua.zt.mezon.e52.timerspslay;

/**
 * Created by MezM on 09.11.2016.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ua.zt.mezon.e52.AllData;
import ua.zt.mezon.e52.R;
import ua.zt.mezon.e52.misc.TimersCategoryInWorkspace;
import ua.zt.mezon.e52.misc.TimersTime;

import static android.support.v7.widget.PopupMenu.OnMenuItemClickListener;

//
public class Tmr2lvlExpandableListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final boolean SHOW_DEBUG = true;
    public static final int HEADER = 0;
    public static final int CHILD = 1;
    public static ArrayList <TimersCategoryInWorkspace> indata;
    public int currWorkSpace;
    private static List<Item> data = new ArrayList<>();
    Context context ;
    private AllData allData = AllData.getInstance();
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilderHeaderActive,mDrawableBuilderHeaderPassive;
    private TextDrawable.IBuilder mDrawableBuilderChildActive,mDrawableBuilderChildPassive;

    public int toPx(int dp) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
    public Tmr2lvlExpandableListAdapter(ArrayList <TimersCategoryInWorkspace> indata,int currWorkSpace) {
        // List<Item> data
        this.indata = indata;
        this.currWorkSpace= currWorkSpace;
        refreshInternalData(indata);


    }

    public void refreshInternalData(ArrayList<TimersCategoryInWorkspace> indata) {
        if ( this.data != null) {
            data.clear();
        }
        for (TimersCategoryInWorkspace z  : indata)
        {

            if (z.active){
                this.data.add(new Item( HEADER, z.name,z.sTmrCategorySymbol,z.active,z.id));
                // Item( HEADER, z.name) public Item(int type, String text, String sTmrCategorySymbol, boolean active, int id)
                for (TimersTime tmp_child:
                        z.timersTimes) {
                    this.data.add(new Item( CHILD, tmp_child.name, "",tmp_child.active,tmp_child.id,tmp_child.time,tmp_child.repeats,tmp_child.nextid,tmp_child.nextDo,tmp_child.maxrepeats ));
                    //  public Item(int type, String text, String sTmrCategorySymbol, boolean active,  int id, long time,  int repeats, int nextid, int nextDo, int maxrepeats ) {
                }
            } else {

                Item Tmp_add_inner_places = new Item( HEADER, z.name,z.sTmrCategorySymbol,z.active,z.id);
                Tmp_add_inner_places.invisibleChildren = new ArrayList<>();

                for (TimersTime tmp_child:
                        z.timersTimes) {
//                        this.data.add(new  Item( CHILD, tmp_child.name));
                    Tmp_add_inner_places.invisibleChildren.add(new Item( CHILD, tmp_child.name, "",tmp_child.active,tmp_child.id,tmp_child.time,tmp_child.repeats,tmp_child.nextid,tmp_child.nextDo,tmp_child.maxrepeats ));
                }
                this.data.add(Tmp_add_inner_places);

            }
        }
//        Tmr2lvlExpandableListAdapter. da
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = null;
        Context context = parent.getContext();
        this.context=context;
        //   mProvider = new DrawableProvider(context);
        mDrawableBuilderHeaderActive = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .useFont(allData.Symbol_TYPEFACE)
                .fontSize(toPx(60))
                //  .bold()
                .endConfig()
                .roundRect(10);

        mDrawableBuilderHeaderPassive = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .useFont(allData.Symbol_TYPEFACE)
                .fontSize(toPx(60))
                //  .bold()
                .endConfig()
                .round();
        mDrawableBuilderChildActive = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .toUpperCase()
                .endConfig()
                .roundRect(10);
        mDrawableBuilderChildPassive = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .toUpperCase()
                .endConfig()
                .round();
        float dp = context.getResources().getDisplayMetrics().density;
        int subItemPaddingLeft = (int) (18 * dp);
        int subItemPaddingTopAndBottom = (int) (5 * dp);
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (type) {
            case HEADER:
                view = inflater.inflate(R.layout.tmr_list_header, parent, false);
                ListHeaderViewHolder header = new ListHeaderViewHolder(view);
                return header;
            case CHILD:

                view = inflater.inflate(R.layout.tmr_list_child, parent, false);
                ListChildViewHolder header1 = new ListChildViewHolder(view);
                return header1;

        }
        return null;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Item item = data.get(position);
        switch (item.type) {
            case HEADER: {
                final ListHeaderViewHolder itemController = (ListHeaderViewHolder) holder;
                itemController.refferalItem = item;
                itemController.mCurrItem = item;
                itemController.mListPos = position;
                itemController.header_title.setText(item.text);
                TextDrawable drawable;
                if (data.get(position).active) {
                    itemController.header_title.setBackgroundColor(Color.GREEN);
                    drawable = mDrawableBuilderHeaderActive.build(item.sTmrCategorySymbol, mColorGenerator.getColor(item.text));
                    itemController.imageView.setImageDrawable(drawable);
                } else {
                    itemController.header_title.setBackgroundColor(Color.TRANSPARENT);
                    drawable = mDrawableBuilderHeaderPassive.build(item.sTmrCategorySymbol, mColorGenerator.getColor(item.text));
                    // mDrawableBuilderHeaderPassive  mDrawableBuilderHeaderActive
                    itemController.imageView.setImageDrawable(drawable);
                }
                if (itemController.menu.getMenu() != null) {
                    itemController.menu.getMenu().clear();
                }

                MenuItem myActionItem=itemController.menu.getMenu().add(0, 0, 0, context.getString(R.string.menu_return) + item.text);
                myActionItem.setIcon(android.R.drawable.ic_menu_myplaces);


                Bitmap BitmapOrg = Bitmap.createBitmap(itemController.imageView.getLayoutParams().width,
                        itemController.imageView.getLayoutParams().height, Bitmap.Config.RGB_565
                        );
                Canvas canvas = new Canvas(BitmapOrg);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                myActionItem.setIcon(resizeImage(BitmapOrg, myActionItem.getIcon().getIntrinsicHeight(),myActionItem.getIcon().getIntrinsicHeight()));


                itemController.pListHeaderPopupMenuFill();

                if (item.invisibleChildren == null) {
                    itemController.btn_expand_toggle.setImageResource(R.drawable.ic_zoom_out_black_24dp);
                    // itemController.header_title.setBackgroundColor( 0xffa1887f);

                } else {
                    itemController.btn_expand_toggle.setImageResource(R.drawable.ic_zoom_in_black_24dp);
                    // itemController.header_title.setBackgroundColor( 0xffe57373);
                }
                itemController.btn_expand_toggle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (item.invisibleChildren == null) {
                            item.invisibleChildren = new ArrayList<Item>();
                            int count = 0;
                            int pos = data.indexOf(itemController.refferalItem);
                            while (data.size() > pos + 1 && data.get(pos + 1).type == CHILD) {
                                item.invisibleChildren.add(data.remove(pos + 1));
                                count++;
                            }
                            notifyItemRangeRemoved(pos + 1, count);
                            itemController.btn_expand_toggle.setImageResource(R.drawable.ic_zoom_in_black_24dp);
                        } else {
                            int pos = data.indexOf(itemController.refferalItem);
                            int index = pos + 1;
                            for (Item i : item.invisibleChildren) {
                                data.add(index, i);
                                index++;
                            }
                            notifyItemRangeInserted(pos + 1, index - pos - 1);
                            itemController.btn_expand_toggle.setImageResource(R.drawable.ic_zoom_out_black_24dp);
                            item.invisibleChildren = null;
                        }
                    }
                });
            }
                break;
            case CHILD:
                final ListChildViewHolder itemChController = (ListChildViewHolder) holder;
                // itemChController;
                itemChController.mCurrItem=item;
                itemChController.mListPos=position;
                itemChController.child_title.setText(data.get(position).text);
                if (data.get(position).active) {
                    itemChController.child_title.setBackgroundColor(Color.GREEN);
                    TextDrawable drawable = mDrawableBuilderChildActive.build(String.valueOf(item.text.charAt(0)), mColorGenerator.getRandomColor());
                    itemChController.imageView.setImageDrawable(drawable);
                }
                else {
                    itemChController.child_title.setBackgroundColor(Color.TRANSPARENT);
                    TextDrawable drawable = mDrawableBuilderChildPassive.build(String.valueOf(item.text.charAt(0)),mColorGenerator.getColor(item.text));
                    /// mDrawableBuilderChildActive
                    itemChController.imageView.setImageDrawable(drawable);
                }
                itemChController.timedescription.setText(TimeInMilisToStr(item.time));
                //Item( CHILD, tmp_child.name, "",tmp_child.active,tmp_child.id,tmp_child.time,tmp_child.repeats,tmp_child.nextid,tmp_child.nextDo,tmp_child.maxrepeats )
//                holder.
//                itemTextView.setText(data.get(position).text);
//                itemController.header_title.setText(data.get(position).text);
                break;
        }
        animate(holder);
    }

    private Drawable resizeImage(Bitmap BitmapOrg, int w, int h) {



            int width = BitmapOrg.getWidth();
            int height = BitmapOrg.getHeight();
            int newWidth = w;
            int newHeight = h;
            // calculate the scale
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // create a matrix for the manipulation
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0,width, height, matrix, true);
            return new BitmapDrawable(resizedBitmap);


    }

    private String TimeInMilisToStr(long time) {
        long second = (time / 1000) % 60;
        long minute = (time / (1000 * 60)) % 60;
        long hour = (time / (1000 * 60 * 60)) % 24;
        return  String.format("%02d:%02d:%02d", hour, minute, second);
    }

    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.anticipate_overshoot_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
    }
    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }

//    @Override
//    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
//        super.registerAdapterDataObserver(observer);
//    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private class ListHeaderViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, View.OnClickListener,
            MenuItem.OnMenuItemClickListener {
        private static final int CM_DELETE = 1;
        private static final int CM_EDIT = 2;
        private static final int CM_SET_ACTIVE = 3 ;
        private static final int CM_ADD = 4 ;

        public TextView header_title;
        public ImageView btn_expand_toggle;
        public Item refferalItem;
       public CardView cv;
       public TextView description;
       public ImageView imageView;
        public Item mCurrItem;
        public int mListPos;
       public PopupMenu menu;
        public ListHeaderViewHolder(View itemView) {
            super(itemView);
            header_title = (TextView) itemView.findViewById(R.id.header_title);
            btn_expand_toggle = (ImageView) itemView.findViewById(R.id.btn_expand_toggle);
            cv = (CardView) itemView.findViewById(R.id.cardView);
            description = (TextView) itemView.findViewById(R.id.description);
            imageView = (ImageView) itemView.findViewById(R.id.himageView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
            menu = new PopupMenu(itemView.getContext(), itemView);
////            // TODO: 15.11.2016 link header to headers name & add his name

//            menu.getMenu().setForceShowIcon(true);

            try {
                Field[] fields = menu.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(menu);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper
                                .getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod(
                                "setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId())  {
                        case CM_DELETE: {
                            if (SHOW_DEBUG)   Toast.makeText(itemView.getContext(), "Выбран пункт DELETE", Toast.LENGTH_LONG)
                                    .show();
//                    if (SHOW_DEBUG)   Toast.makeText( getContext(), "Delete POI>"+mPOIItem.toString(), Toast.LENGTH_SHORT).show();
//                    try {
//                        dbWclass.delPOIs(mPOIItem);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    poi.remove(mPOIItem);
//                    dbWclass.load_INIT_JSON();
//                    dbWclass.For_Detail_DataAdapterInternalRescan();
//                    poiRescan();
//                    adapter.notifyDataSetChanged();
//                    // adapter.notifyItemRemoved(mPOIpos);// DataAdapter.notifyItemRemoved(mPOIpos);
//                    //adapter.notifyItemRemoved(mPOIItem);

                            break;
                        }
                        case CM_EDIT: {
//                    if (SHOW_DEBUG)   Toast.makeText(getContext(), "Выбран пункт CM_EDIT", Toast.LENGTH_LONG)
//                            .show();
                            if (SHOW_DEBUG) {
                                //  Toast.makeText(getContext(), "CM_EDIT POI>" + mCurrItem.toString(), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case CM_SET_ACTIVE: {
                            cm_SetActiveItemInHeader();

                        }
                            break;

                        case CM_ADD: {


                        }
                            break;

                    }


                    return false;
                }
            } );

            // Menu pMenu;

            // menu.getMenu();

            imageView.setOnClickListener(v -> {
                menu.show();

            });

        }

        public void pListHeaderPopupMenuFill() {
            menu.getMenu().add(0, CM_SET_ACTIVE, 0,R.string.menu_active)
                    .setIcon(android.R.drawable.btn_star_big_on)
                    .setOnMenuItemClickListener(this);

            MenuItem myActionItem=menu.getMenu().add(0, CM_EDIT, 0, R.string.menu_edit);
            myActionItem.setEnabled(false);
            myActionItem.setIcon(android.R.drawable.ic_menu_myplaces);
            myActionItem.setOnMenuItemClickListener(this);

            menu.getMenu().add(0, CM_DELETE, 0, R.string.menu_Delete)
                    .setIcon(R.drawable.ic_andr_cross)
                    .setEnabled(false)
                    .setOnMenuItemClickListener(this)
                    .setEnabled(indata.get(mCurrItem.id).timersTimes.isEmpty());

            menu.getMenu().add(0, CM_ADD, 0, R.string.menu_add)
                    .setIcon(android.R.drawable.sym_contact_card)
                    .setEnabled(false)
                    .setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId())  {
                case CM_DELETE: {


                    break;
                }
                case CM_EDIT: {
//                    if (SHOW_DEBUG)   Toast.makeText(getContext(), "Выбран пункт CM_EDIT", Toast.LENGTH_LONG)
//                            .show();
                    if (SHOW_DEBUG) {
                        //  Toast.makeText(getContext(), "CM_EDIT POI>" + mCurrItem.toString(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case CM_SET_ACTIVE: {
                    cm_SetActiveItemInHeader();
                    break;
                }
                case CM_ADD: {
//                    if (SHOW_DEBUG)   Toast.makeText(getContext(), "Выбран пункт ADD POI", Toast.LENGTH_LONG)
//                            .show();
//                    if (SHOW_DEBUG)   Toast.makeText( getContext(), "ADDPOI>"+mPOIItem.toString(), Toast.LENGTH_SHORT).show();
//                    Cicer_poi tmpItem = new Cicer_poi();
//                    tmpItem.setCat(mPOIItem.getCat());
//                    tmpItem.setName("Zhukova "+"sometext proba ");
//                    tmpItem.setLang(50.280992525335744);
//                    tmpItem.setLongit(28.619921183714403);
//                    tmpItem.setWhs(221);
//                    tmpItem.setAlt( 2123);
//                    tmpItem.setBearing(221);
//                    tmpItem.setSpeed(22233);
//                    tmpItem.setSale_id(R.mipmap.ic_launcher);
//                    tmpItem.setiddecor(R.mipmap.ic_launcher);
//
////                       try {
////                           tmpItem=   dbWclass.addPOIs( tmpItem);
////                       } catch (IOException e) {
////                           e.printStackTrace();
////                       }
////
////                       poi.add( tmpItem);
//                    //   adapter.notifyItemInserted(mPOIpos);
//                    // adapter.notifyItemRemoved(mPOIItem);
//                    dbWclass.load_INIT_JSON();
//                    dbWclass.For_Detail_DataAdapterInternalRescan();
//
//                    poiRescan();
//
//                    //   DataAdapter.notifyDataSetChanged();
//                    //  adapter.notifyDataSetChanged();// DataAdapter.notifyItemRemoved(mPOIpos);
//                    //adapter.notifyItemRemoved(mPOIItem);



                    break;
                }
            }





            return true;
        }

        private void cm_SetActiveItemInHeader() {
            for (TimersCategoryInWorkspace tmp:
                 indata) {
                tmp.active=false;
            }
            indata.get(mCurrItem.id).active=true;
            refreshInternalData(indata);
        }
        private void cm_DeleteItemInHeader() { //if not empty no del
            if (indata.get(mCurrItem.id).timersTimes.isEmpty() ) {
                indata.remove(mCurrItem.id);
                refreshInternalData(indata);
            }



        }

        @Override
        public void onClick(View view) {

        }




        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            menu.setHeaderTitle("Timers Header Menu ");
//            // TODO: 15.11.2016 link header to headers name & add timspace name
            menu.setHeaderIcon(android.R.drawable.ic_menu_compass);

            menu.add(0, CM_SET_ACTIVE, 0,R.string.menu_active)
                    .setIcon(android.R.drawable.btn_star_big_on)
                    .setOnMenuItemClickListener(this);

            MenuItem myActionItem=menu.add(0, CM_EDIT, 0, R.string.menu_edit);
            myActionItem.setIcon(android.R.drawable.ic_menu_myplaces);
            myActionItem.setOnMenuItemClickListener(this);

            menu.add(0, CM_DELETE, 0, R.string.menu_Delete)
                    .setIcon(R.drawable.ic_andr_cross)
                    .setOnMenuItemClickListener(this)
                    .setEnabled(indata.get(mCurrItem.id).timersTimes.isEmpty());

            menu.add(0, CM_ADD, 0, R.string.menu_add)
                    .setIcon(android.R.drawable.sym_contact_card)
                    .setOnMenuItemClickListener(this);
        }
    }

    /**
     * ListChildViewHolder
     */
    private static class ListChildViewHolder extends RecyclerView.ViewHolder  implements View.OnCreateContextMenuListener, View.OnClickListener,
            MenuItem.OnMenuItemClickListener {
        public TextView child_title;
        CardView cv;
        TextView timedescription;
        ImageView imageView;
        public Item mCurrItem;
        public int mListPos;
        public ListChildViewHolder(View itemView) {
            super(itemView);
            child_title = (TextView) itemView.findViewById(R.id.child_title);
            cv = (CardView) itemView.findViewById(R.id.child_cardView);
            timedescription = (TextView) itemView.findViewById(R.id.timedescription);
            imageView = (ImageView) itemView.findViewById(R.id.cimageView);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            return false;
        }

        @Override
        public void onClick(View v) {
//            if (SHOW_DEBUG)   Toast.makeText( v.getContext(), "Click in POI pos>"+Integer.toString(mPOIpos), Toast.LENGTH_SHORT).show();
//            if (poiListActivity.mTwoPane) {  // true - fragment POI & CAT in one activity
//
//            } else { // false - fragment POI in separate poi Detail activity
//               // poiDetailActivity.fabMain.startAnimation(poiDetailActivity.rotate_forwardD);
//            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

        }
    }
    public static class Item {
        public int type;
        public String text; //name;
        public List<Item> invisibleChildren;

        public int id;
        public long time; //TimeUnit. ##### .toMillis(1)
        public int repeats;
        public int maxrepeats;
        public int nextDo; //type 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
        public int nextid;

        public String sTmrCategorySymbol; // Character.toString((char) 731); // Таймер помидоро
        public boolean active;



        public Item() {
        }


        public Item(int type, String text, String sTmrCategorySymbol, boolean active, int id) {
            this.type = type;
            this.text = text;
            this.sTmrCategorySymbol = sTmrCategorySymbol;
            this.active = active;
            this.id = id;
        }

        public Item(int type, String text, String sTmrCategorySymbol, boolean active,  int id, long time,  int repeats, int nextid, int nextDo, int maxrepeats ) {
            this.type = type;
            this.text = text;
            this.time = time;
            this.sTmrCategorySymbol = sTmrCategorySymbol;
            this.repeats = repeats;
            this.nextid = nextid;
            this.nextDo = nextDo;
            this.maxrepeats = maxrepeats;
            this.id = id;
            this.active = active;
        }
    }
}



