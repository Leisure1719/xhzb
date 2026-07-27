package com.xhzb.nursing.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "产品列表")
public class ProdoctVo {
    @Schema(title = "产品ID")
    private String productId;

    @Schema(title = "产品名称")
    private String name;
}
