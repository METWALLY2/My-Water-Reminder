===== FILE: settings.gradle.kts =====
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WaterReminder"
include(":app")


===== FILE: build.gradle.kts =====
plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}


===== FILE: gradle.properties =====
org.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official


===== FILE: .gitignore =====
*.iml
.gradle/
local.properties
.idea/
build/
captures/
.externalNativeBuild/
.cxx/
*.apk
*.aab
.DS_Store


===== FILE: .github/workflows/android.yml =====
name: Android CI

on:
  push:
    branches: [ main, master ]
  pull_request:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: 8.9

      # The wrapper JAR is a binary file that can't be pasted as text,
      # so the wrapper is generated here and then used via ./gradlew.
      - name: Generate Gradle wrapper
        run: gradle wrapper --gradle-version 8.9 --no-daemon

      - name: Build debug APK
        run: |
          chmod +x ./gradlew
          ./gradlew assembleDebug --stacktrace

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: water-reminder-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
          if-no-files-found: error


===== FILE: app/build.gradle.kts =====
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.waterreminder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.waterreminder"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Room
    val room = "2.6.1"
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}


===== FILE: app/proguard-rules.pro =====
# Project-specific ProGuard/R8 rules. Room, Compose and DataStore ship their own consumer rules.


===== FILE: app/src/main/AndroidManifest.xml =====
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:name=".WaterApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.WaterReminder">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <receiver
            android:name=".reminder.ReminderReceiver"
            android:exported="false" />

        <receiver
            android:name=".reminder.QuickAddReceiver"
            android:exported="false" />

        <receiver
            android:name=".reminder.BootReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
                <action android:name="android.intent.action.TIME_SET" />
                <action android:name="android.intent.action.TIMEZONE_CHANGED" />
            </intent-filter>
        </receiver>
    </application>
</manifest>


===== FILE: app/src/main/res/drawable/ic_launcher_foreground.xml =====
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M54,30 C54,30 38,50 38,62 C38,71 45,78 54,78 C63,78 70,71 70,62 C70,50 54,30 54,30 Z" />
</vector>


===== FILE: app/src/main/res/drawable/ic_notification.xml =====
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M12,2 C12,2 5,10 5,15 C5,18.9 8.1,22 12,22 C15.9,22 19,18.9 19,15 C19,10 12,2 12,2 Z" />
</vector>


===== FILE: app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml =====
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>


===== FILE: app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml =====
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>


===== FILE: app/src/main/res/values-night/themes.xml =====
<resources>
    <style name="Theme.WaterReminder" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">#FF131218</item>
    </style>
</resources>


===== FILE: app/src/main/res/values/colors.xml =====
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#2E83EB</color>
</resources>


===== FILE: app/src/main/res/values/strings.xml =====
<resources>
    <string name="app_name">Water Reminder</string>
</resources>


===== FILE: app/src/main/res/values/themes.xml =====
<resources>
    <style name="Theme.WaterReminder" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowBackground">#FFFDF8FF</item>
    </style>
</resources>


===== FILE: app/src/main/java/com/example/waterreminder/WaterApp.kt =====
package com.example.waterreminder

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.waterreminder.data.AppDatabase
import com.example.waterreminder.data.ReminderDao
import com.example.waterreminder.data.SettingsRepository
import com.example.waterreminder.data.WaterRepository
import com.example.waterreminder.reminder.NotificationHelper
import com.example.waterreminder.reminder.ReminderScheduler

/** Tiny manual DI container. Everything is lazy so cold start does no unnecessary work. */
class WaterApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val waterRepository: WaterRepository by lazy { WaterRepository(database.drinkDao()) }
    val reminderDao: ReminderDao by lazy { database.reminderDao() }
    val scheduler: ReminderScheduler by lazy { ReminderScheduler(this, settingsRepository, reminderDao) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}

fun CreationExtras.waterApp(): WaterApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WaterApp


===== FILE: app/src/main/java/com/example/waterreminder/MainActivity.kt =====
package com.example.waterreminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.waterreminder.ui.AppNav
import com.example.waterreminder.ui.theme.WaterTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { WaterTheme { AppNav() } }

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Keep alarms alive after app updates / force-stops. Off the main thread.
        val app = application as WaterApp
        lifecycleScope.launch(Dispatchers.Default) { app.scheduler.rescheduleAll() }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/data/Achievements.kt =====
package com.example.waterreminder.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Achievements(
    val currentStreak: Int,
    val bestStreak: Int,
    val goalDays: Int,
    val totalMl: Long
) {
    companion object {
        fun compute(entries: List<DrinkEntry>, target: Int, today: LocalDate, zone: ZoneId): Achievements {
            val byDay: Map<LocalDate, Int> = entries
                .groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
                .mapValues { (_, list) -> list.sumOf { it.amountMl } }

            val goalList = byDay.filterValues { it >= target }.keys.sorted()
            val goalSet = goalList.toHashSet()

            var best = 0
            var run = 0
            var prev: LocalDate? = null
            for (d in goalList) {
                run = if (prev != null && prev.plusDays(1) == d) run + 1 else 1
                if (run > best) best = run
                prev = d
            }

            var current = 0
            var cursor = if (today in goalSet) today else today.minusDays(1)
            while (cursor in goalSet) {
                current++
                cursor = cursor.minusDays(1)
            }

            return Achievements(
                currentStreak = current,
                bestStreak = best,
                goalDays = goalList.size,
                totalMl = entries.sumOf { it.amountMl.toLong() }
            )
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/data/AppDatabase.kt =====
package com.example.waterreminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DrinkEntry::class, CustomReminder::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        // Room opens the file lazily on the first query, so building this is cheap at cold start.
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "water.db"
            ).build().also { instance = it }
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/data/Daos.kt =====
package com.example.waterreminder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkDao {
    @Insert
    suspend fun insert(entry: DrinkEntry): Long

    @Delete
    suspend fun delete(entry: DrinkEntry)

    @Query("SELECT * FROM drink_entries WHERE timestamp >= :from AND timestamp < :to ORDER BY timestamp DESC")
    fun observeRange(from: Long, to: Long): Flow<List<DrinkEntry>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drink_entries WHERE timestamp >= :from AND timestamp < :to")
    suspend fun sumRange(from: Long, to: Long): Int

    @Query("SELECT * FROM drink_entries ORDER BY timestamp DESC")
    suspend fun getAll(): List<DrinkEntry>

    @Query(
        "SELECT drinkType, customName, amountMl, MAX(timestamp) AS lastUsed " +
            "FROM drink_entries GROUP BY drinkType, customName, amountMl " +
            "ORDER BY lastUsed DESC LIMIT :limit"
    )
    fun observeRecents(limit: Int): Flow<List<RecentDrink>>
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM custom_reminders ORDER BY hour, minute")
    fun observeAll(): Flow<List<CustomReminder>>

    @Query("SELECT * FROM custom_reminders")
    suspend fun getAll(): List<CustomReminder>

    @Insert
    suspend fun insert(reminder: CustomReminder): Long

    @Update
    suspend fun update(reminder: CustomReminder)

    @Delete
    suspend fun delete(reminder: CustomReminder)
}


===== FILE: app/src/main/java/com/example/waterreminder/data/Entities.kt =====
package com.example.waterreminder.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "drink_entries", indices = [Index("timestamp")])
data class DrinkEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drinkType: String,
    val customName: String? = null,
    val amountMl: Int,
    val timestamp: Long
)

