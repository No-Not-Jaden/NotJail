package me.jadenp.notjail.old;

import me.jadenp.notjail.NotJail;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Prisoner {
    private String action;
    private UUID uuid;
    private long jailTime;
    private long sentenceLength;
    private int cell;
    private String reason;
    private NotJail notJail;
    private String name;
    private String lastTimeSlot;
    private boolean punished;
    private boolean completedRollCall;
    private int rollCallAttendance;
    private BossBar bar;
    private BossBar bar2;
    private long timeSinceJoined;
    private Location lastLocation;
    private int solitaryTime;
    private Cell solitaryCell;
    private int solitaryNextHour;
    private int tries;
    public Prisoner(String action, UUID uuid, String name, long jailTime, long sentenceLength, int cell, String reason, Location lastLocation, int tries, NotJail notJail){
        this.action = action;
        this.uuid = uuid;
        this.jailTime = jailTime;
        this.name = name;
        this.sentenceLength = sentenceLength;
        this.reason = reason;
        this.notJail = notJail;
        this.lastLocation = lastLocation;
        this.tries = tries + 1;
        completedRollCall = false;
        solitaryNextHour = 0;
        punished = false;
        lastTimeSlot = "";
        rollCallAttendance = 0;
        timeSinceJoined = 0;
        solitaryTime = 0;
        if (cell == -1){
            ArrayList<Integer> cells = new ArrayList<>();
            for (int i = 1; i < notJail.getCellList().size() + 1; i++)
                cells.add(i);
            for (Prisoner p: notJail.getPrisonerList()){
                if (!p.getUuid().equals(uuid))
                    cells.remove( (Integer) p.getCell());
            }
            if (cells.isEmpty()){
                this.cell = -2;
            } else {
                this.cell = cells.get(0);
            }
        } else {
            if (notJail.getCellList().size() >= cell) {
                this.cell = cell;
            } else {
                ArrayList<Integer> cells = new ArrayList<>();
                for (int i = 1; i < notJail.getCellList().size() + 1; i++)
                    cells.add(i);
                for (Prisoner p: notJail.getPrisonerList()){
                    if (!p.getUuid().equals(uuid))
                        cells.remove(p.getCell());
                }
                if (cells.isEmpty()){
                    this.cell = -2;
                } else {
                    this.cell = cells.get(0);
                }
            }
        }
        getRCell().removeVillager();

    }

    public void update(String timeSlot){

        if (action.equalsIgnoreCase("jail")){
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                for (Cell cell : notJail.getMedicalBayList()) {
                    if (cell.inCell(p.getLocation())) {
                        if (p.getHealth() < p.getMaxHealth())
                        p.setHealth(p.getHealth() + 1);
                        break;
                    }
                }
                if (!timeSlot.equalsIgnoreCase("lockup") && !timeSlot.equalsIgnoreCase("sleep")){
                    getRCell().markDoor(p);
                }

                timeSinceJoined++;

                int hours = (int) (notJail.getClock() / 1000);
                int minutes = (int) ((notJail.getClock() % 1000) /  ((long) 1000 / 60));
                String m;
                if (minutes < 10) {
                    m = "0" + minutes;
                } else {
                    m = minutes + "";
                }
                String h;
                if (hours < 10) {
                    h = "0" + hours;
                } else {
                    h = hours + "";
                }
                int nhours = (int) (notJail.getNextTime() / 1000);

                String nh;
                if (nhours < 10) {
                    nh = "0" + nhours;
                } else {
                    nh = nhours + "";
                }
                long secLeft = sentenceLength - jailTime;
                long minLeft = ((int) secLeft / 60);
                long hourLeft = ((int) minLeft / 60);
                secLeft = secLeft - (minLeft * 60);
                minLeft = minLeft - (hourLeft * 60);
                String title = ChatColor.YELLOW + "" + ChatColor.BOLD + h + ":" + m + ChatColor.DARK_GRAY + " | " + ChatColor.RED + hourLeft + "h " + minLeft + "m " + secLeft + "s";
                if (bar == null) {
                    bar = Bukkit.createBossBar(title, BarColor.RED, BarStyle.SOLID);
                    bar.addPlayer(p);
                } else {
                    bar.setTitle(title);
                }
                if (bar2 == null) {
                    bar2 = Bukkit.createBossBar(ChatColor.BLUE + "" + ChatColor.BOLD + "Now: " + ChatColor.YELLOW + timeSlot + ChatColor.DARK_GRAY + " | " + ChatColor.DARK_BLUE + ChatColor.BOLD + "Next: " + ChatColor.YELLOW + notJail.getNextTimeSlot() + ChatColor.WHITE + " at " + ChatColor.YELLOW + nh + ":00", BarColor.BLUE, BarStyle.SOLID);
                    bar2.addPlayer(p);
                } else {
                    bar2.setTitle(ChatColor.BLUE + "" + ChatColor.BOLD + "Now: " + ChatColor.YELLOW + timeSlot + ChatColor.DARK_GRAY + " | " + ChatColor.DARK_BLUE + ChatColor.BOLD + "Next: " + ChatColor.YELLOW + notJail.getNextTimeSlot() + ChatColor.WHITE + " at " + ChatColor.YELLOW + nh + ":00");
                }
                bar.setProgress(((double) sentenceLength - jailTime) / sentenceLength);
                if (!timeSlot.equalsIgnoreCase("sleep")) {
                    bar2.setProgress((double) 1 / ((double) (notJail.getNextTime() - notJail.getThisTime()) / (notJail.getNextTime() - notJail.getClock())));
                } else {
                    long time;
                    if (notJail.getClock() >= 20000) {
                        time = notJail.getClock() - 20000;
                    } else {
                        time = notJail.getClock() + 4000;
                    }
                    bar2.setProgress((double) 1 / ((double) (8000) / (8000 - time)));
                }
                if (!bar.getPlayers().contains(p)) {
                    bar.addPlayer(p);
                }
                if (!bar2.getPlayers().contains(p)) {
                    bar2.addPlayer(p);
                }
                if (solitaryTime == 0) {
                    if (solitaryCell != null)
                    solitaryCell = null;
                    if (!lastTimeSlot.equals(timeSlot)) {
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE,1,1);
                        if (lastTimeSlot.equalsIgnoreCase("lockup")) {
                            if (punished && timeSinceJoined > 1000) {
                                p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✖" + ChatColor.GRAY + "] " + ChatColor.GOLD + "Not in cell for lockup. (+1m)");
                                sentenceLength += 60;
                                notJail.addLogText(p.getName() + " not in cell for lockup (+1m)");
                            }
                        } else if (lastTimeSlot.equalsIgnoreCase("sleep")) {
                            if (punished && timeSinceJoined > 1000) {
                                p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✖" + ChatColor.GRAY + "] " + ChatColor.GOLD + "Not in cell at night. (+1m)");
                                sentenceLength += 60;
                                notJail.addLogText(p.getName() + " not in cell for night (+1m)");
                            }
                        }
                        lastTimeSlot = timeSlot;
                        punished = false;
                        completedRollCall = false;
                        if (timeSlot.equalsIgnoreCase("sleep")) {
                            if (notJail.getNumRolLCall() == rollCallAttendance) {
                                if (notJail.isRequireRollCall()) {
                                    p.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "✔" + ChatColor.GRAY + "] " + ChatColor.GOLD + "Attended all roll calls.");
                                } else {
                                    p.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "✔" + ChatColor.GRAY + "] " + ChatColor.GOLD + "Attended all roll calls. (-5m)");
                                    sentenceLength -= 300;
                                    notJail.addLogText(p.getName() + " attended all roll calls (-5m)");
                                }

                            } else {
                                if (timeSinceJoined > 1000)
                                    if (notJail.isRequireRollCall()) {
                                        p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✖" + ChatColor.GRAY + "] " + ChatColor.GOLD + "Absent on one or more roll calls. (+5m)");
                                        sentenceLength += 300;
                                        notJail.addLogText(p.getName() + " absent for one or more roll calls (+5m)");
                                    } else {
                                        p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "✖" + ChatColor.GRAY + "] " + ChatColor.GOLD + "Absent on one or more roll calls.");
                                    }
                            }
                            rollCallAttendance = 0;
                        }
                        if (solitaryNextHour != 0){
                            if (solitaryNextHour == 1){
                                // hurting villager
                                putInSolitary(300,"Harming another prisoner");
                            } else if (solitaryNextHour == 2){
                                putInSolitary(600,"Harming another player");
                            }

                            solitaryNextHour = 0;
                        }
                    }
                    if (timeSlot.equalsIgnoreCase("lockup")) {
                        if (!getRCell().inCell(p.getLocation())) {
                            p.teleport(getRCell().getSpawn());
                            if (!punished) {
                                punished = true;
                            }
                            p.sendMessage(ChatColor.RED + "You are not allowed outside your cell at this time! (Lockup)");
                        }
                    } else if (timeSlot.equalsIgnoreCase("Roll Call")) {
                        if (!completedRollCall)
                            for (Cell cell : notJail.getRollCallList()) {
                                if (cell.inCell(p.getLocation())) {
                                    completedRollCall = true;
                                    rollCallAttendance++;
                                    p.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "✔" + ChatColor.GRAY + "] " + ChatColor.GOLD + "Attended Roll Call.");
                                    break;
                                }
                            }
                    } else if (timeSlot.equalsIgnoreCase("sleep")) {
                        if (!getRCell().inCell(p.getLocation())) {
                            p.teleport(getRCell().getSpawn());
                            if (!punished) {
                                punished = true;
                            }
                            p.sendMessage(ChatColor.RED + "You are not allowed outside your cell at this time! (Sleep)");
                        }
                    }
                } else {
                    // do solitary things
                    if (solitaryCell == null){
                        List<Cell> openSolitary = notJail.getSolitaryCellList();
                        for (Prisoner prisoner : notJail.getPrisonerList()){
                            if (prisoner.getUuid() != uuid)
                                if (prisoner.getSolitaryCell() != null){
                                    openSolitary.remove(prisoner.getSolitaryCell());
                                }
                        }
                        if (openSolitary.size() > 0)
                            solitaryCell = openSolitary.get(0);
                        else
                            solitaryTime = 1;
                    }
                    if (solitaryTime == 1){
                        solitaryTime = 0;
                        p.sendMessage(ChatColor.YELLOW + "You are free from solitary.");
                        p.teleport(getRCell().getSpawn());
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
                    } else {
                        if (!solitaryCell.inCell(p.getLocation())) {
                            p.teleport(solitaryCell.getSpawn());
                            p.sendMessage(ChatColor.RED + "You are not allowed outside solitary for " + solitaryTime + " more seconds.");
                        }
                    }

                }
            } else {
                timeSinceJoined = 0;

            }
            if (notJail.serveOffline) {
                jailTime++;
                if (solitaryTime > 0)
                    solitaryTime--;
                if (jailTime > sentenceLength) {
                    action = "unjail";
                }

            } else {
                if (Bukkit.getPlayer(uuid) != null) {
                    jailTime++;
                    if (solitaryTime > 0){
                        solitaryTime--;
                    }
                    if (jailTime > sentenceLength) {
                        action = "unjail";
                    }
                }
            }
            if (tries > 30){
                action = "unJail";
                cell = -3;
            }
        } else if (action.equalsIgnoreCase("tbdJail")){
            Player p = Bukkit.getPlayer(name);
            if (p != null){
                // jail
                uuid = p.getUniqueId();
                notJail.saveInventory(p);
                lastLocation = p.getLocation();
                ItemStack[] contents = p.getInventory().getContents();
                ItemStack[] armorContents = p.getInventory().getArmorContents();
                Arrays.fill(contents, null);
                Arrays.fill(armorContents, null);
                p.getInventory().setContents(contents);
                p.getInventory().setArmorContents(armorContents);
                p.updateInventory();
                p.teleport(getRCell().getSpawn());
                p.teleport(getRCell().getSpawn());
                p.sendMessage(ChatColor.RED + "You have been jailed for: " + reason + ".");
                action = "jail";
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1,1);
            } else {
                if (tries > 14){
                    action = "unJail";
                    cell = -3;
                }
            }
        }
    }

    public int getCell() {
        return cell;
    }

    public long getJailTime() {
        return jailTime;
    }

    public Cell getRCell(){
        if (cell == -2){
            return notJail.getHoldingCellList().get(0);
        }
        return notJail.getCellList().get(cell - 1);
    }

    public long getSentenceLength() {
        return sentenceLength;
    }

    public String getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void addSentenceLength(long seconds){
        sentenceLength += seconds;
    }

    public void removeBar(){
        if (bar != null)
        bar.removeAll();
        if (bar2 != null)
        bar2.removeAll();
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void putInSolitary(int time, String reason){
        List<Cell> openSolitary = notJail.getSolitaryCellList();
        for (Prisoner prisoner : notJail.getPrisonerList()){
            if (prisoner.getUuid() != uuid)
            if (prisoner.getSolitaryCell() != null){
                openSolitary.remove(prisoner.getSolitaryCell());
            }
        }
        if (!openSolitary.isEmpty()) {
            solitaryCell = openSolitary.get(0);
            solitaryTime = time;
            int minutes = time / 60;
            int seconds = time - (minutes * 60);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(ChatColor.RED + "You have been put into solitary for " + minutes + "m " + seconds + "s for " + reason + ".");
                p.teleport(solitaryCell.getSpawn());
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_BREAK,1,1);
            }
            notJail.addLogText(name + " has been put into solitary for " + minutes + "m " + seconds + "s for " + reason);


        } else {
            Bukkit.getLogger().warning("No Available Solitary Cells");
            notJail.addLogText(name + " was not approved to go to solitary because of the lack of cells");
        }
    }

    public Cell getSolitaryCell() {
        return solitaryCell;
    }

    public void putInSolitaryNextHour(int i){
        solitaryNextHour = i;
    }

    public int getTries() {
        return tries;
    }
}
