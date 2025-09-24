package com.leadservice.LeadScoring.controller;

import com.leadservice.LeadScoring.model.Lead;
import com.leadservice.LeadScoring.model.Offer;
import com.leadservice.LeadScoring.model.ScoreResult;
import com.leadservice.LeadScoring.service.LeadScoringService;
import com.leadservice.LeadScoring.util.CsvParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class LeadScoringController {

    private final LeadScoringService service;

    public LeadScoringController(LeadScoringService service) {
        this.service = service;
    }

    @PostMapping("/offer")
    public String saveOffer(@RequestBody Offer offer) {
        service.saveOffer(offer);
        return "Offer saved!";
    }

    @PostMapping("/leads/upload")
    public String uploadLeads(@RequestParam("file") MultipartFile file) throws IOException {
        List<Lead> leads = CsvParser.parse(file);
        service.addLeads(leads);
        return leads.size() + " leads uploaded!";
    }

    @PostMapping("/score")
    public List<ScoreResult> scoreLeads() {
        return service.scoreLeads();
    }

    @GetMapping("/score/export")
    public ResponseEntity<byte[]> exportResults() {
        String csv = service.exportResultsAsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=results.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }
}