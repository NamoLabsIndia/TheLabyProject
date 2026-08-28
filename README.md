<img width="256" height="256" alt="icon_128x128@2x" src="https://github.com/user-attachments/assets/90133f83-b4f6-41c6-aab9-25d0859d2a47" />

# The Laby Project

An offline-first, decentralized peer-to-peer communications infrastructure project by Namo Labs. Laby implements a dual-transport architecture: local Bluetooth/Wi-Fi mesh networks for offline communication, and internet-based Nostr protocol integration for global reach. 

This repository contains the Android implementation of Laby, designed with strict architectural boundaries separating the cryptographic identity layer, offline mesh transport, protocol parsing, and application UI.

## Architecture

Laby enforces a clean dependency flow to ensure protocol integrity and cross-platform maintainability:

1. **APPLICATION** (`in.namolabs.laby.ui`): Jetpack Compose MVVM client.
2. **MESSAGING** (`in.namolabs.laby.messaging`): Message routing, queuing, and lifecycle.
3. **PROTOCOL** (`in.namolabs.laby.protocol`): Compact binary packet formats, fragmentation, and deduplication.
4. **MESH / TRANSPORT** (`in.namolabs.laby.mesh`, `.transport`): Bluetooth LE multi-hop routing (max 7 hops) and Wi-Fi Aware.
5. **CRYPTOGRAPHIC IDENTITY & SECURITY** (`in.namolabs.laby.crypto`, `.identity`): Noise Protocol sessions, static key derivation, and Nostr identities.

## Current Development Status

The Laby Project is in active research and development. 

### Implemented Features
- **Offline Mesh Transport**: Direct peer-to-peer communication within Bluetooth range, with multi-hop relaying through nearby devices.
- **Nostr Integration**: Global reach via public relays when the mesh is unavailable.
- **Geohash Channels**: Geographic chat rooms using geohash coordinates over Nostr.
- **End-to-End Encryption**: Private messages secured via the [Noise Protocol](https://noiseprotocol.org) (XX pattern, X25519 + ChaCha20-Poly1305) over the mesh.
- **Cross-Platform Protocol**: Binary protocol compatible with Laby implementations on iOS and macOS.

### Planned Functionality
- **Decentralized Integrations**: Future integration with Solana for identity attestation and coordination. (Note: Message content will strictly remain off-chain and private).
- **Advanced Transports**: Additional high-bandwidth local mesh adapters.

## Building

Requires Android Studio and the Android SDK (API 26+).

```bash
git clone https://github.com/namolabs/laby-android.git
cd laby-android
./gradlew assembleDebug
```

The application requests Bluetooth, location (required by Android for BLE scanning), and notification permissions at runtime.

### Reproducible Builds
Release APKs and the Android App Bundle can be rebuilt byte-for-byte in the pinned Linux container. See [Reproducible builds](docs/reproducible-builds.md) for the build trust model and verification procedures. Maintainers should follow the [Android release guide](docs/maintainer-release-guide.md).

## Testing

```bash
# Unit tests
./gradlew test

# Lint
./gradlew lint

# Instrumented tests (requires a device or emulator)
./gradlew connectedAndroidTest
```

*Note: BLE mesh radio-level behavior requires real physical devices for testing. Protocol and session logic are covered by local unit tests.*

## License

This project is released into the public domain. See the [LICENSE.md](LICENSE.md) file for details. Third-party and open-source attributions are preserved within the codebase.
