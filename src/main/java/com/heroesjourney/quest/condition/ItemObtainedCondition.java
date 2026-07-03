package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;
import net.minecraft.resources.ResourceLocation;

public class ItemObtainedCondition implements QuestCondition {

    private final String itemId;

    public ItemObtainedCondition(ResourceLocation itemId) {
        this.itemId = itemId.toString();
    }

    @Override
    public int target() {
        return 1;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (ctx.event() instanceof QuestEvent.ItemObtained obtained && obtained.itemId().equals(itemId)) {
            return 1;
        }
        return currentProgress;
    }
}
