package com.gameInfo;

import java.util.Map;

public class Probabilities {
    StandardSymbol[] standard_symbols;
    //private int row;
    private BonusSymbols bonus_symbols;

    private class BonusSymbols {
        Map<String, Integer> symbols;
    }

    class StandardSymbol {
        private int column;
        private int row;
        private Map<String, Integer> symbols;

        // Add getters and setters
    }

    class BonusSymbol {
        private Map<String, Integer> symbols;

        // Add getters and setters
    }
}
