# Ruta Universidad Católica de Oriente

Android NFC scavenger hunt for the Complejidad Algorítmica final workshop. Players visit five
campus checkpoints, starting and ending at M401. Correct answers keep them on the shortest route;
wrong answers add distance.

## Build

1. Open this folder in Android Studio and let Gradle sync.
2. In PowerShell, point the build at Android Studio's Java, because the build needs Java 17 or newer:
   `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"`
3. Run the routing tests: `.\gradlew test`
4. Build the APK: `.\gradlew assembleDebug`

The APK is created at `app/build/outputs/apk/debug/app-debug.apk`.

## Replace the placeholder content

| What | File |
|---|---|
| Walking distances, detour waypoint, clues, questions | `app/src/main/java/com/complejidad/rutam401/domain/CampusData.kt` |

## Branding

| What | File |
|---|---|
| App name, short and full | `app/src/main/res/values/strings.xml` |
| University colors | `app/src/main/java/com/complejidad/rutam401/ui/Theme.kt` |
| Logo | `app/src/main/res/drawable-nodpi/logo_universidad.png` |
| App icon | `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` |

After changing the distances, update the expected values in `dijkstraFindsPathsThroughOtherPlaces`
in `app/src/test/java/com/complejidad/rutam401/domain/RoutingTest.kt`.

## Register the NFC tags

Do this once on the phone used for the route, because assignments are saved on that phone.

1. On the start screen, tap **Configurar tags**.
2. Tap a checkpoint and hold the phone to its tag.
3. Repeat for all five, then tap **Listo**.

## Code layout

- `domain/`: Dijkstra, route selection and game rules, with no Android dependencies
- `data/`: saved progress and tag assignments
- `ui/`: Compose screens and theme
- `MainActivity.kt`: NFC reading
