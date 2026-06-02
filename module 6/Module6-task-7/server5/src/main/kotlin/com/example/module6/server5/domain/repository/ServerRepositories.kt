package com.example.module6.server5.domain.repository

import com.example.module6.server5.domain.model.ServerLaureate
import com.example.module6.server5.domain.model.ServerPrize
import com.example.module6.server5.domain.model.ServerUser

interface UserRepository {
    fun findByUsername(username: String): ServerUser?

    fun findById(id: Int): ServerUser?
}

interface PrizeRepository {
    fun getPrizes(year: String?, category: String?): List<ServerPrize>

    fun getPrizeById(prizeId: Int): ServerPrize?

    fun getPrize(year: String, category: String): ServerPrize?

    fun getLaureates(year: String, category: String): List<ServerLaureate>

    fun getLaureateById(id: String): ServerLaureate?

    fun getFavoritePrizes(userId: Int): List<ServerPrize>

    fun addFavoritePrize(userId: Int, prizeId: Int): Boolean

    fun removeFavoritePrize(userId: Int, prizeId: Int): Boolean
}
