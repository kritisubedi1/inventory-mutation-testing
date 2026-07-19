# Inventory System + Mutation Testing (ENGI 9839)

Kriti Subedi (202583086) · Swapnali Kudale (202583270)

Course project for Software Verification and Validation. We built a small Spring Boot inventory API, wrote a basic test suite, then used JaCoCo + PIT to see what the tests were actually catching. After that we added stronger tests (EP / BVA / category-partition style) and a small endpoint that reads the PIT XML report and explains surviving mutants.

## What we got

| Metric | Baseline | After stronger tests |
|---|---:|---:|
| Line coverage (JaCoCo) | 67% (14/21) | 100% (21/21) |
| Mutation score (PIT) | 68% (13/19) | 100% (19/19) |
| Surviving mutants | 1 | 0 |
| Tests (all suites) | 9 | 24 |

Main takeaway: one test checked that a `RuntimeException` was thrown, but not the message. PIT changed the exception lambda to return null, which still threw an NPE (subclass of RuntimeException), so the weak test still passed. Asserting on `"Product not found"` killed that mutant.

## Stack

- Java 21, Spring Boot 3.5.16
- JUnit 5, Mockito, MockMvc
- JaCoCo 0.8.12, PIT 1.15.8
- H2 in-memory DB
- Optional Claude API for the analysis endpoint (works without a key)

## Layout

```
src/main/java/com/mun/inventorysystem/
  model/Product.java
  repository/ProductRepository.java
  service/ProductService.java
  service/AiTestAnalysisService.java
  controller/ProductController.java
  controller/AiAnalysisController.java

src/test/java/...
  service/ProductServiceTest.java          # first suite
  service/ProductServiceStrongTest.java    # edges / invalid inputs
  controller/ProductControllerTest.java
```

## Run it

Needs JDK 21. Maven wrapper is included.

```bash
# tests + jacoco report -> target/site/jacoco/index.html
./mvnw test

# mutation testing -> target/pit-reports/index.html
./mvnw org.pitest:pitest-maven:mutationCoverage

# start app on :8080
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

### Quick API checks

```bash
curl http://localhost:8080/api/products

curl -X POST http://localhost:8080/api/products ^
  -H "Content-Type: application/json" ^
  -d "{\"name\": \"Laptop\", \"price\": 999.99, \"stockQuantity\": 10, \"discountPercentage\": 15}"

curl -X PUT "http://localhost:8080/api/products/1/stock?quantity=5"

curl http://localhost:8080/api/products/1/final-price

# needs PIT report already generated
curl http://localhost:8080/api/analysis/mutation-report
```

Set `ANTHROPIC_API_KEY` if you want live Claude output. Otherwise it uses the local summary.

## Notes on the stronger tests

`ProductServiceStrongTest` covers things the first suite skipped:

- stock going to exactly 0
- stock going below 0 (should throw)
- missing product (check exception message)
- discount at 0% and 100%
- discount outside 0–100

## Other files

- Full write-up: `ENGI9839_Mutation_Testing_Report.docx` (submitted separately)
- Slides: `ENGI9839_Presentation.pptx`
- Repo: https://github.com/kritisubedi1/inventory-mutation-testing

ENGI 9839 coursework — not for redistribution.
