package demo;

import com.mattworzala.crafting.CraftingSupport;
import com.mattworzala.crafting.compat.VanillaRecipeLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Position;
import net.minestom.server.world.biomes.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class MainDemo {
    private static final Logger logger = LoggerFactory.getLogger(MainDemo.class);

    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        // Create the instance
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        // Set the ChunkGenerator
        instanceContainer.setChunkGenerator(new GeneratorDemo());
        // Enable the auto chunk loading (when players come close)
        instanceContainer.enableAutoChunkLoad(true);

        // Crafting
        CraftingSupport.init(true);
        int loaded = VanillaRecipeLoader.loadVanillaRecipes(Paths.get("minecraft_data/data/minecraft/recipes"));
        logger.info("Loaded {} vanilla recipes.", loaded);

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addEventCallback(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Position(0, 42, 0));
        });

        globalEventHandler.addEventCallback(PlayerSpawnEvent.class, event -> {
            event.getPlayer().setGameMode(GameMode.CREATIVE);

            event.getPlayer().getInventory().addItemStack(new ItemStack(Material.CRAFTING_TABLE, (byte) 64));
            event.getPlayer().getInventory().addItemStack(new ItemStack(Material.STONECUTTER, (byte) 64));
            event.getPlayer().getInventory().addItemStack(new ItemStack(Material.GLOWSTONE_DUST, (byte) 64));
        });

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }

    private static class GeneratorDemo implements ChunkGenerator {

        @Override
        public void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ) {
            // Set chunk blocks
            for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++)
                for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    for (byte y = 0; y < 40; y++) {
                        batch.setBlock(x, y, z, Block.STONE);
                    }
                }
        }

        @Override
        public void fillBiomes(Biome[] biomes, int chunkX, int chunkZ) {
            Arrays.fill(biomes, Biome.PLAINS);
        }

        @Override
        public List<ChunkPopulator> getPopulators() {
            return null;
        }
    }

}
