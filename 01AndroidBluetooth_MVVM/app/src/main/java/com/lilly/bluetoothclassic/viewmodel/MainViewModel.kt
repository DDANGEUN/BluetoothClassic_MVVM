package com.lilly.bluetoothclassic.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.lilly.bluetoothclassic.Repository
import com.lilly.bluetoothclassic.util.*
import java.nio.charset.Charset

class MainViewModel(private val repository: Repository): ViewModel() {

    val connected: LiveData<Boolean?>
        get() = repository.connected
    val progressState: LiveData<String>
        get() = repository.progressState
    var btnConnected = ObservableBoolean(false)

    var inProgressView = ObservableBoolean(false)
    var txtProgress: ObservableField<String> = ObservableField("")

    private val _requestBleOn = MutableLiveData<Event<Boolean>>()
    val requestBleOn: LiveData<Event<Boolean>>
        get() = _requestBleOn

    val inProgress: LiveData<Event<Boolean>>
        get() = repository.inProgress

    val connectError: LiveData<Event<Boolean>>
        get() = repository.connectError

    val txtRead: ObservableField<String> = ObservableField("")
    val putTxt: LiveData<String>
        get() = repository.putTxt

    fun setInProgress(en: Boolean){
        repository.inProgress.value = Event(en)
    }
    fun onClickConnect(){
        if(connected.value==false || connected.value == null){
            if (repository.isBluetoothSupport()) {   // 블루투스 지원 체크
                if(repository.isBluetoothEnabled()){ // 블루투스 활성화 체크
                    //Progress Bar
                    setInProgress(true)
                    //디바이스 스캔 시작
                    repository.scanDevice()
                }else{
                    // 블루투스를 지원하지만 비활성 상태인 경우
                    // 블루투스를 활성 상태로 바꾸기 위해 사용자 동의 요청
                    _requestBleOn.value = Event(true)
                }
            }
            else{ //블루투스 지원 불가
                Util.showNotification("Bluetooth is not supported.")
            }
        }else{
            repository.disconnect()
        }
    }

    fun unregisterReceiver(){
        repository.unregisterReceiver()
    }

    fun onClickSendData(sendTxt: String){
        val byteArr = sendTxt.toByteArray(Charset.defaultCharset())
        repository.sendByteData(byteArr)
        Util.showNotification("send data!")
    }





}