val DrinkEntry.drink: DrinkType get() = DrinkType.fromName(drinkType)

fun DrinkEntry.displayLabel(): String = customName?.takeIf { it.isNotBlank() } ?: drink.label

/** Result row for the "Recents" quick-add chips. */
data class RecentDrink(
    val drinkType: String,
    val customName: String?,
    val amountMl: Int,
    val lastUsed: Long
)

/** daysMask: bit 0 = Monday ... bit 6 = Sunday (DayOfWeek.value - 1). */
@Entity(tableName = "custom_reminders")
data class CustomReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val daysMask: Int = 127,
    val enabled: Boolean = true
)

fun CustomReminder.hasDay(day: DayOfWeek): Boolean = (daysMask and (1 shl (day.value - 1))) != 0

fun CustomReminder.toggleDay(day: DayOfWeek): CustomReminder =
    copy(daysMask = daysMask xor (1 shl (day.value - 1)))


===== FILE: app/src/main/java/com/example/waterreminder/data/Models.kt =====
package com.example.waterreminder.data

import com.example.waterreminder.util.TargetCalculator
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale

enum class DrinkType(val label: String, val emoji: String) {
    WATER("Water", "💧"),
    COFFEE("Coffee", "☕"),
    TEA("Tea", "🍵"),
    JUICE("Juice", "🧃"),
    MILK("Milk", "🥛"),
    SODA("Soda", "🥤"),
    BEER("Beer", "🍺"),
    CUSTOM("Custom", "🍹");

    companion object {
        fun fromName(name: String): DrinkType = DrinkType.entries.firstOrNull { it.name == name } ?: WATER
        val selectable: List<DrinkType> get() = DrinkType.entries.filter { it != CUSTOM }
    }
}

enum class Gender { MALE, FEMALE }

enum class UnitSystem { METRIC, IMPERIAL }

enum class ReminderMode(val label: String) {
    SOUND_VIBRATE("Sound and vibrate"),
    SOUND("Sound only"),
    VIBRATE("Vibrate only"),
    SILENT("Silent")
}

data class UserSettings(
    val gender: Gender = Gender.MALE,
    val weightKg: Int = 65,
    val dailyTargetMl: Int = TargetCalculator.calculate(Gender.MALE, 65),
    val units: UnitSystem = UnitSystem.METRIC,
    val weekStart: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek,
    val autoReminders: Boolean = true,
    val wakeMinutes: Int = 7 * 60,
    val bedMinutes: Int = 23 * 60,
    val intervalMinutes: Int = 90,
    val reminderMode: ReminderMode = ReminderMode.SOUND_VIBRATE
)


===== FILE: app/src/main/java/com/example/waterreminder/data/SettingsRepository.kt =====
package com.example.waterreminder.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

