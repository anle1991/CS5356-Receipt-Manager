package api;

import javax.validation.constraints.NotNull;

/**
 * This is an API Object.  It's job is to model and document the JSON API that we expose
 *
 * Fields can be annotated with Validation annotations - these will be applied by the
 * Server when transforming JSON requests into Java objects IFF you specify @Valid in the
 * endpoint.  See {@link controllers.TagController#createTag(CreateTagRequest, String)}  for
 * and example.
 */
public class CreateTagRequest {
    @NotNull
    public Integer receipt_id;
}
