package cat.tron.dictador.activitat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import cat.tron.dictador.R
import cat.tron.dictador.databinding.FragmentDictatBinding
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale

object GestorDeVeu {

   object objTTS {
      private var tts: TextToSpeech? = null
      fun set(t: TextToSpeech?) { tts = t }
      fun get(): TextToSpeech? = tts
      fun inici() { tts?.language = Locale("ca_ES") }
   }

   /*
   Activa el micròfon, recull l'audio amb detecció de veu i el transcriu a text
   */
   fun iniciaReconeixement(context: Context,
                            tempsMaxim: Long,
                            onPreparat: () -> Unit = {},
                            onParlant: () -> Unit = {},
                            onFiDeParla: () -> Unit = {},
                            onResultat: (String) -> Unit,
                            onError: (String) -> Unit) {

      val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
         putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
         putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ca-ES")
         putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // opcional, per tenir text parcial
      }

      val cR = context.resources
      val handler = Handler(Looper.getMainLooper())
      val cancelRunnable = Runnable { recognizer.stopListening() }

      recognizer.setRecognitionListener(object : RecognitionListener {
         override fun onReadyForSpeech(params: Bundle?) {
            onPreparat() // L'usuari pot començar a parlar
            handler.postDelayed(cancelRunnable, tempsMaxim) // inicia el compte enrere
         }
         override fun onResults(results: Bundle?) {
            handler.removeCallbacks(cancelRunnable)
            val paraules = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = paraules?.get(0) ?: ""
            onResultat(text)
         }
         override fun onError(error: Int) {
            handler.removeCallbacks(cancelRunnable)
            val missatge = when (error) {
               SpeechRecognizer.ERROR_AUDIO -> cR.getString(R.string.error_audio)
               SpeechRecognizer.ERROR_NO_MATCH -> cR.getString(R.string.error_no_escolto_res)
               else -> String.format(cR.getString(R.string.error_desconegut), error)
            }
            onError(missatge)
         }
         override fun onBeginningOfSpeech() {
            onParlant()
         }
         override fun onEndOfSpeech() {
            handler.removeCallbacks(cancelRunnable)
            onFiDeParla()
         }
         override fun onRmsChanged(rmsdB: Float) {}
         override fun onBufferReceived(buffer: ByteArray?) {}
         override fun onPartialResults(partialResults: Bundle?) {}
         override fun onEvent(eventType: Int, params: Bundle?) {}
      })
      recognizer.startListening(intent)
   }

   suspend fun preparaReconeixementDeVeu(context: Context, text: String, frgDictat: FragmentDictatBinding): String = suspendCancellableCoroutine {
      cont ->
      iniciaReconeixement(
         context,
         calculaTemps(text),
         onPreparat = {frgDictat.narracio.text = context.resources.getString(R.string.escoltant)},
         onParlant = {frgDictat.error.text = ""},
         onFiDeParla = {frgDictat.narracio.text = ""},
         onResultat = { cont.resume(it) { cause, _, _ -> } },
         onError = { cont.resume("") { cause, _, _ -> } }
      )
   }

   private fun calculaTemps(text: String): Long {
      return  (text.length * 100).toLong()
   }

}