private val GENDER = stringPreferencesKey("gender")
private val WEIGHT = intPreferencesKey("weight_kg")
private val TARGET = intPreferencesKey("daily_target_ml")
private val UNITS = stringPreferencesKey("units")
private val WEEK_START = intPreferencesKey("week_start")
private val AUTO = booleanPreferencesKey("auto_reminders")
private val WAKE = intPreferencesKey("wake_minutes")
private val BED = intPreferencesKey("bed_minutes")
private val INTERVAL = intPreferencesKey("interval_minutes")
private val MODE = stringPreferencesKey("reminder_mode")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsDataStore

    val settings: Flow<UserSettings> = store.data.map { it.toSettings() }

    suspend fun current(): UserSettings = settings.first()

    /** Atomic read-modify-write of the whole settings object. */
    suspend fun update(transform: (UserSettings) -> UserSettings) {
        store.edit { p ->
            val n = transform(p.toSettings())
            p[GENDER] = n.gender.name
            p[WEIGHT] = n.weightKg
            p[TARGET] = n.dailyTargetMl
            p[UNITS] = n.units.name
            p[WEEK_START] = n.weekStart.value
            p[AUTO] = n.autoReminders
            p[WAKE] = n.wakeMinutes
            p[BED] = n.bedMinutes
            p[INTERVAL] = n.intervalMinutes
            p[MODE] = n.reminderMode.name
        }
    }

    private fun Preferences.toSettings(): UserSettings {
        val d = UserSettings()
        return UserSettings(
            gender = this[GENDER]?.let { runCatching { Gender.valueOf(it) }.getOrNull() } ?: d.gender,
            weightKg = this[WEIGHT] ?: d.weightKg,
            dailyTargetMl = this[TARGET] ?: d.dailyTargetMl,
            units = this[UNITS]?.let { runCatching { UnitSystem.valueOf(it) }.getOrNull() } ?: d.units,
            weekStart = this[WEEK_START]?.let { runCatching { DayOfWeek.of(it) }.getOrNull() } ?: d.weekStart,
            autoReminders = this[AUTO] ?: d.autoReminders,
            wakeMinutes = this[WAKE] ?: d.wakeMinutes,
            bedMinutes = this[BED] ?: d.bedMinutes,
            intervalMinutes = this[INTERVAL] ?: d.intervalMinutes,
            reminderMode = this[MODE]?.let { runCatching { ReminderMode.valueOf(it) }.getOrNull() } ?: d.reminderMode
        )
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/data/WaterRepository.kt =====
package com.example.waterreminder.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class WaterRepository(private val dao: DrinkDao) {

    private fun startOf(day: LocalDate): Long =
        day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun entriesForDay(day: LocalDate): Flow<List<DrinkEntry>> =
        dao.observeRange(startOf(day), startOf(day.plusDays(1)))

    fun entriesBetween(start: LocalDate, endExclusive: LocalDate): Flow<List<DrinkEntry>> =
        dao.observeRange(startOf(start), startOf(endExclusive))

    fun recents(limit: Int): Flow<List<RecentDrink>> = dao.observeRecents(limit)

    suspend fun totalForDay(day: LocalDate): Int =
        dao.sumRange(startOf(day), startOf(day.plusDays(1)))

    suspend fun all(): List<DrinkEntry> = dao.getAll()

    suspend fun add(type: DrinkType, customName: String?, amountMl: Int, timestamp: Long) {
        dao.insert(
            DrinkEntry(
                drinkType = type.name,
                customName = customName?.trim()?.takeIf { it.isNotEmpty() },
                amountMl = amountMl,
                timestamp = timestamp
            )
        )
    }

    suspend fun delete(entry: DrinkEntry) = dao.delete(entry)
}


===== FILE: app/src/main/java/com/example/waterreminder/util/Calc.kt =====
package com.example.waterreminder.util

import com.example.waterreminder.data.Gender
import com.example.waterreminder.data.UnitSystem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object TargetCalculator {
    /** ~33 ml per kg for men, 30 ml per kg for women (65 kg male -> 2166 ml). */
    fun calculate(gender: Gender, weightKg: Int): Int {
        val perKg = if (gender == Gender.MALE) 100.0 / 3.0 else 30.0
        return (weightKg * perKg).toInt()
    }
}

/** All data is stored in ml / kg; these helpers only convert for display and input. */
object Units {
    private const val ML_PER_OZ = 29.5735
    private const val LB_PER_KG = 2.20462

    fun volumeUnit(u: UnitSystem): String = if (u == UnitSystem.METRIC) "ml" else "fl oz"
    fun weightUnit(u: UnitSystem): String = if (u == UnitSystem.METRIC) "kg" else "lb"

    fun volumeValue(ml: Int, u: UnitSystem): Int =
        if (u == UnitSystem.METRIC) ml else (ml / ML_PER_OZ).roundToInt()

    fun toMl(value: Int, u: UnitSystem): Int =
        if (u == UnitSystem.METRIC) value else (value * ML_PER_OZ).roundToInt()

    fun weightValue(kg: Int, u: UnitSystem): Int =
        if (u == UnitSystem.METRIC) kg else (kg * LB_PER_KG).roundToInt()

    fun toKg(value: Int, u: UnitSystem): Int =
        if (u == UnitSystem.METRIC) value else (value / LB_PER_KG).roundToInt()

    fun formatVolume(ml: Int, u: UnitSystem, grouped: Boolean = false): String {
        val v = volumeValue(ml, u)
        val number = if (grouped) String.format(Locale.US, "%,d", v) else v.toString()
        return "$number ${volumeUnit(u)}"
    }

    fun formatWeight(kg: Int, u: UnitSystem): String = "${weightValue(kg, u)} ${weightUnit(u)}"

    fun formatTotal(ml: Long, u: UnitSystem): String =
        if (u == UnitSystem.METRIC) String.format(Locale.US, "%.1f L", ml / 1000.0)
        else String.format(Locale.US, "%,d fl oz", (ml / ML_PER_OZ).roundToInt())
}

/** Locale-pinned formatters so digits and names stay consistent with the English UI. */
object Fmt {
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val dayFmt = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)
    private val shortFmt = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
    private val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

    fun clock(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(timeFmt)

    fun minutes(totalMinutes: Int): String =
        String.format(Locale.US, "%02d:%02d", (totalMinutes / 60) % 24, totalMinutes % 60)

    fun dayTitle(d: LocalDate): String = d.format(dayFmt)
    fun shortDate(d: LocalDate): String = d.format(shortFmt)
    fun monthTitle(d: LocalDate): String = d.format(monthFmt)
}


===== FILE: app/src/main/java/com/example/waterreminder/reminder/BootReceiver.kt =====
package com.example.waterreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.waterreminder.WaterApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Re-arms alarms after reboot, app update, clock change or time zone change. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as WaterApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                app.scheduler.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/reminder/NotificationHelper.kt =====
package com.example.waterreminder.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.waterreminder.MainActivity
import com.example.waterreminder.R
import com.example.waterreminder.data.ReminderMode

object NotificationHelper {
    private const val CH_SOUND_VIBRATE = "reminders_sound_vibrate"
    private const val CH_SOUND = "reminders_sound"
    private const val CH_VIBRATE = "reminders_vibrate"
    private const val CH_SILENT = "reminders_silent"
    private const val NOTIFICATION_ID = 2001

    const val ACTION_QUICK_ADD = "com.example.waterreminder.ACTION_QUICK_ADD"
    const val EXTRA_AMOUNT = "amount_ml"
    const val QUICK_ADD_ML = 200

    /**
     * Sound/vibration are fixed per channel once created, so each reminder mode gets its own
     * channel and [show] picks the right one.
     */
    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)

        fun channel(id: String, name: String, importance: Int, vibrate: Boolean, sound: Boolean) =
            NotificationChannel(id, name, importance).apply {
                description = "Drink water reminders"
                enableVibration(vibrate)
                if (!sound) setSound(null, null)
            }

        nm.createNotificationChannels(
            listOf(
                channel(CH_SOUND_VIBRATE, "Reminders (sound + vibrate)", NotificationManager.IMPORTANCE_DEFAULT, vibrate = true, sound = true),
                channel(CH_SOUND, "Reminders (sound only)", NotificationManager.IMPORTANCE_DEFAULT, vibrate = false, sound = true),
                channel(CH_VIBRATE, "Reminders (vibrate only)", NotificationManager.IMPORTANCE_DEFAULT, vibrate = true, sound = false),
                channel(CH_SILENT, "Reminders (silent)", NotificationManager.IMPORTANCE_LOW, vibrate = false, sound = false)
            )
        )
    }

    private fun channelFor(mode: ReminderMode): String = when (mode) {
        ReminderMode.SOUND_VIBRATE -> CH_SOUND_VIBRATE
        ReminderMode.SOUND -> CH_SOUND
        ReminderMode.VIBRATE -> CH_VIBRATE
        ReminderMode.SILENT -> CH_SILENT
    }

    fun show(context: Context, mode: ReminderMode, text: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            flags
        )
        val quickAdd = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, QuickAddReceiver::class.java)
                .setAction(ACTION_QUICK_ADD)
                .putExtra(EXTRA_AMOUNT, QUICK_ADD_ML),
            flags
        )

        val notification = NotificationCompat.Builder(context, channelFor(mode))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to drink water 💧")
            .setContentText(text)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(0, "Drank $QUICK_ADD_ML ml", quickAdd)
            .build()

        context.getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/reminder/QuickAddReceiver.kt =====
package com.example.waterreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.waterreminder.WaterApp
import com.example.waterreminder.data.DrinkType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles the "Drank 200 ml" button on the reminder notification. */
class QuickAddReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val amount = intent.getIntExtra(NotificationHelper.EXTRA_AMOUNT, NotificationHelper.QUICK_ADD_ML)
        val app = context.applicationContext as WaterApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.waterRepository.add(DrinkType.WATER, null, amount, System.currentTimeMillis())
                NotificationHelper.cancel(context)
            } finally {
                pending.finish()
            }
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/reminder/ReminderReceiver.kt =====
package com.example.waterreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.waterreminder.WaterApp
import com.example.waterreminder.util.Units
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as WaterApp
        val kind = intent.getStringExtra(EXTRA_KIND)
        val pending = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = app.settingsRepository.current()
                val total = app.waterRepository.totalForDay(LocalDate.now())
                val remaining = settings.dailyTargetMl - total

                // Auto reminders stay quiet once the daily goal is reached; custom ones always fire.
                val show = kind != KIND_AUTO || remaining > 0
                if (show) {
                    val text = when {
                        total == 0 -> "You haven't logged anything yet today. Time for a glass!"
                        remaining > 0 -> "${Units.formatVolume(remaining, settings.units)} left to reach today's goal."
                        else -> "Goal reached. Stay hydrated!"
                    }
                    NotificationHelper.show(context, settings.reminderMode, text)
                }
                app.scheduler.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_KIND = "kind"
        const val EXTRA_ID = "id"
        const val KIND_AUTO = "auto"
        const val KIND_CUSTOM = "custom"
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/reminder/ReminderScheduler.kt =====
package com.example.waterreminder.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.waterreminder.data.ReminderDao
import com.example.waterreminder.data.SettingsRepository
import java.time.ZonedDateTime

