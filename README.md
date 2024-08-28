# Simple Time Tracker

Simple app that helps track how much time you spend on all the useless activities in the world.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="75">](https://f-droid.org/packages/com.razeeman.util.simpletimetracker/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="75">](https://play.google.com/store/apps/details?id=com.razeeman.util.simpletimetracker)
[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png"
     alt="Get it on GitHub"
     height="75">](https://github.com/Razeeman/Android-SimpleTimeTracker/releases/latest)

<img src="dev_files/preview.gif" width="300"/>

<br>

<p>
<img src="metadata/en-US/images/phoneScreenshots/1.png" width="225"/>
<img src="metadata/en-US/images/phoneScreenshots/2.png" width="225"/>
<img src="metadata/en-US/images/phoneScreenshots/3.png" width="225"/>
<img src="metadata/en-US/images/phoneScreenshots/4.png" width="225"/>
<img src="metadata/en-US/images/phoneScreenshots/5.png" width="225"/>
<img src="metadata/en-US/images/phoneScreenshots/6.png" width="225"/>
</p>

## Wear OS

<p>
<img src="dev_files/publish/wear_play.png" width="200"/>
<img src="dev_files/publish/wear_play_complication.png" width="200"/>
</p>

## Technology stack
- Kotlin
- Multi module
- Single Activity
- MVVM (Jetpack ViewModel + LiveData)
- Jetpack Navigation
- Jetpack Compose
- Hilt
- Room, migrations
- Coroutines
- Wear OS
- Widgets
- Notifications
- Custom Views (Pie Chart, Bar Chart, Color Selection, Calendar)
- Recycler, custom Adapter Delegates, DiffUtils with Payloads
- Drag and Drop, Gesture detection
- Gradle Kotlin DSL
- View Binding
- Database backup and restore, export to csv, automatic backup
- Dark mode
- Unit tests, UI tests
- CI with github actions
- Emojis with EmojiCompat

## Build flavors
- base - F-Droid version, no google play services, no Wear OS support.
- play - Google Play version, with google play services, Wear OS support.

## Directory structure
    .
    ├── .github                               # CI files.
    ├── app                                   # Mobile app.
    ├── buildSrc                              # Deps and versions.
    ├── core                                  # Shared classes, strings.
    ├── data_local                            # Database.
    ├── domain                                # Business logic.
    ├── navigation                            # Navigation interfaces and screen params.
    ├── resources                             # Common resources between phone and watch apps.
    ├── wear                                  # Wear OS app.
    ├── wear_api                              # Mobile - Wear OS communication contracts.
    ├── features
    │   ├── feature_archive                   # Screen for archived data.
    │   ├── feature_base_adapter              # Shared recycler adapters.
    │   ├── feature_categories                # Screen for categories and tags.
    │   ├── feature_change_activity_filter    # Edit activity filter screen.
    │   ├── feature_change_category           # Edit category screen.
    │   ├── feature_change_complex_rule       # Edit complex rule screen.
    │   ├── feature_change_goals              # Edit goals common logic.
    │   ├── feature_change_record             # Edit record screen.
    │   ├── feature_change_record_tag         # Edit tag screen.
    │   ├── feature_change_record_type        # Edit type screen.
    │   ├── feature_change_running_record     # Edit timer screen.
    │   ├── feature_complex_rules             # Screen for complex rules list.
    │   ├── feature_date_edit                 # Data edit screen.
    │   ├── feature_dialogs                   # Dialogs.
    │   ├── feature_goals                     # Separate screen for goals.
    │   ├── feature_main                      # Main screen with tabs.
    │   ├── feature_notification              # Notifications.
    │   ├── feature_pomodoro                  # Pomodoro mode.
    │   ├── feature_records                   # One of main tabs, records list.
    │   ├── feature_records_all               # Screen showing all records.
    │   ├── feature_records_filter            # Dialog for records filters.
    │   ├── feature_running_records           # One of main tabs, timers.
    │   ├── feature_settings                  # One of main tabs, settings.
    │   ├── feature_statistics                # One of main tabs, statistics.
    │   ├── feature_statistics_detail         # Screen showing detailed statistics.
    │   ├── feature_tag_selection             # Screen for selecting tags.
    │   ├── feature_views                     # Custom views.
    │   ├── feature_wear                      # Phone app logic to connect to wear app.
    │   └── feature_widget                    # Widgets.

## License

**Android App**

Copyright (C) 2020-2024
Anton Razinkov devrazeeman@gmail.com

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

**Wear OS App**

Copyright (C) 2023-2024
Joseph Hale https://jhale.dev, [@kantahrek](https://github.com/kantahrek), Anton Razinkov devrazeeman@gmail.com

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at https://mozilla.org/MPL/2.0/.
