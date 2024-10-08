package com.gameInfo;

import java.util.Map;

public class Game {
    private int columns;
    private int rows;
    private Map<String, SymbolDetails> symbols;
    private Probabilities probabilities;
    private WinCombination win_combinations;

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public Map<String, SymbolDetails> getSymbols() {
        return symbols;
    }

    public Probabilities getProbabilities() {
        return probabilities;
    }

    public WinCombination getWin_combinations() {
        return win_combinations;
    }
}
