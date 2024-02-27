<!-- This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/. -->
# Components

Components are "view-only" chunks of UI. In other words, the UI they render
depends only on the values of the parameters passed to them -- no reading data
from any sort of state, local or remote.

These properties make components extraordinarily easy to test, particularly by
adding `@Preview` versions within the source code file.
 - Note `@Preview` versions also render *immediately* in the preview pane,
   dramatically improving development velocity.