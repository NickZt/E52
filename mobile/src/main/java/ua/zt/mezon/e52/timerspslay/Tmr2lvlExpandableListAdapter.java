package ua.zt.mezon.e52.timerspslay;

/**
 * Created by MezM on 09.11.2016.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ua.zt.mezon.e52.AllData;
import ua.zt.mezon.e52.R;
import ua.zt.mezon.e52.misc.TimersCategoryInWorkspace;
import ua.zt.mezon.e52.misc.TimersServiceUtils;
import ua.zt.mezon.e52.misc.TimersTime;
import ua.zt.mezon.e52.servsubtps.ColorGenerator;
import ua.zt.mezon.e52.servsubtps.TextDrawable;

import static android.support.v7.widget.PopupMenu.OnMenuItemClickListener;



//
public class Tmr2lvlExpandableListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final boolean SHOW_DEBUG = true;
    public static final int HEADER = 0;
    public static final int CHILD = 1;
    private static final int BITMAP_WIDTH = 20;
    private static final int BITMAP_HEIGHT = 20;
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
                this.data.add(new Item(indata.indexOf(z), HEADER, z.name,z.sTmrCategorySymbol,z.active,z.id));
                // Item( HEADER, z.name) public Item(int type, String text, String sTmrCategorySymbol, boolean active, int id)
                for (TimersTime tmp_child:
                        z.timersTimes) {
                    this.data.add(new Item(z.timersTimes.indexOf( tmp_child),indata.indexOf(z), CHILD, tmp_child.name, "",tmp_child.active,tmp_child.id,tmp_child.time,tmp_child.repeats,tmp_child.nextid,tmp_child.nextDo,tmp_child.maxrepeats ));
                    //  public Item(int type, String text, String sTmrCategorySymbol, boolean active,  int id, long time,  int repeats, int nextid, int nextDo, int maxrepeats ) {
                }
            } else {

                Item Tmp_add_inner_places = new Item(indata.indexOf(z), HEADER, z.name,z.sTmrCategorySymbol,z.active,z.id);
                Tmp_add_inner_places.invisibleChildren = new ArrayList<>();

                for (TimersTime tmp_child:
                        z.timersTimes) {
//                        this.data.add(new  Item( CHILD, tmp_child.name));
                    Tmp_add_inner_places.invisibleChildren.add(new Item( z.timersTimes.indexOf( tmp_child),indata.indexOf(z),CHILD, tmp_child.name, "",tmp_child.active,tmp_child.id,tmp_child.time,tmp_child.repeats,tmp_child.nextid,tmp_child.nextDo,tmp_child.maxrepeats ));
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
        iniDrwbBuild();
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

    void iniDrwbBuild() {
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
                   // itemController.header_title.setBackgroundColor(Color.GREEN);
                    itemController.cv.setCardBackgroundColor(Color.parseColor(context.getString(R.color.greenprimary)));//Color.GREEN0x8BC34A"#8BC34A"
                    drawable = mDrawableBuilderHeaderActive.build(item.sTmrCategorySymbol, mColorGenerator.getColor(item.text));
                    itemController.imageView.setImageDrawable(drawable);
                } else {
                    itemController.header_title.setBackgroundColor(Color.TRANSPARENT);
                    itemController.cv.setCardBackgroundColor(Color.WHITE);
                    drawable = mDrawableBuilderHeaderPassive.build(item.sTmrCategorySymbol, mColorGenerator.getColor(item.text));
                    // mDrawableBuilderHeaderPassive  mDrawableBuilderHeaderActive
                    itemController.imageView.setImageDrawable(drawable);
                }
                if (itemController.menu.getMenu() != null) {
                    itemController.menu.getMenu().clear();
                }

                MenuItem myActionItem=itemController.menu.getMenu().add(0, 0, 0, context.getString(R.string.menu_return) + item.text);
                myActionItem.setIcon(android.R.drawable.ic_menu_myplaces);


                Bitmap BitmapOrg = Bitmap.createBitmap((int) (itemController.imageView.getLayoutParams().width*1.2),
                        (int) (itemController.imageView.getLayoutParams().height*1.2), Bitmap.Config.RGB_565
                        );
                Canvas canvas = new Canvas(BitmapOrg);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                myActionItem.setIcon(resizeImage(BitmapOrg, (int) (itemController.imageView.getLayoutParams().width*1.2),
                        (int) (itemController.imageView.getLayoutParams().height*1.2)));


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
                TextDrawable drawable;
                itemChController.mCurrItem=item;
                itemChController.mListPos=position;
                itemChController.child_title.setText(data.get(position).text);
                if (data.get(position).active) {
                  //  itemChController.child_title.setBackgroundColor(Color.GREEN);
                    itemChController.cv.setCardBackgroundColor(Color.parseColor(context.getString(R.color.greenprimary_lvl)));//0x689F38
                    drawable = mDrawableBuilderChildActive.build(String.valueOf(item.text.charAt(0)), mColorGenerator.getRandomColor());
                    itemChController.imageView.setImageDrawable(drawable);
                }
                else {
                    itemChController.child_title.setBackgroundColor(Color.TRANSPARENT);
                    itemChController.cv.setCardBackgroundColor(Color.WHITE);//0x689F38
                     drawable = mDrawableBuilderChildPassive.build(String.valueOf(item.text.charAt(0)),mColorGenerator.getColor(item.text));
                    /// mDrawableBuilderChildActive
                    itemChController.imageView.setImageDrawable(drawable);
                }
                itemChController.timedescription.setText(TimeInMilisToStr(item.time));
                if (itemChController.menu.getMenu() != null) {
                    itemChController.menu.getMenu().clear();
                }

                MenuItem myActionItem=itemChController.menu.getMenu().add(0, 0, 0, context.getString(R.string.menu_return) + item.text);
                myActionItem.setIcon(android.R.drawable.ic_menu_myplaces);


                Bitmap BitmapOrg = Bitmap.createBitmap((int) (itemChController.imageView.getLayoutParams().width),//*1.2
                        (int) (itemChController.imageView.getLayoutParams().height), Bitmap.Config.RGB_565
                );
                Canvas canvas = new Canvas(BitmapOrg);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                myActionItem.setIcon(resizeImage(BitmapOrg, (int) (itemChController.imageView.getLayoutParams().width*1.2),
                        (int) (itemChController.imageView.getLayoutParams().height*1.2)));


                itemChController.pListChildPopupMenuFill();

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
                            cm_DeleteItemInHeader();
                            break;
                        }
                        case CM_EDIT: {
                            cm_EditItemInHeader();
                            break;
                        }
                        case CM_SET_ACTIVE: {
                            cm_SetActiveItemInHeader();

                        }
                            break;

                        case CM_ADD: {
                            cm_AddCopyItemInHeader();

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
            myActionItem.setEnabled(true);
            myActionItem.setIcon(android.R.drawable.ic_menu_myplaces);
            myActionItem.setOnMenuItemClickListener(this);

            menu.getMenu().add(0, CM_DELETE, 0, R.string.menu_Delete)
                    .setIcon(R.drawable.ic_andr_cross)
                    .setEnabled(true)
                    .setOnMenuItemClickListener(this)
                    .setEnabled(!indata.get(mCurrItem.idx).active);//indata.get(mCurrItem.id).timersTimes.isEmpty()

            menu.getMenu().add(0, CM_ADD, 0, R.string.menu_add)
                    .setIcon(android.R.drawable.sym_contact_card)
                    .setEnabled(true)
                    .setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId())  {
                case CM_DELETE: {
                    cm_DeleteItemInHeader();

                    break;
                }
                case CM_EDIT: {
                   cm_EditItemInHeader();
                    break;
                }
                case CM_SET_ACTIVE: {
                    cm_SetActiveItemInHeader();
                    break;
                }
                case CM_ADD: {
                    cm_AddCopyItemInHeader();




                    break;
                }
            }





            return true;
        }
        private void cm_EditItemInHeader() {


            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.time_header_dialog); //layout for dialog
            dialog.setTitle("Edit TimersCategory");
            dialog.setCancelable(false); //none-dismiss when touching outside Dialog
            // set the custom dialog components - texts and image
            EditText name = (EditText) dialog.findViewById(R.id.name);
            name.setText(indata.get(mCurrItem.idx).name);
            EditText sCategorySymbol = (EditText) dialog.findViewById(R.id.CategorySymbol);
            sCategorySymbol.setTypeface(allData.Symbol_TYPEFACE);
            sCategorySymbol.setText(indata.get(mCurrItem.idx).sTmrCategorySymbol);

            Spinner spnGender = (Spinner) dialog.findViewById(R.id.symbol_spinner);

            View btnAdd = dialog.findViewById(R.id.btn_ok);
            View btnCancel = dialog.findViewById(R.id.btn_cancel);


            ImageView simageView = (ImageView) dialog.findViewById(R.id.imageView);
            TextDrawable drawable;
            if (indata.get(mCurrItem.idx).active) {
                drawable = mDrawableBuilderHeaderActive.build(indata.get(mCurrItem.idx).sTmrCategorySymbol, mColorGenerator.getColor(mCurrItem.text));

            } else {

                drawable = mDrawableBuilderHeaderPassive.build(indata.get(mCurrItem.idx).sTmrCategorySymbol, mColorGenerator.getColor(mCurrItem.text));
                // mDrawableBuilderHeaderPassive  mDrawableBuilderHeaderActive

            }
            simageView.setImageDrawable(drawable);
            //set spinner adapter
// // TODO: 03.12.2016 create dynamycally character map with all glyph
            //String[] mAllIcons = context.getResources().getStringArray(R.array.all_icons);
            ArrayList<String> mAllIcons=new ArrayList();
            Paint paint=new Paint();
            paint.setTypeface(allData.Symbol_TYPEFACE);
            paint.setTextSize(24.0f);

            for (int i = 0; i <2200; i++) {
                boolean tbp=true;
               String sbp= Character.toString((char) i);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                    tbp = paint.hasGlyph(sbp);

                } else {

                  //  allData.Symbol_TYPEFACE.
                    tbp =  isCharGlyphAvailable(sbp,paint);
                }

                if (tbp){
                   mAllIcons.add(sbp);
               }


            }
//        end     create dynamycally character map with all glyph in mAllIcons

            ArrayAdapter<String> spnAdapter;//gendersList
            spnAdapter = new ArrayAdapter<String>(context,
                    R.layout.spec_spinner_item, (String[]) mAllIcons.toArray(new String[mAllIcons.size()] )) {
                @Override
                public View  getDropDownView(int position, View convertView, ViewGroup parent)
                {
                    View v = super.getDropDownView(position, convertView, parent);
                    ((TextView) v).setTypeface((allData.Symbol_TYPEFACE));//Typeface for dropdown view
                    // ((TextView) v).setBackgroundColor(Color.parseColor("#BBfef3da"));
                    ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_DIP,30);
                    return v;
                }

                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View v = super.getView(position, convertView, parent);
                    ((TextView) v).setTypeface(allData.Symbol_TYPEFACE);//Typeface for normal view
                    ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_DIP,30);
                    return v;
                }
            };
            //spnAdapter.


            spnGender.setAdapter(spnAdapter);
//            TextView spcname = (TextView) spnGender.findViewById(R.id.text1);
//            spcname.setTypeface(allData.Symbol_TYPEFACE);
            //set handling event for 2 buttons and spinner
            spnGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view,int position, long id) {
                    sCategorySymbol.setText(mAllIcons.get(position));

                    TextDrawable drawable;
                    if (indata.get(mCurrItem.idx).active){
                        drawable = mDrawableBuilderHeaderActive.build(mAllIcons.get(position), mColorGenerator.getColor(mCurrItem.text));

                    } else {

                        drawable = mDrawableBuilderHeaderPassive.build(mAllIcons.get(position), mColorGenerator.getColor(mCurrItem.text));
                        // mDrawableBuilderHeaderPassive  mDrawableBuilderHeaderActive

                    }
                    simageView.setImageDrawable(drawable);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            btnAdd.setOnClickListener(view -> {      //onConfirmListener(tmpItem, dialog)
                indata.get(mCurrItem.idx).name = String.valueOf(name.getText());
                indata.get(mCurrItem.idx).sTmrCategorySymbol= String.valueOf(sCategorySymbol.getText());
               // indata.add( tmpItem);
                refreshInternalData(indata);
                dialog.dismiss();

            });
            btnCancel.setOnClickListener(view -> {
                // do nothing
                dialog.dismiss();
            });
            dialog.show();

        }



        private void cm_AddCopyItemInHeader() {
// copy new item
            TimersCategoryInWorkspace tmpItem = new TimersCategoryInWorkspace();
            tmpItem.name=indata.get(mCurrItem.idx).name;
            tmpItem.id=indata.size();
            while (!TimersServiceUtils.isValidId_alTimersCategoryInWorkspace(indata,tmpItem.id)){
                tmpItem.id++;
            }
            tmpItem.sTmrCategorySymbol=indata.get(mCurrItem.idx).sTmrCategorySymbol;
            tmpItem.timersTimes= new ArrayList<>();
            tmpItem.timersTimes.addAll(indata.get(mCurrItem.idx).timersTimes);
            tmpItem.idDescription=indata.get(mCurrItem.idx).idDescription;
            tmpItem.active=false;
// copy new item
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.time_header_dialog); //layout for dialog
            dialog.setTitle("Add a new TimersCategory");
            dialog.setCancelable(false); //none-dismiss when touching outside Dialog
            // set the custom dialog components - texts and image
            EditText name = (EditText) dialog.findViewById(R.id.name);
            name.setText(tmpItem.name);
            EditText sCategorySymbol = (EditText) dialog.findViewById(R.id.CategorySymbol);
            sCategorySymbol.setTypeface(allData.Symbol_TYPEFACE);
            sCategorySymbol.setText(tmpItem.sTmrCategorySymbol);
            Spinner spnSPCSymbol_spinner = (Spinner) dialog.findViewById(R.id.symbol_spinner);
            View btnAdd = dialog.findViewById(R.id.btn_ok);
            View btnCancel = dialog.findViewById(R.id.btn_cancel);


            ImageView simageView = (ImageView) dialog.findViewById(R.id.imageView);
            TextDrawable drawable;
            if (indata.get(mCurrItem.idx).active) {
                drawable = mDrawableBuilderHeaderActive.build( tmpItem.sTmrCategorySymbol, mColorGenerator.getColor( tmpItem.name));

            } else {

                drawable = mDrawableBuilderHeaderPassive.build( tmpItem.sTmrCategorySymbol, mColorGenerator.getColor( tmpItem.name));
                // mDrawableBuilderHeaderPassive  mDrawableBuilderHeaderActive

            }

            simageView.setImageDrawable(drawable);

            //set spinner adapter
// // TODO: 03.12.2016 create dynamically character map with all glyph
            //String[] mAllIcons = context.getResources().getStringArray(R.array.all_icons);
            ArrayList<String> mAllIcons=new ArrayList();
            Paint paint=new Paint();
            paint.setTypeface(allData.Symbol_TYPEFACE);
            paint.setTextSize(24.0f);

            for (int i = 0; i <2200; i++) {
                boolean tbp=true;
                String sbp= Character.toString((char) i);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                    tbp = paint.hasGlyph(sbp);

                } else {

                    //  allData.Symbol_TYPEFACE.
                    tbp =  isCharGlyphAvailable(sbp,paint);
                }

                if (tbp){
                    mAllIcons.add(sbp);
                }


            }
//        end     create dynamically character map with all glyph in mAllIcons

            ArrayAdapter<String> spnAdapter;//gendersList
            spnAdapter = new ArrayAdapter<String>(context,
                    R.layout.spec_spinner_item, (String[]) mAllIcons.toArray(new String[mAllIcons.size()] )) {
                @Override
                public View  getDropDownView(int position, View convertView, ViewGroup parent)
                {
                    View v = super.getDropDownView(position, convertView, parent);
                    ((TextView) v).setTypeface((allData.Symbol_TYPEFACE));//Typeface for dropdown view
                   // ((TextView) v).setBackgroundColor(Color.parseColor("#BBfef3da"));
                    ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_DIP,30);
                    return v;
                }

                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View v = super.getView(position, convertView, parent);
                    ((TextView) v).setTypeface(allData.Symbol_TYPEFACE);//Typeface for normal view
                    ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_DIP,30);
                    return v;
                }
            };
            //spnAdapter.


            spnSPCSymbol_spinner.setAdapter(spnAdapter);
//            TextView spcname = (TextView) spnSPCSymbol_spinner.findViewById(R.id.text1);
//            spcname.setTypeface(allData.Symbol_TYPEFACE);
            //set handling event for 2 buttons and spinner
            spnSPCSymbol_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view,int position, long id) {
                    tmpItem.sTmrCategorySymbol=mAllIcons.get(position);


                    TextDrawable drawable;
                    if (indata.get(mCurrItem.idx).active){
                        drawable = mDrawableBuilderHeaderActive.build(mAllIcons.get(position), mColorGenerator.getColor( tmpItem.name));

                    } else {

                        drawable = mDrawableBuilderHeaderPassive.build(mAllIcons.get(position), mColorGenerator.getColor( tmpItem.name));
                        // mDrawableBuilderHeaderPassive  mDrawableBuilderHeaderActive

                    }
                    simageView.setImageDrawable(drawable);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            btnAdd.setOnClickListener(view -> {      //onConfirmListener(tmpItem, dialog)
                tmpItem.name = String.valueOf(name.getText());
                tmpItem.sTmrCategorySymbol= String.valueOf(sCategorySymbol.getText());
                indata.add( tmpItem);
                refreshInternalData(indata);
                dialog.dismiss();

            });
            btnCancel.setOnClickListener(view -> {
               // do nothing
                dialog.dismiss();
            });
            dialog.show();

        }

        private View.OnClickListener onConfirmListener(TimersCategoryInWorkspace tmpItem, Dialog dialog) {

            return null;

        }


        private void cm_SetActiveItemInHeader() {
            for (TimersCategoryInWorkspace tmp:
                 indata) {
                tmp.active=false;
            }
            indata.get(mCurrItem.idx).active=true;
            refreshInternalData(indata);
        }
        private void cm_DeleteItemInHeader() { //if not empty no del
            if (indata.get(mCurrItem.idx).timersTimes.isEmpty() ) {
                indata.remove(mCurrItem.idx);
                refreshInternalData(indata);
            } else
                if (!indata.get(mCurrItem.idx).active ) {
//                // TODO: 01.12.2016 dialog are your sure killing me softly
//                final Dialog dialog = new Dialog(context);
//
//                dialog.show();
                ArrayList<CharSequence> tmp = new ArrayList<>();
                for (TimersTime z : indata.get(mCurrItem.idx).timersTimes) {
                    tmp.add((CharSequence) z.name);
                }
             //   String[] tmparr = new String[indata.get(mCurrItem.idx).timersTimes.size()+1];

                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.Delete_entry) + indata.get(mCurrItem.idx).name +"?")
                        .setMessage(context.getString(R.string.Delete_entry_1) +
                                context.getString(R.string.Delete_entry_2)
                                + Integer.toString(indata.get(mCurrItem.idx).timersTimes.size())
                                + context.getString(R.string.Delete_entry_3))
                         .setItems((CharSequence[]) tmp.toArray(new CharSequence[tmp.size()]), ((DialogInterface.OnClickListener) (dialog, which) -> {
                             // The 'which' argument contains the index position
                             // of the selected item
//                             CustomTypefaceSpan typefaceSpan = new CustomTypefaceSpan(allData.Symbol_TYPEFACE);
                             //typefaceSpan.
                             //Font helvetica=
                         }))
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            // continue with delete
                            indata.remove(mCurrItem.idx);
                            refreshInternalData(indata);
                        })
                        .setNegativeButton(android.R.string.no, (dialog, which) -> {
                            // do nothing
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                    new AlertDialog.Builder(context)
                            .setTitle(context.getString(R.string.not_delete_Active_section) + indata.get(mCurrItem.idx).name)
                            .setMessage(R.string.not_delete_Active_section_2)


                            .setNeutralButton(android.R.string.cancel, (dialog, which) -> {
                                // do nothing
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
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
                    .setEnabled(!indata.get(mCurrItem.idx).active);//indata.get(mCurrItem.id).timersTimes.isEmpty()

            menu.add(0, CM_ADD, 0, R.string.menu_add)
                    .setIcon(android.R.drawable.sym_contact_card)
                    .setOnMenuItemClickListener(this);
        }
    }

    private boolean isCharGlyphAvailable(String ch, Paint paint) {
        //  paint.measureText(sbp);
//        Bitmap b = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ALPHA_8);
//        Canvas c = new Canvas(b);
//        c.drawText(text, 0, BITMAP_HEIGHT / 2, paint);


// superior accuracy but recource hungry
//        String missingCharacter = "\u0978"; // reserved code point (should not exist).
//        byte[] b1 = getPixels(drawBitmap(ch));
//        byte[] b2 = getPixels(drawBitmap(missingCharacter));
//        return Arrays.equals(b1, b2);

        Rect missingCharBounds = new Rect();
        char missingCharacter = '\u2936';
        paint.getTextBounds(String.valueOf(missingCharacter), 0, 1, missingCharBounds); // takes array as argument, but I need only one char.
        Rect testCharsBounds = new Rect();
        paint.getTextBounds(ch, 0, 1, testCharsBounds);
        return !testCharsBounds.equals(missingCharBounds);

//        return (paint.measureText(sbp)>0);
    }
//    private Bitmap drawBitmap(String text){
//        Bitmap b = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ALPHA_8);
//        Canvas c = new Canvas(b);
//        c.drawText(text, 0, BITMAP_HEIGHT / 2, paint);
//        return b;
//    }
//    private byte[] getPixels(Bitmap b) {
//        ByteBuffer buffer = ByteBuffer.allocate(b.getByteCount());
//        b.copyPixelsToBuffer(buffer);
//        return buffer.array();
//    }



    /**
     * ListChildViewHolder
     */
    private class ListChildViewHolder extends RecyclerView.ViewHolder  implements View.OnCreateContextMenuListener, View.OnClickListener,
            MenuItem.OnMenuItemClickListener {
        private static final int CMC_DELETE = 1;
        private static final int CMC_EDIT = 2;
        private static final int CMC_SET_ACTIVE = 3 ;
        private static final int CMC_ADD = 4 ;
        public PopupMenu menu;
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
                        case CMC_DELETE: {
                            cmc_DeleteItemInChild();
                            break;
                        }
                        case CMC_EDIT: {
                            cmc_EditItemInChild();
                            break;
                        }
                        case CMC_SET_ACTIVE: {
                            cmc_SetActiveItemInChild();

                        }
                        break;

                        case CMC_ADD: {
                            cmc_AddCopyItemInChild();

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

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            return false;
        }

        public void pListChildPopupMenuFill() {
            menu.getMenu().add(0, CMC_SET_ACTIVE, 0,R.string.menu_active)
                    .setIcon(android.R.drawable.btn_star_big_on)
                    .setEnabled(true)
                    .setOnMenuItemClickListener(this);

            MenuItem myActionItem=menu.getMenu().add(0, CMC_EDIT, 0, R.string.menu_edit);
            myActionItem.setEnabled(false);//true
            myActionItem.setIcon(android.R.drawable.ic_menu_myplaces);
            myActionItem.setOnMenuItemClickListener(this);

            menu.getMenu().add(0, CMC_DELETE, 0, R.string.menu_Delete)
                    .setIcon(R.drawable.ic_andr_cross)
                    .setEnabled(true)
                   // .setOnMenuItemClickListener(this)
                    .setEnabled(!indata.get(mCurrItem.parent_idx).timersTimes.get(mCurrItem.idx).active);
//                    .setEnabled(false);

            menu.getMenu().add(0, CMC_ADD, 0, R.string.menu_add)
                    .setIcon(android.R.drawable.sym_contact_card)
                    .setEnabled(false)//true
                    .setOnMenuItemClickListener(this);
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
        private void cmc_EditItemInChild() {


            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.time_header_dialog); //layout for dialog
            dialog.setTitle("Edit TimersCategory");
            dialog.setCancelable(false); //none-dismiss when touching outside Dialog
            // set the custom dialog components - texts and image
            EditText name = (EditText) dialog.findViewById(R.id.name);
            name.setText(indata.get(mCurrItem.idx).name);
            EditText sCategorySymbol = (EditText) dialog.findViewById(R.id.CategorySymbol);
            sCategorySymbol.setTypeface(allData.Symbol_TYPEFACE);
            sCategorySymbol.setText(indata.get(mCurrItem.idx).sTmrCategorySymbol);

            Spinner spnGender = (Spinner) dialog.findViewById(R.id.symbol_spinner);

            View btnAdd = dialog.findViewById(R.id.btn_ok);
            View btnCancel = dialog.findViewById(R.id.btn_cancel);


            ImageView simageView = (ImageView) dialog.findViewById(R.id.imageView);
            TextDrawable drawable;
            if (indata.get(mCurrItem.idx).active) {
                drawable = mDrawableBuilderHeaderActive.build(indata.get(mCurrItem.idx).sTmrCategorySymbol, mColorGenerator.getColor(mCurrItem.text));

            } else {

                drawable = mDrawableBuilderHeaderPassive.build(indata.get(mCurrItem.idx).sTmrCategorySymbol, mColorGenerator.getColor(mCurrItem.text));
                // mDrawableBuilderHeaderPassive  mDrawableBuilderHeaderActive

            }
            simageView.setImageDrawable(drawable);
            //set spinner adapter
// // TODO: 03.12.2016 create dynamycally character map with all glyph
            //String[] mAllIcons = context.getResources().getStringArray(R.array.all_icons);
            ArrayList<String> mAllIcons=new ArrayList();
            Paint paint=new Paint();
            paint.setTypeface(allData.Symbol_TYPEFACE);
            paint.setTextSize(24.0f);

            for (int i = 0; i <2200; i++) {
                boolean tbp=true;
                String sbp= Character.toString((char) i);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                    tbp = paint.hasGlyph(sbp);

                } else {

                    //  allData.Symbol_TYPEFACE.
                    tbp =  isCharGlyphAvailable(sbp,paint);
                }

                if (tbp){
                    mAllIcons.add(sbp);
                }


            }
//        end     create dynamycally character map with all glyph in mAllIcons

            ArrayAdapter<String> spnAdapter;//gendersList
            spnAdapter = new ArrayAdapter<String>(context,
                    R.layout.spec_spinner_item, (String[]) mAllIcons.toArray(new String[mAllIcons.size()] )) {
                @Override
                public View  getDropDownView(int position, View convertView, ViewGroup parent)
                {
                    View v = super.getDropDownView(position, convertView, parent);
                    ((TextView) v).setTypeface((allData.Symbol_TYPEFACE));//Typeface for dropdown view
                    // ((TextView) v).setBackgroundColor(Color.parseColor("#BBfef3da"));
                    ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_DIP,30);
                    return v;
                }

                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View v = super.getView(position, convertView, parent);
                    ((TextView) v).setTypeface(allData.Symbol_TYPEFACE);//Typeface for normal view
                    ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_DIP,30);
                    return v;
                }
            };
            //spnAdapter.


            spnGender.setAdapter(spnAdapter);
