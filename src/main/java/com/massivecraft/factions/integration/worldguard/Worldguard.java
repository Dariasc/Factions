package com.massivecraft.factions.integration.worldguard;

import com.massivecraft.factions.FLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Worldguard {

    public boolean isPVP(Player player);

    public boolean playerCanBuild(Player player, Location loc);

    public boolean checkForRegionsInChunk(FLocation flocation);

}
