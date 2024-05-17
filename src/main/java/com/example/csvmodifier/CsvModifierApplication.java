package com.example.csvmodifier;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class CsvModifierApplication {

	public static void main(String[] args) {
		String sourceFile = "sourceFile.csv";
		String targetFile = "targetFile.csv";
		String outputFile = "outputFile.csv";
		try {

			Map<String, List<Map<String, String>>> sourceFileMap = new HashMap<>();

			// Read source file
			try (CSVReader sourceFileReader = new CSVReader(new FileReader(sourceFile))) {
				String[] line;
				sourceFileReader.readNext(); // Skip header

				while ((line = sourceFileReader.readNext()) != null) {
					//displays the content of the source file
					String lineNameIndex0 = line[0];
					String lineNameIndex1 = line[1];

					Map<String, String> sourceFileData = new HashMap<>();
					sourceFileData.put("lineNameIndex0", lineNameIndex0);
					sourceFileData.put("lineNameIndex1", lineNameIndex1);

					sourceFileMap.computeIfAbsent(lineNameIndex0, k -> new ArrayList<>()).add(sourceFileData);
				}
			} catch (CsvValidationException | IOException e) {
				throw new RuntimeException(e);
			}

			try (CSVReader targetFileReader = new CSVReader(new FileReader(targetFile));
				 CSVWriter outputFileWriter = new CSVWriter(new FileWriter(outputFile))) {
				String[] line;
				String[] header = targetFileReader.readNext(); // Read header
				String[] newHeader = new String[header.length + 1]; // Create new header with an extra column
				System.arraycopy(header, 0, newHeader, 0, header.length);
				newHeader[header.length] = "Updated"; // Add "Updated" column
				outputFileWriter.writeNext(newHeader); // Write new header to output

				List<String[]> updatedRows = new ArrayList<>();

				while ((line = targetFileReader.readNext()) != null) {
					//displays the content of the target File
					String lineNameIndex0 = line[0];
					String lineNameIndex1 = line[1];

					List<Map<String, String>> mapList = sourceFileMap.get(lineNameIndex0);

					boolean updated = false;
					if (mapList != null) {
						for (Map<String, String> sourceFileData : mapList) {
							if (lineNameIndex1.equals(sourceFileData.get("lineNameIndex0"))) {
								line[1] = sourceFileData.get("lineNameIndex1"); // Replace lineNameIndex1 for the target file with lineNameIndex1 for the source file
								updated = true; // Mark this line as updated
								break; // Assuming you want to update with the first match found
							}
						}
					}

					String[] newLine = new String[line.length + 1]; // Create new line with an extra column
					System.arraycopy(line, 0, newLine, 0, line.length);
					newLine[line.length] = updated ? "Y" : "N"; // Add "Y" if updated, otherwise "N"

					if (updated) {
						updatedRows.add(newLine); // Add only updated rows
					}
				}

				// Write only updated rows to the output file
				for (String[] updatedLine : updatedRows) {
					outputFileWriter.writeNext(updatedLine);
				}
			}
		} catch (CsvValidationException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
