import {
    useCallback,
    useEffect,
    useRef,
    useState,
} from 'react';
import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

import { io, Socket } from 'socket.io-client';

import { getDeviceInfo } from '@/src/utils/device';

const { ScreenCapture } = NativeModules;

export interface ScreenCaptureEvent {
  type: 'started' | 'stopped' | 'denied' | 'error' | 'screenData';
  message?: string;
  imageBytes?: number[];
}

export interface UseScreenCaptureResult {
  isSharing: boolean;
  deviceId: string;
  lastError: string;
  startScreenShare: () => Promise<void>;
  stopScreenShare: () => Promise<void>;
}

export const useScreenCapture = (serverUrl: string): UseScreenCaptureResult => {
    const [isSharing, setIsSharing] = useState(false);
    const [deviceId, setDeviceId] = useState('');
    const [lastError, setLastError] = useState('');

    const socketRef = useRef<Socket | null>(null);
    const deviceIdRef = useRef('');

    // Update ref when deviceId changes
    useEffect(() => {
        deviceIdRef.current = deviceId;
    }, [deviceId]);

    const handleScreenCaptureEvent = useCallback((event: ScreenCaptureEvent) => {
        switch (event.type) {
            case 'started':
                setIsSharing(true);
                setLastError('');
                break;
            case 'stopped':
                setIsSharing(false);
                setLastError('');
                break;
            case 'denied':
                setIsSharing(false);
                setLastError('螢幕擷取權限被拒絕');
                break;
            case 'error':
                setIsSharing(false);
                setLastError(event.message ?? '未知錯誤');
                break;
            case 'screenData':
                if (socketRef.current?.connected && deviceIdRef.current) {
                    socketRef.current.emit('screen-data', {
                        deviceId: deviceIdRef.current,
                        frameData: event.imageBytes,
                    });
                }
                break;
            default:
                break;
        }
    }, []);

    useEffect(() => {
        const socket = io(serverUrl, {
            transports: ['websocket'],
            autoConnect: true,
        });

        socket.on('connect', async () => {
            setDeviceId(socket.id ?? '');
            const deviceInfo = await getDeviceInfo();
            socket.emit('register-device', deviceInfo);
        });

        socket.on('disconnect', () => {
            setDeviceId('');
        });

        socket.on('connect_error', () => {
            setLastError('伺服器連線失敗');
        });

        socketRef.current = socket;

        if (Platform.OS === 'android') {
            const eventEmitter = new NativeEventEmitter();
            const subscription = eventEmitter.addListener(
                'screenCaptureEvent',
                handleScreenCaptureEvent,
            );

            return () => {
                subscription.remove();
                socket.disconnect();
                socketRef.current = null;
            };
        }

        return () => {
            socket.disconnect();
            socketRef.current = null;
        };
    }, [serverUrl, handleScreenCaptureEvent]);

    const startScreenShare = async () => {
        try {
            if (Platform.OS === 'android') {
                await ScreenCapture.startScreenCapture();
            }
            else {
                setLastError('此平台不支援螢幕分享');
            }
        }
        catch (error) {
            // eslint-disable-next-line no-console
            console.error(error);
            setLastError('無法啟動螢幕擷取');
            setIsSharing(false);
        }
    };

    const stopScreenShare = async () => {
        try {
            if (Platform.OS === 'android') {
                await ScreenCapture.stopScreenCapture();
            }
        }
        catch (error) {
            // eslint-disable-next-line no-console
            console.error(error);
            setLastError('無法停止螢幕擷取');
        }
    };

    return {
        isSharing,
        deviceId,
        lastError,
        startScreenShare,
        stopScreenShare,
    };
};
