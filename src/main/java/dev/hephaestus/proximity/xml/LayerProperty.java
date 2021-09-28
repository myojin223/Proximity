package dev.hephaestus.proximity.xml;

import dev.hephaestus.proximity.text.Style;
import dev.hephaestus.proximity.util.Result;
import dev.hephaestus.proximity.util.XMLUtil;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import static dev.hephaestus.proximity.util.XMLUtil.apply;

public abstract class LayerProperty<T> {
    private static final Map<String, LayerProperty<?>> PROPERTIES = new HashMap<>();

    public static final LayerProperty<Style> STYLE = register(new LayerProperty<>() {
        @Override
        public Result<Style> parse(RenderableCard.XMLElement element) {
            Style.Builder builder = new Style.Builder();

            builder.font(element.getAttribute("font"));

            apply(element, "italicFont", builder::italics);
            apply(element, "capitalization", Style.Capitalization::parse, builder::capitalization);
            apply(element, "color", Integer::decode, builder::color);
            apply(element, "size", Integer::parseInt, builder::size);
            apply(element, "kerning", Float::parseFloat, builder::kerning);

            XMLUtil.iterate(element, (child, i) -> {
                switch (child.getTagName()) {
                    case "shadow" -> builder.shadow(new Style.Shadow(
                            Integer.decode(child.getAttribute("color")),
                            Integer.decode(child.getAttribute("dX")),
                            Integer.decode(child.getAttribute("dY"))
                    ));
                    case "outline" -> builder.outline(new Style.Outline(
                            Integer.decode(child.getAttribute("color")),
                            Integer.decode(child.getAttribute("weight"))
                    ));
                }
            });

            return Result.of(element.getParent() == null ? builder.build()
                    : element.getParent().getProperty(LayerProperty.STYLE, Style.EMPTY).merge(builder.build()));
        }
    }, "Style");

    public static final LayerProperty<Rectangle> WRAP = register(new LayerProperty<>() {
        @Override
        public Result<Rectangle> parse(RenderableCard.XMLElement element) {
            return Result.of(new Rectangle(
                    Integer.parseInt(element.getAttribute("x")),
                    Integer.parseInt(element.getAttribute("y")),
                    Integer.parseInt(element.getAttribute("width")),
                    Integer.parseInt(element.getAttribute("height"))
            ));
        }
    }, "Wrap", "wrap");

    public static final LayerProperty<Rectangle2D> BOUNDS = register(new LayerProperty<>() {
        @Override
        public Result<Rectangle2D> parse(RenderableCard.XMLElement element) {
            return Result.of(new Rectangle2D.Double(
                    Integer.parseInt(element.getAttribute("x")),
                    Integer.parseInt(element.getAttribute("y")),
                    Integer.parseInt(element.getAttribute("width")),
                    Integer.parseInt(element.getAttribute("height"))
            ));
        }
    }, "Bounds");

    public static <T> LayerProperty<T> register(LayerProperty<T> property, String... tagNames) {
        for (String tagName : tagNames) {
            PROPERTIES.put(tagName, property);
        }

        return property;
    }

    public abstract Result<T> parse(RenderableCard.XMLElement element);

    @SuppressWarnings("unchecked")
    public static <T > LayerProperty<T> get(String tagName) {
        return (LayerProperty<T>) PROPERTIES.get(tagName);
    }
}
