# System design

## CURRENT IMPLEMENTATION

One Spring Boot instance uses PostgreSQL, in-process fixed-window rate limits, synchronous OTP development delivery, one active JWT signing secret, request IDs, and database-backed refresh revocation. Docker Compose is intended for local evaluation, not Internet exposure.

## PRODUCTION EVOLUTION

Place stateless API instances behind an API gateway and load balancer; move counters and short-lived challenge coordination to Redis with atomic scripts. Use managed primary/replica PostgreSQL, bounded connection pools, backups, and tested point-in-time recovery. Put OTP delivery and audit exports on queues. Store secrets and rotating signing keys in a secret manager backed by KMS/HSM, publish key IDs/JWKS, and overlap rotations.

Adopt centralized structured logs, metrics for latency/failure/rate limiting, distributed tracing, alerting, and privacy-aware retention. Feature flags must fail safely. Multi-region service needs region-aware data ownership, replicated revocation, clock monitoring, failover exercises, and documented RPO/RTO. Cache only non-sensitive configuration. A distributed token family model should detect refresh replay and revoke the whole family.
