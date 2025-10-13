# Library Reactive Stack – Dual OAuth2 Resource Servers Example

This project demonstrates a reactive, token-secured library system built with Spring WebFlux and Spring Security (Reactive).
Both services act as OAuth2 Resource Servers, performing JWT validation against a common Authorization Server (Keycloak).
An external Angular SPA (library-client-spa) authenticates users and calls the library-frontend REST API, which in turn calls library-backend.

## Project Structure (MonoRepo)

| Module               | Description                                                            |
|----------------------|------------------------------------------------------------------------|
| **library-commons**  | Shared domain classes, DTOs, and utilities                             |
| **library-frontend** | Reactive API gateway / resource server, called by `library-client-spa` |
| **library-backend**  | Reactive backend service, also an OAuth2 resource server               | |

## Components Overview

| Layer                                                     | Component                           | Purpose                                                                                                 |
|-----------------------------------------------------------|-------------------------------------|---------------------------------------------------------------------------------------------------------|
| **Frontend (`library-frontend`)**                         | Spring Boot WebFlux Resource Server | Validates JWTs from the Angular SPA and forwards requests to the backend using `WebClient`              |
|                                                           | Reactive REST Controllers           | Exposes public JSON APIs (e.g. `/api/books`, `/api/users`)                                              |
|                                                           | WebClient (Reactive)                | Calls `library-backend` with the original Bearer Token                                                  |
| **Backend (`library-backend`)**                           | Spring Boot WebFlux Resource Server | Validates JWTs reactively via the JWKS endpoint from Keycloak                                           |
|                                                           | Reactive REST APIs                  | Provides data and business logic (e.g. `/api/books`, `/api/authors`)                                    |
| **Authorization Server <br/>(Keycloak Docker Container)** | `/token`, `/.well-known/jwks.json`  | Issues JWT access tokens, exposes JWKS for signature verification                                       |
| **Database** <br/>(MySQL Docker Container)                | MySQL                               | Stores Keycloak and Library data                                                                        |~~
| **Client (Browser)**                                      | Webclient SPA (OIDC Client)         | Handles login via Authorization Code Flow (PKCE), stores tokens in browser, and calls the resource APIs |

## Core Spring Components per Module

| Module               | Area                    | Key Classes / Beans                                                                          | Purpose                                              |
|----------------------|-------------------------|----------------------------------------------------------------------------------------------|------------------------------------------------------|
| **library-frontend** | **Security (Reactive)** | `SecurityWebFilterChain` with `.oauth2ResourceServer().jwt()`, `ReactiveJwtDecoder` (Nimbus) | Validates JWTs sent from the Angular SPA             |
|                      | **Web / REST API**      | `@RestController`, returning `Mono<ResponseEntity<?>>` with JSON only                        | Handles REST endpoints and forwards calls to backend |
|                      | **Service / WebClient** | `WebClient` configured with `ExchangeFilterFunction` for bearer propagation                  | Forwards requests and tokens to `library-backend`    |
|                      | **Configuration**       | `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` in `application.yml`                 | Configures JWKS endpoint for token validation        |
| **library-backend**  | **Security (Reactive)** | Same as `library-frontend`: `SecurityWebFilterChain` + `ReactiveJwtDecoder`                  | Validates tokens reactively                          |
|                      | **Web / REST API**      | `@RestController`, JSON only                                                                 | Exposes protected REST endpoints                     |
|                      | **Data Layer**          | `ReactiveCrudRepository`, `@Service`                                                         | Reactive persistence via R2DBC                       |



## Architecture Diagram

```mermaid
flowchart BT
    subgraph SPA["🧭 Angular SPA – library-client-spa"]
        SPA1["Login via OIDC + PKCE"]
        SPA2["Store Access Token"]
        SPA3["Call REST APIs on library-frontend"]
        SPA4["Request User Info from OIDC Endpoint"]
    end

    subgraph FRONTEND["💻 library-frontend (Reactive Resource Server + Gateway)"]
        FE1["Validate JWT"]
        FE2["Forward Request to library-backend (Bearer Token)"]
        FE3["Return JSON to SPA"]
        FE4["Request User Info from OIDC Endpoint"]
    end

    subgraph BACKEND["⚙️ library-backend (Reactive Resource Server)"]
        BE1["Validate JWT via JWKS"]
        BE2["Perform Business Logic"]
        BE3["Return JSON"]
    end

    subgraph AUTH["🛡️ Keycloak (Authorization Server)"]
        AS1["/authorize + /token"]
        AS2["JWKS Endpoint"]
        AS3["/userinfo (User Info Endpoint)"]
    end

    SPA -->|"Authorization Code"| AUTH
    SPA -->|"Bearer Token"| FRONTEND
    FRONTEND -->|"Bearer Token"| BACKEND
    FRONTEND -->|"Validate JWT via JWKS"| AUTH
    FRONTEND -->|"Request User Info"| AUTH
    BACKEND -->|"Validate via JWKS"| AUTH
    BACKEND -->|"JSON"| FRONTEND
    FRONTEND -->|"JSON"| SPA
    SPA -->|"User Info"| AUTH
```

## Sequence Diagram

```mermaid
sequenceDiagram
participant A as Angular SPA
participant F as Frontend Service (Spring WebFlux)
participant K as Keycloak
participant B as Backend Service

    rect rgb(240, 248, 255)
    note over A: 1. User Login
    A->>K: Redirect to Keycloak Login
    K-->>A: Authentication Form
    A->>K: Submit Credentials
    K-->>A: Redirect with Authorization Code
    A->>K: Exchange Code for Tokens
    K-->>A: ID Token + Access Token
    end

    rect rgb(255, 240, 245)
    note over A,F: 2. Access Protected Resource
    A->>F: Request /api/resource (with Bearer Token)
    F->>K: Validate Token (JWT)
    K-->>F: Token Validation Response
    F->>B: Forward Request (with same Bearer Token)
    B->>K: Validate Token (JWT)
    K-->>B: Token Validation Response
    B-->>F: Resource Data
    F-->>A: Resource Data
    end

    rect rgb(230, 255, 230)
        note over A,B: 3. Token Relay Flow
        A->>F: Request with Bearer Token
        F->>F: Extract Token from SecurityContext
        F->>B: Forward Request with same Token
        B->>B: Validate Token & Extract Roles
        B-->>F: Response with Data
        F-->>A: Forward Response
    end
```
