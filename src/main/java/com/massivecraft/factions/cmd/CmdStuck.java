package com.massivecraft.factions.cmd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.massivecraft.factions.*;
import com.massivecraft.factions.config.FactionConfig;
import com.massivecraft.factions.integration.Essentials;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.SpiralTask;
import com.massivecraft.factions.zcore.util.TL;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Singleton
public class CmdStuck extends FCommand {

    @Inject
    private FactionConfig config;

    public CmdStuck() {
        super();

        this.aliases.add("stuck");
        this.aliases.add("halp!"); // halp! c:

        this.requirements = new CommandRequirements.Builder(Permission.STUCK).build();
    }

    @Override
    public void perform(final CommandContext context) {
        final Player player =context.fPlayer.getPlayer();
        final Location sentAt = player.getLocation();
        final FLocation chunk =context.fPlayer.getLastStoodAt();
        final long delay = config.hcf.stuckDelay;
        final int radius = config.hcf.stuckRadius;

        if (P.p.getStuckMap().containsKey(player.getUniqueId())) {
            long wait = P.p.getTimers().get(player.getUniqueId()) - System.currentTimeMillis();
            String time = DurationFormatUtils.formatDuration(wait, TL.COMMAND_STUCK_TIMEFORMAT.toString(), true);
            context.msg(TL.COMMAND_STUCK_EXISTS, time);
        } else {

            // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
            if (!context.payForCommand(Conf.econCostStuck, TL.COMMAND_STUCK_TOSTUCK.format(context.fPlayer.getName()), TL.COMMAND_STUCK_FORSTUCK.format(context.fPlayer.getName()))) {
                return;
            }

            final int id = Bukkit.getScheduler().runTaskLater(P.p, new BukkitRunnable() {

                @Override
                public void run() {
                    if (!P.p.getStuckMap().containsKey(player.getUniqueId())) {
                        return;
                    }

                    // check for world difference or radius exceeding
                    final World world = chunk.getWorld();
                    if (world.getUID() != player.getWorld().getUID() || sentAt.distance(player.getLocation()) > radius) {
                        context.msg(TL.COMMAND_STUCK_OUTSIDE.format(radius));
                        P.p.getTimers().remove(player.getUniqueId());
                        P.p.getStuckMap().remove(player.getUniqueId());
                        return;
                    }

                    final Board board = Board.getInstance();
                    // spiral task to find nearest wilderness chunk
                    new SpiralTask(new FLocation(context.player), radius * 2) {

                        @Override
                        public boolean work() {
                            FLocation chunk = currentFLocation();
                            Faction faction = board.getFactionAt(chunk);
                            if (faction.isWilderness()) {
                                int cx = FLocation.chunkToBlock((int) chunk.getX());
                                int cz = FLocation.chunkToBlock((int) chunk.getZ());
                                int y = world.getHighestBlockYAt(cx, cz);
                                Location tp = new Location(world, cx, y, cz);
                                context.msg(TL.COMMAND_STUCK_TELEPORT, tp.getBlockX(), tp.getBlockY(), tp.getBlockZ());
                                P.p.getTimers().remove(player.getUniqueId());
                                P.p.getStuckMap().remove(player.getUniqueId());
                                if (!Essentials.handleTeleport(player, tp)) {
                                    player.teleport(tp);
                                    P.p.debug("/f stuck used regular teleport, not essentials!");
                                }
                                this.stop();
                                return false;
                            }
                            return true;
                        }
                    };
                }
            }, delay * 20).getTaskId();

            P.p.getTimers().put(player.getUniqueId(), System.currentTimeMillis() + (delay * 1000));
            long wait = P.p.getTimers().get(player.getUniqueId()) - System.currentTimeMillis();
            String time = DurationFormatUtils.formatDuration(wait, TL.COMMAND_STUCK_TIMEFORMAT.toString(), true);
            context.msg(TL.COMMAND_STUCK_START, time);
            P.p.getStuckMap().put(player.getUniqueId(), id);
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_STUCK_DESCRIPTION;
    }
}
