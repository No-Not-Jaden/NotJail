package me.jadenp.notjail.old;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class Cell {
    private String type;
    private Location p1;
    private Location p2;
    private Location spawn;
    private List<Location> cellBlocks = new ArrayList<>();
    private boolean autoCell;
    private Villager villager;
    private Plugin plugin;
    private List<Block> door;
    private boolean hasCell = false;
    public Cell(String type, Location p1, Location p2, Location spawn, Plugin plugin){
        this.type = type;
        this.p1 = p1;
        this.p2 = p2;
        this.spawn = spawn;
        this.plugin = plugin;
        door = findDoor();
        autoCell = false;
        hasCell = true;
    }

    public Cell (String type, Location spawn, Plugin plugin){
        this.type = type;
        this.spawn = spawn;
        this.plugin = plugin;
        autoCell = true;
        findCellBlocks();
    }

    public boolean isAutoCell() {
        return autoCell;
    }

    public void findCellBlocks(){
        //gonna try & do this like A* pathfinding
        List<Location> open = new ArrayList<>(); // possibility that it is in cell
        List<Location> closed = new ArrayList<>(); // in cell
        Location spawnBlock = spawn.getBlock().getLocation();
        closed.add(spawnBlock);
        open.add(new Location(spawnBlock.getWorld(), spawnBlock.getX() + 1, spawnBlock.getY(), spawnBlock.getZ()));
        open.add(new Location(spawnBlock.getWorld(), spawnBlock.getX(), spawnBlock.getY(), spawnBlock.getZ() + 1));
        open.add(new Location(spawnBlock.getWorld(), spawnBlock.getX(), spawnBlock.getY() + 1, spawnBlock.getZ()));
        open.add(new Location(spawnBlock.getWorld(), spawnBlock.getX() - 1, spawnBlock.getY(), spawnBlock.getZ()));
        open.add(new Location(spawnBlock.getWorld(), spawnBlock.getX(), spawnBlock.getY(), spawnBlock.getZ() - 1));
        open.add(new Location(spawnBlock.getWorld(), spawnBlock.getX(), spawnBlock.getY() - 1, spawnBlock.getZ()));
        int maxruns = 100;
        new BukkitRunnable(){
            int runs = 0;
            @Override
            public void run() {
                if (open.size() > 0 && runs < maxruns){
                    List<Location> newClosed = new ArrayList<>();
                    for (Location location : open){
                        // test if they are air blocks
                        if (location.getBlock().getType().isAir()){
                            // add to closed
                            newClosed.add(location);
                        }
                        if (location.getBlock().getType() == Material.IRON_DOOR){
                            door.add(location.getBlock());
                        }
                    }
                    // clear out all open that were tested
                    open.clear();
                    // add blocks around new closed to open
                    for (Location location : newClosed){
                        // test if they are in closed or newclosed already
                        List<Location> newOpen = new ArrayList<>();
                        newOpen.add(new Location(location.getWorld(), location.getX() + 1, location.getY(), location.getZ()));
                        newOpen.add(new Location(location.getWorld(), location.getX() - 1, location.getY(), location.getZ()));
                        newOpen.add(new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ()));
                        newOpen.add(new Location(location.getWorld(), location.getX(), location.getY() - 1, location.getZ()));
                        newOpen.add(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ() + 1));
                        newOpen.add(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ() - 1));
                        for (Location location1: newOpen){
                            if (!newClosed.contains(location1) && !closed.contains(location1)){
                                open.add(location1);
                            }
                        }
                    }
                    runs++;
                } else {
                    this.cancel();
                    if (runs >= maxruns){
                        plugin.getLogger().warning("Cell at " + spawnBlock.getX() + " " + spawnBlock.getY() + " " + spawnBlock.getZ() + " in " + spawnBlock.getWorld() + " could not be registered!\nAuto Cell could not find an enclosed room. (Is it small enough?)\nCreating temporary 3x3 cell.");
                        p1 = new Location(spawnBlock.getWorld(), spawnBlock.getX() + 1, spawnBlock.getY() + 2, spawnBlock.getZ() + 1);
                        p2 = new Location(spawnBlock.getWorld(), spawnBlock.getX() - 1, spawnBlock.getY(), spawnBlock.getZ() - 1);
                        door = findDoor();
                    } else {
                        cellBlocks = new ArrayList<>(closed);
                    }
                    hasCell = true;
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);


    }

    public Location getP1() {
        return p1;
    }

    public Location getP2() {
        return p2;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Location getSpawn() {
        return spawn;
    }

    public boolean inCell(Location pLoc) {
        if (p1 == null) {
            if (pLoc.getWorld().equals(p1.getWorld())) {
                if (pLoc.getX() <= Math.max(p1.getX(), p2.getX()) + 1 && pLoc.getX() >= Math.min(p1.getX(), p2.getX()) - 0.5) {
                    if (pLoc.getY() <= Math.max(p1.getY(), p2.getY() + 1) && pLoc.getY() >= Math.min(p1.getY(), p2.getY()) - 0.5) {
                        if (pLoc.getZ() <= Math.max(p1.getZ(), p2.getZ()) + 1 && pLoc.getZ() >= Math.min(p1.getZ(), p2.getZ()) - 0.5) {
                            return true;
                        }
                    }
                }
            }
        } else if (cellBlocks.size() > 0){
            if (cellBlocks.contains(pLoc.getBlock().getLocation())){
                return true;
            }
        }
        return false;
    }

    public List<Block> findDoor(){
        List<Block> doors = new ArrayList<>();
        for (int x = Math.min(p1.getBlockX(),p2.getBlockX()); x <= Math.max(p1.getBlockX(),p2.getBlockX()); x++){
            for (int y = Math.min(p1.getBlockY(),p2.getBlockY()); y <= Math.max(p1.getBlockY(),p2.getBlockY()); y++){
                for (int z = Math.min(p1.getBlockZ(),p2.getBlockZ()); z <= Math.max(p1.getBlockZ(),p2.getBlockZ()); z++){
                    Location here = new Location(spawn.getWorld(), x, y, z);
                    Block block = here.getBlock();
                    if (block.getRelative(-1,0,0).getType() == Material.IRON_DOOR){
                        if (!doors.contains(block.getRelative(-1,0,0))) {
                            doors.add(block.getRelative(-1, 0, 0));
                            if (block.getRelative(-1, -1, 0).getType() == Material.IRON_DOOR) {
                                doors.add(block.getRelative(-1,-1,0));
                            } else if (block.getRelative(-1, 1, 0).getType() == Material.IRON_DOOR) {
                                doors.add(block.getRelative(-1,1,0));
                            }
                        }
                    }
                    if (block.getRelative(0,0,-1).getType() == Material.IRON_DOOR){
                        if (!doors.contains(block.getRelative(0,0,-1))) {
                            doors.add(block.getRelative(0, 0, -1));
                            if (block.getRelative(0, -1, -1).getType() == Material.IRON_DOOR) {
                                doors.add(block.getRelative(0,-1,-1));
                            } else if (block.getRelative(0, 1, -1).getType() == Material.IRON_DOOR) {
                                doors.add(block.getRelative(0,1,-1));
                            }
                        }
                    }
                    if (block.getRelative(0,0,1).getType() == Material.IRON_DOOR){
                        if (!doors.contains(block.getRelative(0,0,1))) {
                            doors.add(block.getRelative(0, 0, 1));
                            if (block.getRelative(0, -1, 1).getType() == Material.IRON_DOOR) {
                                doors.add(block.getRelative(0,-1,1));
                            } else if (block.getRelative(0, 1, 1).getType() == Material.IRON_DOOR) {
                                doors.add(block.getRelative(0,1,1));
                            }
                        }
                    }
                    if (block.getRelative(1,0,0).getType() == Material.IRON_DOOR){
                        if (!doors.contains(block.getRelative(1,0,0))) {
                            doors.add(block.getRelative(1, 0, 0));
                            if (block.getRelative(1, -1, 0).getType() == Material.IRON_DOOR) {
                                doors.add(block.getRelative(1,-1,0));
                            } else if (block.getRelative(1, 1, 0).getType() == Material.IRON_DOOR) {
                                doors.add(block.getRelative(1,1,0));
                            }
                        }
                    }
                }
            }
        }
        return doors;
    }

    public void setOpen(boolean bool){
        if (hasCell)
        if (!door.isEmpty()){
            for (Block block: door){
                /*BlockState state = block.getState();
                Door d = (Door) state.getBlockData();
                d.setOpen(bool);

                block.getState().setBlockData(d);
                // block.setBlockData(d);
                block.getState().update(true);
                block.getState().update();
*/

                BlockState blockState = block.getState();
                Openable openable = (Openable) blockState.getBlockData();
                if (openable.isOpen() != bool) {
                    openable.setOpen(bool);
                    blockState.setBlockData(openable);
                    blockState.update();

                    if (bool)
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1, 1);
                    else
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 1);
                }
            }
        }
    }

    public void addVillager(){
        if (hasCell) {
            villager = spawn.getWorld().spawn(spawn, Villager.class);
            villager.setCustomName(ChatColor.GRAY + "Prisoner #" + (int) (Math.random() * 10000));
            villager.setAbsorptionAmount(10000000);
            villager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2));
            villager.setMetadata("Prisoner", new FixedMetadataValue(plugin, true));
        }
    }

    public void setAware(boolean aware){
        if (villager != null)
            villager.setAI(aware);
    }

    public void removeVillager(){
        if (villager != null)
            villager.remove();
    }

    public void teleportVillager(Location location){
        if (villager != null)
            villager.teleport(location);
    }

    public boolean isVillagerAlive(){
        return villager != null;
    }

    public void markDoor(Player p){
        if (hasCell)
        for (Block block : door){
            Location location = new Location(block.getWorld(), block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
            p.playEffect(location, Effect.MOBSPAWNER_FLAMES,20);
        }
    }

    public int getNumDoors(){
        return door.size() / 2;
    }
}
