import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeedAppMovies {
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3308/movie_recommend_system_cqupt?useSSL=false&useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "root";
    private static final String DATA_FILE = "scripts/app_movie_seed_30.json";
    private static final Pattern FIELD_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        List<MovieSeed> seeds = loadSeeds(new File(DATA_FILE));
        if (seeds.size() != 30) {
            throw new IllegalStateException("Seed file should contain 30 movies, actual: " + seeds.size());
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);

            Map<String, Long> typeIds = ensureTypes(conn, seeds);
            Map<String, Long> regionIds = ensureRegions(conn, seeds);
            int inserted = insertMovies(conn, seeds, typeIds, regionIds);

            conn.commit();

            int total = queryInt(conn, "SELECT COUNT(*) FROM app_movie");
            int relTotal = queryInt(conn, "SELECT COUNT(*) FROM app_movie_type_rel");
            System.out.println("INSERTED=" + inserted);
            System.out.println("APP_MOVIE_TOTAL=" + total);
            System.out.println("APP_MOVIE_TYPE_REL_TOTAL=" + relTotal);
        }
    }

    private static Map<String, Long> ensureTypes(Connection conn, List<MovieSeed> seeds) throws Exception {
        Set<String> types = new LinkedHashSet<String>();
        for (MovieSeed seed : seeds) {
            types.add(seed.type);
        }
        Map<String, Long> ids = new LinkedHashMap<String, Long>();
        for (String type : types) {
            Long existingId = queryOptionalLong(conn, "SELECT id FROM app_movie_type WHERE type_name = ?", type);
            if (existingId != null) {
                ids.put(type, existingId);
                continue;
            }
            long id = insertAndReturnId(conn,
                "INSERT INTO app_movie_type (type_name, source_note, created_at, updated_at) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                type, "seeded by SeedAppMovies");
            ids.put(type, id);
        }
        return ids;
    }

    private static Map<String, Long> ensureRegions(Connection conn, List<MovieSeed> seeds) throws Exception {
        Set<String> regions = new LinkedHashSet<String>();
        for (MovieSeed seed : seeds) {
            regions.add(seed.region);
        }
        Map<String, Long> ids = new LinkedHashMap<String, Long>();
        for (String region : regions) {
            String normalized = normalize(region);
            Long existingId = queryOptionalLong(conn, "SELECT id FROM app_movie_region WHERE normalized_name = ?", normalized);
            if (existingId != null) {
                ids.put(region, existingId);
                continue;
            }
            long id = insertAndReturnId(conn,
                "INSERT INTO app_movie_region (region_name, normalized_name, source_note, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                region, normalized, "seeded by SeedAppMovies");
            ids.put(region, id);
        }
        return ids;
    }

    private static int insertMovies(Connection conn, List<MovieSeed> seeds, Map<String, Long> typeIds, Map<String, Long> regionIds) throws Exception {
        int inserted = 0;
        for (MovieSeed seed : seeds) {
            Long exists = queryOptionalLong(conn, "SELECT id FROM app_movie WHERE title = ?", seed.title);
            if (exists != null) {
                continue;
            }

            long movieId = insertAndReturnId(conn,
                "INSERT INTO app_movie (" +
                    "movie_type_id, legacy_type_name, title, poster_urls_csv, region_name, region_id, release_date, director_name, cast_names, synopsis, detail_html, " +
                    "like_count, dislike_count, click_count, comment_count, favorite_count, total_score, last_clicked_at, created_at, updated_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, 0, 0, ?, ?, ?)",
                typeIds.get(seed.type),
                seed.type,
                seed.title,
                "",
                seed.region,
                regionIds.get(seed.region),
                Date.valueOf(LocalDate.parse(seed.releaseDate)),
                seed.director,
                seed.cast,
                seed.synopsis,
                seed.detailHtml,
                null,
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now()));

            long typeRelExists = queryCount(conn,
                "SELECT COUNT(*) FROM app_movie_type_rel WHERE movie_id = ? AND type_id = ?",
                movieId, typeIds.get(seed.type));
            if (typeRelExists == 0) {
                execute(conn,
                    "INSERT INTO app_movie_type_rel (movie_id, type_id, is_primary, sort_order, created_at, updated_at) VALUES (?, ?, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    movieId, typeIds.get(seed.type));
            }
            inserted++;
        }
        return inserted;
    }

    private static List<MovieSeed> loadSeeds(File file) throws Exception {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line).append('\n');
            }
        }

        String content = json.toString();
        List<MovieSeed> seeds = new ArrayList<MovieSeed>();
        Matcher objectMatcher = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL).matcher(content);
        while (objectMatcher.find()) {
            String objectBody = objectMatcher.group(1);
            Map<String, String> fields = new LinkedHashMap<String, String>();
            Matcher fieldMatcher = FIELD_PATTERN.matcher(objectBody);
            while (fieldMatcher.find()) {
                fields.put(fieldMatcher.group(1), unescape(fieldMatcher.group(2)));
            }
            seeds.add(new MovieSeed(
                fields.get("title"),
                fields.get("type"),
                fields.get("region"),
                fields.get("releaseDate"),
                fields.get("director"),
                fields.get("cast"),
                fields.get("synopsis"),
                fields.get("detailHtml")
            ));
        }
        return seeds;
    }

    private static String unescape(String value) {
        return value
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static int queryInt(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static long queryCount(Connection conn, String sql, Object... params) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static Long queryOptionalLong(Connection conn, String sql, Object... params) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return null;
            }
        }
    }

    private static void execute(Connection conn, String sql, Object... params) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            ps.executeUpdate();
        }
    }

    private static long insertAndReturnId(Connection conn, String sql, Object... params) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new IllegalStateException("Insert did not return generated key");
    }

    private static void bind(PreparedStatement ps, Object... params) throws Exception {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param == null) {
                ps.setObject(i + 1, null);
            } else {
                ps.setObject(i + 1, param);
            }
        }
    }

    private static final class MovieSeed {
        private final String title;
        private final String type;
        private final String region;
        private final String releaseDate;
        private final String director;
        private final String cast;
        private final String synopsis;
        private final String detailHtml;

        private MovieSeed(String title, String type, String region, String releaseDate, String director, String cast, String synopsis, String detailHtml) {
            this.title = title;
            this.type = type;
            this.region = region;
            this.releaseDate = releaseDate;
            this.director = director;
            this.cast = cast;
            this.synopsis = synopsis;
            this.detailHtml = detailHtml;
        }
    }
}
