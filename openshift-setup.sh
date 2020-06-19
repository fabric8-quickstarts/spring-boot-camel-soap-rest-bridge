#!/bin/bash
minishift start --memory 8GB --cpus 4 --timezone $1
oc login -u system:admin
oc adm policy add-cluster-role-to-user cluster-admin developer
oc login -u developer
oc project openshift
oc create secret docker-registry camel-bridge --docker-server=registry.redhat.io --docker-username=$2 --docker-password=$3
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

oc new-project 3scale
oc create secret docker-registry threescale-registry-auth --docker-server=registry.redhat.io --docker-username=$2 --docker-password=$3
oc secrets link default threescale-registry-auth --for=pull
oc secrets link builder threescale-registry-auth
CLUSTER_IP=`minishift ip`
oc new-app    --param WILDCARD_DOMAIN="${CLUSTER_IP}.nip.io"       -f https://raw.githubusercontent.com/3scale/3scale-amp-openshift-templates/2.8.0.GA/amp/amp-eval-tech-preview.yml
