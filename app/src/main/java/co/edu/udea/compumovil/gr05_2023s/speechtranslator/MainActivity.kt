package co.edu.udea.compumovil.gr05_2023s.speechtranslator

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class MainActivity : ComponentActivity() {
	private val IMAGE_PICK_CODE = 1000
	private var bitmap by mutableStateOf<Bitmap?>(null)
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			MLApp()
		}
	}
	
	@Composable
	fun MLApp() {
		var labelResult by remember { mutableStateOf("") }
		
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Button(onClick = {
				val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
				ActivityCompat.startActivityForResult(this@MainActivity, intent, IMAGE_PICK_CODE, null)
			}) {
				Text("Select Image")
			}
			
			bitmap?.let {
				Image(
					bitmap = bitmap!!.asImageBitmap(),
					contentDescription = null,
					modifier = Modifier
						.fillMaxWidth()
						.height(200.dp),
					contentScale = ContentScale.Crop
				)
			}
			
			Text(labelResult, fontWeight = FontWeight.Bold)
			
			LaunchedEffect(bitmap) {
				if (bitmap != null) {
					val inputImage = InputImage.fromBitmap(bitmap!!, 0)
					val labelerOptions = ImageLabelerOptions.Builder()
						.setConfidenceThreshold(0.75f)
						.build()
					val imageLabeler = ImageLabeling.getClient(labelerOptions)
					
					imageLabeler.process(inputImage)
						.addOnSuccessListener { labels ->
							val result = StringBuilder()
							for (label in labels) {
								result.append("${label.text}: ${label.confidence}\n")
							}
							labelResult = result.toString()
						}
						.addOnFailureListener { e ->
							labelResult = e.localizedMessage ?: "Error"
						}
				}
			}
		}
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
			val imageUri = data?.data
			imageUri?.let {
				bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
			}
		}
	}
}






