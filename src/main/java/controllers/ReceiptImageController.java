package controllers;

import api.ReceiptSuggestionResponse;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.hibernate.validator.constraints.NotEmpty;

import static java.lang.System.out;

@Path("/images")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
public class ReceiptImageController {
    private final AnnotateImageRequest.Builder requestBuilder;

    public ReceiptImageController() {
        // DOCUMENT_TEXT_DETECTION is not the best or only OCR method available
        Feature ocrFeature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        this.requestBuilder = AnnotateImageRequest.newBuilder().addFeatures(ocrFeature);

    }

    /**
     * This borrows heavily from the Google Vision API Docs.  See:
     * https://cloud.google.com/vision/docs/detecting-fulltext
     *
     * YOU SHOULD MODIFY THIS METHOD TO RETURN A ReceiptSuggestionResponse:
     *
     * public class ReceiptSuggestionResponse {
     *     String merchantName;
     *     String amount;
     * }
     */
    @POST
    public ReceiptSuggestionResponse parseReceipt(@NotEmpty String base64EncodedImage) throws Exception {
        Image img = Image.newBuilder().setContent(ByteString.copyFrom(Base64.getDecoder().decode(base64EncodedImage))).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(Collections.singletonList(request));
            List<AnnotateImageResponse> responses = response.getResponsesList();

            AnnotateImageResponse res = responses.get(0);

            String merchantName = "";
            BigDecimal amount = null;

            if(res == null || res.hasError()){
                out.printf("Error: %s\n", res.getError().getMessage());
            }

            // Your Algo Here!!
            // Sort text annotations by bounding polygon.  Top-most non-decimal text is the merchant
            // bottom-most decimal text is the total amount
            TextAnnotation textAnnotation = res.getFullTextAnnotation();

            if(textAnnotation != null && textAnnotation.getText() != null && !textAnnotation.getText().isEmpty()){
                String fullText = textAnnotation.getText();
                String[] lines = fullText.split("\n");

                for(int i = 0; i < lines.length; i++){
                    out.printf("%d: %s \n", i, lines[i]);

                    // Set Merchant Name
                    if(validatePhoneNumber(lines[i]) != null){
                        out.println("Found phone number!");

                        if(i > 3){
                            merchantName = lines[0];
                        }else{
                            StringBuilder sb = new StringBuilder();
                            for(int j = 0; j < i; j++){
                                sb.append(lines[j]);
                            }
                            merchantName = sb.toString();
                        }
                    }

                    String price = matchPriceTag(lines[i]);
                    if(price != null){
                        if(price.charAt(0) == '$'){
                            price = price.substring(1);
                        }
                        amount = new BigDecimal(price);
                    }

                }

                for(int i = lines.length - 1; i >=0 ;i--){
                    String totalPrice = matchTotalPriceTag(lines[i]);
                    if(totalPrice != null){
                        if(totalPrice.charAt(0) == '$'){
                            totalPrice = totalPrice.substring(1);
                        }
                        amount = new BigDecimal(totalPrice);
                        break;
                    }
                }
            }

            System.out.println("Merchant name: "+ merchantName);
            System.out.println("Price: "+ amount);

            return new ReceiptSuggestionResponse(merchantName, amount);
        }
    }

    private static String matchInLine(String str, String regExp){
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        if(m.find()){
            System.out.println(m.group(0));

            return m.group();
        }
        return null;
    }

    private static String validatePhoneNumber(String phoneNo) {
        return matchInLine(phoneNo, "\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}|\\d{10}|\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}|\\(\\d{3}\\)-\\d{3}-\\d{4}");
    }

    private static String matchPriceTag(String str) {
        return matchInLine(str, "\\$?\\d+[\\,\\.]\\d{1,2}?$");
    }

    private static String matchTotalPriceTag(String str) {
        boolean totalMatch = Pattern.compile(Pattern.quote("total"), Pattern.CASE_INSENSITIVE).matcher(str).find();
        String price = matchPriceTag(str);

        if(totalMatch){
            return price;
        }

        return null;
    }
}
