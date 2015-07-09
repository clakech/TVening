package fr.clakech.tvening;

import java.util.Arrays;

public class GridScheduleResult {

    public String StartDate;

    public GridChannel[] GridChannels;

    @Override
    public String toString() {
        return "GridScheduleResult{" +
                "StartDate='" + StartDate + '\'' +
                ", GridChannels=" + Arrays.toString(GridChannels) +
                '}';
    }
}
