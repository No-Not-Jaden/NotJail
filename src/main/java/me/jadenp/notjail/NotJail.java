package me.jadenp.notjail;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public final class NotJail extends JavaPlugin implements CommandExecutor, Listener {
    /**
     * different jail cells with selection tool - X
     * holding cell - X
     * placeholder villagers in non-player filled jail cells - X
     * morning and evening roll call - X
     * lunch - X
     * lockup - X
     * free time - X
     * +5 sec if harming a villager - X
     * -5 min if attending both roll calls (max reduces sentence 75% of original sentence) -
     * +1 m if not in cell at lockup -
     * save player's items to a file & confiscate them at the beginning - X
     * config: make roll call a requirement to attend or +5 min to sentence - X
     * config: reduce sentence while offline - X
     * config: clock match in game clock or independent clock - X
     * record punishments to a file - X
     * /jail (player) (time) (cell#/open) (reason) - X
     * /jail Not_Jaden 1h open too cool to be left out in the open
     * /unjail (player) - X
     * try to get commands to preform when a player logs on if offline - X
     * regular clock works, next  time counts down. seconds go into the negative for jailtime - X
     * message to jailed player - X
     * jail some1 who is already jailed = change jail time - X
     * can  take damage - X
     * check if cell spawn is in cell before creating - X
     * <p>
     * Solitary if hurt a villager (put in after the hour is over) - X
     * Warden intimidation - glow X
     * Cell doors for all cells - not solitary X
     * Change cell restrictions, can go 1 full meter away from the center of a block - X
     * set in config if they loose hunger or health - X
     * medical bay cell - X
     * tp to medical bay if gonna die - X
     * remove all regular items - X
     * log all nonrepeating tasks - X
     * change tabcomplete for extra cells and open thing - X
     * be able to set release location - X
     * auto cleans out old users - X
     * <p>
     * change sout to logger - X
     * marker for ur cell door - X
     * close doors - X
     * punch villagers - X
     * solitary message - X
     * fix bar up top at late night sleep - X
     * sounds - X
     * <p>
     * <p>
     * 2022
     * access jailed ppl's inventories - -
     * language file
     * GUI - x
     * /jail info - to get jail #, cell size, jailed players, has door, etc - x
     * visitation - only visit in certain periods in the schedule ex: freetime - editable in config - option to let prisoners hurt the visitors for longer prison time
     * guard you can punch to go to jail
     * add prisoner action when listing prisoners - x
     * jail logs - viewable through gui
     * /jail (player) then gui pops up - missing arguments pop up gui - x
     * cell info with no cell sends message - x
     * villagers dont spawn - make config option - x
     * option in config to let players interact, break, or place certain blocks -
     * remove or condense uuid when showing - some inventory click events may check for the uuid though - x
     * /jail list prisoners kinda whack - x
     * make name the actual name instead of whatever was typed in /jail player - x
     * cant break allowed blocks x
     * villager doesnt dissapear when logging in jailed x
     * make autocell safer  for accidental open block x
     * /jail inv not working? also open and # pop up as args x
     * jain inv wont let u move stuff x
     * tab complete for unjail - x
     * short blocks - x
     * /jail (player) gets error - x
     * make animals not tp - x
     * add commands to do when jailled/unjailled - x
     * double tp option to stop /back - x
     * few changes to make server run better - x
     *
     * command doesnt work with full args to jail - x
     * make animals sit when jailed - x
     *
     *
     * send to solitary is wrong msg -
     * punching other prisoners doesnt increase ur time - config option? -
     */

    DateFormat simple = new SimpleDateFormat("dd MMM yyyy HH:mm");


    // prefix for messages
    public String prefix = ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + "Not" + ChatColor.AQUA + "Jail" + ChatColor.GRAY + "]" + ChatColor.DARK_GRAY + " >> ";

    // config files
    public File cells = new File(this.getDataFolder() + File.separator + "cells.yml");
    public File jailedInv = new File(this.getDataFolder() + File.separator + "inventories.yml");
    public File logFolder = new File(this.getDataFolder() + File.separator + "log");
    public File jails = new File(this.getDataFolder() + File.separator + "jails");
    public File prisoners = new File(this.getDataFolder() + File.separator + "prisoners.yml");
    public File autoCellIgnore = new File(this.getDataFolder() + File.separator + "autocell_ignore.txt");

    // different cells - really should make children classes for these
    public List<Cell> cellList = new ArrayList<>();
    public List<Cell> rollCallList = new ArrayList<>();
    public List<Cell> holdingCellList = new ArrayList<>();
    public List<Cell> solitaryCellList = new ArrayList<>();
    public List<Cell> medicalBayList = new ArrayList<>();

    // config options
    public String wardenName; // who to make glow
    public boolean requireRollCall;
    public boolean serveOffline;
    public boolean independentClock;
    public boolean hunger; // deplete hunger in jail
    public boolean hurt;
    public int releaseLocation;
    public List<String> presetJailReasons = new ArrayList<>();
    public boolean fillVillager;
    public List<String> jailCommands = new ArrayList<>();
    public List<String> unjailCommands = new ArrayList<>();
    public boolean doubleTeleport;
    public boolean punishCombat;

    // lists for jailed ppl
    public List<Prisoner> prisonerList = new ArrayList<>(); // online jailed ppl
    public List<String> jailedUUID = new ArrayList<>(); // everyone jailed
    public Map<String, ItemStack[]> inventories = new HashMap<>(); // saved inventories for jailed ppl
    public Map<String, ItemStack[]> armor = new HashMap<>(); // saved armor for jailed ppl
    public Map<String, ItemStack[]> extra = new HashMap<>();

    // making cell stuff
    public Map<String, Location[]> selectedPoints = new HashMap<>();

    // schedule stuff
    public long clock; // prison time in ticks
    public String nextTimeSlot; // name for next time slot
    public long nextTime; // time when next time slot starts
    public long thisTime; // time when this time slot started
    public String lastTimeSlot; // actually name for this time slot - only becomes last time slot when slot changes
    public List<String> schedule = new ArrayList<>(); // name of all time slots

    // logging stuff
    public Date now = new Date();
    public SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    public SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");
    public SimpleDateFormat formatAll = new SimpleDateFormat("dd-MM-yyyy--HH-mm-ss");
    public List<String> logText = new ArrayList<>();
    File today = new File(logFolder + File.separator + formatDate.format(now) + ".txt");
    Map<String, String> loggedPlayers = new HashMap<>();

    // some other variables
    public int numRolLCall; // how many roll calls there are in a day
    public boolean wardenNearCell; // if warden is near the prison
    public Location presetRelease; // where prisoners are released (if enabled)
    public Inventory menuInv; // menu gui - gui menu when doing /jail
    public Inventory chooseList; // choose which list to view
    public Inventory createCell; // which cell to create
    // these are all inventories to choose to create a cell with autocell or wand
    public Inventory createRegularCell;
    public Inventory createSolitaryCell;
    public Inventory createHoldingCell;
    public Inventory createMedicalBay;
    public Inventory createRollCall;
    public Inventory removeCell; // remove cellID or from list

    public Items item = new Items(); // storing items

    // prisoner perms
    public List<Material> allowedBreak = new ArrayList<>();
    public List<Material> allowedPlace = new ArrayList<>();
    public List<Material> allowedInteract = new ArrayList<>();

    // what is ignored in autocell search
    List<Material> autoCellIgnoreList = new ArrayList<>();

    List<Prisoner> onlinePrisoners = new ArrayList<>();

    Location prisonCenter;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // initialize commands & events
        Objects.requireNonNull(this.getCommand("notjail")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("unnotjail")).setExecutor(this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getLogger().info("Registered events and commands");
        // create files if they are missing
        this.saveDefaultConfig();
        if (!autoCellIgnore.exists()){
            saveResource("autocell_ignore.txt", false);
        }
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
        if (!cells.exists()) {
            try {
                cells.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!prisoners.exists()) {
            try {
                prisoners.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!jailedInv.exists()) {
            try {
                jailedInv.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!jails.exists()) {
            jails.mkdir();
        }
        // this is creating the log file for today
        if (!today.exists()) {
            try {
                today.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // if there is already a file, get all the logs from that file & add to logText list
            try {
                Scanner scanner = new Scanner(today);
                while (scanner.hasNextLine()) {
                    String data = scanner.nextLine();
                    logText.add(data);
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        Bukkit.getLogger().info("Loaded files");

        // load all cells
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(cells);
        int j = 1;
        while (configuration.getString(j + ".type") != null) {
            if (Objects.requireNonNull(configuration.getString(j + ".type")).equalsIgnoreCase("holding")) {
                if (configuration.getBoolean(j + ".auto-cell")) {
                    holdingCellList.add(new Cell(configuration.getString(j + ".type"), configuration.getLocation(j + ".spawn"), this, autoCellIgnoreList));
                } else {
                    holdingCellList.add(new Cell(configuration.getString(j + ".type"), configuration.getLocation(j + ".p1"), configuration.getLocation(j + ".p2"), configuration.getLocation(j + ".spawn"), this));
                }
            } else if (Objects.requireNonNull(configuration.getString(j + ".type")).equalsIgnoreCase("rollCall")) {
                if (configuration.getBoolean(j + ".auto-cell")) {
                    rollCallList.add(new Cell(configuration.getString(j + ".type"), configuration.getLocation(j + ".spawn"), this, autoCellIgnoreList));
                } else {
                    rollCallList.add(new Cell(configuration.getString(j + ".type"), configuration.getLocation(j + ".p1"), configuration.getLocation(j + ".p2"), configuration.getLocation(j + ".spawn"), this));
                }
            } else if (Objects.requireNonNull(configuration.getString(j + ".type")).equalsIgnoreCase("solitary")) {
                if (configuration.getBoolean(j + ".auto-cell")) {
                    solitaryCellList.add(new Cell(configuration.getString(j + ".type"), configuration.getLocation(j + ".spawn"), this, autoCellIgnoreList));
                } else {
                    solitaryCellList.add(new Cell(configuration.getString(j + ".type"), configuration.getLocation(j + ".p1"), configuration.getLocation(j + ".p2"), configuration.getLocation(j + ".spawn"), this));
                }
            } else if (Objects.requireNonNull(configuration.getString(j + ".type")).equalsIgnoreCase("medical")) {
                if (configuration.getBoolean(j + ".auto-cell")) {
                    medicalBayList.add(new Cell(configuration.getString(j + ".type"), configuration.getLocation(j + ".spawn"), this, autoCellIgnoreList ));
                } else {
                    medicalBayList.add(new Cell(configuration.getString(j + ".type"), configuration.getLocation(j + ".p1"), configuration.getLocation(j + ".p2"), configuration.getLocation(j + ".spawn"), this));
                }
            } else {
                if (configuration.getBoolean(j + ".auto-cell")) {
                    cellList.add(new Cell(configuration.getString(j + ".type"), configuration.getLocation(j + ".spawn"), this, autoCellIgnoreList));
                } else {
                    cellList.add(new Cell(configuration.getString(j + ".type"), configuration.getLocation(j + ".p1"), configuration.getLocation(j + ".p2"), configuration.getLocation(j + ".spawn"), this));
                }
            }
            j++;
        }

        Bukkit.getLogger().info(prefix + ChatColor.YELLOW + "Registered " + cellList.size() + " regular cells");
        Bukkit.getLogger().info(prefix + ChatColor.YELLOW + holdingCellList.size() + " holding cells");
        Bukkit.getLogger().info(prefix + ChatColor.YELLOW + rollCallList.size() + " roll call areas");
        Bukkit.getLogger().info(prefix + ChatColor.YELLOW + solitaryCellList.size() + " solitary cells");
        Bukkit.getLogger().info(prefix + ChatColor.YELLOW + medicalBayList.size() + " medical bays");

        presetRelease = configuration.getLocation("preset-release");

        logText.add("-");
        logText.add("Registered " + cellList.size() + " regular cells");
        logText.add(holdingCellList.size() + " holding cells");
        logText.add(rollCallList.size() + " roll call areas");
        logText.add(solitaryCellList.size() + " solitary cells");
        logText.add(medicalBayList.size() + " medical bays");

        loadConfig();
        // initializing some variables
        clock = 0;
        lastTimeSlot = "";
        wardenNearCell = false;

        // get all prisoners in files and add to list
        YamlConfiguration configuration0 = YamlConfiguration.loadConfiguration(prisoners);
        prisonerList.clear(); // dont need this :eye_roll:
        jailedUUID = configuration0.getStringList("jailed-uuid");
        int y = 1;
        while (configuration0.getString(y + ".action") != null) {
            prisonerList.add(new Prisoner(configuration0.getString(y + ".action"), UUID.fromString(Objects.requireNonNull(configuration0.getString(y + ".uuid"))), configuration0.getString(y + ".name"), configuration0.getLong(y + ".jail-time"), configuration0.getLong(y + ".sentence-length"), configuration0.getInt(y + ".cell"), configuration0.getString(y + ".reason"), configuration0.getLocation(y + ".last-location"), configuration0.getInt(y + ".tries"), this));
            y++;
        }
        int k = 1;
        while (configuration0.getString("logged-players." + k + ".name") != null){
            loggedPlayers.put(configuration0.getString("logged-players." + k + ".name"), configuration0.getString("logged-players." + k + ".uuid"));
            k++;
        }
        // get all inventory and armor contents in files and add to list
        YamlConfiguration configurationI = YamlConfiguration.loadConfiguration(jailedInv);
        int w = 1;
        while (configurationI.getString(w + ".uuid") != null) {
            ItemStack[] contents = new ItemStack[configurationI.getInt(w + ".key.length")];
            for (int i = 0; i < configurationI.getInt(w + ".key.length"); i++) {
                if (configurationI.getItemStack(w + ".items." + i) == null) continue;
                contents[i] = configurationI.getItemStack(w + ".items." + i);
            }
            ItemStack[] armorContents = new ItemStack[configurationI.getInt(w + ".key.length-a")];
            for (int i = 0; i < configurationI.getInt(w + ".key.length-a"); i++) {
                if (configurationI.getItemStack(w + ".armor." + i) == null) continue;
                armorContents[i] = configurationI.getItemStack(w + ".armor." + i);
            }
            ItemStack[] extraContents = new ItemStack[configurationI.getInt(w + ".key.length-b")];
            for (int i = 0; i < configurationI.getInt(w + ".key.length-b"); i++) {
                if (configurationI.getItemStack(w + ".extra." + i) == null) continue;
                extraContents[i] = configurationI.getItemStack(w + ".extra." + i);
            }
            inventories.put(configurationI.getString(w + ".uuid"), contents);
            armor.put(configurationI.getString(w + ".uuid"), armorContents);
            extra.put(configurationI.getString(w + ".uuid"), extraContents);
            w++;
        }

        // create some preset inventories
        // main menu
        menuInv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "            " + ChatColor.RED + "" + ChatColor.BOLD + " Jail Menu " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "            ");
        ItemStack[] contents = menuInv.getContents();
        Arrays.fill(contents, item.get("fill"));
        contents[4] = item.get("prisInv");
        contents[10] = item.get("jailPlayer");
        contents[11] = item.get("createCell");
        contents[12] = item.get("removeCell");
        contents[13] = item.get("wand");
        contents[14] = item.get("unjail");
        contents[15] = item.get("info");
        contents[16] = item.get("solitary");
        contents[26] = item.get("reload");
        contents[22] = item.get("exit");
        contents[18] = item.get("list");
        menuInv.setContents(contents);

        // choose which list to display
        chooseList = Bukkit.createInventory(null, 27, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "            " + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + " Jail List " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "            ");
        contents = chooseList.getContents();
        Arrays.fill(contents, item.get("fill"));
        contents[12] = item.get("listPrisoners");
        contents[14] = item.get("listCells");
        contents[22] = item.get("exit");
        chooseList.setContents(contents);

        // choose which cell to create
        createCell = Bukkit.createInventory(null, 27, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Cell " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           ");
        contents = createCell.getContents();
        Arrays.fill(contents, item.get("fill"));
        contents[11] = item.get("regularCell");
        contents[12] = item.get("solitaryCell");
        contents[13] = item.get("holdingCell");
        contents[14] = item.get("medicalBay");
        contents[15] = item.get("rollCall");
        contents[21] = item.get("exit");
        contents[23] = item.get("returnToMenu");
        createCell.setContents(contents);

        // auto cell or selected points
        createHoldingCell = Bukkit.createInventory(null, 27, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Holding Cell " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       ");
        contents = createHoldingCell.getContents();
        Arrays.fill(contents, item.get("fill"));
        contents[12] = item.get("useSelectedPoints");
        contents[14] = item.get("useAutoCell");
        contents[21] = item.get("exit");
        contents[23] = item.get("returnToMenu");
        createHoldingCell.setContents(contents);

        createMedicalBay = Bukkit.createInventory(null, 27, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "     " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Medical Bay " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "      ");
        contents = createMedicalBay.getContents();
        Arrays.fill(contents, item.get("fill"));
        contents[12] = item.get("useSelectedPoints");
        contents[14] = item.get("useAutoCell");
        contents[21] = item.get("exit");
        contents[23] = item.get("returnToMenu");
        createMedicalBay.setContents(contents);

        createRegularCell = Bukkit.createInventory(null, 27, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Regular Cell " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       ");
        contents = createRegularCell.getContents();
        Arrays.fill(contents, item.get("fill"));
        contents[12] = item.get("useSelectedPoints");
        contents[14] = item.get("useAutoCell");
        contents[21] = item.get("exit");
        contents[23] = item.get("returnToMenu");
        createRegularCell.setContents(contents);

        createRollCall = Bukkit.createInventory(null, 27, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "         " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Roll Call " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "        ");
        contents = createRollCall.getContents();
        Arrays.fill(contents, item.get("fill"));
        contents[12] = item.get("useSelectedPoints");
        contents[14] = item.get("useAutoCell");
        contents[21] = item.get("exit");
        contents[23] = item.get("returnToMenu");
        createRollCall.setContents(contents);

        createSolitaryCell = Bukkit.createInventory(null, 27, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Solitary Cell " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "      ");
        contents = createSolitaryCell.getContents();
        Arrays.fill(contents, item.get("fill"));
        contents[12] = item.get("useSelectedPoints");
        contents[14] = item.get("useAutoCell");
        contents[21] = item.get("exit");
        contents[23] = item.get("returnToMenu");
        createSolitaryCell.setContents(contents);

        removeCell = Bukkit.createInventory(null, 27, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           " + ChatColor.GOLD + "" + ChatColor.BOLD + " Remove Cell " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           ");
        contents = removeCell.getContents();
        Arrays.fill(contents, item.get("fill"));
        contents[12] = item.get("removeFromList");
        contents[14] = item.get("removeFromHere");
        contents[21] = item.get("exit");
        contents[23] = item.get("returnToMenu");
        removeCell.setContents(contents);

        int xAvg = 0;
        int yAvg = 0;
        int zAvg = 0;
        for (Cell cell : cellList){
            // calculate center of prison
            xAvg += cell.getSpawn().getBlockX();
            yAvg += cell.getSpawn().getBlockY();
            zAvg += cell.getSpawn().getBlockZ();
        }

        if (cellList.size() > 0){
            xAvg = xAvg / cellList.size();
            yAvg = yAvg / cellList.size();
            zAvg = zAvg / cellList.size();
            prisonCenter = new Location(cellList.get(0).getSpawn().getWorld(), xAvg, yAvg, zAvg);
        }

        // fill cells with villagers
        new BukkitRunnable(){
            @Override
            public void run() {
                for (Cell cell : cellList){
                    if (fillVillager)
                        cell.addVillager();
                }
            }
        }.runTaskLater(this, 50L);



        new BukkitRunnable() {
            @Override
            public void run() {
                // runs clock
                if (onlinePrisoners.size() > 0) {
                    if (independentClock) {
                        clock += 20;
                        if (clock >= 24000)
                            clock = 0;
                    } else {
                        if (cellList.isEmpty()) {
                            clock = Bukkit.getWorlds().get(0).getTime();
                        } else {
                            clock = Objects.requireNonNull(cellList.get(0).getSpawn().getWorld()).getTime();
                        }
                    }


                    // calculate time slot - gotta by sync cuz changing blocks
                    String timeSlot;
                    if (clock < 4000 || clock >= 20000) {
                        timeSlot = "Sleep";
                    } else {
                        timeSlot = schedule.get(((int) (clock - 4000) / 1000));
                    }
                    if (!lastTimeSlot.equalsIgnoreCase(timeSlot)) {
                        lastTimeSlot = timeSlot;
                        thisTime = clock;
                        // open cells and give villagers awareness in day
                        for (Cell cell : cellList) {
                            if (cell.isVillagerAlive()) {
                                cell.setAware(!timeSlot.equalsIgnoreCase("lockup") && !timeSlot.equalsIgnoreCase("roll call"));
                            }

                            if (timeSlot.equalsIgnoreCase("lockup") || timeSlot.equalsIgnoreCase("sleep")) {
                                if (cell.isVillagerAlive()) {
                                    cell.teleportVillager(cell.getSpawn());
                                }
                                cell.setOpen(false);
                            } else {
                                cell.setOpen(true);
                            }
                        }
                        // open cells in day
                        for (Cell cell : holdingCellList) {
                            cell.setOpen(!timeSlot.equalsIgnoreCase("lockup") && !timeSlot.equalsIgnoreCase("sleep"));
                        }
                        for (Cell cell : rollCallList) {
                            cell.setOpen(!timeSlot.equalsIgnoreCase("lockup") && !timeSlot.equalsIgnoreCase("sleep"));
                        }
                        for (Cell cell : medicalBayList) {
                            cell.setOpen(!timeSlot.equalsIgnoreCase("lockup") && !timeSlot.equalsIgnoreCase("sleep"));
                        }
                    }
                    // calculate next time slot
                    nextTimeSlot = "Sleep";
                    nextTime = 20000;
                    if (timeSlot.equalsIgnoreCase("Sleep")) {
                        nextTimeSlot = schedule.get(0);
                        nextTime = 4000;
                    } else {
                        for (int i = ((int) (clock - 4000) / 1000); i < schedule.size(); i++) {
                            if (!(schedule.get(i).equalsIgnoreCase(timeSlot))) {

                                nextTimeSlot = schedule.get(i);
                                nextTime = ((i) * 1000L) + 4000;

                                break;
                            }
                        }
                    }
                    for (Prisoner prisoner : onlinePrisoners) {
                        prisoner.update(timeSlot);
                    }
                }
            }
        }.runTaskTimer(this, 100L, 20L);

        new BukkitRunnable(){
            @Override
            public void run() {
                if (onlinePrisoners.size() > 0) {
                    // check if warden is online and close to cell
                    wardenNearCell = false;
                    if (isWardenOnline()) {
                        if (prisonCenter != null) {
                            Player warden = Bukkit.getPlayer(wardenName);
                            assert warden != null;
                            if (warden.getWorld().equals(prisonCenter.getWorld())) {
                                if (warden.getLocation().distance(prisonCenter) < 200) {
                                    wardenNearCell = true;
                                }
                            }
                        }
                        // make warden glow red - needs to be in synchronous thread if using effects
                        if (wardenNearCell) {
                            Objects.requireNonNull(Bukkit.getPlayer(wardenName)).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 0));
                        }
                    }
                    // check who needs to be unjailed
                    Iterator<Prisoner> iterator = onlinePrisoners.iterator();
                    while (iterator.hasNext()) {
                        Prisoner prisoner = iterator.next();
                        Player p = prisoner.getPlayer();
                        if (p != null) {
                            if (prisoner.getAction().equalsIgnoreCase("unJail")) {
                                // unjail
                                for (String str : unjailCommands) {
                                    while (str.contains("{player}")) {
                                        str = str.replace("{player}", p.getName());
                                    }
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str);
                                }
                                if (!prisoner.getRCell().isVillagerAlive() && fillVillager) {
                                    prisoner.getRCell().addVillager();
                                }
                                if (releaseLocation == 0) {
                                    p.teleport(Objects.requireNonNull(prisoner.getRCell().getSpawn().getWorld()).getSpawnLocation());
                                    if (doubleTeleport) {
                                        p.teleport(Objects.requireNonNull(prisoner.getRCell().getSpawn().getWorld()).getSpawnLocation());
                                    }
                                } else if (releaseLocation == 1) {
                                    if (prisoner.getLastLocation().getWorld() != null) {
                                        p.teleport(prisoner.getLastLocation());
                                        if (doubleTeleport) {
                                            p.teleport(prisoner.getLastLocation());
                                        }
                                    } else {
                                        p.teleport(Objects.requireNonNull(prisoner.getRCell().getSpawn().getWorld()).getSpawnLocation());
                                        Bukkit.getLogger().info(p.getName() + " last known location is null. Teleporting to cell world spawn.");
                                        if (doubleTeleport) {
                                            p.teleport(Objects.requireNonNull(prisoner.getRCell().getSpawn().getWorld()).getSpawnLocation());
                                        }
                                    }
                                } else if (releaseLocation == 2) {
                                    if (presetRelease != null) {
                                        p.teleport(presetRelease);
                                        if (doubleTeleport) {
                                            p.teleport(presetRelease);
                                        }
                                    } else {
                                        p.teleport(Objects.requireNonNull(prisoner.getRCell().getSpawn().getWorld()).getSpawnLocation());
                                        Bukkit.getLogger().info("Preset release location is null, please reset it or change it in the config.");
                                        if (doubleTeleport) {
                                            p.teleport(Objects.requireNonNull(prisoner.getRCell().getSpawn().getWorld()).getSpawnLocation());
                                        }
                                    }
                                } else {
                                    p.teleport(Objects.requireNonNull(prisoner.getRCell().getSpawn().getWorld()).getSpawnLocation());
                                    if (doubleTeleport) {
                                        p.teleport(Objects.requireNonNull(prisoner.getRCell().getSpawn().getWorld()).getSpawnLocation());
                                    }
                                }
                                prisoner.removeBar();
                                p.sendMessage(ChatColor.YELLOW + "You have been released from jail.");
                                loadSavedInventory(p);
                                p.updateInventory();
                                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    if (player.hasPermission("notjail.admin")) {
                                        player.sendMessage(prefix + ChatColor.GOLD + p.getName() + ChatColor.YELLOW + " has been released from jail.");
                                    }
                                }
                                logText.add("[" + format.format(now) + "] " + p.getName() + " has been released from jail.");
                                jailedUUID.remove(prisoner.getUuid().toString());
                                prisonerList.remove(prisoner);
                                iterator.remove();

                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 110L, 100L);


        // autosave every 5 min
        new BukkitRunnable() {
            @Override
            public void run() {
                saveData();
            }
        }.runTaskTimer(this, 36000, 36000);
    }

    public void loadConfig() {
        // just getting settings from config
        this.reloadConfig();
        requireRollCall = this.getConfig().getBoolean("require-roll-call");
        serveOffline = this.getConfig().getBoolean("serve-offline");
        independentClock = this.getConfig().getBoolean("independent-clock");
        wardenName = this.getConfig().getString("warden");
        hunger = this.getConfig().getBoolean("hunger");
        hurt = this.getConfig().getBoolean("hurt");
        releaseLocation = this.getConfig().getInt("release-location");
        presetJailReasons = this.getConfig().getStringList("preset-jail-reasons");
        fillVillager = this.getConfig().getBoolean("fill-empty-cells");
        jailCommands = this.getConfig().getStringList("jail-commands");
        unjailCommands = this.getConfig().getStringList("unjail-commands");
        doubleTeleport = this.getConfig().getBoolean("double-teleport");
        punishCombat = this.getConfig().getBoolean("punish-combat");

        // adding some stuff to log
        logText.add("[" + format.format(now) + "] " + "Loading config...");
        logText.add("Schedule:");

        // set up schedule & how many roll calls there are
        schedule = this.getConfig().getStringList("schedule");
        for (String s : schedule) {
            logText.add(s);
            if (s.equalsIgnoreCase("Roll Call"))
                numRolLCall++;
        }

        allowedBreak.clear();
        allowedPlace.clear();
        allowedInteract.clear();

        List<String> breakMaterials = this.getConfig().getStringList("allowed-break");
        for (String material : breakMaterials){
            Material mat = Material.getMaterial(material.toUpperCase(Locale.ROOT));
            if (mat != null){
                allowedBreak.add(mat);
            } else {
                Bukkit.getLogger().warning("Could not find the material \"" + material + "\"");
            }
        }

        List<String> placeMaterials = this.getConfig().getStringList("allowed-place");
        for (String material : placeMaterials){
            Material mat = Material.getMaterial(material.toUpperCase(Locale.ROOT));
            if (mat != null){
                allowedPlace.add(mat);
            } else {
                Bukkit.getLogger().warning("Could not find the material \"" + material + "\"");
            }
        }

        List<String> interactMaterials = this.getConfig().getStringList("allowed-interact");
        for (String material : interactMaterials){
            Material mat = Material.getMaterial(material.toUpperCase(Locale.ROOT));
            if (mat != null){
                allowedInteract.add(mat);
            } else {
                Bukkit.getLogger().warning("Could not find the material \"" + material + "\"");
            }
        }

        autoCellIgnoreList.clear();

        try {
            Scanner scanner = new Scanner(autoCellIgnore);
            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                Material mat = Material.getMaterial(data.toUpperCase(Locale.ROOT));
                if (mat != null){
                    autoCellIgnoreList.add(mat);
                } else {
                    Bukkit.getLogger().warning("Could not find the material \"" + data + "\"");
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (!fillVillager){
            for (Cell cell : cellList){
                if (cell.isVillagerAlive()){
                    cell.removeVillager();
                }
            }
        }
    }

    public void saveData(){
        // save files
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("preset-release", presetRelease);
        int i = 1;
        for (Cell cell : cellList) {
            if (cell.isAutoCell()) {
                configuration.set(i + ".auto-cell", true);
            } else {
                configuration.set(i + ".auto-cell", false);
                configuration.set(i + ".p1", cell.getP1());
                configuration.set(i + ".p2", cell.getP2());
            }
            configuration.set(i + ".type", cell.getType());
            configuration.set(i + ".spawn", cell.getSpawn());
            i++;
            cell.removeVillager();
        }
        for (Cell cell : holdingCellList) {
            if (cell.isAutoCell()) {
                configuration.set(i + ".auto-cell", true);
            } else {
                configuration.set(i + ".auto-cell", false);
                configuration.set(i + ".p1", cell.getP1());
                configuration.set(i + ".p2", cell.getP2());
            }
            configuration.set(i + ".type", cell.getType());
            configuration.set(i + ".spawn", cell.getSpawn());
            i++;
        }
        for (Cell cell : rollCallList) {
            if (cell.isAutoCell()) {
                configuration.set(i + ".auto-cell", true);
            } else {
                configuration.set(i + ".auto-cell", false);
                configuration.set(i + ".p1", cell.getP1());
                configuration.set(i + ".p2", cell.getP2());
            }
            configuration.set(i + ".type", cell.getType());
            configuration.set(i + ".spawn", cell.getSpawn());
            i++;
        }
        for (Cell cell : solitaryCellList) {
            if (cell.isAutoCell()) {
                configuration.set(i + ".auto-cell", true);
            } else {
                configuration.set(i + ".auto-cell", false);
                configuration.set(i + ".p1", cell.getP1());
                configuration.set(i + ".p2", cell.getP2());
            }
            configuration.set(i + ".type", cell.getType());
            configuration.set(i + ".spawn", cell.getSpawn());
            i++;
        }
        for (Cell cell : medicalBayList) {
            if (cell.isAutoCell()) {
                configuration.set(i + ".auto-cell", true);
            } else {
                configuration.set(i + ".auto-cell", false);
                configuration.set(i + ".p1", cell.getP1());
                configuration.set(i + ".p2", cell.getP2());
            }
            configuration.set(i + ".type", cell.getType());
            configuration.set(i + ".spawn", cell.getSpawn());
            i++;
        }
        try {
            configuration.save(cells);
        } catch (IOException e) {
            e.printStackTrace();
        }
        YamlConfiguration configuration1 = new YamlConfiguration();
        configuration1.set("jailed-uuid", jailedUUID);
        int y = 1;
        for (Prisoner prisoner : prisonerList) {
            configuration1.set(y + ".action", prisoner.getAction());
            configuration1.set(y + ".uuid", prisoner.getUuid().toString());
            configuration1.set(y + ".name", prisoner.getName());
            configuration1.set(y + ".jail-time", prisoner.getJailTime());
            configuration1.set(y + ".sentence-length", prisoner.getSentenceLength());
            configuration1.set(y + ".cell", prisoner.getCell());
            configuration1.set(y + ".reason", prisoner.getReason());
            configuration1.set(y + ".last-location", prisoner.getLastLocation());
            configuration1.set(y + ".tries", prisoner.getTries());
            y++;
        }
        int l = 1;
        for (Map.Entry<String, String> entry : loggedPlayers.entrySet()){
            configuration1.set("logged-players." + l + ".name", entry.getKey());
            configuration1.set("logged-players." + l + ".uuid", entry.getValue());
            l++;
        }
        try {
            configuration1.save(prisoners);
        } catch (IOException e) {
            e.printStackTrace();
        }
        YamlConfiguration configuration2 = new YamlConfiguration();
        int w = 1;
        for (Map.Entry<String, ItemStack[]> mapElement : inventories.entrySet()) {
            configuration2.set(w + ".uuid", mapElement.getKey());
            int itemAmount = 0;
            ItemStack[] contents = mapElement.getValue();
            configuration2.set(w + ".key.length", contents.length);
            for (int k = 0; k < contents.length; k++) {
                if (contents[k] == null) continue;
                configuration2.set(w + ".items." + k, contents[k]);
                itemAmount++;
            }
            configuration2.set(w + ".key.amount", itemAmount);

            itemAmount = 0;
            ItemStack[] armorContents = (armor.get(mapElement.getKey()));
            configuration2.set(w + ".key.length-a", armorContents.length);
            for (int k = 0; k < armorContents.length; k++) {
                if (armorContents[k] == null) continue;
                configuration2.set(w + ".armor." + k, armorContents[k]);
                itemAmount++;
            }
            configuration2.set(w + ".key.amount-a", itemAmount);

            itemAmount = 0;
            ItemStack[] extraContents = (extra.get(mapElement.getKey()));
            configuration2.set(w + ".key.length-b", extraContents.length);
            for (int k = 0; k < extraContents.length; k++) {
                if (extraContents[k] == null) continue;
                configuration2.set(w + ".extra." + k, extraContents[k]);
                itemAmount++;
            }
            configuration2.set(w + ".key.amount-b", itemAmount);
            w++;
        }
        try {
            configuration2.save(jailedInv);
        } catch (IOException e) {
            e.printStackTrace();
        }

        logText.add("[" + format.format(now) + "] " + prisonerList.size() + " prisoners.");

        try {
            PrintWriter writer = new PrintWriter(today.getPath(), "UTF-8");
            for (String s : logText) {
                writer.println(s);
            }
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveData();
    }

    // oh boy
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("notjail")) {
            if (sender.hasPermission("notjail.admin")) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("wand")) {

                        if (sender instanceof Player) {
                            ((Player) sender).getInventory().addItem(getWand());
                            sender.sendMessage(prefix + ChatColor.YELLOW + "Received Cell Wand.");
                        } else {
                            sender.sendMessage(prefix + ChatColor.YELLOW + "Only players can use this command!");
                        }
                    } else if (args[0].equalsIgnoreCase("create")) {
                        // this is for creating cells
                        if (sender instanceof Player) {
                            if (args.length > 1) {
                                if ((args[1].equalsIgnoreCase("autocell"))) {
                                    if (args.length > 2) {
                                        if (args[2].equalsIgnoreCase("holding")) {
                                            Cell cell = new Cell(args[2], ((Player) sender).getLocation(), this, autoCellIgnoreList);
                                            holdingCellList.add(cell);
                                            sender.sendMessage(prefix + ChatColor.YELLOW + "Created holding cell.");
                                            logText.add("[" + format.format(now) + "] " + sender.getName() + " Created holding cell");
                                        } else if (args[2].equalsIgnoreCase("rollCall")) {
                                            sender.sendMessage(prefix + ChatColor.YELLOW + "Created roll call area.");
                                            logText.add("[" + format.format(now) + "] " + sender.getName() + " Created roll call area");
                                            rollCallList.add(new Cell(args[2], ((Player) sender).getLocation(), this, autoCellIgnoreList));
                                        } else if (args[2].equalsIgnoreCase("solitary")) {
                                            Cell cell = new Cell(args[2], ((Player) sender).getLocation(), this, autoCellIgnoreList);
                                            solitaryCellList.add(cell);
                                            sender.sendMessage(prefix + ChatColor.YELLOW + "Created solitary cell.");
                                        } else if (args[2].equalsIgnoreCase("medical")) {
                                            Cell cell = new Cell(args[2], ((Player) sender).getLocation(), this, autoCellIgnoreList);
                                            medicalBayList.add(cell);
                                            sender.sendMessage(prefix + ChatColor.YELLOW + "Created medical bay.");
                                            logText.add("[" + format.format(now) + "] " + sender.getName() + " Created medical bay");
                                        } else {
                                            Cell cell = new Cell(args[2], ((Player) sender).getLocation(), this, autoCellIgnoreList);
                                            cellList.add(cell);
                                            sender.sendMessage(prefix + ChatColor.YELLOW + "Created regular cell.");
                                            logText.add("[" + format.format(now) + "] " + sender.getName() + " Created regular cell");
                                        }
                                    } else {
                                        openGUI((Player) sender, "createCell", 0);
                                    }
                                } else if (selectedPoints.containsKey(((Player) sender).getUniqueId().toString())) {
                                    Location[] locations = selectedPoints.get(((Player) sender).getUniqueId().toString());
                                    if ((locations[0] != null && locations[1] != null && locations[0].getWorld().equals(locations[1].getWorld()))) {
                                        if (args[1].equalsIgnoreCase("holding")) {
                                            Cell cell = new Cell(args[1], locations[0], locations[1], ((Player) sender).getLocation(), this);
                                            if (cell.inCell(cell.getSpawn())) {
                                                holdingCellList.add(cell);
                                                sender.sendMessage(prefix + ChatColor.YELLOW + "Created holding cell.");
                                                logText.add("[" + format.format(now) + "] " + sender.getName() + " Created holding cell");
                                            } else {
                                                sender.sendMessage(prefix + ChatColor.YELLOW + "Cell spawn is not inside the cell. (Where you are standing)");
                                            }
                                        } else if (args[1].equalsIgnoreCase("rollCall")) {
                                            sender.sendMessage(prefix + ChatColor.YELLOW + "Created roll call area.");
                                            logText.add("[" + format.format(now) + "] " + sender.getName() + " Created roll call area");
                                            rollCallList.add(new Cell(args[1], locations[0], locations[1], ((Player) sender).getLocation(), this));
                                        } else if (args[1].equalsIgnoreCase("solitary")) {
                                            Cell cell = new Cell(args[1], locations[0], locations[1], ((Player) sender).getLocation(), this);
                                            if (cell.inCell(cell.getSpawn())) {
                                                solitaryCellList.add(cell);
                                                sender.sendMessage(prefix + ChatColor.YELLOW + "Created solitary cell.");
                                            } else {
                                                sender.sendMessage(prefix + ChatColor.YELLOW + "Cell spawn is not inside the cell. (Where you are standing)");
                                            }
                                        } else if (args[1].equalsIgnoreCase("medical")) {
                                            Cell cell = new Cell(args[1], locations[0], locations[1], ((Player) sender).getLocation(), this);
                                            if (cell.inCell(cell.getSpawn())) {
                                                medicalBayList.add(cell);
                                                sender.sendMessage(prefix + ChatColor.YELLOW + "Created medical bay.");
                                                logText.add("[" + format.format(now) + "] " + sender.getName() + " Created medical bay");
                                            } else {
                                                sender.sendMessage(prefix + ChatColor.YELLOW + "Medical bay spawn is not inside the bay. (Where you are standing)");
                                            }
                                        } else {
                                            Cell cell = new Cell(args[1], locations[0], locations[1], ((Player) sender).getLocation(), this);
                                            if (cell.inCell(cell.getSpawn())) {
                                                cellList.add(cell);
                                                sender.sendMessage(prefix + ChatColor.YELLOW + "Created regular cell.");
                                                logText.add("[" + format.format(now) + "] " + sender.getName() + " Created regular cell");
                                            } else {
                                                sender.sendMessage(prefix + ChatColor.YELLOW + "Cell spawn is not inside the cell. (Where you are standing)");
                                            }
                                        }
                                    } else {
                                        sender.sendMessage(prefix + ChatColor.YELLOW + "Your selected points cannot be used to create a cell!");
                                    }
                                } else {
                                    sender.sendMessage(prefix + ChatColor.YELLOW + "You have not selected any positions with the wand!");
                                }
                            } else {
                                if (selectedPoints.containsKey(((Player) sender).getUniqueId().toString())) {
                                    Location[] locations = selectedPoints.get(((Player) sender).getUniqueId().toString());
                                    if ((locations[0] != null && locations[1] != null && locations[0].getWorld().equals(locations[1].getWorld()))) {
                                        Cell cell = new Cell("regular", locations[0], locations[1], ((Player) sender).getLocation(), this);
                                        if (cell.inCell(cell.getSpawn())) {
                                            cellList.add(cell);
                                            sender.sendMessage(prefix + ChatColor.YELLOW + "Created regular cell.");
                                            logText.add("[" + format.format(now) + "] " + sender.getName() + " Created regular cell");
                                        } else {
                                            sender.sendMessage(prefix + ChatColor.YELLOW + "Cell spawn is not inside the cell. (Where you are standing)");
                                        }
                                    } else {
                                        sender.sendMessage(prefix + ChatColor.YELLOW + "Your selected points cannot be used to create a cell!");
                                    }
                                } else {
                                    sender.sendMessage(prefix + ChatColor.YELLOW + "You have not selected any positions with the wand!");
                                }
                            }
                        } else {
                            sender.sendMessage(prefix + ChatColor.YELLOW + "Only players can run this command!");
                        }
                    } else if (args[0].equalsIgnoreCase("remove")) {
                        if (args.length > 2) {
                            int num;
                            try {
                                num = Integer.parseInt(args[2]);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Not a number.");
                                return true;
                            }
                            if (args[1].equalsIgnoreCase("solitary")){
                                if (solitaryCellList.size() >= num){
                                    if (solitaryCellList.get(num-1).isVillagerAlive())
                                        solitaryCellList.get(num-1).removeVillager();
                                    solitaryCellList.remove(num - 1);
                                    sender.sendMessage(prefix + ChatColor.GREEN + "Solitary Cell Removed!");
                                } else {
                                    sender.sendMessage(prefix + ChatColor.YELLOW + "There aren't that many solitary cells!");
                                }
                            } else if (args[1].equalsIgnoreCase("holding")){
                                if (holdingCellList.size() >= num){
                                    if (holdingCellList.get(num-1).isVillagerAlive())
                                        holdingCellList.get(num-1).removeVillager();
                                    holdingCellList.remove(num - 1);
                                    sender.sendMessage(prefix + ChatColor.GREEN + "Holding Cell Removed!");
                                } else {
                                    sender.sendMessage(prefix + ChatColor.YELLOW + "There aren't that many holding cells!");
                                }
                            } else if (args[1].equalsIgnoreCase("medical")){
                                if (medicalBayList.size() >= num){
                                    if (medicalBayList.get(num-1).isVillagerAlive())
                                        medicalBayList.get(num-1).removeVillager();
                                    medicalBayList.remove(num - 1);
                                    sender.sendMessage(prefix + ChatColor.GREEN + "Medical Bay Removed!");
                                } else {
                                    sender.sendMessage(prefix + ChatColor.YELLOW + "There aren't that many medical bays!");
                                }
                            } else if (args[1].equalsIgnoreCase("rollcall")){
                                if (rollCallList.size() >= num){
                                    if (rollCallList.get(num-1).isVillagerAlive())
                                        rollCallList.get(num-1).removeVillager();
                                    rollCallList.remove(num - 1);
                                    sender.sendMessage(prefix + ChatColor.GREEN + "Roll Call Area Removed!");
                                } else {
                                    sender.sendMessage(prefix + ChatColor.YELLOW + "There aren't that many roll call areas!");
                                }
                            } else {
                                if (cellList.size() >= num){
                                    if (cellList.get(num-1).isVillagerAlive())
                                        cellList.get(num-1).removeVillager();
                                    cellList.remove(num - 1);
                                    sender.sendMessage(prefix + ChatColor.GREEN + "Regular Cell Removed!");
                                } else {
                                    sender.sendMessage(prefix + ChatColor.YELLOW + "There aren't that many regular cells!");
                                }
                            }
                        } else {
                            if (!(sender instanceof Player)){
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Incorrect Usage: " + ChatColor.GOLD + "/notjail remove (type) (#)");
                            } else {
                                boolean removed = false;
                                Iterator<Cell> cellIterator = cellList.iterator();
                                while (cellIterator.hasNext()){
                                    Cell cell = cellIterator.next();
                                    if (cell.inCell(((Player) sender).getLocation())){
                                        cellIterator.remove();
                                        sender.sendMessage(prefix + ChatColor.GREEN + "Removed regular cell \"" + cell.getType() + "\" at your location.");
                                        removed = true;
                                    }
                                }
                                cellIterator = solitaryCellList.iterator();
                                while (cellIterator.hasNext()){
                                    Cell cell = cellIterator.next();
                                    if (cell.inCell(((Player) sender).getLocation())){
                                        cellIterator.remove();
                                        sender.sendMessage(prefix + ChatColor.GREEN + "Removed solitary cell at your location.");
                                        removed = true;
                                    }
                                }
                                cellIterator = holdingCellList.iterator();
                                while (cellIterator.hasNext()){
                                    Cell cell = cellIterator.next();
                                    if (cell.inCell(((Player) sender).getLocation())){
                                        cellIterator.remove();
                                        sender.sendMessage(prefix + ChatColor.GREEN + "Removed holding cell at your location.");
                                        removed = true;
                                    }
                                }
                                cellIterator = medicalBayList.iterator();
                                while (cellIterator.hasNext()){
                                    Cell cell = cellIterator.next();
                                    if (cell.inCell(((Player) sender).getLocation())){
                                        cellIterator.remove();
                                        sender.sendMessage(prefix + ChatColor.GREEN + "Removed medical bay at your location.");
                                        removed = true;
                                    }
                                }
                                cellIterator = rollCallList.iterator();
                                while (cellIterator.hasNext()){
                                    Cell cell = cellIterator.next();
                                    if (cell.inCell(((Player) sender).getLocation())){
                                        cellIterator.remove();
                                        sender.sendMessage(prefix + ChatColor.GREEN + "Removed roll call area at your location.");
                                        removed = true;
                                    }
                                }
                                if (!removed) {
                                    sender.sendMessage(prefix + ChatColor.RED + "Didn't find any cells here!");
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        if (sender.hasPermission("notjail.reload")){
                            loadConfig();
                            sender.sendMessage(prefix + ChatColor.YELLOW + "Reloaded NotJail version" + this.getDescription().getVersion() + ".");
                        } else {
                            sender.sendMessage(ChatColor.RED + "You do not have permission to preform this command!");
                        }
                    } else if (args[0].equalsIgnoreCase("list")) {
                        if (args.length > 1) {
                            if (args[1].equalsIgnoreCase("cells") || args[1].equalsIgnoreCase("cell")) {
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Current Cells:");
                                sender.sendMessage(ChatColor.YELLOW + "" + cellList.size() + ChatColor.GOLD + " regular cells.");
                                sender.sendMessage(ChatColor.YELLOW + "" + holdingCellList.size() + ChatColor.GOLD + " holding cells.");
                                sender.sendMessage(ChatColor.YELLOW + "" + rollCallList.size() + ChatColor.GOLD + " roll call areas.");
                                sender.sendMessage(ChatColor.YELLOW + "" + solitaryCellList.size() + ChatColor.GOLD + " solitary cells.");
                                sender.sendMessage(ChatColor.YELLOW + "" + medicalBayList.size() + ChatColor.GOLD + " medical bays.");
                                sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + ChatColor.GOLD + "                                           ");
                            } else if (args[1].equalsIgnoreCase("prisoners") || args[1].equalsIgnoreCase("prisoner")) {
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Total Prisoners: " + ChatColor.GOLD + prisonerList.size());
                                sender.sendMessage(ChatColor.GRAY + "Statuses: Jail = in jail, tbdJail = to be jailed when online, unJail = to be un-jailed when online");
                                for (Prisoner prisoner : prisonerList) {
                                    sender.sendMessage(prefix + ChatColor.GOLD + prisoner.getName());
                                    sender.sendMessage(ChatColor.YELLOW + "" + formatTime(prisoner.getJailTime())  + ChatColor.DARK_GRAY + " / " + ChatColor.YELLOW + formatTime(prisoner.getSentenceLength()));
                                    sender.sendMessage(ChatColor.YELLOW + "Cell: " + ChatColor.GOLD + prisoner.getCell() + ChatColor.DARK_GRAY + " (-2 = holding)");
                                    sender.sendMessage(ChatColor.YELLOW + "Reason: " + ChatColor.GOLD + prisoner.getReason());
                                    sender.sendMessage(ChatColor.YELLOW + "Status: " + ChatColor.GOLD + prisoner.getAction());
                                }
                                sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "                                           ");
                            } else {
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Unknown argument.");
                            }
                        } else {
                            openGUI((Player) sender, "list", 0);
                        }
                    } else if (args[0].equalsIgnoreCase("setRelease")) {
                        if (sender instanceof Player) {
                            presetRelease = ((Player) sender).getLocation();
                            sender.sendMessage(prefix + ChatColor.YELLOW + "Set preset release location to " + presetRelease.getBlockX() + " " + presetRelease.getBlockY() + " " + presetRelease.getBlockZ() + " " + presetRelease.getWorld().getName() + ".");
                        } else {
                            sender.sendMessage(prefix + ChatColor.YELLOW + "Only players can use this command!");
                        }
                    } else if (args[0].equalsIgnoreCase("solitary")) {
                        if (args.length > 1) {
                            Player p = Bukkit.getPlayer(args[1]);
                            if (p != null) {
                                if (jailedUUID.contains(p.getUniqueId().toString())) {
                                    if (args[2] != null) {
                                        long solitarySeconds;
                                        if (args[2].contains("s")) {
                                            String s = args[2].replace("s", "");
                                            try {
                                                solitarySeconds = Long.parseLong(s);
                                            } catch (NumberFormatException e) {
                                                sender.sendMessage(prefix + ChatColor.YELLOW + "Incorrect format for solitary time.");
                                                return true;
                                            }
                                        } else {
                                            String s = args[2];
                                            if (args[2].contains("m"))
                                                s = args[2].replace("m", "");
                                            try {
                                                solitarySeconds = Long.parseLong(s) * 60;
                                            } catch (NumberFormatException e) {
                                                sender.sendMessage(prefix + ChatColor.YELLOW + "Incorrect format for solitary time.");
                                                return true;
                                            }
                                        }
                                        for (Prisoner prisoner : prisonerList) {
                                            if (prisoner.getUuid().equals(p.getUniqueId())) {
                                                if (args.length > 3) {
                                                    StringBuilder builder = new StringBuilder();
                                                    for (int i = 3; i < args.length; i++) {
                                                        builder.append(" ").append(args[i]);
                                                    }
                                                    prisoner.putInSolitary((int) solitarySeconds, builder.toString());
                                                } else {
                                                    prisoner.putInSolitary((int) solitarySeconds, "no reason");
                                                }
                                                sender.sendMessage(prefix + ChatColor.YELLOW + "Player has been put into solitary.");
                                                break;
                                            }
                                        }
                                    } else {
                                        Prisoner prisoner = null;
                                        int i = 0;
                                        for (Prisoner prisoner1 : prisonerList) {
                                            if (prisoner1.getUuid().equals(p.getUniqueId())) {
                                                prisoner = prisoner1;
                                            }
                                            i++;
                                        }
                                        if (prisoner == null){
                                            sender.sendMessage(prefix + ChatColor.YELLOW + "Please specify a time.");
                                            return true;
                                        }

                                        ItemStack skull = new ItemStack(Material.PLAYER_HEAD); // Create a new ItemStack of the Player Head type.
                                        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta(); // Get the created item's ItemMeta and cast it to SkullMeta so we can access the skull properties
                                        assert skullMeta != null;
                                        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(prisoner.getUuid())); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
                                        skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + prisoner.getName());
                                        String uuid1 = prisoner.getUuid().toString().substring(0,18);
                                        String uuid2 = prisoner.getUuid().toString().substring(18);
                                        ArrayList<String> lore = new ArrayList<>();
                                        lore.add("");
                                        lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + uuid1);
                                        lore.add(ChatColor.DARK_AQUA + uuid2);
                                        lore.add(ChatColor.AQUA + "Reason: " + ChatColor.DARK_AQUA + prisoner.getReason());
                                        lore.add(ChatColor.AQUA + "Sentence Length: " + ChatColor.DARK_AQUA + formatTime(prisoner.getSentenceLength()));
                                        lore.add(ChatColor.AQUA + "Time Left: " + ChatColor.DARK_AQUA + formatTime(prisoner.getSentenceLength() - prisoner.getJailTime()));
                                        lore.add("");
                                        lore.add(ChatColor.GREEN + "Click to Sent to Solitary.");
                                        lore.add(ChatColor.BLACK + "" + i);
                                        skullMeta.setLore(lore);
                                        skull.setItemMeta(skullMeta);
                                        openGUI((Player) sender, "solitaryTime", 3600, skull);
                                    }
                                } else {
                                    sender.sendMessage(prefix + ChatColor.YELLOW + "Player is not jailed.");
                                }
                            } else {
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Player must be online.");
                            }
                        } else {
                            sender.sendMessage(prefix + ChatColor.YELLOW + "Please specify a player.");
                        }
                    } else if (args[0].equalsIgnoreCase("help")) {
                        sender.sendMessage(prefix + ChatColor.BLUE + "Usage: ");
                        sender.sendMessage(ChatColor.AQUA + "/notjail (player) (time) (cell#/open)" + ChatColor.GRAY + "" + ChatColor.ITALIC + " <reason>" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Jails a player.");
                        sender.sendMessage(ChatColor.AQUA + "/notjail wand" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Gives wand to create rectangle cell.");
                        sender.sendMessage(ChatColor.AQUA + "/notjail create " + ChatColor.GRAY + "" + ChatColor.ITALIC + "<autocell>" + ChatColor.AQUA + " (type)" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Creates a cell.");
                        sender.sendMessage(ChatColor.AQUA + "/notjail remove" + ChatColor.GRAY + "" + ChatColor.ITALIC + " <type> <#>" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Removes a cell.");
                        sender.sendMessage(ChatColor.AQUA + "/notjail list cells/prisoners" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Lists cells or prisoners.");
                        sender.sendMessage(ChatColor.AQUA + "/notjail setRelease" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Sets release location if enabled in config.");
                        sender.sendMessage(ChatColor.AQUA + "/notjail reload" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Reloads plugin.");
                        sender.sendMessage(ChatColor.AQUA + "/notjail solitary (player) (time) (reson)" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Sends a prisoner to a solitary cell.");
                        sender.sendMessage(ChatColor.AQUA + "/notjail inv (player)" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Opens the inventory of a jailed player.");
                        sender.sendMessage(ChatColor.AQUA + "/notjail info" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Gives info on the current cell.");
                        sender.sendMessage(ChatColor.AQUA + "/notjail" + ChatColor.GRAY + "" + ChatColor.ITALIC + " <gui>" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Opens plugin GUI.");
                        sender.sendMessage(ChatColor.AQUA + "/unnotjail (player)" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Unjails a player.");
                        sender.sendMessage(ChatColor.AQUA + "/notjail help" + ChatColor.WHITE + " | " + ChatColor.DARK_AQUA + "Shows this page ;)");
                    } else if (args[0].equalsIgnoreCase("inv")){
                        if (args.length > 1){
                            Player player = Bukkit.getPlayer(args[1]);
                            Prisoner prisoner = null;
                            if (player != null){
                                if (isPrisoner(player)){
                                    for (Prisoner prisoner1 : prisonerList){
                                        if (prisoner1.getUuid().equals(player.getUniqueId())){
                                            prisoner = prisoner1;
                                            break;
                                        }
                                    }
                                } else {
                                    sender.sendMessage(prefix + ChatColor.RED + "This player is not a prisoner!");
                                    return true;
                                }
                            } else {
                                for (Prisoner prisoner1 : prisonerList){
                                    if (prisoner1.getName().equalsIgnoreCase(args[1])){
                                        prisoner = prisoner1;
                                        break;
                                    }
                                }
                            }
                            if (prisoner != null){
                                openInventory((Player) sender, prisoner);
                            } else {
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Could not find a prisoner with this name.");
                            }
                        } else {
                            openGUI((Player) sender, "inventories", 0);
                        }
                    } else if (args[0].equalsIgnoreCase("info")) {
                        if (sender instanceof Player) {
                            boolean foundCell = false;
                            for (Cell cell : cellList) {
                                if (cell.inCell(((Player) sender).getLocation())) {
                                    foundCell = true;
                                    sender.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "                " + ChatColor.BLUE + "" + ChatColor.BOLD + " Cell Info " + ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "               ");
                                    sender.sendMessage(ChatColor.AQUA + "Cell Type: " + ChatColor.DARK_AQUA + cell.getType());
                                    sender.sendMessage(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                                    sender.sendMessage(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                                    sender.sendMessage(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                                    sender.sendMessage(ChatColor.AQUA + "Has Villager: " + ChatColor.DARK_AQUA + cell.isVillagerAlive());
                                    Prisoner prisoner = null;
                                    for (Prisoner p : prisonerList) {
                                        if (p.getRCell().equals(cell)) {
                                            prisoner = p;
                                            break;
                                        }
                                    }
                                    if (prisoner != null) {
                                        sender.sendMessage(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + prisoner.getName());
                                        sender.sendMessage(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                                    } else {
                                        sender.sendMessage(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + "Vacant Cell.");
                                    }
                                    sender.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "                                             ");
                                    break;
                                }
                            }
                            if (!foundCell)
                                for (Cell cell : solitaryCellList) {
                                    if (cell.inCell(((Player) sender).getLocation())) {
                                        foundCell = true;
                                        sender.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "                " + ChatColor.BLUE + "" + ChatColor.BOLD + " Cell Info " + ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "               ");
                                        sender.sendMessage(ChatColor.AQUA + "Cell Type: " + ChatColor.DARK_AQUA + cell.getType());
                                        sender.sendMessage(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                                        sender.sendMessage(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                                        sender.sendMessage(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                                        Prisoner prisoner = null;
                                        for (Prisoner p : prisonerList) {
                                            if (p.getSolitaryCell().equals(cell)) {
                                                prisoner = p;
                                                break;
                                            }
                                        }
                                        if (prisoner != null) {
                                            sender.sendMessage(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + prisoner.getName());
                                            sender.sendMessage(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                                        } else {
                                            sender.sendMessage(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + "Vacant Cell.");
                                        }
                                        sender.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "                                             ");
                                        break;
                                    }
                                }
                            if (!foundCell)
                                for (Cell cell : medicalBayList) {
                                    if (cell.inCell(((Player) sender).getLocation())) {
                                        foundCell = true;
                                        sender.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "                " + ChatColor.BLUE + "" + ChatColor.BOLD + " Cell Info " + ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "               ");
                                        sender.sendMessage(ChatColor.AQUA + "Cell Type: " + ChatColor.DARK_AQUA + cell.getType());
                                        sender.sendMessage(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                                        sender.sendMessage(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                                        sender.sendMessage(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                                        sender.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "                                             ");
                                        break;
                                    }
                                }
                            if (!foundCell)
                                for (Cell cell : holdingCellList) {
                                    if (cell.inCell(((Player) sender).getLocation())) {
                                        foundCell = true;
                                        sender.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "                " + ChatColor.BLUE + "" + ChatColor.BOLD + " Cell Info " + ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "               ");
                                        sender.sendMessage(ChatColor.AQUA + "Cell Type: " + ChatColor.DARK_AQUA + cell.getType());
                                        sender.sendMessage(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                                        sender.sendMessage(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                                        sender.sendMessage(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                                        List<Prisoner> prisoners = new ArrayList<>();
                                        for (Prisoner p : prisonerList) {
                                            if (p.getRCell().equals(cell)) {
                                                prisoners.add(p);
                                            }
                                        }
                                        if (prisoners.size() > 0) {
                                            for (Prisoner prisoner : prisoners) {
                                                sender.sendMessage(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + prisoner.getName());
                                                sender.sendMessage(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                                            }
                                        } else {
                                            sender.sendMessage(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + "Vacant Cell.");
                                        }
                                        sender.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "                                             ");
                                        break;
                                    }
                                }
                            if (!foundCell)
                                for (Cell cell : rollCallList) {
                                    if (cell.inCell(((Player) sender).getLocation())) {
                                        foundCell = true;
                                        sender.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "                " + ChatColor.BLUE + "" + ChatColor.BOLD + " Cell Info " + ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "               ");
                                        sender.sendMessage(ChatColor.AQUA + "Cell Type: " + ChatColor.DARK_AQUA + cell.getType());
                                        sender.sendMessage(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                                        sender.sendMessage(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                                        sender.sendMessage(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                                        sender.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.STRIKETHROUGH + "                                             ");
                                        break;
                                    }
                                }
                            if (!foundCell){
                                sender.sendMessage(prefix + ChatColor.AQUA + "Did not find a cell here!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                        }
                    } else if (args.length > 2) {
                        long sentenceSeconds;
                        if (args[1].contains("s")) {
                            String s = args[1].replace("s", "");
                            try {
                                sentenceSeconds = Long.parseLong(s);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Incorrect format for jail time.");
                                return true;
                            }
                        } else if (args[1].contains("h")) {
                            String s = args[1].replace("h", "");
                            try {
                                sentenceSeconds = Long.parseLong(s) * 3600;
                            } catch (NumberFormatException e) {
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Incorrect format for jail time.");
                                return true;
                            }
                        } else if (args[1].contains("d")) {
                            String s = args[1].replace("d", "");
                            try {
                                sentenceSeconds = Long.parseLong(s) * 86400;
                            } catch (NumberFormatException e) {
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Incorrect format for jail time.");
                                return true;
                            }
                        } else {
                            String s = args[1];
                            if (args[1].contains("m"))
                                s = args[1].replace("m", "");
                            try {
                                sentenceSeconds = Long.parseLong(s) * 60;
                            } catch (NumberFormatException e) {
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Incorrect format for jail time.");
                                return true;
                            }
                        }
                        UUID uuid;
                        Player player = Bukkit.getPlayer(args[0]);
                        if (player == null) {
                            sender.sendMessage(prefix + ChatColor.GOLD + args[0] + ChatColor.YELLOW + " is not online. If they join they will be jailed.");
                            logText.add("[" + format.format(now) + "] " + sender.getName() + " has put in a request to jail " + args[0] + " when they come online");
                            uuid = UUID.randomUUID();
                        } else {
                            uuid = player.getUniqueId();
                            sender.sendMessage(prefix + ChatColor.YELLOW + "Player will been sent to jail.");
                            logText.add("[" + format.format(now) + "] " + sender.getName() + " has sent " + player.getName() + " to jail");
                        }
                        int cell;
                        try {
                            cell = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            cell = -1;
                        }
                        StringBuilder builder = new StringBuilder();
                        for (int i = 3; i < args.length; i++) {
                            builder.append(" ").append(args[i]);
                        }
                        if (args.length < 4) {
                            builder.append("No Reason");
                        }
                        Prisoner onlinePrisoner;
                        if (jailedUUID.contains(uuid.toString())) {
                            int i = -1;
                            for (Prisoner prisoner : prisonerList) {
                                if (prisoner.getUuid().equals(uuid))
                                    i = prisonerList.indexOf(prisoner);
                            }
                            if (i != -1) {
                                prisonerList.get(i).removeBar();
                                onlinePrisoner = new Prisoner("tbdJail", uuid, args[0], 0, sentenceSeconds, cell, builder.toString(), null, 0, this);
                                prisonerList.set(i, onlinePrisoner);

                            } else {
                                onlinePrisoner = new Prisoner("tbdJail", uuid, args[0], 0, sentenceSeconds, cell, builder.toString(), null, 0, this);
                                prisonerList.add(onlinePrisoner);
                            }
                        } else {
                            onlinePrisoner = new Prisoner("tbdJail", uuid, args[0], 0, sentenceSeconds, cell, builder.toString(), null, 0, this);
                            prisonerList.add(onlinePrisoner);
                            jailedUUID.add(String.valueOf(uuid));
                        }
                        onlinePrisoners.add(onlinePrisoner);

                        File record = new File(jails + File.separator + args[0] + "[" + formatAll.format(now) + "].txt");
                        try {
                            record.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            PrintWriter writer = new PrintWriter(record.getPath(), "UTF-8");
                            writer.println(args[0] + " jailed by " + sender.getName() + " on " + formatDate.format(now));
                            if (player == null) {
                                writer.println("UUID not available. (Jailed while offline)");
                            } else {
                                writer.println("UUID: " + player.getUniqueId());
                            }

                            writer.println("Time due in jail: " + formatTime(sentenceSeconds));
                            writer.println("Reason: " + builder);
                            writer.close();
                        } catch (IOException e) {
                            // do something
                        }
                    } else {
                        Player player = Bukkit.getPlayer(args[0]);
                        if (player != null) {
                            /*ItemStack skull = new ItemStack(Material.PLAYER_HEAD); // Create a new ItemStack of the Player Head type.
                            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta(); // Get the created item's ItemMeta and cast it to SkullMeta so we can access the skull properties
                            assert skullMeta != null;
                            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId())); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
                            skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + player.getName());
                            String uuid1 = player.getUniqueId().toString().substring(0, 18);
                            String uuid2 = player.getUniqueId().toString().substring(18);
                            ArrayList<String> lore = new ArrayList<>();
                            lore.add("");
                            lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + uuid1);
                            lore.add(ChatColor.DARK_AQUA + uuid2);
                            lore.add("");
                            lore.add(ChatColor.GREEN + "Click to Select.");
                            skullMeta.setLore(lore);
                            skull.setItemMeta(skullMeta);*/
                            openGUI((Player) sender, "jailReason", 0, player.getUniqueId());
                        } else {
                            if (loggedPlayers.containsKey(args[0].toLowerCase(Locale.ROOT))){
                                openGUI((Player) sender, "jailReason", 0, loggedPlayers.get(args[0].toLowerCase(Locale.ROOT)));
                            }

                            /*
                            openGUI((Player) sender, "jail", 0);
                            sender.sendMessage(prefix + ChatColor.YELLOW + "Player is not online. If you wish to jail an offline player, run the command again with all parameters");
                            sender.sendMessage(ChatColor.GOLD + "/jail (player) (time) (cell#/open) (reason)");*/
                        }
                    }
                } else {
                    if (sender instanceof Player) {
                        // open gui
                        openGUI((Player) sender, "menu", 0);
                    } else {
                        sender.sendMessage(ChatColor.BLUE + "Running NotJail Version " + this.getDescription().getVersion() + ".");
                        sender.sendMessage(ChatColor.DARK_AQUA + "Do " + ChatColor.AQUA + "/jail help" + ChatColor.DARK_AQUA + " to get a list of commands.");
                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to preform this command!");
            }
        } else if (command.getName().equalsIgnoreCase("unnotjail")) {
            if (sender.hasPermission("notjail.admin")) {
                if (args.length == 0) {
                    openGUI((Player) sender, "unjail", 0);
                } else {
                    Player p = Bukkit.getPlayer(args[0]);
                    if (p != null) {
                        boolean found = false;
                        for (Prisoner prisoner : prisonerList) {
                            if (prisoner.getUuid().equals(p.getUniqueId())) {
                                prisoner.setAction("unJail");
                                sender.sendMessage(prefix + ChatColor.YELLOW + "Player will be freed from jail.");
                                found = true;
                            }
                        }
                        if (!found) {
                            sender.sendMessage(prefix + ChatColor.YELLOW + "Player is not in jail.");
                        }
                    } else {
                        sender.sendMessage(prefix + ChatColor.YELLOW + "Unknown Player.");
                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to preform this command!");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> list = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("notjail")) {
            if (sender.hasPermission("notjail.admin")) {
                if (args.length == 1) {
                    list.add("wand");
                    list.add("create");
                    list.add("remove");
                    list.add("list");
                    list.add("reload");
                    list.add("setRelease");
                    list.add("solitary");
                    list.add("gui");
                    list.add("help");
                    list.add("inv");
                    list.add("info");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!isPrisoner(p)) {
                            list.add(p.getName());
                        }
                    }
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("remove")) {
                        list.add("regular");
                        list.add("holding");
                        list.add("rollcall");
                        list.add("solitary");
                        list.add("medical");
                    } else if (args[0].equalsIgnoreCase("create")) {
                        list.add("regular");
                        list.add("holding");
                        list.add("rollcall");
                        list.add("solitary");
                        list.add("medical");
                        list.add("autocell");
                    } else if (args[0].equalsIgnoreCase("list")) {
                        list.add("cells");
                        list.add("prisoners");
                    } else if (args[0].equalsIgnoreCase("inv")) {
                        for (Prisoner prisoner : prisonerList){
                            list.add(prisoner.getName());
                        }
                    } else if (args[0].equalsIgnoreCase("solitary")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (isPrisoner(p)) {
                                list.add(p.getName());
                            }
                        }
                    } else {
                        list.add("(time)s");
                        list.add("(time)m");
                        list.add("(time)h");
                        list.add("(time)d");
                    }

                } else if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("solitary")) {
                        list.add("(time)s");
                        list.add("(time)m");
                    } else if (args[1].equalsIgnoreCase("autocell")) {
                        list.add("regular");
                        list.add("holding");
                        list.add("rollcall");
                        list.add("solitary");
                        list.add("medical");
                    } else if (args[0].equalsIgnoreCase("remove")) {
                        list.add("#");
                    } else if (!args[0].equalsIgnoreCase("wand") && !args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("list") && !args[0].equalsIgnoreCase("reload") && !args[0].equalsIgnoreCase("setRelease") && !args[0].equalsIgnoreCase("inv") && !args[0].equalsIgnoreCase("info")) {
                        list.add("#");
                        list.add("open");
                    }
                }
            }
        } else if (command.getName().equalsIgnoreCase("unnotjail")) {
            if (args.length == 1) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isPrisoner(p))
                        list.add(p.getName());
                }
            }
        }
        String typed = args[args.length-1];
        list.removeIf(test -> test.toLowerCase(Locale.ROOT).indexOf(typed.toLowerCase(Locale.ROOT)) != 0);
        Collections.sort(list);
        return list;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        final ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;
        if (event.getInventory().equals(menuInv)) {
            // main menu
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("jailPlayer"))) {
                openGUI((Player) event.getWhoClicked(), "jail", 0);
            } else if (clickedItem.isSimilar(item.get("createCell"))) {
                openGUI((Player) event.getWhoClicked(), "createCell", 0);
            } else if (clickedItem.isSimilar(item.get("removeCell"))) {
                openGUI((Player) event.getWhoClicked(), "removeCell", 0);
            } else if (clickedItem.isSimilar(item.get("wand"))) {
                event.getView().close();
                event.getWhoClicked().getInventory().addItem(getWand()); // maybe change this to check if they have a full inventory
            } else if (clickedItem.isSimilar(item.get("unjail"))) {
                openGUI((Player) event.getWhoClicked(), "unjail", 0);
            } else if (clickedItem.isSimilar(item.get("info"))) {
                Bukkit.getServer().dispatchCommand(event.getWhoClicked(), "notjail info");
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("solitary"))) {
                openGUI((Player) event.getWhoClicked(), "solitary", 0);
            } else if (clickedItem.isSimilar(item.get("reload"))) {
                if (event.getWhoClicked().hasPermission("notjail.reload")) {
                    loadConfig();
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Reloaded NotJail version" + this.getDescription().getVersion() + ".");
                } else {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "You do not have permission to reload this plugin!");
                }
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("list"))) {
                openGUI((Player) event.getWhoClicked(), "list", 0);
            } else if (clickedItem.isSimilar(item.get("prisInv"))) {
                openGUI((Player) event.getWhoClicked(), "inventories", 0);
            }
        } else if (event.getInventory().equals(chooseList)) {
            // list menu - do you want to list prisoners or cells?
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("listPrisoners"))) {
                openGUI((Player) event.getWhoClicked(), "list", 1);
            } else if (clickedItem.isSimilar(item.get("listCells"))) {
                openGUI((Player) event.getWhoClicked(), "list", 2);
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          " + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + " Prisoner List " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          ")) {
            // list menu - list prisoners
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("back"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6)) - 2;
                openGUI((Player) event.getWhoClicked(), "list", (int) (Math.pow(10, String.valueOf(page).length() - 1) + page));
            } else if (clickedItem.isSimilar(item.get("next"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6));
                openGUI((Player) event.getWhoClicked(), "list", (int) (Math.pow(10, String.valueOf(page).length() - 1) + page));
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "            " + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + " Cell List " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "            ")) {
            // list menu - list cells
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("back"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6)) - 2;
                openGUI((Player) event.getWhoClicked(), "list", (int) (Math.pow(10, String.valueOf(page).length() - 1) * 2 + page));
            } else if (clickedItem.isSimilar(item.get("next"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6));
                openGUI((Player) event.getWhoClicked(), "list", (int) (Math.pow(10, String.valueOf(page).length() - 1) * 2 + page));
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            } else if (!clickedItem.isSimilar(item.get("fill"))  && event.getInventory().equals(event.getView().getTopInventory()) && clickedItem.getType() != Material.PAPER){
                //  teleport to cell
                // lore.add(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                assert clickedItem.getItemMeta() != null;
                assert clickedItem.getItemMeta().getLore() != null;
                assert clickedItem.getItemMeta().getLore().size() > 0;
                String coordsLine = ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(0));
                coordsLine = coordsLine.substring(12); // remove  Cell Spawn:
                float x = Float.parseFloat(coordsLine.substring(0,coordsLine.indexOf("x"))); // get numbers before x
                coordsLine = coordsLine.substring(coordsLine.indexOf("x") + 2); // remove x and space after
                float y = Float.parseFloat(coordsLine.substring(0,coordsLine.indexOf("y"))); // get numbers before y
                coordsLine = coordsLine.substring(coordsLine.indexOf("y") + 2); // remove y and space after
                float z = Float.parseFloat(coordsLine.substring(0,coordsLine.indexOf("z"))); // get numbers before z
                coordsLine = coordsLine.substring(coordsLine.indexOf("z") + 2); // remove z and space after
                // what's left of coordsline should only be the world name  then
                if (Bukkit.getWorld(coordsLine) != null){
                    Location location = new Location(Bukkit.getWorld(coordsLine), x + 0.5, y + 0.5, z + 0.5);
                    event.getWhoClicked().teleport(location);
                    //event.getView().close();
                    event.getWhoClicked().sendMessage(prefix + ChatColor.GREEN + "Teleported to cell!");
                } else {
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Could not find the cell world!");
                }
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Cell " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           ")) {
            // create cell menu - which cell to create?
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            } else if (clickedItem.isSimilar(item.get("regularCell"))) {
                openGUI((Player) event.getWhoClicked(), "cReg", 0);
            } else if (clickedItem.isSimilar(item.get("solitaryCell"))) {
                openGUI((Player) event.getWhoClicked(), "cSol", 0);
            } else if (clickedItem.isSimilar(item.get("holdingCell"))) {
                openGUI((Player) event.getWhoClicked(), "cHol", 0);
            } else if (clickedItem.isSimilar(item.get("medicalBay"))) {
                openGUI((Player) event.getWhoClicked(), "cMed", 0);
            } else if (clickedItem.isSimilar(item.get("rollCall"))) {
                openGUI((Player) event.getWhoClicked(), "cRol", 0);
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Holding Cell " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       ")) {
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("useSelectedPoints"))) {
                Location[] locations = selectedPoints.get(event.getWhoClicked().getUniqueId().toString());
                if ((locations[0] != null && locations[1] != null && locations[0].getWorld().equals(locations[1].getWorld()))) {
                    Cell cell = new Cell("holding", locations[0], locations[1], event.getWhoClicked().getLocation(), this);
                    holdingCellList.add(cell);
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Created holding cell.");
                    logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " Created holding cell");
                    event.getView().close();
                } else {
                    event.getView().close();
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Your selected points cannot be used to create a cell!");
                }
            } else if (clickedItem.isSimilar(item.get("useAutoCell"))) {
                Cell cell = new Cell("holding", event.getWhoClicked().getLocation(), this, autoCellIgnoreList);
                holdingCellList.add(cell);
                event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Created holding cell.");
                logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " Created holding cell");
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "     " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Medical Bay " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "      ")) {
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("useSelectedPoints"))) {
                Location[] locations = selectedPoints.get(event.getWhoClicked().getUniqueId().toString());
                if ((locations[0] != null && locations[1] != null && locations[0].getWorld().equals(locations[1].getWorld()))) {
                    Cell cell = new Cell("medical", locations[0], locations[1], event.getWhoClicked().getLocation(), this);
                    medicalBayList.add(cell);
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Created medical bay.");
                    logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " Created medical bay");
                    event.getView().close();
                } else {
                    event.getView().close();
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Your selected points cannot be used to create a cell!");
                }
            } else if (clickedItem.isSimilar(item.get("useAutoCell"))) {
                Cell cell = new Cell("medical", event.getWhoClicked().getLocation(), this, autoCellIgnoreList);
                medicalBayList.add(cell);
                event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Created medical bay.");
                logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " Created medical bay");
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Regular Cell " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       ")) {
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("useSelectedPoints"))) {
                Location[] locations = selectedPoints.get(event.getWhoClicked().getUniqueId().toString());
                if ((locations[0] != null && locations[1] != null && locations[0].getWorld().equals(locations[1].getWorld()))) {
                    Cell cell = new Cell("regular", locations[0], locations[1], event.getWhoClicked().getLocation(), this);
                    cellList.add(cell);
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Created regular cell.");
                    logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " Created regular cell");
                    event.getView().close();
                } else {
                    event.getView().close();
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Your selected points cannot be used to create a cell!");
                }
            } else if (clickedItem.isSimilar(item.get("useAutoCell"))) {
                Cell cell = new Cell("regular", event.getWhoClicked().getLocation(), this, autoCellIgnoreList);
                cellList.add(cell);
                event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Created regular cell.");
                logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " Created regular cell");
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "         " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Roll Call " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "        ")) {
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("useSelectedPoints"))) {
                Location[] locations = selectedPoints.get(event.getWhoClicked().getUniqueId().toString());
                if ((locations[0] != null && locations[1] != null && locations[0].getWorld().equals(locations[1].getWorld()))) {
                    Cell cell = new Cell("rollcall", locations[0], locations[1], event.getWhoClicked().getLocation(), this);
                    rollCallList.add(cell);
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Created roll call area.");
                    logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " Created roll call area");
                    event.getView().close();
                } else {
                    event.getView().close();
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Your selected points cannot be used to create a cell!");
                }
            } else if (clickedItem.isSimilar(item.get("useAutoCell"))) {
                Cell cell = new Cell("rollcall", event.getWhoClicked().getLocation(), this, autoCellIgnoreList);
                rollCallList.add(cell);
                event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Created roll call area.");
                logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " Created roll call area");
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       " + ChatColor.YELLOW + "" + ChatColor.BOLD + " Create Solitary Cell " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "      ")) {
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("useSelectedPoints"))) {
                Location[] locations = selectedPoints.get(event.getWhoClicked().getUniqueId().toString());
                if ((locations[0] != null && locations[1] != null && Objects.equals(locations[0].getWorld(), locations[1].getWorld()))) {
                    Cell cell = new Cell("solitary", locations[0], locations[1], event.getWhoClicked().getLocation(), this);
                    solitaryCellList.add(cell);
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Created solitary cell.");
                    logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " Created solitary cell");
                    event.getView().close();
                } else {
                    event.getView().close();
                    event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Your selected points cannot be used to create a cell!");
                }
            } else if (clickedItem.isSimilar(item.get("useAutoCell"))) {
                Cell cell = new Cell("solitary", event.getWhoClicked().getLocation(), this, autoCellIgnoreList);
                solitaryCellList.add(cell);
                event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Created solitary cell.");
                logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " Created solitary cell");
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           " + ChatColor.GOLD + "" + ChatColor.BOLD + " Remove Cell " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           ")) {
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("removeFromList"))) {
                // open cell list but check which item they click and add a thing to lore
                openGUI((Player) event.getWhoClicked(), "removeFromList", 0);
            } else if (clickedItem.isSimilar(item.get("removeFromHere"))) {
                // try removing rn
                boolean removed = false;
                Iterator<Cell> cellIterator = cellList.iterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (cell.inCell(event.getWhoClicked().getLocation())) {
                        if (cell.isVillagerAlive())
                            cell.removeVillager();
                        cellIterator.remove();
                        event.getWhoClicked().sendMessage(prefix + ChatColor.GREEN + "Removed regular cell \"" + cell.getType() + "\" at your location.");
                        removed = true;
                    }
                }
                cellIterator = solitaryCellList.iterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (cell.inCell(event.getWhoClicked().getLocation())) {
                        if (cell.isVillagerAlive())
                            cell.removeVillager();
                        cellIterator.remove();
                        event.getWhoClicked().sendMessage(prefix + ChatColor.GREEN + "Removed solitary cell at your location.");
                        removed = true;
                    }
                }
                cellIterator = holdingCellList.iterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (cell.inCell(event.getWhoClicked().getLocation())) {
                        if (cell.isVillagerAlive())
                            cell.removeVillager();
                        cellIterator.remove();
                        event.getWhoClicked().sendMessage(prefix + ChatColor.GREEN + "Removed holding cell at your location.");
                        removed = true;
                    }
                }
                cellIterator = medicalBayList.iterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (cell.inCell(event.getWhoClicked().getLocation())) {
                        if (cell.isVillagerAlive())
                            cell.removeVillager();
                        cellIterator.remove();
                        event.getWhoClicked().sendMessage(prefix + ChatColor.GREEN + "Removed medical bay at your location.");
                        removed = true;
                    }
                }
                cellIterator = rollCallList.iterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (cell.inCell(event.getWhoClicked().getLocation())) {
                        if (cell.isVillagerAlive())
                            cell.removeVillager();
                        cellIterator.remove();
                        event.getWhoClicked().sendMessage(prefix + ChatColor.GREEN + "Removed roll call area at your location.");
                        removed = true;
                    }
                }
                if (!removed) {
                    event.getWhoClicked().sendMessage(prefix + ChatColor.RED + "Didn't find any cells here!");
                }

                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           " + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + " Remove List " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           ")){
            // remove from list menu
            if (clickedItem.isSimilar(item.get("back"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6)) - 2;
                openGUI((Player) event.getWhoClicked(), "removeFromList", page);
            } else if (clickedItem.isSimilar(item.get("next"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6));
                openGUI((Player) event.getWhoClicked(), "removeFromList", page);
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            } else if (!clickedItem.isSimilar(item.get("fill"))  && event.getInventory().equals(event.getView().getTopInventory()) && clickedItem.getType() != Material.PAPER){
                assert clickedItem.getItemMeta() != null;
                String type = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                int index;
                try {
                    index = Integer.parseInt(ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(clickedItem.getItemMeta().getLore().size() - 1)));
                } catch (NumberFormatException e){
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Cell ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(1)");
                    event.getView().close();
                    return;
                }
                Cell cell = null;
                switch (type.toLowerCase(Locale.ROOT)){
                    case "solitary":
                        if (solitaryCellList.size() > index) {
                            cell = solitaryCellList.get(index);
                            if (cell.isVillagerAlive()){
                                cell.removeVillager();
                            }
                            cell.setOpen(true);
                            solitaryCellList.remove(cell);
                        } else {
                            event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Cell ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(S2)");
                            event.getView().close();
                        }
                        break;
                    case "holding":
                        if (holdingCellList.size() > index) {
                            cell = holdingCellList.get(index);
                            if (cell.isVillagerAlive()){
                                cell.removeVillager();
                            }
                            cell.setOpen(true);
                            holdingCellList.remove(cell);
                        } else {
                            event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Cell ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(H2)");
                            event.getView().close();
                        }
                        break;
                    case "medical":
                        if (medicalBayList.size() > index) {
                            cell = medicalBayList.get(index);
                            if (cell.isVillagerAlive()){
                                cell.removeVillager();
                            }
                            cell.setOpen(true);
                            medicalBayList.remove(cell);
                        } else {
                            event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Cell ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(M2)");
                            event.getView().close();
                        }
                        break;
                    case "rollcall":
                        if (rollCallList.size() > index) {
                            cell = rollCallList.get(index);
                            if (cell.isVillagerAlive()){
                                cell.removeVillager();
                            }
                            cell.setOpen(true);
                            rollCallList.remove(cell);
                        } else {
                            event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Cell ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(R2)");
                            event.getView().close();
                        }
                        break;
                    default:
                        if (cellList.size() > index) {
                            cell = cellList.get(index);
                            if (cell.isVillagerAlive()){
                                cell.removeVillager();
                            }
                            cell.setOpen(true);
                            cellList.remove(cell);
                        } else {
                            event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Cell ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(C2)");
                            event.getView().close();
                        }
                        break;
                }
                if (cell != null){
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Removed " + cell.getType() + " cell.");
                }
                event.getView().close();
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "         " + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + " Unjail Prisoner " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "         ")){
            // list prisoners to unjail them
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("back"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6)) - 2;
                openGUI((Player) event.getWhoClicked(), "unjail", (int) (Math.pow(10, String.valueOf(page).length() - 1) + page));
            } else if (clickedItem.isSimilar(item.get("next"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6));
                openGUI((Player) event.getWhoClicked(), "unjail", (int) (Math.pow(10, String.valueOf(page).length() - 1) + page));
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            } else if (!clickedItem.isSimilar(item.get("fill"))  && event.getInventory().equals(event.getView().getTopInventory()) && clickedItem.getType() != Material.PAPER){
                assert clickedItem.getItemMeta() != null;
                int index;
                try {
                    index = Integer.parseInt(ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(clickedItem.getItemMeta().getLore().size() - 1)));
                } catch (NumberFormatException e){
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Prisoner ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(3)");
                    event.getView().close();
                    return;
                }
                if (index < prisonerList.size()) {
                    Prisoner prisoner = prisonerList.get(index);
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Unjailed " + ChatColor.DARK_GREEN + prisoner.getName() + ChatColor.GREEN + ".");
                    prisoner.setAction("unJail");
                } else {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Prisoner ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(4)");
                }
                event.getView().close();
            }

        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "         " + ChatColor.DARK_AQUA + "" + ChatColor.BOLD + " Inventories " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "         ")){
            // list prisoners to open inventory
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("back"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6)) - 2;
                openGUI((Player) event.getWhoClicked(), "inventories", (int) (Math.pow(10, String.valueOf(page).length() - 1) + page));
            } else if (clickedItem.isSimilar(item.get("next"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6));
                openGUI((Player) event.getWhoClicked(), "inventories", (int) (Math.pow(10, String.valueOf(page).length() - 1) + page));
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            } else if (!clickedItem.isSimilar(item.get("fill"))  && event.getInventory().equals(event.getView().getTopInventory()) && clickedItem.getType() != Material.PAPER){
                assert clickedItem.getItemMeta() != null;
                int index;
                try {
                    index = Integer.parseInt(ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(clickedItem.getItemMeta().getLore().size() - 1)));
                } catch (NumberFormatException e){
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Prisoner ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(3)");
                    event.getView().close();
                    return;
                }
                if (index < prisonerList.size()) {
                    Prisoner prisoner = prisonerList.get(index);
                    openInventory((Player) event.getWhoClicked(), prisoner);
                    return;
                } else {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Prisoner ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(4)");
                }
                event.getView().close();
            }

        } else if (event.getView().getTitle().contains("'s Inventory")){

            if (event.getInventory().getContents()[0] == null)
                return;
            if (event.getInventory().getContents()[0].getItemMeta() == null)
                return;

            String title = event.getView().getTitle();
            String name = ChatColor.stripColor(title.substring(0, title.indexOf("'s Inventory")));
            UUID uuid;
            try {
                uuid = UUID.fromString(ChatColor.stripColor(event.getInventory().getContents()[0].getItemMeta().getDisplayName()));
            } catch (IllegalArgumentException ignored){
                return;
            }
            if (!Objects.requireNonNull(Bukkit.getOfflinePlayer(uuid).getName()).equalsIgnoreCase(name)){
                return;
            }


            // in player's inventory
            if (event.getClickedInventory() != null)
            if (clickedItem.isSimilar(item.get("exit"))) {
                event.setCancelled(true);
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                event.setCancelled(true);
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            } else if (event.getSlot() == 0 && event.getClickedInventory().equals(event.getView().getTopInventory())){
                event.setCancelled(true);
            } else if (clickedItem.isSimilar(item.get("fill"))){
                event.setCancelled(true);
            }

        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          " + ChatColor.RED + "" + ChatColor.BOLD + " Jail Prisoner " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          ")){
            // select which player to jail
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("exit"))){
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))){
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            } else if (clickedItem.isSimilar(item.get("back"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6)) - 2;
                openGUI((Player) event.getWhoClicked(), "list", (int) (Math.pow(10, String.valueOf(page).length() - 1) + page));
            } else if (clickedItem.isSimilar(item.get("next"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6));
                openGUI((Player) event.getWhoClicked(), "list", (int) (Math.pow(10, String.valueOf(page).length() - 1) + page));
            } else if (!clickedItem.isSimilar(item.get("fill"))  && event.getInventory().equals(event.getView().getTopInventory()) && clickedItem.getType() != Material.PAPER){
                assert clickedItem.getItemMeta() != null;
                assert clickedItem.getItemMeta().getLore() != null;
                List<String> lore = clickedItem.getItemMeta().getLore();
                String uuidString = ChatColor.stripColor(lore.get(1)).substring(6) + ChatColor.stripColor(lore.get(2));
                UUID uuid;
                try {
                    uuid = UUID.fromString(uuidString);
                } catch (IllegalArgumentException ignored){
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting UUID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(1)");
                    event.getView().close();
                    return;
                }
                    openGUI((Player) event.getWhoClicked(), "jailReason", 0, uuid);

            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          " + ChatColor.RED + "" + ChatColor.BOLD + " Select Reason " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          ")){
            // select the reason to jail
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("exit"))){
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))){
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            } else if (clickedItem.getType() == Material.REDSTONE_BLOCK && event.getClickedInventory().equals(event.getView().getTopInventory())){
                String reason = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                // somehow include this reason in the next gui
                ItemStack item = event.getInventory().getContents()[4];
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                List<String> lore = meta.getLore();
                assert lore != null;
                lore.remove(lore.size()-1);
                lore.add(ChatColor.AQUA + "Reason: " + ChatColor.DARK_AQUA + reason);
                lore.add("");
                meta.setLore(lore);
                item.setItemMeta(meta);
                openGUI((Player) event.getWhoClicked(), "jailTime", 3600, item);
            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       " + ChatColor.WHITE + "" + ChatColor.BOLD + " Send to Solitary " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "      ")){
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("back"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6)) - 2;
                openGUI((Player) event.getWhoClicked(), "unjail", (int) (Math.pow(10, String.valueOf(page).length() - 1) + page));
            } else if (clickedItem.isSimilar(item.get("next"))) {
                int page = Integer.parseInt(event.getInventory().getContents()[50].getItemMeta().getDisplayName().substring(6));
                openGUI((Player) event.getWhoClicked(), "unjail", (int) (Math.pow(10, String.valueOf(page).length() - 1) + page));
            } else if (clickedItem.isSimilar(item.get("exit"))) {
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            } else if (!clickedItem.isSimilar(item.get("fill"))  && event.getInventory().equals(event.getView().getTopInventory()) && clickedItem.getType() != Material.PAPER){
                assert clickedItem.getItemMeta() != null;
                int index;
                try {
                    index = Integer.parseInt(ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(clickedItem.getItemMeta().getLore().size() - 1)));
                } catch (NumberFormatException e){
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Prisoner ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(3)");
                    event.getView().close();
                    return;
                }
                if (index < prisonerList.size()) {
                    Prisoner prisoner = prisonerList.get(index);
                    openGUI((Player) event.getWhoClicked(), "solitaryReason", 0, prisoner.getUuid());
                } else {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Error Getting Prisoner ID! Try opening the GUI again." + ChatColor.DARK_GRAY + "(4)");
                    event.getView().close();
                }

            }
        } else if (event.getView().getTitle().equals(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          " + ChatColor.WHITE + "" + ChatColor.BOLD + " Select Reason " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          ")){
            // select the reason for solitary
            event.setCancelled(true);
            if (clickedItem.isSimilar(item.get("exit"))){
                event.getView().close();
            } else if (clickedItem.isSimilar(item.get("returnToMenu"))){
                openGUI((Player) event.getWhoClicked(), "menu", 0);
            } else if (clickedItem.getType() == Material.REDSTONE_BLOCK && event.getInventory().equals(event.getView().getTopInventory())){
                String reason = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                // somehow include this reason in the next gui
                ItemStack item = event.getInventory().getContents()[4];
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                List<String> lore = meta.getLore();
                assert lore != null;
                lore.remove(lore.size()-1);
                lore.add(ChatColor.AQUA + "Reason: " + ChatColor.DARK_AQUA + reason);
                lore.add("");
                meta.setLore(lore);
                item.setItemMeta(meta);
                openGUI((Player) event.getWhoClicked(), "solitaryTime", 3600, item);
            }
        } else if (event.getView().getTitle().contains(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "  ")){
            ItemStack item1 = event.getInventory().getContents()[0];
            if (item1 != null)
                if (item1.getItemMeta() != null)
            if (event.getInventory().getContents()[0].getItemMeta().getDisplayName().equals(item.get("smile").getItemMeta().getDisplayName())) {
                event.setCancelled(true);
                String time = ChatColor.stripColor(event.getInventory().getContents()[22].getItemMeta().getDisplayName());
                long seconds = Long.parseLong(ChatColor.stripColor(event.getInventory().getContents()[0].getItemMeta().getLore().get(0)));
                //Bukkit.getLogger().info(seconds + "");
                ItemStack head = event.getInventory().getContents()[4];
                // check item for confirm item
                if (clickedItem.isSimilar(item.get("exit"))) {
                    event.getView().close();
                } else if (clickedItem.isSimilar(item.get("returnToMenu"))) {
                    openGUI((Player) event.getWhoClicked(), "menu", 0);
                } else if (clickedItem.isSimilar(item.get("confirmJail"))) {
                    // tbd jail player
                    ItemMeta meta = head.getItemMeta();
                    assert meta != null;
                    List<String> lore = meta.getLore();
                    assert lore.size() >= 4;
                    String uuidString = ChatColor.stripColor(lore.get(1)).substring(6) + ChatColor.stripColor(lore.get(2));
                    UUID uuid = UUID.fromString(uuidString);
                    String name = ChatColor.stripColor(head.getItemMeta().getDisplayName());
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        event.getWhoClicked().sendMessage(prefix + ChatColor.GOLD + name + ChatColor.YELLOW + " is not online. If they join they will be jailed.");
                        logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " has put in a request to jail " + name + " when they come online");
                    } else {
                        event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Player has been sent to jail.");
                        logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " has sent " + name + " to jail");
                    }
                    int cell = -1;
                    String reason = ChatColor.stripColor(lore.get(lore.size() - 2)).substring(8);
                    if (seconds == 0){
                        seconds = 60;
                    }
                    Prisoner onlinePrisoner;
                    if (jailedUUID.contains(uuid.toString())) {
                        int i = -1;
                        for (Prisoner prisoner : prisonerList) {
                            if (prisoner.getUuid().equals(uuid))
                                i = prisonerList.indexOf(prisoner);
                        }
                        if (i != -1) {
                            prisonerList.get(i).removeBar();
                            onlinePrisoner = new Prisoner("tbdJail", uuid, name, 0, seconds, cell, reason, null, 0, this);
                            prisonerList.set(i, onlinePrisoner);

                        } else {
                            onlinePrisoner = new Prisoner("tbdJail", uuid, name, 0, seconds, cell, reason, null, 0, this);
                            prisonerList.add(onlinePrisoner);
                        }
                    } else {
                        onlinePrisoner = new Prisoner("tbdJail", uuid, name, 0, seconds, cell, reason, null, 0, this);
                        prisonerList.add(onlinePrisoner);
                        jailedUUID.add(String.valueOf(uuid));
                    }
                    onlinePrisoners.add(onlinePrisoner);
                    File record = new File(jails + File.separator + name + "[" + formatAll.format(now) + "].txt");
                    try {
                        record.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        PrintWriter writer = new PrintWriter(record.getPath(), "UTF-8");
                        writer.println(name + " jailed by " + event.getWhoClicked().getName() + " on " + formatDate.format(now));
                        if (player == null) {
                            writer.println("UUID not available. (Jailed while offline)");
                        } else {
                            writer.println("UUID: " + player.getUniqueId());
                        }

                        writer.println("Time due in jail: " + formatTime(seconds));
                        writer.println("Reason: " + reason);
                        writer.close();
                    } catch (IOException e) {
                        // do something
                    }
                    event.getView().close();
                    return;
                } else if (clickedItem.isSimilar(item.get("+1min"))) {
                    seconds += 60;
                } else if (clickedItem.isSimilar(item.get("+1hour"))) {
                    seconds += 3600;
                } else if (clickedItem.isSimilar(item.get("+12hour"))) {
                    seconds += 43200;
                } else if (clickedItem.isSimilar(item.get("-12hour"))) {
                    seconds -= 43200;
                } else if (clickedItem.isSimilar(item.get("-1hour"))) {
                    seconds -= 3600;
                } else if (clickedItem.isSimilar(item.get("-1min"))) {
                    seconds -= 60;
                }
                if (seconds <= 0) {
                    seconds = 60;
                }
                openGUI((Player) event.getWhoClicked(), "jailTime", seconds, head);
            } else if (event.getInventory().getContents()[0].getItemMeta().getDisplayName().equals(item.get("smile2").getItemMeta().getDisplayName())){
                event.setCancelled(true);
                String time = ChatColor.stripColor(event.getInventory().getContents()[22].getItemMeta().getDisplayName());
                //long seconds = unFormatTime(time);
                long seconds = Long.parseLong(ChatColor.stripColor(event.getInventory().getContents()[0].getItemMeta().getLore().get(0)));
                ItemStack head = event.getInventory().getContents()[4];
                // check item for confirm item
                if (clickedItem.isSimilar(item.get("exit"))){
                    event.getView().close();
                } else if (clickedItem.isSimilar(item.get("returnToMenu"))){
                    openGUI((Player) event.getWhoClicked(), "menu", 0);
                } else if (clickedItem.isSimilar(item.get("confirmSolitary"))){
                    // tbd jail player
                    ItemMeta meta = head.getItemMeta();
                    assert meta != null;
                    List<String> lore = meta.getLore();
                    assert lore != null;
                    assert lore.size() >= 4;
                    String uuidString = ChatColor.stripColor(lore.get(1)).substring(6) + ChatColor.stripColor(lore.get(2));
                    UUID uuid = UUID.fromString(uuidString);
                    String name = ChatColor.stripColor(head.getItemMeta().getDisplayName());
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        event.getWhoClicked().sendMessage(prefix + ChatColor.GOLD + name + ChatColor.YELLOW + " is not online. If they join they will be sent to solitary");
                        logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " has put in a request to put " + name + " in solitary when they come online");
                    } else {
                        event.getWhoClicked().sendMessage(prefix + ChatColor.YELLOW + "Player has been sent to solitary.");
                        logText.add("[" + format.format(now) + "] " + event.getWhoClicked().getName() + " has sent " + name + " to solitary");
                    }
                    int cell = -1;
                    String reason =  ChatColor.stripColor(lore.get(lore.size() - 2)).substring(8);
                    if (jailedUUID.contains(uuid.toString())) {
                        int i = 0;
                        for (Prisoner prisoner : prisonerList) {
                            if (prisoner.getUuid().equals(uuid)){
                                i = prisonerList.indexOf(prisoner);
                                break;
                            }

                        }
                        prisonerList.get(i).putInSolitary(seconds, reason);
                        event.getWhoClicked().sendMessage(prefix + ChatColor.GREEN + "Sent " + prisonerList.get(i).getName() + " to solitary for " + formatTime(seconds) + ".");
                    } else {
                        event.getWhoClicked().sendMessage(prefix + ChatColor.RED + "That player is no longer in jail!");
                    }
                    event.getView().close();
                    return;
                } else if (clickedItem.isSimilar(item.get("+1min"))){
                    seconds += 60;
                } else if (clickedItem.isSimilar(item.get("+1hour"))){
                    seconds += 3600;
                } else if (clickedItem.isSimilar(item.get("+12hour"))){
                    seconds += 43200;
                } else if (clickedItem.isSimilar(item.get("-12hour"))){
                    seconds -= 43200;
                } else if (clickedItem.isSimilar(item.get("-1hour"))){
                    seconds -= 3600;
                } else if (clickedItem.isSimilar(item.get("-1min"))){
                    seconds -= 60;
                }
                if (seconds <= 0){
                    seconds = 60;
                }
                openGUI((Player) event.getWhoClicked(), "solitaryTime", seconds, head);
            }
        }


    }

    public boolean isInteractable(Block block)
    {
        if (block == null)
            return false;
        Material type = block.getType();
        boolean interactable = type.isInteractable();
        if (!interactable)
            return false;

        switch (type)
        {
            case ACACIA_STAIRS:
            case ANDESITE_STAIRS:
            case BIRCH_STAIRS:
            case BLACKSTONE_STAIRS:
            case BRICK_STAIRS:
            case COBBLESTONE_STAIRS:
            case CRIMSON_STAIRS:
            case DARK_OAK_STAIRS:
            case DARK_PRISMARINE_STAIRS:
            case DIORITE_STAIRS:
            case END_STONE_BRICK_STAIRS:
            case GRANITE_STAIRS:
            case JUNGLE_STAIRS:
            case MOSSY_COBBLESTONE_STAIRS:
            case MOSSY_STONE_BRICK_STAIRS:
            case NETHER_BRICK_STAIRS:
            case OAK_STAIRS:
            case POLISHED_ANDESITE_STAIRS:
            case POLISHED_BLACKSTONE_BRICK_STAIRS:
            case POLISHED_BLACKSTONE_STAIRS:
            case POLISHED_DIORITE_STAIRS:
            case POLISHED_GRANITE_STAIRS:
            case PRISMARINE_BRICK_STAIRS:
            case PRISMARINE_STAIRS:
            case PURPUR_STAIRS:
            case QUARTZ_STAIRS:
            case RED_NETHER_BRICK_STAIRS:
            case RED_SANDSTONE_STAIRS:
            case SANDSTONE_STAIRS:
            case SMOOTH_QUARTZ_STAIRS:
            case SMOOTH_RED_SANDSTONE_STAIRS:
            case SMOOTH_SANDSTONE_STAIRS:
            case SPRUCE_STAIRS:
            case STONE_BRICK_STAIRS:
            case STONE_STAIRS:
            case WARPED_STAIRS:

            case ACACIA_FENCE:
            case BIRCH_FENCE:
            case CRIMSON_FENCE:
            case DARK_OAK_FENCE:
            case JUNGLE_FENCE:
            case MOVING_PISTON:
            case NETHER_BRICK_FENCE:
            case OAK_FENCE:
            case PUMPKIN:
            case REDSTONE_ORE:
            case REDSTONE_WIRE:
            case SPRUCE_FENCE:
            case WARPED_FENCE:
                return false;
            default:
                return true;
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (jailedUUID.contains(event.getPlayer().getUniqueId().toString())) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (event.getItem() != null) {
                    if (event.getItem().getData().getItemType().isEdible()) {
                        return;
                    }
                }
            }
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                if (event.getClickedBlock() != null){
                    if (!allowedInteract.contains(event.getClickedBlock().getType())){
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
            if (event.getClickedBlock() != null)
                if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
                    if (allowedBreak.contains(event.getClickedBlock().getType())){
                        event.setCancelled(false);
                    }
                }
            if (event.getItem() != null)
            if (!isInteractable(event.getClickedBlock()))
                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                    if (allowedPlace.contains(event.getItem().getType())){
                        event.setCancelled(false);
                    }
                }


        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getItem() != null) {
                if (event.getItem().isSimilar(getWand())) {
                    if (event.getPlayer().hasPermission("notjail.admin")) {
                        event.setCancelled(true);
                        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            if (selectedPoints.containsKey(event.getPlayer().getUniqueId().toString())) {
                                Location[] locations = selectedPoints.get(event.getPlayer().getUniqueId().toString());
                                locations[1] = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                                selectedPoints.replace(event.getPlayer().getUniqueId().toString(), locations);
                            } else {
                                Location[] locations = new Location[2];
                                locations[1] = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                                selectedPoints.put(event.getPlayer().getUniqueId().toString(), locations);
                            }
                            event.getPlayer().sendMessage(prefix + ChatColor.YELLOW + "Right Click Position Selected.");
                        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            if (selectedPoints.containsKey(event.getPlayer().getUniqueId().toString())) {
                                Location[] locations = selectedPoints.get(event.getPlayer().getUniqueId().toString());
                                locations[0] = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                                selectedPoints.replace(event.getPlayer().getUniqueId().toString(), locations);
                            } else {
                                Location[] locations = new Location[2];
                                locations[0] = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                                selectedPoints.put(event.getPlayer().getUniqueId().toString(), locations);
                            }
                            event.getPlayer().sendMessage(prefix + ChatColor.YELLOW + "Left Click Position Selected.");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (jailedUUID.contains(event.getPlayer().getUniqueId().toString())) {
            if (!allowedBreak.contains(event.getBlock().getType())){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (jailedUUID.contains(event.getPlayer().getUniqueId().toString())) {
            if (!allowedPlace.contains(event.getBlock().getType())){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCommandSend(PlayerCommandPreprocessEvent event) {
        if (jailedUUID.contains(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot use commands while jailed!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("notjail.admin")) {
                    p.sendMessage(ChatColor.GRAY + "(Jailed) " + ChatColor.DARK_GRAY + event.getPlayer().getName() + ChatColor.GRAY + " > " + event.getMessage());
                    logText.add("[" + format.format(now) + "] " + "(Jailed) " + event.getPlayer().getName() + " > " + event.getMessage());
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (jailedUUID.contains(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot chat while jailed!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("notjail.admin")) {
                    p.sendMessage(ChatColor.GRAY + "(Jailed) " + ChatColor.DARK_GRAY + event.getPlayer().getName() + ChatColor.GRAY + " > " + event.getMessage());
                    logText.add("[" + format.format(now) + "] " + "(Jailed) " + event.getPlayer().getName() + " > " + event.getMessage());
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (jailedUUID.contains(event.getDamager().getUniqueId().toString())) {
            if (punishCombat) {
                if (event.getEntity() instanceof Villager)
                    if (event.getEntity().hasMetadata("Prisoner")) {
                        for (Prisoner prisoner : prisonerList) {
                            if (event.getDamager().getUniqueId().equals(prisoner.getUuid())) {
                                event.getDamager().sendMessage(ChatColor.RED + "Harming another prisoner. (+5sec)");
                                prisoner.addSentenceLength(5);
                                prisoner.putInSolitaryNextHour(1);
                                break;
                            }
                        }
                        return;
                    }
                if (event.getEntity() instanceof Player) {
                    if (!event.isCancelled())
                        for (Prisoner prisoner : prisonerList) {
                            if (event.getEntity().getUniqueId().equals(prisoner.getUuid())) {
                                event.getDamager().sendMessage(ChatColor.RED + "Harming another Player. (+10sec)");
                                prisoner.putInSolitaryNextHour(2);
                                prisoner.addSentenceLength(10);
                            }
                        }
                    return;
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (jailedUUID.contains(event.getEntity().getUniqueId().toString()))
                if (!hurt) {
                    event.setCancelled(true);
                } else {
                    if (((Player) event.getEntity()).getHealth() - event.getDamage() <= 0) {
                        event.setCancelled(true);
                        ((Player) event.getEntity()).setHealth(((Player) event.getEntity()).getMaxHealth());
                        if (medicalBayList.isEmpty()) {
                            for (Prisoner prisoner : prisonerList) {
                                if (prisoner.getUuid().equals(event.getEntity().getUniqueId())) {
                                    event.getEntity().teleport(prisoner.getRCell().getSpawn());
                                    break;
                                }
                            }
                        } else {
                            event.getEntity().teleport(medicalBayList.get(0).getSpawn());
                        }
                    }
                }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            if (jailedUUID.contains(event.getEntity().getUniqueId().toString()))
                if (!hunger)
                    event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        if (jailedUUID.contains(event.getPlayer().getUniqueId().toString())) {
            for (Prisoner prisoner : prisonerList) {
                if (prisoner.getUuid().equals(event.getPlayer().getUniqueId())) {
                    if (!prisoner.getRCell().isVillagerAlive()) {
                        prisoner.getRCell().addVillager();
                    }
                    onlinePrisoners.remove(prisoner);
                }
            }

        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if (event.getView().getTitle().contains("'s Inventory")){
            if (event.getInventory().getContents()[0] == null)
                return;
            if (event.getInventory().getContents()[0].getItemMeta() == null)
                return;

            String title = event.getView().getTitle();
            String name = ChatColor.stripColor(title.substring(0, title.indexOf("'s Inventory")));
            UUID uuid;
            try {
                uuid = UUID.fromString(ChatColor.stripColor(event.getInventory().getContents()[0].getItemMeta().getDisplayName()));
            } catch (IllegalArgumentException ignored){
                return;
            }
            if (!Objects.requireNonNull(Bukkit.getOfflinePlayer(uuid).getName()).equalsIgnoreCase(name)){
                return;
            }
            ItemStack[] contents = event.getInventory().getContents();

            // if they still have the data
            if (inventories.containsKey(uuid.toString())){
                ItemStack[] inventoryContents = new ItemStack[36];
                ItemStack[] armorContents = new ItemStack[4];
                ItemStack[] extraContents = new ItemStack[1];

                armorContents[0] = contents[11];
                armorContents[1] = contents[2];
                armorContents[2] = contents[10];
                armorContents[3] = contents[1];

                extraContents[0] = contents[4];

                System.arraycopy(contents, 18, inventoryContents, 0,36);

                inventories.replace(uuid.toString(), inventoryContents);
                armor.replace(uuid.toString(), armorContents);
                extra.replace(uuid.toString(), extraContents);
            }
        }
    }

    public void saveInventory(Player p) {
        if (inventories.containsKey(p.getUniqueId().toString())) {
            inventories.replace(p.getUniqueId().toString(), p.getInventory().getStorageContents());
        } else {
            inventories.put(p.getUniqueId().toString(), p.getInventory().getStorageContents());
        }
        if (armor.containsKey(p.getUniqueId().toString())) {
            armor.replace(p.getUniqueId().toString(), p.getInventory().getArmorContents());
        } else {
            armor.put(p.getUniqueId().toString(), p.getInventory().getArmorContents());
        }
        if (extra.containsKey(p.getUniqueId().toString())) {
            extra.replace(p.getUniqueId().toString(), p.getInventory().getExtraContents());
        } else {
            extra.put(p.getUniqueId().toString(), p.getInventory().getExtraContents());
        }
        logText.add("[" + format.format(now) + "] " + "Inventory of " + p.getName() + ": " + Arrays.toString(p.getInventory().getStorageContents()));
        logText.add("[" + format.format(now) + "] " + "Armor of " + p.getName() + ": " + Arrays.toString(p.getInventory().getArmorContents()));
        logText.add("[" + format.format(now) + "] " + "Off-hand of " + p.getName() + ": " + Arrays.toString(p.getInventory().getExtraContents()));
    }

    public void loadSavedInventory(Player p) {
        if (inventories.containsKey(p.getUniqueId().toString())) {
            p.getInventory().setStorageContents(inventories.get(p.getUniqueId().toString()));
            p.getInventory().setArmorContents(armor.get(p.getUniqueId().toString()));
            p.getInventory().setExtraContents(extra.get(p.getUniqueId().toString()));
            inventories.remove(p.getUniqueId().toString());
            armor.remove(p.getUniqueId().toString());
            extra.remove(p.getUniqueId().toString());
        } else {
            Bukkit.getLogger().warning("Trying to load an inventory that isnt saved!");
        }

    }

    public List<Prisoner> getPrisonerList() {
        return prisonerList;
    }

    public List<Cell> getCellList() {
        return cellList;
    }

    public boolean isIndependentClock() {
        return independentClock;
    }

    public boolean isRequireRollCall() {
        return requireRollCall;
    }

    public boolean isServeOffline() {
        return serveOffline;
    }

    public long getClock() {
        return clock;
    }

    public ItemStack getWand() {
        ItemStack wand = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = wand.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Cell Wand");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.DARK_PURPLE + "Used to create cells");
        lore.add("");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        wand.setItemMeta(meta);
        wand.addUnsafeEnchantment(Enchantment.CHANNELING, 1);
        return wand;
    }

    public List<Cell> getRollCallList() {
        return rollCallList;
    }

    public List<Cell> getHoldingCellList() {
        return holdingCellList;
    }

    public int getNumRolLCall() {
        return numRolLCall;
    }

    public long getNextTime() {
        return nextTime;
    }

    public String getNextTimeSlot() {
        return nextTimeSlot;
    }

    public long getThisTime() {
        return thisTime;
    }

    public boolean isWardenOnline() {
        return Bukkit.getPlayer(wardenName) != null;
    }

    public void addLogText(String s) {
        logText.add("[" + format.format(now) + "] " + s);
    }

    public List<Cell> getSolitaryCellList() {
        return solitaryCellList;
    }

    public List<Cell> getMedicalBayList() {
        return medicalBayList;
    }

    public boolean isPrisoner(UUID uuid) {
        return jailedUUID.contains(uuid.toString());
    }

    public boolean isPrisoner(Player player) {
        return jailedUUID.contains(player.getUniqueId().toString());
    }

    public void openGUI(Player player, String menu, long page, Object extra){
        if (menu.equalsIgnoreCase("jailReason")){
            UUID uuid = (UUID) extra;
            Player toJail = Bukkit.getPlayer(uuid);
            String uuid1 = uuid.toString().substring(0,18);
            String uuid2 = uuid.toString().substring(18);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD); // Create a new ItemStack of the Player Head type.
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta(); // Get the created item's ItemMeta and cast it to SkullMeta so we can access the skull properties
            assert skullMeta != null;
            if (toJail != null) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid)); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
                skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + toJail.getName());

                ArrayList<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + uuid1);
                lore.add(ChatColor.DARK_AQUA + uuid2);
                lore.add("");
                skullMeta.setLore(lore);
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid)); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
                skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + offlinePlayer.getName());
                ArrayList<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + uuid1);
                lore.add(ChatColor.DARK_AQUA + uuid2);
                lore.add(ChatColor.AQUA + "Last Played: " + ChatColor.DARK_AQUA + simple.format(new Date(offlinePlayer.getLastPlayed())));
                lore.add("");
                skullMeta.setLore(lore);
            }
            skull.setItemMeta(skullMeta);

            Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          " + ChatColor.RED + "" + ChatColor.BOLD + " Select Reason " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          ");
            ItemStack[] contents = inventory.getContents();
            Arrays.fill(contents, item.get("fill"));
            contents[0] = item.get("fill2");
            contents[1] = item.get("fill2");
            contents[2] = item.get("fill2");
            contents[3] = item.get("fill2");
            contents[4] = skull;
            contents[5] = item.get("fill2");
            contents[6] = item.get("fill2");
            contents[7] = item.get("fill2");
            contents[8] = item.get("fill2");

            contents[45] = item.get("fill2");
            contents[46] = item.get("fill2");
            contents[47] = item.get("fill2");
            contents[48] = item.get("exit");
            contents[49] = item.get("fill2");
            contents[50] = item.get("returnToMenu");
            contents[51] = item.get("fill2");
            contents[52] = item.get("fill2");
            contents[53] = item.get("fill2");

            for (int i = 0; i < Math.min(presetJailReasons.size(), 36) ; i++){
                ItemStack reason = new ItemStack(Material.REDSTONE_BLOCK);
                ItemMeta meta = reason.getItemMeta();
                assert  meta != null;
                meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + presetJailReasons.get(i));
                reason.setItemMeta(meta);
                contents[i + 9] = reason;
            }

            inventory.setContents(contents);
            player.openInventory(inventory);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("jailTime")){
            // jail time selector - similar to shopgui+ buy selector
            ItemStack head = (ItemStack) extra; // 9 1 7 1 9
            // page is time in minutes
            String timeFormated;
            if (page == 0){
                timeFormated = "1h";
            } else {
                timeFormated = formatTime(page);
            }
            int strikethoughChar = (32 - timeFormated.length()) / 2;
            StringBuilder strikethrough = new StringBuilder();
            for (int i = 0; i < strikethoughChar; i++){
                strikethrough.append(" ");
            }
            // now get how long the strikethough has to be
            Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + strikethrough + ChatColor.RED + "" + ChatColor.BOLD + " " + timeFormated + " " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + strikethrough);
            ItemStack[] contents = inventory.getContents();
            Arrays.fill(contents, item.get("fill"));

            ItemStack smile = item.get("smile");
            ItemMeta meta1 = smile.getItemMeta();
            assert meta1 != null;
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.BLACK + "" + page);
            meta1.setLore(lore);
            smile.setItemMeta(meta1);

            contents[0] = smile;
            contents[1] = item.get("fill2");
            contents[2] = item.get("fill2");
            contents[3] = item.get("fill2");
            contents[4] = head;
            contents[5] = item.get("fill2");
            contents[6] = item.get("fill2");
            contents[7] = item.get("fill2");
            contents[8] = item.get("fill2");

            contents[45] = item.get("fill2");
            contents[46] = item.get("fill2");
            contents[47] = item.get("fill2");
            contents[48] = item.get("exit");
            contents[49] = item.get("fill2");
            contents[50] = item.get("returnToMenu");
            contents[51] = item.get("fill2");
            contents[52] = item.get("fill2");
            contents[53] = item.get("fill2");

            ItemStack time = new ItemStack(Material.CLOCK);
            ItemMeta meta = time.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + timeFormated);
            time.setItemMeta(meta);
            contents[22] = time;

            contents[31] = item.get("confirmJail");

            contents[11] = item.get("+1min");
            contents[20] = item.get("+1hour");
            contents[29] = item.get("+12hour");
            contents[15] = item.get("-1min");
            contents[24] = item.get("-1hour");
            contents[33] = item.get("-12hour");

            contents[48] = item.get("returnToMenu");
            contents[49] = item.get("exit");
            inventory.setContents(contents);
            player.openInventory(inventory);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("solitaryReason")){
            UUID uuid = (UUID) extra;
            Player toJail = Bukkit.getPlayer(uuid);
            String uuid1 = uuid.toString().substring(0,18);
            String uuid2 = uuid.toString().substring(18);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD); // Create a new ItemStack of the Player Head type.
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta(); // Get the created item's ItemMeta and cast it to SkullMeta so we can access the skull properties
            assert skullMeta != null;
            if (toJail != null) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid)); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
                skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + toJail.getName());
                ArrayList<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + uuid1);
                lore.add(ChatColor.DARK_AQUA + uuid2);
                lore.add("");
                skullMeta.setLore(lore);
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid)); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
                skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + offlinePlayer.getName());
                ArrayList<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + uuid1);
                lore.add(ChatColor.DARK_AQUA + uuid2);
                lore.add(ChatColor.AQUA + "Last Played: " + ChatColor.DARK_AQUA + simple.format(new Date(offlinePlayer.getLastPlayed())));
                lore.add("");
                skullMeta.setLore(lore);
            }
            skull.setItemMeta(skullMeta);

            Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          " + ChatColor.WHITE + "" + ChatColor.BOLD + " Select Reason " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          ");
            ItemStack[] contents = inventory.getContents();
            Arrays.fill(contents, item.get("fill"));
            contents[0] = item.get("fill2");
            contents[1] = item.get("fill2");
            contents[2] = item.get("fill2");
            contents[3] = item.get("fill2");
            contents[4] = skull;
            contents[5] = item.get("fill2");
            contents[6] = item.get("fill2");
            contents[7] = item.get("fill2");
            contents[8] = item.get("fill2");

            contents[45] = item.get("fill2");
            contents[46] = item.get("fill2");
            contents[47] = item.get("fill2");
            contents[48] = item.get("exit");
            contents[49] = item.get("fill2");
            contents[50] = item.get("returnToMenu");
            contents[51] = item.get("fill2");
            contents[52] = item.get("fill2");
            contents[53] = item.get("fill2");

            for (int i = 0; i < Math.min(presetJailReasons.size(), 36) ; i++){
                ItemStack reason = new ItemStack(Material.REDSTONE_BLOCK);
                ItemMeta meta = reason.getItemMeta();
                assert  meta != null;
                meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + presetJailReasons.get(i));
                reason.setItemMeta(meta);
                contents[i + 9] = reason;
            }

            inventory.setContents(contents);
            player.openInventory(inventory);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("solitaryTime")){
            // jail time selector - similar to shopgui+ buy selector
            ItemStack head = (ItemStack) extra; // 9 1 7 1 9
            // page is time in minutes
            String timeFormated;
            if (page == 0){
                timeFormated = "1h";
            } else {
                timeFormated = formatTime(page);
            }
            int strikethoughChar = (32 - timeFormated.length()) / 2;
            StringBuilder strikethrough = new StringBuilder();
            for (int i = 0; i < strikethoughChar; i++){
                strikethrough.append(" ");
            }
            // now get how long the strikethough has to be
            Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + strikethrough + ChatColor.WHITE + "" + ChatColor.BOLD + " " + timeFormated + " " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + strikethrough);
            ItemStack[] contents = inventory.getContents();
            Arrays.fill(contents, item.get("fill"));

            ItemStack smile = item.get("smile2");
            ItemMeta meta1 = smile.getItemMeta();
            assert meta1 != null;
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.BLACK + "" + page);
            meta1.setLore(lore);
            smile.setItemMeta(meta1);
            contents[0] = smile;
            contents[1] = item.get("fill2");
            contents[2] = item.get("fill2");
            contents[3] = item.get("fill2");
            contents[4] = head;
            contents[5] = item.get("fill2");
            contents[6] = item.get("fill2");
            contents[7] = item.get("fill2");
            contents[8] = item.get("fill2");

            contents[45] = item.get("fill2");
            contents[46] = item.get("fill2");
            contents[47] = item.get("fill2");
            contents[48] = item.get("exit");
            contents[49] = item.get("fill2");
            contents[50] = item.get("returnToMenu");
            contents[51] = item.get("fill2");
            contents[52] = item.get("fill2");
            contents[53] = item.get("fill2");

            ItemStack time = new ItemStack(Material.CLOCK);
            ItemMeta meta = time.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + timeFormated);
            time.setItemMeta(meta);
            contents[22] = time;

            contents[31] = item.get("confirmSolitary");

            contents[11] = item.get("+1min");
            contents[20] = item.get("+1hour");
            contents[29] = item.get("+12hour");
            contents[15] = item.get("-1min");
            contents[24] = item.get("-1hour");
            contents[33] = item.get("-12hour");

            contents[48] = item.get("returnToMenu");
            contents[49] = item.get("exit");
            inventory.setContents(contents);
            player.openInventory(inventory);
            player.updateInventory();
        }
    }

    public void openGUI(Player player, String menu, int page) {
        if (menu.equalsIgnoreCase("menu")) {
            player.openInventory(menuInv);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("jail")) {
            // open list of online players
            // select reason from preset reasons
            // select time
            Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          " + ChatColor.RED + "" + ChatColor.BOLD + " Jail Prisoner " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          ");
            ItemStack[] contents = inventory.getContents();
            Arrays.fill(contents, item.get("fill"));
            List<Player> playerList = (List<Player>) Bukkit.getOnlinePlayers();
            for (int i = page * 44; i < Math.min(playerList.size(), page * 44 + 44); i++) {
                Player toJail = playerList.get(i);
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD); // Create a new ItemStack of the Player Head type.
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta(); // Get the created item's ItemMeta and cast it to SkullMeta so we can access the skull properties
                assert skullMeta != null;
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(toJail.getUniqueId())); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
                skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + toJail.getName());
                String uuid1 = toJail.getUniqueId().toString().substring(0,18);
                String uuid2 = toJail.getUniqueId().toString().substring(18);
                ArrayList<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + uuid1);
                lore.add(ChatColor.DARK_AQUA + uuid2);
                lore.add("");
                lore.add(ChatColor.GREEN + "Click to Select.");
                skullMeta.setLore(lore);
                skull.setItemMeta(skullMeta);
                contents[i - (page * 44)] = skull;
            }
            contents[49] = item.get("exit");
            if (page > 0) {
                contents[45] = item.get("back");
            }
            if (prisonerList.size() > page * 44) {
                contents[53] = item.get("next");
            }
            contents[48] = item.get("returnToMenu");
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GRAY + "Page: " + page + 1);
            item.setItemMeta(meta);
            contents[50] = item;
            inventory.setContents(contents);
            player.openInventory(inventory);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("unjail")) {
            // list all jailed prisoners
            Inventory inventory = Bukkit.createInventory(player, 54, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "         " + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + " Unjail Prisoner " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "         ");
            ItemStack[] contents = inventory.getContents();
            Arrays.fill(contents, item.get("fill"));
            for (int i = page * 44; i < Math.min(prisonerList.size(), page * 44 + 44); i++) {
                Prisoner prisoner = prisonerList.get(i);
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD); // Create a new ItemStack of the Player Head type.
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta(); // Get the created item's ItemMeta and cast it to SkullMeta so we can access the skull properties
                assert skullMeta != null;
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(prisoner.getUuid())); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
                skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + prisoner.getName());
                ArrayList<String> lore = new ArrayList<>();
                lore.add("");
                //lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                lore.add(ChatColor.AQUA + "Reason: " + ChatColor.DARK_AQUA + prisoner.getReason());
                lore.add(ChatColor.AQUA + "Sentence Length: " + ChatColor.DARK_AQUA + formatTime(prisoner.getSentenceLength()));
                lore.add(ChatColor.AQUA + "Time Left: " + ChatColor.DARK_AQUA + formatTime(prisoner.getSentenceLength() - prisoner.getJailTime()));
                lore.add("");
                lore.add(ChatColor.GREEN + "Click to Unjail.");
                lore.add(ChatColor.BLACK + "" + i);
                skullMeta.setLore(lore);
                skull.setItemMeta(skullMeta);
                contents[i - (page * 44)] = skull;
            }
            contents[49] = item.get("exit");
            if (page > 0) {
                contents[45] = item.get("back");
            }
            if (prisonerList.size() > page * 44) {
                contents[53] = item.get("next");
            }
            contents[48] = item.get("returnToMenu");
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GRAY + "Page: " + page + 1);
            item.setItemMeta(meta);
            contents[50] = item;
            inventory.setContents(contents);
            player.openInventory(inventory);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("createCell")) {
            player.openInventory(createCell);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("removeCell")) {
            player.openInventory(removeCell);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("list")) {
            if (page == 0) {
                // choose btw cells or players
                player.openInventory(chooseList);
                player.updateInventory();
            } else {
                int type = (int) (page / Math.pow(10, String.valueOf(page).length() - 1));
                int extent = page % (int) Math.pow(10, (int) Math.log10(page));
                if (type == 1) {
                    // prisoner list
                    Inventory inventory = Bukkit.createInventory(player, 54, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          " + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + " Prisoner List " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "          ");
                    ItemStack[] contents = inventory.getContents();
                    Arrays.fill(contents, item.get("fill"));
                    for (int i = extent * 44; i < Math.min(prisonerList.size(), extent * 44 + 44); i++) {
                        Prisoner prisoner = prisonerList.get(i);
                        ItemStack skull = new ItemStack(Material.PLAYER_HEAD); // Create a new ItemStack of the Player Head type.
                        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta(); // Get the created item's ItemMeta and cast it to SkullMeta so we can access the skull properties
                        assert skullMeta != null;
                        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(prisoner.getUuid())); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
                        skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + prisoner.getName());
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add("");
                        //lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                        lore.add(ChatColor.AQUA + "Reason: " + ChatColor.DARK_AQUA + prisoner.getReason());
                        lore.add(ChatColor.AQUA + "Sentence Length: " + ChatColor.DARK_AQUA + formatTime(prisoner.getSentenceLength()));
                        lore.add(ChatColor.AQUA + "Time Left: " + ChatColor.DARK_AQUA + formatTime(prisoner.getSentenceLength() - prisoner.getJailTime()));
                        lore.add("");
                        skullMeta.setLore(lore);
                        skull.setItemMeta(skullMeta);
                        contents[i - (extent * 44)] = skull;
                    }
                    contents[49] = item.get("exit");
                    if (extent > 0) {
                        contents[45] = item.get("back");
                    }
                    if (prisonerList.size() > extent * 44) {
                        contents[53] = item.get("next");
                    }
                    contents[48] = item.get("returnToMenu");
                    ItemStack item = new ItemStack(Material.PAPER);
                    ItemMeta meta = item.getItemMeta();
                    assert meta != null;
                    meta.setDisplayName(ChatColor.GRAY + "Page: " + extent + 1);
                    item.setItemMeta(meta);
                    contents[50] = item;
                    inventory.setContents(contents);
                    player.openInventory(inventory);
                    player.updateInventory();
                } else if (type == 2) {
                    // cell list
                    Inventory inventory = Bukkit.createInventory(player, 54, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "            " + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + " Cell List " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "            ");
                    ItemStack[] contents = inventory.getContents();
                    Arrays.fill(contents, item.get("fill"));

                    int index = extent * 44;

                    for (; index < Math.min(cellList.size(), extent * 44 + 44); index++) {
                        Cell cell = cellList.get(index);
                        ItemStack item = new ItemStack(Material.IRON_DOOR);
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + cell.getType());
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                        lore.add(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                        lore.add(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                        lore.add(ChatColor.AQUA + "Has Villager: " + ChatColor.DARK_AQUA + cell.isVillagerAlive());
                        Prisoner prisoner = null;
                        for (Prisoner p : prisonerList) {
                            if (p.getRCell().equals(cell)) {
                                prisoner = p;
                                break;
                            }
                        }
                        if (prisoner != null) {
                            lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + prisoner.getName());
                            //lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                        } else {
                            lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + "Vacant Cell.");
                        }
                        lore.add(ChatColor.YELLOW + "Click " + ChatColor.WHITE + "to teleport!");
                        meta.setLore(lore);
                        if (prisoner != null) {
                            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        }
                        item.setItemMeta(meta);
                        contents[index - (extent * 44)] = item;
                    }
                    if (index >= extent * 44 + 44) {
                        contents[49] = item.get("exit");
                        if (page > 0) {
                            contents[45] = item.get("back");
                        }
                        if (cellList.size() + solitaryCellList.size() + rollCallList.size() + medicalBayList.size() + holdingCellList.size() > page * 44) {
                            contents[53] = item.get("next");
                        }
                        contents[48] = item.get("returnToMenu");
                        ItemStack item = new ItemStack(Material.PAPER);
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.GRAY + "Page: " + extent + 1);
                        item.setItemMeta(meta);
                        contents[50] = item;
                        inventory.setContents(contents);
                        player.openInventory(inventory);
                        player.updateInventory();
                        return;
                    }
                    int prevIndex = index;
                    for (; index < Math.min(solitaryCellList.size() + prevIndex, extent * 44 + 44); index++) {
                        Cell cell = solitaryCellList.get(index - prevIndex);
                        ItemStack item = new ItemStack(Material.DEEPSLATE_BRICK_WALL);
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + cell.getType());
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                        lore.add(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                        lore.add(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                        Prisoner prisoner = null;
                        for (Prisoner p : prisonerList) {
                            if (p.getSolitaryCell().equals(cell)) {
                                prisoner = p;
                                break;
                            }
                        }
                        if (prisoner != null) {
                            lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + prisoner.getName());
                            //lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                        } else {
                            lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + "Vacant Cell.");
                        }
                        lore.add(ChatColor.YELLOW + "Click " + ChatColor.WHITE + "to teleport!");
                        meta.setLore(lore);
                        if (prisoner != null) {
                            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        }
                        item.setItemMeta(meta);
                        contents[index - (extent * 44)] = item;
                    }
                    if (index >= extent * 44 + 44) {
                        contents[49] = item.get("exit");
                        if (page > 0) {
                            contents[45] = item.get("back");
                        }
                        if (cellList.size() + solitaryCellList.size() + rollCallList.size() + medicalBayList.size() + holdingCellList.size() > page * 44) {
                            contents[53] = item.get("next");
                        }
                        contents[48] = item.get("returnToMenu");
                        ItemStack item = new ItemStack(Material.PAPER);
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.GRAY + "Page: " + extent + 1);
                        item.setItemMeta(meta);
                        contents[50] = item;
                        inventory.setContents(contents);
                        player.openInventory(inventory);
                        player.updateInventory();
                        return;
                    }
                    prevIndex = index;
                    for (; index < Math.min(medicalBayList.size() + prevIndex, extent * 44 + 44); index++) {
                        Cell cell = medicalBayList.get(index - prevIndex);
                        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + cell.getType());
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                        lore.add(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                        lore.add(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                        lore.add(ChatColor.YELLOW + "Click " + ChatColor.WHITE + "to teleport!");
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        contents[index - (extent * 44)] = item;
                    }
                    if (index >= extent * 44 + 44) {
                        contents[49] = item.get("exit");
                        if (page > 0) {
                            contents[45] = item.get("back");
                        }
                        if (cellList.size() + solitaryCellList.size() + rollCallList.size() + medicalBayList.size() + holdingCellList.size() > page * 44) {
                            contents[53] = item.get("next");
                        }
                        contents[48] = item.get("returnToMenu");
                        ItemStack item = new ItemStack(Material.PAPER);
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.GRAY + "Page: " + extent + 1);
                        item.setItemMeta(meta);
                        contents[50] = item;
                        inventory.setContents(contents);
                        player.openInventory(inventory);
                        player.updateInventory();
                        return;
                    }
                    prevIndex = index;
                    for (; index < Math.min(holdingCellList.size() + prevIndex, extent * 44 + 44); index++) {
                        Cell cell = holdingCellList.get(index - prevIndex);
                        ItemStack item = new ItemStack(Material.BUCKET);
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + cell.getType());
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                        lore.add(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                        lore.add(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                        List<Prisoner> prisoners = new ArrayList<>();
                        for (Prisoner p : prisonerList) {
                            if (p.getRCell().equals(cell)) {
                                prisoners.add(p);
                            }
                        }
                        lore.add(ChatColor.AQUA + "Prisoners: " + ChatColor.DARK_AQUA + prisoners.size());
                        if (prisoners.size() == 1) {
                            for (Prisoner prisoner : prisoners) {
                                lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + prisoner.getName());
                                //lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                            }
                        } else if (prisoners.size() == 2) {
                            for (Prisoner prisoner : prisoners) {
                                lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + prisoner.getName());
                            }
                        }
                        lore.add(ChatColor.YELLOW + "Click " + ChatColor.WHITE + "to teleport!");
                        meta.setLore(lore);
                        if (prisonerList.size() > 0) {
                            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        }
                        item.setItemMeta(meta);
                        contents[index - (extent * 44)] = item;
                    }
                    if (index >= extent * 44 + 44) {
                        contents[49] = item.get("exit");
                        if (page > 0) {
                            contents[45] = item.get("back");
                        }
                        if (cellList.size() + solitaryCellList.size() + rollCallList.size() + medicalBayList.size() + holdingCellList.size() > page * 44) {
                            contents[53] = item.get("next");
                        }
                        contents[48] = item.get("returnToMenu");
                        ItemStack item = new ItemStack(Material.PAPER);
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.GRAY + "Page: " + extent + 1);
                        item.setItemMeta(meta);
                        contents[50] = item;
                        inventory.setContents(contents);
                        player.openInventory(inventory);
                        player.updateInventory();
                        return;
                    }
                    prevIndex = index;
                    for (; index < Math.min(rollCallList.size() + prevIndex, extent * 44 + 44); index++) {
                        Cell cell = rollCallList.get(index - prevIndex);
                        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + cell.getType());
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                        lore.add(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                        lore.add(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                        lore.add(ChatColor.YELLOW + "Click " + ChatColor.WHITE + "to teleport!");
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        contents[index - (extent * 44)] = item;
                    }
                    contents[49] = item.get("exit");
                    if (extent > 0) {
                        contents[45] = item.get("back");
                    }
                    if (cellList.size() + solitaryCellList.size() + rollCallList.size() + medicalBayList.size() + holdingCellList.size() > extent * 44) {
                        contents[53] = item.get("next");
                    }
                    contents[48] = item.get("returnToMenu");
                    ItemStack item = new ItemStack(Material.PAPER);
                    ItemMeta meta = item.getItemMeta();
                    assert meta != null;
                    meta.setDisplayName(ChatColor.GRAY + "Page: " + extent + 1);
                    item.setItemMeta(meta);
                    contents[50] = item;
                    inventory.setContents(contents);
                    player.openInventory(inventory);
                    player.updateInventory();
                }
            }
        } else if (menu.equalsIgnoreCase("solitary")) {
            // list all jailed players
            Inventory inventory = Bukkit.createInventory(player, 54, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "       " + ChatColor.WHITE + "" + ChatColor.BOLD + " Send to Solitary " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "      ");
            ItemStack[] contents = inventory.getContents();
            Arrays.fill(contents, item.get("fill"));
            for (int i = page * 44; i < Math.min(prisonerList.size(), page * 44 + 44); i++) {
                Prisoner prisoner = prisonerList.get(i);
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD); // Create a new ItemStack of the Player Head type.
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta(); // Get the created item's ItemMeta and cast it to SkullMeta so we can access the skull properties
                assert skullMeta != null;
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(prisoner.getUuid())); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
                skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + prisoner.getName());
                String uuid1 = prisoner.getUuid().toString().substring(0,18);
                String uuid2 = prisoner.getUuid().toString().substring(18);
                ArrayList<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + uuid1);
                lore.add(ChatColor.DARK_AQUA + uuid2);
                lore.add(ChatColor.AQUA + "Reason: " + ChatColor.DARK_AQUA + prisoner.getReason());
                lore.add(ChatColor.AQUA + "Sentence Length: " + ChatColor.DARK_AQUA + formatTime(prisoner.getSentenceLength()));
                lore.add(ChatColor.AQUA + "Time Left: " + ChatColor.DARK_AQUA + formatTime(prisoner.getSentenceLength() - prisoner.getJailTime()));
                lore.add("");
                lore.add(ChatColor.GREEN + "Click to Sent to Solitary.");
                lore.add(ChatColor.BLACK + "" + i);
                skullMeta.setLore(lore);
                skull.setItemMeta(skullMeta);
                contents[i - (page * 44)] = skull;
            }
            contents[49] = item.get("exit");
            if (page > 0) {
                contents[45] = item.get("back");
            }
            if (prisonerList.size() > page * 44) {
                contents[53] = item.get("next");
            }
            contents[48] = item.get("returnToMenu");
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GRAY + "Page: " + page + 1);
            item.setItemMeta(meta);
            contents[50] = item;
            inventory.setContents(contents);
            player.openInventory(inventory);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("cReg")) {
            player.openInventory(createRegularCell);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("cSol")) {
            player.openInventory(createSolitaryCell);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("cHol")) {
            player.openInventory(createHoldingCell);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("cMed")) {
            player.openInventory(createMedicalBay);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("cRol")) {
            player.openInventory(createRollCall);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("removeFromList")){
            Inventory inventory = Bukkit.createInventory(player, 54, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           " + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + " Remove List " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "           ");
            ItemStack[] contents = inventory.getContents();
            int index = page * 44;
            for (; index < Math.min(cellList.size(), page * 44 + 44); index++){
                Cell cell = cellList.get(index);
                ItemStack item = new ItemStack(Material.IRON_DOOR);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + cell.getType());
                ArrayList<String> lore = new ArrayList<>();
                lore.add(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                lore.add(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                lore.add(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                lore.add(ChatColor.AQUA + "Has Villager: " + ChatColor.DARK_AQUA + cell.isVillagerAlive());
                Prisoner prisoner = null;
                for (Prisoner p : prisonerList) {
                    if (p.getRCell().equals(cell)) {
                        prisoner = p;
                        break;
                    }
                }
                if (prisoner != null) {
                    lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + prisoner.getName());
                    //lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                } else {
                    lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + "Vacant Cell.");
                }
                lore.add("");
                lore.add(ChatColor.RED + "Click to remove.");
                lore.add(ChatColor.BLACK + "" + index);
                meta.setLore(lore);
                if (prisoner != null) {
                    item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                item.setItemMeta(meta);
                contents[index - (page * 44)] = item;
            }
            if (index >= page*44 + 44){
                contents[49] = item.get("exit");
                if (page > 0) {
                    contents[45] = item.get("back");
                }
                if (cellList.size() + solitaryCellList.size() + rollCallList.size() + medicalBayList.size() + holdingCellList.size() > page * 44) {
                    contents[53] = item.get("next");
                }
                contents[48] = item.get("returnToMenu");
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.GRAY + "Page: " + page + 1);
                item.setItemMeta(meta);
                contents[50] = item;
                inventory.setContents(contents);
                player.openInventory(inventory);
                player.updateInventory();
                return;
            }
            int prevIndex = index;
            for (; index < Math.min(solitaryCellList.size() + prevIndex, page * 44 + 44); index++){
                Cell cell = solitaryCellList.get(index - prevIndex);
                ItemStack item = new ItemStack(Material.DEEPSLATE_BRICK_WALL);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + cell.getType());
                ArrayList<String> lore = new ArrayList<>();
                lore.add(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                lore.add(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                lore.add(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                Prisoner prisoner = null;
                for (Prisoner p : prisonerList) {
                    if (p.getSolitaryCell().equals(cell)) {
                        prisoner = p;
                        break;
                    }
                }
                if (prisoner != null) {
                    lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + prisoner.getName());
                    //lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                } else {
                    lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + "Vacant Cell.");
                }
                lore.add("");
                lore.add(ChatColor.RED + "Click to remove.");
                lore.add(ChatColor.BLACK + "" + (index-prevIndex));
                meta.setLore(lore);
                if (prisoner != null) {
                    item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                item.setItemMeta(meta);
                contents[index - (page * 44)] = item;
            }
            if (index >= page*44 + 44){
                contents[49] = item.get("exit");
                if (page > 0) {
                    contents[45] = item.get("back");
                }
                if (cellList.size() + solitaryCellList.size() + rollCallList.size() + medicalBayList.size() + holdingCellList.size() > page * 44) {
                    contents[53] = item.get("next");
                }
                contents[48] = item.get("returnToMenu");
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.GRAY + "Page: " + page + 1);
                item.setItemMeta(meta);
                contents[50] = item;
                inventory.setContents(contents);
                player.openInventory(inventory);
                player.updateInventory();
                return;
            }
            prevIndex = index;
            for (; index < Math.min(medicalBayList.size() + prevIndex, page * 44 + 44); index++) {
                Cell cell = medicalBayList.get(index - prevIndex);
                ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + cell.getType());
                ArrayList<String> lore = new ArrayList<>();
                lore.add(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                lore.add(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                lore.add(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                lore.add("");
                lore.add(ChatColor.RED + "Click to remove.");
                lore.add(ChatColor.BLACK + "" + (index-prevIndex));
                meta.setLore(lore);
                item.setItemMeta(meta);
                contents[index - (page * 44)] = item;
            }
            if (index >= page*44 + 44){
                contents[49] = item.get("exit");
                if (page > 0) {
                    contents[45] = item.get("back");
                }
                if (cellList.size() + solitaryCellList.size() + rollCallList.size() + medicalBayList.size() + holdingCellList.size() > page * 44) {
                    contents[53] = item.get("next");
                }
                contents[48] = item.get("returnToMenu");
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.GRAY + "Page: " + page + 1);
                item.setItemMeta(meta);
                contents[50] = item;
                inventory.setContents(contents);
                player.openInventory(inventory);
                player.updateInventory();
                return;
            }
            prevIndex = index;
            for (; index < Math.min(holdingCellList.size() + prevIndex, page * 44 + 44); index++) {
                Cell cell = holdingCellList.get(index - prevIndex);
                ItemStack item = new ItemStack(Material.BUCKET);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + cell.getType());
                ArrayList<String> lore = new ArrayList<>();
                lore.add(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                lore.add(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                lore.add(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                List<Prisoner> prisoners = new ArrayList<>();
                for (Prisoner p : prisonerList) {
                    if (p.getRCell().equals(cell)) {
                        prisoners.add(p);
                    }
                }
                lore.add(ChatColor.AQUA + "Prisoners: " + ChatColor.DARK_AQUA + prisoners.size());
                if (prisoners.size() == 1) {
                    for (Prisoner prisoner : prisoners) {
                        lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + prisoner.getName());
                        //lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                    }
                } else if (prisoners.size() == 2) {
                    for (Prisoner prisoner : prisoners) {
                        lore.add(ChatColor.AQUA + "Prisoner: " + ChatColor.DARK_AQUA + prisoner.getName());
                    }
                }
                lore.add("");
                lore.add(ChatColor.RED + "Click to remove.");
                lore.add(ChatColor.BLACK + "" + (index-prevIndex));
                meta.setLore(lore);
                if (prisonerList.size() > 0) {
                    item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                item.setItemMeta(meta);
                contents[index - (page * 44)] = item;
            }
            if (index >= page*44 + 44){
                contents[49] = item.get("exit");
                if (page > 0) {
                    contents[45] = item.get("back");
                }
                if (cellList.size() + solitaryCellList.size() + rollCallList.size() + medicalBayList.size() + holdingCellList.size() > page * 44) {
                    contents[53] = item.get("next");
                }
                contents[48] = item.get("returnToMenu");
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.GRAY + "Page: " + page + 1);
                item.setItemMeta(meta);
                contents[50] = item;
                inventory.setContents(contents);
                player.openInventory(inventory);
                player.updateInventory();
                return;
            }
            prevIndex = index;
            for (; index < Math.min(rollCallList.size() + prevIndex, page * 44 + 44); index++) {
                Cell cell = rollCallList.get(index - prevIndex);
                ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + cell.getType());
                ArrayList<String> lore = new ArrayList<>();
                lore.add(ChatColor.AQUA + "Cell Spawn: " + ChatColor.DARK_AQUA + (Math.round(cell.getSpawn().getX() * 10) / 10) + "x " + (Math.round(cell.getSpawn().getY() * 10) / 10) + "y " + (Math.round(cell.getSpawn().getZ() * 10) / 10) + "z " + cell.getSpawn().getWorld().getName());
                lore.add(ChatColor.AQUA + "Auto Cell: " + ChatColor.DARK_AQUA + cell.isAutoCell());
                lore.add(ChatColor.AQUA + "Doors: " + ChatColor.DARK_AQUA + cell.getNumDoors());
                lore.add("");
                lore.add(ChatColor.RED + "Click to remove.");
                lore.add(ChatColor.BLACK + "" + (index-prevIndex));
                meta.setLore(lore);
                item.setItemMeta(meta);
                contents[index - (page * 44)] = item;
            }
            contents[49] = item.get("exit");
            if (page > 0) {
                contents[45] = item.get("back");
            }
            if (cellList.size() + solitaryCellList.size() + rollCallList.size() + medicalBayList.size() + holdingCellList.size() > page * 44) {
                contents[53] = item.get("next");
            }
            contents[48] = item.get("returnToMenu");
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GRAY + "Page: " + page + 1);
            item.setItemMeta(meta);
            contents[50] = item;
            inventory.setContents(contents);
            player.openInventory(inventory);
            player.updateInventory();
        } else if (menu.equalsIgnoreCase("inventories")){
            // list all jailed ppl
            Inventory inventory = Bukkit.createInventory(player, 54, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "         " + ChatColor.DARK_AQUA + "" + ChatColor.BOLD + " Inventories " + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "         ");
            ItemStack[] contents = inventory.getContents();
            Arrays.fill(contents, item.get("fill"));
            for (int i = page * 44; i < Math.min(prisonerList.size(), page * 44 + 44); i++) {
                Prisoner prisoner = prisonerList.get(i);
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD); // Create a new ItemStack of the Player Head type.
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta(); // Get the created item's ItemMeta and cast it to SkullMeta so we can access the skull properties
                assert skullMeta != null;
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(prisoner.getUuid())); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
                skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + prisoner.getName());
                ArrayList<String> lore = new ArrayList<>();
                lore.add("");
                //lore.add(ChatColor.AQUA + "UUID: " + ChatColor.DARK_AQUA + prisoner.getUuid());
                lore.add(ChatColor.AQUA + "Reason: " + ChatColor.DARK_AQUA + prisoner.getReason());
                lore.add(ChatColor.AQUA + "Sentence Length: " + ChatColor.DARK_AQUA + formatTime(prisoner.getSentenceLength()));
                lore.add(ChatColor.AQUA + "Time Left: " + ChatColor.DARK_AQUA + formatTime(prisoner.getSentenceLength() - prisoner.getJailTime()));
                lore.add("");
                lore.add(ChatColor.GREEN + "Click to View Inventory.");
                lore.add(ChatColor.BLACK + "" + i);
                skullMeta.setLore(lore);
                skull.setItemMeta(skullMeta);
                contents[i - (page * 44)] = skull;
            }
            contents[49] = item.get("exit");
            if (page > 0) {
                contents[45] = item.get("back");
            }
            if (prisonerList.size() > page * 44) {
                contents[53] = item.get("next");
            }
            contents[48] = item.get("returnToMenu");
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GRAY + "Page: " + page + 1);
            item.setItemMeta(meta);
            contents[50] = item;
            inventory.setContents(contents);
            player.openInventory(inventory);
            player.updateInventory();
        }
    }

    public String formatTime(long seconds) {
        StringBuilder time = new StringBuilder();
        long sLength = seconds;
        if (sLength >= 86400) {
            time.append((int) sLength / 86400).append("d ");
            sLength = sLength % 86400;
        }
        if (sLength >= 3600) {
            time.append((int) sLength / 3600).append("h ");
            sLength = sLength % 3600;
        }
        if (sLength >= 60) {
            time.append((int) sLength / 60).append("m ");
            sLength = sLength % 60;
        }
        if (sLength > 0)
        time.append(sLength).append("s");
        //Bukkit.getLogger().info("Seconds: " + seconds + " = " + time);
        return time.toString();
    }

    public long unFormatTime(String time){

        long seconds = 0;
        // reverse the string
        StringBuilder newTime = new StringBuilder();
        char ch;
        for (int i = 0; i < time.length(); i++){
            ch = time.charAt(i);
            newTime.insert(0, ch);
        }
        time = newTime.toString();
        if (time.contains("s")){
            // get time in string
            String number = time.substring(time.indexOf("s") + 1, indexOfNonNum(time.substring(time.indexOf("s") + 1)) + time.indexOf("s") + 1);
            try {
                // try to ade it to time
                seconds += Long.parseLong(number);
            } catch (NumberFormatException exception){
                Bukkit.getLogger().warning(exception.toString());
            }
            // remove time we used
            time = time.substring(time.indexOf(number) + number.length());
        } else if (time.contains("m")){
            // get time in string
            String number = time.substring(time.indexOf("m") + 1, indexOfNonNum(time.substring(time.indexOf("m") + 1)) + time.indexOf("m") + 1);
            try {
                // try to ade it to time
                seconds += Long.parseLong(number) * 60;
            } catch (NumberFormatException exception){
                Bukkit.getLogger().warning(exception.toString());
            }
            // remove time we used
            time = time.substring(time.indexOf(number) + number.length());
        } else if (time.contains("h")){
            // get time in string
            String number = time.substring(time.indexOf("h") + 1, indexOfNonNum(time.substring(time.indexOf("h") + 1)) + time.indexOf("h") + 1);
            try {
                // try to ade it to time
                seconds += Long.parseLong(number) * 3600;
            } catch (NumberFormatException exception){
                Bukkit.getLogger().warning(exception.toString());
            }
            // remove time we used
            time = time.substring(time.indexOf(number) + number.length());
        } else if (time.contains("d")){
            // get time in string
            String number = time.substring(time.indexOf("d") + 1, indexOfNonNum(time.substring(time.indexOf("d") + 1)) + time.indexOf("d") + 1);
            try {
                // try to ade it to time
                seconds += Long.parseLong(number) * 86400;
            } catch (NumberFormatException exception){
                Bukkit.getLogger().warning(exception.toString());
            }
        }
        Bukkit.getLogger().info("Time: " + time + " = " + seconds);
        return seconds;
    }

    public int indexOfNonNum(String str){
        int index = str.length();
        for (int i = 0; i < str.length(); i++){
            char ch = str.charAt(i);
            if (ch != 0 && ch != 1 && ch != 2 && ch != 3 && ch != 4 && ch != 5 && ch != 6 && ch != 7 && ch != 8 && ch != 9){
                index = i;
                break;
            }
        }
        return index;
    }

    public void openInventory(Player player, Prisoner prisoner){
        player.sendMessage(ChatColor.GREEN + "Opening Inventory of " + ChatColor.DARK_GREEN + prisoner.getName() + ChatColor.GREEN + ".");
        // open inventory - save to hashmaps when they close the inventory - check to make sure there is still a spot in the map
        ItemStack[] inventoryContents = inventories.get(prisoner.getUuid().toString());
        ItemStack[] armorContents = armor.get(prisoner.getUuid().toString());
        ItemStack[] extraContents = extra.get(prisoner.getUuid().toString());
        Inventory inventory = Bukkit.createInventory(player, 54, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + prisoner.getName() + "'s Inventory");
        ItemStack[] contents = inventory.getContents();
        Arrays.fill(contents, item.get("fill"));
        // armor
        contents[1] = armorContents[3];
        contents[10] = armorContents[2];
        contents[2] = armorContents[1];
        contents[11] = armorContents[0];
        // offhand
        contents[4] = extraContents[0];
        // inventory
        System.arraycopy(inventoryContents, 0, contents, 18, 36); // damn didnt know this was a thing
        // leave things
        contents[6] = item.get("returnToMenu");
        contents[7] = item.get("exit");
        // uuid for later usage
        ItemStack info = item.get("fill");
        ItemMeta meta = info.getItemMeta();
        assert meta != null;
        meta.setDisplayName(prisoner.getUuid().toString());
        info.setItemMeta(meta);
        contents[0] = info;

        inventory.setContents(contents);

        player.openInventory(inventory);
        player.updateInventory();
    }

    @EventHandler
    public void onJoin (PlayerJoinEvent event){
        // check if they are logged yet
        if (!loggedPlayers.containsValue(event.getPlayer().getUniqueId().toString())){
            // if not, add them
            loggedPlayers.put(event.getPlayer().getName().toLowerCase(Locale.ROOT), event.getPlayer().getUniqueId().toString());
        } else {
            // if they are, check if their username has changed, and update it
            if (!loggedPlayers.containsKey(event.getPlayer().getName().toLowerCase(Locale.ROOT))){
                String nameToRemove = "";
                for (Map.Entry<String, String> entry : loggedPlayers.entrySet()) {
                    if (entry.getValue().equals(event.getPlayer().getUniqueId().toString())){
                        nameToRemove = entry.getKey();
                    }
                }
                if (!Objects.equals(nameToRemove, "")) {
                    loggedPlayers.remove(nameToRemove);
                }
                loggedPlayers.put(event.getPlayer().getName().toLowerCase(Locale.ROOT), event.getPlayer().getUniqueId().toString());
            }
        }
        if (jailedUUID.contains(event.getPlayer().getUniqueId().toString())){
            for (Prisoner prisoner : prisonerList){
                if (prisoner.getUuid().equals(event.getPlayer().getUniqueId())){
                    if (prisoner.getRCell().isVillagerAlive()){
                        prisoner.getRCell().removeVillager();
                    }
                    onlinePrisoners.add(prisoner);
                }
            }
        }
    }

    @EventHandler
    public void onTp(EntityTeleportEvent event){
        if (event.getEntity() instanceof Tameable){
            if (((Tameable) event.getEntity()).isTamed()){
                if (((Tameable) event.getEntity()).getOwner() instanceof Player){
                    if (jailedUUID.contains(((Tameable) event.getEntity()).getOwner().getUniqueId().toString())){
                        event.setCancelled(true);
                        if (event.getEntity() instanceof Sittable){
                            ((Sittable) event.getEntity()).setSitting(true);
                        }
                    }
                }
            }
        }
    }
}
