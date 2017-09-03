package dao;

import generated.tables.records.TagsRecord;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

import static generated.Tables.TAGS;

public class TagDao {
    DSLContext dsl;

    public TagDao(Configuration jooqConfig) {
        this.dsl = DSL.using(jooqConfig);
    }

    public void putTag(Integer receipt_id, String tag) {
        List<TagsRecord> thisTag = dsl.selectFrom(TAGS).where(TAGS.RECEIPT_ID.eq(receipt_id).and(TAGS.TAG.eq(tag))).fetch();

        if (thisTag.size() == 0){
            TagsRecord tagsRecord = dsl
                    .insertInto(TAGS, TAGS.RECEIPT_ID, TAGS.TAG)
                    .values(receipt_id, tag)
                    .returning(TAGS.ID)
                    .fetchOne();
        }else{
            for(TagsRecord tagsRecord : thisTag){
                dsl.executeDelete(tagsRecord);
            }
        }
    }

    public List<TagsRecord> getAllTags() {
        return dsl.selectFrom(TAGS).fetch();
    }

    public List<TagsRecord> getAllTagsWith(String tag) {
        return dsl.selectFrom(TAGS).where(TAGS.TAG.eq(tag)).fetch();
    }
}
