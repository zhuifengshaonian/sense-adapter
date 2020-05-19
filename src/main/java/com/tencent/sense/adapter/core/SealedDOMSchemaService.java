/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sense.adapter.core;

import com.google.common.collect.ClassToInstanceMap;
import org.opendaylight.mdsal.dom.api.DOMSchemaServiceExtension;
import org.opendaylight.mdsal.dom.broker.schema.ScanningSchemaServiceProvider;

public class SealedDOMSchemaService extends ScanningSchemaServiceProvider {

  @Override
  public ClassToInstanceMap<DOMSchemaServiceExtension> getExtensions() {
    System.out.println("take over by sealed DOMSchemaService");
    return super.getExtensions();
  }
}
