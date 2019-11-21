# OpenAPI specification of Perun RPC API

This project contains specification of [Perun RPC API](https://perun-aai.org/documentation/technical-documentation/rpc-api/index.html) in [OpenAPI](https://swagger.io/docs/specification/about/) 3 
format and a Maven pom.xml file for generating a Java client for the API.

## Direct calls in Swagger Editor

The OpenAPI description can be opened in the on-line Swagger Editor, just click on this link:

https://editor.swagger.io/?url=https%3A%2F%2Fraw.githubusercontent.com%2FCESNET%2Fperun%2Fmaster%2Fperun-openapi%2Fopenapi.yml

In the right part of the editor, select the desired Perun server and authentication method in the “Server variables” form.
Then click on the name of the method that you want to call. Click on "Try it out", fill up needed parameters,
then click on "Execute".

## Java client
The generated client can be used in the following way:

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
import cz.metacentrum.perun.openapi.model.Group
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
