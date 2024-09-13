package com.main;

import com.gameInfo.Game;
import com.gameInfo.Probabilities;
import com.gameInfo.WinCombination;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        String filePath = "config.json";

        Gson gson = new Gson();
        FileReader reader = new FileReader(filePath);
        Game game = gson.fromJson(reader, Game.class);

        Map<String, Double> standardRewards = new HashMap<>();
        Map<String, Double> bonusRewardsMultiply = new HashMap<>();
        Map<String, Integer> bonusRewardsAdd = new HashMap<>();
        Map<String, Integer> repeatsOfStandartSymbols = new HashMap<>();
        game.getSymbols().entrySet().forEach(entry -> {
            if(entry.getValue().getType().equals("standard")) {
                standardRewards.put(entry.getKey(), entry.getValue().getReward_multiplier());
                repeatsOfStandartSymbols.put(entry.getKey(), 0);
            } else if(entry.getValue().getType().equals("bonus")) {
                if(entry.getKey().contains("x")) {
                    bonusRewardsMultiply.put(entry.getKey(), entry.getValue().getReward_multiplier());
                } else if(entry.getKey().contains("+")) {
                    bonusRewardsAdd.put(entry.getKey(), entry.getValue().getExtra());
                }
            }
        });
        List<String> standardSymbols = standardRewards.keySet().stream().collect(Collectors.toList());
        List<String> bonusMultiplySymbols = bonusRewardsMultiply.keySet().stream().collect(Collectors.toList());
        List<String> bonusAddSymbols = bonusRewardsAdd.keySet().stream().collect(Collectors.toList());

        String[][] matrix = new String[game.getRows()][game.getColumns()];
        Probabilities.StandardSymbols[] probabilitiesOfStandardSymbols = game.getProbabilities().getStandard_symbols();
        for (Probabilities.StandardSymbols standardSymbol : probabilitiesOfStandardSymbols) {
            matrix[standardSymbol.getRow()][standardSymbol.getColumn()] = generateSymbol(standardSymbol);
            repeatsOfStandartSymbols.put(matrix[standardSymbol.getRow()][standardSymbol.getColumn()],
                    repeatsOfStandartSymbols.get(matrix[standardSymbol.getRow()][standardSymbol.getColumn()])+1);
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
        WinCombination winCombination = game.getWin_combinations();
        rewardForSameSymbols.put(3, (Double)winCombination.getSame_symbol_3_times().get("reward_multiplier"));
        rewardForSameSymbols.put(4, (Double)winCombination.getSame_symbol_4_times().get("reward_multiplier"));
        rewardForSameSymbols.put(5, (Double)winCombination.getSame_symbol_5_times().get("reward_multiplier"));
        rewardForSameSymbols.put(6, (Double)winCombination.getSame_symbol_6_times().get("reward_multiplier"));
        rewardForSameSymbols.put(7, (Double)winCombination.getSame_symbol_7_times().get("reward_multiplier"));
        rewardForSameSymbols.put(8, (Double)winCombination.getSame_symbol_8_times().get("reward_multiplier"));
        rewardForSameSymbols.put(9, (Double)winCombination.getSame_symbol_9_times().get("reward_multiplier"));


        double rewardForSameSymbolsHorizontally = (double)winCombination.getSame_symbols_horizontally().get("reward_multiplier");
        List<List<String>> coveredAreasHorizontally = (List<List<String>>) winCombination.getSame_symbols_horizontally().get("covered_areas");
        double rewardForSameSymbolsVertically = (double)winCombination.getSame_symbols_vertically().get("reward_multiplier");
        List<List<String>> coveredAreasVertically = (List<List<String>>) winCombination.getSame_symbols_vertically().get("covered_areas");
        double rewardForSameSymbolsDiagonallyLeftToRight = (double)winCombination.getSame_symbols_diagonally_left_to_right().get("reward_multiplier");
        List<List<String>> coveredAreasDiagonallyLeftToRight = (List<List<String>>) winCombination.getSame_symbols_diagonally_left_to_right().get("covered_areas");
        double rewardForSameSymbolsDiagonallyRightToLeft = (double)winCombination.getSame_symbols_diagonally_right_to_left().get("reward_multiplier");
        List<List<String>> coveredAreasDiagonallyRightToLeft = (List<List<String>>) winCombination.getSame_symbols_diagonally_right_to_left().get("covered_areas");

        Map<String, Integer> symbolsHorizontallyCounter = countPatternMatchForSymbols(coveredAreasHorizontally, standardSymbols, matrix);
        Map<String, Integer> symbolsVerticallyCounter = countPatternMatchForSymbols(coveredAreasVertically, standardSymbols, matrix);
        Map<String, Integer> symbolsDiagonallyLeftToRightCounter = countPatternMatchForSymbols(coveredAreasDiagonallyLeftToRight, standardSymbols, matrix);
        Map<String, Integer> symbolsDiagonallyRightToLeftCounter = countPatternMatchForSymbols(coveredAreasDiagonallyRightToLeft, standardSymbols, matrix);


        int betAmount = 100;
        double reward = 0;
        for (int i = 0; i < standardSymbols.size(); i++) {
            reward += betAmount*standardRewards.get(standardSymbols.get(i))*
                    rewardForSameSymbols.getOrDefault(repeatsOfStandartSymbols.get(standardSymbols.get(i)), 0.0)*
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


        System.out.println("{");
        System.out.println("matrix: [");
        for (String[] row : matrix) {
            System.out.print("[");
            for (String val : row) {
                System.out.print(val + " ");
            }
            System.out.println("],");
        }
        System.out.println("],");
        System.out.print("reward: " + reward);
        if(reward>0) {
            System.out.println(",");
            System.out.println("applied_winning_combinations: {");
            repeatsOfStandartSymbols.forEach((symbol, repeat) -> {
                if(rewardForSameSymbols.containsKey(repeat)) {
                    System.out.print(symbol + ": [same_symbol_" + repeat + "_times");
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
            System.out.println("},");
            System.out.print("applied bonus symbol:");
            appliedBonusSymbols.forEach(appliedBonusSymbol -> {
                System.out.print(" " + appliedBonusSymbol);
            });
        }
        System.out.println();
        System.out.println("}");
    }

    private static String generateSymbol(Probabilities.Symbols symbols) {
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
}