//            TextView spcname = (TextView) spnGender.findViewById(R.id.text1);
//            spcname.setTypeface(allData.Symbol_TYPEFACE);
            //set handling event for 2 buttons and spinner
            spnGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view,int position, long id) {
                    sCategorySymbol.setText(mAllIcons.get(position));

                    TextDrawable drawable;
                    if (indata.get(mCurrItem.idx).active){
                        drawable = mDrawableBuilderHeaderActive.build(mAllIcons.get(position), mColorGenerator.getColor(mCurrItem.text));

                    } else {

                        drawable = mDrawableBuilderHeaderPassive.build(mAllIcons.get(position), mColorGenerator.getColor(mCurrItem.text));
                        // mDrawableBuilderHeaderPassive  mDrawableBuilderHeaderActive

                    }
                    simageView.setImageDrawable(drawable);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            btnAdd.setOnClickListener(view -> {      //onConfirmListener(tmpItem, dialog)
//                indata.get(mCurrItem.idx).name = String.valueOf(name.getText());
//                indata.get(mCurrItem.idx).sTmrCategorySymbol= String.valueOf(sCategorySymbol.getText());
                // indata.add( tmpItem);
                indata.get(mCurrItem.parent_idx).timersTimes.get( mCurrItem.idx).name = String.valueOf(name.getText());;
                refreshInternalData(indata);
                dialog.dismiss();

            });
            btnCancel.setOnClickListener(view -> {
                // do nothing
                dialog.dismiss();
            });
            dialog.show();

        }



        private void cmc_AddCopyItemInChild() {
// copy new item

           TimersTime tmpItem = new TimersTime();
            tmpItem.name=indata.get(mCurrItem.idx).name;
            tmpItem.id=indata.size();
            while (!TimersServiceUtils.isValidId_alTimersCategoryInWorkspace(indata,tmpItem.id)){
                tmpItem.id++;
            }
//            tmpItem.sTmrCategorySymbol=indata.get(mCurrItem.idx).sTmrCategorySymbol;
//            tmpItem.timersTimes= new ArrayList<>();
//            tmpItem.timersTimes.addAll(indata.get(mCurrItem.idx).timersTimes);
            tmpItem.idDescription=indata.get(mCurrItem.idx).idDescription;
            tmpItem.active=false;
// copy new item
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.time_header_dialog); //layout for dialog
            dialog.setTitle("Add a new TimersCategory");
            dialog.setCancelable(false); //none-dismiss when touching outside Dialog
            // set the custom dialog components - texts and image
            EditText name = (EditText) dialog.findViewById(R.id.name);
            name.setText(tmpItem.name);
            EditText sCategorySymbol = (EditText) dialog.findViewById(R.id.CategorySymbol);
            sCategorySymbol.setTypeface(allData.Symbol_TYPEFACE);
            sCategorySymbol.setText(indata.get(mCurrItem.idx).sTmrCategorySymbol);
            Spinner spnSPCSymbol_spinner = (Spinner) dialog.findViewById(R.id.symbol_spinner);
            View btnAdd = dialog.findViewById(R.id.btn_ok);
            View btnCancel = dialog.findViewById(R.id.btn_cancel);


            ImageView simageView = (ImageView) dialog.findViewById(R.id.imageView);
            TextDrawable drawable;
            if (indata.get(mCurrItem.idx).active) {
                drawable = mDrawableBuilderChildActive.build( indata.get(mCurrItem.idx).sTmrCategorySymbol, mColorGenerator.getColor( tmpItem.name));

            } else {

                drawable = mDrawableBuilderChildPassive.build( indata.get(mCurrItem.idx).sTmrCategorySymbol, mColorGenerator.getColor( tmpItem.name));
                // mDrawableBuilderHeaderPassive  mDrawableBuilderHeaderActive

            }

            simageView.setImageDrawable(drawable);

            //set spinner adapter
