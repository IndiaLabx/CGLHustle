package com.cglhustle.core.network

import com.cglhustle.core.network.auth.AuthTokenProvider

class FakeAuthTokenProvider : AuthTokenProvider {
    var tokenToReturn: String? = "fake_token"
    override suspend fun getLatestAccessToken(): String? = tokenToReturn
}
