# Description #
This Android application helps people in choosing the bus station on which to wait. In real time it gives information about the estimated time of arrival for the selected station. The language of application is Bulgarian and works only for the region of Sofia. Participate in the contest Application Factory, organized by Mobiltel and HTC.

(In bulgarian:
Android приложението помага на хората при избора на спирка на градски транспорт, на която да чакат. В реално време дава информация за приблизителния час на пристигане за избрана спирка. Езикът на приложението е български и работи само за района на гр.София. Участва в конкурса Application Factory, организиран от Mobiltel и HTC.
)

This is my first Android application so I think there is many broken patterns in it (sorry of that). It is here to help people in learning Android SDK. If someone can help me with advices, good patterns, fixing of bad pieces in code, ideas or something else - feel free to do it. Thanks in advance.

# Screenshots #
![http://wiki.sofia-public-transport-navigator.googlecode.com/git/images/map.png](http://wiki.sofia-public-transport-navigator.googlecode.com/git/images/map.png)
![http://wiki.sofia-public-transport-navigator.googlecode.com/git/images/estimates.png](http://wiki.sofia-public-transport-navigator.googlecode.com/git/images/estimates.png)
![http://wiki.sofia-public-transport-navigator.googlecode.com/git/images/favourities.png](http://wiki.sofia-public-transport-navigator.googlecode.com/git/images/favourities.png)
![http://wiki.sofia-public-transport-navigator.googlecode.com/git/images/favourities-edit.png](http://wiki.sofia-public-transport-navigator.googlecode.com/git/images/favourities-edit.png)
![http://wiki.sofia-public-transport-navigator.googlecode.com/git/images/preferences.png](http://wiki.sofia-public-transport-navigator.googlecode.com/git/images/preferences.png)

# Used APIs #
Application uses some Android APIs like MapActivity, ItemizedOverlay in MapActivity (to show closes stops on map), PreferenceActivity (for settings), location, HttpClient (to get response from m.sumc.bg/vt), ContentProvider with SQL lite (to store bus stop coordinates), XMLReader (to parse bus stop coordinates and to load them into DB), embeded WebView and some other UI components.

# SofSpirka #
There is one similar application, called SofSpirka (http://sofspirka.sirma.mobi/). It was created after sofia-public-transport-navigator. I have not tried it but I think it is more useful because it is created from company, and it is not an amateur project like mine. So if you just want to use such an application - try it, too... Second thing that is better in SofSpirka is that they have internal connection with SUMC servers (as I know) and their data will be more accurate. sofia-public-transport-navigation uses site m.sumc.bg/vt in order to get data for approximate arrival time. Response is plain HTML and after response is received it is parsed in order to get needed logical data (bus1 - arrival time, bus2 - arrival time). The worst thing is that m.sumc.bg is not working all the time and from time to time anomalies happens (like information for trams for bus stops that are only for busses).

P.S. sorry for my bad English (in all pages) :(


# From Android market (in Bulgarian) #
Помага на хората в София при избора на спирка на градски транспорт, на която да чакат. В реално време дава информация за приблизителния час на пристигане за избрана спирка. Данните се взимат от GPS системата на СКГТ и би трябвало да са максимално близки до реалността.
Поддържат се времената на пристигане на автобусния, тролейбусния и трамвайния транспорт в София. Данни за метрото НЯМА, защото диспечерската система която поддържа подземната железница в София не изнася информацията за времената на пристигане извън диспечерските зали.

Tix.bg е интегриран в приложението - изберете MENU>Задръствания/маршрути, след което трябва да инсталирате tix.bg и ще можете да виждате задръстванията в София. За маршрут от tix.bg (до където се стига от MENU>Задръствания/маршрути) изберете MENU>Маршрут

Може да изберете начален екран - карта (по подразбиране) или списък с избрани спирки (favourities): От [MENU>Настройки>'Избрани спирки' за начален екран] можете да укажете списъкът с избрани спирки да се показва при стартиране на приложението (вместо картата), благодарности на Marto за идеята! За да се върнете от 'Избрани спирки' към 'Карта' натиснете BACK на телефона или изберете спирка.


Проектът е с отворен код, за повече информация:
Open source at http://sofia-public-transport-navigator.googlecode.com

СРАМОТА!!! Хората от "Софийска компания градски транспорт" май не искат услугата да е максимално достъпна за хората (и по този начин да предлагат по-добра услуга), а точно обратното и в тази връзка постоянно променят формата на страницата с времената на пристигане. Моят стремеж е максимално бързо след такава промяна да я отразя и в приложението. Според мен това е недопустимо от тяхна страна и работят срещу интересите си - техните и на техните клиенти, но това е положението... Докато мисленето на ръководните фактори е такова няма как да се промени към по-добро битието...
Ако това не Ви харесва може да се обадите на телефоните им или да им напишете писмо и по този начин заедно да променим това отношение. Благодаря предварително!


Подобни приложения за град София - потребителят трябва да има право на избор:
  * Софспирка/Sofspirka от Sirma mobile
  * Градски транспорт в София от vanangelov
  * Bus Info cgm от Vladovsoft
  * Ватман от oborudko
  * Разписание на автобусите mTrans от Ligla.com
  * ако искате и вашето приложение да се появи тук - просто го напишете в коментарите и ще го добавя тук

**ПОПРАВЕНО: От 26 януари SUMC са сменили начина на ползване на сайта си и приложението не работи - очаквайте до утре (27 януари) работеща версия. Извинявам се за причиненото неудобството!**
Участва в конкурса Application Factory, организиран от Mobiltel и HTC, Sofia, Bulgaria

# Versions
### Version 1.39 (01.10.2017) ###
  * Connected to the new API endpoint

### Version 1.38 (18.07.2017) ###
  * Added current location button
  * Fixed bus stops coordinates
  * Migrated to Android Studio

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
  
# Station coordinates #

The "Urban Mobility center" (sumc.bg) does not provide list with bus stations and their corresponding GPS coordinates. I'm not sure what is the reason for this?! They do not respond to my letters...

I have list with approximate 2500 bus stops (thank ... he knows).
If someone needs this data - get it from application: SofiaPublicTransportNavigator/res/raw/coordinates.xml ( http://code.google.com/p/sofia-public-transport-navigator/source/browse/res/raw/coordinates.xml ). Format is pretty simple so you will understand without explanations.

**Update:** there is separate project for this: http://sumc-crawler.googlecode.com/
