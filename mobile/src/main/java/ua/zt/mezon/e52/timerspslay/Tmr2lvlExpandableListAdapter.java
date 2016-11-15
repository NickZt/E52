package ua.zt.mezon.e52.timerspslay;

/**
 * Created by MezM on 09.11.2016.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ua.zt.mezon.e52.AllData;
import ua.zt.mezon.e52.R;
import ua.zt.mezon.e52.misc.TimersCategoryInWorkspace;
import ua.zt.mezon.e52.misc.TimersTime;

//
public class Tmr2lvlExpandableListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int HEADER = 0;
    public static final int CHILD = 1;
    private ArrayList <TimersCategoryInWorkspace> indata;
    private List<Item> data = new ArrayList<>();
    Context context ;
    private AllData allData = AllData.getInstance();
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilderHeaderActive,mDrawableBuilderHeaderPassive;
    private TextDrawable.IBuilder mDrawableBuilderChildActive,mDrawableBuilderChildPassive;

    public int toPx(int dp) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
    public Tmr2lvlExpandableListAdapter(ArrayList <TimersCategoryInWorkspace> indata) {
       // List<Item> data
        this.indata = indata;

        for (TimersCategoryInWorkspace z  : indata)
            {
                if (z.active){
                    this.data.add(new  Item( HEADER, z.name,z.sTmrCategorySymbol,z.active,z.id));
                    // Item( HEADER, z.name) public Item(int type, String text, String sTmrCategorySymbol, boolean active, int id)
                    for (TimersTime tmp_child:
                         z.timersTimes) {
                        this.data.add(new  Item( CHILD, tmp_child.name, "",tmp_child.active,tmp_child.id,tmp_child.time,tmp_child.repeats,tmp_child.nextid,tmp_child.nextDo,tmp_child.maxrepeats ));
                        //  public Item(int type, String text, String sTmrCategorySymbol, boolean active,  int id, long time,  int repeats, int nextid, int nextDo, int maxrepeats ) {
                    }
                } else {

                    Item Tmp_add_inner_places = new  Item( HEADER, z.name,z.sTmrCategorySymbol,z.active,z.id);
                    Tmp_add_inner_places.invisibleChildren = new ArrayList<>();

                    for (TimersTime tmp_child:
                            z.timersTimes) {
//                        this.data.add(new  Item( CHILD, tmp_child.name));
                        Tmp_add_inner_places.invisibleChildren.add(new  Item( CHILD, tmp_child.name, "",tmp_child.active,tmp_child.id,tmp_child.time,tmp_child.repeats,tmp_child.nextid,tmp_child.nextDo,tmp_child.maxrepeats ));
                    }
                    this.data.add(Tmp_add_inner_places);

                }
            }



        
        
        
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
            case HEADER:
                final ListHeaderViewHolder itemController = (ListHeaderViewHolder) holder;
                itemController.refferalItem = item;
                itemController.header_title.setText(item.text);
                if (data.get(position).active) {
                    itemController.header_title.setBackgroundColor(Color.GREEN);
                    TextDrawable drawable = mDrawableBuilderHeaderActive.build(item.sTmrCategorySymbol, mColorGenerator.getRandomColor());
                    itemController.imageView.setImageDrawable(drawable);
                }
                else {
                    itemController.header_title.setBackgroundColor(Color.TRANSPARENT);
                    TextDrawable drawable = mDrawableBuilderHeaderPassive.build(item.sTmrCategorySymbol, mColorGenerator.getColor(item.text));
                    // mDrawableBuilderHeaderPassive  mDrawableBuilderHeaderActive
                    itemController.imageView.setImageDrawable(drawable);
                }


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
                break;
            case CHILD:
                final ListChildViewHolder itemChController = (ListChildViewHolder) holder;
               // itemChController;
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

    @Override
    public int getItemCount() {
        return data.size();
    }

    private static class ListHeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView header_title;
        public ImageView btn_expand_toggle;
        public Item refferalItem;
        CardView cv;
        TextView description;
        ImageView imageView;
        public ListHeaderViewHolder(View itemView) {
            super(itemView);
            header_title = (TextView) itemView.findViewById(R.id.header_title);
            btn_expand_toggle = (ImageView) itemView.findViewById(R.id.btn_expand_toggle);
            cv = (CardView) itemView.findViewById(R.id.cardView);
            description = (TextView) itemView.findViewById(R.id.description);
            imageView = (ImageView) itemView.findViewById(R.id.himageView);
        }
    }
    private static class ListChildViewHolder extends RecyclerView.ViewHolder {
        public TextView child_title;
        CardView cv;
        TextView timedescription;
        ImageView imageView;
        public ListChildViewHolder(View itemView) {
            super(itemView);
            child_title = (TextView) itemView.findViewById(R.id.child_title);
            cv = (CardView) itemView.findViewById(R.id.child_cardView);
            timedescription = (TextView) itemView.findViewById(R.id.timedescription);
            imageView = (ImageView) itemView.findViewById(R.id.cimageView);
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





/*
public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    class ViewHolder0 extends RecyclerView.ViewHolder {
        public ViewHolder0(View itemView) {
            super(itemView);
        }
        //...
    }

    class ViewHolder2 extends RecyclerView.ViewHolder {
        public ViewHolder2(View itemView) {
            super(itemView);
        }
        //  ...
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return position % 2 * 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0: return new ViewHolder0();
            case 2: return new ViewHolder2();
           // ...
            default: return new ViewHolder2();
        }
    }

    public class Sample1Binder extends DataBinder<Sample1Binder.ViewHolder> {

        private List<String> mDataSet = new ArrayList();

        public Sample1Binder(DataBindAdapter dataBindAdapter) {
            super(dataBindAdapter);
        }

        @Override
        public ViewHolder newViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_sample1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void bindViewHolder(ViewHolder holder, int position) {
            String title = mDataSet.get(position);
            holder.mTitleText.setText(title);
        }

        @Override
        public int getItemCount() {
            return mDataSet.size();
        }

        public void setDataSet(List<String> dataSet) {
            mDataSet.addAll(dataSet);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            TextView mTitleText;

            public ViewHolder(View view) {
                super(view);
                mTitleText = (TextView) view.findViewById(R.id.title_type1);
            }
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder{

        //These are the general elements in the RecyclerView
        public TextView place;
        public ImageView pics;

        //This is the Header on the Recycler (viewType = 0)
        public TextView name, description;

        //This constructor would switch what to findViewBy according to the id of viewType
        public ViewHolder(View v, int viewType) {
            super(v);
            if (viewType == 0) {
                name = (TextView) v.findViewById(R.id.name);
                decsription = (TextView) v.findViewById(R.id.description);
            } else if (viewType == 1) {
                place = (TextView) v.findViewById(R.id.place);
                pics = (ImageView) v.findViewById(R.id.pics);
            }
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType)
    {
        View v;
        ViewHolder vh;
        // create a new view
        switch (viewType) {
            case 0: //This would be the header view in my Recycler
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_welcome, parent, false);
                vh = new ViewHolder(v,viewType);
                return  vh;
            default: //This would be the normal list with the pictures of the places in the world
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_picture, parent, false);
                vh = new ViewHolder(v, viewType);
                v.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, nextActivity.class);
                        intent.putExtra("ListNo",mRecyclerView.getChildPosition(v));
                        mContext.startActivity(intent);
                    }
                });
                return vh;
        }
    }

    //Overriden so that I can display custom rows in the recyclerview
    @Override
    public int getItemViewType(int position) {
        int viewType = 1; //Default is 1
        if (position == 0) viewType = 0; //if zero, it will be a header view
        return viewType;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //position == 0 means its the info header view on the Recycler
        if (position == 0) {
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"name clicked", Toast.LENGTH_SHORT).show();
                }
            });
            holder.description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"description clicked", Toast.LENGTH_SHORT).show();
                }
            });
            //this means it is beyond the headerview now as it is no longer 0. For testing purposes, I'm alternating between two pics for now
        } else if (position > 0) {
            holder.place.setText(mDataset[position]);
            if (position % 2 == 0) {
                holder.pics.setImageDrawable(mContext.getResources().getDrawable(R.drawable.pic1));
            }
            if (position % 2 == 1) {
                holder.pics.setImageDrawable(mContext.getResources().getDrawable(R.drawable.pic2));
            }

        }
    }

    private class DataBinder<T> {
    }
}*/
