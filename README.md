# Can Tools Find Real Bugs? — Code Review Proof of Concept

Small experiment for my bachelor thesis *"Vergleich automatisierter Code-Review-Ansätze anhand realer Softwarefehler"*.
I took 4 real bugs from [Defects4J](https://github.com/rjust/defects4j) and checked whether two static analysis tools and an LLM can find them.

## Background

- **Defects4J**: a collection of real bugs from open-source Java projects, each with the buggy and the fixed version.
- **Static analysis**: tools that check code without running it.
  - **PMD** reads the source code (`.java`) and checks it against rules.
  - **SpotBugs** reads the compiled code (`.class`) and looks for bug patterns such as null pointer dereferences.
- **LLM review**: asking a language model (e.g. ChatGPT, Claude) to review the code.

## The bugs

| Bug | Project | File | Buggy line | What goes wrong |
|---|---|---|---|---|
| Lang-33 | Commons Lang | `Lang33.java` | 19 | Calls `getClass()` on a `null` element → NullPointerException |
| Math-94 | Commons Math | `Math94.java` | 10 | `u * v == 0` overflows for big numbers → wrong result |
| Chart-1 | JFreeChart | `Chart1.java` | 36 | `!=` instead of `==` in a null check |
| Lang-39 | Commons Lang | `Lang39.java` | 53 | Loop does not skip `null` entries → NullPointerException |

Each file contains one method copied from the buggy version, simplified so it compiles on its own.
`tests/ReproduceBugs.java` runs every method with an input that triggers the bug.

## How to run

Requirements: JDK 17+.

**1. Prove the bugs are real**
```
javac -g -d build bugs/*.java tests/ReproduceBugs.java
java -cp build ReproduceBugs
```
All 4 lines should print `FAIL`.

**2. PMD** (download the `pmd-dist-...-bin.zip` from https://github.com/pmd/pmd/releases and unzip it into `tools/`)
```
tools/pmd-bin-7.28.0/bin/pmd check -d bugs -R rulesets/java/quickstart.xml -f text -r results/pmd.txt
```
On Windows use `pmd.bat` instead of `pmd`.

**3. SpotBugs** (download `spotbugs-....zip` from https://github.com/spotbugs/spotbugs/releases and unzip it into `tools/`)
```
java -jar tools/spotbugs-4.10.4/lib/spotbugs.jar -textui -low -output results/spotbugs.txt build
```

**4. LLM review**: paste each file from `bugs/` into a new chat (delete the first comment lines, they reveal the bug name) with:
> Review this Java code for bugs. Only report real defects, not style. Give the line number for each.

Save each answer as `results/llm-<bug>.txt`.

## Results

A tool "found" a bug if it reports a problem on the buggy line (or, for Chart-1, the line where the null value is used).

| Bug | PMD | SpotBugs | LLM |
|---|---|---|---|
| Lang-33 | no | no | ? |
| Math-94 | no | no | ? |
| Chart-1 | no | yes | ? |
| Lang-39 | no | no | ? |
| **Found** | 0/4 | 1/4 | ?/4 |
| **Other warnings** | 9 | 2 | ? |

## Observations

_(to be written after running)_

## Limitations

- Only 4 bugs, so this is a test of the method, not a result.
- Defects4J is public, so the LLM may have seen these bugs during training.
- LLM answers change between runs.
