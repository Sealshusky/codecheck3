package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.horstmann.codecheck.checker.Util;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import services.ServiceException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

@RequestScoped
@jakarta.ws.rs.Path("/")
public class AssignmentController {
    @Inject services.Assignment assignmentService;
    @Context UriInfo uriInfo;
    @Context HttpHeaders headers;
    @CookieParam("ccid") String ccid;
    @CookieParam("cckey") String editKey;

    @GET
    @jakarta.ws.rs.Path("/newAssignment")
    @Produces(MediaType.TEXT_HTML)
    public Response edit() throws IOException {
        return edit(null, null);
    }

    @GET
    @jakarta.ws.rs.Path("/private/editAssignment/{assignmentID}/{key}")
    @Produces(MediaType.TEXT_HTML)
    public Response edit(@PathParam("assignmentID") String assignmentID, @PathParam("key") String key) throws IOException {
        try {
            String result = assignmentService.edit(assignmentID, key);
            return Response.ok(result).build();
        } catch (ServiceException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @GET
    @jakarta.ws.rs.Path("/copyAssignment/{assignmentID}")
    @Produces(MediaType.TEXT_HTML)
    public Response edit(@PathParam("assignmentID") String assignmentID) throws IOException {
        return edit(assignmentID, null);
    }

    @GET
    @jakarta.ws.rs.Path("/assignment/{assignmentID}")
    @Produces(MediaType.TEXT_HTML)
    public Response studentStartsWork(@PathParam("assignmentID") String assignmentID) throws IOException, GeneralSecurityException {
        try {
            String prefix = HttpUtil.prefix(uriInfo, headers);
            ccid = Util.createPronouncableUID();
            editKey = Util.createPrivateUID();
            String result = assignmentService.work(prefix, assignmentID, ccid, editKey, true /* student */, false /* editKeySaved */);
            return Response.ok(result)
                    .cookie(HttpUtil.buildCookie("ccid", ccid))
                    .cookie(HttpUtil.buildCookie("cckey", editKey))
                    .build();
        }
        catch (ServiceException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    // TODO: Hacky. Change route name to /changeAssignmentID?
    @GET
    @jakarta.ws.rs.Path("/assignment/{assignmentID}/{ccid}")
    @Produces(MediaType.TEXT_HTML)
    public Response studentChangesID(@PathParam("assignmentID") String assignmentID, @PathParam("ccid") String ccid) throws IOException, GeneralSecurityException {
        try {
            String prefix = HttpUtil.prefix(uriInfo, headers);
            editKey = Util.createPrivateUID();
            String result = assignmentService.work(prefix, assignmentID, ccid, editKey, true /* student */, false /* editKeySaved */);
            return Response.ok(result)
                    .cookie(HttpUtil.buildCookie("ccid", ccid))
                    .cookie(HttpUtil.buildCookie("cckey", editKey))
                    .build();
        }
        catch (ServiceException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @GET
    @jakarta.ws.rs.Path("/private/resume/{assignmentID}/{ccid}/{key}")
    @Produces(MediaType.TEXT_HTML)
    public Response studentResumesWork(@PathParam("assignmentID") String assignmentID, @PathParam("ccid") String ccid, @PathParam("key") String editKey) throws IOException, GeneralSecurityException {
        try {
            String prefix = HttpUtil.prefix(uriInfo, headers);
            String result = assignmentService.work(prefix, assignmentID, ccid, editKey, true /* student */, true /* editKeySaved */);
            return Response.ok(result)
                    .cookie(HttpUtil.buildCookie("ccid", ccid))
                    .cookie(HttpUtil.buildCookie("cckey", editKey))
                    .build();
        }
        catch (ServiceException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @GET
    @jakarta.ws.rs.Path("/private/submission/{assignmentID}/{ccid}/{key}")
    @Produces(MediaType.TEXT_HTML)
    public Response instructorViewsStudentWork(@PathParam("assignmentID") String assignmentID, @PathParam("ccid") String ccid, @PathParam("key") String editKey) throws IOException, GeneralSecurityException {
        try {
            String prefix = HttpUtil.prefix(uriInfo, headers);
            String result = assignmentService.work(prefix, assignmentID, ccid, editKey, false /* student */, false /* editKeySaved */);
            return Response.ok(result).build();
        }
        catch (ServiceException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @GET
    @jakarta.ws.rs.Path("/viewAssignment/{assignmentID}")
    @Produces(MediaType.TEXT_HTML)
    public Response instructorViewsOtherAssignment(@PathParam("assignmentID") String assignmentID) throws IOException, GeneralSecurityException {
        try {
            String prefix = HttpUtil.prefix(uriInfo, headers);
            String result = assignmentService.work(prefix, assignmentID, null /* ccid */, null /* editKey */, false /* student */, false /* editKeySaved */);
            return Response.ok(result).build();
        }
        catch (ServiceException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @GET
    @jakarta.ws.rs.Path("/private/assignment/{assignmentID}/{key}")
    @Produces(MediaType.TEXT_HTML)
    public Response instructorViewsOwnAssignment(@PathParam("assignmentID") String assignmentID, @PathParam("key") String editKey) throws IOException, GeneralSecurityException {
        try {
            String prefix = HttpUtil.prefix(uriInfo, headers);
            String result = assignmentService.work(prefix, assignmentID, null /* ccid */, editKey, false /* student */, false /* editKeySaved */);
            return Response.ok(result).build();
        } catch (ServiceException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @GET
    @jakarta.ws.rs.Path("/private/viewSubmissions/{assignmentID}/{key}")
    @Produces(MediaType.TEXT_HTML)
    public Response instructorViewsSubmissions(@PathParam("assignmentID") String assignmentID, @PathParam("key") String editKey) throws IOException, GeneralSecurityException {
        try {
            String result = assignmentService.viewSubmissions(assignmentID, editKey);
            return Response.ok(result).build();
        } catch (ServiceException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @POST
    @jakarta.ws.rs.Path("/saveAssignment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response instructorSavesAssignment(JsonNode params) throws IOException {
        try {
            String prefix = HttpUtil.prefix(uriInfo, headers);
            ObjectNode result = assignmentService.saveAssignment(prefix, params);
            return Response.ok(result).build();
        } catch (ServiceException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @POST
    @jakarta.ws.rs.Path("/saveComment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response instructorSavesComment(JsonNode params) throws IOException {
        try {
            ObjectNode result = assignmentService.saveComment(params);
            return Response.ok(result).build();
        } catch (ServiceException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @POST
    @jakarta.ws.rs.Path("/saveWork")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response studentSavesWork(JsonNode params) throws IOException, NoSuchAlgorithmException {
        try {
            ObjectNode result = assignmentService.saveWork(params);
            return Response.ok(result).build();
        } catch (ServiceException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }
}
