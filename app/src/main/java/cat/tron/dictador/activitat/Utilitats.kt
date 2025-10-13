package cat.tron.dictador.activitat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object Utilitats {

   const val REQUEST_CODE_OPEN_DIRECTORY = 101
   private const val STORAGE_PERMISSION_CODE = 100
   private const val ARXIU = "pensaments"

   /* Obté la llista d'arxius */
   fun listFilesInDownloads(): List<File> {
      val filesList = mutableListOf<File>()
      val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

      if (downloadsDir.exists() && downloadsDir.isDirectory) {
         val files = downloadsDir.listFiles()

         files?.forEach { file ->
            if (file.isFile) {
               filesList.add(file)
               //print("Nombre: ${file.name}, Ruta: ${file.absolutePath}")
            }
         }
      }
      return filesList
   }

   /*
   Escriu el text dictat a un arxiu de emmagatzematge
   */
   suspend fun desaArxiu(fileName: String, dades: String, context: Context, errorView: TextView): Boolean {
      val nomArxiu = fileName.ifEmpty { ARXIU }
      return try {
         val directori = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
         val carpetaApp = File(directori, "Dictador")
         if (!carpetaApp.exists()) { carpetaApp.mkdirs() }

         val arxiu = File(carpetaApp, "$nomArxiu.txt")
         FileOutputStream(arxiu).use { output ->
            output.write(dades.toByteArray())
         }
         // Notificar al sistema que se ha creado un nuevo archivo
         MediaScannerConnection.scanFile(context, arrayOf(arxiu.absolutePath), null, null)
         true
      }catch (e: Exception) {
         e.printStackTrace()
         errorView.text = e.message
         false
      }
   }

   fun demanaPermissos(cntx: Context, aca: AppCompatActivity) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
         //VERSION_CODES.M es igual a 23, o sea Android 6.0
         try {
            val noPermis = cntx.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                  cntx.checkSelfPermission(Manifest.permission.MANAGE_DOCUMENTS) != PackageManager.PERMISSION_GRANTED ||
                  cntx.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            if (noPermis) {
               ActivityCompat.requestPermissions(aca,
                  arrayOf(
                     Manifest.permission.RECORD_AUDIO,
                     Manifest.permission.MANAGE_DOCUMENTS,
                     Manifest.permission.READ_EXTERNAL_STORAGE
                  ),
                  STORAGE_PERMISSION_CODE
               )
            }
         }catch(e: UnknownError) {
            println(e)
         }
      }
   }

   fun canviaIdioma(idioma: String, context: Context) {
      val displayMetrics = context.resources.displayMetrics
      val configuracio = context.resources.configuration
      configuracio.setLocale(Locale(idioma))
      context.resources.updateConfiguration(configuracio, displayMetrics)
      configuracio.locale = Locale(idioma)
      context.resources.updateConfiguration(configuracio, displayMetrics)
   }

}
