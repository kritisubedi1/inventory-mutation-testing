package com.mun.inventorysystem.controller;

import com.mun.inventorysystem.service.AiTestAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiAnalysisController {

    @Autowired
    private AiTestAnalysisService aiTestAnalysisService;

    // reads PIT's report and returns a written analysis of any surviving mutants
    @GetMapping("/api/analysis/mutation-report")
    public String getMutationAnalysis() {
        return aiTestAnalysisService.analyzeMutationReport("target/pit-reports/mutations.xml");
    }
}