/**
 * Keeps exactly one pending alarm per reminder (one for the auto schedule, one per custom
 * reminder). When an alarm fires, [ReminderReceiver] shows the notification and calls
 * [rescheduleAll] to arm the next occurrence.
 */
class ReminderScheduler(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val reminderDao: ReminderDao
) {
    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()

    suspend fun rescheduleAll() {
        val settings = settingsRepository.current()
        val now = ZonedDateTime.now()

        val nextAuto = if (settings.autoReminders) ReminderTimes.nextAuto(settings, now) else null
        if (nextAuto != null) {
            schedule(AUTO_CODE, ReminderReceiver.KIND_AUTO, -1L, nextAuto)
        } else {
            cancel(AUTO_CODE)
        }

        for (r in reminderDao.getAll()) {
            val code = CUSTOM_BASE + r.id.toInt()
            val next = if (r.enabled) ReminderTimes.nextCustom(r, now) else null
            if (next != null) schedule(code, ReminderReceiver.KIND_CUSTOM, r.id, next) else cancel(code)
        }
    }

    fun cancelCustom(id: Long) = cancel(CUSTOM_BASE + id.toInt())

    private fun schedule(code: Int, kind: String, id: Long, at: ZonedDateTime) {
        val pi = pendingIntent(code, kind, id)
        val millis = at.toInstant().toEpochMilli()
        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        }
    }

    private fun cancel(code: Int) {
        val pi = pendingIntent(code, ReminderReceiver.KIND_CUSTOM, -1L)
        alarmManager.cancel(pi)
        pi.cancel()
    }

    private fun pendingIntent(code: Int, kind: String, id: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_KIND, kind)
            .putExtra(ReminderReceiver.EXTRA_ID, id)
        return PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val AUTO_CODE = 1
        const val CUSTOM_BASE = 1000
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/reminder/ReminderTimes.kt =====
package com.example.waterreminder.reminder

import com.example.waterreminder.data.CustomReminder
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.data.hasDay
import java.time.ZonedDateTime

/** Pure time math, kept separate from Android APIs so it is easy to reason about and test. */
object ReminderTimes {

    /**
     * Minutes since midnight for each auto reminder of one "schedule day".
     * If bedtime is earlier than wake-up (e.g. wake 12:30, bed 01:00) it belongs to the next
     * calendar day, so values may exceed 1440.
     */
    fun autoMinutes(s: UserSettings): List<Int> {
        val wake = s.wakeMinutes
        val bed = if (s.bedMinutes <= wake) s.bedMinutes + 1440 else s.bedMinutes
        val step = s.intervalMinutes.coerceAtLeast(15)
        val out = ArrayList<Int>()
        var t = wake
        while (t <= bed) {
            out.add(t)
            t += step
        }
        return out
    }

    fun nextAuto(s: UserSettings, now: ZonedDateTime): ZonedDateTime? {
        val minutes = autoMinutes(s)
        val today = now.toLocalDate()
        var best: ZonedDateTime? = null
        // Yesterday's schedule can spill past midnight, tomorrow's covers late evenings.
        for (offset in -1L..1L) {
            val base = today.plusDays(offset).atStartOfDay()
            for (m in minutes) {
                val candidate = base.plusMinutes(m.toLong()).atZone(now.zone)
                if (candidate.isAfter(now) && (best == null || candidate.isBefore(best))) {
                    best = candidate
                }
            }
        }
        return best
    }

    fun nextCustom(r: CustomReminder, now: ZonedDateTime): ZonedDateTime? {
        val today = now.toLocalDate()
        for (offset in 0L..7L) {
            val date = today.plusDays(offset)
            if (!r.hasDay(date.dayOfWeek)) continue
            val candidate = date.atTime(r.hour, r.minute).atZone(now.zone)
            if (candidate.isAfter(now)) return candidate
        }
        return null
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/theme/Theme.kt =====
package com.example.waterreminder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF005FB8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9ECAFF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondaryContainer = Color(0xFFC6E7FF),
    onSecondaryContainer = Color(0xFF001E2D),
    background = Color(0xFFFDF8FF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFDF8FF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF2E83EB),
    onPrimary = Color(0xFF00214F),
    primaryContainer = Color(0xFF7BB2E3),
    onPrimaryContainer = Color(0xFF00214F),
    secondaryContainer = Color(0xFF00566F),
    onSecondaryContainer = Color(0xFFC6E7FF),
    background = Color(0xFF131218),
    onBackground = Color(0xFFE4E1E9),
    surface = Color(0xFF131218),
    onSurface = Color(0xFFE4E1E9),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC6C6D0),
    outline = Color(0xFF90909A),
    outlineVariant = Color(0xFF45464F)
)

@Composable
fun WaterTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/components/Common.kt =====
package com.example.waterreminder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
fun ValueRow(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    value: String? = null,
    showChevron: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(20.dp))
        }
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        if (value != null) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        if (showChevron) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TextInputDialog(
    title: String,
    initial: String,
    keyboardType: KeyboardType,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun <T> ChoiceDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(selected = value == selected, onClick = { onSelect(value) })
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = (initialMinutes / 60) % 24,
        initialMinute = initialMinutes % 60,
        is24Hour = true
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = state)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text("OK") }
                }
            }
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/AppNav.kt =====
package com.example.waterreminder.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.waterreminder.ui.home.HomeScreen
import com.example.waterreminder.ui.reminders.RemindersScreen
import com.example.waterreminder.ui.settings.SettingsScreen
import com.example.waterreminder.ui.stats.StatsScreen

private object Routes {
    const val HOME = "home"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val REMINDERS = "reminders"
}

