package dev.vavateam1.service;

import com.google.inject.Inject;
import dev.vavateam1.dao.InventoryIngredientDao;
import dev.vavateam1.model.InventoryIngredient;
import dev.vavateam1.model.InventoryItemStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
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
import java.util.ArrayList;
import java.util.List;

public class InventoryServiceImpl implements InventoryService {

    private final InventoryIngredientDao inventoryIngredientDao;

    @Inject
    public InventoryServiceImpl(InventoryIngredientDao inventoryIngredientDao) {
        this.inventoryIngredientDao = inventoryIngredientDao;
    }

    @Override
    public List<InventoryIngredient> getAll() {
        return inventoryIngredientDao.findAll();
    }

    @Override
    public void saveAll(List<InventoryIngredient> items) {
        inventoryIngredientDao.saveAll(items);
    }

    @Override
    public void delete(int id) {
        inventoryIngredientDao.delete(id);
    }

    @Override
    public InventoryItemStatus getStatus(InventoryIngredient item) {
        BigDecimal quantity = zeroIfNull(item.getQuantity());
        BigDecimal minimalQuantity = zeroIfNull(item.getMinimalQuantity());

        if (minimalQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return quantity.compareTo(BigDecimal.ZERO) <= 0 ? InventoryItemStatus.CRITICAL : InventoryItemStatus.OK;
        }

        if (quantity.compareTo(minimalQuantity.multiply(new BigDecimal("0.5"))) <= 0) {
            return InventoryItemStatus.CRITICAL;
        }

        if (quantity.compareTo(minimalQuantity) < 0) {
            return InventoryItemStatus.LOW;
        }

        return InventoryItemStatus.OK;
    }

    @Override
    public List<InventoryIngredient> importFromXml(Path path) throws IOException {
        List<InventoryIngredient> importedItems = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(path.toFile());

            NodeList itemNodes = document.getElementsByTagName("item");
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Element itemElement = (Element) itemNodes.item(i);
                importedItems.add(mapIngredient(itemElement));
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Invalid inventory XML", e);
        }

        return importedItems;
    }

    @Override
    public void exportToXml(Path path, List<InventoryIngredient> items) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element rootElement = document.createElement("inventory");
            document.appendChild(rootElement);

            for (InventoryIngredient item : items) {
                Element itemElement = document.createElement("item");
                rootElement.appendChild(itemElement);

                appendText(document, itemElement, "id", String.valueOf(item.getId()));
                appendText(document, itemElement, "name", safeValue(item.getName()));
                appendText(document, itemElement, "quantity", formatDecimal(item.getQuantity()));
                appendText(document, itemElement, "minimal_quantity", formatDecimal(item.getMinimalQuantity()));
                appendText(document, itemElement, "unit", safeValue(item.getUnit()));
                appendText(document, itemElement, "cost_per_unit", formatDecimal(item.getCostPerUnit()));
                appendText(document, itemElement, "status", getStatus(item).name());
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(new DOMSource(document), new StreamResult(path.toFile()));
        } catch (ParserConfigurationException | TransformerException e) {
            throw new IOException("Failed to write inventory XML", e);
        }
    }

    private InventoryIngredient mapIngredient(Element itemElement) {
        InventoryIngredient ingredient = new InventoryIngredient();
        ingredient.setId(parseInteger(getChildText(itemElement, "id"), 0));
        ingredient.setName(getChildText(itemElement, "name"));
        ingredient.setQuantity(parseDecimal(getChildText(itemElement, "quantity")));
        ingredient.setMinimalQuantity(parseDecimal(getChildText(itemElement, "minimal_quantity")));
        ingredient.setUnit(getChildText(itemElement, "unit"));
        ingredient.setCostPerUnit(parseDecimal(getChildText(itemElement, "cost_per_unit")));
        return ingredient;
    }

    private void appendText(Document document, Element parent, String tagName, String value) {
        Element element = document.createElement(tagName);
        element.setTextContent(value);
        parent.appendChild(element);
    }

    private String getChildText(Element parent, String tagName) {
        NodeList childNodes = parent.getElementsByTagName(tagName);
        if (childNodes.getLength() == 0) {
            return "";
        }
        return childNodes.item(0).getTextContent().trim();
    }

    private int parseInteger(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Integer.parseInt(value.trim());
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatDecimal(BigDecimal value) {
        return zeroIfNull(value).stripTrailingZeros().toPlainString();
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
