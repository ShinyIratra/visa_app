package app.visa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/transfert-visa")
public class TransfertVisaController {

    @GetMapping
    public String list() {
        return "transfert-visa/list.html";
    }

    @GetMapping("/new/ada")
    public String newFormAvecDonneeAnterieure() {
        return "transfert-visa/new_ada.html";
    }

    @GetMapping("/new/sda")
    public String newFormSansDonneeAnterieure() {
        return "transfert-visa/new_sda.html";
    }
}
