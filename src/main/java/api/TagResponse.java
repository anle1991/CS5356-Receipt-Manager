package api;

import com.fasterxml.jackson.annotation.JsonProperty;
import generated.tables.records.ReceiptsRecord;
import generated.tables.records.TagsRecord;

import java.math.BigDecimal;
import java.sql.Time;

/**
 * This is an API Object.  Its purpose is to model the JSON API that we expose.
 * This class is NOT used for storing in the Database.
 *
 * This ReceiptResponse in particular is the model of a Receipt that we expose to users of our API
 *
 * Any properties that you want exposed when this class is translated to JSON must be
 * annotated with {@link JsonProperty}
 */
public class TagResponse {
    @JsonProperty
    Integer id;

    @JsonProperty
    Integer receiptId;

    @JsonProperty
    String tag;

    public TagResponse(TagsRecord dbRecord) {
        this.receiptId = dbRecord.getReceiptId();
        this.tag = dbRecord.getTag();
        this.id = dbRecord.getId();
    }
}
