package fr.clakech.tvening.service;

import android.graphics.Bitmap;

import android.util.Log;
import android.view.View;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.mariux.teleport.lib.TeleportService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import fr.clakech.tvening.Airing;
import fr.clakech.tvening.GridChannel;
import fr.clakech.tvening.ProgramImage;
import fr.clakech.tvening.ProgramResult;
import fr.clakech.tvening.PropertyReader;
import fr.clakech.tvening.TVCommons;
import fr.clakech.tvening.api.TVApi;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.SimpleTimeZone;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TVService extends TeleportService {

    private TVApi tvApi;
    private Locale locale;
    private String defaultService;

    @Override
    public void onCreate() {
        super.onCreate();
        locale = getResources().getConfiguration().locale;
        PropertyReader propertyReader = new PropertyReader(this);
        Properties properties = propertyReader.getMyProperties("countrydefaultservice.properties");
        defaultService = properties.getProperty(locale.getCountry());

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 5 * 1000))
                .build();
        ImageLoader.getInstance().init(config);

        RequestInterceptor requestInterceptor = request -> request.addHeader("Accept", "application/json");

        tvApi = new RestAdapter.Builder()
                .setEndpoint(TVApi.ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(requestInterceptor)
                .setLog(msg -> Log.d("TV", msg))
                .build()
                .create(TVApi.class);


        setOnGetMessageTaskBuilder(
                new OnGetMessageTask.Builder() {
                    @Override
                    public OnGetMessageTask build() {
                        return new MessageTask();
                    }
                }
        );

    }

    public void getSchedule() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        String startDate = sdf.format(Calendar.getInstance().getTime()) + "T19:00:00Z";
        String localeForRovi = locale.getLanguage() + "-" + locale.getCountry();
        String service = defaultService != null ? defaultService : TVApi.DEFAULT_SERVICE;
        String urlSchedule = "http://api.rovicorp.com/TVlistings/v9/listings/gridschedule/" + service + "/info";

        Observable<List<GridChannel>> scheduleObservable = tvApi.getSchedule(urlSchedule, startDate, localeForRovi)
                .flatMap(schedule -> Observable.from(Arrays.asList(schedule.GridScheduleResult.GridChannels)))
                .take(10)
                .flatMap(gridChannel -> {

                    List<Airing> listAllAirings = Arrays.asList(gridChannel.Airings);
                    Observable<List<Airing>> listAiring = Observable.from(listAllAirings)
                            .flatMap(airing -> {
                                String urlProgram = "http://api.rovicorp.com/TVlistings/v9/listings/programdetails/" + airing.ProgramId + "/info";
                                Observable<ProgramResult> programObs = tvApi.getProgram(urlProgram, localeForRovi);

                                // pfff, to avoid API limitation of 5 hits / sec
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                return Observable.zip(
                                        Observable.just(airing),
                                        programObs,
                                        (airing1, program) -> {
                                            ProgramImage[] programImages = program.ProgramDetailsResult.Program.ProgramImages;
                                            if (programImages.length > 0) {
                                                airing1.ImageUrl = programImages[0].ImageUrl;
                                            }
                                            airing1.CopyText = program.ProgramDetailsResult.Program.CopyText;
                                            return airing1;
                                        }
                                );
                            })
                            .toList();

                    return Observable.zip(
                            Observable.just(gridChannel),
                            listAiring,
                            (grid, airings) -> {
                                grid.Airings = airings.toArray(new Airing[0]);
                                return grid;
                            }
                    );
                })
                .toList();


        scheduleObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new ArrayList<>();
                })
                .subscribe(listGridChannel -> {

                    final PutDataMapRequest putDataMapReq = PutDataMapRequest.create(TVCommons.PATH_TVENING);
                    Set<String> imgUrlProgramIds = new HashSet<>();
                    Set<String> imgUrlChannels = new HashSet<>();

                    final ArrayList<DataMap> scheduleDataMap = new ArrayList<>();

                    Log.d("TV", "listGridChannel: " + listGridChannel);

                    if (listGridChannel.isEmpty()) {
                        syncBoolean(TVCommons.KEY_SCHEDULE_ERROR, true);
                    }

                    for (GridChannel gridChannel : listGridChannel) {
                        final DataMap gridChannelDataMap = new DataMap();

                        gridChannelDataMap.putString("DisplayName", gridChannel.DisplayName);
                        gridChannelDataMap.putString("Channel", gridChannel.Channel);

                        if (gridChannel.ChannelImages != null && gridChannel.ChannelImages.length > 0) {
                            imgUrlChannels.add(gridChannel.ChannelImages[0].ImageUrl);
                            gridChannelDataMap.putString("ImageUrl", gridChannel.ChannelImages[0].ImageUrl);
                        }

                        final ArrayList<DataMap> airingsDataMap = new ArrayList<>();
                        for (Airing airing : gridChannel.Airings) {
                            if (airing.Duration < 30)
                                break;
                            final DataMap airingChannel = new DataMap();
                            airingChannel.putString("AiringTime", airing.AiringTime);
                            airingChannel.putString("EpisodeTitle", airing.EpisodeTitle);
                            airingChannel.putString("Title", airing.Title);
                            airingChannel.putString("CopyText", airing.CopyText);
                            airingChannel.putInt("Duration", airing.Duration);
                            airingChannel.putString("ImageUrl", airing.ImageUrl);
                            airingChannel.putString("ProgramId", airing.ProgramId);
                            if (airing.ImageUrl != null) {
                                imgUrlProgramIds.add(airing.ImageUrl);
                            }
                            airingsDataMap.add(airingChannel);
                        }
                        gridChannelDataMap.putDataMapArrayList("Airings", airingsDataMap);

                        scheduleDataMap.add(gridChannelDataMap);

                    }

                    putDataMapReq.getDataMap().putDataMapArrayList(TVCommons.KEY_SCHEDULE, scheduleDataMap);
                    putDataMapReq.getDataMap().putLong("timestamp", System.currentTimeMillis());
                    syncDataItem(putDataMapReq);

                    int count = imgUrlProgramIds.size() + imgUrlChannels.size();
                    PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(TVCommons.PATH_TVENING_IMAGE_COUNT);
                    putDataMapRequest.getDataMap().putLong("timestamp", System.currentTimeMillis());
                    putDataMapRequest.getDataMap().putInt(TVCommons.KEY_IMAGE_COUNT, count);
                    syncDataItem(putDataMapRequest);

                    for (String urlProgramId : imgUrlProgramIds) {
                        loadImageUrl(urlProgramId, 150);
                    }

                    for (String urlChannel : imgUrlChannels) {
                        loadImageUrl(urlChannel, 32);
                    }

                });

    }

    private void loadImageUrl(final String imgUrl, final int size) {

        ImageSize targetSize = new ImageSize(size, size);

        String[] splitted = imgUrl.split("\\?");
        String proxyImgUrl = TVApi.ENDPOINT + "/?" + splitted[1] + "&$url=" + splitted[0];

        ImageLoader.getInstance().loadImage(proxyImgUrl, targetSize, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (loadedImage != null) {
                    Asset asset = createAssetFromBitmap(loadedImage);

                    final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(TVCommons.PATH_TVENING_IMAGE + imgUrl);
                    putDataMapRequest.getDataMap().putLong("timestamp", System.currentTimeMillis());
                    putDataMapRequest.getDataMap().putAsset("image", asset);
                    putDataMapRequest.getDataMap().putString("key", imgUrl);
                    putDataMapRequest.getDataMap().putString(TVCommons.KEY_IMAGE, TVCommons.KEY_IMAGE);
                    syncDataItem(putDataMapRequest);
                } else {
                    Log.w("TV", "loadedImage is null:" + imgUrl);
                    failLoadImage(null);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                failLoadImage(failReason);
            }

            private void failLoadImage(FailReason failReason) {
                Log.w("TV", "failed to loaded Image:" + imgUrl + " " + failReason);

                final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(TVCommons.PATH_TVENING_IMAGE + imgUrl);
                putDataMapRequest.getDataMap().putLong("timestamp", System.currentTimeMillis());
                putDataMapRequest.getDataMap().putString("key", imgUrl);
                putDataMapRequest.getDataMap().putAsset("image", null);
                putDataMapRequest.getDataMap().putString(TVCommons.KEY_IMAGE, TVCommons.KEY_IMAGE);
                putDataMapRequest.getDataMap().putString(TVCommons.KEY_SCHEDULE_ERROR, TVCommons.KEY_SCHEDULE_ERROR);
                syncDataItem(putDataMapRequest);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                failLoadImage(null);
            }
        });

    }

    public static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private class MessageTask extends OnGetMessageTask {

        @Override
        protected void onPostExecute(String path) {
            if (path.equals("bonjour")) {
                new Thread(() -> getSchedule()).start();
            }
        }
    }

}
