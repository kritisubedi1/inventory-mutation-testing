# AI-Assisted Mutation Testing and Test Adequacy Analysis in Spring Boot Microservices

**ENGI 9839 — Software Verification and Validation**
Memorial University of Newfoundland | Instructor: Raja Abbas

**Authors:** Kriti Subedi (202583086) 
· Swapnali Kudale (202583270)

---

## Overview

High code coverage does not guarantee effective tests. This project demonstrates that
gap concretely using a Spring Boot inventory management system as a case study, then
closes it using three complementary strategies:

1. **Mutation testing (PIT)** to measure whether tests actually detect injected faults, not just whether code executes.
2. **Course-taught test design techniques** — Equivalence Partitioning, Boundary Value Analysis, and the Category-Partition Method — to systematically strengthen a weak test suite.
3. **An AI-assisted analysis module** that automatically interprets PIT's mutation reports and recommends specific test improvements.

## Key Results

| Metric | Baseline Suite | Strengthened Suite | Change |
|:--|:-:|:-:|:-:|
| Line Coverage (JaCoCo) | 67% (14/21) | **100%** (21/21) | +33% |
| Mutation Score (PIT) | 68% (13/19) | **100%** (19/19) | +32% |
| Surviving Mutants | 1 | **0** | \u22121 |
| Uncovered Lines | 5 | **0** | \u22125 |
| Total Passing Tests | 9 | **24** | +15 |

> A baseline suite reaching 67% coverage still let a real defect through undetected:
> a test verified that an exception was *thrown*, but not that it carried the *correct
> message*. A mutant that silently corrupted the exception into a `NullPointerException`
> passed the test anyway. See [Section 5.2 of the project report](#reports-and-documentation)
> for the full walkthrough.

## Tech Stack

| Category | Technology |
|:--|:--|
| Language | Java 21 |
| Framework | Spring Boot 3.5.16 |
| Testing | JUnit 5, Mockito, Spring MockMvc |
| Coverage Analysis | JaCoCo 0.8.12 |
| Mutation Testing | PIT (Pitest) 1.15.8 + pitest-junit5-plugin |
| Database | H2 (in-memory) |
| Build Tool | Maven (via included wrapper — no local install needed) |
| AI Integration | Claude API (optional — demo mode requires no key) |

## Project Structure

```
src/main/java/com/mun/inventorysystem/
├── model/
│   └── Product.java                  Entity with Jakarta Bean Validation rules
├── repository/
│   └── ProductRepository.java        Spring Data JPA repository
├── service/
│   ├── ProductService.java           Core business logic (stock, pricing)
│   └── AiTestAnalysisService.java    AI-assisted mutation report analysis
└── controller/
    ├── ProductController.java        REST API for product operations
    └── AiAnalysisController.java     Endpoint for AI-assisted analysis

src/test/java/com/mun/inventorysystem/
├── service/
│   ├── ProductServiceTest.java        Baseline ("happy path") suite
│   └── ProductServiceStrongTest.java  Strengthened suite (EP / BVA / Category-Partition)
└── controller/
    └── ProductControllerTest.java     Web-layer tests (MockMvc)
```

## Getting Started

### Prerequisites

- Java 21 (JDK)
- No local Maven installation required — the project includes the Maven Wrapper (`mvnw`)

### 1. Clone the repository

```bash
git clone https://github.com/kritisubedi1/inventory-mutation-testing.git
cd inventory-mutation-testing
```

### 2. Run the test suite

```bash
./mvnw test
```

Runs all 24 tests (baseline + strengthened + controller suites) and generates a
JaCoCo coverage report at:

```
target/site/jacoco/index.html
```

### 3. Run mutation testing

```bash
./mvnw org.pitest:pitest-maven:mutationCoverage
```

Generates a PIT mutation report at:

```
target/pit-reports/index.html
```

### 4. Start the application

```bash
./mvnw spring-boot:run
```

The API is now available at `http://localhost:8080`.

## Sample Usage

**List all products**
```bash
curl http://localhost:8080/api/products
```

**Add a product**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop", "price": 999.99, "stockQuantity": 10, "discountPercentage": 15}'
```

**Update stock**
```bash
curl -X PUT "http://localhost:8080/api/products/1/stock?quantity=5"
```

**Get final price after discount**
```bash
curl http://localhost:8080/api/products/1/final-price
```

**Run the AI-assisted mutation analysis**
```bash
curl http://localhost:8080/api/analysis/mutation-report
```

> Reads the report generated in step 3 above. Runs in **demo mode** by default —
> no API key required. To enable live Claude-generated analysis instead, set the
> `ANTHROPIC_API_KEY` environment variable before starting the application.

## Test Design Methodology

The strengthened test suite (`ProductServiceStrongTest`) was not simply larger than
the baseline — it was built using three techniques from this course, with every test
case traceable to a specific design rationale:

- **Equivalence Partitioning (EP)** — inputs grouped into classes expected to be
  handled identically (e.g., a stock update that keeps quantity non-negative vs.
  one that would drive it negative).
- **Boundary Value Analysis (BVA)** — tests placed precisely at partition edges,
  where off-by-one faults are most likely (e.g., stock reduced to exactly `0`,
  discount at exactly `0%` and `100%`).
- **Category-Partition Method** — parameters formalized into categories with
  explicit single-constraint (corner-case) and error-constraint (invalid input)
  test cases.

## Reports and Documentation

| Artifact | Location |
|:--|:--|
| Coverage report (HTML) | `target/site/jacoco/index.html` (generated by step 2) |
| Mutation testing report (HTML) | `target/pit-reports/index.html` (generated by step 3) |
| Full project report (IEEE format) | Submitted separately per course requirements |
| Literature summary & bibliography | Included in project report, Section 2 |
| Presentation slides | Submitted separately per course requirements |

## License

Submitted as coursework for ENGI 9839, Memorial University of Newfoundland.
Not licensed for external use or distribution.