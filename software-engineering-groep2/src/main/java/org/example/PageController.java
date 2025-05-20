package org.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String loginPage() {
        return "index";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/stock")
    public String stockPage() {
        return "stock";
    }

    @GetMapping("/order")
    public String requestPage() {
        return "order";
    }

    @GetMapping("/request/user")
    public String requestUserPage() {
        return "request-user";
    }

    @GetMapping("/request/view")
    public String requestUserDetailPage() {
        return "request-view";
    }

    @GetMapping("/request/admin")
    public String requestAdminPage() {
        return "request-admin";
    }
}
