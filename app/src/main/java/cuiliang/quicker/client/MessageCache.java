package cuiliang.quicker.client;

import cuiliang.quicker.messages.recv.UpdateButtonsMessage;
import cuiliang.quicker.messages.recv.VolumeStateMessage;

/**
 * 应用状态
 */
public class MessageCache {
//    public SparseArray buttons = new SparseArray<UpdateButtonsMessage.ButtonItem>();
//
//    public boolean mute;
//
//    public int masterVolume;
//
//    public String profileName;

    public UpdateButtonsMessage lastUpdateButtonsMessage = null;

    public VolumeStateMessage lastVolumeStateMessage = null;

//    public void processUpdateButtonsMessage(UpdateButtonsMessage msg){
//        this.lastUpdateButtonsMessage = msg;
//    }
//
//    public void processVolumeStateMessage(VolumeStateMessage msg){
//        this.lastVolumeStateMessage = msg;
//    }

}
