package com.crazine.goo2tool.gamefiles.savefile;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;

public class WOG2SaveData {

    public static class WOG2SaveFileLevel {

        private boolean valid;
        @JsonGetter() public boolean isValid() {
            return valid;
        }
        @JsonSetter() public void setValid(boolean valid) {
            this.valid = valid;
        }

        private boolean locked;
        @JsonGetter() public boolean isLocked() {
            return locked;
        }
        @JsonSetter() public void setLocked(boolean locked) {
            this.locked = locked;
        }

        private int completeState;
        @JsonGetter() public int getCompleteState() {
            return completeState;
        }
        @JsonSetter() public void setCompleteState(int completeState) {
            this.completeState = completeState;
        }

        private boolean timeOcd;
        @JsonGetter() public boolean isTimeOcd() {
            return timeOcd;
        }
        @JsonSetter() public void setTimeOcd(boolean timeOcd) {
            this.timeOcd = timeOcd;
        }

        private boolean movesOcd;
        @JsonGetter() public boolean isMovesOcd() {
            return movesOcd;
        }
        @JsonSetter() public void setMovesOcd(boolean movesOcd) {
            this.movesOcd = movesOcd;
        }

        private boolean ballsOcd;
        @JsonGetter() public boolean isBallsOcd() {
            return ballsOcd;
        }
        @JsonSetter() public void setBallsOcd(boolean ballsOcd) {
            this.ballsOcd = ballsOcd;
        }

        private int bestTime;
        @JsonGetter() public int getBestTime() {
            return bestTime;
        }
        @JsonSetter() public void setBestTime(int bestTime) {
            this.bestTime = bestTime;
        }

        private int bestMoves;
        @JsonGetter() public int getBestMoves() {
            return bestMoves;
        }
        @JsonSetter() public void setBestMoves(int bestMoves) {
            this.bestMoves = bestMoves;
        }

        private int bestBalls;
        @JsonGetter() public int getBestBalls() {
            return bestBalls;
        }
        @JsonSetter() public void setBestBalls(int bestBalls) {
            this.bestBalls = bestBalls;
        }

        private int totalTime;
        @JsonGetter() public int getTotalTime() {
            return totalTime;
        }
        @JsonSetter() public void setTotalTime(int totalTime) {
            this.totalTime = totalTime;
        }

        private int attempts;
        @JsonGetter() public int getAttempts() {
            return attempts;
        }
        @JsonSetter() public void setAttempts(int attempts) {
            this.attempts = attempts;
        }

        @JsonIgnore
        private String name;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

    }

    public static class WOG2SaveFileIsland {

        private boolean locked;
        @JsonGetter() public boolean isLocked() {
            return locked;
        }
        @JsonSetter() public void setLocked(boolean locked) {
            this.locked = locked;
        }

        private boolean visited;
        @JsonGetter() public boolean isVisited() {
            return visited;
        }
        @JsonSetter() public void setVisited(boolean visited) {
            this.visited = visited;
        }

        private WOG2SaveFileLevel[] levels;
        @JsonGetter() public WOG2SaveFileLevel[] getLevels() {
            return levels;
        }
        @JsonSetter() public void setLevels(WOG2SaveFileLevel[] levels) {
            this.levels = levels;
        }

    }

    public static class WOG2SaveFile {

        private boolean active;
        @JsonGetter() public boolean isActive() {
            return active;
        }
        @JsonSetter() public void setActive(boolean active) {
            this.active = active;
        }

        private String uuid;
        @JsonGetter() public String getUuid() {
            return uuid;
        }
        @JsonSetter() public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        private int seconds;
        @JsonGetter() public int getSeconds() {
            return seconds;
        }
        @JsonSetter() public void setSeconds(int seconds) {
            this.seconds = seconds;
        }

        private boolean whistleAvailable;
        @JsonGetter() public boolean isWhistleAvailable() {
            return whistleAvailable;
        }
        @JsonSetter() public void setWhistleAvailable(boolean whistleAvailable) {
            this.whistleAvailable = whistleAvailable;
        }

        private int zoomFactor;
        @JsonGetter() public int getZoomFactor() {
            return zoomFactor;
        }
        @JsonSetter() public void setZoomFactor(int zoomFactor) {
            this.zoomFactor = zoomFactor;
        }

        private WOG2SaveFileIsland[] islands;
        @JsonGetter() public WOG2SaveFileIsland[] getIslands() {
            return islands;
        }
        @JsonSetter() public void setIslands(WOG2SaveFileIsland[] islands) {
            this.islands = islands;
        }

    }

    private ArrayList<String> values;
    @JsonGetter() public ArrayList<String> getValues() {
        return values;
    }
    @JsonSetter() public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    private WOG2SaveFile saveFile;
    @JsonGetter() public WOG2SaveFile getSaveFile() {
        return saveFile;
    }
    @JsonSetter() public void setSaveFile(WOG2SaveFile saveFile) {
        this.saveFile = saveFile;
    }

}
