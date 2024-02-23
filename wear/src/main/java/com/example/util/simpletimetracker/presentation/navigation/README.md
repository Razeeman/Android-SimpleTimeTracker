<!-- This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/. -->
# Navigation

Navigation is responsible for defining the screens available in the app and marshalling the correct
parameters to each requested screen.

Typically, each screen will accept a `navigation: NavController` parameter so it can decide when to
send the user to another screen.