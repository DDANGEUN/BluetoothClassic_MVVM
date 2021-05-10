package com.lilly.bluetoothclassic

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.lilly.bluetoothclassic.util.Event
import com.lilly.bluetoothclassic.util.SPP_UUID
import com.lilly.bluetoothclassic.util.Util
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


class Repository {

    var connected: MutableLiveData<Boolean?> = MutableLiveData(null)
    var progressState: MutableLiveData<String> = MutableLiveData("")
    val putTxt: MutableLiveData<String> = MutableLiveData("")

    val inProgress = MutableLiveData<Event<Boolean>>()
    val connectError = MutableLiveData<Event<Boolean>>()

    var mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var mBluetoothStateReceiver: BroadcastReceiver? = null
    var targetDevice: BluetoothDevice? = null
    var socket: BluetoothSocket? = null
    var mOutputStream: OutputStream? = null
    var mInputStream: InputStream? = null

    var foundDevice:Boolean = false

    private lateinit var sendByte:ByteArray
    var discovery_error = false

    fun isBluetoothSupport():Boolean{
        return if(mBluetoothAdapter==null) {
            Util.showNotification("Bluetooth 지원을 하지 않는 기기입니다.")
            false
        }else{
            true
        }
    }
    fun isBluetoothEnabled():Boolean{
        return if (!mBluetoothAdapter!!.isEnabled) {
            // 블루투스를 지원하지만 비활성 상태인 경우
            // 블루투스를 활성 상태로 바꾸기 위해 사용자 동의 요청
            Util.showNotification("Bluetooth를 활성화 해 주세요.")
            false
        }else{
            true
        }
    }
    
    fun scanDevice(){
        progressState.postValue("device 스캔 중...")

        registerBluetoothReceiver()

        val bluetoothAdapter = mBluetoothAdapter
        foundDevice = false
        bluetoothAdapter?.startDiscovery() //블루투스 기기 검색 시작
    }
    fun registerBluetoothReceiver(){
        //intentfilter
        val stateFilter = IntentFilter()
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED) //연결 확인
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //연결 끊김 확인
        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND) //기기 검색됨
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //기기 검색 시작
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //기기 검색 종료
        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        mBluetoothStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action //입력된 action
                if (action != null) {
                    Log.d("Bluetooth action", action)
                }
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                var name: String? = null
                if (device != null) {
                    name = device.name //broadcast를 보낸 기기의 이름을 가져온다.
                }
                when (action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state = intent.getIntExtra(
                            BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR
                        )
                        when (state) {
                            BluetoothAdapter.STATE_OFF -> {
                            }
                            BluetoothAdapter.STATE_TURNING_OFF -> {
                            }
                            BluetoothAdapter.STATE_ON -> {
                            }
                            BluetoothAdapter.STATE_TURNING_ON -> {
                            }
                        }
                    }
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {

                    }
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        connected.postValue(false)
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    }
                    BluetoothDevice.ACTION_FOUND -> {
                        if (!foundDevice) {
                            val device_name = device!!.name
                            val device_Address = device.address
                            
                            // It only searches for devices with the prefix "RNM" in the Bluetooth device name.
                            if (device_name != null && device_name.length > 4) {
                                if (device_name.substring(0, 3) == "RNM") {
                                    // filter your targetDevice and use connectToTargetedDevice()
                                    targetDevice = device
                                    foundDevice = true
                                    connectToTargetedDevice(targetDevice)
                                }
                            }
                            
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        if (!foundDevice) {
                            Util.showNotification("디바이스를 찾을 수 없습니다. 다시 시도해 주세요.")
                            inProgress.postValue(Event(false))
                        }
                    }

                }
            }
        }
        MyApplication.applicationContext().registerReceiver(
            mBluetoothStateReceiver,
            stateFilter
        )

    }


    @ExperimentalUnsignedTypes
    private fun connectToTargetedDevice(targetedDevice: BluetoothDevice?) {
        progressState.postValue("${targetDevice?.name}에 연결중..")

        val thread = Thread {
            //선택된 기기의 이름을 갖는 bluetooth device의 object
            val uuid = UUID.fromString(SPP_UUID)
            try {
                // 소켓 생성
                socket = targetedDevice?.createRfcommSocketToServiceRecord(uuid)

                socket?.connect()

                /**
                 * After Connect Device
                 */
                connected.postValue(true)
                mOutputStream = socket?.outputStream
                mInputStream = socket?.inputStream
                // 데이터 수신
                beginListenForData()

            } catch (e: java.lang.Exception) {
                // 블루투스 연결 중 오류 발생
                    e.printStackTrace()
                connectError.postValue(Event(true))
                try {
                    socket?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        //연결 thread를 수행한다
        thread.start()
    }


    fun disconnect(){
        try {
            socket?.close()
            connected.postValue(false)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun unregisterReceiver(){
        if(mBluetoothStateReceiver!=null) {
            MyApplication.applicationContext().unregisterReceiver(mBluetoothStateReceiver)
            mBluetoothStateReceiver = null
        }
    }

    /**
     * 블루투스 데이터 송신
     */
    fun sendByteData(data: ByteArray) {
        Thread {
            try {
                mOutputStream?.write(data) // 프로토콜 전송
            } catch (e: Exception) {
                // 문자열 전송 도중 오류가 발생한 경우.
                e.printStackTrace()
            }
        }.run()
    }

    /**
     * Convert
     * @ByteToUint : byte[] -> uint
     * @byteArrayToHex : byte[] -> hex string
     */
    private val m_ByteBuffer: ByteBuffer = ByteBuffer.allocateDirect(8)
    // byte -> uint
    fun ByteToUint(data: ByteArray?, offset: Int, endian: ByteOrder): Long {
        synchronized(m_ByteBuffer) {
            m_ByteBuffer.clear()
            m_ByteBuffer.order(endian)
            m_ByteBuffer.limit(8)
            if (endian === ByteOrder.LITTLE_ENDIAN) {
                m_ByteBuffer.put(data, offset, 4)
                m_ByteBuffer.putInt(0)
            } else {
                m_ByteBuffer.putInt(0)
                m_ByteBuffer.put(data, offset, 4)
            }
            m_ByteBuffer.position(0)
            return m_ByteBuffer.long
        }
    }

    fun byteArrayToHex(a: ByteArray): String? {
        val sb = StringBuilder()
        for (b in a) sb.append(String.format("%02x ", b /*&0xff*/))
        return sb.toString()
    }

    /**
     * 블루투스 데이터 수신 Listener
     */
    @ExperimentalUnsignedTypes
    fun beginListenForData() {

        val mWorkerThread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val bytesAvailable = mInputStream?.available()
                    if (bytesAvailable != null) {
                        if (bytesAvailable > 0) { //데이터가 수신된 경우
                            val packetBytes = ByteArray(bytesAvailable)
                            mInputStream?.read(packetBytes)

                            /**
                             * 한 버퍼 처리
                             */
                            val s = String(packetBytes,Charsets.UTF_8)
                            putTxt.postValue(s)

                            /**
                             * 한 바이트씩 처리
                             */
                            for (i in 0 until bytesAvailable) {
                                val b = packetBytes[i]
                                Log.d("inputData", String.format("%02x", b))
                            }
                        }
                    }
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        //데이터 수신 thread 시작
        mWorkerThread.start()
    }


}
