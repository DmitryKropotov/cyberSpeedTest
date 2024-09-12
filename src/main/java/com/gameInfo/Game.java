package com.gameInfo;

import java.util.Map;

public class Game {
    private int columns;
    private int rows;
    private Map<String, SymbolDetails> symbols;
    private Probabilities probabilities;
    private WinCombination win_combinations;

    private class SymbolDetails {
        double reward_multiplier;
        int extra;
        String type;
        String impact;
    }
}
