package com.main;

import com.gameInfo.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) {
        final String FILE_PATH = (args.length >= 2 && args[1].endsWith(".json"))? args[1]: "config.json";
        final int BET_AMOUNT = (args.length >= 4 && isInteger(args[3]))? Integer.valueOf(args[3]): 100;

        Gson gson = new Gson();
        FileReader reader = null;
        try {
            reader = new FileReader(FILE_PATH);
        } catch (FileNotFoundException e) {
            System.out.println(("File " + FILE_PATH + " is not found. Please provide correct file name"));
            return;
        }
        Game game = gson.fromJson(reader, Game.class);

        Map<String, Double> standardRewards = new HashMap<>();
        Map<String, Double> bonusRewardsMultiply = new HashMap<>();
        Map<String, Integer> bonusRewardsAdd = new HashMap<>();
        Map<String, Integer> repeatsOfStandardSymbols = new HashMap<>();
        game.getSymbols().entrySet().forEach(entry -> {
            if(entry.getValue().getType().equals("standard")) {
                standardRewards.put(entry.getKey(), entry.getValue().getReward_multiplier());
                repeatsOfStandardSymbols.put(entry.getKey(), 0);
            } else if(entry.getValue().getType().equals("bonus")) {
                if(entry.getKey().contains("x")) {
                    bonusRewardsMultiply.put(entry.getKey(), entry.getValue().getReward_multiplier());
                } else if(entry.getKey().contains("+")) {
                    bonusRewardsAdd.put(entry.getKey(), entry.getValue().getExtra());
                }
            }
        });
        List<String> standardSymbols = new ArrayList<>(standardRewards.keySet());

        String[][] matrix;
        if(game.getColumns() == 0 || game.getRows() == 0) {
            int[] result = calculateColumnsAndRows(game.getProbabilities().getStandard_symbols());
            matrix = new String[result[0]][result[1]];
        } else {
            matrix = new String[game.getColumns()][game.getRows()];
        }
        //standard symbols
        StandardSymbols[] probabilitiesOfStandardSymbols = game.getProbabilities().getStandard_symbols();
        for (StandardSymbols standardSymbol : probabilitiesOfStandardSymbols) {
            matrix[standardSymbol.getColumn()][standardSymbol.getRow()] = generateSymbol(standardSymbol);
            repeatsOfStandardSymbols.put(matrix[standardSymbol.getColumn()][standardSymbol.getRow()],
                    repeatsOfStandardSymbols.get(matrix[standardSymbol.getColumn()][standardSymbol.getRow()])+1);
        }

        //bonus Symbols
        Map<String, Integer> repeatsOfMultiplyBonusSymbols = new HashMap<>();
        Map<String, Integer> repeatsOfAddBonusSymbols = new HashMap<>();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == null) {
                    matrix[i][j] = generateSymbol(game.getProbabilities().getBonus_symbols());
                    if(!matrix[i][j].equals("MISS")) {
                        if(matrix[i][j].contains("x")) {
                         repeatsOfMultiplyBonusSymbols.put(matrix[i][j], repeatsOfMultiplyBonusSymbols.getOrDefault(matrix[i][j], 0) + 1);
                        }
                        if(matrix[i][j].contains("+")) {
                          repeatsOfAddBonusSymbols.put(matrix[i][j], repeatsOfAddBonusSymbols.getOrDefault(matrix[i][j], 0) + 1);
                        }
                    }
                }
            }
        }

        Map<Integer, Double> rewardForSameSymbols = new HashMap<>();
        Map<String, WinCombination> winCombination = game.getWin_combinations();

        double rewardForSameSymbolsHorizontally = 0;
        List<List<String>> coveredAreasHorizontally = new ArrayList<>();
        double rewardForSameSymbolsVertically = 0;
        List<List<String>> coveredAreasVertically = new ArrayList<>();
        double rewardForSameSymbolsDiagonallyLeftToRight = 0;
        List<List<String>> coveredAreasDiagonallyLeftToRight = new ArrayList<>();
        double rewardForSameSymbolsDiagonallyRightToLeft = 0;
        List<List<String>> coveredAreasDiagonallyRightToLeft = new ArrayList<>();

        for (Map.Entry<String, WinCombination> entry : winCombination.entrySet()) {
            String combinationName = entry.getKey();
            WinCombination combination = entry.getValue();
            if (combinationName.matches("same_symbol_(\\d+)_times")) {
                rewardForSameSymbols.put(combination.getCount(), combination.getReward_multiplier());
            }
            if (combinationName.equals("same_symbols_horizontally")) {
                rewardForSameSymbolsHorizontally = combination.getReward_multiplier();
                coveredAreasHorizontally = combination.getCoveredAreas();
            }
            if (combinationName.equals("same_symbols_vertically")) {
                rewardForSameSymbolsVertically = combination.getReward_multiplier();
                coveredAreasVertically = combination.getCoveredAreas();
            }
            if (combinationName.equals("same_symbols_diagonally_left_to_right")) {
                rewardForSameSymbolsDiagonallyLeftToRight = combination.getReward_multiplier();
                coveredAreasDiagonallyLeftToRight = combination.getCoveredAreas();
            }
            if (combinationName.equals("same_symbols_diagonally_right_to_left")) {
                rewardForSameSymbolsDiagonallyRightToLeft = combination.getReward_multiplier();
                coveredAreasDiagonallyRightToLeft = combination.getCoveredAreas();
            }
        }


        Map<String, Integer> symbolsHorizontallyCounter = countPatternMatchForSymbols(coveredAreasHorizontally, standardSymbols, matrix);
        Map<String, Integer> symbolsVerticallyCounter = countPatternMatchForSymbols(coveredAreasVertically, standardSymbols, matrix);
        Map<String, Integer> symbolsDiagonallyLeftToRightCounter = countPatternMatchForSymbols(coveredAreasDiagonallyLeftToRight, standardSymbols, matrix);
        Map<String, Integer> symbolsDiagonallyRightToLeftCounter = countPatternMatchForSymbols(coveredAreasDiagonallyRightToLeft, standardSymbols, matrix);


        double reward = 0;
        for (int i = 0; i < standardSymbols.size(); i++) {
            reward += BET_AMOUNT*standardRewards.get(standardSymbols.get(i))*
                    rewardForSameSymbols.getOrDefault(repeatsOfStandardSymbols.get(standardSymbols.get(i)), 0.0)*
                    Math.pow(rewardForSameSymbolsHorizontally, symbolsHorizontallyCounter.getOrDefault(standardSymbols.get(i), 0))*
                    Math.pow(rewardForSameSymbolsVertically, symbolsVerticallyCounter.getOrDefault(standardSymbols.get(i), 0))*
                    Math.pow(rewardForSameSymbolsDiagonallyLeftToRight, symbolsDiagonallyLeftToRightCounter.getOrDefault(standardSymbols.get(i), 0))*
                    Math.pow(rewardForSameSymbolsDiagonallyRightToLeft, symbolsDiagonallyRightToLeftCounter.getOrDefault(standardSymbols.get(i), 0));
        }

        List<String> appliedBonusSymbols = new ArrayList<>();
        if(reward>0) {
            List<Map.Entry<String, Integer>> bonusSymbolsMultiplyRepeatAsList = repeatsOfMultiplyBonusSymbols.entrySet().stream().toList();
            for (int i = 0; i < bonusSymbolsMultiplyRepeatAsList.size(); i++) {
                for (int j = 0; j < bonusSymbolsMultiplyRepeatAsList.get(i).getValue(); j++) {
                    reward *= bonusRewardsMultiply.get(bonusSymbolsMultiplyRepeatAsList.get(i).getKey());
                }
                appliedBonusSymbols.add(bonusSymbolsMultiplyRepeatAsList.get(i).getKey());
            }
            List<Map.Entry<String, Integer>> bonusSymbolsAddRepeatAsList = repeatsOfAddBonusSymbols.entrySet().stream().toList();
            for (int i = 0; i < bonusSymbolsAddRepeatAsList.size(); i++) {
                for (int j = 0; j < bonusSymbolsAddRepeatAsList.get(i).getValue(); j++) {
                    reward += bonusRewardsAdd.get(bonusSymbolsAddRepeatAsList.get(i).getKey());
                }
                appliedBonusSymbols.add(bonusSymbolsAddRepeatAsList.get(i).getKey());
            }
        }


        printOutput(reward, matrix, repeatsOfStandardSymbols, rewardForSameSymbols, symbolsHorizontallyCounter, symbolsVerticallyCounter,
                symbolsDiagonallyLeftToRightCounter, symbolsDiagonallyRightToLeftCounter, appliedBonusSymbols);
    }

    private static int[] calculateColumnsAndRows(StandardSymbols[] standardSymbols) {
        int columns = 2;
        int rows = 2;
        for (StandardSymbols s : standardSymbols) {
            rows = Math.max(rows, s.getRow()+1);
            columns = Math.max(columns, s.getColumn()+1);
        }
        return new int[] {columns, rows};
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String generateSymbol(Symbols symbols) {
        Map<String, Integer> symbolProbabilities = symbols.getSymbols();
        int sum = 0;
        for (int value : symbolProbabilities.values()) {
            sum += value;
        }
        List<Integer> random = IntStream.rangeClosed(1, sum).boxed().collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(random);

        double p = (double)random.get(0)/(double)sum;
        double steps = 0;
        Map.Entry<String, Integer> entries[] = symbolProbabilities.entrySet().toArray(new  Map.Entry[symbolProbabilities.entrySet().size()]);
        for (Map.Entry<String, Integer> entry : entries) {
            steps += (double)entry.getValue()/(double)sum;
            if(p<=(double)entry.getValue()/(double) sum) {
                return entry.getKey();
            }
        }
        return entries[entries.length-1].getKey();
    }

    private static Map<String, Integer> countPatternMatchForSymbols(List<List<String>> coordinatesPattern,
                                        List<String> standardSymbols, String[][] matrix) {
        Map<String, Integer> symbolsHorizontallyCounter = new HashMap();
        coordinatesPattern.forEach(list -> {
            int[][] allCoordinates = new int[list.size()][2];
            for (int i = 0; i < list.size(); i++) {
                int[] coordinates = Arrays.stream(list.get(i).split(":")).mapToInt(c -> Integer.valueOf(c)).toArray();
                allCoordinates[i] = coordinates;
            }
            String symbol = matrix[allCoordinates[0][0]][allCoordinates[0][1]];
            boolean match = true;
            if(standardSymbols.contains(symbol)) {
                for(int i = 1; i < allCoordinates.length; i++) {
                    if(!matrix[allCoordinates[i][0]][allCoordinates[i][1]].equals(symbol)) {
                        match = false;
                        break;
                    }
                }
            }
            if(match) {
                symbolsHorizontallyCounter.put(symbol, symbolsHorizontallyCounter.getOrDefault(symbol,0) + 1);
            }
        });
        return symbolsHorizontallyCounter;
    }

    private static void printOutput(double reward, String[][] matrix, Map<String, Integer> repeatsOfStandartSymbols,
                                    Map<Integer, Double> rewardForSameSymbols, Map<String, Integer> symbolsHorizontallyCounter,
                                    Map<String, Integer> symbolsVerticallyCounter, Map<String, Integer> symbolsDiagonallyLeftToRightCounter,
                                    Map<String, Integer> symbolsDiagonallyRightToLeftCounter, List<String> appliedBonusSymbols) {
        System.out.println("{");
        System.out.println(" matrix: [");
        for (int i = 0; i < matrix.length; i++) {
            System.out.print("  [");
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + ((j < matrix[i].length - 1) ? ", " : ""));
            }
            System.out.println("]" + (i==matrix.length-1?"":","));
        }
        System.out.println(" ],");
        System.out.print(" reward: " + reward);
        if(reward>0) {
            System.out.println(",");
            System.out.println(" applied_winning_combinations: {");
            repeatsOfStandartSymbols.forEach((symbol, repeat) -> {
                if(rewardForSameSymbols.containsKey(repeat)) {
                    System.out.print("  " + symbol + ": [same_symbol_" + repeat + "_times");
                    if(symbolsHorizontallyCounter.containsKey(symbol)) {
                        System.out.print(", same_symbols_horizontally");
                    }
                    if(symbolsVerticallyCounter.containsKey(symbol)) {
                        System.out.print(", same_symbols_vertically");
                    }
                    if(symbolsDiagonallyLeftToRightCounter.containsKey(symbol)) {
                        System.out.print(", same_symbols_diagonally_left_to_right");
                    }
                    if(symbolsDiagonallyRightToLeftCounter.containsKey(symbol)) {
                        System.out.print(", same_symbols_diagonally_right_to_left");
                    }
                    System.out.println("]");
                }
            });
            System.out.print(" }");
            if(!appliedBonusSymbols.isEmpty()) {
                System.out.println(",");
                System.out.print(" applied bonus symbol:");
                for (int i = 0; i < appliedBonusSymbols.size(); i++) {
                    System.out.print((i!=0?",":"") + " " + appliedBonusSymbols.get(i));
                }
            }
        }
        System.out.println();
        System.out.println("}");
    }
}