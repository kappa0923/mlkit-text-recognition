// MIT License
//
// Copyright (c) 2019 kappa0923
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package jp.kappa0923.android.mlkit

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class MainActivity : AppCompatActivity() {
    companion object {
        const val INTENT_PHOTO = 1001
    }

    private var imageMaxWidth = 0
    private var imageMaxHeight = 0
    private lateinit var outputFileUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        captureImageFab.setOnClickListener {
            setImageViewSize()
            showCameraWithPermissionCheck()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            INTENT_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    val capturePhoto = getScaledBitmap()
                    setRecognizePhotoToView(capturePhoto)

                    // オーバーレイ表示のスケールを修正
                    val scaleX = (imageMaxWidth.toFloat() / capturePhoto.width)
                    val scaleY = (imageMaxHeight.toFloat() / capturePhoto.height)
                    graphicOverlay.scale(scaleX, scaleY)

                    // 画像から文字認識
                    runTextRecognition(capturePhoto)
                } else {
                    showToast("Camera canceled")
                }
            }
        }
    }

    /**
     * カメラの呼び出し
     */
    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "MLKit_codelab")
            outputFileUri = contentResolver
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            startActivityForResult(intent, INTENT_PHOTO)
        } else {
            showToast("Camera is missing.")
        }
    }

    /**
     * 撮影した画像を変形しつつ読み込む
     * @return 変形後の読み込み画像
     */
    private fun getScaledBitmap(): Bitmap {
        val srcImage = FirebaseVisionImage
                .fromFilePath(baseContext, outputFileUri).bitmap

        // 画像を変形させるための差分を計算
        val scaleFactor = Math.min(
                srcImage.width / imageView.width.toFloat(),
                srcImage.height / imageView.height.toFloat()
        )

        val deltaWidth = (srcImage.width - imageView.width * scaleFactor).toInt()
        val deltaHeight = (srcImage.height - imageView.height * scaleFactor).toInt()

        // 画像の変形
        val scaledImage = Bitmap.createBitmap(
                srcImage, deltaWidth / 2, deltaHeight / 2,
                srcImage.width - deltaWidth, srcImage.height - deltaHeight
        )
        srcImage.recycle()
        return scaledImage
    }

    /**
     * 対象の画像を画面にセットする
     * @param photo 対象の画像
     */
    private fun setRecognizePhotoToView(photo: Bitmap) {
        runOnUiThread {
            imageView.setImageBitmap(photo)
        }
    }

    /**
     * 対象の画像のテキスト認識を行う
     * @param recognitionTarget 認識対象の画像
     */
    private fun runTextRecognition(recognitionTarget: Bitmap) {
        // TODO : 05 解析用オブジェクト生成

        // TODO : 05 文字認識
    }

    /**
     * テキスト認識の結果を処理する
     * @param texts 認識結果のテキスト情報
     */
    private fun processTextRecognitionResult(texts: FirebaseVisionText) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            showToast("Text not found")
            return
        }

        graphicOverlay.clear()

        // TODO : 05 認識したテキストを表示する
    }

    /**
     * 言語を識別する
     * @param block 識別するテキスト
     */
    private fun identifyLanguage(block: FirebaseVisionText.TextBlock) {
        // TODO : 07 言語の識別
    }

    /**
     * テキストを日本語に翻訳する
     * @param block 翻訳する文章
     * @param language 翻訳する前の言語
     */
    private fun translateToJapanese(block: FirebaseVisionText.TextBlock, language: String) {
        val sourceLanguageCode = FirebaseTranslateLanguage.languageForLanguageCode(language)
                ?: FirebaseTranslateLanguage.EN

        val options = FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(FirebaseTranslateLanguage.JA)
                .build()

        // TODO : 08 テキストを翻訳する
    }

    /**
     * TextBlockを画面上に表示する
     * @param block 表示するBlock
     */
    private fun showTextBlock(block: FirebaseVisionText.TextBlock) {
        val textGraphic = TextGraphic(graphicOverlay, block)
        graphicOverlay.add(textGraphic)
    }

    private fun setImageViewSize() {
        if (imageMaxWidth == 0) {
            imageMaxWidth = imageView.width
        }
        if (imageMaxHeight == 0) {
            imageMaxHeight = imageView.height
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

}
