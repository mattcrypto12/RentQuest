# RentQuest Deployment Guide

## 📱 App Icon Preview

Open `app-icon.svg` in your browser or any SVG editor to preview the icon. The current design is:
- **Solana green coin** with an **unlocked padlock** (representing unlocking your rent SOL)
- **Sparkle effects** for polish
- **Deep purple gradient background** matching Solana brand

---

## 🔧 Building for Release

### 1. Create a Signing Keystore

```bash
keytool -genkey -v -keystore rentquest-release.keystore \
  -alias rentquest \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Save the keystore file somewhere safe (NOT in git). Remember your passwords!

### 2. Configure Signing in Gradle

Create `app/keystore.properties` (add to .gitignore):

```properties
storeFile=../rentquest-release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=rentquest
keyPassword=YOUR_KEY_PASSWORD
```

Update `app/build.gradle.kts` to add signing config:

```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("app/keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = java.util.Properties()
                keystoreProperties.load(keystorePropertiesFile.inputStream())
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 3. Build Release APK

```bash
./gradlew assembleRelease
```

The signed APK will be at: `app/build/outputs/apk/release/app-release.apk`

---

## 🏪 Solana dApp Store Submission

### Step 1: Create Publisher Account

1. Go to **https://publisher.solanamobile.com**
2. Connect your Solana wallet (same one you develop with)
3. Complete publisher profile:
   - Publisher name: Your name or company
   - Contact email
   - Website (optional)

### Step 2: Prepare Store Assets

Required assets for submission:

| Asset | Size | Format |
|-------|------|--------|
| App Icon | 512x512px | PNG |
| Feature Graphic | 1024x500px | PNG/JPG |
| Screenshots | 1080x1920px (portrait) or 1920x1080px (landscape) | PNG/JPG |
| Short Description | 80 chars max | Text |
| Full Description | 4000 chars max | Text |

**Suggested descriptions:**

**Short:** Reclaim SOL locked in empty token accounts. One tap to sweep your rent back.

**Full:**
```
RentQuest helps you recover SOL that's locked away in empty Solana token accounts.

Every token account on Solana requires a small rent deposit (~0.002 SOL). When you trade tokens and they go to zero, that rent stays locked. Over time, this can add up to significant SOL.

Features:
• Scan your wallet for empty token accounts
• See exactly how much SOL you can reclaim  
• Close accounts in batches with one signature
• Works with both SPL Token and Token-2022
• Secure signing via Seed Vault

RentQuest uses your phone's secure Seed Vault for signing - your keys never leave the device.

Built for Solana Seeker.
```

### Step 3: Submit Your App

1. Go to publisher dashboard
2. Click "Submit New App"
3. Upload your signed release APK
4. Fill in app details and upload assets
5. Submit for review

Review typically takes 2-5 business days.

---

## 💰 Solana Seeker Developer Airdrop

### Eligibility Requirements

The Seeker Developer Airdrop rewards developers who:
1. Publish an app on the Solana dApp Store
2. App demonstrates meaningful utility
3. Submitted before the program deadline

### How to Apply

1. **Publish your app first** - You must have a live app on the dApp Store

2. **Register for the program:**
   - Go to **https://solanamobile.com/developers**
   - Look for "Seeker Developer Program" or "Genesis Airdrop for Developers"
   - Connect the wallet you used to publish

3. **Fill out the application:**
   - App name and dApp Store link
   - Description of functionality
   - Your contribution to the Solana Mobile ecosystem

4. **Verification:**
   - The team verifies your app is legitimate
   - Not a clone or low-effort submission
   - Actually provides value to users

### Important Links

- **dApp Store Publisher Portal:** https://publisher.solanamobile.com
- **Solana Mobile Developer Docs:** https://docs.solanamobile.com
- **Developer Discord:** https://discord.gg/solanamobile
- **Seeker Announcements:** https://twitter.com/solonamobile

### Tips for Approval

1. **Quality over speed** - Make sure your app is polished
2. **Unique functionality** - RentQuest fills a real need
3. **Good UX** - Easy onboarding, clear value proposition
4. **Proper Seed Vault integration** - Required for Seeker apps
5. **No bugs** - Test thoroughly on actual Seeker hardware

---

## 🚀 Quick Deployment Checklist

- [ ] Create signing keystore
- [ ] Configure release signing in build.gradle.kts
- [ ] Build release APK: `./gradlew assembleRelease`
- [ ] Test release APK on device
- [ ] Create 512x512 app icon PNG
- [ ] Create 1024x500 feature graphic
- [ ] Take 3-5 screenshots on Seeker
- [ ] Write short and full descriptions
- [ ] Create publisher account at publisher.solanamobile.com
- [ ] Submit app for review
- [ ] Register for developer airdrop after app is live

---

## 📞 Support Channels

If you run into issues:

1. **Solana Mobile Discord** - #developer-support channel
2. **GitHub Issues** - For SDK/MWA bugs
3. **Twitter** - @solanamobile for announcements

Good luck with your submission! 🎉
