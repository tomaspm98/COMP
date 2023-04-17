package pt.up.fe.comp2023;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp2023.jasmin.JasminBackender;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);

        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Check if file exists
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFile + "'.");
        }

        // Read contents of input file
        String code = SpecsIo.read(inputFile);

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        System.out.println(parserResult.getRootNode().toTree());

        JmmAnalyser analyser = new JmmAnalyser();

        JmmSemanticsResult analysisResult = analyser.semanticAnalysis(parserResult);

        TestUtils.noErrors(analysisResult.getReports());

        String code1 = SpecsIo.read(inputFile);

        var ollirResult= new OllirResult(code1, new HashMap<>());

        var jasminBackend = new JasminBackender();

        var backendResult = jasminBackend.toJasmin(ollirResult);

        TestUtils.noErrors(backendResult);

        Path resultsDirectory = Paths.get("generated-files/");

        try {
            if (!Files.exists(resultsDirectory)) {
                Files.createDirectory(resultsDirectory);
            }
        } catch (IOException e) {
            System.out.println("Error creating the " + resultsDirectory + " directory.");
            return;
        }

        Path path = Paths.get("generated-files/" + backendResult.getClassName() + "/");

        try {
            FileWriter fileWriter = new FileWriter(path + ".j");
            fileWriter.write(backendResult.getJasminCode());
            fileWriter.close();
            System.out.println("Jasmin file saved successfully!");
        } catch (IOException e) {
            System.out.println("Error while writing the .j file.");
        }

        // Generate .class file
        backendResult.compile(path.toFile());
        System.out.println(".class file saved successfully!");
    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        return config;
    }

}
