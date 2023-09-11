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

*Please note that Android 4.x emulators are broken with VPNs and will not connect.*

While waiting for the application to build, set up a personal token in github.
1. In Github, click on your profile and click on `Settings`
2. Then, Click on `Developer settings`
3. In the side menu click on `Personal access tokens` > `Tokens (classic)`
4. Then, `Generate new token` > `Generate new token (classic)`
5. You will be ask to enter two-factor authentication code from Okta 
6. Name the token, select the expiration date to 90 days and 
   - delete: packages
   - write: packages
7. Finally, click on `Generate token`
8. Copy the generated token

On your computer, go to your root folder,
1. Press this command on your keyboard, `cmd` + `shift` + `.`, this will show the hidden files on your root folder
2. Open `.zshenv` file, if it doesn't exist create one.
3. Paste the code 
   - export **GITHUB_USERNAME** = *your git hub username*
   - export **GITHUB_TOKEN** = *your generated token*

By this time, the build should had been completed,
1. Go to Android Studio, click on `local.properties`
2. In the file, add the following:
   - **GITHUB_USERNAME** = *your git hub username*
   - **GITHUB_TOKEN** = *your generated token*
3. Save
4. Sync the project with Gradle files
5. Build the application
6. You should be able to run the application with no issue

Note: We are doing this because the repository is not in public yet. Once it goes to public, these steps are not necessary.

## Documentation

#### Architecture
Code structure via packages:

* `app` - the main application module. It acts as a proxy to all features implemented within the `features` module.
* `buildSrc` - holds a list of dependencies used within the app.
* `capabilities` - holds modules `notifications`, `shareevents` and `ui` used across the app.
  * `ui` - defines shared UI elements, themes, fonts, styling, etc.
* `core` - contains core functionality modules.
  * `localprefs` - a module containing more functionality specific modules. It holds the shared preferences and the respective data objects.
  * `payments` - implements payments flow for both Google and Amazon. This is defined by a `product` flavor within the `build.gradle` file.
  * `router` - our own implementation of navigation.
  * `vpnconnect` - logic for establishing a VPN connection.
* `features` - contains modules for single features, such as region selection, connection, signup, etc.

#### Coding Style

#### Significant Classes and files

### Contributing 

### Authors

 * Iva Nedeleva - kp-iva-nedeleva
 * Ruth Ondrada - kp-ruth-o

### License

NEED TO FILL THIS OUT!!!
This project is licensed under the MIT (Expat) license, which can be found here.

### Acknowledgements

This product includes software developed by the OpenSSL Project for use in the OpenSSL Toolkit. (https://www.openssl.org/)

© 2002-2017 OpenVPN Inc. - OpenVPN is a registered trademark of OpenVPN Inc.


<!-- Markdown link & img dfn's -->
[pia-image]: https://assets-cms.privateinternetaccess.com/img/frontend/pia_menu_logo_light.svg
[pia-url]: https://www.privateinternetaccess.com/
[wiki]: https://en.wikipedia.org/wiki/Private_Internet_Access
