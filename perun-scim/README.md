# Perun SCIM protocol support

This module contains support for SCIM protocol v2 within Perun system. For manipulation with resources, SCIM provides
REST API with a simple set of operations.

Current situation:

* we don't support neither filtering nor sorting while obtaining resources from Perun system
* we support `GET` methods only

For more information about SCIM Protocol, please visit [this page](http://www.simplecloud.info/).

### Discovery

To simplify interoperability, SCIM provides three endpoints to discover supported features and specific attribute
details.

 Method | Endpoint                         | Result                                                                | Implemented? 
:------:|----------------------------------|-----------------------------------------------------------------------|--------------
 `GET`  | `/api/v2/ServiceProviderConfigs` | Returns specification compliance, authentication schemes, data models | Yes          
 `GET`  | `/api/v2/ResourceTypes`          | Returns types of available resources                                  | Yes          
 `GET`  | `/api/v2/Schemas`                | Returns resources and attribute extensions                            | Yes          

### Users

Users are members of the groups.

 Method | Endpoint              | Result                | Implemented? 
:------:|-----------------------|-----------------------|--------------
 `GET`  | `/api/v2/Users/{uid}` | Returns a single user | Yes          

### Groups

Groups are used to model the organizational structure of provisioned resources. Groups can contain users or other
groups.

 Method | Endpoint               | Result                 | Implemented? 
:------:|------------------------|------------------------|--------------
 `GET`  | `/api/v2/Groups/{gid}` | Returns a single group | Yes          
