package org.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller that maps HTTP GET requests to specific Thymeleaf view templates.
 * <p>
 * This controller handles page routing for both users and admins,
 * directing them to login, home, stock, order, and request overview pages.
 */
@Controller
public class PageController {

    /**
     * Handles requests to the root URL and returns the login page.
     *
     * @return The name of the Thymeleaf template for the login page.
     */
    @GetMapping("/")
    public String loginPage() {
        return "index";
    }

    /**
     * Handles requests to the home page after successful login.
     *
     * @return The name of the Thymeleaf template for the home page.
     */
    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    /**
     * Displays the stock management page (admin only).
     *
     * @return The name of the Thymeleaf template for the stock page.
     */
    @GetMapping("/stock")
    public String stockPage() {
        return "stock";
    }

    /**
     * Displays the form where users can build their medication order.
     *
     * @return The name of the Thymeleaf template for the order page.
     */
    @GetMapping("/order")
    public String requestPage() {
        return "order";
    }

    /**
     * Displays a list of all previously submitted requests by the current user.
     *
     * @return The name of the Thymeleaf template for the user's request overview.
     */
    @GetMapping("/request/user")
    public String requestUserPage() {
        return "request-user";
    }

    /**
     * Displays the detailed view of a specific user request.
     *
     * @return The name of the Thymeleaf template for viewing a specific request.
     */
    @GetMapping("/request/view")
    public String requestUserDetailPage() {
        return "request-view";
    }

    /**
     * Displays the admin page for reviewing and managing all user requests.
     *
     * @return The name of the Thymeleaf template for the admin request overview.
     */
    @GetMapping("/request/admin")
    public String requestAdminPage() {
        return "request-admin";
    }
}
