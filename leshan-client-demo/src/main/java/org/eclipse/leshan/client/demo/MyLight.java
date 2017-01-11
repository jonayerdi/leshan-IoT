package org.eclipse.leshan.client.demo;

import java.util.Random;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Jon Ayerdi on 11/01/2017.
 */
public class MyLight extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(MyDevice.class);

    int lightId, groupNo;
    String deviceType, lightState, userType, userId, lightColor, roomId, behaviorDeployment, ownershipProperty, lightBehavior;
    boolean lowLight;
    float locationX, locationY;

    public MyLight() {
        lightId = new Random().nextInt(1000);
        deviceType = "Light Device";
        lightState = "FREE";
        userType = "USER"+(new Random().nextInt(3)+1);
        userId = "Office-Worker-"+new Random().nextInt(50);
        lightColor = "(255,255,255)";
        lowLight = true;
        groupNo = new Random().nextInt(1000);
        locationX = new Random().nextFloat();
        locationY = new Random().nextFloat();
        roomId = "Room-28";
        behaviorDeployment = "Broker";
        ownershipProperty = "";
        lightBehavior = "";
    }

    @Override
    public ReadResponse read(int resourceid) {
        LOG.info("Read on Light Resource " + resourceid);
        switch (resourceid) {
            case 0:
                return ReadResponse.success(resourceid, lightId);
            case 1:
                return ReadResponse.success(resourceid, deviceType);
            case 2:
                return ReadResponse.success(resourceid, lightState);
            case 3:
                return ReadResponse.success(resourceid, userType);
            case 4:
                return ReadResponse.success(resourceid, userId);
            case 5:
                return ReadResponse.success(resourceid, lightColor);
            case 6:
                return ReadResponse.success(resourceid, lowLight);
            case 7:
                return ReadResponse.success(resourceid, groupNo);
            case 8:
                return ReadResponse.success(resourceid, new Random().nextFloat());
            case 9:
                return ReadResponse.success(resourceid, new Random().nextFloat());
            case 10:
                return ReadResponse.success(resourceid, roomId);
            case 11:
                return ReadResponse.success(resourceid, behaviorDeployment);
            default:
                return super.read(resourceid);
        }
    }

    @Override
    public ExecuteResponse execute(int resourceid, String params) {
        LOG.info("Execute on Light resource " + resourceid);
        if (params != null && params.length() != 0)
            System.out.println("\t params " + params);
        return ExecuteResponse.success();
    }

    @Override
    public WriteResponse write(int resourceid, LwM2mResource value) {
        LOG.info("Write on Light Resource " + resourceid + " value " + value);
        switch (resourceid) {
            case 2:
                lightState = (String)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 3:
                userType = (String)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 4:
                userId = (String)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 5:
                lightColor = (String)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 6:
                lowLight = (Boolean)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 7:
                groupNo = (Integer)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 8:
                locationX = (Float)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 9:
                locationY = (Float)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 10:
                roomId = (String)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 11:
                behaviorDeployment = (String)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 12:
                LOG.info("Ownership Priority update:  " + value.getValue());
                return WriteResponse.success();
            case 13:
                LOG.info("Light Behavior update:  " + value.getValue());
                return WriteResponse.success();
            default:
                return super.write(resourceid, value);
        }
    }

}
