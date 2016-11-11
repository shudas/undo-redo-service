package com.shudas.rewind.webapp.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.shudas.rewind.undoredo.controller.RewindController;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Slf4j
@Path("/")
@Singleton
public class RewindResource {
    @Inject private RewindController rewindController;

    @POST
    @Path("undo/{type}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode undo(@PathParam("type") String type,
                        @PathParam("id") String id,
                        @QueryParam("numUndo") @DefaultValue("1") int numUndo) {
        return rewindController.undo(type, id, numUndo <= 0 ? 1 : numUndo).getValue();
    }

    @POST
    @Path("redo/{type}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode redo(@PathParam("type") String type,
                        @PathParam("id") String id,
                        @QueryParam("numUndo") @DefaultValue("1") int numUndo) {
        return rewindController.redo(type, id, numUndo <= 0 ? 1 : numUndo).getValue();
    }

    @PUT
    @Path("save/{type}/{id}")
    public void save(JsonNode current,
                     @PathParam("type") String type,
                     @PathParam("id") String id,
                     @QueryParam("overrideFrom") long version) {
        rewindController.saveNewObject(type, id, current);
    }
}
