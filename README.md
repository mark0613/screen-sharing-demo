# Screen Sharing Demo
實現手機螢幕分享並串流到後端的最小實踐方案。
(目前尚只支援 Android)

## 架構
- 前端: Flutter 或 React Native Expo
- 後端: Node.js
- 串流: Socket.io

## 環境參考
- Android Studio
- Flutter 3.27.3
- Expo 52.0.27
- Node.js 22.13.0

## 啟動
1. 啟動 Android Studio Emulator
    ```bash
    # 先查看可用的 AVD
    emulator -list-avds

    # 啟動指定的 AVD
    emulator -avd <AVD_NAME>
    ```
2. 啟動後端(擇一)
    ```bash
    cd backend
    yarn install
    yarn start
    ```
3. 啟動前端
    - Flutter
        ```bash
        cd flutter_app
        flutter pub get
        flutter run
        ```
    - React Native Expo
        ```bash
        cd rne_app
        yarn install
        yarn android
        ```
4. 開啟瀏覽器
    ```
    http://localhost:3000
    ```
5. 操作手機
    - 點擊 `開始分享` 就可以到網頁上看到手機畫面
    - 點擊 `停止分享` 就會中斷分享

## 流程參考
請看各自的 `README.md`
- [Flutter](flutter_app/README.md)
- [React Native Expo](rne_app/README.md)
- [Backend](backend/README.md)
