ARG CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX=""

FROM ${CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX}${CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX:+/}bitnami/postgresql:16-debian-12

COPY --chown=1001:1001 --chmod=777 test-schema.sql /docker-entrypoint-initdb.d/
