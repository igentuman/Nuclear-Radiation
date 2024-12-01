package igentuman.nr.api;

import java.util.List;

public interface IPointRadiation {

    long getRadiation();

    void setRadiation(long radiation);

    long getChunk();

    List<INucleotide> getNucleotides();

    void addNucleotide(INucleotide nucleotide);

    void removeNucleotide(INucleotide nucleotide);

    long getRadiationAtPoint(int x, int y, int z);
}
