package mysqldbhelper;

import bobthebuildtool.pojos.buildfile.Project;
import bobthebuildtool.pojos.error.VersionTooOld;
import org.flywaydb.core.Flyway;

import java.nio.file.Files;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import static bobthebuildtool.services.Update.requireBobVersion;

public enum BobPlugin {;

    public static void installPlugin(final Project project) throws VersionTooOld {
        requireBobVersion("5");
        project.addTask("localdb", "Creates a localdb from the migration scripts", BobPlugin::createLocalDB);
    }

    private static void createLocalDB(final Project project, final Map<String, String> environment) throws SQLException {
        try (final var conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/", "root", "")) {
            conn.createStatement().executeUpdate("CREATE DATABASE " + project.config.name);
        }

        final String[] locations = project.getResourceDirectories()
            .stream()
            .map(path -> path.resolve("db/migration"))
            .filter(Files::isDirectory)
            .map(path -> "filesystem:" + path.toAbsolutePath())
            .toArray(String[]::new);

        Flyway.configure()
            .dataSource("jdbc:mariadb://localhost:3306/"+ project.config.name, "root", "")
            .locations(locations)
            .load()
            .migrate();
    }

}
