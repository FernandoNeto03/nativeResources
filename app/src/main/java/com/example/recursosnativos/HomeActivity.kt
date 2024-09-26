package com.example.recursosnativos

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MyApp()
        }
    }

    @Composable
    fun MyApp() {
        val context = LocalContext.current
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var comment by remember { mutableStateOf("") }
        var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
        var latitude by remember { mutableStateOf<String?>(null) }
        var longitude by remember { mutableStateOf<String?>(null) }

        val dbHelper = DatabaseHelper(context)

        var formDataList by remember { mutableStateOf(listOf<FormData>()) }
        LaunchedEffect(Unit) {
            formDataList = dbHelper.getAllFormData()
        }

        val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) Toast.makeText(context, "Falha ao capturar a imagem", Toast.LENGTH_SHORT).show()
        }

        val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) {
                capturedImageUri = createImageFileUri(context)
                capturedImageUri?.let { uri ->
                    cameraLauncher.launch(uri)
                }
            } else {
                Toast.makeText(context, "Permissão de câmera negada", Toast.LENGTH_SHORT).show()
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            TextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = comment, onValueChange = { comment = it }, label = { Text("Comentário") })
            Spacer(modifier = Modifier.height(16.dp))


            Button(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    capturedImageUri = createImageFileUri(context)
                    capturedImageUri?.let { uri -> cameraLauncher.launch(uri) }
                } else {
                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            }) {
                Text("Tirar Foto")
            }


            capturedImageUri?.let { uri ->
                Image(painter = rememberAsyncImagePainter(model = uri), contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))


            Button(onClick = {
                if (capturedImageUri != null) {
                    val db = dbHelper.writableDatabase
                    val values = ContentValues().apply {
                        put("name", name)
                        put("email", email)
                        put("comment", comment)
                        put("image_path", capturedImageUri.toString())
                    }
                    db.insert("FormData", null, values)
                    Toast.makeText(context, "Dados salvos no banco de dados", Toast.LENGTH_SHORT).show()

                    formDataList = dbHelper.getAllFormData()
                } else {
                    Toast.makeText(context, "Tire uma foto primeiro", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Salvar no SQLite")
            }

            Spacer(modifier = Modifier.height(16.dp))


            Button(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLocationAndShow(context, fusedLocationClient) { lat, lon ->
                        latitude = lat
                        longitude = lon
                        Toast.makeText(context, "Localização obtida: $lat, $lon", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    ActivityCompat.requestPermissions(this@HomeActivity,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
            }) {
                Text("Obter Localização")
            }


            latitude?.let { Text("Latitude: $it") }
            longitude?.let { Text("Longitude: $it") }



            Spacer(modifier = Modifier.height(16.dp))


            LazyColumn {
                items(formDataList) { formData ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Nome: ${formData.name}")
                        Text("Email: ${formData.email}")
                        Text("Comentário: ${formData.comment}")
                        formData.imagePath?.let { path ->
                            val imageUri = Uri.parse(path)
                            Image(painter = rememberAsyncImagePainter(model = imageUri), contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationAndShow(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        onLocationReceived: (String, String) -> Unit
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val lat = it.latitude.toString()
                val lon = it.longitude.toString()

                onLocationReceived(lat, lon)
            } ?: run {
                Toast.makeText(context, "Não foi possível obter a localização", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Erro ao tentar obter a localização", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun createImageFileUri(context: Context): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", imageFile)
    }
}


