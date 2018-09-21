package com.massivecraft.factions.zcore.fupgrade.upgrades;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.zcore.fupgrade.FUpgrade;
import com.massivecraft.factions.zcore.fupgrade.FUpgradeRoot;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import java.util.HashMap;
import java.util.Map;

public class UpgradeSpawner extends FUpgrade {

    private HashMap<Integer, Double> rateMap;

    @Override
    public String id() {
        return "SPAWNER";
    }
    @Override
    public String translation() {
        return TL.UPGRADE_SPAWNER.toString();
    }

    public UpgradeSpawner(FUpgradeRoot root) {
        super(root);
    }

    protected void registerAttributes() {
        rateMap = new HashMap<>();
        for (Map.Entry<Integer, ConfigurationSection> entry : levelConfigs.entrySet()) {
            rateMap.put(entry.getKey(), entry.getValue().getDouble("rate", 1));
        }
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        FLocation factionLoc = new FLocation(event.getLocation());
        Faction factionAt = Board.getInstance().getFactionAt(factionLoc);
        int level = factionAt.getUpgradeLevel(id());

        event.getSpawner().setDelay((int) (event.getSpawner().getDelay() / rateMap.get(level)));
    }

}
