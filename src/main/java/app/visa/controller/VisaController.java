package app.visa.controller;

import app.visa.service.VisaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/visas")
@RequiredArgsConstructor
public class VisaController {

    private final VisaService visaService;

    @GetMapping
    public String listPage() {
        return "visa/list";
    }

    @GetMapping("/data")
    @ResponseBody
    public List<Map<String, Object>> getListData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return visaService.listVisasAvecInfos(start, end);
    }
}
