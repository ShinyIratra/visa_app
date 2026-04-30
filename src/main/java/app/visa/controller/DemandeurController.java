package app.visa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import app.visa.controller.response.ApiResponse;
import app.visa.entity.Demandeur;
import app.visa.service.DemandeurService;

@Controller
@RequestMapping("/demandeurs")
public class DemandeurController {
    @GetMapping
    public String listPage() {
        return "demandeur/list";
    }
}
