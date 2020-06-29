---
title: Permissions
---
The Teamscale plugin comes with these permissions:

* Global permissions:
     * configure Teamscale globally: May read and modify the global Teamscale configuration
     * configure Teamscale repository-specific: May read and modify the repository-specific Teamscale configuration
     * read Teamscale Findings: May read Teamscale findings for all repositories
     * modify Teamscale Findings: May modify Teamscale findings for all repositories
* Repository-specific permissions:
    * configure Teamscale: May read and modify the Teamscale configuration
    * Teamscale Findings lesen: May read Teamscale findings
    * Teamscale Findings schreiben: May modify Teamscale findings

Users with the READ role can automatically read teamscale findings. 
There exists a role named TEAMSCALE which provides permissions to read and modify Teamscale findings. This role is intended for technical user.
