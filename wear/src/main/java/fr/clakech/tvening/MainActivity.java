package fr.clakech.tvening;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.mariux.teleport.lib.TeleportClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    public static final int PAGINATION = 6;
    private GridViewPager pager;
    private DotsPageIndicator dotsPageIndicator;
    private ScheduleGridPagerAdapter adapter;
    private List<GridChannel> gridChannels;

    private TeleportClient teleportClient;
    private int lastIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastIndex = 0;
        setContentView(R.layout.activity_main);
        pager = (GridViewPager) findViewById(R.id.pager);
        dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);

        teleportClient = new TeleportClient(this);

        teleportClient.setOnSyncDataItemTaskBuilder(
                new TeleportClient.OnSyncDataItemTask.Builder() {
                    @Override
                    public TeleportClient.OnSyncDataItemTask build() {
                        return new SyncDataTask();
                    }
                }
        );

    }

    @Override
    protected void onStart() {
        super.onStart();
        teleportClient.connect();

        teleportClient.sendMessage("bonjour", null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        teleportClient.disconnect();
    }

    public void startMainScreen() {
        Log.d("TV", "startMainScreen - lastIndex:" + lastIndex + " gridChannels:" + gridChannels.size());

        runOnUiThread(() -> {
            findViewById(R.id.textView).setVisibility(View.INVISIBLE);
            ActionFragment.Listener listener = () -> {
                teleportClient.sendMessage("nextPlease-" + (lastIndex + PAGINATION), null);
                Intent intent = new Intent(this, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.SUCCESS_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                        getString(R.string.loading));
                startActivity(intent);
            };

            if (pager != null && pager.getAdapter() == null) {
                adapter = new ScheduleGridPagerAdapter(gridChannels,
                        listener,
                        MainActivity.this,
                        getFragmentManager());
                pager.setAdapter(adapter);
            } else if (adapter != null) {
                adapter.loadRows(gridChannels, listener);
                adapter.notifyDataSetChanged();
                pager.setCurrentItem(lastIndex, 0, true);
            } else {
                Log.e("TV", "Error startMainScreen!");
            }
        });
    }

    private class SyncDataTask extends TeleportClient.OnSyncDataItemTask {

        @Override
        protected void onPostExecute(DataMap dataMap) {
            Log.d("TV", "dataMap:" + dataMap);

            if (dataMap.containsKey(TVCommons.KEY_SCHEDULE)) {
                lastIndex = dataMap.getInt(TVCommons.KEY_CURRENT_INDEX);

                if (lastIndex < 1) {
                    gridChannels = new ArrayList<>();
                    DrawableCache.getInstance().evictAll();
                }

                List<DataMap> gridChannelsDataMap = dataMap.getDataMapArrayList(TVCommons.KEY_SCHEDULE);

                Log.d("TV", "gridChannelsDataMap:" + gridChannelsDataMap);

                for (DataMap gridChannelDataMap : gridChannelsDataMap) {
                    GridChannel gridChannel = new GridChannel();
                    gridChannel.DisplayName = gridChannelDataMap.getString("DisplayName");
                    gridChannel.Channel = gridChannelDataMap.getString("Channel");
                    gridChannel.ChannelImages = new ChannelImage[1];
                    gridChannel.ChannelImages[0] = new ChannelImage();
                    gridChannel.ChannelImages[0].ImageUrl = gridChannelDataMap.getString("ImageUrl");

                    List<DataMap> airingsDataMap = gridChannelDataMap.getDataMapArrayList("Airings");

                    List<fr.clakech.tvening.Airing> airings = new ArrayList<>();
                    for (DataMap airingDataMap : airingsDataMap) {
                        Airing a = new Airing();
                        a.AiringTime = airingDataMap.getString("AiringTime");
                        a.Duration = airingDataMap.getInt("Duration");
                        a.EpisodeTitle = airingDataMap.getString("EpisodeTitle");
                        a.CopyText = airingDataMap.getString("CopyText");
                        a.Title = airingDataMap.getString("Title");
                        a.ImageUrl = airingDataMap.getString("ImageUrl");
                        a.ProgramId = airingDataMap.getString("ProgramId");
                        Log.d("TV", "airing: " + a);

                        airings.add(a);
                    }

                    gridChannel.Airings = airings.toArray(new Airing[0]);

                    gridChannels.add(gridChannel);
                }

            } else if (dataMap.containsKey(TVCommons.KEY_IMAGE)) {
                Asset image = dataMap.getAsset("image");
                String keyValue = dataMap.getString("key");

                Log.d("TV", "image:" + image);
                if (dataMap.containsKey(TVCommons.KEY_SCHEDULE_ERROR)) {
                    putInCache(keyValue, new ColorDrawable(Color.TRANSPARENT));
                } else {
                    new LoadImage(keyValue).execute(image, teleportClient.getGoogleApiClient());
                }
            } else if (dataMap.containsKey(TVCommons.KEY_IMAGE_COUNT)) {
                int count = dataMap.getInt(TVCommons.KEY_IMAGE_COUNT);
                Log.d("TV", "KEY_IMAGE_COUNT:" + count);
                DrawableCache.getInstance().resize(DrawableCache.getInstance().size() + count);
            }

        }
    }

    class LoadImage extends TeleportClient.ImageFromAssetTask {

        private String key;

        public LoadImage(String key) {
            this.key = key;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Drawable drawable;
            if (bitmap != null) {
                drawable = new BitmapDrawable(MainActivity.this.getResources(), bitmap);
            } else {
                Log.d("TV", "LoadImage bitmap is null");
                drawable = new ColorDrawable(Color.TRANSPARENT);
            }
            putInCache(this.key, drawable);
        }
    }

    private void putInCache(String key, Drawable drawable) {
        Log.d("TV", "put:" + key);
        Log.d("TV", "DrawableCache.getInstance().size():" + DrawableCache.getInstance().size());
        Log.d("TV", "DrawableCache.getInstance().maxSize():" + DrawableCache.getInstance().maxSize());

        DrawableCache.getInstance().put(key, drawable);
        if (DrawableCache.getInstance().size() == DrawableCache.getInstance().maxSize()) {
            startMainScreen();
        }
    }

}
