package com.kape.httpclient.data

import com.kape.httpclient.domain.GetWebsiteDownloadLink
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GetWebsiteDownloadLinkImpl : GetWebsiteDownloadLink {
    override fun invoke(): Flow<String> = flow {
        val client = HttpClient(OkHttp)
        val response =
            client.get(urlString = "https://privateinternetaccess.com/api/client/android/latest_release")
                .bodyAsText()
        val result = Json.decodeFromString<UpdateClientResponse>(response)
        emit(result.url)
    }
}

@Serializable
data class UpdateClientResponse(
    @SerialName("version_name")
    val versionName: String,
    @SerialName("version_code")
    val versionCode: String,
    val sha256: String,
    val url: String,
)