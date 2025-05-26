# MediTrack - A Web-Based System for Managing Medication Requests and Inventory

This project was developed for the course "2.5 Software Engineering" as part of our Bachelor's program. Our team was assigned a real-world problem by an external client and asked to create a working software solution based on their specific needs.

## 📌 Table of Contents

- [About the Project](#-about-the-project)
- [Features](#-features)
- [Built With](#-built-with)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Usage](#-usage)
- [Acknowledgements](#-acknowledgements)

---

## 🧠 About the Project

This project was developed as part of the 2.5 Software Engineering course within the Bachelor's degree program Medical Informatics at the University of Amsterdam. 
The goal of the course is to teach students how to work in a professional software development environment.
Each team was randomly assigned a real-world problem by an external stakeholder. 
Our team was tasked with developing an application to manage medication inventory and handle user requests.

**Team Members:**
- Walther van Ginkel (Project manager)
- Roy van den Kommer (Product Owner)
- Bram Schut (Product Owner)
- Sam Luttmer (Developer - Frontend Integration)
- Dylan Zhong (Developer - Backend Integration)

---

## ✨ Features

This application supports two user profiles—Users and Admins—each with their own set of features and permissions, enabled through a secure login system with role-based access.

### 👤 User Features
- Add medication items from stock to a request list, specifying the desired amount
- Submit the entire request list in one click and automatically reduce stock quantities
- View a full history of previously submitted requests, including date and status
- Open detailed overviews of each submitted request (medication type, quantity, etc.)
- Cancel submitted requests that are still in "Pending" status

### 🛠️ Admin Features
- View all submitted requests from all users in a central admin dashboard
- Approve or reject individual requests with one click, updating their status
- View itemized details for each request (what was ordered, by whom, from which location)
- Add, update, or remove medication items from the central inventory
- Monitor and control stock levels as requests are processed

---

## 🔧 Built With

- Java 17
- Spring Boot
- Maven
- Thymeleaf
- Spring Security
- HTML, CSS, JavaScript

---

## 📂 Project Structure

```
software-engineering-groep2/
├── src/
│   └── main/
│       ├── java/org/example/
│       │   ├── ItemController.java
│       │   ├── Request.java
│       │   ├── SecurityConfig.java
│       │   ├── ...
│       └── resources/
│           ├── templates/
│           └── application.properties
├── pom.xml
└── README.md
```

---

## 🚀 Getting Started

To run this project locally:

1. **Clone the repository:**
   ```bash
   git clone [your-repo-url]
   ```

2. **Open the project in IntelliJ IDEA (recommended)**

3. **Run the project:**
   ```bash
   mvn spring-boot:run
   ```

4. Visit [http://localhost:8080](http://localhost:8080)

---

## 🧪 Usage


To test the application locally, you can log in using one of the predefined test accounts:

### 🔐 Test Accounts

- **Regular User**
   - Username: `user`
   - Password: `user123`

- **Administrator**
   - Username: `admin`
   - Password: `admin123`

> You can modify or add test accounts in the `CustomUserDetails.java` class.

### 🧭 How to Use

#### As a User:
1. Log in using the regular user credentials.
2. Browse the medication stock and add desired items to your request.
3. Submit the request when ready.
4. View the status of your previous requests via the "My Requests" page.
5. If a request is still **Pending**, you can cancel it directly from the overview.

#### As an Admin:
1. Log in using the admin credentials.
2. Navigate to the admin dashboard to see all incoming requests from all users.
3. Click on a request to view its full details.
4. Approve or reject requests as needed.
5. Manage the inventory by adding, editing, or deleting medication items.
---

## 🙌 Acknowledgements

We would like to express our gratitude to the following contributors who made this project possible:

- Our client, for providing a meaningful and realistic use case that challenged us to apply our skills to a real-world scenario.
- Our course mentors and instructors, for their valuable guidance, feedback, and support throughout the development process.
- Our team members, for their collaboration, dedication, and commitment to delivering a functional and well-designed application.
