package com.xquare.v1userservice.user.spi

import com.xquare.v1userservice.configuration.property.ServiceProperties
import com.xquare.v1userservice.user.exceptions.PointRequestFailedException
import com.xquare.v1userservice.user.spi.dtos.PointResponse
import com.xquare.v1userservice.user.spi.dtos.UserPointResponse
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Repository
class PointSpiImpl(
    private val webClient: WebClient,
    private val serviceProperties: ServiceProperties
) : PointSpi {
    override suspend fun getUserPoint(userId: UUID): PointResponse {
        return webClient.get().uri {
            it.scheme("https")
                .host(serviceProperties.baseHost)
                .path("/point/{userId}")
                .build(userId)
        }.retrieve()
            .onStatus(HttpStatus::isError) {
                throw PointRequestFailedException("Failed request to get user point", it.rawStatusCode())
            }
            .awaitBody<UserPointResponse>().let {
                PointResponse(
                    goodPoint = it.goodPoint,
                    badPoint = it.badPoint
                )
            }
    }
}