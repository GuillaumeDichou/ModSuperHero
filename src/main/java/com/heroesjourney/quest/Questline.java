package com.heroesjourney.quest;

import java.util.List;
import java.util.Optional;

/**
 * Ordered list of {@link QuestStage}s for one hero. Stages are always progressed strictly in
 * order, which matches every questline in the design document; nothing here is Batman-specific.
 */
public record Questline(String heroId, List<QuestStage> stages) {

    public Questline {
        stages = List.copyOf(stages);
    }

    public int size() {
        return stages.size();
    }

    public boolean isComplete(int stageIndex) {
        return stageIndex >= stages.size();
    }

    public Optional<QuestStage> stageAt(int index) {
        if (index < 0 || index >= stages.size()) {
            return Optional.empty();
        }
        return Optional.of(stages.get(index));
    }

    public int indexOf(String stageId) {
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).id().equals(stageId)) {
                return i;
            }
        }
        return -1;
    }
}
