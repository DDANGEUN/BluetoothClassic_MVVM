#include <SoftwareSerial.h>  



const int pinTx = 5;  // 블루투스 TX 연결 핀 번호
const int pinRx = 4;  // 블루투스 RX 연결 핀 번호

SoftwareSerial   bluetooth( pinTx, pinRx );  


void  setup()
{
  bluetooth.begin(9600);  // 블루투스 통신 초기화 (속도= 9600 bps)
  Serial.begin(115200);
}


void  loop()
{
  
  // 블루투스 수신 
  if ( bluetooth.available() ) 
  {
    Serial.print((char)bluetooth.read());
  }
  else
  {
    delay( 10 );
  }
  // 블루투스 송신
  if (Serial.available()) { 
    //시리얼 모니터에서 입력된 값을 송신
    char toSend = (char)Serial.read();
    bluetooth.print(toSend);
  }
}

   
