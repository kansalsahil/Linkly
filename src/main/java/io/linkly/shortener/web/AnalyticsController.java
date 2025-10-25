package io.linkly.shortener.web;

import io.linkly.shortener.analytics.repo.UrlHitCountRepository;
import io.linkly.shortener.analytics.repo.UrlHitEventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsController {

    private final UrlHitCountRepository countRepo;
    private final UrlHitEventRepository eventRepo;

    public AnalyticsController(UrlHitCountRepository countRepo, UrlHitEventRepository eventRepo) {
        this.countRepo = countRepo;
        this.eventRepo = eventRepo;
    }

    @GetMapping("/analytics")
    public String dashboard(Model model) {
        model.addAttribute("counts", countRepo.findAll());
        model.addAttribute("events", eventRepo.findAll());
        return "analytics";
    }
}


