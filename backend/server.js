/* eslint-disable no-console */

import express from 'express';
import { createServer } from 'http';
import path from 'path';
import { Server } from 'socket.io';
import { fileURLToPath } from 'url';

const app = express();
const http = createServer(app);
const io = new Server(http, {
    cors: {
        origin: '*',
        methods: ['GET', 'POST'],
    },
    transports: ['polling', 'websocket'],
    maxHttpBufferSize: 1e8,
});

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));

app.get('/', (req, res) => {
    res.render('dashboard');
});

const connectedDevices = new Map();

const updateDevices = () => {
    io.emit('devices-updated', Array.from(connectedDevices.values()));
};

io.on('connection', (socket) => {
    console.log('New client connected:', socket.id);
    updateDevices();

    socket.on('register-device', (deviceInfo) => {
        connectedDevices.set(socket.id, {
            id: socket.id,
            info: deviceInfo,
            lastActive: new Date(),
            screenData: null,
        });
        updateDevices();
    });

    socket.on('screen-data', (data) => {
        console.log('Screen data from:', socket.id);
        const device = connectedDevices.get(socket.id);
        if (device) {
            device.screenData = data;
            device.lastActive = new Date();

            // 將 binary data 轉換為 Blob URL
            const frameData = Buffer.from(data.frameData);
            socket.broadcast.emit('screen-update', {
                deviceId: socket.id,
                data: {
                    frameData,
                    type: 'image/jpeg',
                },
            });
        }
    });

    socket.on('disconnect', () => {
        console.log('Client disconnected:', socket.id);
        connectedDevices.delete(socket.id);
        updateDevices();
    });
});

const PORT = process.env.PORT || 3000;
http.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
