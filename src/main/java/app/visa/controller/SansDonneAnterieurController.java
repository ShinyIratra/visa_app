package app.visa.controller;

import org.springframework.ui.Model;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/sans-donne-anterieur")
public class SansDonneAnterieurController {

    @GetMapping("/{type}")
    public String list(@PathVariable String type, Model model) {
        model.addAttribute("type", type);
        return "sans-donnee-anterieure/choice-sans-donnee.html";
    }
}
