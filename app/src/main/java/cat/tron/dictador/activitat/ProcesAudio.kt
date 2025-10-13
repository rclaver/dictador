package cat.tron.dictador.activitat

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import cat.tron.dictador.R
import cat.tron.dictador.databinding.FragmentAudioBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProcesAudio : AppCompatActivity() {
   private lateinit var ctxAudio: Context
   private lateinit var frgAudio: FragmentAudioBinding
   lateinit var cR: Resources

   private var titol = ""
   private var nouText = ""

   fun iniciTranscripcio() {
      //GestorDeVeu.objTTS.inici()
      val filepath = obreArxiu()
      CoroutineScope(Dispatchers.Main).launch {
         processaAudio(filepath)
         desaArxiu()
      }
   }

   private suspend fun processaAudio(filepath: String) {
      nouText = GestorDeVeu.transcribeAudioFile(filepath)
      mostraTranscripcio(nouText)
      //withContext(Dispatchers.Main) { frgAudio.lectura.text = nouText }
   }

   private fun obreArxiu(): String {
      var filepath = ""
      return filepath
   }

   suspend fun desaArxiu(): Boolean {
      val ret = nouText.isNotEmpty()
      if (ret) {
         val errorTextView = frgAudio.error
         if (Utilitats.desaArxiu(titol, nouText, ctxAudio, errorTextView)) {
            withContext(Dispatchers.Main) {
               frgAudio.error.text = cR.getString(R.string.text_desat)
            }
            delay(5000)
         }
      } else {
         mostraError(cR.getString(R.string.noutext_buit))
         delay(5000)
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
