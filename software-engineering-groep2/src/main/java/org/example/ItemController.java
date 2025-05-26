package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * REST controller that handles operations related to inventory stock and medication requests.
 * <p>
 * This controller provides endpoints for:
 * <ul>
 *     <li>Viewing, adding, editing, and deleting items in stock</li>
 *     <li>Creating, modifying, and submitting medication request lists</li>
 *     <li>Reviewing requests per user or as admin</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class ItemController {

    /** List that holds all available stock items initialized from Excel.*/
    private final List<ItemDetails> stockList = new ArrayList<>();

    /**
     * Map that stores all submitted requests, grouped by username.
     * Key = username, Value = list of requests submitted by that user.
     */
    private final Map<String, List<Request>> requestMap = new HashMap<>();

    /**
     * Temporary in-progress request object used to build the current order before submission.
     */
    private Request temporaryRequest = new Request("", "", "", "", new ArrayList<>());

    public ItemController() {
        initializeStockFromExcel();
    }

    /**
     * Initializes the stock list by reading items from the 'AMCVoorraad.xlsx' Excel file
     * located in the classpath. Each row in the first sheet (excluding the header) is
     * interpreted as an item, where:
     * <ul>
     *     <li>Column 0 = NDC code</li>
     *     <li>Column 1 = ID</li>
     *     <li>Column 2 = Details</li>
     * </ul>
     * Each item is added to the internal stock list with a default quantity of 1000 units.
     */
    private void initializeStockFromExcel() {
        try (InputStream fis = getClass().getClassLoader().getResourceAsStream("AMCVoorraad.xlsx")) {
            if (fis == null) {
                System.out.println("Excel file not found");
                return;
            }

            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String id = getCellValueAsString(row.getCell(1));
                String ndc = getCellValueAsString(row.getCell(0));
                String details = getCellValueAsString(row.getCell(2));

                stockList.add(new ItemDetails(id, ndc, details, 1000));
            }

            workbook.close();
        } catch (IOException e) {
            System.err.println("Failed to read AMCVoorraad.xlsx: " + e.getMessage());
        }
    }

    /**
     * Converts the given Excel cell to a string, regardless of its original data type.
     * This method ensures consistent string output for numeric, textual, date, or formula cells,
     * using Apache POI's {@link DataFormatter}.
     *
     * @param cell The Excel cell to convert.
     * @return A string representation of the cell's value, or an empty string if the cell is null.
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    // --------- STOCK-MAPPINGS ---------

    /**
     * Returns the full list of available stock items.
     *
     * @return ResponseEntity containing the list of all stock items.
     */
    @GetMapping("/stock/all")
    public ResponseEntity<List<ItemDetails>> getStock() {
        return ResponseEntity.status(200).body(stockList);
    }

    /**
     * Retrieves a specific item from stock based on its ID.
     *
     * @param id The unique identifier of the item.
     * @return ResponseEntity with the item if found, or 404 if not found.
     */
    @GetMapping("/stock/get")
    public ResponseEntity<ItemDetails> getItemById(@RequestParam("id") String id) {
        for (ItemDetails x : stockList) {
            if (x.getId().equals(id)) {
                return ResponseEntity.status(200).body(x);
            }
        }
        return ResponseEntity.status(404).build();
    }

    /**
     * Adds a new item to the stock if it doesn't already exist.
     *
     * @param id The unique ID of the item.
     * @param ndc The NDC code of the item.
     * @param details Additional details of the item.
     * @param amount Quantity to add.
     * @return ResponseEntity indicating success or conflict if ID already exists.
     */
    @PostMapping("/stock/add")
    public ResponseEntity<String> addItem(
            @RequestParam("id") String id,
            @RequestParam("ndc") String ndc,
            @RequestParam("details") String details,
            @RequestParam("amount") int amount) {

        for (ItemDetails x : stockList) {
            if (x.getId().equals(id)) {
                return ResponseEntity
                        .status(409)
                        .body("Error: This item with the given ID already exists in stock.");
            }
        }

        stockList.add(new ItemDetails(id, ndc, details, amount));
        return ResponseEntity
                .status(201)
                .body("Item successfully added to stock.");
    }

    /**
     * Updates the information of an existing item in the stock.
     *
     * @param id The ID of the item to update.
     * @param ndc The new NDC code.
     * @param details The new item details.
     * @param amount The new quantity.
     * @return ResponseEntity indicating success or failure if item not found.
     */
    @PutMapping("/stock/edit")
    public ResponseEntity<String> editItem(
            @RequestParam("id") String id,
            @RequestParam("ndc") String ndc,
            @RequestParam("details") String details,
            @RequestParam("amount") int amount) {

        for (ItemDetails x : stockList) {
            if (x.getId().equals(id)) {
                x.setNdc(ndc);
                x.setDetails(details);
                x.setAmount(amount);
                return ResponseEntity
                        .status(200)
                        .body("Item successfully updated.");
            }
        }

        return ResponseEntity
                .status(404)
                .body("Error: Item with the given ID was not found in stock.");
    }

    /**
     * Deletes an item from the stock list based on its ID.
     *
     * @param id The unique identifier of the item to be deleted.
     * @return ResponseEntity with a success message if the item was deleted,
     *         or an error message if the item was not found.
     */
    @DeleteMapping("/stock/delete")
    public ResponseEntity<String> deleteItem(@RequestParam("id") String id) {
        ItemDetails toRemove = null;

        for (ItemDetails x : stockList) {
            if (x.getId().equals(id)) {
                toRemove = x;
                break;
            }
        }

        if (toRemove != null) {
            stockList.remove(toRemove);
            return ResponseEntity
                    .status(200)
                    .body("Item successfully deleted from stock.");
        }

        return ResponseEntity
                .status(404)
                .body("Error: Item with the given ID was not found in stock.");
    }

    // --------- ORDER-MAPPINGS ---------

    /**
     * Retrieves the current in-progress order list.
     *
     * @return ResponseEntity containing the list of items in the current request.
     */
    @GetMapping("/order/all")
    public ResponseEntity<List<ItemDetails>> getOrder() {
        return ResponseEntity.ok(temporaryRequest.getItems());
    }

    /**
     * Retrieves the most recently submitted request of the current user.
     *
     * @return ResponseEntity containing the list of items in the most recent request.
     */
    @GetMapping("/order/recent")
    public ResponseEntity<List<ItemDetails>> getRecentOrder() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Request> userRequestList = requestMap.get(username);

        if (userRequestList == null || userRequestList.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Request recentRequest = userRequestList.get(userRequestList.size() - 1);

        temporaryRequest = new Request(
                userDetails.getFullName(),
                userDetails.getLocation(),
                recentRequest.getDate(),
                recentRequest.getStatus(),
        new ArrayList<>(recentRequest.getItems())
        );

        return ResponseEntity.ok(temporaryRequest.getItems());
    }

    /**
     * Adds an item to the current request if it exists in stock and quantity is available.
     *
     * @param id ItemDetails ID.
     * @param ndc ItemDetails NDC code.
     * @param details ItemDetails details.
     * @param amount Desired quantity.
     * @return ResponseEntity indicating success, insufficient stock, or duplication.
     */
    @PostMapping("/order/add")
    public ResponseEntity<String> addOrder(
            @RequestParam("id") String id,
            @RequestParam("ndc") String ndc,
            @RequestParam("details") String details,
            @RequestParam("amount") int amount) {

        for (ItemDetails x : temporaryRequest.getItems()) {
            if (x.getId().equals(id)) {
                return ResponseEntity.status(409)
                        .body("Error: This order is already in the current request.");
            }
        }

        for (ItemDetails y : stockList) {
            if (y.getId().equals(id)) {
                if (y.getAmount() >= amount) {
                    temporaryRequest.getItems().add(new ItemDetails(id, ndc, details, amount));
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


    /**
     * Submits the current request for processing and updates the stock.
     *
     * @return ResponseEntity indicating success or failure if stock is insufficient.
     */
    @PostMapping("/order/submit")
    public ResponseEntity<String> submitOrder() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Request> userRequestList = requestMap.computeIfAbsent(username, key -> new ArrayList<>());
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm", Locale.ENGLISH));

        if (temporaryRequest.getItems().isEmpty()) {
            return ResponseEntity.status(400).body("Error: Request is empty.");
        }

        List<ItemDetails> itemDetails = new ArrayList<>(temporaryRequest.getItems());

        Request submittedRequest = new Request(
                userDetails.getFullName(),
                userDetails.getLocation(),
                dateNow,
                "Pending",
                itemDetails
        );

        userRequestList.add(submittedRequest);

        for (ItemDetails i : itemDetails) {
            for (ItemDetails j : stockList) {
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


    /**
     * Edits the amount of an item in the current request.
     *
     * @param id The ID of the item.
     * @param amount The new quantity.
     * @return ResponseEntity indicating success or not found.
     */
    @PutMapping("/order/edit")
    public ResponseEntity<String> editAmountOfOrder(
            @RequestParam("id") String id,
            @RequestParam("amount") int amount) {

        for (ItemDetails x : temporaryRequest.getItems()) {
            if (x.getId().equals(id)) {
                x.setAmount(amount);
                return ResponseEntity.ok("Order successfully updated.");
            }
        }

        return ResponseEntity.status(404)
                .body("Error: Order with the given ID was not found in the request.");
    }

    /**
     * Removes an item from the current request.
     *
     * @param id The ID of the item to remove.
     * @return ResponseEntity indicating success or item not found.
     */
    @DeleteMapping("/order/remove")
    public ResponseEntity<String> removeOrder(@RequestParam("id") String id) {
        ItemDetails toRemove = null;

        for (ItemDetails x : temporaryRequest.getItems()) {
            if (x.getId().equals(id)) {
                toRemove = x;
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

    /**
     * Cancels the current temporary request and clears its content.
     *
     * @return ResponseEntity confirming that the request was canceled.
     */
    @DeleteMapping("/order/cancel")
    public ResponseEntity<String> cancelOrder() {
        temporaryRequest.getItems().clear();
        return ResponseEntity.ok("Request cancelled successfully.");
    }

    // --------- REQUEST-MAPPINGS ---------

    /**
     * Retrieves all previously submitted requests of the current user.
     *
     * @return ResponseEntity containing the list of the user's requests.
     */
    @GetMapping("/request/user/all")
    public ResponseEntity<List<Request>> getALlRequestForUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Request> requestList = requestMap.computeIfAbsent(username, key -> new ArrayList<>());

        return ResponseEntity.ok(requestList);
    }

    /**
     * Retrieves all requests from all users (admin-only usage).
     *
     * @return ResponseEntity containing a list of all requests in the system.
     */
    @GetMapping("/request/admin/all")
    public ResponseEntity<List<Request>> getAllRequestForAdmin() {
        List<Request> adminRequestList = new ArrayList<>();

        for (List<Request> userRequests : requestMap.values()) {
            adminRequestList.addAll(userRequests);
        }

        return ResponseEntity.ok(adminRequestList);
    }

    /**
     * Cancels a previously submitted request for the currently authenticated user.
     *
     * @param id The unique identifier of the request to be cancelled (UUID as String).
     * @return ResponseEntity with a success message if the request was removed, or an error message if the request was not found.
     */
    @DeleteMapping("/request/user/cancel")
    public ResponseEntity<String> cancelSubmittedRequest(@RequestParam("id") String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Request> userRequests = requestMap.get(username);

        if (userRequests != null) {
            Request toRemove = null;

            for (Request r : userRequests) {
                if (r.getId().equals(id)) {
                    toRemove = r;
                    break;
                }
            }

            if (toRemove != null) {
                userRequests.remove(toRemove);
                return ResponseEntity.ok("Request successfully cancelled.");
            }
        }

        return ResponseEntity.status(404).body("Error: Request with the given ID not found.");
    }


    /**
     * Updates the status of a request (e.g., Pending → Approved).
     * Accessible by admin roles.
     *
     * @param id The UUID of the request.
     * @param status The new status value (e.g., Approved, Rejected).
     * @return ResponseEntity indicating success or request not found.
     */
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

    /**
     * Retrieves the items from a specific request by ID.
     * Admins can access all requests; users only their own.
     *
     * @param id The unique identifier of the request.
     * @return ResponseEntity containing the request's items or 404 if not found.
     */
    @GetMapping("/request/view")
    public ResponseEntity<List<ItemDetails>> getRequestById(@RequestParam("id") String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            for (List<Request> adminRequestList : requestMap.values()) {
                for (Request x : adminRequestList) {
                    if (x.getId().equals(id)) {
                        return ResponseEntity.ok(x.getItems());
                    }
                }
            }
        }

        List<Request> userRequestList = requestMap.get(username);
        if (userRequestList != null) {
            for (Request y : userRequestList) {
                if (y.getId().equals(id)) {
                    return ResponseEntity.ok(y.getItems());
                }
            }
        }

        return ResponseEntity.status(404).body(null);
    }

    // --------- EXTRAs ---------

    /**
     * Returns the full name of the currently authenticated user.
     * <p>
     * This endpoint can be used to personalize the frontend,
     * such as showing a welcome message with the user's full name.
     * </p>
     *
     * @return The full name of the logged-in user as a plain text string.
     */
    @GetMapping("/name")
    public String getFullName() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getFullName();
    }
}
