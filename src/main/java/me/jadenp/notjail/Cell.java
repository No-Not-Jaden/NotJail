package me.jadenp.notjail;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    private final Location spawnLocation;
    private List<Location> cellBlocks = new ArrayList<>();
    private List<Block> doors = new ArrayList<>();

    public Cell(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    private void discoverCell(){
        doors.clear();
        cellBlocks.clear();
        cellBlocks.add(spawnLocation);
        new BukkitRunnable() {
            List<Location> open = new ArrayList<>(getAdjacentBlockLocations(spawnLocation)); // potential blocks in cell
            @Override
            public void run() {
                List<Location> nextBlocks = new ArrayList<>();
                // iterate through every location in open
                for (Location location : open) {
                    // check if the location is an air block - save iron doors
                    if (!location.getBlock().getType().isAir()) {
                        if (location.getBlock().getType() == Material.IRON_DOOR && !doors.contains(location.getBlock())) {
                            doors.add(location.getBlock());
                            Block up = location.getBlock().getRelative(BlockFace.UP);
                            if (up.getType() == Material.IRON_DOOR && !doors.contains(up))
                                doors.add(up);
                            Block down = location.getBlock().getRelative(BlockFace.DOWN);
                            if (down.getType() == Material.IRON_DOOR && !doors.contains(down))
                                doors.add(down);
                        }
                        continue;
                    }
                    // check if the location isn't already in closed or nextBlocks
                    if (!cellBlocks.contains(location) && !nextBlocks.contains(location)) {
                        // add the location to closed and the adjacent locations to next blocks
                        cellBlocks.add(location);
                        nextBlocks.addAll(getAdjacentBlockLocations(location));
                    }
                }
                // set open to be next blocks
                open = nextBlocks;
                if (open.isEmpty() || cellBlocks.size() > ConfigOptions.maxCellSize)
                    this.cancel();
            }
        }.runTaskTimerAsynchronously(NotJail.getInstance(), 0, 1);

    }

    private List<Location> getAdjacentBlockLocations(Location location) {
        List<Location> blocks = new ArrayList<>();
        blocks.add(new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY(), location.getBlockZ()));
        blocks.add(new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()));
        blocks.add(new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ() - 1));
        blocks.add(new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY(), location.getBlockZ()));
        blocks.add(new Location(location.getWorld(), location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()));
        blocks.add(new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ() + 1));
        return blocks;
    }

}
