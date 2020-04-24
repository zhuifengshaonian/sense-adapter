/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sence.adapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.MutableClassToInstanceMap;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.mdsal.dom.spi.SimpleDOMMountPoint;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DOMMountPointServiceImpl implements DOMMountPointService {
  private static final Logger LOG = LoggerFactory.getLogger(DOMMountPointServiceImpl.class);
  private final Map<YangInstanceIdentifier, DOMMountPoint> mountPoints = new HashMap();
  private final ListenerRegistry<DOMMountPointListener> listeners = ListenerRegistry.create();

  public DOMMountPointServiceImpl() {}

  public Optional<DOMMountPoint> getMountPoint(final YangInstanceIdentifier path) {
    return Optional.ofNullable((DOMMountPoint) this.mountPoints.get(path));
  }

  public DOMMountPointBuilder createMountPoint(final YangInstanceIdentifier path) {
    Preconditions.checkState(!this.mountPoints.containsKey(path), "Mount point already exists");
    return new DOMMountPointServiceImpl.DOMMountPointBuilderImpl(path);
  }

  public ListenerRegistration<DOMMountPointListener> registerProvisionListener(
      final DOMMountPointListener listener) {
    return this.listeners.register(listener);
  }

  private ObjectRegistration<DOMMountPoint> registerMountPoint(
      final SimpleDOMMountPoint mountPoint) {
    YangInstanceIdentifier mountPointId = mountPoint.getIdentifier();
    synchronized (this.mountPoints) {
      DOMMountPoint prev = (DOMMountPoint) this.mountPoints.putIfAbsent(mountPointId, mountPoint);
      Preconditions.checkState(
          prev == null, "Mount point %s already exists as %s", mountPointId, prev);
    }

    this.listeners
        .getRegistrations()
        .forEach(
            (listener) -> {
              try {
                ((DOMMountPointListener) listener.getInstance()).onMountPointCreated(mountPointId);
              } catch (Exception var4) {
                LOG.error(
                    "Listener {} failed on mount point {} created event",
                    new Object[] {listener, mountPoint, var4});
              }
            });
    return new AbstractObjectRegistration<DOMMountPoint>(mountPoint) {
      protected void removeRegistration() {
        DOMMountPointServiceImpl.this.unregisterMountPoint(
            (YangInstanceIdentifier) ((DOMMountPoint) this.getInstance()).getIdentifier());
      }
    };
  }

  private void unregisterMountPoint(final YangInstanceIdentifier mountPointId) {
    synchronized (this.mountPoints) {
      if (this.mountPoints.remove(mountPointId) == null) {
        LOG.warn("Removing non-existent mount point {} at", mountPointId, new Throwable());
        return;
      }
    }

    this.listeners
        .getRegistrations()
        .forEach(
            (listener) -> {
              try {
                ((DOMMountPointListener) listener.getInstance()).onMountPointRemoved(mountPointId);
              } catch (Exception var3) {
                LOG.error(
                    "Listener {} failed on mount point {} removed event",
                    new Object[] {listener, mountPointId, var3});
              }
            });
  }

  private final class DOMMountPointBuilderImpl implements DOMMountPointBuilder {
    private final MutableClassToInstanceMap<DOMService> services =
        MutableClassToInstanceMap.create();
    private final YangInstanceIdentifier path;
    private SchemaContext schemaContext;
    private SimpleDOMMountPoint mountPoint;

    DOMMountPointBuilderImpl(final YangInstanceIdentifier path) {
      this.path = (YangInstanceIdentifier) Objects.requireNonNull(path);
    }

    public <T extends DOMService> DOMMountPointBuilder addService(
        final Class<T> type, final T impl) {
      this.services.putInstance(
          (Class) Objects.requireNonNull(type), (DOMService) Objects.requireNonNull(impl));
      return this;
    }

    public DOMMountPointBuilder addInitialSchemaContext(final SchemaContext ctx) {
      this.schemaContext = (SchemaContext) Objects.requireNonNull(ctx);
      return this;
    }

    public ObjectRegistration<DOMMountPoint> register() {
      Preconditions.checkState(this.mountPoint == null, "Mount point is already built.");
      this.mountPoint = SimpleDOMMountPoint.create(this.path, this.services, this.schemaContext);
      return DOMMountPointServiceImpl.this.registerMountPoint(this.mountPoint);
    }
  }
}
