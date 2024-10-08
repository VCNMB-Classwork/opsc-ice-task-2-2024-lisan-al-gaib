﻿### opsc-ice-task-2-2024-lisan-al-gaib

# Tea Recipe Notification App

This application demonstrates how to use notifications in an Android app to display random tea recipes. It also includes a Firebase component, which is still in progress.

## Features

- Display random tea recipes as notifications.
- Copy the Firebase token from the notification to the clipboard.
- Enable or disable notifications.
- VIew notification history.

## Prerequisites

- Android Studio
- Firebase account
- Firebase project with FCM enabled

## Setup

1. Clone the repository:

    git clone <repository-url>
    cd <repository-directory>

2. Open the project in Android Studio.

3. Add the 'google-services.json' file:

    - Download the 'google-services.json' file from your Firebase project settings.
    - Place the file in the 'app' directory of your project.
    - Sync the project with Gradle files.

## Permissions

Ensure the following permissions are added in the 'AndroidManifest.xml' file:

[ <uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/> ]

## Usage
Run the app on an Android device or emulator.

1. Enable notifications:

2. Use the radio buttons to enable or disable notifications.
Display a random tea recipe:

3. Click the "Give Me Tea" button to display a random tea recipe as a notification.
Generate and display Firebase token:

4. Click the "Give Me Fire" button to generate and display the Firebase token as a notification.
Copy notification text:

5. Click the "Copy" action button in the notification to copy the token text to the clipboard.

## Firebase Component
The Firebase component is still in progress. As of now, you can only view and copy the access token.
