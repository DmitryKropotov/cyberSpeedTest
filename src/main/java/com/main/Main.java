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

        String[][] matrix = new String[game.getColumns()][game.getRows()];
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
                            if (!repeatsOfMultiplyBonusSymbols.containsKey(matrix[i][j])) {
                                repeatsOfMultiplyBonusSymbols.put(matrix[i][j], 1);
                            } else {
                                repeatsOfMultiplyBonusSymbols.put(matrix[i][j], repeatsOfMultiplyBonusSymbols.get(matrix[i][j]) + 1);
                            }
                        }
                        if(matrix[i][j].contains("+")) {
                            if (!repeatsOfAddBonusSymbols.containsKey(matrix[i][j])) {
                                repeatsOfAddBonusSymbols.put(matrix[i][j], 1);
                            } else {
                                repeatsOfAddBonusSymbols.put(matrix[i][j], repeatsOfAddBonusSymbols.get(matrix[i][j]) + 1);
                            }
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


        //optional part
        double rewardForSameSymbolsHorizontally = (double)winCombination.getSame_symbols_horizontally().get("reward_multiplier");
        List<?> coveredAreasHorizontally = (List<?>) winCombination.getSame_symbols_horizontally().get("covered_areas");
        double rewardForSameSymbolsVertically = (double)winCombination.getSame_symbols_vertically().get("reward_multiplier");
        List<?> coveredAreasVertically = (List<?>) winCombination.getSame_symbols_vertically().get("covered_areas");
        double rewardForSameSymbolsDiagonallyLeftToRight = (double)winCombination.getSame_symbols_diagonally_left_to_right().get("reward_multiplier");
        List<?> coveredAreasDiagonallyLeftToRight = (List<?>) winCombination.getSame_symbols_diagonally_left_to_right().get("covered_areas");
        double rewardForSameSymbolsDiagonallyRightToLeft = (double)winCombination.getSame_symbols_diagonally_right_to_left().get("reward_multiplier");
        List<?> coveredAreasDiagonallyRightToLeft = (List<?>) winCombination.getSame_symbols_diagonally_right_to_left().get("covered_areas");


        int betAmount = 100;
        double reward = 0;
        for (int i = 0; i < standardSymbols.size(); i++) {
            reward += betAmount*standardRewards.get(standardSymbols.get(i))*
                    rewardForSameSymbols.getOrDefault(repeatsOfStandartSymbols.get(standardSymbols.get(i)), 0.0);
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

        //(bet_amount x reward(symbol_A) x reward(same_symbol_5_times) x reward(same_symbols_vertically)) + (bet_amount x reward(symbol_B) x reward(same_symbol_3_times) x reward(same_symbols_vertically)) (+/x) reward(+1000) = (100 x5 x5 x2) + (100 x3 x1 x2) + 1000 = 5000 + 600 + 1000 = 6600

        for (String[] column : matrix) {
            for (String row : column) {
                System.out.print(row + " ");
            }
            System.out.println();
        }
        System.out.println("reward:" + reward + ",");
        System.out.println("applied_winning_combinations: {");
        repeatsOfStandartSymbols.forEach((symbol, repeat) -> {
            if(rewardForSameSymbols.containsKey(repeat)) {
                System.out.println(symbol + ": [ dame_symbol_" + repeat + "_times],");
            }
        });
        System.out.println("}");
        System.out.print("applied bonus symbol:");
        appliedBonusSymbols.forEach(appliedBonusSymbol -> {
            System.out.print(" " + appliedBonusSymbol);
        });
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
}