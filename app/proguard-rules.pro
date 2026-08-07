# قواعد ProGuard - التصغير معطّل حالياً في نسخة الإصدار (release) لتفادي مشاكل التوافق
# يمكن تفعيل isMinifyEnabled = true في app/build.gradle.kts لاحقاً مع اختبار شامل

-keep class com.aseelan.qiblayemen.data.local.** { *; }
-keep class com.aseelan.qiblayemen.data.model.** { *; }
