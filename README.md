# Watchpoint Guide

Watchpoint is a set of two paired applications. A smartphone application for Android devices and a smartwatch application for WearOS devices.

## Pre-Requisites 

### Installing the Watchpoint applications

Included in this folder is are two APKs, one for the mobile application and one for the wearOS application. Their file names tell you which is which.
Getting it to run on Android is as easy as transfering and launching the APK on a phone, but WearOS proves more difficult.
There are a few complicated methods to transfer the APK, but I recommend just launching the source code in Android Studio and using it's built in ADB Bridge to launch the app.

Here is a tutorial for getting a smartwatch linked with Android Studio:
https://medium.com/@elliptic1/connecting-to-your-pixel-watch-to-android-studio-8478aa329b26

Please make sure that the smartwatch application has been given permission to access health information.

### Setting up the devices

Once both applications are installed, ensure the Android Device and WearOS device are paired using the 'Watch' app from the PlayStore.

Here is an in-depth tutorial provided by Google for pairing:
https://support.google.com/googlepixelwatch/answer/12651780?hl=en

Once everything is installed and paired the apps are ready for use!

## Average Application Use

When first opening the Watchpoint application, you will immediately be greeted with the Home Page. For ease of use, Watchpoint does not use any account features, keeping user identity separate from the usage of the application.
In the rest of this section, I will describe activities you can complete using Watchpoint.

### Starting & Completing a Guide

First, you can check out the 'All Guides' section by clicking on the bottom left card.

On this page, all guides currently implemented are displayed, including a search box.

When you select one that interests you, you will be taken to the individual guide page.
On this page you can:
- Mark guides as 'Daily' by selecting the document icon in the top right. This will display a shortcut to the individual guide page on the home page of Watchpoint. Also displayed alongside the shortcut is a checkbox, displaying if that guide has been completed on today's date.
- Mark guides as 'Favourite' by selecting the heart icon in the top left. This will push guides to the top of all 'All Guides' page, allowing for quick access to guides that aren't fulfilled daily.
- Start the guide using the 'Start Guide on Watch' button.

Before starting the guide, make sure that Watchpoint is open on the Smartwatch. It doesn't need to be in the foreground.
If it's your first time starting a guide, you may need to accept the health information permission request and press the 'start' button again.

Now, the first step of the guide should be displaying on your watch-face!
Press the right side of the watch-face to advance the guide a step and the left side to go back a step.
During the guide, the watch automatically tracks your heart-rate information.
You can now complete the guide at your own pace.

### Starting & Completing a Resting Heart-Rate Tracking Session 

By selecting the settings icon on the home page, a pop-up menu will appear with three options.
- Start Resting Heart-Rate Tracking on Watch
- Delete Just Resting Heart-Rate Data
- Delete Just Heart-Rate Data from Guide Sessions

Select the first option and a timer will start on your watch. It is up to you how long you let it run for, but I recommend over 30 seconds at least.

### Viewing & Deleting Heart-Rate Information

Once a guide has been completed, you can view it in your heart-rate data, including your average BPM during the guide by navigating to the home page and pressing 'View Health Info'.
You can delete all your heart-rate data on this page too, by tapping the big red 'Delete All Heart-Rate Data' button and then tapping again to confirm.
If you want to delete just your resting heart-rate data or just heart-rate data collected from guides, this can be completed through the settings menu on the home page.

### How to tell when heart-Rate data has been marked as concerning

If the application has marked heart-rate data it analysed as concerning, the application with notify you by making the 'View Health Info' button pulsate a bright red colour.
This will not go away until the concern is addressed, but does not impact functionality of the application, so if you are busy you can still complete guides normally.
Once you are on the heart-rate information tab, the concerning guide session will be highlighted red. These can be clicked on.
Once clicked on, you will be led to a page which displays information about the severity of the heart-rate event, which guide was being completed when it happened and what day it happened on.
Included in this page are links to useful resources that point you to support.

Once this page is closed, the notification on the home page will clear.

### Filling the Daily Box

To fill the daily box with guides you want to complete daily, navigate to the guides you want and select the top right document icon.
These guides will now appear in the daily box, alongside a checkmark to signify if they've been completed today or not. This checkmark auto logs on completion of guides.

### Making the Guide's Background Yellow

Upon opening the smartwatch application, select the 'Yellow Background' slider to activate yellow backgrounds during the guides. This is especially useful for users with dyslexia.
