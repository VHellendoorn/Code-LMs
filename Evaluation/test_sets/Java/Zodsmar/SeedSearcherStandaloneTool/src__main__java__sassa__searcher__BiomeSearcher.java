package sassa.searcher;

import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.rand.seed.WorldSeed;
import sassa.util.Singleton;

import java.util.ArrayList;
import java.util.Collection;

public class BiomeSearcher {

    public static ArrayList<Biome> findBiome(int searchSize, long worldSeed, Collection<Biome> biomeToFind, int incrementer) {
        // Since I'm deleting out of the array to make sure we are checking everytime properly I am shallow copying the array
        ArrayList<Biome> biomesToFindCopy = new ArrayList<>(biomeToFind);
        //BiomeSource source = Searcher.getBiomeSource(dimension, worldSeed);
        //BiomeSource source = Searcher.getBiomeSource("OVERWORLD", worldSeed);
        BiomeSource source = Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed);
        BiomeSource source1 = Searcher.getBiomeSource(Dimension.NETHER, worldSeed);
        BiomeSource source2 = Searcher.getBiomeSource(Dimension.END, worldSeed);

        int xSize = 0;
        int zSize = 0;
        if(Singleton.getInstance().getSpawnPoint().isSelected()) {
            xSize = Integer.parseInt(Singleton.getInstance().getXCoordSpawn().getText());
            zSize = Integer.parseInt(Singleton.getInstance().getZCoordSpawn().getText());
        }
        for(int x = -searchSize + xSize; x < searchSize + xSize; x += incrementer) {
            for(int z = -searchSize + zSize; z < searchSize + zSize; z += incrementer) {

                biomesToFindCopy.remove(source.getBiome(x, 0, z));
                biomesToFindCopy.remove(source1.getBiome(x, 0, z));
                biomesToFindCopy.remove(source2.getBiome(x, 0, z));

                if(biomesToFindCopy.isEmpty()) {
                    //System.out.format("Found world seed %d (Shadow %d)\n", worldSeed, WorldSeed.getShadowSeed(worldSeed));
                    return biomesToFindCopy;
                }
            }
        }

        return biomesToFindCopy;
    }

    public static ArrayList<Biome> findBiomeEx(int searchSize, long worldSeed, Collection<Biome> biomeToFind, int incrementer) {
        // Since I'm deleting out of the array to make sure we are checking everytime properly I am shallow copying the array
        ArrayList<Biome> biomesToFindCopy = new ArrayList<>(biomeToFind);
        //BiomeSource source = Searcher.getBiomeSource(dimension, worldSeed);
        BiomeSource source = Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed);
        BiomeSource source1 = Searcher.getBiomeSource(Dimension.NETHER, worldSeed);
        BiomeSource source2 = Searcher.getBiomeSource(Dimension.END, worldSeed);
        int xSize = 0;
        int zSize = 0;
        if(Singleton.getInstance().getSpawnPoint().isSelected()) {
            xSize = Integer.parseInt(Singleton.getInstance().getXCoordSpawn().getText());
            zSize = Integer.parseInt(Singleton.getInstance().getZCoordSpawn().getText());
        }

        for(int i = -searchSize + xSize; i < searchSize + xSize; i += incrementer) {
            for(int j = -searchSize + zSize; j < searchSize + zSize; j += incrementer) {
                if(biomesToFindCopy.contains(source.getBiome(i, 0, j)) || biomesToFindCopy.contains(source1.getBiome(i, 0, j)) || biomesToFindCopy.contains(source2.getBiome(i, 0, j))){
                    return biomesToFindCopy;
                }
                //biomesToFindCopy.remove(source1.getBiome(i, 0, j));
                //biomesToFindCopy.remove(source2.getBiome(i, 0, j));
            }
        }

        return new ArrayList<>();
    }

    public static ArrayList<Biome.Category> findBiomeFromCategory(int searchSize, long worldSeed, Collection<Biome.Category> biomeToFind, int incrementer) {
        // Since I'm deleting out of the array to make sure we are checking everytime properly I am shallow copying the array
        ArrayList<Biome.Category> biomesToFindCopy = new ArrayList<>(biomeToFind);

        BiomeSource source = Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed);
        BiomeSource source1 = Searcher.getBiomeSource(Dimension.NETHER, worldSeed);
        BiomeSource source2 = Searcher.getBiomeSource(Dimension.END, worldSeed);
        int xSize = 0;
        int zSize = 0;
        if(Singleton.getInstance().getSpawnPoint().isSelected()) {
            xSize = Integer.parseInt(Singleton.getInstance().getXCoordSpawn().getText());
            zSize = Integer.parseInt(Singleton.getInstance().getZCoordSpawn().getText());
        }

        for(int i = -searchSize + xSize; i < searchSize + xSize; i += incrementer) {
            for(int j = -searchSize + zSize; j < searchSize + zSize; j += incrementer) {
                biomesToFindCopy.remove(source.getBiome(i, 0, j).getCategory());
                biomesToFindCopy.remove(source1.getBiome(i, 0, j).getCategory());
                biomesToFindCopy.remove(source2.getBiome(i, 0, j).getCategory());

                if(biomesToFindCopy.isEmpty()) {
                    System.out.println("Found: "+ source.getBiome(i, 0, j).getCategory() + " At: " + i +"  "+ j);
                    return biomesToFindCopy;
                }
            }
        }

        return biomesToFindCopy;
    }

    public static ArrayList<Biome.Category> findBiomeFromCategoryEx(int searchSize, long worldSeed, Collection<Biome.Category> biomeToFind, int incrementer) {
        // Since I'm deleting out of the array to make sure we are checking everytime properly I am shallow copying the array
        ArrayList<Biome.Category> biomesToFindCopy = new ArrayList<>(biomeToFind);
        BiomeSource source = Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed);
        BiomeSource source1 = Searcher.getBiomeSource(Dimension.NETHER, worldSeed);
        BiomeSource source2 = Searcher.getBiomeSource(Dimension.END, worldSeed);
        int xSize = 0;
        int zSize = 0;
        if(Singleton.getInstance().getSpawnPoint().isSelected()) {
            xSize = Integer.parseInt(Singleton.getInstance().getXCoordSpawn().getText());
            zSize = Integer.parseInt(Singleton.getInstance().getZCoordSpawn().getText());
        }

        for(int i = -searchSize + xSize; i < searchSize + xSize; i += incrementer) {
            for(int j = -searchSize + zSize; j < searchSize + zSize; j += incrementer) {
                if(biomesToFindCopy.contains(source.getBiome(i, 0, j).getCategory()) || biomesToFindCopy.contains(source1.getBiome(i, 0, j).getCategory())|| biomesToFindCopy.contains(source2.getBiome(i, 0, j).getCategory())){
                    return biomesToFindCopy;
                }
            }
        }

        return new ArrayList<>();
    }

}
