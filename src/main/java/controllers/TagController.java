package controllers;

import api.ReceiptResponse;
import api.TagResponse;
import dao.ReceiptDao;
import dao.TagDao;
import generated.tables.records.ReceiptsRecord;
import generated.tables.records.TagsRecord;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/tags")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TagController {
    final TagDao tags;
    final ReceiptDao receipts;

    public TagController(TagDao tags, ReceiptDao receipts) {
        this.tags = tags;
        this.receipts = receipts;
    }

    /**
     * PUT tag
     * @param body
     * @param tagName
     */
    @PUT
    @Path("/{tag}")
    public void putTag(String body, @PathParam("tag") String tagName) {
        if(body == null || body.isEmpty()) return;
        tags.putTag(Integer.parseInt(body), tagName);
    }

    @GET
    @Path("/{tag}")
    public List<ReceiptResponse> getReceipts(@PathParam("tag") String tagName) {
        List<TagsRecord> tagsRecords = tags.getAllTagsWith(tagName);
        List<ReceiptsRecord> result = new ArrayList<>();

        if(tagsRecords != null && !tagsRecords.isEmpty()){
            for (TagsRecord tagsRecord : tagsRecords){
                result.addAll(receipts.getAllReceiptsWith(tagsRecord.getReceiptId()));
            }
        }

        return result.stream().map(ReceiptResponse::new).collect(toList());
    }

    @GET
    @Path("/receipts")
    public List<ReceiptResponse> getReceiptsWithTag() {
        List<ReceiptsRecord> receiptsRecords = receipts.getAllReceipts();
        List<ReceiptResponse> response = new ArrayList<>();

        for(ReceiptsRecord receiptsRecord : receiptsRecords){
            List<TagsRecord> tagsRecords = tags.getAllTagsWithReceiptId(receiptsRecord.getId());
            List<String> tags = new ArrayList<>();
            for(TagsRecord tagsRecord : tagsRecords){
                tags.add(tagsRecord.getTag());
            }

            ReceiptResponse receiptResponse = new ReceiptResponse(receiptsRecord);
            receiptResponse.setTag(tags.toString());
            response.add(receiptResponse);
        }
        return response;
    }

    @GET
    @Path("/receiptid/{receiptid}")
    public List<TagResponse> getTag(@PathParam("receiptid") String receiptid) {
        List<TagsRecord> tagsRecords = tags.getAllTagsWithReceiptId(Integer.parseInt(receiptid));
        return tagsRecords.stream().map(TagResponse::new).collect(toList());
    }



    @GET
    public List<TagResponse> getAllTags(){
        List<TagsRecord> tagsRecords = tags.getAllTags();
        return tagsRecords.stream().map(TagResponse::new).collect(toList());
    }
}
