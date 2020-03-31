package com.gmail.filoghost.witherdecay;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WitherDecay extends JavaPlugin implements Listener {
	
	private static final int INVULNERABILITY_TICKS = 220;
	
	private Set<Wither> withers = new HashSet<>();
	
	@Override
	public void onEnable() {
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		// Task per danneggiare i wither (alta frequenza)
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for (Iterator<Wither> iterator = withers.iterator(); iterator.hasNext();) {
					Wither wither = iterator.next();
					
					if (!wither.isValid()) {
						iterator.remove();
						continue;
					}
					
					int ticksLived = wither.getTicksLived();
					ticksLived = Math.max(ticksLived - INVULNERABILITY_TICKS, 0); // Inizia a calcolare il danno dopo l'invulnerabilità (220 ticks dallo spawn)
					
					if (ticksLived > 0) {
						double damage = Math.min(ticksLived / 200.0, 5.0); // Danneggia sempre di più il wither, con un massimo di 5 danni per volta
						
						if (wither.getHealth() > damage) {
							wither.setHealth(wither.getHealth() - damage);
						} else {
							wither.damage(damage); // Last hit per i drop e il suono della morte
						}
					}
				}
			}
		}.runTaskTimer(this, 0, 2);
		
		// Task per rilevare i wither
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for (World world : Bukkit.getWorlds()) {
					for (Wither wither : world.getEntitiesByClass(Wither.class)) {
						withers.add(wither); // Se esiste già non viene aggiunto
					}
				}
			}
		}.runTaskTimer(this, 0, 20);
	}
	
	@EventHandler
	public void onWitherDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.WITHER) {
			getLogger().info("Un Wither ha vissuto per " + ((event.getEntity().getTicksLived() - INVULNERABILITY_TICKS) / 20.0) + " secondi.");
		}
	}
	
	
	
}
