package fr.clakech.tvening;

import java.util.Arrays;

public class GridChannel {

    public String DisplayName;

    public String Channel;

    public Airing[] Airings;

    public ChannelImage[] ChannelImages;

    @Override
    public String toString() {
        return "GridChannel{" +
                "DisplayName='" + DisplayName + '\'' +
                ", Channel='" + Channel + '\'' +
                ", Airings=" + Arrays.toString(Airings) +
                ", ChannelImages=" + Arrays.toString(ChannelImages) +
                '}';
    }
}
