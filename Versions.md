### Version 1.21 (17.11.2013) ###
  * Varna support (Thanks to Kristina Petrova)
  * Enabled traffic layer
  * Support for updating bus stop configuration
  * Fix for hiding wait dialog when fetching estimates info (Thanks to Stefan Dimov and Ivan Ivanov)
  * Fixed order of estimates after 00:00 (Thanks to Nikola Hadilev)
  * Other fixes/refactoring
  * Added statistics
  * Minimal Android version changed to 2.1

### Version 1.20 (21.01.2013) ###
  * Added "Search by bus stop ID" option, thanks to **Deno** for the idea
  * New HOLO theme for supported devices
  * Fixed estimates when arrival time is in next day, thanks to **Nikola Hadilev** for the bug report
  * Added English language in MENU>Настройки>Use English language, thanks to **Kevin Smith** and **Konrad Lewalski** for the request

### Version 1.19 (29.07.2012) ###
  * Workaround for bug in Globul proxy server, thanks to **Kalin Kanev** for bug reporting!
  * App2SD enabled

### Version 1.18 (23.02.2012) ###
  * Cyrillic support for Android 3.0+ (incl. 4.0), thanks to **Delian** for bug reporting!
  * Location service is not enabled when unlocking phone if estimates dialog is displayed

### Version 1.17 (11.02.2012) ###
  * Integration with tix.bg
  * Bug fixes

### Version 1.16 (04.02.2012) ###
  * Look and feel improvements
  * Better SUMC integration

### Version 1.15 (02.02.2012) ###
  * Changed request to sumc according to their madness - user is asked for captcha

### Version 1.14 (30.01.2012) ###
  * Changed request to sumc according to their new page - two requests are created - one for query parameters, second for estimates

### Version 1.13 (26.01.2012) ###
  * Changed request to sumc according to their new page

### Version 1.12 (14.01.2012) ###
  * Fixed map API key

### Version 1.11 (08.01.2012) ###
  * Fixed bug with String.format() and default locale causing float values (coordinates) to have "," (instead of ".") in SQL queries

### Version 1.10 (27.10.2011) ###
  * Change startup screen - map or favorities

### Version 1.09 (01.07.2011) ###
  * Support for bus stop favorities
  * Button for refreshing estimates in dialog

### Version 1.08 (19.12.2010) ###
  * Fixed searching of bus stop by ID

### Version 1.07 (12.12.2010) ###
  * Phone time is used in estimates (not server, which is unreliable)

### Version 1.06 (31.10.2010) ###
  * Many visual improvements

### Version 1.05 (28.10.2010) ###
  * Fixed: new format of m.sumc.bg (Defect #13)
  * Removed old plain dialog (no time to support and rich dialog is more useful)
  * Added link to bus schedule in estimates dialog

### Version 1.04 (24.10.2010) ###
  * Fixed bug: wrong map key used in version 1.03

### Version 1.03 (24.10.2010) ###
  * Added feature: Compass arrow in my position (Feature #12).

### Version 1.2 (18.10.2010) ###
  * Fixed bug: wrong map key used in version 1.1

### Version 1.1 (16.10.2010) ###
  * _unusable: wrong map key used_
  * Fixed bug: Location dot prevents bus stop icon from being tapped (Defect #1)...
  * Added feature: Bus stop code in title of estimates dialog (Feature #8).

### Version 1.0 (13.06.2010) ###
  * Initial version - see [about section](About.md).