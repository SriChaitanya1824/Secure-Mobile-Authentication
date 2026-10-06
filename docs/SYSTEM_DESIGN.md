# System Design & Scalability — Secure Digital Certificate Wallet

## 1. Production Architecture Evolution

While the portfolio prototype provides a complete local trust environment with Spring Boot, PostgreSQL, and Android, an enterprise digital certificate platform (such as VIDA's production infrastructure) evolves into a high-throughput, fault-tolerant distributed system.

```mermaid
flowchart TD
    Client[Android Wallets / Verifier Clients] --> CDN[Cloudflare CDN / WAF / DDoS Protection]
    CDN --> APIGW[Kong API Gateway / OAuth2 Token Introspection]
    
    subgraph CoreServices[Distributed Microservices Cluster]
        APIGW --> AuthSvc[Authentication & Session Service]
        APIGW --> IssueSvc[Issuance & Signing Service]
        APIGW --> VerifySvc[Verification & Status Engine]
        APIGW --> SyncSvc[Delta Sync Engine]
    end

    subgraph SecurityTier[Hardware Security Layer]
        IssueSvc <--> CloudHSM[AWS CloudHSM / Google Cloud KMS]
    end

    subgraph CacheTier[High-Performance Cache]
        VerifySvc <--> RedisCluster[(Redis Cluster - Revocation Cache & Nonces)]
    end

    subgraph DataTier[Distributed Database Tier]
        AuthSvc & IssueSvc & SyncSvc --> AuroraPG[(Amazon Aurora PostgreSQL Multi-AZ)]
        AuroraPG --> ReadReplica1[(Read Replica 1)]
        AuroraPG --> ReadReplica2[(Read Replica 2)]
    end

    subgraph EventStream[Asynchronous Audit & Event Log]
        IssueSvc & VerifySvc --> Kafka[Apache Kafka Event Bus]
        Kafka --> ClickHouse[(ClickHouse Audit Warehouse)]
        Kafka --> SIEM[Enterprise SIEM / Splunk]
    end
```
