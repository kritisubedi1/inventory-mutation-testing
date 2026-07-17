package com.mun.inventorysystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// reads PIT's mutation XML report and generates plain-language feedback
// on why mutants survived and how to fix the tests. Uses Claude if an
// API key is configured, otherwise falls back to a rule-based demo mode.
@Service
public class AiTestAnalysisService {

    public String analyzeMutationReport(String xmlPath) {
        List<Map<String, String>> mutants = parseMutations(xmlPath);

        if (mutants.isEmpty()) {
            return "No mutation report found at " + xmlPath + " - run PIT first.";
        }

        List<Map<String, String>> survived = new ArrayList<>();
        for (Map<String, String> m : mutants) {
            if ("SURVIVED".equals(m.get("status"))) {
                survived.add(m);
            }
        }

        if (survived.isEmpty()) {
            return "All " + mutants.size() + " mutants were killed - no weaknesses detected " +
                    "in the current test suite.";
        }

        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return buildDemoAnalysis(survived, mutants.size());
        }

        try {
            return callClaude(apiKey, survived, mutants.size());
        } catch (Exception e) {
            // if the API call fails for any reason, don't crash - just fall back
            return "AI analysis unavailable (" + e.getMessage() + "), showing demo mode instead:\n\n"
                    + buildDemoAnalysis(survived, mutants.size());
        }
    }

    private List<Map<String, String>> parseMutations(String xmlPath) {
        List<Map<String, String>> results = new ArrayList<>();
        File file = new File(xmlPath);
        if (!file.exists()) {
            return results;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList mutationNodes = doc.getElementsByTagName("mutation");
            for (int i = 0; i < mutationNodes.getLength(); i++) {
                Element el = (Element) mutationNodes.item(i);
                Map<String, String> m = new HashMap<>();
                m.put("status", el.getAttribute("status"));
                m.put("mutatedMethod", getTagValue(el, "mutatedMethod"));
                m.put("lineNumber", getTagValue(el, "lineNumber"));
                m.put("description", getTagValue(el, "description"));
                results.add(m);
            }
        } catch (Exception e) {
            System.out.println("Could not parse mutation report: " + e.getMessage());
        }
        return results;
    }

    private String getTagValue(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl.getLength() == 0) return "";
        return nl.item(0).getTextContent();
    }

    // no API key set - build a readable, rule-based analysis directly from the parsed data
    private String buildDemoAnalysis(List<Map<String, String>> survived, int total) {
        StringBuilder sb = new StringBuilder();
        sb.append("[DEMO MODE - no ANTHROPIC_API_KEY set]\n\n");
        sb.append(survived.size()).append(" of ").append(total).append(" mutants survived.\n\n");

        for (Map<String, String> m : survived) {
            sb.append("- ").append(m.get("mutatedMethod"))
                    .append(" (line ").append(m.get("lineNumber")).append("): ")
                    .append(m.get("description")).append("\n")
                    .append("  Suggestion: this mutant likely survived because no test asserted the ")
                    .append("exact value/message/state changed by this mutation. Add an assertion ")
                    .append("that checks that specific behaviour, not just that the method runs.\n\n");
        }
        return sb.toString();
    }

    // sends the surviving mutants to Claude and returns its written analysis
    private String callClaude(String apiKey, List<Map<String, String>> survived, int total) throws Exception {
        StringBuilder prompt = new StringBuilder();
        prompt.append("I ran PIT mutation testing on a Java service class. ")
                .append(survived.size()).append(" out of ").append(total).append(" mutants survived. ")
                .append("For each one below, explain briefly why it likely survived and suggest a ")
                .append("specific test improvement.\n\n");

        for (Map<String, String> m : survived) {
            prompt.append("- Method: ").append(m.get("mutatedMethod"))
                    .append(", Line: ").append(m.get("lineNumber"))
                    .append(", Mutation: ").append(m.get("description")).append("\n");
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode message = mapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt.toString());

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "claude-sonnet-4-5");
        body.put("max_tokens", 1000);
        body.set("messages", mapper.createArrayNode().add(message));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = mapper.readTree(response.body());
        return json.get("content").get(0).get("text").asText();
    }
}