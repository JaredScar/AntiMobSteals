package com.core.wolfbadger.anti.kill.steal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: MayoDwarf
 * Date: 6/25/14
 * Time: 7:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BukkitRunnable() {
            @Override
            public void run() {
                if(counter.size() >= 1) {
                    for(UUID ids : entityTracker.keySet()) {
                        Player players = Bukkit.getPlayer(ids);
                        if(counter.containsKey(players.getUniqueId())) {
                            Integer i = getAmount(players);
                            counter.put(players.getUniqueId(), i-1);
                            if(getAmount(players) == 0) {
                                removePlayer(players);
                            }
                        }
                    }
                }
            }
        }, 0, 20);
    }

    @Override
    public void onDisable() {}

    final private HashMap<UUID, UUID> entityTracker = new HashMap<UUID, UUID>();
    final private HashMap<UUID, Integer> counter = new HashMap<UUID, Integer>();
    private void removePlayer(Player p) {
        this.counter.remove(p.getUniqueId());
        this.entityTracker.remove(p.getUniqueId());
    }
    public int getAmount(Player p) {
        return this.counter.get(p.getUniqueId());
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        Entity ent = e.getEntity();
        Entity damager = e.getDamager();
        if(damager instanceof Player) {
            Player p = (Player) damager;
            if(entityTracker.containsKey(p.getUniqueId())) {
                if(entityTracker.get(p.getUniqueId()).equals(ent.getUniqueId())) {
                  counter.put(p.getUniqueId(), getConfig().getInt("TimeBeforeExpiration"));
                } else {
                    e.setCancelled(true);
                }
            } else
                if(!entityTracker.containsKey(p.getUniqueId()) && !entityTracker.containsValue(ent.getUniqueId())) {
                    entityTracker.put(p.getUniqueId(), ent.getUniqueId());
                    counter.put(p.getUniqueId(), getConfig().getInt("TimeBeforeExpiration"));
                } else
                    if(!entityTracker.containsKey(p.getUniqueId()) && entityTracker.containsValue(ent.getUniqueId())) {
                        e.setCancelled(true);
                    }
        }
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent e) {
        Entity ent = e.getEntity();
        if(e.getEntity().getKiller() instanceof Player) {
            Player killer = e.getEntity().getKiller();
            if(entityTracker.containsValue(ent.getUniqueId())) {
                entityTracker.remove(killer.getUniqueId());
                counter.remove(killer.getUniqueId());
            }
        }
    }
}
