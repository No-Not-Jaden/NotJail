package me.jadenp.notjail;

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
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public class Cell {
    private String type;
    private Location p1;
    private Location p2;
    private Location spawn;
    private List<Location> cellBlocks = new ArrayList<>();
    private boolean autoCell;
    private Villager villager;
    private Plugin plugin;
    private List<Block> door = new ArrayList<>();
    private boolean hasCell = false;
    private List<Material> shortBlocks = new ArrayList<>();
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

    public Cell (String type, Location spawn, Plugin plugin, List<Material> autoCellIgnore){
        this.type = type;
        this.spawn = spawn;
        this.plugin = plugin;
        autoCell = true;
        createAutoCell(autoCellIgnore);
    }

    public boolean isAutoCell() {
        return autoCell;
    }

    public void createAutoCell(List<Material> autoCellIgnore){
        // ok so basically its still adding duplicate locations into closed
        // need to do a better method of comparing - like using vector lists, or get block
        // using vectors may decrease the processing power

        List<Vector> closed = new ArrayList<>();
        List<Vector> newOpen = new ArrayList<>();

        new BukkitRunnable(){
            int i = 0;
            List<Vector> open = new ArrayList<>();

            @Override
            public void run() {
                if (i == 0){
                    open.add(new Vector(spawn.getBlockX(), spawn.getY(), spawn.getBlockZ()));
                }
                if (open.size() > 0){
                    for (Vector location : open){
                        Block block = new Location(spawn.getWorld(), location.getX(), location.getY(), location.getZ()).getBlock();
                        if (block.getType().isAir() || block.isLiquid() || block.isPassable() || autoCellIgnore.contains(block.getType())){
                            closed.add(location);
                            Vector location1 = new Vector( location.getX() + 1, location.getY(), location.getZ());
                            Vector location2 = new Vector( location.getX() - 1, location.getY(), location.getZ());
                            Vector location3 = new Vector( location.getX(), location.getY() + 1, location.getZ());
                            Vector location4 = new Vector( location.getX(), location.getY() - 1, location.getZ());
                            Vector location5 = new Vector( location.getX(), location.getY(), location.getZ() + 1);
                            Vector location6 = new Vector( location.getX(), location.getY(), location.getZ() - 1);
                            if (!listMatch(closed, location1) && !listMatch(open, location1) && !listMatch(newOpen, location1)){
                                newOpen.add(location1);
                            }
                            if (!listMatch(closed, location2) && !listMatch(open, location2) && !listMatch(newOpen, location2)){
                                newOpen.add(location2);
                            }
                            if (!listMatch(closed, location3) && !listMatch(open, location3) && !listMatch(newOpen, location3)){
                                newOpen.add(location3);
                            }
                            if (!listMatch(closed, location4) && !listMatch(open, location4) && !listMatch(newOpen, location4)){
                                newOpen.add(location4);
                            }
                            if (!listMatch(closed, location5) && !listMatch(open, location5) && !listMatch(newOpen, location5)){
                                newOpen.add(location5);
                            }
                            if (!listMatch(closed, location6) && !listMatch(open, location6) && !listMatch(newOpen, location6)){
                                newOpen.add(location6);
                            }

                        } else if (block.getType() == Material.IRON_DOOR){
                            if (!door.contains(block)){
                                door.add(block);
                                //Bukkit.getLogger().info("door");
                            }

                        }
                    }
                    //Bukkit.getLogger().info(i + " " + newOpen.size());
                    i++;
                    open.clear();
                    open = new ArrayList<>(newOpen);
                    newOpen.clear();
                    if (i >= 20 || open.size() > 2000){
                        //Bukkit.getLogger().info(i + " " + newOpen.size());
                        plugin.getLogger().warning("Cell at " + spawn.getX() + " " + spawn.getY() + " " + spawn.getZ() + " in " + spawn.getWorld().getName() + " could not be registered!\nAuto Cell could not find an enclosed room. (Is it small enough?)\nCreating temporary 3x3 cell.");
                        p1 = new Location(spawn.getWorld(), spawn.getX() + 1, spawn.getY() + 2, spawn.getZ() + 1);
                        p2 = new Location(spawn.getWorld(), spawn.getX() - 1, spawn.getY(), spawn.getZ() - 1);
                        door = findDoor();
                        hasCell = true;
                        this.cancel();
                    }
                } else {
                    for (Vector location : closed){
                        //Bukkit.getLogger().info(location.getX() + "x " + location.getY() + "y " + location.getZ() + "z ");
                        cellBlocks.add(new Location(spawn.getWorld(), location.getX(), location.getY(), location.getZ()));
                    }
                    for (Block block : door){
                        cellBlocks.add(block.getLocation());
                    }
                    hasCell = true;
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);



    }

    public boolean listMatch(List<Vector> locations, Vector location){
        for (Vector location1 : locations){
            if (isSameBlock(location1, location)){
                return true;
            }
        }
        return false;
    }

    public boolean isSameBlock(Vector l1, Vector l2){
        double moe = 0.3;
                //if (l1.getX() == l2.getX() && l1.getY() == l2.getY() && l1.getZ() == l2.getZ()){
                if (Math.abs(l1.getX() - l2.getX()) < moe && Math.abs(l1.getY() - l2.getY()) < moe && Math.abs(l1.getZ() - l2.getZ()) < moe){
                    return true;
                }
        return false;
    }

    // this is too bad - crashes server
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
            List<Block> tempDoors = new ArrayList<>();
            @Override
            public void run() {
                if (open.size() > 0 && runs < maxruns){
                    List<Location> newClosed = new ArrayList<>();
                    for (Location location : open) {
                        // test if they are air blocks
                        if (location.getBlock().getType().isAir()) {
                            // add to closed
                            newClosed.add(location);
                        }
                        if (location.getBlock().getType() == Material.IRON_DOOR) {
                            tempDoors.add(location.getBlock());
                        }
                    }
                    // clear out all open that were tested
                    open.clear();
                    // add blocks around new closed to open
                    for (Location location : newClosed) {
                        // test if they are in closed or newclosed already
                        List<Location> newOpen = new ArrayList<>();
                        newOpen.add(new Location(location.getWorld(), location.getX() + 1, location.getY(), location.getZ()));
                        newOpen.add(new Location(location.getWorld(), location.getX() - 1, location.getY(), location.getZ()));
                        newOpen.add(new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ()));
                        newOpen.add(new Location(location.getWorld(), location.getX(), location.getY() - 1, location.getZ()));
                        newOpen.add(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ() + 1));
                        newOpen.add(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ() - 1));
                        for (Location location1 : newOpen) {
                            if (!newClosed.contains(location1) && !closed.contains(location1)) {
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
                    door = new ArrayList<>(tempDoors);
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);


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

        if (p1 != null) {
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
            if (spawn.getWorld().equals(pLoc.getWorld())) {
                if (pLoc.distance(spawn) < 10)
                for (Location location : cellBlocks) {
                    Location blockLoc = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5);
                    double distance = Math.sqrt(Math.pow(blockLoc.getX() - pLoc.getX(), 2) + Math.pow(blockLoc.getZ() - pLoc.getZ(), 2));
                    if (distance < 1 && pLoc.getBlockY() == blockLoc.getBlockY()) {
                        return true;
                    }


                }
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

    public Villager getVillager() {
        return villager;
    }

    public boolean isVillagerAlive(){
        if (villager == null){
            return false;
        }
        return !villager.isDead();
    }

    public void markDoor(Player p){
        if (hasCell)
        for (Block block : door){
            Location location = new Location(block.getWorld(), block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
            p.playEffect(location, Effect.MOBSPAWNER_FLAMES,20);
        }
    }

    public int getNumDoors(){
        if (door.size() > 0){
            return door.size() / 2;
        }
        return 0;
    }
}
