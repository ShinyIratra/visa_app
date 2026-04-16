package app.visa.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.visa.entity.VisaRequest;
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
        model.addAttribute("visaRequests", visaRequestService.findAll());
        return "visa-requests/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("visaRequest", new VisaRequest());
        model.addAttribute("statuses", VisaRequest.RequestStatus.values());
        return "visa-requests/form";
    }

    @PostMapping
    public String create(@ModelAttribute("visaRequest") VisaRequest visaRequest) {
        if (visaRequest.getStatus() == null) {
            visaRequest.setStatus(VisaRequest.RequestStatus.PENDING);
        }
        visaRequestService.save(visaRequest);
        return "redirect:/visa-requests";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<VisaRequest> visaRequestOptional = visaRequestService.findById(id);
        if (visaRequestOptional.isEmpty()) {
            return "redirect:/visa-requests";
        }

        model.addAttribute("visaRequest", visaRequestOptional.get());
        model.addAttribute("statuses", VisaRequest.RequestStatus.values());
        return "visa-requests/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("visaRequest") VisaRequest visaRequest) {
        visaRequest.setId(id);
        if (visaRequest.getStatus() == null) {
            visaRequest.setStatus(VisaRequest.RequestStatus.PENDING);
        }
        visaRequestService.save(visaRequest);
        return "redirect:/visa-requests";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        visaRequestService.deleteById(id);
        return "redirect:/visa-requests";
    }
}
