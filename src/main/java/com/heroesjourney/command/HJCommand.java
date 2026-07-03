package com.heroesjourney.command;

import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.hero.HeroDefinition;
import com.heroesjourney.hero.HeroRegistry;
import com.heroesjourney.network.HJNetworking;
import com.heroesjourney.quest.QuestManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Admin/debug command. Not gated behind any permission mod - relies on vanilla's op-level
 * requirement (level 2) like other server-admin commands.
 * <p>
 * {@code /heroesjourney progress <player> <hero> <stageIndex>} - jump straight to a stage,
 * skipping/replaying objectives, for testing without playing the whole questline every time.
 */
public final class HJCommand {

    private HJCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("heroesjourney")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("progress")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("hero", StringArgumentType.word())
                                        .then(Commands.argument("stage", IntegerArgumentType.integer(0))
                                                .executes(HJCommand::setProgress)))))
                .then(Commands.literal("activate")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("hero", StringArgumentType.word())
                                        .executes(HJCommand::activate))))
                .then(Commands.literal("unlockability")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("ability", StringArgumentType.word())
                                        .executes(HJCommand::unlockAbility))))
                .then(Commands.literal("listheroes").executes(HJCommand::listHeroes))
        );
    }

    private static int setProgress(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        String heroId = StringArgumentType.getString(ctx, "hero");
        int stage = IntegerArgumentType.getInteger(ctx, "stage");
        if (HeroRegistry.get(heroId).isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Unknown hero: " + heroId));
            return 0;
        }
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        HeroProgress progress = data.getOrCreateProgress(heroId);
        progress.setStageIndex(stage);
        HJNetworking.syncHeroData(player);
        ctx.getSource().sendSuccess(() -> Component.literal("Set " + player.getGameProfile().getName() + "'s " + heroId + " stage to " + stage), true);
        return 1;
    }

    private static int activate(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        String heroId = StringArgumentType.getString(ctx, "hero");
        QuestManager.INSTANCE.requestActivateHero(player, heroId);
        ctx.getSource().sendSuccess(() -> Component.literal("Activated " + heroId + " for " + player.getGameProfile().getName()), true);
        return 1;
    }

    private static int unlockAbility(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        String abilityId = StringArgumentType.getString(ctx, "ability");
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        if (!data.hasActiveHero()) {
            ctx.getSource().sendFailure(Component.literal("Player has no active hero."));
            return 0;
        }
        data.getOrCreateProgress(data.activeHero()).unlockAbility(abilityId);
        HJNetworking.syncHeroData(player);
        ctx.getSource().sendSuccess(() -> Component.literal("Unlocked ability " + abilityId), true);
        return 1;
    }

    private static int listHeroes(CommandContext<CommandSourceStack> ctx) {
        StringBuilder sb = new StringBuilder("Registered heroes: ");
        for (HeroDefinition hero : HeroRegistry.all()) {
            sb.append(hero.id()).append(" (").append(hero.questline().size()).append(" stages)  ");
        }
        ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }
}
