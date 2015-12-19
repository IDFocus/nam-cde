NetIQ Access Manager Conditional Data Extension
===============================================

This is an extension to **NetIQ Access Manager 3.x and 4.x** which allows for *volatile, virtual user attributes* to be created at authentication time. 
A set of admin-specified rules of condition-action type determines the values of these attributes. 

The extension comes in two varieties: an *authentication class* and a *policy extension* of the type external attribtue source. 

The authentication class implementation of the rule engine is the most powerful version of the two. It also uses the most custom code and undocumented Access Manager APIs. 
All rules that are configured for the authentication class are evaluated at authentication time, and all results are written to the LDAP profile of the user that is logging in. 
Rules contained in the authentication class MUST contain a destination attribute name or the result will be lost. Any number of rules is supported. 
The class has been tested on NAM 3.2.x, 4.0.x, 4.1.x and 4.2 versions. 
