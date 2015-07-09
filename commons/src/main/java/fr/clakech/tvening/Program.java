package fr.clakech.tvening;

import java.util.Arrays;

public class Program {

    public String MasterTitle;
    public String EpisodeTitle;
    public String CopyText;

    public ProgramImage[] ProgramImages;

    @Override
    public String toString() {
        return "Program{" +
                "MasterTitle='" + MasterTitle + '\'' +
                ", EpisodeTitle='" + EpisodeTitle + '\'' +
                ", CopyText='" + CopyText + '\'' +
                ", ProgramImages=" + Arrays.toString(ProgramImages) +
                '}';
    }
}
