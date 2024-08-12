package com.crazine.goo2tool.saveFile;

import java.util.ArrayList;

public class WOG2SaveData {

    public static class WOG2SaveFileLevel {

        private boolean valid;
        public boolean isValid() {
            return valid;
        }
        public void setValid(boolean valid) {
            this.valid = valid;
        }

        private boolean locked;
        public boolean isLocked() {
            return locked;
        }
        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        private int completeState;
        public int getCompleteState() {
            return completeState;
        }
        public void setCompleteState(int completeState) {
            this.completeState = completeState;
        }

        private boolean timeOcd;
        public boolean isTimeOcd() {
            return timeOcd;
        }
        public void setTimeOcd(boolean timeOcd) {
            this.timeOcd = timeOcd;
        }

        private boolean movesOcd;
        public boolean isMovesOcd() {
            return movesOcd;
        }
        public void setMovesOcd(boolean movesOcd) {
            this.movesOcd = movesOcd;
        }

        private boolean ballsOcd;
        public boolean isBallsOcd() {
            return ballsOcd;
        }
        public void setBallsOcd(boolean ballsOcd) {
            this.ballsOcd = ballsOcd;
        }

        private int bestTime;
        public int getBestTime() {
            return bestTime;
        }
        public void setBestTime(int bestTime) {
            this.bestTime = bestTime;
        }

        private int bestMoves;
        public int getBestMoves() {
            return bestMoves;
        }
        public void setBestMoves(int bestMoves) {
            this.bestMoves = bestMoves;
        }

        private int bestBalls;
        public int getBestBalls() {
            return bestBalls;
        }
        public void setBestBalls(int bestBalls) {
            this.bestBalls = bestBalls;
        }

        private int totalTime;
        public int getTotalTime() {
            return totalTime;
        }
        public void setTotalTime(int totalTime) {
            this.totalTime = totalTime;
        }

        private int attempts;
        public int getAttempts() {
            return attempts;
        }
        public void setAttempts(int attempts) {
            this.attempts = attempts;
        }

    }

    public static class WOG2SaveFileIsland {

        private boolean locked;
        public boolean isLocked() {
            return locked;
        }
        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        private boolean visited;
        public boolean isVisited() {
            return visited;
        }
        public void setVisited(boolean visited) {
            this.visited = visited;
        }

        private WOG2SaveFileLevel[] levels;
        public WOG2SaveFileLevel[] getLevels() {
            return levels;
        }
        public void setLevels(WOG2SaveFileLevel[] levels) {
            this.levels = levels;
        }

    }

    public static class WOG2SaveFile {

        private boolean active;
        public boolean isActive() {
            return active;
        }
        public void setActive(boolean active) {
            this.active = active;
        }

        private String uuid;
        public String getUuid() {
            return uuid;
        }
        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        private int seconds;
        public int getSeconds() {
            return seconds;
        }
        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }

        private boolean whistleAvailable;
        public boolean isWhistleAvailable() {
            return whistleAvailable;
        }
        public void setWhistleAvailable(boolean whistleAvailable) {
            this.whistleAvailable = whistleAvailable;
        }

        private int zoomFactor;
        public int getZoomFactor() {
            return zoomFactor;
        }
        public void setZoomFactor(int zoomFactor) {
            this.zoomFactor = zoomFactor;
        }

        private WOG2SaveFileIsland[] islands;
        public WOG2SaveFileIsland[] getIslands() {
            return islands;
        }
        public void setIslands(WOG2SaveFileIsland[] islands) {
            this.islands = islands;
        }

    }

    private ArrayList<String> values;
    public ArrayList<String> getValues() {
        return values;
    }
    public void setValues(ArrayList<String> values) {
        this.values = values;
    }


    private WOG2SaveFile saveFile;
    public WOG2SaveFile getSaveFile() {
        return saveFile;
    }
    public void setSaveFile(WOG2SaveFile saveFile) {
        this.saveFile = saveFile;
    }

}
