<!-- This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/. -->
# Presentation

The UI for the app. Responsible for everything the user sees and interacts with.

## MainActivity

The `MainActivity` is what gets launched when the user opens the app on his/her
smartwatch. The `MainActivity` typically immediately delegates to a `navigation`
component which then manages which `screen` is first loaded and shown to the
user.

Each `screen` fetches the data it needs (typically using a `remember` function)
then renders a `layout` containing one or more `components` for the user to interact with.