package tw.nolions.updateprocess

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import tw.nolions.updateprocess.Model.CoverResp
import tw.nolions.updateprocess.Model.LoginResp

interface Service {

    @POST("login-live")
    suspend fun login(
        @Header("X-Site-Code") siteCode: String,
        @Body body: RequestBody
    ): Response<LoginResp>

    @Multipart
    @POST("live/member/cover")
    suspend fun updateCover(
        @Header("X-Site-Code") siteCode: String,
        @Header("authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<CoverResp>
}