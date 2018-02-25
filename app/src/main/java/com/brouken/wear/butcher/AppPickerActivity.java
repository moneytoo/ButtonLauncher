package com.brouken.wear.butcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import static com.brouken.wear.butcher.Utils.log;

public class AppPickerActivity extends Activity {

    private WearableRecyclerView mWearableRecyclerView;
    private CustomRecyclerAdapter mCustomRecyclerAdapter;

    private List<ResolveInfo> pkgAppsList;

    Context mContext;

    String pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        pref = getIntent().getStringExtra("pref");

        //loadFonts();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        pkgAppsList = mContext.getPackageManager().queryIntentActivities( mainIntent, 0);

        for (ResolveInfo resolveInfo : pkgAppsList) {
            /*if (resolveInfo.activityInfo.packageName.equals(getPackageName()))
                pkgAppsList.remove(resolveInfo);*/
        }

        Collections.sort(pkgAppsList, new ResolveInfo.DisplayNameComparator(mContext.getPackageManager()));
        pkgAppsList.add(0, null);

        setContentView(R.layout.activity_app);

        mWearableRecyclerView = findViewById(R.id.recycler_view);

        //mWearableRecyclerView.setCircularScrollingGestureEnabled(true);
        //mWearableRecyclerView.setBezelFraction(0.5f);
        //mWearableRecyclerView.setScrollDegreesPerScreen(90);

        mWearableRecyclerView.setEdgeItemsCenteringEnabled(true);

        // Customizes scrolling so items farther away form center are smaller.
        ScalingScrollLayoutCallback scalingScrollLayoutCallback = new ScalingScrollLayoutCallback();
        mWearableRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this, scalingScrollLayoutCallback));

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        mWearableRecyclerView.setHasFixedSize(true);

        mCustomRecyclerAdapter = new CustomRecyclerAdapter(pkgAppsList.toArray(new ResolveInfo[pkgAppsList.size()]),this);

        mWearableRecyclerView.setAdapter(mCustomRecyclerAdapter);

        // TODO: HACK
        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String name = PreferenceManager.getDefaultSharedPreferences(mContext).getString("typeface", JustTimeWatchFace.preference_typeface);
                mWearableRecyclerView.scrollToPosition(fonts.indexOf(name));
            }
        }, 200);
        */
    }


    public void itemSelected(String pkg, String cls) {
        //Log.d("FONT", item);
        Intent intent=new Intent();
        intent.putExtra("pref", pref);

        if (pkg != null && cls != null)
            intent.putExtra("app", pkg + "/" + cls);

        intent.putExtra("pkg", pkg);
        intent.putExtra("cls", cls);
        setResult(RESULT_OK, intent);
        finish();
    }

    private static final class CustomRecyclerAdapter extends WearableRecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder> {

        private static final String TAG = "CustomRecyclerAdapter";
        private ResolveInfo[] mDataSet;
        private AppPickerActivity mAppPickerActivity;

        public static class ViewHolder extends android.support.wearable.view.WearableRecyclerView.ViewHolder {

            private final TextView mTextView;
            private final ImageView mImageView;

            public ViewHolder(View view) {
                super(view);
                mTextView = (TextView) view.findViewById(R.id.textView);
                mImageView = (ImageView) view.findViewById(R.id.imageView);
            }

            @Override
            public String toString() { return (String) mTextView.getText(); }
        }

        public CustomRecyclerAdapter(ResolveInfo[] dataSet, AppPickerActivity appPickerActivity) {
            mDataSet = dataSet;
            mAppPickerActivity = appPickerActivity;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            log("Element " + position + " set.");

            viewHolder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mDataSet[position] == null) {
                        mAppPickerActivity.itemSelected(null, null);
                        return;
                    }

                    String pkg = mDataSet[position].activityInfo.packageName;
                    String cls = mDataSet[position].activityInfo.name;
                    mAppPickerActivity.itemSelected(pkg, cls);

                }
            });

            // Replaces content of view with correct element from data set
            //viewHolder.mTextView.setText(mDataSet[position]);
            if (mDataSet[position] == null) {
                viewHolder.mTextView.setText("None");

                return;
            }

            viewHolder.mTextView.setText(mDataSet[position].activityInfo.loadLabel(mAppPickerActivity.getPackageManager()).toString());
            viewHolder.mImageView.setImageDrawable(mDataSet[position].loadIcon(mAppPickerActivity.getPackageManager()));

            /*if (typefaces.get(position) != null)
                viewHolder.mTextView.setTypeface(typefaces.get(position));*/
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataSet.length;
        }
    }


}
