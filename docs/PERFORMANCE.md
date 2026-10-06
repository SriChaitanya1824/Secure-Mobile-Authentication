# Performance report

No reproducible latency measurements are reported because this environment did not provide a running Android emulator and the report must not invent numbers. The included service can be measured locally after `docker compose up --build`.

Measure p50/p95/p99 and error rate for registration/login, OTP verification, and refresh with a fixed warm-up and synthetic accounts. Measure SDK initialization with Android Benchmark and sample startup with Macrobenchmark on a named physical device and emulator image. Record CPU, memory, database connections, JVM warm-up, network topology, payloads, and commit SHA.

Expected investigation points—not measured results—are BCrypt cost during login/OTP, database writes during refresh rotation, container cold start, Retrofit construction during SDK initialization, and Compose first frame. Production decisions require repeated tests under concurrency; localhost numbers are not capacity claims.
