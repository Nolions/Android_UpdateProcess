package tw.nolions.updateprocess

import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSink
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tw.nolions.updateprocess.Model.*
import java.io.File
import java.io.RandomAccessFile


class Repository(private var listener: EventListener, api: String) {
    companion object {
        const val TAG = "UpdateProcess"
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(api)
        .client(getHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun getHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .retryOnConnectionFailure(false)
            .cache(null)
        return httpClient.build()
    }

    suspend fun login(siteCode: String, body: LoginReq): RespDataModel<LoginResp> {
        val result = retrofit.create(Service::class.java).login(
            siteCode,
            Gson().toJson(body).toRequestBody("application/json; charset=utf-8".toMediaType())
        )

        return RespDataModel(
            result.isSuccessful,
            result.code(),
            null,
            result.body(),
            result.errorBody()?.let {
                val fromJson = Gson().fromJson(it.string(), RespDataModel.Error::class.java)
                fromJson
            }
        )
    }

    private fun requestPlus(
        file: File,
        mediaType: MediaType? = "image/jpg".toMediaTypeOrNull()
    ): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return mediaType
            }

            override fun writeTo(sink: BufferedSink) {
                val randomAccessFile = RandomAccessFile(file, "rw")
                var progress = 0
                var totalLength: Long = 0
                var currentUpLength: Long = 0
                if (totalLength == 0L) {
                    totalLength = randomAccessFile.length()
                }
                val bytes = ByteArray(2048)
                var len = 0

                try {
                    while (randomAccessFile.read(bytes).also { len = it } != -1) {
                        sink.write(bytes, 0, len)
                        currentUpLength += len
                        progress = (currentUpLength.toDouble() / totalLength.toDouble() * 100).toInt()
                        Log.d("TAG", "writeTo: total:$totalLength, current:$currentUpLength, progress: $progress")

                        listener.updateProcess(progress)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "update fail")
                } finally {
                    randomAccessFile.close()
                    Log.d(TAG, "update end, flow close")
                }
            }
        }
    }

    suspend fun updateCover(siteCode: String, token: String, file: File): RespDataModel<CoverResp> {
        val result = retrofit.create(Service::class.java).updateCover(
            siteCode, "Bearer $token", MultipartBody.Part.createFormData(
                "cover",
                file.name,
                requestPlus(file, "image/jpg".toMediaTypeOrNull())
            )
        )

        return RespDataModel(
            result.isSuccessful,
            result.code(),
            null,
            result.body(),
            result.errorBody()?.let {
                val fromJson = Gson().fromJson(it.string(), RespDataModel.Error::class.java)
                fromJson
            }
        )
    }

    interface EventListener {
        fun updateProcess(progress: Int)
    }
}