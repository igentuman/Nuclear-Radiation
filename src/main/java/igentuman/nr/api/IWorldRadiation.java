package igentuman.nr.api;

import net.minecraft.core.BlockPos;

public interface IWorldRadiation {

    long getChunkRadiation(long chunk);

    long getChunkRadiation(BlockPos pos);

    void contaminateChunk(long chunk, INucleotide nucleotide);

    void contaminateChunk(BlockPos pos, INucleotide nucleotide);

    void addPointRadiation(long chunk, IPointRadiation pointRadiation);

    void addPointRadiation(BlockPos pos, IPointRadiation pointRadiation);

    void removePointRadiation(long chunk, IPointRadiation pointRadiation);

    void removePointRadiation(BlockPos pos, IPointRadiation pointRadiation);

    long getRadaitionAtPoint(BlockPos pos);

}
