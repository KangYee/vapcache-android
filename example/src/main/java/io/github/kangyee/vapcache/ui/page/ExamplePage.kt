package io.github.kangyee.vapcache.ui.page

import android.content.Intent
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import com.tencent.qgame.animplayer.AnimView
import io.github.kangyee.vapcache.R
import io.github.kangyee.vapcache.library.VapCompositionResult
import io.github.kangyee.vapcache.library.VapCompositionSpec
import io.github.kangyee.vapcache.library.rememberVapComposition
import io.github.kangyee.vapcache.ui.ExampleActivity
import java.io.File

@Composable
fun ExamplePage() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24F.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .height(100F.dp)
                .wrapContentHeight(),
            text = "VapCache Example by Jetpack Compose",
            style = MaterialTheme.typography.titleLarge,
        )
        ExampleFromRaw()
        ExampleFromAssets()
        ExampleFromUrl()
        ExampleFromFile()
        Button(
            onClick = {
                val intent = Intent(context, ExampleActivity::class.java)
                context.startActivity(intent)
            }
        ) {
           Text(text = "查看View版本")
        }
    }
}

@Composable
fun ExampleFromRaw() {
    val vapFile by rememberVapComposition(
        VapCompositionSpec.RawRes(R.raw.demo)
    )

    vapFile?.let {
        Row {
            Text("From RawRes:")
            Spacer(Modifier.width(20F.dp))
            VapAnimView(it)
        }
    }
}

@Composable
fun ExampleFromAssets() {
    val vapFile by rememberVapComposition(
        VapCompositionSpec.Asset("demo.mp4")
    )

    vapFile?.let {
        Row {
            Text("From Assets:")
            Spacer(Modifier.width(20F.dp))
            VapAnimView(it)
        }
    }
}

@Composable
fun ExampleFromUrl() {
    val composition = rememberVapComposition(
        VapCompositionSpec.Url("http://xxx.mp4")
    )

    Row {
        Text("From Url:")
        Spacer(Modifier.width(20F.dp))
        when {
            composition.isFailure -> {
                Text("${composition.error?.localizedMessage}")
            }
            composition.isLoading -> {
                Text("Loading")
            }
            composition.isSuccess -> {
                composition.value?.let {
                    VapAnimView(it)
                }
            }
        }
    }
}

@Composable
fun ExampleFromFile() {
    val composition = rememberVapComposition(
        VapCompositionSpec.File("/xxx.mp4")
    )

    Row {
        Text("From File:")
        Spacer(Modifier.width(20F.dp))
        when {
            composition.isFailure -> {
                Text("${composition.error?.localizedMessage}")
            }
            composition.isLoading -> {
                Text("Loading")
            }
            composition.isSuccess -> {
                composition.value?.let {
                    VapAnimView(it)
                }
            }
        }
    }
}

@Composable
fun VapAnimView(
    animFile: File
) {
    AndroidView(
        modifier = Modifier.size(100F.dp),
        factory = { context ->
            val animView = AnimView(context)
            animView.post {
                animView.updateLayoutParams {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
            animView.setMute(true)
            animView.setLoop(Int.MAX_VALUE)
            animView.startPlay(animFile)

            animView
        }
    )
}