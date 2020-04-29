#!/bin/bash

oc login -u system:admin
oc adm policy add-cluster-role-to-user cluster-admin developer
oc login -u developer
oc project openshift
oc create secret docker-registry camel-bridge --docker-server=registry.redhat.io --docker-username="6340056|yfang" --docker-password="eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiI3MGIyNjA3ZmYyNGY0MDRhOWMzZjU4NzU0YTMxZGU5YiJ9.YK5bJZpUNu0Srbl6bhCaaz1PkDswi8vPYfzVWURWIaYAGn_B0bVJkaKqFfg_4J_9HoaJoV__Rs-wkTG5KXJOMAWN_fvX0GL1TEia8TXzqnCgdkkLmcVwF9b_lUriFSvmoov6xUIvi1LtA5Q5Vm9q8r-nryLXfgwwqY5QMXLVOddbdlT7695cV-z-D8K3Rd9j4tCcwcxMrZMmG5pLZTbPK2RlGmPeI1a_aV7Tcy9SAMLVP6wPw-EbB3m78wwv7C0V3e0K4Yho5A0yjmvXRPYW2ER-Zx981FTtNYi75nzxSPsV6EXg5DqKdXrsoogVq3odEchqJyzLJ6eDfJLZOvY7Z6nM9Yoh5CLP-TvdovilzKCnxDXOCa2BzmzD4W5vjXVDxwGyFVdLOpCtkIrgAm3iu8684DVC1sbwA5gL4qgNM6QnaxSoWHVfnkIii3At0o3IAgE0zsY-EWOsQ6hUFnVyQRVQmrjkwoh8f45ADgP6Y3zvPTq5uxaPSCXV4gniRKKFwxz96uvZoK6dEXw3WLMyGncpD1LGbWQ9lET7a-_huTEgOsFCh6ClS9nf0QABd-LP3BBNMK6q5lawL_6Ri9r3q0kE04fk50qQywO8swed5rjI9TwkS_6FU3UguObRNMdUGHcVRVmayhOtuBDxHi3rzEdDX-xaP2jm-PAdUCFH7RA"
oc secrets link default camel-bridge --for=pull
oc secrets link builder camel-bridge

for resource in sso74-image-stream.json \
  sso74-https.json \
  sso74-postgresql.json \
  sso74-postgresql-persistent.json \
  sso74-x509-https.json \
  sso74-x509-postgresql-persistent.json
do
  oc create -f \
  https://raw.githubusercontent.com/jboss-container-images/redhat-sso-7-openshift-image/sso74-dev/templates/${resource}
done

oc policy add-role-to-user view system:serviceaccount:$(oc project -q):default

oc new-app --template=sso74-x509-postgresql-persistent

BASEURL=https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.0.fuse-760043-redhat-00001
oc create  -f ${BASEURL}/fis-image-streams.json
oc create  -f ${BASEURL}/fis-console-namespace-template.json
oc new-app -f https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.0.fuse-760043-redhat-00001/fis-console-namespace-template.json

for template in eap-camel-amq-template.json  eap-camel-cdi-template.json  eap-camel-cxf-jaxrs-template.json  eap-camel-cxf-jaxws-template.json  eap-camel-jpa-template.json  karaf-camel-amq-template.json  karaf-camel-log-template.json  karaf-camel-rest-sql-template.json  karaf-cxf-rest-template.json  spring-boot-camel-amq-template.json  spring-boot-camel-config-template.json  spring-boot-camel-drools-template.json  spring-boot-camel-infinispan-template.json  spring-boot-camel-rest-sql-template.json  spring-boot-camel-rest-3scale-template.json  spring-boot-camel-template.json  spring-boot-camel-xa-template.json  spring-boot-camel-xml-template.json  spring-boot-cxf-jaxrs-template.json  spring-boot-cxf-jaxws-template.json ;  do  oc create  -f  ${BASEURL}/quickstarts/${template};  done

for template in spring-boot-2-camel-amq-template.json  spring-boot-2-camel-config-template.json  spring-boot-2-camel-drools-template.json  spring-boot-2-camel-infinispan-template.json  spring-boot-2-camel-rest-3scale-template.json  spring-boot-2-camel-rest-sql-template.json  spring-boot-2-camel-template.json  spring-boot-2-camel-xa-template.json  spring-boot-2-camel-xml-template.json  spring-boot-2-cxf-jaxrs-template.json  spring-boot-2-cxf-jaxws-template.json  spring-boot-2-cxf-jaxrs-xml-template.json  spring-boot-2-cxf-jaxws-xml-template.json ;  do oc create  -f  https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.0.fuse-sb2-760039-redhat-00001/quickstarts/${template};  done

oc new-project 3scale
oc create secret docker-registry threescale-registry-auth --docker-server=registry.redhat.io --docker-username="6340056|yfang" --docker-password="eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiI3MGIyNjA3ZmYyNGY0MDRhOWMzZjU4NzU0YTMxZGU5YiJ9.YK5bJZpUNu0Srbl6bhCaaz1PkDswi8vPYfzVWURWIaYAGn_B0bVJkaKqFfg_4J_9HoaJoV__Rs-wkTG5KXJOMAWN_fvX0GL1TEia8TXzqnCgdkkLmcVwF9b_lUriFSvmoov6xUIvi1LtA5Q5Vm9q8r-nryLXfgwwqY5QMXLVOddbdlT7695cV-z-D8K3Rd9j4tCcwcxMrZMmG5pLZTbPK2RlGmPeI1a_aV7Tcy9SAMLVP6wPw-EbB3m78wwv7C0V3e0K4Yho5A0yjmvXRPYW2ER-Zx981FTtNYi75nzxSPsV6EXg5DqKdXrsoogVq3odEchqJyzLJ6eDfJLZOvY7Z6nM9Yoh5CLP-TvdovilzKCnxDXOCa2BzmzD4W5vjXVDxwGyFVdLOpCtkIrgAm3iu8684DVC1sbwA5gL4qgNM6QnaxSoWHVfnkIii3At0o3IAgE0zsY-EWOsQ6hUFnVyQRVQmrjkwoh8f45ADgP6Y3zvPTq5uxaPSCXV4gniRKKFwxz96uvZoK6dEXw3WLMyGncpD1LGbWQ9lET7a-_huTEgOsFCh6ClS9nf0QABd-LP3BBNMK6q5lawL_6Ri9r3q0kE04fk50qQywO8swed5rjI9TwkS_6FU3UguObRNMdUGHcVRVmayhOtuBDxHi3rzEdDX-xaP2jm-PAdUCFH7RA"
oc secrets link default threescale-registry-auth --for=pull
oc secrets link builder threescale-registry-auth
oc new-app    --param WILDCARD_DOMAIN="$(minishift ip).nip.io"       -f https://raw.githubusercontent.com/3scale/3scale-amp-openshift-templates/2.8.0.GA/amp/amp-eval-tech-preview.yml
