package com.crazine.goo2tool.gamefiles.islands;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Islands {

    public static class IslandLevel {

        private String level;
        @JsonGetter public String getLevel() {
            return level;
        }
        @JsonSetter public void setLevel(String level) {
            this.level = level;
        }

        private boolean adventure;
        @JsonGetter public boolean isAdventure() {
            return adventure;
        }
        @JsonSetter public void setAdventure(boolean adventure) {
            this.adventure = adventure;
        }

        private String transition;
        @JsonGetter public String getTransition() {
            return transition;
        }
        @JsonSetter public void setTransition(String transition) {
            this.transition = transition;
        }

        private String[] depends;
        @JsonGetter public String[] getDepends() {
            return depends;
        }
        @JsonSetter public void setDepends(String[] depends) {
            this.depends = depends;
        }

        private String cutscene;
        @JsonGetter public String getCutscene() {
            return cutscene;
        }
        @JsonSetter public void setCutscene(String cutscene) {
            this.cutscene = cutscene;
        }

        private boolean skippable;
        @JsonGetter public boolean isSkippable() {
            return skippable;
        }
        @JsonSetter public void setSkippable(boolean skippable) {
            this.skippable = skippable;
        }
        
        private boolean unlockNextIsland;
        @JsonGetter public boolean getUnlockNextIsland() {
            return unlockNextIsland;
        }
        @JsonSetter public void setUnlockNextIsland(boolean unlockNextIsland) {
            this.unlockNextIsland = unlockNextIsland;
        }
        
        private int ocdCount;
        @JsonGetter public int getOcdCount() {
            return ocdCount;
        }
        @JsonSetter public void setOcdCount(int ocdCount) {
            this.ocdCount = ocdCount;
        }
    }


    public static class Island {

        private String id;
        @JsonGetter public String getId() {
            return id;
        }
        @JsonSetter public void setId(String id) {
            this.id = id;
        }

        private String map;
        @JsonGetter public String getMap() {
            return map;
        }
        @JsonSetter public void setMap(String map) {
            this.map = map;
        }

        private IslandLevel[] levels;
        @JsonGetter public IslandLevel[] getLevels() {
            return levels;
        }
        @JsonSetter public void setLevels(IslandLevel[] levels) {
            this.levels = levels;
        }

    }

    private Island[] islands;
    @JsonGetter public Island[] getIslands() {
        return islands;
    }
    @JsonSetter public void setIslands(Island[] islands) {
        this.islands = islands;
    }

}
