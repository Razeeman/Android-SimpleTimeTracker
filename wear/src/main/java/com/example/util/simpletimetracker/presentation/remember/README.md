<!-- This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/. -->
# Remember Functions

A package of `remember` functions for usage in other Composables.

> If you have worked with ReactJS, this package will remind you of
[React Hooks](https://react.dev/reference/react/hooks).

Essentially, these `remember` functions are reusable chunks of logic that can be
plugged into and shared between multiple Composables.

They contain zero UI/rendering code. Rather they focus on wrapping reasonably
complex chunks of logic (e.g. fetching data from a remote source) for easy
consumption by a UI element.