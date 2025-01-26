/* eslint-disable no-console */
import { io } from 'socket.io-client';

const socket = io('ws://localhost:3000');

socket.on('connect', () => {
    console.log('Connected to server');
});

socket.on('disconnect', () => {
    console.log('Disconnected from server');
});
