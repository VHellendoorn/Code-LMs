package sassa.searcher;

import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.math.Vec3i;
import sassa.util.Singleton;
import sassa.util.StructureProvider;

import java.util.*;

public class StructureSearcher {

    public static ArrayList<StructureProvider> findStructure(int searchSize, long worldSeed, ArrayList<StructureProvider> list) {
        ChunkRand rand = new ChunkRand();

        //BiomeSource source1 = Searcher.getBiomeSource("NETHER", worldSeed);
        //BiomeSource source2 = Searcher.getBiomeSource("END", worldSeed);
        // TODO: Add biome percision back in int biomePercision
        ArrayList<StructureProvider> listReturn = new ArrayList<>(list);

        int xSize = 0;
        int zSize = 0;
        //Can probably still clean this up but it works...
        if(Singleton.getInstance().getSpawnPoint().isSelected()) {
            BiomeSource source = Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed);
            xSize = Integer.parseInt(Singleton.getInstance().getXCoordSpawn().getText());
            zSize = Integer.parseInt(Singleton.getInstance().getZCoordSpawn().getText());
            if(!Searcher.checkSpawnPoint(source)){
                return listReturn;
            }
        }

        for(StructureProvider searchStructure: list) {
            RegionStructure<?,?> struct = searchStructure.getStructureSupplier().create(Singleton.getInstance().getMinecraftVersion());

            RegionStructure.Data<?> lowerBound = struct.at(-searchSize + xSize >> 4, -searchSize + zSize >> 4);
            RegionStructure.Data<?> upperBound = struct.at(searchSize + xSize >> 4, searchSize + zSize >> 4);

            int howManyStructures = 0;

            for (int regionX = lowerBound.regionX; regionX <= upperBound.regionX; regionX++) {
                for (int regionZ = lowerBound.regionZ; regionZ <= upperBound.regionZ; regionZ++) {
                    CPos structs = struct.getInRegion(worldSeed, regionX, regionZ, rand);

                    if (structs == null) continue;
                    if (structs.distanceTo(Vec3i.ZERO, DistanceMetric.CHEBYSHEV) > searchSize >> 4) continue;
                    if (!struct.canSpawn(structs.getX(), structs.getZ(), Searcher.getBiomeSource(searchStructure.getDimension(), worldSeed))) continue; // || !struct.canSpawn(structs.getX(), structs.getZ(), source1) || !struct.canSpawn(structs.getX(), structs.getZ(), source2)

                    howManyStructures++;
                    if(howManyStructures >= Collections.frequency(listReturn, searchStructure)){
                        for(StructureProvider ss: list){
                            if(ss == searchStructure){
                                listReturn.remove(searchStructure);
                            }
                        }
                    }

                    //System.out.println(list.size());
                    if(listReturn.size() == 0) {
                        return listReturn;
                    }
                    //System.out.println("Found world seed " + worldSeed + " with structure seed " + structureSeed);
                    //System.out.println("The structure is at (" + struct.getX() * 16 + ", " + struct.getZ() * 16 + ")");
                }
            }
        }
        return listReturn;
    }

    public static ArrayList<StructureProvider> findStructureEx(int searchSize, long worldSeed, ArrayList<StructureProvider> list) {
        ChunkRand rand = new ChunkRand();

        //BiomeSource source1 = Searcher.getBiomeSource("NETHER", worldSeed);
        //BiomeSource source2 = Searcher.getBiomeSource("END", worldSeed);
        // TODO: Add biome percision back in int biomePercision
        ArrayList<StructureProvider> listReturn = new ArrayList<>(list);

        int xSize = 0;
        int zSize = 0;
        if(Singleton.getInstance().getSpawnPoint().isSelected()) {
            BiomeSource source = Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed);
            xSize = Integer.parseInt(Singleton.getInstance().getXCoordSpawn().getText());
            zSize = Integer.parseInt(Singleton.getInstance().getZCoordSpawn().getText());
            if(!Searcher.checkSpawnPoint(source)){
                return listReturn;
            }
        }

        for(StructureProvider searchStructure: list) {
            RegionStructure<?,?> struct = searchStructure.getStructureSupplier().create(Singleton.getInstance().getMinecraftVersion());

            RegionStructure.Data<?> lowerBound = struct.at(-searchSize + xSize >> 4, -searchSize + zSize >> 4);
            RegionStructure.Data<?> upperBound = struct.at(searchSize + xSize >> 4, searchSize + zSize >> 4);

            int howManyStructures = 0;

            for (int regionX = lowerBound.regionX; regionX <= upperBound.regionX; regionX++) {
                for (int regionZ = lowerBound.regionZ; regionZ <= upperBound.regionZ; regionZ++) {
                    CPos structs = struct.getInRegion(worldSeed, regionX, regionZ, rand);

                    if (structs == null) continue;
                    if (structs.distanceTo(Vec3i.ZERO, DistanceMetric.CHEBYSHEV) > searchSize >> 4) continue;
                    if (!struct.canSpawn(structs.getX(), structs.getZ(), Searcher.getBiomeSource(searchStructure.getDimension(), worldSeed))) continue; // || !struct.canSpawn(structs.getX(), structs.getZ(), source1) || !struct.canSpawn(structs.getX(), structs.getZ(), source2)

                    howManyStructures++;
                    if(howManyStructures >= Collections.frequency(listReturn, searchStructure)){
                        return list;
                    }

                    //System.out.println("Found world seed " + worldSeed + " with structure seed " + structureSeed);
                    //System.out.println("The structure is at (" + struct.getX() * 16 + ", " + struct.getZ() * 16 + ")");
                }
            }
        }
        return new ArrayList<>();
    }

    // Fairly slow....
    public static void findMineshaft(int searchSize, long worldSeed, Mineshaft mineshaft) {
        ChunkRand rand = new ChunkRand();
        int count = 0;

        for(int chunkX = -searchSize >> 4; chunkX < searchSize >> 4; chunkX++){
            for(int chunkZ = -searchSize >> 4; chunkZ < searchSize >> 4; chunkZ++){
                Mineshaft.Data<?> mineshaftData = mineshaft.at(chunkX, chunkZ);
                if(!mineshaftData.testStart(worldSeed, rand))continue;
                OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_15, worldSeed);
                if(!mineshaftData.testBiome(source))continue;
                count++;
            }
        }

        System.out.println("Mineshaft: " + count);
    }

}


/*
    SISTER SEED (Much faster for seed searching)
    for(long upperBits = 0; upperBits < 1L << 16; upperBits++) {
                long worldSeed = (upperBits << 48) | structureSeed;

    Converting to a structure Seed
    for(long structureSeed = 0; structureSeed < 1L << 48; structureSeed++) {
 */
