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

    @GetMapping("/request")
    public String requestPage() {
        return "request";
    }

    @GetMapping("/stock")
    public String stockPage() {
        return "stock";
    }
}
