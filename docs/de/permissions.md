---
title: Berechtigungen
---
Das Teamscale Plugin bringt folgende Berechtigungen mit:

* Globale Berechtigungen:
     * Teamscale global konfigurieren: Darf die globale Teamscale Konfiguration im SCM-Manager lesen und ändern
     * Teamscale repository-spezifisch konfigurieren: Darf die Repository Teamscale Konfiguration im SCM-Manager lesen und ändern
     * Teamscale Findings lesen: Darf Teamscale Findings (Badge) sehen
     * Teamscale Findings schreiben: Darf Teamscale Findings liefern / setzen.
* Repository-spezifische Berechtigungen:
    * Teamscale konfigurieren: Darf die Teamscale Konfiguration im SCM-Manager lesen und ändern
    * Teamscale Findings lesen: Darf Teamscale Findings (Badge) sehen
    * Teamscale Findings schreiben: Darf Teamscale Findings liefern / setzen.

Benutzer mit der Rolle READ können automatisch Findings an Pull Requests dieses Repositories lesen.
Es gibt eine Rolle TEAMSCALE, die Findings lesen und ändern darf. Diese Rolle sollte einem technischen Benutzer zugeordnet werden.
