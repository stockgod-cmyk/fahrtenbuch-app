
package com.oliverheld.fahrtenbuch

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.oliverheld.fahrtenbuch.data.AppDatabase
import com.oliverheld.fahrtenbuch.data.Fahrt
import com.oliverheld.fahrtenbuch.pdf.PdfGenerator
import com.oliverheld.fahrtenbuch.voice.VoiceParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

class FahrtenbuchViewModel: androidx.lifecycle.ViewModel() {
    val datum = MutableStateFlow(LocalDate.now().toString())
    val start = MutableStateFlow("")
    val ziel = MutableStateFlow("")
    val km = MutableStateFlow("")
    val anlass = MutableStateFlow("")
}

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "fahrtenbuch").build()
        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val fahrten by db.dao().alle().collectAsState(initial = emptyList())
                
                val vm: FahrtenbuchViewModel = viewModel()
                val datum by vm.datum.collectAsState()
                val start by vm.start.collectAsState()
                val ziel by vm.ziel.collectAsState()
                val km by vm.km.collectAsState()
                val anlass by vm.anlass.collectAsState()

                var lastPdfPath by remember { mutableStateOf("") }

                val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

                val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: return@rememberLauncherForActivityResult
                    val parsed = VoiceParser.parse(spoken)
                    vm.start.value = parsed.start.ifBlank { vm.start.value }
                    vm.ziel.value = parsed.ziel.ifBlank { vm.ziel.value }
                    if(parsed.km > 0) vm.km.value = parsed.km.toString()
                    vm.anlass.value = spoken
                }

                Scaffold(
                    floatingActionButton = {
                        Column {
                            ExtendedFloatingActionButton(onClick = {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMANY.toString())
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Sage z.B. 'Von Höhenkirchen nach München 20 km Kundentermin'")
                                }
                                speechLauncher.launch(intent)
                            }) { Text("🎤 Spracheingabe") }
                            Spacer(Modifier.height(8.dp))
                            ExtendedFloatingActionButton(onClick = {
                                scope.launch {
                                    val file = PdfGenerator.create(context, fahrten)
                                    lastPdfPath = file.absolutePath
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".provider", file))
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(share, "PDF teilen"))
                                }
                            }) { Text("📄 PDF erzeugen") }
                        }
                    }
                ) { pad ->
                    Column(Modifier.padding(pad).padding(16.dp).fillMaxSize()) {
                        Text("Fahrtenbuch", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(12.dp))
                        
                        OutlinedTextField(datum, { vm.datum.value = it }, label = { Text("Datum (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(start, { vm.start.value = it }, label = { Text("Start") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(ziel, { vm.ziel.value = it }, label = { Text("Ziel") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(km, { vm.km.value = it }, label = { Text("Kilometer") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(anlass, { vm.anlass.value = it }, label = { Text("Anlass / Spracheingabe") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                        Spacer(Modifier.height(12.dp))
                        Button(onClick = {
                            scope.launch {
                                val f = Fahrt(datum = datum, start = start, ziel = ziel, km = km.replace(",",".").toDoubleOrNull()?:0.0, anlass = anlass)
                                db.dao().insert(f)
                                vm.start.value = ""; vm.ziel.value = ""; vm.km.value = ""; vm.anlass.value = ""
                                Toast.makeText(context, "Fahrt gespeichert", Toast.LENGTH_SHORT).show()
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Fahrt speichern") }

                        Spacer(Modifier.height(16.dp))
                        Text("Deine Fahrten (${fahrten.size}) - Gesamt: ${fahrten.sumOf { it.km }} km", style = MaterialTheme.typography.titleMedium)
                        LazyColumn {
                            items(fahrten) { f ->
                                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("${f.datum}: ${f.start} -> ${f.ziel} (${f.km} km)", style = MaterialTheme.typography.bodyLarge)
                                        Text(f.anlass, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                        if(lastPdfPath.isNotBlank()) Text("Letztes PDF: $lastPdfPath", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
