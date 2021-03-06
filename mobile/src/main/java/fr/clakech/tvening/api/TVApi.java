package fr.clakech.tvening.api;


import fr.clakech.tvening.ProgramResult;
import fr.clakech.tvening.Schedule;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface TVApi {

    String ENDPOINT = "http://rovicloud-clakech.rhcloud.com";
    String DEFAULT_SERVICE = "891380";

    @GET("/?duration=60&includechannelimages=true&titletype=2")
    Observable<Schedule> getSchedule(@Query("$url") String url, @Query("startdate") String startDate, @Query("locale") String locale);

    @GET("/?imagecount=1&imageformatid=16&include=Program,Image&copytype=1")
    Observable<ProgramResult> getProgram(@Query("$url") String url, @Query("locale") String locale);

}
