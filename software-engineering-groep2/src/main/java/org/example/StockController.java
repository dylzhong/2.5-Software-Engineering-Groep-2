package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
public class StockController {

    private final List<Item> stock = new ArrayList<>();

    public StockController() {
        try (InputStream fis = getClass().getClassLoader().getResourceAsStream("AMCVoorraad.xlsx")) {
            if (fis == null) {
                System.out.println("Excel file not found");
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
                stock.add(item);
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

    @GetMapping("/stock")
    public ResponseEntity<?> getStock() {
        if (stock.isEmpty()) {
            return ResponseEntity.ok("Item not found");
        } else {
            return ResponseEntity.ok(stock);
        }
    }

    @GetMapping("/stock/get")
    public ResponseEntity<?> getItemById(@RequestParam("id") String id) {
        for (Item item : stock) {
            if (item.getId().equals(id)) {
                return ResponseEntity.ok(item);
            }
        }

        return ResponseEntity.ok("Item not found");
    }

    @GetMapping("/stock/new")
    public ResponseEntity<String> addItem(@RequestParam("id") String id, @RequestParam("ndc") String ndc, @RequestParam("details") String details) {
        for (Item i : stock) {
            if (i.getId().equals(id)) {
                return ResponseEntity.ok("This item already exists");
            }
        }

        stock.add(new Item(id, ndc, details));
        return ResponseEntity.ok("Item added");
    }

    @GetMapping("/stock/edit")
    public ResponseEntity<String> editItem(@RequestParam("id") String id, @RequestParam("ndc") String ndc, @RequestParam("details") String details) {
        for (Item i : stock) {
            if (i.getId().equals(id)) {
                i.setNdc(ndc);
                i.setDetails(details);
                return ResponseEntity.ok("Item updated");
            }
        }

        return ResponseEntity.ok("Item not found");
    }

    @GetMapping("/stock/delete")
    public ResponseEntity<String> deleteItem(@RequestParam("id") String id) {
        Item i = null;

        for (Item item : stock) {
            if (item.getId().equals(id)) {
                i = item;
                break;
            }
        }

        if (i != null) {
            stock.remove(i);
            return ResponseEntity.ok("Item deleted");
        }

        return ResponseEntity.ok("Item not found");
    }
}