package com.xquare.v1userservice.user.handler

import com.xquare.v1userservice.configuration.validate.RequestBodyValidator
import com.xquare.v1userservice.user.User
import com.xquare.v1userservice.user.router.dto.getuser.GetUserByAccountIdResponse
import com.xquare.v1userservice.user.router.dto.saveuser.CreateUserRequest
import com.xquare.v1userservice.user.router.dto.signin.SignInRequest
import com.xquare.v1userservice.user.saveuser.api.CreateUserApi
import com.xquare.v1userservice.user.saveuser.api.GetUserInformationService
import com.xquare.v1userservice.user.saveuser.service.CreatUserDomainRequest
import com.xquare.v1userservice.user.signin.api.UserSignInApi
import com.xquare.v1userservice.user.signin.service.SignInDomainRequest
import java.net.URI
import java.util.UUID
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait

@Component
class UserHandler(
    private val createUserApi: CreateUserApi,
    private val getUserInformationService: GetUserInformationService,
    private val requestBodyValidator: RequestBodyValidator,
    private val userSignInApi: UserSignInApi
) {
    suspend fun saveUserHandler(serverRequest: ServerRequest): ServerResponse {
        val requestBody: CreateUserRequest = serverRequest.getCreateUserRequestBody()
        requestBodyValidator.validate(requestBody)

        val domainRequest = requestBody.toDomainRequest()
        createUserApi.saveUser(domainRequest)

        return ServerResponse.created(URI("/users")).buildAndAwait()
    }

    private suspend fun ServerRequest.getCreateUserRequestBody() =
        this.bodyToMono<CreateUserRequest>().awaitSingle()

    private fun CreateUserRequest.toDomainRequest() = CreatUserDomainRequest(
        accountId = this.accountId,
        verificationCode = this.verificationCode,
        profileFileName = this.profileFileName,
        password = this.password
    )

    suspend fun checkSignInAvailableAndRespondUserInformation(serverRequest: ServerRequest): ServerResponse {
        val signInRequest = serverRequest.getSignInRequestBody()
        val domainRequest = signInRequest.toDomainRequest()
        val user = userSignInApi.userSignIn(domainRequest)
        val userResponseDto = user.toGetUserByAccountIdResponseDto()
        return ServerResponse.ok().bodyValueAndAwait(userResponseDto)
    }

    private suspend fun ServerRequest.getSignInRequestBody() =
        this.bodyToMono<SignInRequest>().awaitSingle()

    private fun SignInRequest.toDomainRequest() =
        SignInDomainRequest(
            accountId = this.accountId,
            password = this.password
        )

    suspend fun getUserByIdHandler(serverRequest: ServerRequest): ServerResponse {
        val userId = serverRequest.pathVariable("userId")
        val user = getUserInformationService.getUserById(UUID.fromString(userId))
        val userResponseDto = user.toGetUserByAccountIdResponseDto()
        return ServerResponse.ok().bodyValueAndAwait(userResponseDto)
    }

    suspend fun getUserByAccountIdHandler(serverRequest: ServerRequest): ServerResponse {
        val accountId = serverRequest.pathVariable("accountId")
        val user = getUserInformationService.getUserByAccountId(accountId)
        val userResponseDto = user.toGetUserByAccountIdResponseDto()
        return ServerResponse.ok().bodyValueAndAwait(userResponseDto)
    }

    private fun User.toGetUserByAccountIdResponseDto() =
        GetUserByAccountIdResponse(
            accountId = this.accountId,
            password = this.password,
            name = this.name,
            profileFileName = this.profileFileName,
            classNum = this.classNum,
            grade = this.grade,
            num = this.num,
            birthDay = this.birthDay,
            id = this.id
        )
}
