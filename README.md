# FamilyBubbles - Android home-screen calling widget

A deliberately simple Android widget for children: tap a family member's face and the phone immediately places a normal phone call to that person.

## What is included

- Parent setup app
- Add/edit/delete up to 6 family members shown on the widget
- Pick a person from Android Contacts without requesting full contacts access
- Choose a photo from the device
- Circular face photos in the widget
- Tap face/name -> immediate `ACTION_CALL`
- Runtime `CALL_PHONE` permission setup
- Widget updates automatically when family members are changed
- Empty widget state opens the setup app

## Open and run

1. Open this folder in Android Studio.
2. Allow Gradle to sync and install Android SDK 35 if Android Studio asks. The included launcher can fetch the small Gradle wrapper JAR on first use if it is not already present.
3. Run the app on a real Android phone.
4. In FamilyBubbles, press **Allow calls** and grant phone-call permission.
5. Add family members with their phone numbers/photos.
6. Go to the Android home screen, long-press an empty area and choose **Widgets**.
7. Add the **FamilyBubbles** widget.
8. Tap a face to test.

## Important Android behaviour

The app uses Android's `CALL_PHONE` permission and `Intent.ACTION_CALL`, so once permission has been granted, a face tap requests an immediate normal telephone call rather than opening the dialler first.

Some phones can still interrupt this flow because of manufacturer settings, parental controls, dual-SIM selection, lock-screen rules or telecom restrictions. Test the exact child device before relying on it.

## WhatsApp

Version 1 intentionally uses normal phone calls. WhatsApp does not provide a dependable public Android deep link that starts a specific voice call with zero further taps, so it has not been hacked in with accessibility/autoclick behaviour.

## Project

- Kotlin
- XML layouts + ViewBinding
- Android AppWidget / RemoteViews
- Min SDK 26
- Target / Compile SDK 35
