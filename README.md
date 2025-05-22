# 2.5-Software-Engineering-Groep-2

A simple web application for managing medical item stock and user requests within a hospital setting. The system includes separate interfaces for regular users and admins.

## 🌐 Features

### ✅ For Users
- View available stock items
- Create a new medication request from available stock
- Submit a request (if all items are in stock)
- View history of previous requests
- View item details per request
- Request status tracking (Pending, Approved, Rejected)

### 🔐 For Admins
- View all submitted requests from all users
- Approve or reject requests
- View request details (all items within the request)
- Live request status updates
- View current stock (with inline editing and delete buttons)

## 📦 Technologies Used

- Java 17
- Spring Boot (REST API + Security)
- Thymeleaf (HTML templates)
- jQuery + DataTables (frontend interactiviteit)
- Apache POI (voor inlezen van Excelbestand)
- In-memory data storage (no database)
- Spring Security (simple role-based login)

## 🛠 How to Run

1. Clone the repository
2. Open in your IDE (e.g. IntelliJ)
3. Make sure the file `AMCVoorraad.xlsx` is present in `src/main/resources/`
4. Run the Spring Boot application
5. Access the app via:
    - `http://localhost:8080` (login required)

## 👥 User Roles

- **User**:
    - Can make medication requests
    - Can only see own request history
- **Admin**:
    - Can see **all** requests
    - Can approve/reject requests
    - Can view stock and edit inline

## 📁 Project Structure

- `/api/stock/...` → Stock management endpoints
- `/api/order/...` → Temporary request logic
- `/api/request/...` → Request history and status
- `/templates/...` → Thymeleaf frontend pages
- `/static/...` → CSS & JS assets

## 📝 Notes

- No database: all data is held in memory (will reset on restart).
- All requests are uniquely identified with a `UUID`.
- Date format: `E, dd MMM yyyy HH:mm` (e.g. `Mon, 20 May 2025 13:45`).

---

Made for educational/demo purposes.