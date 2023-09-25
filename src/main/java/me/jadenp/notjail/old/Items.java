package me.jadenp.notjail.old;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class Items {
    public ItemStack get(String name){
        if (name.equalsIgnoreCase("fill")){
            ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName("");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("fill2")){
            ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName("");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("jailPlayer")){
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Jail Player");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Send a player to jail.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("createCell")){
            ItemStack item = new ItemStack(Material.IRON_BARS);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Create Cell");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Create a cell with cell wand");
            lore.add(ChatColor.GRAY + "or try to automatically find");
            lore.add(ChatColor.GRAY + "a cell where you are standing.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("removeCell")){
            ItemStack item = new ItemStack(Material.IRON_PICKAXE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Remove Cell");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Remove cell from list or remove");
            lore.add(ChatColor.GRAY + "the cell you are standing in.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("wand")){
            ItemStack item = new ItemStack(Material.WOODEN_HOE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Get Jail Wand");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Wand that selects points for");
            lore.add(ChatColor.GRAY + "creating jail cells.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("useSelectedPoints")){
            ItemStack item = new ItemStack(Material.WOODEN_HOE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Use Selected Points");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Use the points selected with");
            lore.add(ChatColor.GRAY + "the jail wand.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("useAutoCell")){
            ItemStack item = new ItemStack(Material.COMPARATOR);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Use Auto Cell");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Try to automatically find a");
            lore.add(ChatColor.GRAY + "room and doors for a jail cell.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("list")){
            ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "List Cells & Prisoners");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "List details about each prisoner");
            lore.add(ChatColor.GRAY + "as well as details about cells.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("reload")){
            ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Reload Plugin");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Reloads the plugin's configuration.");
            lore.add(ChatColor.GRAY + "Do not use this unless you know");
            lore.add(ChatColor.GRAY + "what you are doing.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("solitary")){
            ItemStack item = new ItemStack(Material.IRON_DOOR);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + "Send Prisoner to Solitary");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Send a prisoner to spend time");
            lore.add(ChatColor.GRAY + "in a solitary cell.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }  else if (name.equalsIgnoreCase("info")){
            ItemStack item = new ItemStack(Material.COMPASS);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Cell Info");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Get information about the cell");
            lore.add(ChatColor.GRAY + "you are standing in.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("exit")){
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Exit");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("unjail")){
            ItemStack item = new ItemStack(Material.EMERALD);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Unjail Player");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Release a prisoner. The plugin");
            lore.add(ChatColor.GRAY + "will try to release any offline");
            lore.add(ChatColor.GRAY + "prisoners when they return.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("listPrisoners")){
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "List Prisoners");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("listCells")){
            ItemStack item = new ItemStack(Material.IRON_BARS);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "List Cells");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("next")){
            ItemStack item = new ItemStack(Material.ARROW);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Next Page");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("back")){
            ItemStack item = new ItemStack(Material.ARROW);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Last Page");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("returnToMenu")){
            ItemStack item = new ItemStack(Material.LIGHT_BLUE_BED);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Return to Menu");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("rollCall")){
            ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Roll Call");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "If enabled in the config,");
            lore.add(ChatColor.GRAY + "prisoners must attend");
            lore.add(ChatColor.GRAY + "roll call in this area.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("holdingCell")){
            ItemStack item = new ItemStack(Material.BUCKET);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Holding Cell");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "The cell assigned to");
            lore.add(ChatColor.GRAY + "prisoners when all regular");
            lore.add(ChatColor.GRAY + "cells are full.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("medicalBay")){
            ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Medical Bay");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Prisoners get teleported");
            lore.add(ChatColor.GRAY + "to this area when they get");
            lore.add(ChatColor.GRAY + "hurt " + ChatColor.ITALIC + "enough.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("solitaryCell")){
            ItemStack item = new ItemStack(Material.DEEPSLATE_BRICK_WALL);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Solitary Cell");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "The cell that prisoners");
            lore.add(ChatColor.GRAY + "get teleported to when");
            lore.add(ChatColor.GRAY + "they misbehave.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("regularCell")){
            ItemStack item = new ItemStack(Material.IRON_DOOR);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Regular Cell");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "The cell prisoners will");
            lore.add(ChatColor.GRAY + "be in for sleep and lockdown.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("removeFromList")){
            ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Remove From List");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Remove cell from list of all cells.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("removeFromHere")){
            ItemStack item = new ItemStack(Material.IRON_DOOR);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Remove From This Location");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Try to remove a cell from the");
            lore.add(ChatColor.GRAY + "current location you are standing.");
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("+1min")){
            ItemStack item = new ItemStack(Material.LIME_WOOL);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "+1 min");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("+1hour")){
            ItemStack item = new ItemStack(Material.LIME_CONCRETE_POWDER);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "+1 hour");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("+12hour")){
            ItemStack item = new ItemStack(Material.LIME_CONCRETE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "+12 hours");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("-1min")){
            ItemStack item = new ItemStack(Material.RED_WOOL);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "-1 min");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("-1hour")){
            ItemStack item = new ItemStack(Material.RED_CONCRETE_POWDER);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "-1 hour");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("-12hour")){
            ItemStack item = new ItemStack(Material.RED_CONCRETE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "-12 hours");
            item.setItemMeta(meta);
            return item;
        } else if (name.equalsIgnoreCase("smile")){
            ItemStack confirmation = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta confMeta = confirmation.getItemMeta();
            assert  confMeta != null;
            confMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.MAGIC + "|" + ChatColor.YELLOW + ":)" + ChatColor.MAGIC + "|");
            confirmation.setItemMeta(confMeta);
            return confirmation;
        } else if (name.equalsIgnoreCase("confirmJail")){
            ItemStack confirmation = new ItemStack(Material.IRON_BARS);
            ItemMeta confMeta = confirmation.getItemMeta();
            assert  confMeta != null;
            confMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Jail Player");
            confirmation.setItemMeta(confMeta);
            return confirmation;
        }
        return null;
    }

}
