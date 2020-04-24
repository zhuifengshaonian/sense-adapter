/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v10.html
 */
package com.tencent.sence.adapter.swagger;

import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.opendaylight.netconf.sal.rest.doc.api.ApiDocService;
import org.opendaylight.netconf.sal.rest.doc.impl.ApiDocGeneratorDraftO2;
import org.opendaylight.netconf.sal.rest.doc.impl.ApiDocGeneratorRFC8040;
import org.opendaylight.netconf.sal.rest.doc.impl.MountPointSwaggerGeneratorDraft02;
import org.opendaylight.netconf.sal.rest.doc.impl.MountPointSwaggerGeneratorRFC8040;
import org.opendaylight.netconf.sal.rest.doc.jaxrs.ApiDocApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SwaggerApidocService {

  private static final String APIDOC_PATH = "/apidoc";
  private static final String TRUE = "true";

  @Autowired private MountPointSwaggerGeneratorDraft02 mountPointSwaggerGeneratorDraft02;
  @Autowired private MountPointSwaggerGeneratorRFC8040 mountPointSwaggerGeneratorRFC8040;
  @Autowired private ApiDocService apiDocService;

  public boolean init() {
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    ServletContextHandler mainHandler =
        new ServletContextHandler(contexts, APIDOC_PATH, true, false);

    String basePathString =
        restConfConfiguration.getRestconfServletContextPath().replaceAll("^/+", "");

    switch (restConfConfiguration.getJsonRestconfServiceType()) {
      case DRAFT_02:
        {
          mountPointSwaggerGeneratorDraft02 =
              new MountPointSwaggerGeneratorDraft02(
                  lightyServices.getDOMSchemaService(),
                  lightyServices.getDOMMountPointService(),
                  basePathString);
          ApiDocGeneratorDraftO2 apiDocGeneratorDraft02 =
              new ApiDocGeneratorDraftO2(lightyServices.getDOMSchemaService(), basePathString);
          apiDocService =
              new ApiDocServiceDraft02(mountPointSwaggerGeneratorDraft02, apiDocGeneratorDraft02);
          ApiDocApplication apiDocApplication = new ApiDocApplication(apiDocService);

          ServletContainer restServletContainer =
              new ServletContainer(ResourceConfig.forApplication(apiDocApplication));
          ServletHolder restServletHolder = new ServletHolder(restServletContainer);

          LOG.info(
              "initializing swagger doc generator at http(s)://{hostname:port}{}/apis",
              APIDOC_PATH);
          mainHandler.addServlet(restServletHolder, "/apis/*");

          addStaticResources(mainHandler, "/explorer", "static-content-02");
          break;
        }
      case DRAFT_18:
        {
          mountPointSwaggerGeneratorRFC8040 =
              new MountPointSwaggerGeneratorRFC8040(
                  lightyServices.getDOMSchemaService(),
                  lightyServices.getDOMMountPointService(),
                  basePathString);
          ApiDocGeneratorRFC8040 apiDocGeneratorRFC8040 =
              new ApiDocGeneratorRFC8040(lightyServices.getDOMSchemaService(), basePathString);
          apiDocService =
              new io.lighty.swagger.ApiDocServiceRFC8040(
                  mountPointSwaggerGeneratorRFC8040, apiDocGeneratorRFC8040);
          ApiDocApplication apiDocApplication = new ApiDocApplication(apiDocService);

          ServletContainer restServletContainer =
              new ServletContainer(ResourceConfig.forApplication(apiDocApplication));
          ServletHolder restServletHolder = new ServletHolder(restServletContainer);

          LOG.info(
              "initializing swagger doc generator at http(s)://{hostname:port}{}/18/apis",
              APIDOC_PATH);
          mainHandler.addServlet(restServletHolder, "/18/apis/*");

          addStaticResources(mainHandler, "/18/explorer", "static-content-18");
          addStaticResources(mainHandler, "/explorer", "static-content-02");
          break;
        }
      default:
        throw new UnsupportedOperationException(
            "Unsupported restconf service type: "
                + restConfConfiguration.getJsonRestconfServiceType());
    }
    LOG.info("adding context handler ...");
    jettyServerBuilder.addContextHandler(contexts);
    return true;
  }

  @Override
  protected boolean stopProcedure() {
    LOG.info("shutting down swagger ...");
    if (mountPointSwaggerGeneratorDraft02 != null) {
      mountPointSwaggerGeneratorDraft02.close();
    }
    if (mountPointSwaggerGeneratorRFC8040 != null) {
      mountPointSwaggerGeneratorRFC8040.close();
    }
    return true;
  }

  private void addStaticResources(
      ServletContextHandler mainHandler, String path, String servletName) {
    LOG.info(
        "initializing swagger UI at: http(s)://{hostname:port}{}/{}index.html", APIDOC_PATH, path);
    String externalResource =
        io.lighty.swagger.SwaggerLighty.class.getResource(path).toExternalForm();
    LOG.info("externalResource: {}", externalResource);
    DefaultServlet defaultServlet = new DefaultServlet();
    ServletHolder holderPwd = new ServletHolder(servletName, defaultServlet);
    holderPwd.setInitParameter("resourceBase", externalResource);
    holderPwd.setInitParameter("dirAllowed", TRUE);
    holderPwd.setInitParameter("pathInfoOnly", TRUE);
    mainHandler.addServlet(holderPwd, path + "/*");
  }

  @RequestMapping("/api")
  ApiDocService getApiDocService() {
    return apiDocService;
  }
}
