package dev.vavateam1.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import dev.vavateam1.dao.ClosingDao;
import dev.vavateam1.model.CashOperationType;
import dev.vavateam1.report.ClosingSummary;

public class ClosingServiceImpl implements ClosingService {
    private static final Logger log = LoggerFactory.getLogger(ClosingServiceImpl.class);
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    private final ClosingDao closingDao;

    @Inject
    public ClosingServiceImpl(ClosingDao closingDao) {
        this.closingDao = closingDao;
    }

    @Override
    public ClosingSummary getClosingSummary() {
        log.info("Fetching closing summary");
        return closingDao.getClosingSummary();
    }

    @Override
    public ClosingSummary addCashFloat(int userId, BigDecimal amount) {
        validateAmount(amount);
        log.info("Adding cash float of {} for user id: {}", amount, userId);
        ClosingSummary summary = closingDao.recordCashOperation(userId, CashOperationType.CASH_FLOAT, amount, "Cash float insert");
        log.info("Cash float recorded for user id: {}", userId);
        return summary;
    }

    @Override
    public ClosingSummary withdrawCash(int userId, BigDecimal amount) {
        validateAmount(amount);
        log.info("Withdrawing cash of {} for user id: {}", amount, userId);
        ClosingSummary summary = closingDao.recordCashOperation(userId, CashOperationType.WITHDRAWAL, amount, "Cash withdrawal");
        log.info("Cash withdrawal recorded for user id: {}", userId);
        return summary;
    }

    @Override
    public boolean closeDay(int userId) {
        log.info("Closing day for user id: {}", userId);
        ClosingSummary summary = closingDao.getClosingSummary();
        boolean closed = closingDao.createDailyClosing(userId, summary);
        if (closed) {
            log.info("Day closed successfully for user id: {}, business date: {}", userId, summary.businessDate());
        } else {
            log.info("Day close skipped (already closed) for business date: {}", summary.businessDate());
        }
        return closed;
    }

    @Override
    public void exportReport(ClosingSummary summary, Path path) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element rootElement = document.createElement("closingReport");
            document.appendChild(rootElement);

            appendTextElement(document, rootElement, "businessDate", summary.businessDate().format(REPORT_DATE_FORMAT));
            appendTextElement(document, rootElement, "totalPaid", formatMoney(summary.totalPaid()));
            appendTextElement(document, rootElement, "totalTips", formatMoney(summary.totalTips()));
            appendTextElement(document, rootElement, "grandTotal", formatMoney(summary.grandTotal()));
            appendTextElement(document, rootElement, "cashFloat", formatMoney(summary.cashFloat()));
            appendTextElement(document, rootElement, "cash", formatMoney(summary.cash()));
            appendTextElement(document, rootElement, "card", formatMoney(summary.card()));

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(new DOMSource(document), new StreamResult(path.toFile()));
        } catch (ParserConfigurationException | TransformerException e) {
            throw new IOException("Failed to write closing XML", e);
        }
    }

    private void appendTextElement(Document document, Element parent, String tagName, String value) {
        Element element = document.createElement(tagName);
        element.setTextContent(value);
        parent.appendChild(element);
    }

    private String formatMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
}
