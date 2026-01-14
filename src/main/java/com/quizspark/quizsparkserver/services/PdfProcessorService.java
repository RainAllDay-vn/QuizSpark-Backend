package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.ai.ProcessedPageDTO;
import com.quizspark.quizsparkserver.models.DbFile;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.model.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

@Service
public class PdfProcessorService {
    public List<ProcessedPageDTO> splitPages(DbFile dbFile) {
        List<ProcessedPageDTO> pages = new ArrayList<>();

        try (PDDocument document = PDDocument.load(dbFile.getData())) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageNum = 0;
            for (PDPage page : document.getPages()) {
                pageNum++;
                // 1. Image Detection
                int significantImages = 0;
                PDResources resources = page.getResources();
                if (resources != null) {
                    for (COSName xObjectName : resources.getXObjectNames()) {
                        PDXObject xObject = resources.getXObject(xObjectName);
                        if (xObject instanceof PDImageXObject image) {
                            if (image.getWidth() > 50 && image.getHeight() > 50) {
                                significantImages++;
                            }
                        }
                    }
                }

                // 2. Annotation Detection
                boolean hasVisualAnnotations = false;
                List<PDAnnotation> annotations = page.getAnnotations();
                if (annotations != null) {
                    for (PDAnnotation annotation : annotations) {
                        String subtype = annotation.getSubtype();
                        if ("Highlight".equals(subtype) || "Ink".equals(subtype) || "Square".equals(subtype)
                                || "Circle".equals(subtype) || "Line".equals(subtype) || "Polygon".equals(subtype)
                                || "PolyLine".equals(subtype)) {
                            hasVisualAnnotations = true;
                            break;
                        }
                    }
                }

                // 3. Text Extraction
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
                String text = stripper.getText(document).trim();

                // 4. Decision & Extraction
                if (significantImages > 0 || hasVisualAnnotations || text.length() <= 50) {
                    // VISION Strategy
                    pages.add(new ProcessedPageDTO(text, true));
                } else {
                    // TEXT Strategy
                    pages.add(new ProcessedPageDTO(text, false));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing PDF: " + e.getMessage(), e);
        }
        return pages;
    }

    public List<byte[]> convertToImages(DbFile dbFile) {
        List<byte[]> images = new ArrayList<>();
        try (PDDocument document = PDDocument.load(dbFile.getData())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bim, "png", baos);
                images.add(baos.toByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error converting PDF to images: " + e.getMessage(), e);
        }
        return images;
    }

    public List<Media> convertToMedias(DbFile dbFile, List<Integer> indexes) {
        try (PDDocument document = PDDocument.load(dbFile.getData())) {
            List<Media> images = new ArrayList<>();
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int index : indexes) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(index, 300, ImageType.RGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bim, "png", baos);
                images.add(new Media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(baos.toByteArray())));
            }
            return images;
        } catch (IOException e) {
            throw new RuntimeException("Error processing PDF: " + e.getMessage(), e);
        }
    }
}
