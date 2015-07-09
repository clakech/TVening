package fr.clakech.tvening;

import java.io.Serializable;

public class Airing implements Serializable{

    public String Title;

    public String EpisodeTitle;

    public String AiringTime;

    public String ProgramId;

    public int Duration;

    public String CopyText;

    public String ImageUrl;

    @Override
    public String toString() {
        return "Airing{" +
                "Title='" + Title + '\'' +
                ", EpisodeTitle='" + EpisodeTitle + '\'' +
                ", AiringTime='" + AiringTime + '\'' +
                ", ProgramId='" + ProgramId + '\'' +
                ", Duration=" + Duration +
                ", CopyText='" + CopyText + '\'' +
                ", ImageUrl='" + ImageUrl + '\'' +
                '}';
    }
}
