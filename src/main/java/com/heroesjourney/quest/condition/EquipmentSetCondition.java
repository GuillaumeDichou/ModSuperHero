package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

/** Complete once the player is simultaneously wearing every item in {@code requiredWorn} (checked via the heartbeat). */
public class EquipmentSetCondition implements QuestCondition {

    private final List<Supplier<Item>> requiredWorn;

    public EquipmentSetCondition(List<Supplier<Item>> requiredWorn) {
        this.requiredWorn = requiredWorn;
    }

    @Override
    public int target() {
        return 1;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (!(ctx.event() instanceof QuestEvent.Heartbeat)) {
            return currentProgress;
        }
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (Supplier<Item> required : requiredWorn) {
            Item item = required.get();
            boolean worn = false;
            for (EquipmentSlot slot : armorSlots) {
                if (ctx.player().getItemBySlot(slot).is(item)) {
                    worn = true;
                    break;
                }
            }
            if (!worn) {
                return 0;
            }
        }
        return 1;
    }
}
