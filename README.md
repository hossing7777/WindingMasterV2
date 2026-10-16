# استاد سیم‌پیچی (Winding Master)

اپلیکیشن اندروید کاملاً آفلاین و دوزبانه (فارسی/انگلیسی) برای محاسبه و رسم سیم‌پیچی موتورهای الکتریکی تک‌فاز و سه‌فاز.

## ساخت پروژه از اسکریپت

    bash create_project.sh
    cd WindingMaster

## ساخت Gradle Wrapper (یک بار)

اسکریپت gradlew و gradle-wrapper.jar نمی‌سازد. یکی از دو راه:

    # اگر Gradle 8.5 نصب است
    gradle wrapper --gradle-version 8.5

یا پوشه‌ی WindingMaster را در Android Studio باز کنید تا Sync خودش همه چیز را بسازد.

## دستورهای ساخت

    ./gradlew testDebugUnitTest      # تست‌های واحد موتور محاسبات
    ./gradlew assembleDebug          # APK دیباگ در app/build/outputs/apk/debug
    ./gradlew assembleRelease        # APK ریلیز (بدون امضا)

## امضای نسخه‌ی انتشار (کافه‌بازار)

کافه‌بازار APK یا AAB امضاشده می‌خواهد. ساخت keystore:

    keytool -genkeypair -v -keystore release.jks -alias windingmaster \
      -keyalg RSA -keysize 2048 -validity 10000

برای امضا در GitHub Actions این Secret ها را بسازید:
KEYSTORE_BASE64، KEYSTORE_PASSWORD، KEY_ALIAS و KEY_PASSWORD.

## ساختار

    data/          Room، Repository، تنظیمات
    domain/        مدل‌ها، WindingCalculator، CoilGroupAssigner، UseCase
    presentation/  صفحه‌ها، دیاگرام‌ها (Canvas)، ناوبری، تم

## نکات

- بدون هیچ مجوز اینترنتی و بدون Google Play Services.
- تغییر زبان زنده با AppCompatDelegate.setApplicationLocales.
- تک‌فاز فعلاً با کمربندهای ۱۸۰ درجه (نصف‌نصف) محاسبه می‌شود.
