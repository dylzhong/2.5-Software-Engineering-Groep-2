package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ItemController {

    private final List<Item> stockList = new ArrayList<>();
    private List<Item> temporaryList = new ArrayList<>();
    private final Map<String, List<List<Item>>> requestMap = new HashMap<>();

    public ItemController() {
        // Probeer het Excel-bestand 'AMCVoorraad.xlsx' te openen.
        try (InputStream fis = getClass().getClassLoader().getResourceAsStream("AMCVoorraad.xlsx")) {

            // Als het bestand niet gevonden wordt, geef een melding en stop de uitvoering.
            if (fis == null) {
                System.out.println("Excel file not found");
                return;
            }

            // Lees het Excel-bestand in met Apache POI
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0); // Gebruik het eerste werkblad

            // Loop door alle rijen in het werkblad (behalve de header)
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                // Haal cellen op voor NDC, ID en details.
                Cell excelNdc = row.getCell(0);
                Cell excelId = row.getCell(1);
                Cell excelDetails = row.getCell(2);

                // Zet de celwaarden om naar strings.
                String id = getCellValueAsString(excelId);
                String ndc = getCellValueAsString(excelNdc);
                String details = getCellValueAsString(excelDetails);

                // Voeg het item toe aan de voorraadlijst met een standaard hoeveelheid van 100.
                stockList.add(new Item(id, ndc, details, 100));
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

    //STOCK-MAPPINGS

    // Deze methode retourneert de voorraadlijst.
    @GetMapping("/stock")
    public ResponseEntity<List<Item>> getStock() {
        return ResponseEntity.status(200).body(stockList);
    }

    // Deze methode zoekt een item op in de voorraad aan de hand van het ID.
    @GetMapping("/stock/get")
    public ResponseEntity<Item> getItemById(@RequestParam("id") String id) {
        for (Item i : stockList) {
            if (i.getId().equals(id)) {
                return ResponseEntity.status(200).body(i);
            }
        }
        return ResponseEntity.status(404).build();
    }

    // Deze methode voegt een nieuw item toe aan de voorraad.
    @PostMapping("/stock/add")
    public ResponseEntity<String> addItemToStock(
            @RequestParam("id") String id,
            @RequestParam("ndc") String ndc,
            @RequestParam("details") String details,
            @RequestParam("amount") int amount) {

        for (Item item : stockList) {
            if (item.getId().equals(id)) {
                return ResponseEntity
                        .status(409)
                        .body("Error: This item with the given ID already exists in stock.");
            }
        }

        stockList.add(new Item(id, ndc, details, amount));
        return ResponseEntity
                .status(201)
                .body("Item successfully added to stock.");
    }

    // Deze methode past een item in de voorraad aan.
    @PutMapping("/stock/edit")
    public ResponseEntity<String> editItemInStock(
            @RequestParam("id") String id,
            @RequestParam("ndc") String ndc,
            @RequestParam("details") String details,
            @RequestParam("amount") int amount) {

        for (Item item : stockList) {
            if (item.getId().equals(id)) {
                item.setNdc(ndc);
                item.setDetails(details);
                item.setAmount(amount);
                return ResponseEntity
                        .status(200)
                        .body("Item successfully updated.");
            }
        }

        return ResponseEntity
                .status(404)
                .body("Error: Item with the given ID was not found in stock.");
    }

    // Deze methode verwijdert een item uit de voorraad op basis van het ID.
    @DeleteMapping("/stock/delete")
    public ResponseEntity<String> deleteItemInStock(@RequestParam("id") String id) {
        Item target = null;

        for (Item item : stockList) {
            if (item.getId().equals(id)) {
                target = item;
                break;
            }
        }

        if (target != null) {
            stockList.remove(target);
            return ResponseEntity
                    .status(200)
                    .body("Item successfully deleted from stock.");
        }

        return ResponseEntity
                .status(404)
                .body("Error: Item with the given ID was not found in stock.");
    }

    //REQUEST-MAPPINGS

    // Deze methode retourneert de aanvraaglijst.
    @GetMapping("/request")
    public ResponseEntity<List<Item>> getRequest() {
        return ResponseEntity.status(200).body(temporaryList);
    }

    // Haalt de meest recent ingediende aanvraaglijst op voor de huidige gebruiker.
    @GetMapping("/request/recent")
    public ResponseEntity<List<Item>> getRecentRequest() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<List<Item>> requestList = requestMap.get(username);

        if (requestList == null || requestList.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        List<Item> recentRequest = requestList.get(requestList.size() - 1);
        temporaryList = new ArrayList<>(recentRequest);
        return ResponseEntity.ok(temporaryList);
    }

    // Deze methode voegt een item toe aan de huidige aanvraaglijst, mits het in de voorraad zit en beschikbaar is.
    @PostMapping("/request/add")
    public ResponseEntity<String> addItemToRequest(
            @RequestParam String id,
            @RequestParam String ndc,
            @RequestParam String details,
            @RequestParam int amount) {

        // Controleer of het item al in de tijdelijke aanvraaglijst zit
        for (Item i : temporaryList) {
            if (i.getId().equals(id)) {
                return ResponseEntity
                        .status(409)
                        .body("Error: This item is already in the current request.");
            }
        }

        // Controleer of het item in de voorraad zit en of er voldoende beschikbaar is
        for (Item j : stockList) {
            if (j.getId().equals(id)) {
                if (j.getAmount() >= amount) {
                    temporaryList.add(new Item(id, ndc, details, amount));
                    return ResponseEntity
                            .status(201)
                            .body("Item successfully added to the request.");
                } else {
                    return ResponseEntity
                            .status(409)
                            .body("Error: Not enough stock available for this item.");
                }
            }
        }

        return ResponseEntity
                .status(404)
                .body("Error: Item with the given ID was not found in stock.");
    }


    // Deze methode verstuurt de aanvraaglijst en werkt de voorraad bij.
    @PostMapping("/request/submit")
    public ResponseEntity<String> submitListToRequest() {
        // Haal de gebruikersnaam op uit de beveiligingscontext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Haal de bestaande aanvragenlijst op of maak er één aan voor de gebruiker
        List<List<Item>> requestList = requestMap.computeIfAbsent(username, key -> new ArrayList<>());

        // Maak een kopie van de tijdelijke aanvraaglijst
        List<Item> newRequest = new ArrayList<>(temporaryList);
        requestList.add(newRequest);

        // Controleer voor elk item of er genoeg voorraad is en trek het af
        for (Item i : newRequest) {
            for (Item j : stockList) {
                if (j.getId().equals(i.getId())) {
                    if (j.getAmount() < i.getAmount()) {
                        return ResponseEntity
                                .status(409)
                                .body("Error: Not enough stock for item: " + i.getId());
                    }

                    j.setAmount(j.getAmount() - i.getAmount());
                    break;
                }
            }
        }

        // Maak de tijdelijke lijst leeg na succesvolle versturing
        temporaryList.clear();

        return ResponseEntity
                .status(201)
                .body("Request submitted and stock updated successfully.");
    }

    // Deze methode past het hoeveelheid van een item in de aanvraag.
    @PutMapping("/request/edit")
    public ResponseEntity<String> editAmountOfRequest(
            @RequestParam("id") String id,
            @RequestParam("amount") int amount) {

        for (Item i : temporaryList) {
            if (i.getId().equals(id)) {
                i.setAmount(amount);
                return ResponseEntity
                        .status(200)
                        .body("Item successfully updated.");
            }
        }

        return ResponseEntity
                .status(404)
                .body("Error: Item with the given ID was not found in stock.");
    }

    // Deze methode annuleert de huidige tijdelijke aanvraaglijst zonder wijzigingen in de voorraad.
    @DeleteMapping("/request/cancel")
    public ResponseEntity<String> cancelRecentRequest() {
        temporaryList.clear();
        return ResponseEntity
                .status(200)
                .body("Temporary request list cancelled successfully.");
    }
}
