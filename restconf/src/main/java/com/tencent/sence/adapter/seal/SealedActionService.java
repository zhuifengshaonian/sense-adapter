/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sence.adapter.seal;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMActionServiceExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class SealedActionService implements DOMActionService {
  @Override
  public ListenableFuture<? extends DOMActionResult> invokeAction(
      SchemaPath type, DOMDataTreeIdentifier path, ContainerNode input) {
    return null;
  }

  @Override
  public @NonNull ClassToInstanceMap<DOMActionServiceExtension> getExtensions() {
    return null;
  }
}
