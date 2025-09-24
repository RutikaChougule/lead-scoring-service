package com.leadservice.LeadScoring.util;

import com.leadservice.LeadScoring.model.Lead;
import com.opencsv.CSVReader;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

    public static List<Lead> parse(MultipartFile file) {
        List<Lead> leads = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] nextLine;
            reader.readNext(); // skip header
            while ((nextLine = reader.readNext()) != null) {
                Lead lead = new Lead();
                lead.setName(nextLine[0]);
                lead.setRole(nextLine[1]);
                lead.setCompany(nextLine[2]);
                lead.setIndustry(nextLine[3]);
                lead.setLocation(nextLine[4]);
                lead.setLinkedinBio(nextLine[5]);
                leads.add(lead);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing CSV", e);
        }
        return leads;
    }
}