private data class NavTab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    NavTab(Routes.HOME, "Home", Icons.Filled.Home),
    NavTab(Routes.STATS, "Stats", Icons.Filled.BarChart),
    NavTab(Routes.SETTINGS, "Settings", Icons.Filled.Settings)
)

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    // route is null on the very first frame; show the bar so it doesn't pop in.
    val showBar = route == null || tabs.any { it.route == route }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route || (route == null && tab.route == Routes.HOME),
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(inner),
            enterTransition = { fadeIn(tween(150)) },
            exitTransition = { fadeOut(tween(150)) }
        ) {
            composable(Routes.HOME) { HomeScreen() }
            composable(Routes.STATS) { StatsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(onOpenReminders = { nav.navigate(Routes.REMINDERS) })
            }
            composable(Routes.REMINDERS) { RemindersScreen() }
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/home/AddDrinkSheet.kt =====
package com.example.waterreminder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.data.DrinkType
import com.example.waterreminder.data.UnitSystem
import com.example.waterreminder.ui.components.SectionLabel
import com.example.waterreminder.ui.components.TextInputDialog
import com.example.waterreminder.util.Units
import kotlinx.coroutines.launch

private val VOLUME_PRESETS_ML = listOf(100, 150, 200, 250, 300, 400, 500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDrinkSheet(
    units: UnitSystem,
    onDismiss: () -> Unit,
    onAdd: (type: DrinkType, customName: String?, ml: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var type by remember { mutableStateOf(DrinkType.WATER) }
    var customName by remember { mutableStateOf("") }
    var volumeMl by remember { mutableIntStateOf(200) }
    var isCustomVolume by remember { mutableStateOf(false) }
    var dialog by remember { mutableIntStateOf(0) } // 0 = none, 1 = beverage name, 2 = volume

    val canAdd = volumeMl > 0 && (type != DrinkType.CUSTOM || customName.isNotBlank())

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Add drink",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            SectionLabel("Beverage")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Tile(
                        label = if (customName.isBlank()) "Add custom" else customName,
                        selected = type == DrinkType.CUSTOM,
                        onClick = { dialog = 1 }
                    ) { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(32.dp)) }
                }
                items(DrinkType.selectable) { drink ->
                    Tile(
                        label = drink.label,
                        selected = type == drink,
                        onClick = { type = drink }
                    ) { Text(drink.emoji, fontSize = 28.sp) }
                }
            }

            SectionLabel("Volume")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Tile(
                        label = if (isCustomVolume) Units.formatVolume(volumeMl, units) else "Add custom",
                        selected = isCustomVolume,
                        onClick = { dialog = 2 }
                    ) { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(32.dp)) }
                }
                items(VOLUME_PRESETS_ML) { ml ->
                    Tile(
                        label = Units.formatVolume(ml, units),
                        selected = !isCustomVolume && volumeMl == ml,
                        onClick = {
                            isCustomVolume = false
                            volumeMl = ml
                        }
                    ) { Icon(Icons.Filled.LocalDrink, contentDescription = null, modifier = Modifier.size(32.dp)) }
                }
            }

            Button(
                onClick = {
                    onAdd(type, if (type == DrinkType.CUSTOM) customName else null, volumeMl)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onDismiss()
                    }
                },
                enabled = canAdd,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                    .height(56.dp)
            ) {
                Text("ADD", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.LocalDrink, contentDescription = null)
            }
        }
    }

    when (dialog) {
        1 -> TextInputDialog(
            title = "Beverage name",
            initial = customName,
            keyboardType = KeyboardType.Text,
            onConfirm = { text ->
                if (text.isNotBlank()) {
                    customName = text.trim()
                    type = DrinkType.CUSTOM
                }
                dialog = 0
            },
            onDismiss = { dialog = 0 }
        )
        2 -> TextInputDialog(
            title = "Volume (${Units.volumeUnit(units)})",
            initial = if (isCustomVolume) Units.volumeValue(volumeMl, units).toString() else "",
            keyboardType = KeyboardType.Number,
            onConfirm = { text ->
                text.toIntOrNull()?.takeIf { it > 0 }?.let { value ->
                    volumeMl = Units.toMl(value, units).coerceIn(10, 5000)
                    isCustomVolume = true
                }
                dialog = 0
            },
            onDismiss = { dialog = 0 }
        )
    }
}

@Composable
private fun Tile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val container = if (selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
    val content = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .width(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompositionLocalProvider(LocalContentColor provides content) {
            icon()
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/home/HomeScreen.kt =====
package com.example.waterreminder.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterreminder.data.Achievements
import com.example.waterreminder.data.DrinkEntry
import com.example.waterreminder.data.DrinkType
import com.example.waterreminder.data.UnitSystem
import com.example.waterreminder.data.displayLabel
import com.example.waterreminder.data.drink
import com.example.waterreminder.ui.components.SectionLabel
import com.example.waterreminder.util.Fmt
import com.example.waterreminder.util.Units
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var achievements by remember { mutableStateOf<Achievements?>(null) }

    val settings = state.settings
    val units = settings.units
    val progress =
        if (settings.dailyTargetMl > 0) state.totalMl.toFloat() / settings.dailyTargetMl else 0f

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Title + achievements
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.size(48.dp))
                    Text(
                        text = if (state.isToday) "Today" else Fmt.dayTitle(state.day),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = { scope.launch { achievements = vm.achievements() } }) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = "Achievements",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Progress circle with day navigation
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = vm::previousDay) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous day")
                    }
                    WaterCircle(
                        progress = progress,
                        valueText = Units.volumeValue(state.totalMl, units).toString(),
                        targetText = "of ${Units.formatVolume(settings.dailyTargetMl, units)}",
                        modifier = Modifier.weight(1f)
                    )
                    if (state.isToday) {
                        Spacer(Modifier.size(48.dp))
                    } else {
                        IconButton(onClick = vm::nextDay) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next day")
                        }
                    }
                }
            }

            // Recents quick-add chips
            item { SectionLabel("Recents") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recents) { r ->
                        OutlinedButton(
                            onClick = { vm.add(DrinkType.fromName(r.drinkType), r.customName, r.amountMl) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text("+ ${Units.volumeValue(r.amountMl, units)}")
                            Spacer(Modifier.width(8.dp))
                            Text(DrinkType.fromName(r.drinkType).emoji)
                        }
                    }
                }
            }

            // Activity timeline
            item { SectionLabel("Activity") }
            if (state.entries.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No activity for ${if (state.isToday) "Today" else "this day"}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add a record",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(state.entries, key = { it.id }) { entry ->
                    ActivityRow(entry, units, onDelete = { vm.delete(entry) })
                }
            }
        }

        FloatingActionButton(
            onClick = { showSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add drink")
        }
    }

    if (showSheet) {
        AddDrinkSheet(
            units = units,
            onDismiss = { showSheet = false },
            onAdd = { type, name, ml -> vm.add(type, name, ml) }
        )
    }

    achievements?.let { a ->
        AlertDialog(
            onDismissRequest = { achievements = null },
            title = { Text("Achievements") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔥 Current streak: ${a.currentStreak} days")
                    Text("🏆 Best streak: ${a.bestStreak} days")
                    Text("🎯 Goal reached: ${a.goalDays} days")
                    Text("💧 Total logged: ${Units.formatTotal(a.totalMl, units)}")
                }
            },
            confirmButton = { TextButton(onClick = { achievements = null }) { Text("OK") } }
        )
    }
}

