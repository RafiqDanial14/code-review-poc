# Can Tools Find Real Bugs? — Code Review Proof of Concept

Small experiment for my bachelor thesis *"Vergleich automatisierter Code-Review-Ansätze anhand realer Softwarefehler"*.
I took 4 real bugs from [Defects4J](https://github.com/rjust/defects4j) and checked whether four static analysis tools and two LLMs can find them.

## Background

- **Defects4J**: a collection of real bugs from open-source Java projects, each with the buggy and the fixed version.
- **Static analysis**: tools that check code without running it.
  - **PMD** and **Checkstyle** check source code (`.java`) against rules.
  - **Error Prone** runs inside the Java compiler and looks for known bug patterns.
  - **SpotBugs** analyses compiled code (`.class`) and tracks which values variables can have.
- **LLM review**: asking a language model to review the code.

## The bugs

| Bug | Project | File | Buggy line | What goes wrong |
|---|---|---|---|---|
| Lang-33 | Commons Lang | `Lang33.java` | 19 | Calls `getClass()` on a `null` element → NullPointerException |
| Math-94 | Commons Math | `Math94.java` | 10 | `u * v == 0` overflows for big numbers → wrong result |
| Chart-1 | JFreeChart | `Chart1.java` | 36 | `!=` instead of `==` in a null check |
| Lang-39 | Commons Lang | `Lang39.java` | 53 | Loop does not skip `null` entries → NullPointerException |

Each file contains one method from the buggy version, simplified so it compiles on its own.
`tests/ReproduceBugs.java` triggers every bug at runtime (all 4 print `FAIL`).

## Tools and settings

| Tool | Version | Settings |
|---|---|---|
| PMD | 7.28.0 | `rulesets/java/quickstart.xml` |
| Checkstyle | 10.26.1 | `sun_checks.xml` (version 14 needs Java 21) |
| Error Prone | 2.36.0 | default checks, via Maven 3.10.0 (`pom.xml`, `.mvn/jvm.config`) |
| SpotBugs | 4.10.4 | `-low` |
| ChatGPT | web app | thinking off, one new chat per file |
| Mistral | Ollama 0.35.1, local | one new chat per file |

All runs on Java 17. Both LLMs got the same prompt:
> Review this Java code for bugs. Only report real defects, not style. Say which line of code each bug is on.

**LLM input:** the files in `llm-input/` are the bug files with the header comments removed and the class renamed to `Example`, so the bug ID is not visible. Line numbers are unchanged.

## How to run

Requirements: JDK 17+. Put the tools into `tools/` (download links on each tool's GitHub releases page; Maven from https://maven.apache.org/download.cgi). On Windows use `pmd.bat` and `mvn.cmd`.

```
javac -g -d build bugs/*.java tests/ReproduceBugs.java
java -cp build ReproduceBugs

tools/pmd-bin-7.28.0/bin/pmd check -d bugs -R rulesets/java/quickstart.xml -f text -r results/pmd.txt
java -jar tools/checkstyle-10.26.1-all.jar -c /sun_checks.xml bugs -o results/checkstyle.txt
tools/apache-maven-3.10.0/bin/mvn clean compile -l results/errorprone.txt
java -jar tools/spotbugs-4.10.4/lib/spotbugs.jar -textui -low -output results/spotbugs.txt build
```

LLM answers are in `results/llm-<model>-clean-<bug>.txt`.

## Scoring

Scoring follows Habib & Pradel, *"How Many of All Bugs Do We Find? A Study of Static Bug Detectors"* (ASE 2018). A warning is a candidate if it is on or within 1 line of a line changed by the fix (diff-based), or if it disappears after the fix (fixed-warnings-based). Each candidate is then checked by hand and labelled **full match**, **partial match** or **mismatch**.

LLM answers are judged by the code they quote, since their line numbers are not always correct. Because LLMs explain the bug in words, they get their own labels (not the paper's):
- **full**: points at the buggy code and explains the defect correctly
- **partial**: points at the buggy code and suggests a correct fix, but explains the defect wrongly
- **–**: does not point at the buggy code

Example: SpotBugs warned about Chart-1 at line 39, three lines from the fix on line 36. After applying the fix (`fixed/Chart1.java`), both warnings disappear (`results/spotbugs-fixed-Chart1.txt`), so it is a candidate. The fix changes only the line that causes the null dereference, so it is labelled a full match.

## Results

| Bug | PMD | Checkstyle | Error Prone | SpotBugs | ChatGPT | Mistral |
|---|---|---|---|---|---|---|
| Lang-33 | – | – | – | – | full | partial |
| Math-94 | – | – | – | – | full | – |
| Chart-1 | – | – | – | full | full | – |
| Lang-39 | – | – | – | – | full | – |
| **Full matches** | 0/4 | 0/4 | 0/4 | 1/4 | 4/4 | 0/4 |
| **Other warnings** | 9 | 45 | 4 | 2 | 0 | 11 |

– = no candidate, or candidate labelled mismatch.

Mistral on Lang-33 is labelled partial (LLM rule): it pointed at the correct line and its fix contained the correct null check, but it claimed null elements are "ignored" instead of causing a NullPointerException.

## Observations

- PMD, Checkstyle and Error Prone produced 58 warnings together, all about style, design, formatting or the missing package. None pointed at a bug.
- SpotBugs found only Chart-1, the one bug where a variable is clearly always `null` when used.
- ChatGPT found all 4 bugs, including the ones that break the documented behaviour (Lang-33, Lang-39) or depend on number ranges (Math-94). It gave wrong line numbers but quoted the correct code.
- Mistral found none fully. Most of its 11 other remarks were wrong, and some of its suggested fixes kept or added bugs.
- A large cloud model clearly outperformed a small local model in this test. The two models differ in more than size (maker, training data, settings), so this is a hypothesis, not a conclusion.
- LLM answers vary between runs: an earlier ChatGPT run (with the bug ID still in the file) made an extra remark about `Integer.MIN_VALUE` that the clean run did not repeat.

## Limitations

- Only 4 bugs, so this is a test of the method, not a result.
- Defects4J is public, so the LLMs may know these bugs from training even without the bug ID.
- Each LLM reviewed each file once; answers vary between runs.
- Tools only saw single simplified methods, not the full projects.
- The fixed-version check was only run for SpotBugs on Chart-1.

## Open questions for the thesis

- Does ChatGPT's lead hold on many more bugs, and does it come from understanding or from training data?
- Should valid extra findings (like the `Integer.MIN_VALUE` remark) count separately from false positives?
- Defects4J verifies its bugs with Java 11; the tools here ran on Java 17. Does that combination work for the full projects?