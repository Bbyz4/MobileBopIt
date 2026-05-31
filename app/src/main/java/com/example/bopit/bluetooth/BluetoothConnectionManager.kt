package com.example.bopit.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID

enum class ConnectionState
{
    DISCONNECTED,
    HOSTING,
    CONNECTING,
    CONNECTED
}

object BluetoothConnectionManager
{
    private const val TAG = "BluetoothManager"

    private const val APP_NAME = "BopIt"

    private val APP_UUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc")

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private var socket: BluetoothSocket? = null

    private var serverSocket: BluetoothServerSocket? = null

    private var lastMessage: BluetoothMessage? = null


    private var input: ObjectInputStream? = null
    private var output: ObjectOutputStream? = null

    private var connectionState: ConnectionState = ConnectionState.DISCONNECTED

    var onMessageReceivedCallback: ((BluetoothMessage) -> Unit)? = null

    private fun initializeStreams()
    {
        output = ObjectOutputStream(socket?.outputStream)
        input = ObjectInputStream(socket?.inputStream)
    }

    @SuppressLint("MissingPermission")
    fun host()
    {
        Thread{
            try{
                connectionState = ConnectionState.HOSTING

                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME,
                    APP_UUID)

                Log.d(TAG, "Waiting for connection...")

                socket = serverSocket?.accept()

                Log.d(TAG, "Client connected")

                connectionState = ConnectionState.CONNECTED

                initializeStreams()

                startListening()
            }
            catch(e: Exception)
            {
                connectionState = ConnectionState.DISCONNECTED
                Log.e(TAG, "Host failed", e)
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice)
    {
        Thread{

            try{
                connectionState = ConnectionState.CONNECTING

                bluetoothAdapter?.cancelDiscovery()

                socket = device.createRfcommSocketToServiceRecord(APP_UUID)

                socket?.connect()

                Log.d(TAG, "Connected!")

                connectionState = ConnectionState.CONNECTED

                initializeStreams()

                startListening()

                sendMessage(
                    BluetoothMessage(
                        messageType = "Hello"
                    )
                )
            }
            catch(e: Exception)
            {
                connectionState = ConnectionState.DISCONNECTED
                Log.e(TAG, "Connection failed", e)
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun sendMessage(message: BluetoothMessage)
    {
        if(connectionState != ConnectionState.CONNECTED)
        {
            return
        }

        try{
            output?.writeObject(message)

            output?.flush()
        }
        catch(e: Exception)
        {
            Log.e(TAG, "Send failed", e)
        }
    }

    private fun startListening()
    {
        Thread{

            try{

                while(connectionState == ConnectionState.CONNECTED)
                {
                    val message = input?.readObject() as? BluetoothMessage ?: break

                    onMessageReceived(message)
                }
            }
            catch(e: Exception)
            {
                Log.e(TAG, "Listen failed", e)

                disconnect()
            }

        }.start()
    }

    private fun onMessageReceived(message: BluetoothMessage)
    {
        lastMessage = message

        onMessageReceivedCallback?.invoke(message)

        Log.d(TAG, "Received message: $message")
    }

    fun getLastMessage(): BluetoothMessage?
    {
        return lastMessage
    }

    fun isConnected(): Boolean
    {
        return socket?.isConnected == true
    }

    fun getConnectionState(): ConnectionState
    {
        return connectionState
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice>
    {
        return bluetoothAdapter?.bondedDevices?.toList()?: emptyList()
    }

    fun disconnect()
    {
        try {
            input?.close()
            output?.close()
            socket?.close()
            serverSocket?.close()
        }
        catch(_: Exception)
        {

        }

        input = null
        output = null
        socket = null
        serverSocket = null
        lastMessage = null
        connectionState = ConnectionState.DISCONNECTED
    }
}