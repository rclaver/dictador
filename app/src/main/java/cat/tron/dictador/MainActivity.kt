package cat.tron.dictador

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import cat.tron.dictador.activitat.GestorDeVeu
import cat.tron.dictador.activitat.Utilitats
import cat.tron.dictador.databinding.ActivityMainBinding
import java.util.Locale

open class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

   private lateinit var binding: ActivityMainBinding
   private val idioma: Locale = Locale("ca", "ES")
   private var tts: TextToSpeech? = null
   private val engine = "com.google.android.tts" //motor de Google TTS

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      binding = ActivityMainBinding.inflate(layoutInflater)
      setContentView(binding.root)

      Utilitats.demanaPermissos(applicationContext, this)
      GestorDeVeu.objTTS.set(TextToSpeech(this, this, engine))
      tts = GestorDeVeu.objTTS.get()
   }

   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(requestCode, resultCode, data)
      if (requestCode == Utilitats.REQUEST_CODE_OPEN_DIRECTORY && resultCode == RESULT_OK) {
         val treeUri = data?.data ?: return
         // Agafem el permís permanent
         contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
         )
      }
   }

   // TextToSpeech.OnInitListener
   override fun onInit(status: Int) {
      if (status == TextToSpeech.SUCCESS) {
         tts?.setEngineByPackageName(engine)
         val result = tts?.setLanguage(idioma)
         if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            print(R.string.idioma_no_soportat)
            // L'usuari haurà d'instal·lar l'enginy Google TTS
            val installIntent = Intent().apply {
               action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            }
            startActivity(installIntent)         }
      } else {
         print(R.string.error_inici_TTS)
      }
   }

   override fun onDestroy() {
      tts?.stop()
      tts?.shutdown()
      super.onDestroy()
   }

}
