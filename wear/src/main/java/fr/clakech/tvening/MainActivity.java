package fr.clakech.tvening;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

    private GridViewPager pager;
    private DotsPageIndicator dotsPageIndicator;

    private List<GridChannel> gridChannels;

    private TeleportClient teleportClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Log.d("TV", "startMainScreen!");

        runOnUiThread(() -> {
            findViewById(R.id.textView).setVisibility(View.INVISIBLE);

            if (pager != null && pager.getAdapter() == null) {
                pager.setAdapter(new ScheduleGridPagerAdapter(gridChannels, MainActivity.this, getFragmentManager()));
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
                gridChannels = new ArrayList<>();

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
                int max = dataMap.getInt(TVCommons.KEY_IMAGE_COUNT);
                Log.d("TV", "KEY_IMAGE_COUNT:" + max);
                DrawableCache.init(max);
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
        DrawableCache.getInstance().put(key, drawable);
        if (DrawableCache.getInstance().size() == DrawableCache.getInstance().maxSize()) {
            startMainScreen();
        }
    }

}
