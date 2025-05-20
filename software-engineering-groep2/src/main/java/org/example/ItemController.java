package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ItemController {

    @Autowired
    private Map<String, Profile> userProfiles;

    private final List<Item> stockList = new ArrayList<>();

    private final Map<String, List<Request>> requestMap = new HashMap<>();

    private Request temporaryRequest = new Request(null, null, null, new ArrayList<>(), null);

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
    @GetMapping("/stock/all")
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
    public ResponseEntity<String> addItem(
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
    public ResponseEntity<String> editItem(
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
    public ResponseEntity<String> deleteItem(@RequestParam("id") String id) {
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

    //ORDER-MAPPINGS

    // Deze methode retourneert de aanvraaglijst.
    @GetMapping("/order/all")
    public ResponseEntity<List<Item>> getOrder() {
        return ResponseEntity.ok(temporaryRequest.getItems());
    }

    // Haalt de meest recent ingediende aanvraaglijst op voor de huidige gebruiker.
    @GetMapping("/order/recent")
    public ResponseEntity<List<Item>> getRecentOrder() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Request> userRequests = requestMap.get(username);

        if (userRequests == null || userRequests.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Request recent = userRequests.get(userRequests.size() - 1);
        temporaryRequest = new Request(
                recent.getName(),
                recent.getLocation(),
                recent.getDate(),
                new ArrayList<>(recent.getItems()),
                recent.getStatus()
        );
        return ResponseEntity.ok(temporaryRequest.getItems());
    }

    // Deze methode voegt een item toe aan de huidige aanvraaglijst, mits het in de voorraad zit en beschikbaar is.
    @PostMapping("/order/add")
    public ResponseEntity<String> addOrder(
            @RequestParam("id") String id,
            @RequestParam("ndc") String ndc,
            @RequestParam("details") String details,
            @RequestParam("amount") int amount) {

        for (Item i : temporaryRequest.getItems()) {
            if (i.getId().equals(id)) {
                return ResponseEntity.status(409)
                        .body("Error: This order is already in the current request.");
            }
        }

        for (Item j : stockList) {
            if (j.getId().equals(id)) {
                if (j.getAmount() >= amount) {
                    temporaryRequest.getItems().add(new Item(id, ndc, details, amount));
                    return ResponseEntity.status(201)
                            .body("Order successfully added to the request.");
                } else {
                    return ResponseEntity.status(409)
                            .body("Error: Not enough stock available for this order.");
                }
            }
        }

        return ResponseEntity.status(404)
                .body("Error: Order with the given ID was not found in stock.");
    }


    // Deze methode verstuurt de aanvraaglijst en werkt de voorraad bij.
    @PostMapping("/order/submit")
    public ResponseEntity<String> submitOrder() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Request> userRequests = requestMap.computeIfAbsent(username, key -> new ArrayList<>());

        if (temporaryRequest.getItems().isEmpty()) {
            return ResponseEntity.status(400).body("Error: Request is empty.");
        }

        List<Item> items = new ArrayList<>(temporaryRequest.getItems());
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm", Locale.ENGLISH));

        Profile profile = userProfiles.get(username);
        Request request = new Request(profile.getName(), profile.getLocation(), now, items, "Pending");
        userRequests.add(request);

        // Trek voorraad af
        for (Item i : items) {
            for (Item j : stockList) {
                if (j.getId().equals(i.getId())) {
                    if (j.getAmount() < i.getAmount()) {
                        return ResponseEntity.status(409).body("Error: Not enough stock for item: " + i.getId());
                    }
                    j.setAmount(j.getAmount() - i.getAmount());
                    break;
                }
            }
        }

        temporaryRequest.getItems().clear();
        return ResponseEntity.status(201).body("Request submitted successfully.");
    }

    // Deze methode past het hoeveelheid van een item in de aanvraag.
    @PutMapping("/order/edit")
    public ResponseEntity<String> editOrder(
            @RequestParam("id") String id,
            @RequestParam("amount") int amount) {

        for (Item i : temporaryRequest.getItems()) {
            if (i.getId().equals(id)) {
                i.setAmount(amount);
                return ResponseEntity.ok("Order successfully updated.");
            }
        }

        return ResponseEntity.status(404)
                .body("Error: Order with the given ID was not found in the request.");
    }

    @DeleteMapping("/order/remove")
    public ResponseEntity<String> removeOrder(@RequestParam("id") String id) {
        Item toRemove = null;

        for (Item i : temporaryRequest.getItems()) {
            if (i.getId().equals(id)) {
                toRemove = i;
                break;
            }
        }

        if (toRemove != null) {
            temporaryRequest.getItems().remove(toRemove);
            return ResponseEntity.ok("Order successfully removed from request.");
        }

        return ResponseEntity.status(404)
                .body("Error: Order with the given ID was not found in the request.");
    }

    // Deze methode annuleert de huidige tijdelijke aanvraaglijst zonder wijzigingen in de voorraad.
    @DeleteMapping("/order/cancel")
    public ResponseEntity<String> cancelOrder() {
        temporaryRequest.getItems().clear();
        return ResponseEntity.ok("Request cancelled successfully.");
    }

    //MY REQUEST-MAPPING

    @GetMapping("/request/user/all")
    public ResponseEntity<List<Request>> getALlRequestForUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Request> userRequests = requestMap.computeIfAbsent(username, key -> new ArrayList<>());

        return ResponseEntity.ok(userRequests);
    }

    @GetMapping("/request/view")
    public ResponseEntity<List<Item>> getRequestById(@RequestParam("id") String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        // Als admin → zoek in alle aanvragen van alle gebruikers
        if (isAdmin) {
            for (List<Request> userRequests : requestMap.values()) {
                for (Request r : userRequests) {
                    if (r.getId().equals(id)) {
                        return ResponseEntity.ok(r.getItems());
                    }
                }
            }
        }

        List<Request> userRequests = requestMap.get(username);
        if (userRequests != null) {
            for (Request r : userRequests) {
                if (r.getId().equals(id)) {
                    return ResponseEntity.ok(r.getItems());
                }
            }
        }

        return ResponseEntity.status(404).body(null);
    }

    @GetMapping("/request/admin/all")
    public ResponseEntity<List<Request>> getAllRequestForAdmin() {
        List<Request> allRequests = new ArrayList<>();

        for (List<Request> userRequests : requestMap.values()) {
            allRequests.addAll(userRequests);
        }

        return ResponseEntity.ok(allRequests);
    }

    @PostMapping("/request/admin/status")
    public ResponseEntity<String> updateRequestStatus(@RequestParam UUID id, @RequestParam String status) {
        for (List<Request> userRequests : requestMap.values()) {
            for (Request r : userRequests) {
                if (r.getId().equals(id.toString())) {
                    r.setStatus(status);
                    return ResponseEntity.ok("Status updated to " + status);
                }
            }
        }
        return ResponseEntity.status(404).body("Request not found");
    }
}
