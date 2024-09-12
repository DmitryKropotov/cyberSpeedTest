package com.main;

import com.gameInfo.Game;
import com.gameInfo.Probabilities;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        String filePath = "config.json"; // Specify the path to your text file

        Gson gson = new Gson();
        FileReader reader = new FileReader(filePath); // Provide the path to your JSON file
        Game game = gson.fromJson(reader, Game.class);

        String[][] matrix = new String[game.getColumns()][game.getRows()];
        Probabilities.StandardSymbols[] standardSymbols = game.getProbabilities().getStandard_symbols();
        for (Probabilities.StandardSymbols standardSymbol : standardSymbols) {
            matrix[standardSymbol.getRow()][standardSymbol.getColumn()] = generateSymbol(standardSymbol);
        }
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == null) {
                    matrix[i][j] = generateSymbol(game.getProbabilities().getBonus_symbols());
                }
            }
        }

        System.out.println(game);
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