# PeriodicSyncAndroid

This is a minimal Android project using Jetpack Compose and WorkManager to perform periodic synchronization every five minutes. Counts are stored in a Room database and aggregated by a `SyncWorker`.

Due to Android limitations, periodic workers may run less frequently on production devices. This example requests a five‑minute interval for demonstration purposes.
