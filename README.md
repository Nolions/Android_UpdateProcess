# UpdateProcess

okhttp3 + retrofit2 上傳圖片，並取得即時上傳進度

## Setting

複製 `local.properties.example` => `local.properties`

```
cp local.properties.example local.properties
```

修改local.properties中的`site.code` & `paras.api` 值


## 改寫 okhttp3.RequestBody

```
    fun requestBody(file: File,  mediaType: MediaType? = "image/jpg".toMediaTypeOrNull()): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return mediaType
            }
            override fun writeTo(sink: BufferedSink) {
                val randomAccessFile = RandomAccessFile(file, "rw")
                var progress = 0
                var totalLength: Long = 0
                var currentUpLength: Long = 0
                val bytes = ByteArray(2048)
                var len = 0

                if (totalLength == 0L) {
                    totalLength = randomAccessFile.length()
                }

                try {
                    while (randomAccessFile.read(bytes).also { len = it } != -1) {
                        sink.write(bytes, 0, len)
                        currentUpLength += len
                        progress = (currentUpLength.toDouble() / totalLength.toDouble() * 100).toInt()
                        Log.d(TAG, "writeTo: total=$totalLength, current=$currentUpLength, progress: progress: $progress")
                        // TODO 可以透過callback或是RXJava方式將進度回傳到main thread
                    }
                } catch (e: Exception) {
                     Log.e("TAG", "update fail")
                } finally {
                    randomAccessFile.close()
                    Log.d(TAG, "update end, flow close")
                }
            }
        }
    }
}
```
