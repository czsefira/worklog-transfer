package cz.sefira.jira.wltransfer.rest;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import cz.sefira.jira.wltransfer.services.WLTransferService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/worklogs")
public class WorklogResource {

  private final WLTransferService wlTransferService;
  private final DateTimeFormatter dateTimeFormatter;

  public WorklogResource(WLTransferService wlts, DateTimeFormatter dateTimeFormatter) {
    this.wlTransferService = wlts;
    this.dateTimeFormatter = dateTimeFormatter;
  }

  @GET
  @AnonymousAllowed
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPost(@QueryParam("issuekey") String issuekey, @QueryParam("username") String user) {
    if (issuekey == null)
      return Response.noContent().build();
    return Response.ok(new WorklogGroupRepresentation(wlTransferService.getWorklogs(issuekey, user), dateTimeFormatter)).build();
  }
}
