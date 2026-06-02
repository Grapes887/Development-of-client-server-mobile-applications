package com.example.module6.server5.data.repository

import com.example.module6.server5.domain.model.ServerUser
import com.example.module6.server5.domain.repository.UserRepository
import javax.sql.DataSource

class JdbcUserRepository(
    private val dataSource: DataSource
) : UserRepository {

    override fun findByUsername(username: String): ServerUser? {
        val sql = "SELECT id, username, password_hash, role FROM users WHERE username = ? LIMIT 1"
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, username)
                statement.executeQuery().use { rs ->
                    if (!rs.next()) return@use null
                    rs.toUser()
                }
            }
        }
    }

    override fun findById(id: Int): ServerUser? {
        val sql = "SELECT id, username, password_hash, role FROM users WHERE id = ? LIMIT 1"
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, id)
                statement.executeQuery().use { rs ->
                    if (!rs.next()) return@use null
                    rs.toUser()
                }
            }
        }
    }
}

private fun java.sql.ResultSet.toUser(): ServerUser {
    return ServerUser(
        id = getInt("id"),
        username = getString("username"),
        passwordHash = getString("password_hash"),
        role = getString("role")
    )
}
