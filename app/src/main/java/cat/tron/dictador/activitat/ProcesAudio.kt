package cat.tron.dictador.activitat

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cat.tron.dictador.R
import cat.tron.dictador.databinding.FragmentAudioBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProcesAudio : AppCompatActivity() {
   private val AUDIO_PICK_REQUEST_CODE = 1002
   private lateinit var ctxAudio: Context
   private lateinit var frgAudio: FragmentAudioBinding
   lateinit var cR: Resources

   private var titol = ""
   private var nouText = ""

   // seleccionar archivos
   private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == RESULT_OK) {
         result.data?.data?.let { uri ->
            processaAudio(uri.path!!)
         }
      }
   }

   // permisos de almacenamiento
   private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      if (isGranted) {
         openFilePicker()
      } else {
         frgAudio.error.text = "Permiso denegado"
      }
   }

   fun iniciTranscripcio() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
         // Android 11+ - Usar Storage Access Framework
         openFilePicker()
      } else {
         // Android 10 y anteriores - Solicitar permiso READ_EXTERNAL_STORAGE
         if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openLegacyFilePicker()
         } else {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
         }
      }
   }

   private fun openFilePicker() {
      val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
         addCategory(Intent.CATEGORY_OPENABLE)
         type = "audio/mpeg" // Para archivos MP3
         putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/mpeg", "audio/mp3"))

         // Opcional: Mostrar solo archivos de audio
         putExtra(Intent.EXTRA_LOCAL_ONLY, true)

         // Para Android 5.0+ - mostrar el selector de documentos
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, DocumentsContract.buildRootsUri("com.android.externalstorage.documents"))
         }
      }
      filePickerLauncher.launch(intent)
   }

   private fun openLegacyFilePicker() {
      val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
         type = "audio/*"
         addCategory(Intent.CATEGORY_OPENABLE)
      }
      try {
         startActivityForResult(
            Intent.createChooser(intent, "Selecciona un archivo MP3"),
            AUDIO_PICK_REQUEST_CODE
         )
      } catch (ex: android.content.ActivityNotFoundException) {
         frgAudio.error.text = "No hay aplicaciones para manejar archivos\nerror: " + ex.message
      }
   }

   private fun processaAudio(filepath: String) {
      CoroutineScope(Dispatchers.Main).launch {
         nouText = GestorDeVeu.transcribeAudioFile(filepath)
         mostraTranscripcio(nouText)
      }
   }

   fun desaArxiu(): Boolean {
      val ret = nouText.isNotEmpty()
      CoroutineScope(Dispatchers.Main).launch {
         withContext(Dispatchers.Main) {
            if (ret) {
               if (Utilitats.desaArxiu(titol, nouText, ctxAudio, frgAudio.error)) {
                  frgAudio.error.text = cR.getString(R.string.text_desat)
               }
            } else {
               mostraError(cR.getString(R.string.noutext_buit))
            }
         }
      }
      return ret
   }

   private suspend fun mostraTranscripcio(text: String) {
      withContext(Dispatchers.Main) {
         frgAudio.escriptura.text = text
         delay(100)
      }
   }

   private suspend fun mostraError(text: String) {
      withContext(Dispatchers.Main) {
         frgAudio.error.text = text
      }
   }

   fun setUp(fragmentAudio: FragmentAudioBinding, contextAudio: Context) {
      frgAudio = fragmentAudio
      ctxAudio = contextAudio
      cR = ctxAudio.resources
   }

}
