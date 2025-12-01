package uy.edu.tse.hcen.politicas.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Singleton EJB que se ejecuta al inicio del despliegue para realizar migraciones
 * y actualizaciones del esquema de base de datos del servicio de políticas.
 */
@Singleton
@Startup
public class DatabaseInitializer {

    private static final Logger LOG = Logger.getLogger(DatabaseInitializer.class.getName());

    @Resource(lookup = "java:jboss/datasources/ExampleDS")
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        LOG.info("🚀 DatabaseInitializer (Políticas) - Verificando migraciones...");
        try {
            migrateDatabase();
            LOG.info("✅ Migraciones completadas exitosamente");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "❌ Error en migraciones: " + e.getMessage(), e);
        }
    }

    private void migrateDatabase() throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            // Migración: Agregar columna clinica_autorizada si no existe
            addColumnIfNotExists(c, "politica_acceso", "clinica_autorizada", "VARCHAR(100)");
        }
    }

    /**
     * Agrega una columna a una tabla si no existe.
     */
    private void addColumnIfNotExists(Connection conn, String tableName, String columnName, String columnDefinition) throws SQLException {
        // Verificar si la columna ya existe
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            if (columns.next()) {
                LOG.info(String.format("  ✅ Columna %s.%s ya existe, omitiendo migración", tableName, columnName));
                return;
            }
        }

        // La columna no existe, agregarla
        String alterTable = String.format(
            "ALTER TABLE %s ADD COLUMN %s %s",
            tableName, columnName, columnDefinition
        );

        try (PreparedStatement ps = conn.prepareStatement(alterTable)) {
            ps.execute();
            LOG.info(String.format("  ✅ Columna %s.%s agregada exitosamente", tableName, columnName));
        } catch (SQLException e) {
            // Si la columna ya existe (por ejemplo, si se creó entre la verificación y el ALTER),
            // solo loguear una advertencia y continuar
            String errorMsg = e.getMessage().toLowerCase();
            if (errorMsg.contains("already exists") || 
                errorMsg.contains("duplicate") || 
                errorMsg.contains("duplicate column name")) {
                LOG.warning(String.format("  ⚠️ Columna %s.%s ya existe (creada concurrentemente o en migración anterior), omitiendo", tableName, columnName));
                // No lanzar excepción, solo continuar
                return;
            } else {
                throw e;
            }
        }
    }
}

