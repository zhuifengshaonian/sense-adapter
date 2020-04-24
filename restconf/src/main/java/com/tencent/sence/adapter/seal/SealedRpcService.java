/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sence.adapter.seal;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class SealedRpcService implements DOMRpcService {

  @Override
  public @NonNull ListenableFuture<DOMRpcResult> invokeRpc(
      @NonNull SchemaPath type, @Nullable NormalizedNode<?, ?> input) {
    return null;
  }

  @Override
  public @NonNull <T extends DOMRpcAvailabilityListener>
      ListenerRegistration<T> registerRpcListener(@NonNull T listener) {
    return null;
  }
}
