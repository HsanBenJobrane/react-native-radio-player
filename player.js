import { 
    DeviceEventEmitter,
    NativeModules,
    
} from 'react-native'

let PlayerModule = NativeModules.PlayerModule;

class Player {
    constructor() {
        this.isPause = true;
        this.start = this.start.bind(this)
        this.stop = this.stop.bind(this)
        this.setVolume = this.setVolume.bind(this)
        this.addListener = this.addListener.bind(this)
        this.removeEventListener = this.removeEventListener.bind(this)
    }

    start(url) {
        PlayerModule.start(url)
        this.isPause = false;
    }

    stop() {
        PlayerModule.stop()
        this.isPause = true;
    }


    startPlayerService(url) {
        PlayerModule.startPlayerService(url);
    }

    stopPlayerService() {
        PlayerModule.stopPlayerService();
    }

    setVolume(volume = 0) {
        PlayerModule.setVolume(volume)
    }

    addListener(type, cb) {
        switch(type) {
            case "start":
                DeviceEventEmitter.addListener("start", cb)
                break
            case "stop":
                DeviceEventEmitter.addListener("stop", cb)
                break
            case "volume_changed":
                DeviceEventEmitter.addListener("volume_changed", cb)
                break
        }
    }

    removeEventListener(type) {
        DeviceEventEmitter.removeAllListeners(type)
    }
}


export default (new Player());