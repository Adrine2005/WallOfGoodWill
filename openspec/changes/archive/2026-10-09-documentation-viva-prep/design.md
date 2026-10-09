# Design: Comprehensive Documentation, Architecture & Viva Prep

## Context

See `proposal.md` for overall context. Wall of Goodwill is a complete campus marketplace and donation system developed with:
- **Backend**: Go with `net/http`, `go-chi/chi/v5`, synchronized domain services (`sync.RWMutex`), RESTful JSON endpoints, and zero-cost in-memory state.
- **Android Client**: Native Java (API 34), Material 3, ViewBinding, MVVM (Repository + ViewModel + LiveData), Retrofit2, and OkHttp3.

## Goals / Non-Goals

**Goals:**
- Provide a clean, comprehensive `README.md` that serves as the entry point for evaluators, teammates, and recruiters.
- Generate `docs/architecture-and-api.md` containing full ASCII/Mermaid component architecture, database schemas, and request lifecycles.
- Generate `docs/viva-defense-guide.md` containing viva examination questions and answers, trade-off defenses, and a 5-minute live demo script.

**Non-Goals:**
- Changing existing production Go or Android source code.
- Adding third-party external paid cloud dependencies.

## Decisions

1. **Modular Documentation Structure**:
   - `README.md`: Quickstart, prerequisites, repository overview, and feature summaries.
   - `docs/architecture-and-api.md`: Deep dive into architectural patterns, concurrency controls, API schemas, and sequence diagrams.
   - `docs/viva-defense-guide.md`: Comprehensive examination prep, covering expected professor questions across Go concurrency, Android MVVM, security rules, and architectural trade-offs.

2. **Mermaid & ASCII Visualizations**:
   - Use clear markdown diagrams for system architecture, sequence flows (QR verification, donation handshake, item reporting), and entity relationships.

3. **Curated Viva Defense Topics**:
   - Concurrency & Thread-Safety: `sync.RWMutex` vs channels in Go.
   - Android Architecture: MVVM vs MVC/MVP, ViewBinding vs `findViewById`, LiveData lifecycle awareness.
   - Offline & Zero-Cost Strategy: How the project runs without credit card requirements or cloud bills.
   - Security: Campus verification via physical notice board QR codes, token bearer authentication, and seller authorization guards.

## Risks / Trade-offs

- **[Risk]** Documentation falling out of sync with future code changes.
  - **Mitigation**: Base documentation strictly on current, verified codebase components and tests.
