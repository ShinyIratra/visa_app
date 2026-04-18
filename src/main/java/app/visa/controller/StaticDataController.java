package app.visa.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.visa.controller.response.ApiResponse;
import app.visa.service.StaticDataService;

@RestController
@RequestMapping("/api/static")
public class StaticDataController {

    private final StaticDataService staticDataService;

    public StaticDataController(StaticDataService staticDataService) {
        this.staticDataService = staticDataService;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> all() {
        try {
            Map<String, Object> data = staticDataService.getAllStaticData();
            return ResponseEntity.ok(new ApiResponse<>(true, data, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}
