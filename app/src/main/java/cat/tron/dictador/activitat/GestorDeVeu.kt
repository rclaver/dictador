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
import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.RecognizeRequest
import com.google.cloud.speech.v1.RecognizeResponse
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.speech.v1.SpeechRecognitionAlternative
import com.google.cloud.speech.v1.SpeechRecognitionResult
import java.io.File

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
               //SpeechRecognizer.ERROR_NO_MATCH -> cR.getString(R.string.error_no_escolto_res)
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

   suspend fun preparaReconeixementDeVeu(context: Context, frgDictat: FragmentDictatBinding): String = suspendCancellableCoroutine {
      cont ->
      iniciaReconeixement(
         context,
         5000L,
         onPreparat = { frgDictat.avis.text = context.resources.getString(R.string.escoltant) },
         onParlant = { frgDictat.error.text = "" },
         onFiDeParla = { frgDictat.avis.text = "silenci" },
         onResultat = { cont.resume(it) { cause, _, _ -> } },
         onError = { cont.resume("") { cause, _, _ -> } }
      )
   }

   fun transcribeAudioFile(filePath: String): String {
      var transcripcio = ""

      val speechClient = SpeechClient.create()
      val audio = RecognitionAudio.newBuilder().setUri(filePath).build()

      val config = RecognitionConfig.newBuilder()
         .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
         .setLanguageCode("es-ES")
         .setSampleRateHertz(16000)
         .build()

      val request = RecognizeRequest.newBuilder()
         .setConfig(config)
         .setAudio(audio)
         .build()

      val response: RecognizeResponse = speechClient.recognize(request)
      for (result: SpeechRecognitionResult in response.resultsList) {
         for (alternative: SpeechRecognitionAlternative in result.alternativesList) {
            transcripcio += alternative.transcript
         }
      }
      speechClient.close()
      return transcripcio
   }

}
