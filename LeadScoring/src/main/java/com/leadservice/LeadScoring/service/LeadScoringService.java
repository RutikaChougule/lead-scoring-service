package com.leadservice.LeadScoring.service;

import com.leadservice.LeadScoring.model.Lead;
import com.leadservice.LeadScoring.model.Offer;
import com.leadservice.LeadScoring.model.ScoreResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Service
public class LeadScoringService {

    private Offer offer;
    private final List<Lead> leads = new ArrayList<>();
    private List<ScoreResult> lastResults = new ArrayList<>();

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String openAiKey;

    @Value("${openai.api.url}")
    private String openAiUrl;

    // ✅ Configurable batch size
    @Value("${openai.batch.size:20}")
    private int batchSize;

    public LeadScoringService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    // --- Offer ---
    public void saveOffer(Offer offer) {
        this.offer = offer;
    }

    // --- Leads ---
    public void addLeads(List<Lead> leadList) {
        leads.clear();
        leads.addAll(leadList);
    }

    // --- Scoring ---
    public List<ScoreResult> scoreLeads() {
        List<ScoreResult> results = new ArrayList<>();

        // Rule scores first (fast, local)
        Map<Lead, Integer> ruleScores = new HashMap<>();
        for (Lead lead : leads) {
            ruleScores.put(lead, calculateRuleScore(lead, offer));
        }

        // AI scores in batches
        Map<Lead, Integer> aiScores = getAiScoresInBatches(leads, offer);

        // Merge results
        for (Lead lead : leads) {
            int ruleScore = ruleScores.getOrDefault(lead, 0);
            int aiScore = aiScores.getOrDefault(lead, 30); // fallback Medium
            int finalScore = ruleScore + aiScore;

            ScoreResult result = new ScoreResult();
            result.setName(lead.getName());
            result.setRole(lead.getRole());
            result.setCompany(lead.getCompany());
            result.setScore(finalScore);
            result.setIntent(getIntent(finalScore));
            result.setReasoning("Rule: " + ruleScore + " + AI: " + aiScore);

            results.add(result);
        }
        lastResults = results;
        return results;
    }

    // --- CSV Export ---
    public String exportResultsAsCsv() {
        if (lastResults.isEmpty()) {
            lastResults = scoreLeads();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Name,Role,Company,Score,Intent,Reasoning");

        for (ScoreResult r : lastResults) {
            pw.printf("\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\"%n",
                    r.getName(),
                    r.getRole(),
                    r.getCompany(),
                    r.getScore(),
                    r.getIntent(),
                    r.getReasoning().replace("\"","'")
            );
        }
        pw.flush();
        return sw.toString();
    }

    // --- Private Helpers ---
    private int calculateRuleScore(Lead lead, Offer offer) {
        int score = 0;
        String role = lead.getRole().toLowerCase();

        if (role.contains("head") || role.contains("vp") || role.contains("founder") || role.contains("director"))
            score += 20;
        else if (role.contains("manager") || role.contains("lead"))
            score += 10;

        if (lead.getIndustry().equalsIgnoreCase(offer.getIdealUseCases()))
            score += 20;
        else if (lead.getIndustry().toLowerCase().contains(offer.getIdealUseCases().toLowerCase()))
            score += 10;

        if (lead.getName() != null && lead.getRole() != null && lead.getCompany() != null &&
                lead.getIndustry() != null && lead.getLocation() != null && lead.getLinkedinBio() != null)
            score += 10;

        return score;
    }

    // ✅ Batch AI scoring
    private Map<Lead, Integer> getAiScoresInBatches(List<Lead> leads, Offer offer) {
        Map<Lead, Integer> scores = new HashMap<>();

        for (int i = 0; i < leads.size(); i += batchSize) {
            List<Lead> batch = leads.subList(i, Math.min(i + batchSize, leads.size()));
            scores.putAll(callOpenAiForBatch(batch, offer));

            try {
                Thread.sleep(1000L); // small delay to avoid hitting rate limits
            } catch (InterruptedException ignored) {}
        }

        return scores;
    }

    private Map<Lead, Integer> callOpenAiForBatch(List<Lead> batch, Offer offer) {
        StringBuilder promptBuilder = new StringBuilder("Classify intent for these leads:\n");
        promptBuilder.append("Offer: ").append(offer.getName()).append(" - ").append(offer.getValueProps())
                .append("\nIdeal Use Cases: ").append(offer.getIdealUseCases()).append("\n\n");

        int index = 1;
        for (Lead lead : batch) {
            promptBuilder.append(index++).append(". ")
                    .append("Name: ").append(lead.getName()).append(", ")
                    .append("Role: ").append(lead.getRole()).append(", ")
                    .append("Company: ").append(lead.getCompany()).append(", ")
                    .append("Industry: ").append(lead.getIndustry()).append(", ")
                    .append("Location: ").append(lead.getLocation()).append(", ")
                    .append("Bio: ").append(lead.getLinkedinBio())
                    .append("\n");
        }

        promptBuilder.append("\nRespond with numbered results like:\n")
                .append("1. High - reason...\n2. Medium - reason...\n");

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are an AI sales assistant."),
                        Map.of("role", "user", "content", promptBuilder.toString())
                ),
                "max_tokens", 500
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri(openAiUrl)
                    .header("Authorization", "Bearer " + openAiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                String content = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");

                return parseBatchResponse(content, batch);
            }
        } catch (Exception e) {
            System.out.println("AI scoring batch error: " + e.getMessage());
        }

        // fallback
        Map<Lead, Integer> fallback = new HashMap<>();
        for (Lead lead : batch) {
            fallback.put(lead, 30);
        }
        return fallback;
    }

    // ✅ Simple parser to map AI text → score
    private Map<Lead, Integer> parseBatchResponse(String response, List<Lead> batch) {
        Map<Lead, Integer> scores = new HashMap<>();
        String[] lines = response.split("\n");

        for (int i = 0; i < batch.size() && i < lines.length; i++) {
            String line = lines[i].toLowerCase();
            if (line.contains("high")) scores.put(batch.get(i), 50);
            else if (line.contains("medium")) scores.put(batch.get(i), 30);
            else scores.put(batch.get(i), 10);
        }

        return scores;
    }

    private String getIntent(int score) {
        if (score >= 80) return "High";
        else if (score >= 50) return "Medium";
        return "Low";
    }
}
