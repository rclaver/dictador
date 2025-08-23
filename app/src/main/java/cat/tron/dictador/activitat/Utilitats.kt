package cat.tron.dictador.activitat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.Locale

object Utilitats {

   const val REQUEST_CODE_OPEN_DIRECTORY = 101
   private const val STORAGE_PERMISSION_CODE = 100
   private const val arxiuParametres = "parametres.json"

   object objEnFagmentSeleccio {
      private var estat: Boolean = false
      fun set(e: Boolean) { estat = e }
      fun get(): Boolean = estat
   }

   object DirectoriDescarregues {
      private var dir: DocumentFile? = null
      fun set(d: DocumentFile?) { dir = d }
      fun get(): DocumentFile? = dir
   }

   object objCompanyia {
      private var titol: String? = null
      private var idioma: String = "ca"

      fun set(json: JSONObject?) {
         json?.let {
            titol = it.optString("titolDeLobra", "")
            idioma = it.optString("idioma", "")
         }
      }

      fun get(): JSONObject {
         val json = JSONObject()
         json.put("titolDeLobra", titol)
         json.put("idioma", idioma)
         return json
      }

      fun setTitol(t: String) { titol = t }
      fun setIdioma(i: String) { idioma = i }

      fun getTitol(): String = titol.orEmpty()
      fun getIdioma(): String = idioma
   }

   fun obraSencera(nomArxiu: String): DocumentFile? {
      val dir = DirectoriDescarregues.get()
      if (dir?.exists() == true) {
         dir.listFiles().forEach { file ->
            if (file.isFile && file.name == nomArxiu)
               return file
         }
      }
      return null
   }

   fun llistaFragmentsObra(patroBase: String, patroActor: String, omissio: String): List<DocumentFile>  {
      val arxius = llistaDirectoriDescarregues(patroBase.toRegex())
      var ret = arxius.filter { it.name.toString().matches(Regex(patroActor)) }
      if (ret.isEmpty()) {
         ret = arxius.filter { it.name.toString().matches(Regex(omissio)) }
      }
      return ret
   }

   fun llistaDirectoriDescarregues(patro: Regex): List<DocumentFile> {
      var llistaArxiusDescarregues = mutableListOf<DocumentFile>()
      val dir = DirectoriDescarregues.get()
      if (dir?.exists() == true) {
         dir.listFiles().forEach { file ->
            if (file.isFile && patro.containsMatchIn(file.name.toString())) {
               llistaArxiusDescarregues.add(file)
            }
         }
      }
      return llistaArxiusDescarregues
   }

   fun llegeixArxiu(context: Context, document: DocumentFile): String {
      return try {
         context.contentResolver.openInputStream(document.uri)?.use { inputStream ->
            inputStream.bufferedReader().use { reader ->
               reader.readText()
            }
         } ?: "No he pogut obrir el fitxer"
      } catch (e: Exception) {
         "Error llegint el fitxer: ${e.message}"
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

   // Sol·licitar permisos persistents per accedir als arxius contínuament.
   fun demanaAccessDescarregues(aca: AppCompatActivity) {
      val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
         flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                 Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                 Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
      }
      aca.startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY)
   }

   /*
   Obtenir les dades de l'obra a partir de la llista d'arxius del directori Apuntador
   */
   fun obtenirDadesCompanyia() {
      // Obtenir el titol de l'obra
      var titol = ""
      var arxius: List<DocumentFile> = listOf<DocumentFile>()
      val patroTitol = Regex("""[a-z_A-Z]+?-?(?=[a-z_A-Z]*?-?[0-9]*?)\.txt""")
      val arxiusTitol = llistaDirectoriDescarregues(patroTitol)

      for (arxiuT in arxiusTitol) {
         val t = arxiuT.name!!.replace(".txt", "")
         val patroArxius = Regex("""${t}-[a-z_A-Z]+?-[0-9]+?\.txt""")
         arxius = llistaDirectoriDescarregues(patroArxius)
         if (arxius.isNotEmpty()) {
            titol = t
            break
         }
      }
      if (titol != "") {
         objCompanyia.setTitol(titol)
      }
   }

   /*
   Escriu a l'arxiu de paràmetres les dades de la Companyia en format JSON
   */
   fun desaJsonArxiu(file: String?, data: JSONObject?, context: Context): Boolean {
      return try {
         val arxiu = file ?: arxiuParametres
         val dades = data ?: objCompanyia.get()
         context.openFileOutput(arxiu, Context.MODE_PRIVATE).use {
            it.write(dades.toString().toByteArray())
         }
         true
      } catch (e: IOException) {
         false
      }
   }

   /*
   Llegeix l'arxiu de paràmetres per obtenir les dades en format JSON
   */
   fun llegeixJsonArxiu(file: String?, context: Context): JSONObject? {
      return try {
         val arxiu = file ?: arxiuParametres  //  /data/data/cat.tron.dictador/files/parametres.json
         val jsonString = File(context.filesDir, arxiu).readText()
         JSONObject(jsonString)
      } catch (e: Exception) {
         null
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
