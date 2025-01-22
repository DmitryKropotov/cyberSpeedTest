package com.gameInfo;

import java.util.List;

public class WinCombination {
    private double reward_multiplier;
    private String when;
    private int count; // Optional, only for some win combinations
    private String group;
    private List<List<String>> covered_areas; // Optional, only for some win combinations

    public double getReward_multiplier() {
        return reward_multiplier;
    }

    public String getWhen() {
        return when;
    }

    public int getCount() {
        return count;
    }

    public String getGroup() {
        return group;
    }

    public List<List<String>> getCoveredAreas() {
        return covered_areas;
    }
}
