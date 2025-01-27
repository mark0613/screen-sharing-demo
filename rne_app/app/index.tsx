import {
    Button,
    StyleSheet,
    Text,
    View,
} from 'react-native';

import { useScreenCapture } from '@/src/hooks/useScreenCapture';

const SERVER_URL = 'http://10.0.2.2:3000';

const Index = () => {
    const {
        isSharing,
        lastError,
        startScreenShare,
        stopScreenShare,
    } = useScreenCapture(SERVER_URL);

    return (
        <View style={styles.container}>
            <Text style={styles.title}>
                {isSharing ? '正在分享螢幕...' : '螢幕分享已停止'}
            </Text>

            {lastError ? (
                <Text style={styles.error}>{lastError}</Text>
            ) : null}

            <View style={styles.buttonContainer}>
                <Button
                    onPress={isSharing ? stopScreenShare : startScreenShare}
                    title={isSharing ? '停止分享' : '開始分享'}
                />
            </View>
        </View>
    );
};

export default Index;

const styles = StyleSheet.create({
    buttonContainer: {
        marginTop: 20,
    },
    container: {
        alignItems: 'center',
        flex: 1,
        justifyContent: 'center',
        padding: 20,
    },
    error: {
        color: 'red',
        marginBottom: 10,
    },
    title: {
        fontSize: 24,
        marginBottom: 10,
    },
});
