import { NativeModules } from 'react-native';

const { GoogleCast } = NativeModules;

export default {
  startScan: function () {
	GoogleCast.startScan();
  },
  stopScan: function () {
	GoogleCast.stopScan();
  },
  isConnected: function (callback: (x: boolean) => boolean) {
	  return GoogleCast.isConnected(callback);
  },
  getDevices: function () {
	  return GoogleCast.getDevices();
  },
  sendMessage: function (msg: string) {
    GoogleCast.sendMessage(msg);
  },
  connectToDevice: function (deviceId: string) {
	GoogleCast.connectToDevice(deviceId);
  },
  disconnect: function () {
	GoogleCast.disconnect();
  },
  castMedia: function (mediaUrl: string, title: string, imageUrl: string, seconds: number = 0) {
	GoogleCast.castMedia(mediaUrl, title, imageUrl, seconds);
  },
  DEVICE_AVAILABLE: GoogleCast.DEVICE_AVAILABLE,
  DEVICE_CONNECTED: GoogleCast.DEVICE_CONNECTED,
  DEVICE_DISCONNECTED: GoogleCast.DEVICE_DISCONNECTED
};