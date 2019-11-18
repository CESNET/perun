# OpenAPI specification of Perun RPC API

This project contains specification of [Perun RPC API](https://perun-aai.org/documentation/technical-documentation/rpc-api/index.html) in [OpenAPI](https://swagger.io/docs/specification/about/) 3 
format and a Maven pom.xml file for generating a Java client for the API.

The generated client can be used int this way:

First specify a Maven dependency on this project:
```xml
<dependency>
	<groupId>cz.metacentrum.perun</groupId>
	<artifactId>perun-openapi</artifactId>
	<version>${perun.version}</version>
</dependency>
```

then use the class PerunRPC in your code:

```
import cz.metacentrum.perun.openapi.PerunRPC;
import cz.metacentrum.perun.openapi.PerunException;

...

     PerunRPC perunRPC = new PerunRPC(PerunRPC.PERUN_URL_CESNET, user, password);
     try {

        Group group = perunRPC.getGroupsManager().getGroupById(1);

     } catch (HttpClientErrorException ex) {
         throw PerunException.to(ex);
     } catch (RestClientException ex) {
        log.error("connection problem",ex);
     }
```
