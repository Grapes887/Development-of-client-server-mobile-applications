package com.example.module6.server5.data.repository

import com.example.module6.server5.domain.model.ServerLaureate
import com.example.module6.server5.domain.model.ServerPrize
import com.example.module6.server5.domain.repository.PrizeRepository
import javax.sql.DataSource

class JdbcPrizeRepository(
    private val dataSource: DataSource
) : PrizeRepository {

    override fun getPrizes(year: String?, category: String?): List<ServerPrize> {
        val baseSql = buildString {
            append(
                """
                SELECT p.id, p.award_year, p.category, p.category_title, p.detail_link
                FROM prizes p
                WHERE 1=1
                """.trimIndent()
            )
            if (!year.isNullOrBlank()) append(" AND p.award_year = ?")
            if (!category.isNullOrBlank()) append(" AND LOWER(p.category) = LOWER(?)")
            append(" ORDER BY p.award_year DESC, p.category ASC")
        }

        return dataSource.connection.use { connection ->
            connection.prepareStatement(baseSql).use { statement ->
                var index = 1
                if (!year.isNullOrBlank()) statement.setString(index++, year)
                if (!category.isNullOrBlank()) statement.setString(index, category)
                statement.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            val prizeId = rs.getInt("id")
                            add(
                                ServerPrize(
                                    id = prizeId,
                                    awardYear = rs.getString("award_year"),
                                    category = rs.getString("category"),
                                    categoryTitle = rs.getString("category_title"),
                                    detailLink = rs.getString("detail_link"),
                                    laureates = getLaureatesByPrizeId(connection, prizeId)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun getPrizeById(prizeId: Int): ServerPrize? {
        val sql =
            "SELECT id, award_year, category, category_title, detail_link FROM prizes WHERE id = ? LIMIT 1"
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, prizeId)
                statement.executeQuery().use { rs ->
                    if (!rs.next()) return@use null
                    ServerPrize(
                        id = rs.getInt("id"),
                        awardYear = rs.getString("award_year"),
                        category = rs.getString("category"),
                        categoryTitle = rs.getString("category_title"),
                        detailLink = rs.getString("detail_link"),
                        laureates = getLaureatesByPrizeId(connection, prizeId)
                    )
                }
            }
        }
    }

    override fun getPrize(year: String, category: String): ServerPrize? {
        return getPrizes(year, category).firstOrNull()
    }

    override fun getLaureates(year: String, category: String): List<ServerLaureate> {
        return getPrize(year, category)?.laureates.orEmpty()
    }

    override fun getLaureateById(id: String): ServerLaureate? {
        val sql =
            "SELECT id, prize_id, full_name, portion, motivation, birth_location, portrait_url FROM laureates WHERE id = ? LIMIT 1"
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { rs ->
                    if (!rs.next()) return@use null
                    rs.toLaureate()
                }
            }
        }
    }

    override fun getFavoritePrizes(userId: Int): List<ServerPrize> {
        val sql =
            """
            SELECT p.id, p.award_year, p.category, p.category_title, p.detail_link
            FROM user_prizes up
            JOIN prizes p ON p.id = up.prize_id
            WHERE up.user_id = ?
            ORDER BY up.added_at DESC
            """.trimIndent()
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, userId)
                statement.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            val prizeId = rs.getInt("id")
                            add(
                                ServerPrize(
                                    id = prizeId,
                                    awardYear = rs.getString("award_year"),
                                    category = rs.getString("category"),
                                    categoryTitle = rs.getString("category_title"),
                                    detailLink = rs.getString("detail_link"),
                                    laureates = getLaureatesByPrizeId(connection, prizeId)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun addFavoritePrize(userId: Int, prizeId: Int): Boolean {
        val sql =
            """
            INSERT INTO user_prizes(user_id, prize_id)
            VALUES (?, ?)
            ON CONFLICT (user_id, prize_id) DO NOTHING
            """.trimIndent()
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, userId)
                statement.setInt(2, prizeId)
                statement.executeUpdate() > 0
            }
        }
    }

    override fun removeFavoritePrize(userId: Int, prizeId: Int): Boolean {
        val sql = "DELETE FROM user_prizes WHERE user_id = ? AND prize_id = ?"
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, userId)
                statement.setInt(2, prizeId)
                statement.executeUpdate() > 0
            }
        }
    }

    private fun getLaureatesByPrizeId(
        connection: java.sql.Connection,
        prizeId: Int
    ): List<ServerLaureate> {
        val sql =
            """
            SELECT id, prize_id, full_name, portion, motivation, birth_location, portrait_url
            FROM laureates
            WHERE prize_id = ?
            ORDER BY id
            """.trimIndent()
        return connection.prepareStatement(sql).use { statement ->
            statement.setInt(1, prizeId)
            statement.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(rs.toLaureate())
                    }
                }
            }
        }
    }
}

private fun java.sql.ResultSet.toLaureate(): ServerLaureate {
    return ServerLaureate(
        id = getString("id"),
        prizeId = getInt("prize_id"),
        fullName = getString("full_name"),
        portion = getString("portion"),
        motivation = getString("motivation"),
        birthLocation = getString("birth_location"),
        portraitUrl = getString("portrait_url")
    )
}
