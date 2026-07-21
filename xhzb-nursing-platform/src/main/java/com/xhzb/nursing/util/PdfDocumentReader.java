package com.xhzb.nursing.util;

import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.document.Document;


import java.util.List;

public class PdfDocumentReader {
    public static List<Document> getDocsFromPdf(String url) {

        org.springframework.ai.reader.pdf.PagePdfDocumentReader pdfReader = new org.springframework.ai.reader.pdf.PagePdfDocumentReader(url,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build());

        return pdfReader.read();
    }

}