package com.main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void NoStandardSymbolsLostGameTest() {
        Path resourceDirectory = Paths.get("src","test","resources", "noStandardSymbolsLostGameTest.json");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        String[] args = {"--config", absolutePath, "--betting-amount", "200"};
        Main.main(args);
        StringBuilder expectedStart = new StringBuilder();
        expectedStart.append("{\r\n");
        expectedStart.append(" matrix: [\r\n");
        StringBuilder expectedEnd = new StringBuilder();
        expectedEnd.append(" ],\r\n");
        expectedEnd.append(" reward: 0.0\r\n");
        expectedEnd.append("}\r\n");
        assertTrue(outputStreamCaptor.toString().trim().startsWith(expectedStart.toString().trim()));
        assertTrue(outputStreamCaptor.toString().trim().endsWith(expectedEnd.toString().trim()));
    }

    @Test
    public void OneStandardSymbolBet50Test() {
        Path resourceDirectory = Paths.get("src","test","resources", "OneStandardSymbolTest.json");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        String[] args = {"--config", absolutePath, "--betting-amount", "50"};
        Main.main(args);
        StringBuilder expected = new StringBuilder();
        expected.append("{\r\n");
        expected.append(" matrix: [\r\n");
        expected.append("  [A, A, A],\r\n");
        expected.append("  [A, A, A],\r\n");
        expected.append("  [A, A, A]\r\n");
        expected.append(" ],\r\n");
        expected.append(" reward: 9720000.0,\r\n");
        expected.append(" applied_winning_combinations: {\r\n");
        expected.append("  A: [same_symbol_9_times, same_symbols_horizontally, same_symbols_vertically, same_symbols_diagonally_left_to_right, same_symbols_diagonally_right_to_left]\r\n");
        expected.append(" }\r\n");
        expected.append("}\r\n");
        assertTrue(outputStreamCaptor.toString().trim().equals(expected.toString().trim()));
    }

    @Test
    public void OneStandardSymbolBet25Test() {
        Path resourceDirectory = Paths.get("src","test","resources", "OneStandardSymbolTest.json");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        String[] args = {"--config", absolutePath, "--betting-amount", "25"};
        Main.main(args);
        StringBuilder expected = new StringBuilder();
        expected.append("{\r\n");
        expected.append(" matrix: [\r\n");
        expected.append("  [A, A, A],\r\n");
        expected.append("  [A, A, A],\r\n");
        expected.append("  [A, A, A]\r\n");
        expected.append(" ],\r\n");
        expected.append(" reward: 4860000.0,\r\n");
        expected.append(" applied_winning_combinations: {\r\n");
        expected.append("  A: [same_symbol_9_times, same_symbols_horizontally, same_symbols_vertically, same_symbols_diagonally_left_to_right, same_symbols_diagonally_right_to_left]\r\n");
        expected.append(" }\r\n");
        expected.append("}\r\n");
        assertTrue(outputStreamCaptor.toString().trim().equals(expected.toString().trim()));
    }

    @Test
    public void OneStandardSymbolDefaultBetTest() {
        Path resourceDirectory = Paths.get("src","test","resources", "OneStandardSymbolTest.json");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        String[] args = {"--config", absolutePath, "--betting-amount", "not number"};
        Main.main(args);
        StringBuilder expected = new StringBuilder();
        expected.append("{\r\n");
        expected.append(" matrix: [\r\n");
        expected.append("  [A, A, A],\r\n");
        expected.append("  [A, A, A],\r\n");
        expected.append("  [A, A, A]\r\n");
        expected.append(" ],\r\n");
        expected.append(" reward: 1.944E7,\r\n");
        expected.append(" applied_winning_combinations: {\r\n");
        expected.append("  A: [same_symbol_9_times, same_symbols_horizontally, same_symbols_vertically, same_symbols_diagonally_left_to_right, same_symbols_diagonally_right_to_left]\r\n");
        expected.append(" }\r\n");
        expected.append("}\r\n");
        assertTrue(outputStreamCaptor.toString().trim().equals(expected.toString().trim()));
    }

    @Test
    public void FileDoesntExistTest() {
        String[] args = {"--config", "NotExistingFile.json", "--betting-amount", "200"};
        Main.main(args);
        assertTrue(outputStreamCaptor.toString().trim().equals("File NotExistingFile.json is not found. Please provide correct file name"));
    }


}
