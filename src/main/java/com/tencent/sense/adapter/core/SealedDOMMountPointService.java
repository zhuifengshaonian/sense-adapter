/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sense.adapter.core;

import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.mdsal.dom.broker.DOMMountPointServiceImpl;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SealedDOMMountPointService extends DOMMountPointServiceImpl{

    private static Map<String, YangInstanceIdentifier> temp = new HashMap<String, YangInstanceIdentifier>(10);

    @Override
    public Optional<DOMMountPoint> getMountPoint(YangInstanceIdentifier path) {
        System.out.println("getmp: "+path);
        return super.getMountPoint(path);
    }

    @Override
    public DOMMountPointBuilder createMountPoint(YangInstanceIdentifier path) {
        System.out.println("createmp: "+path);
        temp.put(((YangInstanceIdentifier.NodeIdentifierWithPredicates.Singleton)(path.getPathArguments().get(4))).values().toArray()[0].toString(), path);
        return super.createMountPoint(path);
    }

    @Override
    public ListenerRegistration<DOMMountPointListener> registerProvisionListener(DOMMountPointListener listener) {
        System.out.println("reg listener: "+listener);
        return super.registerProvisionListener(listener);
    }

    public Optional<DOMMountPoint> getMountPoint(String shortName) {
        return this.getMountPoint(temp.get(shortName));
    }
}
