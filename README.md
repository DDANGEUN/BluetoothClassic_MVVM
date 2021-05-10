# BluetoothClassic_MVVM
Bluetooth Classic Android App(MVVM), Arduino SPP

## Info

   
- If you want to see your bluetooth device like this code preview, modify `mBluetoothStateReceiver` code in  ***registerBluetoothReceiver(), Repository.kt***
```Kotlin
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
```
　  


## Preview


### Device Connect
![connect](https://github.com/DDANGEUN/BluetoothClassic_MVVM/blob/main/demo/android_connect.gif)

### Android Data Receive&Send
![androiddata](https://github.com/DDANGEUN/BluetoothClassic_MVVM/blob/main/demo/android_recvsend.gif)

### Arduino Data Receive&Send
![arduinodata](https://github.com/DDANGEUN/BluetoothClassic_MVVM/blob/main/demo/arduino_serialmonitor.gif)

### Blog
- [[안드로이드-아두이노] bluetooth classic 자동 페어링&연결 / 데이터 송,수신](https://ddangeun.tistory.com/102)

