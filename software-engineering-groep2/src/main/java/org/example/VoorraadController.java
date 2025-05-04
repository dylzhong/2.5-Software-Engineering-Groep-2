package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
public class VoorraadController {

    private final List<Item> voorraad = new ArrayList<>();

    public VoorraadController() {
        try (InputStream fis = getClass().getClassLoader().getResourceAsStream("AMCVoorraad.xlsx")) {
            if (fis == null) {
                System.out.println("Excel-bestand niet gevonden.");
                return;
            }

            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Cell excelNdc = row.getCell(0);
                Cell excelId = row.getCell(1);
                Cell excelDetails = row.getCell(2);

                String id = getCellValueAsString(excelId);
                String ndc = getCellValueAsString(excelNdc);
                String details = getCellValueAsString(excelDetails);

                Item item = new Item(id, ndc, details);
                voorraad.add(item);
            }

            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    @GetMapping("/voorraad")
    public List<Item> getVoorraad() {
        return voorraad;
    }
}