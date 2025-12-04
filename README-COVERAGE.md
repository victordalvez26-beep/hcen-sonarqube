# Configuración de Cobertura de Código con JaCoCo y SonarQube

Este documento explica cómo está configurada la cobertura de código en el proyecto HCEN.

## Configuración de JaCoCo

JaCoCo está configurado en el POM padre (`hcen/pom.xml`) y se ejecuta automáticamente cuando se ejecutan los tests.

### Reportes generados

1. **Reporte HTML individual por módulo**: `target/site/jacoco/index.html`
2. **Reporte XML para SonarQube**: `target/site/jacoco/jacoco.xml`
3. **Reporte agregado consolidado**: `target/site/jacoco-aggregate/jacoco.xml`

## Configuración de SonarQube

SonarQube está configurado para leer los reportes XML de JaCoCo desde:

```properties
sonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml,**/target/site/jacoco-aggregate/jacoco.xml
```

## Workflow de GitHub Actions

El workflow ejecuta los tests con `mvn clean install` (sin `-DskipTests`) para:

1. Compilar el código
2. Ejecutar los tests
3. Generar los reportes de cobertura con JaCoCo
4. Enviar los reportes a SonarQube

## Archivos excluidos de la cobertura

Los siguientes tipos de archivos están excluidos del cálculo de cobertura:

- Tests (`**/test/**`, `**/*Test.java`, `**/*Tests.java`)
- DTOs (`**/dto/**`, `**/*DTO.java`)
- Modelos (`**/model/**`)
- Entidades (`**/entity/**`)
- Converters (`**/converter/**`)
- Configuraciones (`**/config/**`)
- Excepciones (`**/*Exception.java`)

## Verificar cobertura localmente

Para verificar la cobertura localmente:

```bash
# Ejecutar tests y generar reportes
cd hcen
mvn clean install

# Ver reporte HTML
# Abrir en navegador: hcen/target/site/jacoco-aggregate/index.html
```

## Troubleshooting

### Cobertura 0.0% en SonarQube

**Causas posibles:**

1. **Tests no se ejecutan**: Verificar que el workflow no use `-DskipTests`
2. **Reportes XML no se generan**: Verificar que JaCoCo esté configurado correctamente
3. **Rutas incorrectas**: Verificar que `sonar.coverage.jacoco.xmlReportPaths` apunte a las rutas correctas
4. **Tests fallan**: Si los tests fallan, JaCoCo no genera reportes

**Solución:**

1. Verificar los logs del workflow de GitHub Actions
2. Buscar mensajes como "Found XML: ..." en el paso de debugging
3. Verificar que los archivos `jacoco.xml` existan en `target/site/jacoco/`
4. Asegurarse de que los tests se ejecuten exitosamente

### Reportes XML no encontrados

Si SonarQube no encuentra los reportes XML:

1. Verificar que los tests se ejecuten: `mvn test` (sin `-DskipTests`)
2. Verificar que los reportes se generen: buscar `target/site/jacoco/jacoco.xml`
3. Verificar las rutas en `sonar-project.properties`
4. Usar rutas absolutas si es necesario

### Tests fallan en CI/CD

Si los tests fallan en GitHub Actions:

1. Revisar los logs del workflow
2. Ejecutar los tests localmente para reproducir el error
3. Considerar usar `continue-on-error: true` temporalmente para módulos opcionales
4. Asegurarse de que todas las dependencias estén disponibles

## Mejores prácticas

1. **Ejecutar tests siempre**: No usar `-DskipTests` en el workflow de CI/CD
2. **Revisar cobertura regularmente**: Mantener un mínimo de cobertura (ej: 60-80%)
3. **Excluir archivos apropiados**: DTOs, modelos y excepciones no necesitan cobertura
4. **Integración continua**: Ejecutar tests y análisis de cobertura en cada PR

