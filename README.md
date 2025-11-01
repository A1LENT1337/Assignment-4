# Assignment 4: Smart City Task Scheduling

## Overview
A comprehensive Java application implementing graph algorithms for optimizing task scheduling in smart city infrastructure. The system analyzes task dependencies using Strongly Connected Components (SCC), topological sorting, and critical path analysis to determine optimal execution sequences and identify scheduling bottlenecks.

## Project Structure
````
assignment4/
├── src/main/java/ # Source code
│ ├── graph/ # Core graph algorithms
│ │ ├── scc/ # Strongly Connected Components
│ │ ├── topo/ # Topological Sort
│ │ ├── dagsp/ # Shortest/Longest Paths in DAG
│ │ ├── core/ # Graph data structures
│ │ └── metrics/ # Performance tracking
│ ├── generator/ # Dataset generation
│ └── Main.java # Application entry point
├── src/test/java/ # JUnit tests (31 tests)
├── data/ # 9 test datasets (JSON)
├── results/ # CSV output files
├── pom.xml # Maven configuration
├── README.md # This file
└── REPORT.md # Comprehensive analysis report
````
## Build Instructions
```bash
# Clone repository
git clone https://github.com/A1LENT1337/Assignment-4
cd assignment4

# Build with Maven
mvn clean compile

# Run tests
mvn test

# Run main program
mvn exec:java -Dexec.mainClass="Main"
```
