//package ru.netology.nmedia.activity
//
//class NewPostResultContract {
//}

package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

object NewPostResultContract : ActivityResultContract<String?, String?>() {

    override fun createIntent(context: Context, input: String?): Intent =
        Intent(context, NewPostActivity::class.java).putExtra("postContent", input)

//    override fun createIntent(context: Context, input: String?): Intent {
//        return Intent(context, SecondActivity::class.java) .putExtra("my_input_key", input)
//    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        if (resultCode == Activity.RESULT_OK) {
            intent?.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }
}