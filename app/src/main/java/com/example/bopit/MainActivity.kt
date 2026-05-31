package com.example.bopit

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bopit.bluetooth.BluetoothConnectionManager
import com.example.bopit.bluetooth.BluetoothMessage
import kotlin.random.Random

class MainActivity : AppCompatActivity()
{
    private val N = GameModeFactory.GetGamemodeNumber()

    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startGamePendingIntent?.let { startActivity(it) }
            }
        }

    private var currentGameSettings: GameSettings? = null

    private var startGamePendingIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val roundsInput = findViewById<EditText>(R.id.roundsInput)
        val seedInput = findViewById<EditText>(R.id.seedInput)
        val startButton = findViewById<Button>(R.id.startButton)
        val nameInput = findViewById<EditText>(R.id.nameInput)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            if(
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            )
            {
                ActivityCompat.requestPermissions(
                    this,
                    bluetoothPermissions,
                    100
                )
            }
        }

        startButton.setOnClickListener {

            val rounds = roundsInput.text.toString().toIntOrNull() ?: 3
            val seed = seedInput.text.toString().toIntOrNull() ?: Random.nextInt(0, Int.MAX_VALUE - 1)

            val gameModes = mutableListOf<Boolean>()

            for (i in 0 until N)
            {
                val idName = "mode$i"

                val resId = resources.getIdentifier(idName, "id", packageName)
                val checkBox = findViewById<CheckBox>(resId)

                gameModes.add(checkBox.isChecked)
            }

            val data = GameSettings(rounds, seed, gameModes)

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("GAME_SETTINGS", data)
            intent.putExtra("MULTIPLAYER", false)

            if (gameModes.getOrNull(2) == true) {
                startGamePendingIntent = intent
                requestAudioPermission.launch(android.Manifest.permission.RECORD_AUDIO)
            } else {
                startActivity(intent)
            }
        }

        val historyButton = findViewById<Button>(R.id.historyButton)

        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        //multi----------------------------------------------------

        val hostButton = findViewById<Button>(R.id.hostButton)
        val connectButton = findViewById<Button>(R.id.connectButton)

        hostButton.setOnClickListener {
            val rounds = roundsInput.text.toString().toIntOrNull() ?: 3
            val seed = seedInput.text.toString().toIntOrNull() ?: Random.nextInt(0, Int.MAX_VALUE - 1)

            val gameModes = mutableListOf<Boolean>()

            for (i in 0 until N)
            {
                val idName = "mode$i"

                val resId = resources.getIdentifier(idName, "id", packageName)
                val checkBox = findViewById<CheckBox>(resId)

                gameModes.add(checkBox.isChecked)
            }

            val settings = GameSettings(rounds, seed, gameModes)
            currentGameSettings = settings

            BluetoothConnectionManager.host()

            BluetoothConnectionManager.onMessageReceivedCallback = { message ->
                if(message.messageType == "Hello")
                {
                    BluetoothConnectionManager.sendMessage(
                        BluetoothMessage(
                            messageType = "GameRules",
                            settings = currentGameSettings,
                            playerName = nameInput.text.toString().trim().ifEmpty { "Dude" }
                        )
                    )

                    runOnUiThread {
                        startGameAsHost(currentGameSettings!!, message.playerName)
                    }
                }
            }
        }

        connectButton.setOnClickListener {
            val devices = BluetoothConnectionManager.getPairedDevices()
            if(devices.isEmpty())
            {
                AlertDialog.Builder(this)
                    .setTitle("No paired devices")
                    .setMessage("Pair a device in Bluetooth settings first.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            @SuppressLint("MissingPermission")
            val deviceNames = devices.map {"${it.name}\n${it.address}"}.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("Choose a device")
                .setItems(deviceNames) {_, which ->
                    val device = devices[which]
                    BluetoothConnectionManager.connect(device, nameInput.text.toString().trim().ifEmpty { "Dude" })

                    BluetoothConnectionManager.onMessageReceivedCallback = {message ->
                        if(message.messageType == "GameRules")
                        {
                            message.settings?.let {settings ->
                                runOnUiThread {startGameAsClient(settings, message.playerName)}
                            }
                        }
                    }
                }
                .show()
        }
    }

    private fun startGameAsHost(settings: GameSettings, opponentName: String?)
    {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("GAME_SETTINGS", settings)
            putExtra("MULTIPLAYER_HOST", true)
            putExtra("MULTIPLAYER", true)
            putExtra("OPPONENT_NAME", opponentName)
        }

        BluetoothConnectionManager.onMessageReceivedCallback = null

        if (settings.gameModes.getOrNull(2) == true) {
            startGamePendingIntent = intent
            requestAudioPermission.launch(android.Manifest.permission.RECORD_AUDIO)
        } else {
            startActivity(intent)
        }
    }

    private fun startGameAsClient(settings: GameSettings, opponentName: String?)
    {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("GAME_SETTINGS", settings)
            putExtra("MULTIPLAYER_CLIENT", true)
            putExtra("MULTIPLAYER", true)
            putExtra("OPPONENT_NAME", opponentName)
        }

        BluetoothConnectionManager.onMessageReceivedCallback = null

        if (settings.gameModes.getOrNull(2) == true) {
            startGamePendingIntent = intent
            requestAudioPermission.launch(android.Manifest.permission.RECORD_AUDIO)
        } else {
            startActivity(intent)
        }
    }
}