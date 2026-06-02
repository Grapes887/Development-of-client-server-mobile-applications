package com.example.module6.server5.data.database

import com.example.module6.server5.domain.model.SeedPrize
import com.example.module6.server5.security.PasswordHasher
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.sql.Connection
import java.util.Properties
import javax.sql.DataSource

object DatabaseFactory {
    fun createDataSource(): HikariDataSource {
        val settings = resolveDatabaseSettings()
        val config = HikariConfig().apply {
            jdbcUrl = settings.jdbcUrl
            username = settings.username
            password = settings.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 4
            isAutoCommit = true
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    fun initDatabase(dataSource: DataSource) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            createTables(connection)
            seedUsers(connection)
            seedPrizes(connection)
            connection.commit()
        }
    }

    private fun createTables(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(100) UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role VARCHAR(40) NOT NULL
                )
                """.trimIndent()
            )
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS prizes (
                    id SERIAL PRIMARY KEY,
                    award_year VARCHAR(10) NOT NULL,
                    category VARCHAR(80) NOT NULL,
                    category_title TEXT NOT NULL,
                    detail_link TEXT NOT NULL,
                    UNIQUE(award_year, category)
                )
                """.trimIndent()
            )
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS laureates (
                    id VARCHAR(40) PRIMARY KEY,
                    prize_id INTEGER NOT NULL REFERENCES prizes(id) ON DELETE CASCADE,
                    full_name TEXT NOT NULL,
                    portion VARCHAR(20) NOT NULL,
                    motivation TEXT NOT NULL,
                    birth_location TEXT NOT NULL,
                    portrait_url TEXT
                )
                """.trimIndent()
            )
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS user_prizes (
                    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    prize_id INTEGER NOT NULL REFERENCES prizes(id) ON DELETE CASCADE,
                    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY(user_id, prize_id)
                )
                """.trimIndent()
            )
        }
    }

    private fun seedUsers(connection: Connection) {
        val usersCount = connection.createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM users").use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
        if (usersCount > 0) return

        val sql = "INSERT INTO users(username, password_hash, role) VALUES (?, ?, ?)"
        connection.prepareStatement(sql).use { statement ->
            listOf(
                Triple("student", "student123", "user"),
                Triple("admin", "admin123", "admin")
            ).forEach { (username, password, role) ->
                statement.setString(1, username)
                statement.setString(2, PasswordHasher.hash(password))
                statement.setString(3, role)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }

    private fun seedPrizes(connection: Connection) {
        val prizesCount = connection.createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM prizes").use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
        if (prizesCount > 0) return

        SeedPrize.entries.forEach { seedPrize ->
            val prizeId = connection.prepareStatement(
                """
                INSERT INTO prizes(award_year, category, category_title, detail_link)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, seedPrize.awardYear)
                statement.setString(2, seedPrize.category)
                statement.setString(3, seedPrize.categoryTitle)
                statement.setString(4, seedPrize.detailLink)
                statement.executeQuery().use { rs ->
                    rs.next()
                    rs.getInt("id")
                }
            }

            connection.prepareStatement(
                """
                INSERT INTO laureates(id, prize_id, full_name, portion, motivation, birth_location, portrait_url)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
            ).use { statement ->
                seedPrize.laureates.forEach { laureate ->
                    statement.setString(1, laureate.id)
                    statement.setInt(2, prizeId)
                    statement.setString(3, laureate.fullName)
                    statement.setString(4, laureate.portion)
                    statement.setString(5, laureate.motivation)
                    statement.setString(6, laureate.birthLocation)
                    statement.setString(7, laureate.portraitUrl)
                    statement.addBatch()
                }
                statement.executeBatch()
            }
        }
    }

    private fun resolveDatabaseSettings(): DatabaseSettings {
        val rawUrl = System.getenv("DATABASE_URL")
            ?: loadFromLocalProperties()
            ?: error("DATABASE_URL not found in environment or local.properties")
        if (rawUrl.startsWith("jdbc:")) {
            return DatabaseSettings(
                jdbcUrl = rawUrl,
                username = null,
                password = null
            )
        }

        val uri = URI(rawUrl)
        val userInfo = uri.userInfo?.split(":", limit = 2).orEmpty()
        val host = checkNotNull(uri.host) { "Database host is missing" }
        val databasePath = uri.path
        val queryPart = uri.query?.let { "?$it" }.orEmpty()

        return DatabaseSettings(
            jdbcUrl = "jdbc:postgresql://$host$databasePath$queryPart",
            username = userInfo.getOrNull(0),
            password = userInfo.getOrNull(1)
        )
    }

    private fun loadFromLocalProperties(): String? {
        val candidates = listOf(
            File("local.properties"),
            File("../local.properties")
        )
        candidates.forEach { file ->
            if (!file.exists()) return@forEach
            val value = Properties().apply {
                FileInputStream(file).use(::load)
            }.getProperty("DATABASE_URL")
            if (!value.isNullOrBlank()) return value
        }
        return null
    }
}

private data class DatabaseSettings(
    val jdbcUrl: String,
    val username: String?,
    val password: String?
)
