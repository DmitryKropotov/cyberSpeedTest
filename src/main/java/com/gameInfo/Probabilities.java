package com.gameInfo;

import java.util.Map;

public class Probabilities {
    private StandardSymbols[] standard_symbols;
    //private int row;
    private BonusSymbols bonus_symbols;


    public abstract class Symbols {
        private Map<String, Integer> symbols;

        public Map<String, Integer> getSymbols() {
            return symbols;
        }
    }

    public class StandardSymbols extends Symbols {
        private int column;
        private int row;

        public int getColumn() {
            return column;
        }

        public int getRow() {
            return row;
        }
    }

    public class BonusSymbols extends Symbols {}

    public StandardSymbols[] getStandard_symbols() {
        return standard_symbols;
    }

    public BonusSymbols getBonus_symbols() {
        return bonus_symbols;
    }
}
