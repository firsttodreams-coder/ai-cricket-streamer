AI Cricket Streamer Android v2 — Roman Urdu

Yeh Android Studio project hai jo aapke mobile control panel ko native Android app ke andar chalata hai.

ZAROORI HAQEEQAT:
- Is ZIP mein abhi compiled APK shamil nahi; yeh APK banane wala source project hai.
- Is project ko GitHub par upload karne ke baad Actions workflow debug APK build kar sakta hai.
- APK download karne ke liye GitHub account/repository aur workflow run ki zaroorat hogi.
- YouTube + Facebook dono par asal live stream ke liye alag RTMP relay/server aur encoder abhi bhi zaroori hai. Is app mein live button jaan-boojh kar disabled hai; keys enter karna akela kaafi nahi.
- CricketLiveNow URL app mein pehle se configured hai. Agar website apna content embed hone se rokti hai to link browser mein khulega.

APK banane ka tareeqa:
1. ZIP ko GitHub repository mein upload karein (repository public ya private ho sakti hai).
2. GitHub repository ke Actions tab mein "Build Android APK" workflow chalayein.
3. Build complete hone par workflow run ke Artifacts section se "ai-cricket-streamer-debug-apk" download karein.
4. ZIP artifact extract karke app-debug.apk Android phone par install karein. Android unknown-app install permission maang sakta hai.

Is workflow ke liye GitHub aur internet connection chahiye. Yeh build workflow free GitHub Actions quota ke andar ho sakta hai, lekin account limits apply hoti hain.
