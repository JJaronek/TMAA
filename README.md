Aplikace pro trackování šipkových her, zatím jen 501 a 301 (DO)


lokální databáze za pomocí ROOM knihovny - po každé hře se hráči a jejich skóre lokálně uloží

alespoň základní práce s cloudovou Firestore databází - Ve firestore jsou uloženy nejlepší průměry hráčů

práce s notifikacemi - Na Firestore je přilepený listener, a pokud nějaký z hráčů vylepší své skóre, přijde nám o tom notifikace.

jednoduchá komunikace se zvoleným API - Tady jsem dlouho nevěděl, jak užitečně pojmout. Nakonec se po výhře zobrazí gif z [Giphy](https://developers.giphy.com/)

Known issues

aplikace je P O M A L Á
ne každý checkout by měl být možný (checkout lichého čísla mezi 160 a 170, checkout mezi 170 a 180).

Todo

Rozšíření pro více gamemodů, hlavně solo soutěž (around the clock,..)
