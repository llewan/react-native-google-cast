package com.googlecast;

import android.support.annotation.Nullable;
import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.IllegalViewOperationException;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.Cast;

import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.DataCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.DataCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.DataCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.CastException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.OnFailedListener;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

/**
 * Created by Charlie on 5/29/16.
 */
public class GoogleCastModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private DataCastManager mCastManager;
    private DataCastConsumer mCastConsumer;
    Map<String, MediaRouter.RouteInfo> currentDevices = new HashMap<>();
    private WritableMap deviceAvailableParams;


    @VisibleForTesting
    public static final String REACT_CLASS = "GoogleCastModule";
    private static final String DEVICE_AVAILABLE = "GoogleCast:DeviceAvailable";
    private static final String DEVICE_CONNECTED = "GoogleCast:DeviceConnected";
    private static final String DEVICE_DISCONNECTED = "GoogleCast:DeviceDisconnected";
    private static final String MEDIA_LOADED = "GoogleCast:MediaLoaded";

    public GoogleCastModule(ReactApplicationContext reactContext) {
        super(reactContext);
        getReactApplicationContext().addLifecycleEventListener(this);
        final CastConfiguration options = GoogleCastService.getCastConfig();
    }

    @Override
    public String getName() {
        return "GoogleCast";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("DEVICE_AVAILABLE", DEVICE_AVAILABLE);
        constants.put("DEVICE_CONNECTED", DEVICE_CONNECTED);
        constants.put("DEVICE_DISCONNECTED", DEVICE_DISCONNECTED);
        constants.put("MEDIA_LOADED", MEDIA_LOADED);
        return constants;
    }

    private void emitMessageToRN(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    private void addDevice(MediaRouter.RouteInfo info) {
        currentDevices.put(info.getId(), info);
    }

    private void removeDevice(MediaRouter.RouteInfo info) {
        currentDevices.remove(info.getId());
    }

    @ReactMethod
    public void stopScan() {
        Log.e(REACT_CLASS, "Stopping Scan");
        if (mCastManager != null) {
            mCastManager.decrementUiCounter();
        }
    }

    @ReactMethod
    public void getDevices(Promise promise) {
        WritableArray devicesList = Arguments.createArray();
        WritableMap singleDevice = Arguments.createMap();
        try {
            Log.e(REACT_CLASS, "devices size " + currentDevices.size());
            for (MediaRouter.RouteInfo existingChromecasts : currentDevices.values()) {
                singleDevice.putString("id", existingChromecasts.getId());
                singleDevice.putString("name", existingChromecasts.getName());
                devicesList.pushMap(singleDevice);
            }
            promise.resolve(devicesList);
        } catch (IllegalViewOperationException e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void isConnected(Callback successCallback) {
        boolean isConnected = DataCastManager.getInstance().isConnected();
        Log.e(REACT_CLASS, "Am I connected ? " + isConnected);
        successCallback.invoke(isConnected);
    }

    @ReactMethod
    public void connectToDevice(@Nullable String deviceId) {
        Log.e(REACT_CLASS, "received deviceName " + deviceId);
        try {
            Log.e(REACT_CLASS, "devices size " + currentDevices.size());
            MediaRouter.RouteInfo info = currentDevices.get(deviceId);
            CastDevice device = CastDevice.getFromBundle(info.getExtras());
            mCastManager.onDeviceSelected(device, info);
        } catch (IllegalViewOperationException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void disconnect() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    mCastManager.stopApplication();
                    mCastManager.disconnect();
                } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @ReactMethod
    public void sendMessage(@Nullable String message) {
        Log.e(REACT_CLASS, "#sendMessage Chromecast start sendMessage to receiver!" + message);
        if (mCastManager != null) {
            final String msg = message;
            mCastManager = DataCastManager.getInstance();
            UiThreadUtil.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        mCastManager.addNamespace("urn:x-cast:com.google.cast.sample.helloworld");
                        mCastManager.sendDataMessage(msg, "urn:x-cast:com.google.cast.sample.helloworld");
                        Log.e(REACT_CLASS, "#sendMessage Chromecast sendMessage to receiver!");
                    } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
                        e.printStackTrace();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @ReactMethod
    public void startScan() {
        Log.e(REACT_CLASS, "start scan Chromecast ");
        if (mCastManager != null) {
            mCastManager = DataCastManager.getInstance();
            UiThreadUtil.runOnUiThread(new Runnable() {
                public void run() {
                    mCastManager.incrementUiCounter();
                    mCastManager.startCastDiscovery();
                }
            });

            Log.e(REACT_CLASS, "Chromecast Initialized by getting instance");
        } else {
            final CastConfiguration options = GoogleCastService.getCastConfig();
            UiThreadUtil.runOnUiThread(new Runnable() {
                public void run() {
                    DataCastManager.initialize(getCurrentActivity(), options);
                    mCastManager = DataCastManager.getInstance();
                    mCastConsumer = new DataCastConsumerImpl() {

                        @Override
                        public void onConnected() {
                            super.onConnected();
                            Log.e(REACT_CLASS, "Device Connected");
                            emitMessageToRN(getReactApplicationContext(), DEVICE_CONNECTED, null);
                        }

                        @Override
                        public void onDisconnected() {
                            super.onDisconnected();
                            Log.e(REACT_CLASS, "Device Disconnected");
                            emitMessageToRN(getReactApplicationContext(), DEVICE_DISCONNECTED, null);
                        }

                        @Override
                        public void onRouteRemoved(MediaRouter.RouteInfo info) {
                            super.onRouteRemoved(info);
                            removeDevice(info);
                        }

                        @Override
                        public void onCastDeviceDetected(MediaRouter.RouteInfo info) {
                            super.onCastDeviceDetected(info);
                            deviceAvailableParams = Arguments.createMap();
                            Log.e(REACT_CLASS, "detecting devices " + info.getName());
                            deviceAvailableParams.putBoolean("device_available", true);
                            emitMessageToRN(getReactApplicationContext(), DEVICE_AVAILABLE, deviceAvailableParams);
                            addDevice(info);
                        }

                        @Override
                        public void onApplicationConnectionFailed(int errorCode) {
                            Log.e(REACT_CLASS, "I failed :( with error code ");
                        }

                        @Override
                        public void onFailed(int resourceId, int statusCode) {
                            Log.e(REACT_CLASS, "I failed :( " + statusCode);
                        }

                        @Override
                        public void onCastAvailabilityChanged(boolean castPresent) {
                            deviceAvailableParams = Arguments.createMap();
                            Log.e(REACT_CLASS, "onCastAvailabilityChanged: exists? " + Boolean.toString(castPresent));
                            deviceAvailableParams.putBoolean("device_available", castPresent);
                            emitMessageToRN(getReactApplicationContext(), DEVICE_AVAILABLE, deviceAvailableParams);
                        }

                    };
                    mCastManager.addDataCastConsumer(mCastConsumer);
                    mCastManager.incrementUiCounter();
                    mCastManager.startCastDiscovery();
                    Log.e(REACT_CLASS, "Chromecast Initialized for the first time!");
                }
            });
        }
    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {

    }
}