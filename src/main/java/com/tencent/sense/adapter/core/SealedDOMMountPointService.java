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
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import java.util.Optional;

public class SealedDOMMountPointService implements DOMMountPointService {
  @Override
  public Optional<DOMMountPoint> getMountPoint(YangInstanceIdentifier path) {
    DOMMountPoint mountPoint =
        new DOMMountPoint() {
          @Override
          public YangInstanceIdentifier getIdentifier() {
            return path;
          }

          @Override
          public <T extends DOMService> Optional<T> getService(Class<T> cls) {
            return Optional.of(null);
          }

          @Override
          public SchemaContext getSchemaContext() {
            return null;
          }
        };

    return Optional.empty();
  }

  @Override
  public DOMMountPointBuilder createMountPoint(YangInstanceIdentifier path) {
    return null;
  }

  @Override
  public ListenerRegistration<DOMMountPointListener> registerProvisionListener(
      DOMMountPointListener listener) {
    return null;
  }
}
