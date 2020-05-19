package com.tencent.sense.adapter;

import com.tencent.sense.adapter.controller.NcTopologyController;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class ConnectNcDeviceTests {

    @Test
    public void testDevInit(){
        try {
            new NcTopologyController().testOnDataTreeChange();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

