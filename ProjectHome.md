# Description #
This Android application helps people in choosing the bus station on which to wait. In real time it gives information about the estimated time of arrival for the selected station. The language of application is Bulgarian and works only for the region of Sofia. Participate in the contest Application Factory, organized by Mobiltel and HTC.

This is my first Android application so I think there is many broken patterns in it (sorry of that). It is here to help people in learning Android SDK. If someone can help me with advices, good patterns, fixing of bad pieces in code, ideas or something else - feel free to do it. Thanks in advance.

You can use this application in BlackBerry 10+, too - just download the .apk file (send me email)

For more information go to [Wiki pages](http://code.google.com/p/sofia-public-transport-navigator/w/list).

# Version #
Current version 1.21
  * Varna support (Thanks to Kristina Petrova)
  * Enabled traffic layer
  * Support for updating bus stop configuration
  * Fix for hiding wait dialog when fetching estimates info (Thanks to Stefan Dimov and Ivan Ivanov)
  * Fixed order of estimates after 00:00 (Thanks to Nikola Hadilev)
  * Other fixes/refactoring
  * Added statistics
  * Minimal Android version changed to 2.1

Previous version 1.20
  * Added "Search by bus stop ID" option, thanks to **Deno** for the idea
  * New HOLO theme for supported devices
  * Fixed estimates when arrival time is in next day, thanks to **Nikola Hadilev** for the bug report
  * Added English language in MENU>Настройки>Use English language, thanks to **Kevin Smith** and **Konrad Lewalski** for the request

Previous version: 1.19:
  * Workaround for bug in Globul proxy server, thanks to **Kalin Kanev** for bug reporting!
  * App2SD enabled

For **downloads** go to [download page](http://code.google.com/p/sofia-public-transport-navigator/downloads/list) or scan this QR code using your Android phone (link to Android Market):
![http://sofia-public-transport-navigator.googlecode.com/files/qr-code-market.png](http://sofia-public-transport-navigator.googlecode.com/files/qr-code-market.png)

For information about changes in versions - go to [versions page](Versions.md).

# Screenshots #
![http://wiki.sofia-public-transport-navigator.googlecode.com/hg/images/map.png](http://wiki.sofia-public-transport-navigator.googlecode.com/hg/images/map.png)
![http://wiki.sofia-public-transport-navigator.googlecode.com/hg/images/estimates.png](http://wiki.sofia-public-transport-navigator.googlecode.com/hg/images/estimates.png)
![http://wiki.sofia-public-transport-navigator.googlecode.com/hg/images/favourities.png](http://wiki.sofia-public-transport-navigator.googlecode.com/hg/images/favourities.png)
![http://wiki.sofia-public-transport-navigator.googlecode.com/hg/images/favourities-edit.png](http://wiki.sofia-public-transport-navigator.googlecode.com/hg/images/favourities-edit.png)
![http://wiki.sofia-public-transport-navigator.googlecode.com/hg/images/preferences.png](http://wiki.sofia-public-transport-navigator.googlecode.com/hg/images/preferences.png)

# Used APIs #
Application uses some Android APIs like MapActivity, ItemizedOverlay in MapActivity (to show closes stops on map), PreferenceActivity (for settings), location, HttpClient (to get response from m.sumc.bg/vt), ContentProvider with SQL lite (to store bus stop coordinates), XMLReader (to parse bus stop coordinates and to load them into DB), embeded WebView and some other UI components.

# SofSpirka #
There is one similar application, called SofSpirka (http://sofspirka.sirma.mobi/). It was created after sofia-public-transport-navigator. I have not tried it but I think it is more useful because it is created from company, and it is not an amateur project like mine. So if you just want to use such an application - try it, too... Second thing that is better in SofSpirka is that they have internal connection with SUMC servers (as I know) and their data will be more accurate. sofia-public-transport-navigation uses site m.sumc.bg/vt in order to get data for approximate arrival time. Response is plain HTML and after response is received it is parsed in order to get needed logical data (bus1 - arrival time, bus2 - arrival time). The worst thing is that m.sumc.bg is not working all the time and from time to time anomalies happens (like information for trams for bus stops that are only for busses).


---

_P.S. sorry for my bad English (in all pages) :(_

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