package com.heroesjourney.command;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.config.HJConfig;
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
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
                .then(Commands.literal("resetattributes")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(HJCommand::resetAttributes)))
        );
    }

    /**
     * Attribute modifiers this mod adds via {@code addPermanentModifier} get saved into the
     * player's own NBT data, keyed by the {@code ResourceLocation} id passed at the time - if a
     * past version of the mod used a different id for a bonus that has since been renamed/removed
     * (e.g. an old boots-speed bonus), the granting code stops re-adding/removing it once that id
     * is no longer referenced anywhere, but the modifier itself stays stuck in the save file
     * forever, since nothing asks to remove that specific id anymore. This strips every attribute
     * modifier in this mod's namespace (regardless of id, so it also catches ones from versions
     * before this command existed) from every attribute the mod has ever touched, so the next
     * pulse re-adds only whatever the *current* code actually grants.
     */
    private static final Holder<Attribute>[] CLEANABLE_ATTRIBUTES = new Holder[] {
            Attributes.ATTACK_DAMAGE, Attributes.ATTACK_SPEED, Attributes.MOVEMENT_SPEED,
            Attributes.ARMOR, Attributes.KNOCKBACK_RESISTANCE, Attributes.WATER_MOVEMENT_EFFICIENCY
    };

    private static int resetAttributes(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        List<String> removed = new ArrayList<>();
        for (Holder<Attribute> attribute : CLEANABLE_ATTRIBUTES) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            for (AttributeModifier modifier : instance.getModifiers()) {
                if (modifier.id().getNamespace().equals(HeroesJourney.MODID)) {
                    instance.removeModifier(modifier.id());
                    removed.add(attribute.value().getDescriptionId() + " -> " + modifier.id()
                            + " (amount=" + modifier.amount() + ", op=" + modifier.operation() + ")");
                }
            }
        }
        if (removed.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "No heroesjourney-namespaced attribute modifiers found on " + player.getGameProfile().getName() + " - nothing to clean."), true);
        } else {
            String report = String.join("; ", removed);
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Removed " + removed.size() + " stale modifier(s) from " + player.getGameProfile().getName() + ": " + report), true);
        }
        return 1;
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

        send(ctx, String.format("Movement speed attribute: %.4f (isSprinting=%s - vanilla adds its own +30%% sprint modifier on top of everything below while sprinting)",
                player.getAttributeValue(Attributes.MOVEMENT_SPEED), player.isSprinting()));
        send(ctx, String.format("  run-training speed bonus config: base=%.4f fullSuit=%.4f (raw config values, not the final attribute)",
                HJConfig.RUN_TRAINING_SPEED_BONUS.get(), HJConfig.RUN_TRAINING_SPEED_BONUS_FULL_SUIT.get()));
        AttributeInstance movementSpeedInstance = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeedInstance != null) {
            for (AttributeModifier modifier : movementSpeedInstance.getModifiers()) {
                send(ctx, "  movement_speed modifier present: " + modifier.id() + " amount=" + modifier.amount()
                        + " op=" + modifier.operation()
                        + (modifier.id().getNamespace().equals(HeroesJourney.MODID) ? "" : " (NOT ours - vanilla/another mod)"));
            }
        }
        send(ctx, String.format("Attack damage attribute: %.2f (final, real damage dealt - all bonuses are attribute modifiers, none are applied as a hidden event-only add)", player.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        send(ctx, String.format("Attack speed attribute: %.2f", player.getAttributeValue(Attributes.ATTACK_SPEED)));
        send(ctx, String.format("Armor attribute: %.2f", player.getAttributeValue(Attributes.ARMOR)));
        send(ctx, String.format("Knockback resistance attribute: %.2f", player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
        send(ctx, String.format("Effective DPS (damage x attack speed): %.2f", player.getAttributeValue(Attributes.ATTACK_DAMAGE) * player.getAttributeValue(Attributes.ATTACK_SPEED)));
        send(ctx, "Main hand: " + player.getMainHandItem() + " (martial arts / full-suit damage bonuses are general - they apply regardless of what's in hand)");
        send(ctx, "Active potion effects: " + player.getActiveEffects());

        boolean cowl = player.getItemBySlot(EquipmentSlot.HEAD).is(HJItems.BAT_COWL.get());
        boolean chestplate = player.getItemBySlot(EquipmentSlot.CHEST).is(HJItems.BAT_ARMORED_CHESTPLATE.get());
        boolean leggings = player.getItemBySlot(EquipmentSlot.LEGS).is(HJItems.BAT_LEGGINGS.get());
        boolean boots = player.getItemBySlot(EquipmentSlot.FEET).is(HJItems.BAT_BOOTS.get());
        send(ctx, "Worn suit pieces: cowl=" + cowl + " chestplate=" + chestplate + " leggings=" + leggings + " boots=" + boots
                + " -> full suit bonus active=" + (cowl && chestplate && leggings && boots));
        send(ctx, "onGround=" + player.onGround() + " velocityY=" + player.getDeltaMovement().y
                + " elytraGliding=" + player.isFallFlying() + " (real vanilla Elytra-style flight via BatChestplateItem - jump once while airborne with the chestplate worn + Batman active to start, it persists on its own, look down to dive, sneak while gliding to cancel)");
        return 1;
    }

    private static void send(CommandContext<CommandSourceStack> ctx, String line) {
        ctx.getSource().sendSuccess(() -> Component.literal(line), false);
    }
}
