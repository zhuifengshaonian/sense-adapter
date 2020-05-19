/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sense.adapter.core;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Collection;

public class SealedNotificationService implements DOMNotificationService {
  @Override
  public <T extends DOMNotificationListener> ListenerRegistration<T> registerNotificationListener(
      @NonNull T listener, @NonNull Collection<SchemaPath> types) {
    return null;
  }

  @Override
  public <T extends DOMNotificationListener> ListenerRegistration<T> registerNotificationListener(
      @NonNull T listener, SchemaPath... types) {
    return null;
  }
}
