# Grama-Yatri

Grama-Yatri is a community-powered rural bus tracking Android app built with Kotlin, XML layouts, Material Design, MVVM, RecyclerView, Firebase Authentication, Firebase Realtime Database, and Firebase Cloud Messaging hooks.

## Project Folder Structure

```text
Grama-Yatri/
  settings.gradle.kts
  build.gradle.kts
  app/
    build.gradle.kts
    google-services.json.example
    src/main/
      AndroidManifest.xml
      java/com/gramayatri/app/
        GramaYatriApp.kt
        data/model/
        data/repository/
        notifications/
        ui/auth/
        ui/home/
        ui/routes/
        ui/tracking/
        ui/notifications/
        ui/admin/
      res/layout/
      res/values/
      res/values-night/
```

## Firebase Realtime Database Structure

```json
{
  "users": {
    "uid": {
      "uid": "uid",
      "name": "Reporter name",
      "email": "user@example.com",
      "admin": false
    }
  },
  "routes": {
    "routeId": {
      "id": "routeId",
      "name": "Village A - Market",
      "villageFrom": "Village A",
      "villageTo": "Market",
      "cancelled": false,
      "updatedAt": 1720000000000,
      "stops": [
        { "name": "Village A", "averageMinutesFromPrevious": 0 },
        { "name": "School Stop", "averageMinutesFromPrevious": 8 },
        { "name": "Market", "averageMinutesFromPrevious": 12 }
      ]
    }
  },
  "bus_pings": {
    "routeId": {
      "pingId": {
        "id": "pingId",
        "routeId": "routeId",
        "stopIndex": 1,
        "stopName": "School Stop",
        "type": "BUS_PASSED",
        "reporterId": "uid",
        "reporterName": "Ravi",
        "createdAt": 1720000000000
      }
    }
  },
  "eta_updates": {
    "routeId": {
      "routeId": "routeId",
      "nextStopName": "Market",
      "etaMinutes": 12,
      "sourcePingId": "pingId",
      "updatedAt": 1720000000000
    }
  },
  "notifications": {
    "notificationId": {
      "id": "notificationId",
      "routeId": "routeId",
      "routeName": "Village A - Market",
      "title": "Bus cancelled",
      "message": "Village A - Market is reported cancelled today.",
      "type": "CANCELLED",
      "createdAt": 1720000000000
    }
  }
}
```

## Step-by-Step Setup

1. Open this folder in Android Studio.
2. Create a Firebase project at <https://console.firebase.google.com/>.
3. Add an Android app with package name `com.gramayatri.app`.
4. Download `google-services.json`.
5. Put it at `app/google-services.json`.
6. Enable Firebase Authentication, then enable Email/Password sign-in.
7. Create a Realtime Database and choose a region close to your users.
8. Start in test mode for development, then replace rules before release.
9. Sync Gradle in Android Studio.
10. Run the app on an emulator or Android phone.

## Suggested Development Database Rules

Use only for local testing:

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

For production, restrict route administration to users with `users/$uid/admin == true`.

## How to Use

1. Register a user.
2. Open Admin Panel and create a route.
3. Open Select Route.
4. Choose a stop and press `Bus Passed` or `I am on the Bus`.
5. Other users on the same route see live ping history and ETA updates.
6. Press `Report Cancellation` to update the route and create an alert.

## Push Notification Note

The app includes an FCM service and local notification display. A real remote push to all passengers after cancellation needs a trusted server or Firebase Cloud Function that listens to `/notifications` and sends an FCM message to subscribed devices. The client already stores cancellation alerts in Firebase and can display local alerts.

## Optional Google Maps

Google Maps is intentionally not enabled because the requirement marks it optional and rural low-data usage is a priority. Add the Maps SDK later if routes need map visualization.
