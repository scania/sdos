
FROM openjdk:17


ARG configurationUrl
ARG serviceId
ARG build_version_tag
ARG build_environment_tag
ARG build_service_url

ARG stardogBaseUrl
ARG resultDb
ARG ofgDb
ARG threadPoolSize
ARG sdosClientSecret
ARG stardogClientScope
ARG zureTenantId

ENV service_url=$build_service_url
ENV configurationUrlVar=$configurationUrl
ENV serviceIdVar=$serviceId
ENV version_tag=$build_version_tag
ENV environment_tag=$build_environment_tag

ENV stardog_url=$stardogBaseUrl
ENV resultDbNameVar=$resultDb
ENV ofgDbName=$ofgDb
ENV threadPool_Size=$threadPoolSize
ENV sdosClientSecret=$sdosClientSecret
ENV stardogClientScope=$stardogClientScope
ENV azureTenantId=$azureTenantId



RUN cd /opt && mkdir apps

COPY ./build/libs/sdos.jar /opt/apps/sdos.jar

CMD java -Dlog4j2.formatMsgNoLookups=true -DjvmArgs="-Xms10g -Xmx12g -Xss6g" -jar /opt/apps/sdos.jar -b $stardog_url \
         -r $resultDbNameVar -ofg $ofgDbName -id $serviceIdVar -tpSize $threadPool_Size \
         -clientSecret $sdosClientSecret -clientScope $stardogClientScope -tenantId $azureTenantId;

EXPOSE 8080
