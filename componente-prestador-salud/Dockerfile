FROM quay.io/wildfly/wildfly:latest-jdk17

USER root

COPY ear/target/prestador-salud-mockup.ear /opt/jboss/wildfly/standalone/deployments/

RUN chown -R jboss:jboss /opt/jboss/wildfly

USER jboss

EXPOSE 8080 9990

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

