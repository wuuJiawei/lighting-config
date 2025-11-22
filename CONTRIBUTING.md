# Contributing to lighting-config

Thank you for considering a contribution! Please follow these guidelines to keep the project healthy and releasable to Maven Central.

## Getting started
- JDK 11+, Maven 3.8+.
- Run `mvn clean verify` before submitting changes.
- Keep changes aligned with `docs/系统设计文档.md` and module boundaries.

## Workflow
- Fork or create a feature branch from `main`.
- Add tests for new behavior; avoid breaking public APIs without discussion.
- Format/organize imports per module conventions (no auto-generated reformats across the tree).
- Commit with clear messages; open a PR describing motivation, design impact, and testing.

## Code of Conduct
This project follows the Contributor Covenant. By participating, you agree to abide by the rules in `CODE_OF_CONDUCT.md`.
