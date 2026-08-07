# قبلة اليمن (Qibla Yemen)

تطبيق أندرويد احترافي لتحديد اتجاه القبلة بدقة عالية، مبني بـ Kotlin + Jetpack Compose.

## الميزات
- تحديد تلقائي عبر GPS مع بوصلة حية (Magnetometer + Accelerometer مع Tilt Compensation)
- عند تعذّر GPS أو ضعف الإشارة: انتقال تلقائي لخيار الاختيار اليدوي
- قاعدة بيانات محلية (Room + JSON) لـ 178 مديرية/مركز في 12 محافظة يمنية، تعمل بالكامل بدون إنترنت
- حساب اتجاه القبلة بمعادلة Great Circle الدقيقة (وليس خطاً مستقيماً على خريطة مسطحة)
- عرض المسافة إلى الكعبة بالكيلومترات
- هوية بصرية موحّدة (أخضر/ذهبي) مطابقة لأيقونة التطبيق

## هيكل المشروع
```
app/src/main/java/com/aseelan/qiblayemen/
  ├── MainActivity.kt              نقطة الدخول
  ├── ui/screens/                  الشاشة الرئيسية + شاشة الاختيار اليدوي
  ├── ui/components/               قرص البوصلة المرسوم يدوياً
  ├── ui/theme/                    الألوان والخطوط
  ├── viewmodel/                   منطق التحكم (GPS → Fallback → يدوي)
  ├── location/                    LocationProvider (GPS بمهلة زمنية)
  ├── sensor/                      QiblaSensorManager (دمج المستشعرات)
  ├── util/                        QiblaCalculator (معادلة Great Circle)
  └── data/                        Room + Repository + JSON Seeder
app/src/main/assets/yemen_locations.json   قاعدة البيانات المحلية (قابلة للتعديل)
```

## تحديث/إضافة محافظات ومديريات لاحقاً
عدّل الملف `app/src/main/assets/yemen_locations.json` مباشرة عبر واجهة GitHub على المتصفح، بإضافة عنصر جديد بنفس الصيغة:
```json
{
  "governorate": "اسم المحافظة",
  "district": "اسم المديرية",
  "place": "اسم المركز/القرية",
  "lat": 15.0300,
  "lon": 45.8300,
  "qibla_bearing": 319.85,
  "id": 179
}
```
لحساب `qibla_bearing` تلقائياً، أرسل لي الإحداثيات الجديدة وسأحسبها وأرسل لك التعديل جاهزاً.
بعد أي تعديل وحفظ (commit) على فرع `main`، سيبني GitHub Actions نسخة APK جديدة تلقائياً.

## النشر عبر Termux (من الموبايل بالكامل)

### 1. رفع المشروع لأول مرة
```bash
pkg update && pkg upgrade -y
pkg install git gh -y
gh auth login          # اختر GitHub.com -> HTTPS -> تسجيل الدخول عبر المتصفح

cd ~
git clone https://github.com/abuhamam2015929-netizen/qibla-yemen.git
cd qibla-yemen
# انسخ كل ملفات المشروع هنا (سأرسلها لك كحزمة مضغوطة)
git add .
git commit -m "إصدار أولي - قبلة اليمن"
git push origin main
```

### 2. متابعة بناء APK
بعد الرفع (push)، افتح تبويب **Actions** في مستودع GitHub على المتصفح، وستجد عملية بناء تعمل تلقائياً باسم "بناء تطبيق قبلة اليمن (APK)". عند اكتمالها بنجاح (علامة ✅)، افتح الـ workflow واضغط على **قبلة-اليمن-debug** أسفل قسم Artifacts لتحميل ملف APK مباشرة إلى هاتفك.

### 3. تثبيت APK
افتح ملف APK الذي حمّلته من إشعارات المتصفح أو مدير الملفات، واسمح بالتثبيت من مصادر غير معروفة إذا طُلب ذلك.

### 4. أي تعديل لاحق (مثل تحديث قاعدة البيانات)
```bash
cd ~/qibla-yemen
# عدّل الملف المطلوب (مثلاً عبر nano أو استلام ملف جديد مني)
nano app/src/main/assets/yemen_locations.json
git add .
git commit -m "تحديث قاعدة بيانات المديريات"
git push origin main
```
سيبني GitHub Actions نسخة جديدة تلقائياً خلال دقائق.

## ملاحظات تقنية
- إحداثيات الكعبة المستخدمة في الحسابات: 21.4225° شمالاً، 39.8262° شرقاً
- إذا رُفض إذن الموقع، ينتقل التطبيق تلقائياً لعرض خيار الاختيار اليدوي
- مهلة انتظار GPS الافتراضية: 7 ثوانٍ، بعدها ينتقل تلقائياً لعرض شاشة "تعذّر تحديد الموقع تلقائياً"
