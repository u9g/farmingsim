package com.example;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Mod(modid = "examplemod", version = "1.0.0")
public class ExampleMod {
    static String s = "x";
    DecimalFormat formatter = new DecimalFormat("##.00");
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        float[] saved_percent = {0};
        float[] percent_per_30sec = {0};
        long[] last_time = {0L};
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (Minecraft.getMinecraft().player != null && sidebarLines().size() > 10) {
                        String rank_on_sidebar = sidebarLines().get(8 - 1)
                                .replace("\uD83C\uDF20", "")
                                .replace("§7|§f Rank: ", "")
                                .replace("\uD83C\uDF20", "")
                                .replace("❈", "")
                                .replace("§9", "")
                                .replaceAll(",", "");
                        String prestige_on_sidebar = sidebarLines().get(7 - 1)
                                .replace("\uD83C\uDF6D§d", "")
                                .replace("✣", "")
                                .replace("§7| §fPrestige: ", "")
                                .replaceAll(",", "");
                        String megapoints_on_sidebar = sidebarLines().get(5 - 1)
                                .replace("§2§lMega Points:\uD83C\uDFC0§2§l §2§l", "")
                                .replaceAll(",", "");
                        int parsed_rank = Integer.parseInt(rank_on_sidebar);
                        int parsed_prestige = Integer.parseInt(prestige_on_sidebar);
                        int parsed_megapoints = Integer.parseInt(megapoints_on_sidebar);
                        Minecraft.getMinecraft().player.experienceLevel = parsed_prestige;
                        int goal_rank = 0;
                        int goal_prestige = 0;
                        int goal_megapoints = 0;
                        if (parsed_prestige >= 1000) {
                            goal_megapoints = 20;
                            goal_prestige = 2000;
                        } else if (parsed_prestige >= 500) {
                            goal_prestige = 1000;
                        } else if (parsed_prestige >= 250) {
                            goal_prestige = 500;
                        } else if (parsed_prestige >= 100) {
                            goal_prestige = 250;
                        } else if (parsed_prestige >= 35) {
                            goal_prestige = 100;
                        } else if (parsed_prestige >= 15) {
                            goal_prestige = 35;
                        } else if (parsed_prestige >= 5) {
                            goal_prestige = 15;
                        } else if (parsed_prestige >= 2) {
                            goal_prestige = 5;
                        } else if (parsed_prestige >= 1) {
                            goal_prestige = 2;
                        } else {
                            if (parsed_rank >= 1000) {
                                goal_prestige = 1;
                            } else if (parsed_rank >= 500) {
                                goal_rank = 1000;
                            } else if (parsed_rank >= 250) {
                                goal_rank = 500;
                            } else if (parsed_rank >= 75) {
                              goal_rank = 250;
                            } else if (parsed_rank >= 20) {
                                goal_rank = 75;
                            } else if (parsed_rank >= 8) {
                                goal_rank = 20;
                            } else if (parsed_rank >= 3) {
                                goal_rank = 8;
                            } else if (parsed_rank >= 1) {
                                goal_rank = 3;
                            } else {
                                goal_rank = 1;
                            }
                        }

                        String money_on_sidebar = sidebarLines().get(11 - 1)
                                .replace("\uD83D\uDD2E§6", "")
                                .replace("⛂", "")
                                .replace("Coins: §6", "")
                                .replace("\uD83D\uDD2E", "")
                                .replaceAll(",", "");
                        String exp_on_sidebar = sidebarLines().get(10 - 1)
                                .replace("\uD83D\uDC0D§3", "")
                                .replace("✫", "")
                                .replace("Exp: §3", "")
                                .replace("\uD83D\uDC0D", "")
                                .replaceAll(",", "");
                        BigDecimal current_money_or_xp = new BigDecimal(exp_on_sidebar)
                                .min(new BigDecimal(money_on_sidebar));
                        BigDecimal wanted_money_or_xp = BigDecimal.valueOf(
                                10_000_000L * (goal_prestige - parsed_prestige) +
                                        (5000 * goal_rank - parsed_rank)
                        );
                        if (goal_megapoints > 0) {
                            wanted_money_or_xp = wanted_money_or_xp.add(
                                    new BigDecimal("1000000000").multiply(
                                            BigDecimal.valueOf(goal_megapoints - parsed_megapoints)));
                        }
                        if (wanted_money_or_xp.compareTo(current_money_or_xp) <= 0) {
                            s = "Done";
                            return;
                        }
                        float percentage = current_money_or_xp
                                .setScale(4, RoundingMode.HALF_UP)
                                .divide(wanted_money_or_xp, RoundingMode.HALF_UP)
                                .min(BigDecimal.ONE)
                                .floatValue();
                        if (System.currentTimeMillis() - last_time[0] > 1000 * 30) {
                            if (saved_percent[0] != 0) {
                                percent_per_30sec[0] = percentage - saved_percent[0];
                            }
                            last_time[0] = System.currentTimeMillis();
                            saved_percent[0] = percentage;
                        }
                        StringBuilder b = new StringBuilder(formatter.format(percentage * 100)).append("%");
                        if (percent_per_30sec[0] > 0) {
                            b.append(" (").append(formatter.format(percent_per_30sec[0] * 2 * 5 * 100)).append("% / 5 mins)");

                            Duration dur = Duration.ofSeconds(30).multipliedBy((int) ((1 - percentage) / percent_per_30sec[0]));
                            String durStr;
                            if (dur.toDays() > 0) {
                                durStr = dur.toDays() + " days";
                            } else if (dur.toHours() > 0) {
                                durStr = dur.toHours() + " hours";
                            } else if (dur.toMinutes() > 0) {
                                durStr = dur.toMinutes() + " mins";
                            } else {
                                durStr = (dur.toMillis() * 1000) + " secs";
                            }
                            b.append(" (").append(durStr).append(" left)");
                        }
                        s = b.toString();
                        Minecraft.getMinecraft().player.experience = percentage;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 50);
    }

    public static List<String> sidebarLines() {
        Scoreboard scoreboard = Minecraft.getMinecraft().world.getScoreboard();
        if (scoreboard == null) return Collections.EMPTY_LIST;
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return Collections.EMPTY_LIST;
        return scoreboard
                .getSortedScores(objective).stream()
                .filter(c -> c != null && c.getPlayerName() != null && !c.getPlayerName().startsWith("#"))
                .limit(15)
                .map(c -> ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(c.getPlayerName()), c.getPlayerName()))
                .collect(Collectors.toList());
    }

    public static SPacketChat handleChat(SPacketChat packetIn) {
        if (packetIn.getType() == ChatType.GAME_INFO) {
//            if (packetIn.getChatComponent().getUnformattedText().equals("§eYou are currently in §a§c§l[ＵＰＤＡＴＥ] §2Ｆａｒｍｉｎｇ Ｓｉｍｕｌａｔｏｒ§e!")) {
//                ci.cancel();
//            }
            return new SPacketChat(new TextComponentString(s), ChatType.GAME_INFO);
        }
        return packetIn;
    }
}

