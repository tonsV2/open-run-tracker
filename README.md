# Open Run Tracker
Open source run tracker build for fun... https://play.google.com/store/apps/details?id=dk.fitfit.runtracker

# Features
* Start/stop tracking
* Show a list of previous runs
** Export a run as GPX
** Delete

# Development Setup
`app/src/main/res/values/google_maps_api_key.xml` should be populated with a valid Google Maps API key. Same goes for `app/src/release/res/values/google_maps_api_key.xml`

# TODO
* Proper permission handling...
** Show proper text on request screen
** If background GPS isn't select show message that it's important
* Create dedicated MapLocationViewModel... Only updateRunId method and locations
* Detect GPS single
* Delete from history
* Show notification when app isn't in focus
* Settings
** Imperial/metrics
** AM/PM or 24h
** Show notification
** Theme
* Dark theme
** Make history icon black... Atleast not white
* Backend for storage
* Web interface
* Login
* Stats
** Run done stats... Avg. speed, height meters, etc.
** Over time stats... Fastest 1k, 3k, 5k, etc. Longest run
** Visual stats... Graphs

# Inspiration
* https://github.com/android/location-samples/tree/master/LocationUpdatesBackgroundKotlin

# Credits
* Thanks to Freepik for making the application available to public domain - I couldn't find it on their own site - https://www.flaticon.com/free-icon/running_763812?term=run%20sport&page=1&position=9
