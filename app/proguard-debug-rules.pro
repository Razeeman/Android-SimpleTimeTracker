-dontobfuscate

-keep class * implements dagger.MembersInjector { *; }
-keepclassmembers class com.example.util.simpletimetracker.feature_records.view.RecordsContainerFragment$Companion { void setViewPagerSmoothScroll(boolean); }
-keepclassmembers class com.example.util.simpletimetracker.feature_statistics.view.StatisticsContainerFragment$Companion { void setViewPagerSmoothScroll(boolean); }
-keepclassmembers class com.example.util.simpletimetracker.core.mapper.ColorMapper$Companion { synthetic java.util.List getAvailableColors$default(com.example.util.simpletimetracker.core.mapper.ColorMapper$Companion,boolean,int,java.lang.Object); }
-keep class com.example.util.simpletimetracker.core.utils.TestUtils { *; }
-keep class kotlin.collections.CollectionsKt { *; }
-keep class androidx.test.espresso.IdlingRegistry { *; }
-keep class androidx.test.espresso.IdlingResource { *; }

