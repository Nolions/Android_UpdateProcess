package tw.nolions.updateprocess

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.nolions.updateprocess.Model.LoginReq
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), Repository.EventListener {
    private lateinit var mRepo: Repository
    private lateinit var mToken: String
    private lateinit var mSiteCode: String

    companion object {
        const val TAG = "UpdateProcess"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRepo = Repository(this, getString(R.string.PARAS_API))
        mSiteCode = getString(R.string.SITE_CODE)

        login()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                10000 -> {
                    updateCover(resultData!!.data!!)
                }
            }
        }
    }

    override fun updateProcess(progress: Int) {
        Log.d(TAG, "updateProcess(), progress: $progress")
    }

    fun onClick(view: View) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT


        startActivityForResult(intent, 10000)
    }

    private fun login() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = mRepo.login(mSiteCode, LoginReq("1qaz2wsx", "qwe123"))
            withContext(Dispatchers.Main) {
                Log.d("networkoperation", "result: $result")
                mToken = result.data!!.token
            }
        }
    }

    private fun updateCover(uri: Uri) {
        val bitmap = uriConvertToBitmap(contentResolver, uri)

        val timeStamp = System.currentTimeMillis()
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val file = File.createTempFile(
                "png_${timeStamp}",
                ".png",
                storageDir
        )

        val bos = ByteArrayOutputStream() //Convert bitmap to byte array
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos) // compress
        val bitmapData = bos.toByteArray()

        file.createNewFile() //create a file to write bitmap data

        val fos = FileOutputStream(file)
        fos.write(bitmapData)
        fos.flush()
        fos.close()


        CoroutineScope(Dispatchers.IO).launch {
            val result = mRepo.updateCover(mSiteCode, mToken, file)
            withContext(Dispatchers.Main) {
                Log.d("networkoperation", "result: $result")
            }
        }
    }

    private fun uriConvertToBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }
}