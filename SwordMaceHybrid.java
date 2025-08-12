package com.example.swordmace;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class SwordMaceHybrid extends JavaPlugin implements Listener {

    private NamespacedKey hybridKey;

    @Override
    public void onEnable() {
        this.hybridKey = new NamespacedKey(this, "hybrid_weapon");
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("giveswordmace").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player) && args.length == 0) {
                sender.sendMessage(ChatColor.RED + "You must specify a player from console.");
                return true;
            }

            Player target;
            if (args.length > 0) {
                target = Bukkit.getPlayerExact(args[0]);
            } else {
                target = (Player) sender;
            }

            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            ItemStack sword = createHybridSword();
            target.getInventory().addItem(sword);
            target.sendMessage(ChatColor.GOLD + "You have been given the Soulbreaker!");

            return true;
        });
    }

    private ItemStack createHybridSword() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Soulbreaker");
        meta.setLore(Arrays.asList(ChatColor.DARK_PURPLE + "A sword and mace fused together.",
                                   ChatColor.GRAY + "Fast strikes + crushing blows."));
        meta.addEnchant(Enchantment.DAMAGE_ALL, 5, true); // Sharpness V
        meta.addEnchant(Enchantment.DURABILITY, 3, true); // Unbreaking III
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(hybridKey, PersistentDataType.BYTE, (byte) 1);

        sword.setItemMeta(meta);
        return sword;
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isHybridSword(item)) return;

        if (player.getFallDistance() >= 1.5F) {
            double bonusDamage = 6.0;
            event.setDamage(event.getDamage() + bonusDamage);

            Entity target = event.getEntity();
            if (target instanceof LivingEntity) {
                LivingEntity le = (LivingEntity) target;
                le.setVelocity(le.getVelocity().add(player.getLocation().getDirection().multiply(0.5).setY(0.2)));
            }

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 0.8f);
            player.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, 
                    player.getLocation(), 20, 0.5, 0.5, 0.5, Material.NETHERITE_BLOCK.createBlockData());
        }
    }

    private boolean isHybridSword(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD) return false;
        if (!item.hasItemMeta()) return false;
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        return data.has(hybridKey, PersistentDataType.BYTE);
    }
}
