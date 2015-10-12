# Station coordinates #

The "Urban Mobility center" (sumc.bg) does not provide list with bus stations and their corresponding GPS coordinates. I'm not sure what is the reason for this?! They do not respond to my letters...

I have list with approximate 2500 bus stops (thank ... he knows).
If someone needs this data - get it from application: SofiaPublicTransportNavigator/res/raw/coordinates.xml ( http://code.google.com/p/sofia-public-transport-navigator/source/browse/res/raw/coordinates.xml ). Format is pretty simple so you will understand without explanations.

The bad news is that this information is not very up to date - from time to time SUMC (SKGT) add and remove stops.
To fix this - I need to add a module to Android application which will download XML file (with new bus stops) and than add them to application's SQL lite DB in Android.
New desktop application should be written that crawls website sumc.bg and gets new bus stops. If someone really needs it - just say - it would not be difficult to me to write it (if someone actually needs it).

**Update:** there is separate project for this: http://sumc-crawler.googlecode.com/