@Composable
private fun ActivityRow(entry: DrinkEntry, units: UnitSystem, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(entry.drink.emoji, fontSize = 28.sp)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "${Units.formatVolume(entry.amountMl, units)} of ${entry.displayLabel()}",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = Fmt.clock(entry.timestamp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/home/HomeViewModel.kt =====
package com.example.waterreminder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.waterreminder.WaterApp
import com.example.waterreminder.data.Achievements
import com.example.waterreminder.data.DrinkEntry
import com.example.waterreminder.data.DrinkType
import com.example.waterreminder.data.RecentDrink
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.waterApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class HomeUiState(
    val day: LocalDate = LocalDate.now(),
    val entries: List<DrinkEntry> = emptyList(),
    val totalMl: Int = 0,
    val settings: UserSettings = UserSettings(),
    val recents: List<RecentDrink> = emptyList()
) {
    val isToday: Boolean get() = day == LocalDate.now()
}

class HomeViewModel(private val app: WaterApp) : ViewModel() {

    private val repo = app.waterRepository
    private val day = MutableStateFlow(LocalDate.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HomeUiState> = day
        .flatMapLatest { d ->
            combine(
                repo.entriesForDay(d),
                app.settingsRepository.settings,
                repo.recents(6)
            ) { entries, settings, recents ->
                HomeUiState(
                    day = d,
                    entries = entries,
                    totalMl = entries.sumOf { it.amountMl },
                    settings = settings,
                    recents = withDefaults(recents)
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun previousDay() {
        day.value = day.value.minusDays(1)
    }

    fun nextDay() {
        if (day.value.isBefore(LocalDate.now())) day.value = day.value.plusDays(1)
    }

    /** Adds a drink to the selected day (current time of day on that date). */
    fun add(type: DrinkType, customName: String?, ml: Int) {
        val timestamp = LocalDateTime.of(day.value, LocalTime.now())
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModelScope.launch { repo.add(type, customName, ml, timestamp) }
    }

    fun delete(entry: DrinkEntry) {
        viewModelScope.launch { repo.delete(entry) }
    }

    suspend fun achievements(): Achievements = withContext(Dispatchers.Default) {
        Achievements.compute(
            entries = repo.all(),
            target = state.value.settings.dailyTargetMl,
            today = LocalDate.now(),
            zone = ZoneId.systemDefault()
        )
    }

    private fun withDefaults(recents: List<RecentDrink>): List<RecentDrink> {
        val defaults = listOf(
            RecentDrink(DrinkType.WATER.name, null, 200, 0L),
            RecentDrink(DrinkType.COFFEE.name, null, 100, 0L)
        )
        return (recents + defaults)
            .distinctBy { Triple(it.drinkType, it.customName, it.amountMl) }
            .take(4)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(waterApp()) }
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/home/WaterCircle.kt =====
package com.example.waterreminder.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Progress ring + a "water level" that rises inside the inner circle. */
@Composable
fun WaterCircle(
    progress: Float,
    valueText: String,
    targetText: String,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "waterProgress"
    )
    val ring = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val water = MaterialTheme.colorScheme.primaryContainer

    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.085f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)

            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )
            if (animated > 0f) {
                drawArc(
                    color = ring,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            val r = size.minDimension / 2f - stroke
            val circle = Path().apply { addOval(Rect(center, r)) }
            clipPath(circle) {
                val top = center.y + r - 2f * r * animated
                drawRect(
                    color = water,
                    topLeft = Offset(center.x - r, top),
                    size = Size(2f * r, center.y + r - top + 1f)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = valueText,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = ring
            )
            Text(
                text = targetText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/stats/StatsScreen.kt =====
package com.example.waterreminder.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterreminder.util.Fmt
import com.example.waterreminder.util.Units
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(vm: StatsViewModel = viewModel(factory = StatsViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    val s = state
    val weekly = s?.mode != StatsMode.MONTHLY

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Stats",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        )
        TabRow(selectedTabIndex = if (weekly) 0 else 1) {
            Tab(selected = weekly, onClick = { vm.setMode(StatsMode.WEEKLY) }, text = { Text("Weekly") })
            Tab(selected = !weekly, onClick = { vm.setMode(StatsMode.MONTHLY) }, text = { Text("Monthly") })
        }
        if (s != null) {
            StatsContent(s, onPrevious = vm::previous, onNext = vm::next)
        }
    }
}

@Composable
private fun StatsContent(s: StatsUiState, onPrevious: () -> Unit, onNext: () -> Unit) {
    val weekly = s.mode == StatsMode.WEEKLY
    val rangeLabel = if (weekly) {
        "${Fmt.shortDate(s.start)} - ${Fmt.shortDate(s.endInclusive)}"
    } else {
        Fmt.monthTitle(s.start)
    }

    Column(Modifier.padding(16.dp)) {
        Text(
            text = Units.formatVolume(s.totalMl, s.units, grouped = true),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "of ${Units.formatVolume(s.targetMl, s.units, grouped = true)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
            }
            Text(rangeLabel, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onNext, enabled = s.canGoNext) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
            }
        }

        Spacer(Modifier.height(16.dp))
        BarChart(days = s.days, dailyTarget = s.dailyTargetMl, weekly = weekly)
    }
}

/** Bars show each day as a percentage of the daily target (capped at 100%). */
@Composable
private fun BarChart(days: List<DayStat>, dailyTarget: Int, weekly: Boolean) {
    val barColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gap = if (weekly) 8.dp else 2.dp
    val labelFont = if (weekly) 12.sp else 9.sp

    Row(Modifier.fillMaxWidth().height(280.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                Canvas(Modifier.fillMaxSize()) {
                    for (i in 0..5) {
                        val y = size.height * (1f - i / 5f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEach { d ->
                        val fraction =
                            if (dailyTarget > 0) (d.ml.toFloat() / dailyTarget).coerceIn(0f, 1f) else 0f
                        if (fraction > 0f) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(barColor)
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                days.forEach { d ->
                    val label = when {
                        weekly -> d.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        d.date.dayOfMonth == 1 || d.date.dayOfMonth % 5 == 0 -> d.date.dayOfMonth.toString()
                        else -> ""
                    }
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = labelFont,
                        maxLines = 1,
                        softWrap = false,
                        color = labelColor
                    )
                }
            }
        }

        // Y axis labels (percent of daily target)
        Column(Modifier.fillMaxHeight().padding(start = 6.dp).width(40.dp)) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("100%", "80%", "60%", "40%", "20%", "0").forEach {
                    Text(it, fontSize = 10.sp, color = labelColor)
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/stats/StatsViewModel.kt =====
package com.example.waterreminder.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.waterreminder.WaterApp
import com.example.waterreminder.data.DrinkEntry
import com.example.waterreminder.data.UnitSystem
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.waterApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class StatsMode { WEEKLY, MONTHLY }

data class DayStat(val date: LocalDate, val ml: Int)

data class StatsUiState(
    val mode: StatsMode,
    val start: LocalDate,
    val endInclusive: LocalDate,
    val days: List<DayStat>,
    val totalMl: Int,
    val targetMl: Int,
    val dailyTargetMl: Int,
    val units: UnitSystem,
    val canGoNext: Boolean
)

class StatsViewModel(private val app: WaterApp) : ViewModel() {

    private val mode = MutableStateFlow(StatsMode.WEEKLY)
    private val offset = MutableStateFlow(0) // 0 = current period, -1 = previous, ...

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<StatsUiState?> =
        combine(mode, offset, app.settingsRepository.settings) { m, o, s -> Triple(m, o, s) }
            .flatMapLatest { (m, o, s) ->
                val (start, endExclusive) = period(m, o, s.weekStart, LocalDate.now())
                app.waterRepository.entriesBetween(start, endExclusive)
                    .map { entries -> build(m, o, s, start, endExclusive, entries) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setMode(m: StatsMode) {
        mode.value = m
        offset.value = 0
    }

    fun previous() {
        offset.value -= 1
    }

    fun next() {
        if (offset.value < 0) offset.value += 1
    }

    private fun period(
        m: StatsMode,
        offset: Int,
        weekStart: DayOfWeek,
        today: LocalDate
    ): Pair<LocalDate, LocalDate> =
        if (m == StatsMode.WEEKLY) {
            val start = today.with(TemporalAdjusters.previousOrSame(weekStart)).plusWeeks(offset.toLong())
            start to start.plusDays(7)
        } else {
            val start = today.withDayOfMonth(1).plusMonths(offset.toLong())
            start to start.plusMonths(1)
        }

    private fun build(
        m: StatsMode,
        o: Int,
        s: UserSettings,
        start: LocalDate,
        endExclusive: LocalDate,
        entries: List<DrinkEntry>
    ): StatsUiState {
        val zone = ZoneId.systemDefault()
        val byDay = entries
            .groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
            .mapValues { (_, list) -> list.sumOf { it.amountMl } }

        val days = generateSequence(start) { it.plusDays(1) }
            .takeWhile { it.isBefore(endExclusive) }
            .map { DayStat(it, byDay[it] ?: 0) }
            .toList()

        return StatsUiState(
            mode = m,
            start = start,
            endInclusive = endExclusive.minusDays(1),
            days = days,
            totalMl = days.sumOf { it.ml },
            targetMl = s.dailyTargetMl * days.size,
            dailyTargetMl = s.dailyTargetMl,
            units = s.units,
            canGoNext = o < 0
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { StatsViewModel(waterApp()) }
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/settings/SettingsScreen.kt =====
package com.example.waterreminder.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterreminder.data.Gender
import com.example.waterreminder.data.UnitSystem
import com.example.waterreminder.ui.components.ChoiceDialog
import com.example.waterreminder.ui.components.TextInputDialog
import com.example.waterreminder.ui.components.ValueRow
import com.example.waterreminder.util.Units
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private enum class SettingsDialog { NONE, TARGET, GENDER, WEIGHT, UNITS, WEEK }

@Composable
fun SettingsScreen(
    onOpenReminders: () -> Unit,
    vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val s by vm.settings.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf(SettingsDialog.NONE) }
    val close = { dialog = SettingsDialog.NONE }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        )

        ValueRow(
            title = "Reminders",
            icon = Icons.Filled.Notifications,
            showChevron = true,
            onClick = onOpenReminders
        )
        ValueRow(
            title = "Daily target",
            icon = Icons.Filled.GpsFixed,
            value = Units.formatVolume(s.dailyTargetMl, s.units),
            onClick = { dialog = SettingsDialog.TARGET }
        )
        ValueRow(
            title = "Gender",
            icon = Icons.Filled.Wc,
            value = if (s.gender == Gender.MALE) "Male" else "Female",
            onClick = { dialog = SettingsDialog.GENDER }
        )
        ValueRow(
            title = "Weight",
            icon = Icons.Filled.MonitorWeight,
            value = Units.formatWeight(s.weightKg, s.units),
            onClick = { dialog = SettingsDialog.WEIGHT }
        )
        ValueRow(
            title = "Units",
            icon = Icons.Filled.Straighten,
            value = if (s.units == UnitSystem.METRIC) "kg, ml" else "lb, fl oz",
            onClick = { dialog = SettingsDialog.UNITS }
        )
        ValueRow(
            title = "Start week on",
            icon = Icons.Filled.CalendarViewWeek,
            value = s.weekStart.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
            onClick = { dialog = SettingsDialog.WEEK }
        )

        Text(
            text = "Your daily target is calculated from your gender and weight. You can also set it manually.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
    }

    when (dialog) {
        SettingsDialog.NONE -> Unit

        SettingsDialog.TARGET -> TextInputDialog(
            title = "Daily target (${Units.volumeUnit(s.units)})",
            initial = Units.volumeValue(s.dailyTargetMl, s.units).toString(),
            keyboardType = KeyboardType.Number,
            onConfirm = { text ->
                text.toIntOrNull()?.let { vm.setDailyTarget(Units.toMl(it, s.units).coerceIn(500, 8000)) }
                close()
            },
            onDismiss = close
        )

        SettingsDialog.GENDER -> ChoiceDialog(
            title = "Gender",
            options = listOf(Gender.MALE to "Male", Gender.FEMALE to "Female"),
            selected = s.gender,
            onSelect = { vm.setGender(it); close() },
            onDismiss = close
        )

        SettingsDialog.WEIGHT -> TextInputDialog(
            title = "Weight (${Units.weightUnit(s.units)})",
            initial = Units.weightValue(s.weightKg, s.units).toString(),
            keyboardType = KeyboardType.Number,
            onConfirm = { text ->
                text.toIntOrNull()?.let { vm.setWeightKg(Units.toKg(it, s.units).coerceIn(20, 300)) }
                close()
            },
            onDismiss = close
        )

        SettingsDialog.UNITS -> ChoiceDialog(
            title = "Units",
            options = listOf(UnitSystem.METRIC to "kg, ml", UnitSystem.IMPERIAL to "lb, fl oz"),
            selected = s.units,
            onSelect = { vm.setUnits(it); close() },
            onDismiss = close
        )

        SettingsDialog.WEEK -> ChoiceDialog(
            title = "Start week on",
            options = (1..7).map {
                DayOfWeek.of(it) to DayOfWeek.of(it).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            },
            selected = s.weekStart,
            onSelect = { vm.setWeekStart(it); close() },
            onDismiss = close
        )
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/settings/SettingsViewModel.kt =====
package com.example.waterreminder.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.waterreminder.WaterApp
import com.example.waterreminder.data.Gender
import com.example.waterreminder.data.UnitSystem
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.util.TargetCalculator
import com.example.waterreminder.waterApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class SettingsViewModel(private val app: WaterApp) : ViewModel() {

    val settings: StateFlow<UserSettings> = app.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    private fun update(block: (UserSettings) -> UserSettings) {
        viewModelScope.launch { app.settingsRepository.update(block) }
    }

    // Changing gender or weight recalculates the daily target; the target can also be set by hand.
    fun setGender(g: Gender) = update {
        it.copy(gender = g, dailyTargetMl = TargetCalculator.calculate(g, it.weightKg))
    }

    fun setWeightKg(kg: Int) = update {
        it.copy(weightKg = kg, dailyTargetMl = TargetCalculator.calculate(it.gender, kg))
    }

    fun setDailyTarget(ml: Int) = update { it.copy(dailyTargetMl = ml) }

    fun setUnits(u: UnitSystem) = update { it.copy(units = u) }

    fun setWeekStart(d: DayOfWeek) = update { it.copy(weekStart = d) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(waterApp()) }
        }
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/reminders/RemindersScreen.kt =====
package com.example.waterreminder.ui.reminders

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterreminder.data.CustomReminder
import com.example.waterreminder.data.ReminderMode
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.data.hasDay
import com.example.waterreminder.data.toggleDay
import com.example.waterreminder.reminder.ReminderTimes
import com.example.waterreminder.ui.components.ChoiceDialog
import com.example.waterreminder.ui.components.TimePickerDialog
import com.example.waterreminder.ui.components.ValueRow
import com.example.waterreminder.util.Fmt
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun RemindersScreen(vm: RemindersViewModel = viewModel(factory = RemindersViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current

    // Re-check when returning from the system "Alarms & reminders" screen.
    var canExact by remember { mutableStateOf(vm.canScheduleExact()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { canExact = vm.canScheduleExact() }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Reminders") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Settings") })
        }

        if (!canExact) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Allow \"Alarms & reminders\" so reminders arrive exactly on time.",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = {
                        if (Build.VERSION.SDK_INT >= 31) {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        }
                    }) { Text("Allow") }
                }
            }
        }

        val s = state
        Box(Modifier.weight(1f)) {
            if (s != null) {
                if (tab == 0) CustomTab(s, vm) else AutoTab(s.settings, vm)
            }
        }
    }
}

// ---------- Custom reminders ----------

@Composable
private fun CustomTab(state: RemindersUiState, vm: RemindersViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CustomReminder?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Custom",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            if (state.reminders.isEmpty()) {
                item {
                    Text(
                        text = "No custom reminders yet. Tap + to add one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(state.reminders, key = { it.id }) { r ->
                ReminderCard(
                    reminder = r,
                    weekStart = state.settings.weekStart,
                    onToggle = { vm.updateReminder(r.copy(enabled = it)) },
                    onDayClick = { vm.updateReminder(r.toggleDay(it)) },
                    onEdit = { editing = r },
                    onDelete = { vm.deleteReminder(r) }
                )
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add reminder")
        }
    }

    if (showAdd) {
        TimePickerDialog(
            initialMinutes = 9 * 60,
            onConfirm = { vm.addReminder(it); showAdd = false },
            onDismiss = { showAdd = false }
        )
    }
    editing?.let { r ->
        TimePickerDialog(
            initialMinutes = r.hour * 60 + r.minute,
            onConfirm = { m ->
                vm.updateReminder(r.copy(hour = m / 60, minute = m % 60))
                editing = null
            },
            onDismiss = { editing = null }
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: CustomReminder,
    weekStart: DayOfWeek,
    onToggle: (Boolean) -> Unit,
    onDayClick: (DayOfWeek) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Fmt.minutes(reminder.hour * 60 + reminder.minute),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onEdit)
                )
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit time") },
                            onClick = { menuOpen = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { menuOpen = false; onDelete() }
                        )
                    }
                }
                Switch(checked = reminder.enabled, onCheckedChange = onToggle)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (0..6).map { weekStart.plus(it.toLong()) }.forEach { day ->
                    DayChip(
                        label = day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                        selected = reminder.hasDay(day),
                        onClick = { onDayClick(day) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 12.sp, color = fg, maxLines = 1)
    }
}

// ---------- Auto reminders ----------

private enum class AutoDialog { NONE, WAKE, BED, INTERVAL, MODE }

@Composable
private fun AutoTab(settings: UserSettings, vm: RemindersViewModel) {
    var dialog by remember { mutableStateOf(AutoDialog.NONE) }
    val close = { dialog = AutoDialog.NONE }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (settings.autoReminders) "Auto reminders enabled" else "Auto reminders disabled",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = settings.autoReminders,
                    onCheckedChange = { on -> vm.updateSettings { it.copy(autoReminders = on) } }
                )
            }
        }

        ValueRow(
            title = "Wake up time",
            icon = Icons.Filled.Alarm,
            value = Fmt.minutes(settings.wakeMinutes),
            onClick = { dialog = AutoDialog.WAKE }
        )
        ValueRow(
            title = "Bedtime",
            icon = Icons.Filled.Bedtime,
            value = Fmt.minutes(settings.bedMinutes),
            onClick = { dialog = AutoDialog.BED }
        )
        ValueRow(
            title = "Reminder interval",
            value = "${settings.intervalMinutes} min",
            onClick = { dialog = AutoDialog.INTERVAL }
        )
        ValueRow(
            title = "Reminder mode",
            value = settings.reminderMode.label,
            onClick = { dialog = AutoDialog.MODE }
        )

        if (settings.autoReminders) {
            Text(
                text = "Daily schedule: " +
                    ReminderTimes.autoMinutes(settings).joinToString("  ·  ") { Fmt.minutes(it) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }

    when (dialog) {
        AutoDialog.NONE -> Unit

        AutoDialog.WAKE -> TimePickerDialog(
            initialMinutes = settings.wakeMinutes,
            onConfirm = { m -> vm.updateSettings { it.copy(wakeMinutes = m) }; close() },
            onDismiss = close
        )

        AutoDialog.BED -> TimePickerDialog(
            initialMinutes = settings.bedMinutes,
            onConfirm = { m -> vm.updateSettings { it.copy(bedMinutes = m) }; close() },
            onDismiss = close
        )

        AutoDialog.INTERVAL -> ChoiceDialog(
            title = "Reminder interval",
            options = listOf(30, 45, 60, 90, 120, 150, 180).map { it to "$it min" },
            selected = settings.intervalMinutes,
            onSelect = { v -> vm.updateSettings { it.copy(intervalMinutes = v) }; close() },
            onDismiss = close
        )

        AutoDialog.MODE -> ChoiceDialog(
            title = "Reminder mode",
            options = ReminderMode.entries.map { it to it.label },
            selected = settings.reminderMode,
            onSelect = { v -> vm.updateSettings { it.copy(reminderMode = v) }; close() },
            onDismiss = close
        )
    }
}


===== FILE: app/src/main/java/com/example/waterreminder/ui/reminders/RemindersViewModel.kt =====
package com.example.waterreminder.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.waterreminder.WaterApp
import com.example.waterreminder.data.CustomReminder
import com.example.waterreminder.data.UserSettings
import com.example.waterreminder.waterApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RemindersUiState(
    val reminders: List<CustomReminder>,
    val settings: UserSettings
)

class RemindersViewModel(private val app: WaterApp) : ViewModel() {

    val state: StateFlow<RemindersUiState?> =
        combine(app.reminderDao.observeAll(), app.settingsRepository.settings) { list, s ->
            RemindersUiState(list, s)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun canScheduleExact(): Boolean = app.scheduler.canScheduleExact()

    fun addReminder(minutesOfDay: Int) {
        viewModelScope.launch {
            app.reminderDao.insert(CustomReminder(hour = minutesOfDay / 60, minute = minutesOfDay % 60))
            app.scheduler.rescheduleAll()
        }
    }

    fun updateReminder(reminder: CustomReminder) {
        viewModelScope.launch {
            app.reminderDao.update(reminder)
            app.scheduler.rescheduleAll()
        }
    }

    fun deleteReminder(reminder: CustomReminder) {
        viewModelScope.launch {
            app.scheduler.cancelCustom(reminder.id)
            app.reminderDao.delete(reminder)
        }
    }

    /** Any change to auto-reminder settings re-arms the alarms. */
    fun updateSettings(block: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            app.settingsRepository.update(block)
            app.scheduler.rescheduleAll()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { RemindersViewModel(waterApp()) }
        }
    }
}
