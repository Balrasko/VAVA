package dev.vavateam1.service;

import com.google.inject.Inject;
import dev.vavateam1.dao.CategoryDao;
import dev.vavateam1.dao.FinanceDao;
import dev.vavateam1.dto.FinanceSummary;
import dev.vavateam1.model.Category;
import dev.vavateam1.report.FinanceItemReport;
import dev.vavateam1.report.FinanceReport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FinanceServiceImpl implements FinanceService {

    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    private final FinanceDao financeDao;
    private final CategoryDao categoryDao;

    @Inject
    public FinanceServiceImpl(FinanceDao financeDao, CategoryDao categoryDao) {
        this.financeDao = financeDao;
        this.categoryDao = categoryDao;
    }

    @Override
    public List<FinanceItemReport> getFinanceItems(LocalDate from, LocalDate to, Integer categoryId) {
        return financeDao.getFinanceItems(from, to, categoryId);
    }

    @Override
    public LocalDate getLatestReportDate() {
        FinanceReport report = financeDao.getFinanceReport();
        return report != null ? report.reportDate() : null;
    }

    @Override
    public List<Category> getCategories() {
        return categoryDao.getAllCategories();
    }

    @Override
    public LocalDate resolveReportDate(LocalDate from, LocalDate to, LocalDate latestDate) {
        if (to != null) {
            return to;
        }
        if (from != null) {
            return from;
        }
        if (latestDate != null) {
            return latestDate;
        }
        return LocalDate.now();
    }

    @Override
    public FinanceSummary computeSummary(List<FinanceItemReport> items) {
        int soldItemsTotal = items.stream()
                .mapToInt(FinanceItemReport::soldPieces)
                .sum();

        BigDecimal totalSales = items.stream()
                .map(item -> item.pricePerPiece().multiply(BigDecimal.valueOf(item.soldPieces())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FinanceSummary(totalSales, soldItemsTotal);
    }

    @Override
    public void exportReport(FinanceReport report, Path path) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element root = document.createElement("financeReport");
            document.appendChild(root);

            Element summary = document.createElement("summary");
            root.appendChild(summary);
            appendText(document, summary, "reportDate", report.reportDate().format(REPORT_DATE_FORMAT));
            appendText(document, summary, "dailySales", formatDecimal(report.dailySales()));
            appendText(document, summary, "soldItemsTotal", String.valueOf(report.soldItemsTotal()));

            Element items = document.createElement("items");
            root.appendChild(items);
            for (FinanceItemReport item : report.items()) {
                Element itemEl = document.createElement("item");
                items.appendChild(itemEl);
                appendText(document, itemEl, "itemId", String.valueOf(item.itemId()));
                appendText(document, itemEl, "name", item.name());
                appendText(document, itemEl, "soldPieces", String.valueOf(item.soldPieces()));
                appendText(document, itemEl, "pricePerPiece", formatDecimal(item.pricePerPiece()));
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(new DOMSource(document), new StreamResult(path.toFile()));
        } catch (ParserConfigurationException | TransformerException e) {
            throw new IOException("Failed to write finance XML", e);
        }
    }

    private void appendText(Document document, Element parent, String tagName, String value) {
        Element element = document.createElement(tagName);
        element.setTextContent(value);
        parent.appendChild(element);
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }
}
