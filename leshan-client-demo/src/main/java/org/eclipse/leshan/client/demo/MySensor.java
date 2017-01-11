package org.eclipse.leshan.client.demo;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by Jon Ayerdi on 11/01/2017.
 */
public class MySensor extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(MyDevice.class);

    int sensorId, groupNo;
    String deviceType, sensorState, userId, lightColor, roomId, behaviorDeployment, ownershipProperty, lightBehavior;
    boolean lowLight;
    float locationX, locationY;

    public MySensor() {
        sensorId = new Random().nextInt(1000);
        deviceType = "Sensor Device";
        sensorState = "FREE";
        userId = "Office-Worker-"+new Random().nextInt(50);
        groupNo = new Random().nextInt(1000);
        locationX = new Random().nextFloat();
        locationY = new Random().nextFloat();
        roomId = "Room-28";
    }

    @Override
    public ReadResponse read(int resourceid) {
        LOG.info("Read on Light Resource " + resourceid);
        switch (resourceid) {
            case 0:
                return ReadResponse.success(resourceid, sensorId);
            case 1:
                return ReadResponse.success(resourceid, deviceType);
            case 2:
                return ReadResponse.success(resourceid, sensorState);
            case 3:
                return ReadResponse.success(resourceid, userId);
            case 4:
                return ReadResponse.success(resourceid, groupNo);
            case 5:
                return ReadResponse.success(resourceid, new Random().nextFloat());
            case 6:
                return ReadResponse.success(resourceid, new Random().nextFloat());
            case 7:
                return ReadResponse.success(resourceid, roomId);
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
                sensorState = (String)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 3:
                userId = (String)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 4:
                groupNo = (Integer)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 5:
                locationX = (Float)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 6:
                locationY = (Float)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            case 7:
                roomId = (String)value.getValue();
                fireResourcesChange(resourceid);
                return WriteResponse.success();
            default:
                return super.write(resourceid, value);
        }
    }

}
