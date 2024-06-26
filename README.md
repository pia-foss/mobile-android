[![PIA logo][pia-image]][pia-url]

# Private Internet Access
Private Internet Access is the world's leading consumer VPN service. At Private Internet Access we believe in unfettered access for all, and as a firm supporter of the open source ecosystem we have made the decision to open source our VPN clients. For more information about the PIA service, please visit our website [privateinternetaccess.com](https://privateinternetaccess.com).

# Android Application
A re-write of the PIA Android application. Written in Kotlin, adhering to the Clean Code principles.

## Installation

### Requirements
 - Git (latest)
 - Android Studio (Stable channel)
 - Gradle (latest)
 - ADB installed
 - NDK (latest)
 - Android 7+


Please use these instructions to install Git on your computer if it is not already installed: [Installing Git](https://gist.github.com/derhuerst/1b15ff4652a867391f03)

Please use these instructions to install and download Android Studio on your computer if it is not already installed: [Android Studio Download Link](https://developer.android.com/studio/index.html)

#### Download Codebase
Using the terminal:

`git clone https://github.com/xvpn/kp_vpn_android *folder-name*`

Type in what folder you want to put in without the ** or use a graphical interface like Android Studio's to clone the repo.

This will pull the main repository as well as the required dependencies.


#### Building

>Once the project is cloned, the project will build once opened in Android Studio. This will require building the binaries for the underlying modules and configurations. This can take a while for certain computers and is only done on complete clean and rebuilds. You can see progress in the gradle console. Once completed, the app will be able to be run on a device connected to the computer or an emulator running on your computer.

## Documentation

#### Architecture
Code structure via packages:

* `app` - the main application module. It acts as a proxy to all features implemented within the `features` module.
* `buildSrc` - holds a list of dependencies used within the app.
* `capabilities` - holds modules for various app capabilities.
  * `csi` - contains customer support capabilities, responsible for collecting and sending logs.
  * `location` - checks for location permissions.
  * `networkmanagement` - provides the ability to set, change and remove network rules.
  * `notifications` 
  * `shareevents` - used to record connection status and speeds.
  * `snooze` - contains snooze-related code.
  * `ui` - defines shared UI elements, themes, fonts, styling, etc. It also contains all string resources.
* `core` - contains core functionality modules.
  * `httpclient`
  * `localprefs` - a module containing more functionality specific modules. It holds the shared preferences and the respective data objects.
  * `obfuscator`
  * `payments` - implements payments flow for both Google and Amazon. This is defined by a `product` flavor within the `build.gradle` file.
  * `portforwarding`
  * `regions`
  * `router` - our own implementation of navigation.
  * `utils`
  * `vpnconnect` - logic for establishing a VPN connection.
  * `vpnlauncher`
* `features` - contains modules for single features, such as region selection, connection, signup, etc.

#### Coding Style

#### Significant Classes and files

### Contributing

By contributing to this project you are agreeing to the terms stated in the Contributor License Agreement (CLA) [here](/CLA.rst).

For more details please see [CONTRIBUTING](/CONTRIBUTING.md).

Issues and Pull Requests should use these templates: [ISSUE](/.github/ISSUE_TEMPLATE.md) and [PULL REQUEST](/.github/PULL_REQUEST_TEMPLATE.md).

### Testing

In order to run the instrumented test we need to set environment variables containing valid credentials.
They need to have the format `PIA_VALID_USERNAME`, `PIA_VALID_PASSWORD` and `PIA_VALID_DIP_TOKEN`.

### License

This project is licensed under the [MIT (Expat) license](https://choosealicense.com/licenses/mit/), which can be found [here](/LICENSE).

### Acknowledgements

This product includes software developed by the OpenSSL Project for use in the OpenSSL Toolkit. (https://www.openssl.org/)

© 2002-2017 OpenVPN Inc. - OpenVPN is a registered trademark of OpenVPN Inc.


<!-- Markdown link & img dfn's -->
[pia-image]: https://assets-cms.privateinternetaccess.com/img/frontend/pia_menu_logo_light.svg
[pia-url]: https://www.privateinternetaccess.com/
[wiki]: https://en.wikipedia.org/wiki/Private_Internet_Access
