package com.paymentchain.product.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
The effort to standardize rest API error reports  is support by ITEF
(Internet Engineering Task Force, open standard organization  that which develop and promotes voluntary internet standards)
in RFC 7807 which created a generalized error-handling schema composed by five parts.
1- type — A URI identifier that categorizes the error
2-title — A brief, human-readable message about the error
3-code —  The unique error code
4-detail — A human-readable explanation of the error
5-instance — A URI that identifies the specific occurrence of the error
Standarized is optional but have advantage, it is use for facebook and twitter ie
 https://graph.facebook.com/oauth/access_token
 https://api.twitter.com/1.1/statuses/update.json?include_entities=true
 */

@Getter
@Setter
public class StandardizedApiExceptionResponse {

    @Schema(description = "The unique uri identifier that categorizes the error", name = "type",
            required = true, example = "/errors/authentication/not-authorized")
    private String type ="/errors/uncategorized";


    @Schema(description = "A brief, human-readable message about the error", name = "title",
            required = true, example = "The user does not have authorization")
    private String title;


    @Schema(description = "The unique error code", name = "code",
            required = false, example = "192")
    private String code;


    @Schema(description = "A human-readable explanation of the error", name = "detail",
            required = true, example = "The user does not have the property permissions to access the "
            + "resource, please contact with https://digitalthinking.biz/es/ada-enterprise-core#contactus")
    private String detail;


    @Schema(description = "A URI that identifies the specific occurrence of the error", name = "detail",
            required = true, example = "/errors/authentication/not-authorized/01")
    private String instance ="/errors/uncategorized/bank";

    public StandardizedApiExceptionResponse(String title, String code, String detail) {
        super();
        this.title = title;
        this.code = code;
        this.detail = detail;
    }
}
