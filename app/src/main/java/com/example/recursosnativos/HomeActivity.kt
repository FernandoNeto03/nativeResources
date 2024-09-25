package com.example.recursosnativos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.recursosnativos.ui.theme.RecursosNativosTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecursosNativosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeActivityScreen()
                }
            }
        }
    }
}

@Composable
fun HomeActivityScreen(
    uri: Uri? = null,
    directory: File? = null,
    onSetUri: (Uri) -> Unit = {}
) {

    val context = LocalContext.current
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        BuildConfig.APPLICATION_ID + ".provider", file
    )

    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            capturedImageUri = uri
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
       if (it) {
           Toast.makeText(context, "Permissão Garantida", Toast.LENGTH_SHORT).show()
           cameraLauncher.launch(uri)
       } else {
           Toast.makeText(context, "Permissão Negada", Toast.LENGTH_SHORT).show()
       }
    }


    var login by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("") }

    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ){
        TextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
//                val intent = Intent(activity, MenuActivity::class.java)
//                activity.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val permissionCheckResult =
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(uri)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
            }
        ) {
            Text("Take Photo")
        }
    }

    if (capturedImageUri.path?.isNotEmpty() == true){
        Image(
            modifier = Modifier
                .padding(16.dp, 8.dp),
            painter = rememberAsyncImagePainter(cameraLauncher),
            contentDescription = null
        )
    }
}




@SuppressLint("SimpleDateFormat")
fun Context.createImageFile(): File {

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )

    return image
}
