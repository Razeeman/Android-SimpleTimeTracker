<!-- This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/. -->
# Navigation

Navigation is responsible for moving users between screens of the app.

Typically, each screen will take one or more listener lambdas as parameters, to
which Navigation will provide implementations which move the user from one
screen to another.