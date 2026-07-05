package com.heroesjourney.command;

import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.hero.HeroDefinition;
import com.heroesjourney.hero.HeroRegistry;
import com.heroesjourney.item.HJItems;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;

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
                .then(Commands.literal("stats")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(HJCommand::showStats)))
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

    /**
     * Prints the player's current hero progress and the live values of the attributes/gear this
     * mod's quest rewards actually modify, so a tester can confirm a bonus really applied without
     * having to infer it indirectly (e.g. by fighting something).
     */
    private static int showStats(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        HeroData data = player.getData(HJAttachments.HERO_DATA);

        send(ctx, "=== Hero's Journey - " + player.getGameProfile().getName() + " ===");
        send(ctx, "Active hero: " + data.activeHero());

        if (data.hasActiveHero()) {
            HeroProgress progress = data.getProgress(data.activeHero());
            if (progress != null) {
                send(ctx, "Stage index: " + progress.stageIndex());
                send(ctx, "Flags: " + progress.flags());
                send(ctx, "Unlocked abilities: " + progress.unlockedAbilities());
            }
        }

        send(ctx, String.format("Movement speed attribute: %.4f", player.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        send(ctx, String.format("Attack damage attribute: %.2f", player.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        send(ctx, String.format("Attack speed attribute: %.2f", player.getAttributeValue(Attributes.ATTACK_SPEED)));
        send(ctx, "Main hand: " + player.getMainHandItem());
        send(ctx, "Active potion effects: " + player.getActiveEffects());

        send(ctx, "Worn suit pieces: cowl=" + player.getItemBySlot(EquipmentSlot.HEAD).is(HJItems.BAT_COWL.get())
                + " chestplate=" + player.getItemBySlot(EquipmentSlot.CHEST).is(HJItems.BAT_ARMORED_CHESTPLATE.get())
                + " leggings=" + player.getItemBySlot(EquipmentSlot.LEGS).is(HJItems.BAT_LEGGINGS.get())
                + " boots=" + player.getItemBySlot(EquipmentSlot.FEET).is(HJItems.BAT_BOOTS.get()));
        send(ctx, "Sneaking=" + player.isShiftKeyDown() + " onGround=" + player.onGround()
                + " velocityY=" + player.getDeltaMovement().y);
        return 1;
    }

    private static void send(CommandContext<CommandSourceStack> ctx, String line) {
        ctx.getSource().sendSuccess(() -> Component.literal(line), false);
    }
}
