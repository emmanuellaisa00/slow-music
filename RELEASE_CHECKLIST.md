# Slow Music Release Checklist

## Required environment

- JDK 17
- Android SDK 34
- Gradle wrapper from this repo

## Verify build

```bash
./gradlew clean assembleDebug test
```

## Release signing

The release build reads signing credentials from environment variables so secrets are not committed:

```bash
export SLOW_MUSIC_KEYSTORE_FILE=/absolute/path/slow-music-release.jks
export SLOW_MUSIC_KEYSTORE_PASSWORD='***'
export SLOW_MUSIC_KEY_ALIAS='slow_music_release'
export SLOW_MUSIC_KEY_PASSWORD='***'
./gradlew assembleRelease
```

Create a keystore locally if needed:

```bash
keytool -genkeypair -v \
  -keystore slow-music-release.jks \
  -alias slow_music_release \
  -keyalg RSA -keysize 2048 -validity 10000
```

## Replace before Play Store release

- `@string/privacy_policy_url`
- `@string/terms_url`
- `@string/admob_app_id` with production AdMob app id
- Billing product ids in `BillingManager.kt` with Play Console subscription IDs, if different
- Store listing screenshots and feature graphic

## Security

Never commit keystores, passwords, access tokens, or generated signing configs.
