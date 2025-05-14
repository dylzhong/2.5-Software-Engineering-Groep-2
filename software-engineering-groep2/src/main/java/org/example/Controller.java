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
public class Controller {

    private final List<Item> stock = new ArrayList<>();
    private final List<Item> request = new ArrayList<>();

    public Controller() {
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

                stock.add(new Item(id, ndc, details, 100));
            }

            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Deze methode zorgt ervoor dat de celwaarde altijd als string wordt teruggegeven,
    // ongeacht of de originele waarde in Excel als getal, tekst, datum of iets anders is opgeslagen.
    // Dit voorkomt problemen bij het uitlezen van cellen met verschillende datatypes.
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    //Stock

    @GetMapping("/stock")
    public ResponseEntity<List<Item>> getStock() {
        return ResponseEntity.status(200).body(stock);
    }

    @GetMapping("/stock/get")
    public ResponseEntity<Item> getItemById(@RequestParam("id") String id) {
        for (Item item : stock) {
            if (item.getId().equals(id)) {
                return ResponseEntity.status(200).body(item);
            }
        }
        return ResponseEntity.status(404).build();
    }

    @PostMapping("/stock/new")
    public ResponseEntity<String> addItemToStock(
            @RequestParam("id") String id,
            @RequestParam("ndc") String ndc,
            @RequestParam("details") String details,
            @RequestParam("amount") int amount) {

        for (Item item : stock) {
            if (item.getId().equals(id)) {
                return ResponseEntity.status(409).build();
            }
        }

        stock.add(new Item(id, ndc, details, amount));
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/stock/edit")
    public ResponseEntity<String> editItemInStock(
            @RequestParam("id") String id,
            @RequestParam("ndc") String ndc,
            @RequestParam("details") String details,
            @RequestParam("amount") int amount) {

        for (Item item : stock) {
            if (item.getId().equals(id)) {
                item.setNdc(ndc);
                item.setDetails(details);
                item.setAmount(amount);
                return ResponseEntity.status(200).build();
            }
        }

        return ResponseEntity.status(404).build();
    }

    @DeleteMapping("/stock/delete")
    public ResponseEntity<String> deleteItemInStock(@RequestParam("id") String id) {
        Item target = null;

        for (Item item : stock) {
            if (item.getId().equals(id)) {
                target = item;
                break;
            }
        }

        if (target != null) {
            stock.remove(target);
            return ResponseEntity.status(200).build();
        }

        return ResponseEntity.status(404).build();
    }

    //Request

    @GetMapping("/request")
    public ResponseEntity<List<Item>> getRequest() {
        return ResponseEntity.status(200).body(request);
    }

    @GetMapping("/request/get")
    public ResponseEntity<Item> getOrderById(@RequestParam("id") String id) {
        for (Item item : request) {
            if (item.getId().equals(id)) {
                return ResponseEntity.status(200).body(item);
            }
        }
        return ResponseEntity.status(404).build();
    }

    @PostMapping("/request/new")
    public ResponseEntity<String> addOrderToRequest(@RequestParam("id") String id, @RequestParam("ndc") String ndc, @RequestParam("details") String details, @RequestParam("amount") int amount) {
        for (Item item : request) {
            if (item.getId().equals(id)) {
                return ResponseEntity.ok("This item is already in the request");
            }
        }

        for (Item item: stock) {
            if (item.getId().equals(id)) {
                if (item.getAmount() >= amount) {
                    item.setAmount(item.getAmount() - amount);
                    request.add(new Item(id, ndc, details, amount));
                    return ResponseEntity.status(201).body("Order successfully added.");
                } else {
                    return ResponseEntity.status(409).body("Not enough stock available");
                }
            }
        }

        return ResponseEntity.status(404).body("Item not found in stock");
    }

    @DeleteMapping("/request/delete")
    public ResponseEntity<String> deleteItemInRequest(@RequestParam("id") String id) {
        Item target = null;

        for (Item item : request) {
            if (item.getId().equals(id)) {
                target = item;
                break;
            }
        }

        if (target != null) {
            request.remove(target);
            return ResponseEntity.ok("Item deleted");
        }

        return ResponseEntity.status(404).body("Item not found");
    }
}
