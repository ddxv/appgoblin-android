package dev.thirdgate.appgoblin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    AboutPage()
                }
            }
        }
    }
}

@Composable
fun AboutPage() {

    val context = LocalContext.current

    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName
    val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        packageInfo.longVersionCode.toInt()
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode
    }
    val versionText = "$versionName ($versionCode)"


    val currentContext = rememberUpdatedState(context)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AppGoblin", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Version $versionText", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Source code available", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("App Source Code", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            "github.com/ddxv/appgoblin-android",
            fontSize = 20.sp,
            modifier = Modifier.clickable {
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ddxv/appgoblin-android"))
                currentContext.value.startActivity(intent)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Source of Data", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            "github.com/ddxv/appgoblin",
            fontSize = 20.sp,
            modifier = Modifier.clickable {
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ddxv/appgoblin"))
                currentContext.value.startActivity(intent)
            }
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text("About Developer", fontSize = 18.sp, fontWeight = FontWeight.Bold,)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "thirdgate.dev",
            fontSize = 18.sp,
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://thirdgate.dev"))
                currentContext.value.startActivity(intent)
            }
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text("Privacy Policy", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "thirdgate.dev/privacypolicy.html",
            fontSize = 18.sp,
            modifier = Modifier.clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://thirdgate.dev/privacypolicy.html")
                )
                currentContext.value.startActivity(intent)
            }
        )

    }
}

@Preview(showBackground = true)
@Composable
fun AboutPagePreview() {
    AboutPage()
}
