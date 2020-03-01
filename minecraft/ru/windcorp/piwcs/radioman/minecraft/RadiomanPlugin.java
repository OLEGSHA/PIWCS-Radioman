package ru.windcorp.piwcs.radioman.minecraft;

import java.io.FileNotFoundException;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.earth2me.essentials.Essentials;

import ru.windcorp.piwcs.radioman.RadiomanTemplates;

public class RadiomanPlugin extends JavaPlugin {
	
	public static RadiomanPlugin inst = null;
	private static BukkitTask minecraftBroadcaster = null;
	
	private static Function<Player, String> nicknameGetter = Player::getDisplayName;
	
	@Override
	public void onEnable() {
		inst = this;
		try {
			RadiomanTemplates.load();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		getLogger().info("Connecting to bot in separate thread");
		new Thread(RadiomanClient::runClient, "PIWCS Radioman Bot Connection").start();
		minecraftBroadcaster = Bukkit.getScheduler().runTaskTimer(this, RadiomanEngineMinecraft::doBroadcasts, 0, 10);
		Bukkit.getPluginManager().registerEvents(new RadiomanEngineMinecraft(), this);
		
		try {
			
			Essentials ess = getPlugin(Essentials.class);
			nicknameGetter = player -> ess.getUser(player).getNick(true);
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void onDisable() {
		minecraftBroadcaster.cancel();
		RadiomanClient.stop();
	}
	
	public static void shutdown() {
		Bukkit.getPluginManager().disablePlugin(inst);
	}
	
	public static String getDisplayName(Player player) {
		return nicknameGetter.apply(player);
	}

}