// // TODO: 03.12.2016 create dynamically character map with all glyph
            //String[] mAllIcons = context.getResources().getStringArray(R.array.all_icons);
            ArrayList<String> mAllIcons=new ArrayList();
            Paint paint=new Paint();
            paint.setTypeface(allData.Symbol_TYPEFACE);
            paint.setTextSize(24.0f);

            for (int i = 0; i <2200; i++) {
                boolean tbp=true;
                String sbp= Character.toString((char) i);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                    tbp = paint.hasGlyph(sbp);

                } else {

                    //  allData.Symbol_TYPEFACE.
                    tbp =  isCharGlyphAvailable(sbp,paint);
                }

                if (tbp){
                    mAllIcons.add(sbp);
                }


            }
//        end     create dynamically character map with all glyph in mAllIcons

            ArrayAdapter<String> spnAdapter;//gendersList
            spnAdapter = new ArrayAdapter<String>(context,
                    R.layout.spec_spinner_item, (String[]) mAllIcons.toArray(new String[mAllIcons.size()] )) {
                @Override
                public View  getDropDownView(int position, View convertView, ViewGroup parent)
                {
                    View v = super.getDropDownView(position, convertView, parent);
                    ((TextView) v).setTypeface((allData.Symbol_TYPEFACE));//Typeface for dropdown view
                    // ((TextView) v).setBackgroundColor(Color.parseColor("#BBfef3da"));
                    ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_DIP,30);
                    return v;
                }

                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View v = super.getView(position, convertView, parent);
                    ((TextView) v).setTypeface(allData.Symbol_TYPEFACE);//Typeface for normal view
                    ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_DIP,30);
                    return v;
                }
            };
            //spnAdapter.


            spnSPCSymbol_spinner.setAdapter(spnAdapter);
