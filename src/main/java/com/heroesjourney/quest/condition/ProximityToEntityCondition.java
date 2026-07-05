package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/** Complete as soon as an entity of {@code target} type has, at some point, been within {@code radius} blocks. */
public class ProximityToEntityCondition implements QuestCondition {

    private final EntityType<?> target;
    private final double radius;

    public ProximityToEntityCondition(EntityType<?> target, double radius) {
        this.target = target;
        this.radius = radius;
    }

    @Override
    public int target() {
        return 1;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (currentProgress >= 1 || !(ctx.event() instanceof QuestEvent.Heartbeat)) {
            return currentProgress;
        }
        var box = ctx.player().getBoundingBox().inflate(radius);
        for (Entity entity : ctx.level().getEntities(target, box, Entity::isAlive)) {
            if (entity.closerThan(ctx.player(), radius)) {
                return 1;
            }
        }
        return currentProgress;
    }
}
