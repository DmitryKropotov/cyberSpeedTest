package com.main;

import com.gameInfo.Game;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        String filePath = "config.json"; // Specify the path to your text file

        Gson gson = new Gson();
        FileReader reader = new FileReader(filePath); // Provide the path to your JSON file
        Game game = gson.fromJson(reader, Game.class);

        System.out.println(game);

//        try (FileReader fileReader = new FileReader(filePath);
//             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
//
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                System.out.println(line); // Print each line of text from the file
//            }
//        } catch (IOException e) {
//            System.err.println("Error reading the file: " + e.getMessage());
//        }
    }
}