//            TextView spcname = (TextView) spnSPCSymbol_spinner.findViewById(R.id.text1);
//            spcname.setTypeface(allData.Symbol_TYPEFACE);
            //set handling event for 2 buttons and spinner
            spnSPCSymbol_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view,int position, long id) {
//                    tmpItem.sTmrCategorySymbol=mAllIcons.get(position);


                    TextDrawable drawable;
                    if (indata.get(mCurrItem.idx).active){
                        drawable = mDrawableBuilderHeaderActive.build(mAllIcons.get(position), mColorGenerator.getColor( tmpItem.name));

                    } else {

                        drawable = mDrawableBuilderHeaderPassive.build(mAllIcons.get(position), mColorGenerator.getColor( tmpItem.name));
                        // mDrawableBuilderHeaderPassive  mDrawableBuilderHeaderActive

                    }
                    simageView.setImageDrawable(drawable);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            btnAdd.setOnClickListener(view -> {      //onConfirmListener(tmpItem, dialog)
                tmpItem.name = String.valueOf(name.getText());
//                tmpItem.sTmrCategorySymbol= String.valueOf(sCategorySymbol.getText());
                indata.get(mCurrItem.parent_idx).timersTimes.add( tmpItem);
                refreshInternalData(indata);
                dialog.dismiss();

            });
            btnCancel.setOnClickListener(view -> {
                // do nothing
                dialog.dismiss();
            });
            dialog.show();

        }
        private void cmc_SetActiveItemInChild() {
            for (TimersTime tmp:
                    indata.get(mCurrItem.parent_idx).timersTimes) {
                tmp.active=false;
            }
            indata.get(mCurrItem.parent_idx).timersTimes.get(mCurrItem.idx).active=true;
            refreshInternalData(indata);
        }
        private void cmc_DeleteItemInChild() { //if not empty no del

            if (!indata.get(mCurrItem.parent_idx).timersTimes.get( mCurrItem.idx).active ) {
//                // TODO: 01.12.2016 dialog are your sure killing me softly
//                final Dialog dialog = new Dialog(context);
//
//                dialog.show();
//                ArrayList<CharSequence> tmp = new ArrayList<>();
//                for (TimersTime z : indata.get(mCurrItem.idx).timersTimes) {
//                    tmp.add((CharSequence) z.name);
//                }
                //   String[] tmparr = new String[indata.get(mCurrItem.idx).timersTimes.size()+1];

                new AlertDialog.Builder(context)

                        .setTitle(context.getString(R.string.Delete_entry) + indata.get(mCurrItem.parent_idx).timersTimes.get( mCurrItem.idx).name +"?")
                        .setMessage(context.getString(R.string.Delete_entry_1)
//                                context.getString(R.string.Delete_entry_2)
//                                + Integer.toString(indata.get(mCurrItem.idx).timersTimes.size())+
//                                +context.getString(R.string.Delete_entry_3)
                        )
                        /*.setItems((CharSequence[]) tmp.toArray(new CharSequence[tmp.size()]), ((DialogInterface.OnClickListener) (dialog, which) -> {
                            // The 'which' argument contains the index position
                            // of the selected item
//                             CustomTypefaceSpan typefaceSpan = new CustomTypefaceSpan(allData.Symbol_TYPEFACE);
                            //typefaceSpan.
                            //Font helvetica=
                        }))*/
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            // continue with delete
                            indata.get(mCurrItem.parent_idx).timersTimes.remove( mCurrItem.idx);
                            refreshInternalData(indata);
                        })
                        .setNegativeButton(android.R.string.no, (dialog, which) -> {
                            // do nothing
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.not_delete_Active_section) + indata.get(mCurrItem.idx).name)
                        .setMessage(R.string.not_delete_Active_section_2)


                        .setNeutralButton(android.R.string.cancel, (dialog, which) -> {
                            // do nothing
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }




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
        public int idx;
        public int parent_idx;
        public long time; //TimeUnit. ##### .toMillis(1)
        public int repeats;
        public int maxrepeats;
        public int nextDo; //type 0 no repeats if rep>= maxrepeats & go next numb ** 1 go to next id / name
        public int nextid;

        public String sTmrCategorySymbol; // Character.toString((char) 731); // Таймер помидоро
        public boolean active;



        public Item() {
        }


        public Item(int idx,int type, String text, String sTmrCategorySymbol, boolean active, int id) {
            this.idx = idx;
            this.type = type;
            this.text = text;
            this.sTmrCategorySymbol = sTmrCategorySymbol;
            this.active = active;
            this.id = id;
        }

        public Item(int idx,int parent_idx, int type, String text, String sTmrCategorySymbol, boolean active,  int id, long time,  int repeats, int nextid, int nextDo, int maxrepeats ) {
            this.idx = idx;
            this.parent_idx=parent_idx;
            this.type = type;
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



