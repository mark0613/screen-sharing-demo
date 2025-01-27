import { Platform } from 'react-native';

import * as Device from 'expo-device';

export interface DeviceInfo {
  platform: string;
  model: string;
  osVersion: string;
}

export const getDeviceInfo = (): DeviceInfo => {
    const model = Device.modelName ?? 'Unknown';
    const osVersion = Device.osVersion ?? 'Unknown';
    const platform = Platform.OS;

    return {
        platform,
        model,
        osVersion,
    };
};
