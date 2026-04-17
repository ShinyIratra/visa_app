package app.visa.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import app.visa.controller.response.ApiResponse;
import app.visa.entity.Demande;
import app.visa.service.VisaRequestService;

@Controller
@RequestMapping("/visa-requests")
public class VisaRequestController {

    private final VisaRequestService visaRequestService;

    public VisaRequestController(VisaRequestService visaRequestService) {
        this.visaRequestService = visaRequestService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("demandes", visaRequestService.findAll());
        return "visa-requests/list";
    }

    @GetMapping("/new")
    public String showCreateForm() {
        return "visa-requests/form";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> create(@RequestBody Object data) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, data, null)); // Tonga dia hitan'ny client hafa ankotran navigateur nefa tsy nanamboatra CORS akory isika ? Eny e zarantsika aza
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Demande> demandeOptional = visaRequestService.findById(id);
        if (demandeOptional.isEmpty()) {
            return "redirect:/visa-requests";
        }

        model.addAttribute("demande", demandeOptional.get());
        return "visa-requests/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("demande") Demande demande) {
        demande.setId(id);
        if (demande.getDateCreation() == null) {
            demande.setDateCreation(LocalDateTime.now());
        }
        visaRequestService.save(demande);
        return "redirect:/visa-requests";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        visaRequestService.deleteById(id);
        return "redirect:/visa-requests";
    }
}
