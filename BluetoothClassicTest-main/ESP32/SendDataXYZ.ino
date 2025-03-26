#include "BluetoothSerial.h"
#include "esp_gap_bt_api.h"

#define LED_PIN 13

static void gap_callback(esp_bt_gap_cb_event_t event, esp_bt_gap_cb_param_t *param);

BluetoothSerial SerialBT;
esp_bt_pin_code_t pin_code = {1, 2, 3, 4};

void setup() {
    Serial.begin(115200);
    randomSeed(analogRead(0)); // 乱数のシードを設定
    SerialBT.begin("BTC Device 01");
    esp_bt_gap_register_callback(gap_callback);

    esp_bt_pin_type_t pin_type = ESP_BT_PIN_TYPE_FIXED;
    esp_bt_gap_set_pin(pin_type, 4, pin_code);

    Serial.println("Bluetoothデバイスが起動しました。Androidから接続してください。");

    pinMode(LED_PIN, OUTPUT);
    digitalWrite(LED_PIN, LOW);
}

void loop() {
    double a = random(0, 1001) / 100.0;   // 0.00 ～ 10.00
    double b = random(-1000, 1001) / 100.0; // -10.00 ～ 10.00
    double c = random(-1000, 1001) / 100.0; // -10.00 ～ 10.00
    double h = random(0, 1001) / 100.0;   // 0.00 ～ 10.00
    double i = random(0, 20001) / 100.0;  // 0.00 ～ 200.00
    double o = random(-36000, 36001) / 100.0; // -360.00 ～ 360.00
    double p = random(-36000, 36001) / 100.0; // -360.00 ～ 360.00
    double q = random(-36000, 36001) / 100.0; // -360.00 ～ 360.00
    double v = random(0, 2) + 0.0; // 0 or 1
    double w = random(0, 2) + 0.0; // 0 or 1
    double x = random(0, 2) + 0.0; // 0 or 1

    static unsigned long lastSendTime = 0;
    unsigned long currentMillis = millis();

    if (SerialBT.connected()) {
        if (currentMillis - lastSendTime >= 100) { // 100ms ごとにデータ送信
            String dataToSend = "A:" + String(a) + ",B:" + String(b) + ",C:" + String(c) + ",H:" + String(h) + 
                                ",I:" + String(i) + ",O:" + String(o) + ",P:" + String(p) + ",Q:" + String(q) + 
                                ",V:" + String(v) + ",W:" + String(w) + ",X:" + String(x) + "\n";
            SerialBT.print(dataToSend);
            Serial.println(dataToSend);
            lastSendTime = currentMillis;
        }
        digitalWrite(LED_PIN, HIGH);
    } else {
        digitalWrite(LED_PIN, LOW);
        delay(10);
    }
}

static void gap_callback(esp_bt_gap_cb_event_t event, esp_bt_gap_cb_param_t *param) {
    Serial.print("event: ");
    Serial.println(event);

    switch (event) {
        case ESP_BT_GAP_AUTH_CMPL_EVT:
            Serial.println("認証が完了しました。");
            break;
        case ESP_BT_GAP_PIN_REQ_EVT:
            Serial.println("PINコード認証が要求されました。");
            esp_bt_gap_pin_reply(param->pin_req.bda, true, 4, pin_code);
            break;
        default:
            break;
    }
}
