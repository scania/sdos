# SDOS

Semantic Data Orchestration Service. This service is used for running orchestration flow graph and store the rdf data 
to stardog.

## System Components

![Architecture of SDOS](doc/resources/SDOS_opensource_arch.png)

Semantic Data Orchestration service is an application which makes api calls to different systems, then combine the data 
and transform it into knowlegde graph. It help to achieve the process of ETL, that is Extract-Transform-Load. It 
executes the flow based on OFG(Orchestration Flow Graph). OFG has instances of Orchestration Ontology(OO - model which 
defines rules, options and restriction to create flow 
graphs). These flow graph instance can be executed to get data from specific systems and transform the data to knowledge
graph(rdf) and store it in a result graph. 

It is integrated with Azure AD to support SSO(Single-SignOn). It also supports the OBO flow(On-behalf Of). So user can 
use their AD credentials to access the application. 

## How does SDOS works?
* when SDOS receives the request, first it validates the token. Token will be verified using the signature. Once the token
  has been verified, it fetches the OBO token.  

* Using the OBO token SDOS calls Stardog to read the OFG. SDOS uses the subject iri in the request to fetch the OFG.

* It starts the execution based on OFG.

* Finally push the tranformed RDF data into Stardog Named graph

**(The value of database where the OFG's are stored and the database where it pushes the data into stardog and Azure AD details can 
be configured by passing the values as environment variable)**

## Prerequisites

UPDATE NEEDED

- Java jdk 17 (for building and running the application)
- Stardog >= 9.x (RDF db for saving the knowledge graph)
- Github (to download the source code)
- Gradle 7.4.2 (to build jar file)
- Azure AD (to support SSO)
- SpringBoot 3.2.2

# QuickStart
## Technical Note

Create Azure App for both SDOS & Stardog. Create Stardog Azure App as mobile application in Azure. Register SDOS App as client 
application with required scopes in Stardog App. Then add User in both SDOS App and Stardog 
App with required roles. Upload flow graph in Named Graph(in ofg database). Example Flow Graph can be found [here](https://github.com/scania/sdos-orchestration-flow-graph/blob/main/Pizza/OFG_Pizza.ttl). 
Make sure Stardog supports OIDC and also enable these option for stardog Databases. 

* security.named.graphs: false
* query.all.graphs: true
* search.enabled: true
## Building

1. Clone the project from gitlab.

   ```
   gh repo clone scania/sdos
   ```

2. Go to the root of the project

   ```	
   cd sdos
   ```

3. Update submodules

   ```
   git submodule update --init --recursive
   ```

4. Run gradle, a gradle binary is included in the project, so you don't need to download it
   yourself.

   ```
   ./gradlew build
   ```

5. The binary should now have been built and popped up in the
   folder `{your_project_root}/build/libs`.

## Running

Run it as any other jar file to get the usage output:

	java -jar {your_project_root}/build/libs/sdos.jar -h

This should produce the following output:

      usage: sdos -b <arg> [-h] [-id <arg>] -ofg <arg> -r <arg> -esUrl <arg>
      -b,--stardogBaseUrl <arg>   the baseUrl for a stardog endpoint, this
      option is mandatory.
      -h,--help
      -id,--serviceId <arg>                  A unique identifier for this service, this option is mandatory.
      -ofg,--ofgDb <arg>                     A unique identifier for orchestration database name in stardog, this 
                                             option is mandatory.
      -r,--resultDb <arg>                    A unique identifier for result database name in stardog, this option is 
                                             mandatory.
      -tpSize,--threadPoolSize <arg>         The thread pool size to initialize the Thread Executor service.
      -clientSecret,--sdosClientSecret <arg> SDOS Azure app client secret used for Azure communication.
      -clientScope,--stardogClientScope <arg> Stardog client scope that SDOS can use for token exchange.
      -tenantId,--AzureTenantId <arg>        Tenant id used for Azure communication.

### Arguments

All Arguments will be listed below. Not all arguments are mandatory for the service to run but may
be needed for special cases and needs.

| Program Argument      | MANDATORY               | Description                                                                                                                       | Allowed Values         | Image-build-command                                       | Environment Name   |
|-----------------------|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------|------------------------|-----------------------------------------------------------|--------------------| 
| stardogBaseUrl        | True                    | The base-url of stardog.                                                                                                          | a url                  | --build-arg stardogBaseUrl=http://localhost:8080          | stardog_url        |
| serviceId             | True                    | A unique identifier for this service.                                                                                             | a unique name          | --build-arg serviceId=SDOS                                | serviceIdVar       |
| result                | True                    | The name of result database name.                                                                                                 | a unique name          | --build-arg resultDb=result                               | resultDbNameVar    |
| ofg                   | True                    | The name of Orchestration database name.                                                                                          | a unique name          | --build-arg ofgDb=ofg                                     | ofgDbName          |
| build_version_tag     | False                   | This argument is used to show current sdip version in the swagger page.                                                           | version tag            | --build-arg build_version_tag=4.0.0                       | version_tag        |
| build_environment_tag | False                   | This argument is used to show current sdip environment tag in the swagger page.                                                   | environment name       | --build-arg build_environment_tag=sandbox                 | environment_tag    |
| build_service_url     | False (True with Https) | This argument Sets the base url which swagger will use to curl. This is mandatory is you want to use the swagger page with https. | https base url         | --build-arg build_service_url=https://localhost:8050/sdos | service_url        |
| tpSize                | True                    | The thread pool size to initialize the Thread Executor                                                                            | number                 | --build-arg threadPoolSize=100                            | threadPool_Size    |
| sdosClientSecret      | True                    | SDOS Client Secret used to fetch OBO token for Stardog                                                                            | Azure App secret       | --build-arg sdosClientSecret=<app_secret>                 | sdosClientSecret   |
| stardogClientScope    | True                    | Stardog client scope where SDOS is registered with to get OBO token                                                               | Stardog Client scope   | --build-arg stardogClientScope=<stardog_clientScope>      | stardogClientScope |
| AzureTenantId         | True                    | Tenant id used for Azure communication                                                                                            | Scania Azure tenant id | --build-arg=<tenant_id>                                   | azureTenantId      |

Follow the usage and provide mandatory arguments. 

   Ex. java -jar sdos.jar -id SDOS -ofg <ofg_db_name> -r <result_db_name> -tpSize <threadpool_size> -clientSecret <sdos_clientsecret> -clientScope <clientScope_Stardog> -tenantId <azure_tenant_id>

The application should now start.

After the application is up and running the swagger doc can be found
at http://localhost:8050/sdos/swagger-ui.html

## Support

If you face any issues, find any bugs or have any questions regarding the application the SDP
development team is available through mail [sdos@scania.com](mailto:sdos@scania.com)

## Quick links

[Register App in Azure Active Directory](https://learn.microsoft.com/en-us/entra/identity-platform/quickstart-register-app)

[OBO Flow](https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-on-behalf-of-flow)

[Token signature validation](https://www.voitanos.io/blog/validating-entra-id-generated-oauth-tokens/)


## License
SDOS is licensed under Affero General Public License 3.0 