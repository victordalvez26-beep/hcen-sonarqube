FROM quay.io/wildfly/wildfly:latest-jdk17

USER root

RUN mkdir -p /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main

ADD https://jdbc.postgresql.org/download/postgresql-42.6.0.jar /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/

COPY configure-wildfly.cli /opt/jboss/

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/configure-wildfly.cli

COPY ear/target/hcen.ear /opt/jboss/wildfly/standalone/deployments/

RUN chown -R jboss:jboss /opt/jboss/wildfly

USER jboss

EXPOSE 8080 9990

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-full.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

