package emissary.server.mvc;

import emissary.core.EmissaryException;
import emissary.core.Namespace;
import emissary.directory.DirectoryPlace;
import emissary.directory.DirectoryXmlContainer;
import emissary.directory.IDirectoryPlace;
import emissary.server.mvc.adapters.RequestUtil;
import emissary.util.web.HtmlEscaper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
// context is emissary
public class TransferDirectoryAction {
    private static final Logger LOG = LoggerFactory.getLogger(TransferDirectoryAction.class);

    public final String TARGET_DIR_PARAM = "targetDir";

    @GET
    @Path("/TransferDirectory.action")
    @Produces(MediaType.APPLICATION_XML)
    public Response dumpDirectory(@Nullable @QueryParam(TARGET_DIR_PARAM) String dirname) {
        final IDirectoryPlace value;
        String cleanDirectoryName = RequestUtil.sanitizeParameter(dirname);
        try {
            if (cleanDirectoryName == null) {
                LOG.debug("Lookup is using default name since no {} was specified", TARGET_DIR_PARAM);
                value = DirectoryPlace.lookup();
            } else {
                LOG.debug("Lookup is using directory name {}", cleanDirectoryName);
                value = (IDirectoryPlace) Namespace.lookup(cleanDirectoryName);
            }
            if (value != null) {
                LOG.debug("Lookup returned {}", value);
                String msg = DirectoryXmlContainer.toXmlString(value);
                return Response.ok().entity(msg).build();
            } else {
                return Response.status(404).entity("Nothing found for " + HtmlEscaper.escapeHtml(cleanDirectoryName)).build();
            }
        } catch (EmissaryException e) {
            LOG.error("Problem looking up", e);
            return Response.status(500).entity("Directory lookup failed").build();

        }
    }

}
