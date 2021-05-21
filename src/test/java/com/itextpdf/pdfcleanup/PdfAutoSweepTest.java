/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.pdfcleanup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.CompressionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfcleanup.autosweep.CompositeCleanupStrategy;
import com.itextpdf.pdfcleanup.autosweep.PdfAutoSweep;
import com.itextpdf.pdfcleanup.autosweep.RegexBasedCleanupStrategy;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

@Category(IntegrationTest.class)
public class PdfAutoSweepTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/PdfAutoSweepTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/PdfAutoSweepTest/";


    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void redactLipsum() throws IOException, InterruptedException {
        String input = inputPath + "Lipsum.pdf";
        String output = outputPath + "cleanUpDocument.pdf";
        String cmp = inputPath + "cmp_cleanUpDocument.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(D|d)olor").setRedactionColor(ColorConstants.GREEN));

        PdfWriter writer = new PdfWriter(output);
        writer.setCompressionLevel(0);
        PdfDocument pdf = new PdfDocument(new PdfReader(input), writer);

        // sweep
        PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
        autoSweep.cleanUp(pdf);

        pdf.close();

        // compare
        compareByContent(cmp, output, outputPath, "diff_cleanUpDocument_");
    }

    @Test
    public void cleanUpPageTest() throws IOException, InterruptedException {
        String input = inputPath + "Lipsum.pdf";
        String output = outputPath + "cleanUpPage.pdf";
        String cmp = inputPath + "cmp_cleanUpPage.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(D|d)olor").setRedactionColor(ColorConstants.GREEN));

        PdfWriter writer = new PdfWriter(output);
        writer.setCompressionLevel(0);
        PdfDocument pdf = new PdfDocument(new PdfReader(input), writer);

        // sweep
        PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
        autoSweep.cleanUp(pdf.getPage(1));

        pdf.close();

        // compare
        compareByContent(cmp, output, outputPath, "diff_cleanUpPage_");
    }

    @Test
    public void tentativeCleanUpTest() throws IOException, InterruptedException {
        String input = inputPath + "Lipsum.pdf";
        String output = outputPath + "tentativeCleanUp.pdf";
        String cmp = inputPath + "cmp_tentativeCleanUp.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(D|d)olor").setRedactionColor(ColorConstants.GREEN));

        try (PdfWriter writter = new PdfWriter(output) ;
            PdfDocument pdf = new PdfDocument(new PdfReader(input), writter)) {
          writter.setCompressionLevel(0);
          // sweep
          PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
          autoSweep.tentativeCleanUp(pdf);
        }
        // compare
        compareByContent(cmp, output, outputPath, "diff_tentativeCleanUp_");
    }

    @Test
    public void getPdfCleanUpLocationsTest() throws IOException {
        String input = inputPath + "Lipsum.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(D|d)olor"));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(new ByteArrayOutputStream()));

        // sweep
        PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
        List cleanUpLocations = (List) autoSweep.getPdfCleanUpLocations(pdf.getPage(1));

        pdf.close();

        // compare
        Assert.assertEquals(2, cleanUpLocations.size());
    }

    @Test
    public void highlightTest() throws IOException, InterruptedException {
        String input = inputPath + "Lipsum.pdf";
        String output = outputPath + "highlightTest.pdf";
        String cmp = inputPath + "cmp_highlightTest.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(D|d)olor").setRedactionColor(ColorConstants.GREEN));

        try (PdfWriter writter = new PdfWriter(output) ;
            PdfDocument pdf = new PdfDocument(new PdfReader(input), writter)){
          writter.setCompressionLevel(CompressionConstants.NO_COMPRESSION);
          // sweep
          PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
          autoSweep.highlight(pdf);
        }

        // compare
        compareByContent(cmp, output, outputPath, "diff_highlightTest_");
    }

    @Test
    public void redactLipsumPatternStartsWithWhiteSpace() throws IOException, InterruptedException {
        String input = inputPath + "Lipsum.pdf";
        String output = outputPath + "redactLipsumPatternStartsWithWhitespace.pdf";
        String cmp = inputPath + "cmp_redactLipsumPatternStartsWithWhitespace.pdf";
        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("\\s(D|d)olor").setRedactionColor(ColorConstants.GREEN));
        PdfWriter writer = new PdfWriter(output);
        writer.setCompressionLevel(0);
        PdfDocument pdf = new PdfDocument(new PdfReader(input), writer);
        // sweep
        PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
        autoSweep.cleanUp(pdf);
        pdf.close();
        // compare
        compareByContent(cmp, output, outputPath, "diff_redactLipsumPatternStartsWithWhitespace_");
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = CleanUpLogMessageConstant.FAILED_TO_PROCESS_A_TRANSFORMATION_MATRIX, count = 2))
    public void redactPdfWithNoninvertibleMatrix() throws IOException, InterruptedException {
        String input = inputPath + "noninvertibleMatrix.pdf";
        String output = outputPath + "redactPdfWithNoninvertibleMatrix.pdf";
        String cmp = inputPath + "cmp_redactPdfWithNoninvertibleMatrix.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("Hello World!").setRedactionColor(ColorConstants.GREEN));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        // sweep
        PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
        autoSweep.cleanUp(pdf);

        pdf.close();

        // compare
        compareByContent(cmp, output, outputPath, "diff_redactPdfWithNoninvertibleMatrix_");
    }

    @Test
    @Ignore("DEVSIX-4047")
    public void lineArtsDrawingOnCanvasTest() throws IOException, InterruptedException {
        String input = inputPath + "lineArtsDrawingOnCanvas.pdf";
        String output = outputPath + "lineArtsDrawingOnCanvas.pdf";
        String cmp = inputPath + "cmp_lineArtsDrawingOnCanvas.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(iphone)|(iPhone)"));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
        autoSweep.cleanUp(pdf);

        pdf.close();

        compareByContent(cmp, output, outputPath, "diff_lineArtsDrawingOnCanvasTest_");
    }

    private void compareByContent(String cmp, String output, String targetDir, String diffPrefix) throws IOException, InterruptedException {
        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(output, cmp, targetDir, diffPrefix + "_");

        if (errorMessage != null) {
            Assert.fail(errorMessage);
        }
    }
}
