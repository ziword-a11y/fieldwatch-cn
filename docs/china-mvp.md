# Fieldwatch China MVP

This branch adapts Fieldwatch for practical use in China without pretending that a phone can identify every camera or prove a fake base station.

## Goals

1. Prioritize radio signatures that are useful in China: Hikvision, Dahua, Uniview, EZVIZ, Tapo, Reolink, DJI, Autel, Remote ID, and common tracker families.
2. Fuse multiple independent observations for hidden-camera hunting instead of treating one OUI, SSID, or BLE name as proof.
3. Build on Fieldwatch's existing cell-place baseline. A new cell, downgrade, signal jump, short-lived identity, unexpected PLMN, and platform cellular-security warnings are evidence signals, not an attack verdict.
4. Treat public-camera ownership separately from camera detection. Brand, shape, pole mounting, or radio signature cannot prove government ownership. Ownership labels require a source-backed record.

## First implementation

`ChinaPrivacyProfile.kt` contains pure Kotlin scoring logic with no Android dependency, so it can be unit-tested before UI integration.

Three assessments are provided:

- Hidden camera: verified camera signature + optical reflection + local-network camera service + proximity/persistence.
- Public camera: radio + visual + fixed infrastructure + known-camera map record. Ownership remains UNKNOWN unless a provenance record exists.
- Cell anomaly: existing learned-place cell baseline plus independent anomalies. A single new serving cell intentionally remains low evidence.

The score is not a probability. It is a deterministic evidence weight used to rank what deserves inspection.

## Safety / accuracy rules

- Never display "government camera" from an OUI, SSID, BLE UUID, vendor, or appearance alone.
- Never display "IMSI catcher", "Stingray", or "fake base station detected" from stock Android cell information.
- Generic vendor matches stay weak evidence.
- Camera detection and ownership attribution are separate fields.
- Do not add a Chinese device signature until it has a reproducible packet capture, source, or multiple independent observations.
- Keep scanning passive. Do not probe networks the user is not connected to or authorized to test.

## Next engineering steps

1. Add a China profile toggle that reorders/filters the existing signature catalog without deleting US signatures.
2. Wire the evidence assessments into a new "Privacy findings" screen.
3. Add an optional local-LAN camera-service inspector limited to the current authorized network.
4. Add an optical-reflection workflow using the phone camera/flash as a user-guided check.
5. Collect verified Chinese signatures in an RSI-compatible pack with provenance and test vectors.
6. Add Chinese Remote ID test vectors before claiming full GB-standard compatibility.
7. Add regression tests showing that broad OUIs and generic ESP/Tuya devices cannot become high-confidence camera findings by themselves.

## Upstream

Based on OffGridPete/Fieldwatch under the MIT License. Keep upstream as the source of general Fieldwatch improvements and periodically rebase this fork.
