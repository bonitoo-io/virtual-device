package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.bonitoo.qa.conf.VirDevConfigException;
import io.bonitoo.qa.conf.VirDevDeserializer;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.data.generator.NumGenerator;
import io.bonitoo.qa.plugin.PluginProperties;
import io.bonitoo.qa.plugin.PluginResultType;
import io.bonitoo.qa.plugin.item.ItemGenPlugin;
import io.bonitoo.qa.plugin.item.ItemPluginConfigClass;
import io.bonitoo.qa.plugin.item.ItemPluginMill;
import io.bonitoo.qa.util.LogHelper;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deserializes YAML configuration node of an item configuration into
 * an ItemConfig object.
 */
public class ItemConfigDeserializer extends VirDevDeserializer<ItemConfig> {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


  public ItemConfigDeserializer() {
    this(null);
  }

  protected ItemConfigDeserializer(Class<?> vc) {
    super(vc);
  }

  // following boolean helps avoid recursion loop through calls from plugin subclasses
  boolean calledOnce = false;

  @Override
  public ItemConfig deserialize(JsonParser jsonParser,
                                DeserializationContext ctx)
      throws IOException {

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    ItemType type = ItemType.valueOf(safeGetNode(node, "type").asText());
    String name = safeGetNode(node, "name").asText();
    String label = safeGetNode(node, "label").asText();
    int count = getDefaultIntNode(node, "count", 1).asInt();
    ItemArType serialType = ItemArType.valueOf(
        getDefaultStringNode(node, "arType", "Undefined").asText()
    );
    String plugin;
    Object max;
    Object min;
    double period;
    double dev;
    List<String> vals;

    switch (type) {
      case Double:
        max = safeGetNode(node, "max").asDouble();
        min = safeGetNode(node, "min").asDouble();
        period = safeGetNode(node, "period").asDouble();
        dev = getDefaultDoubleNode(node, "dev", NumGenerator.DEFAULT_DEV).asDouble();
        JsonNode precNode = node.get("prec");
        Integer prec = precNode == null ? null : precNode.asInt();
        ItemNumConfig dblConf = new ItemNumConfig(name, label, type, (Double) min,
            (Double) max, period, dev, prec);
        if (count > 1) {
          dblConf.setCount(count);
          dblConf.setArType(serialType);
        }
        return dblConf;
      case Long:
        max = safeGetNode(node, "max").asLong();
        min = safeGetNode(node, "min").asLong();
        period = safeGetNode(node, "period").asDouble();
        dev = getDefaultDoubleNode(node, "dev", NumGenerator.DEFAULT_DEV).asDouble();
        ItemNumConfig longConf = new ItemNumConfig(name, label, type,
            (Long) min, (Long) max, period, dev);
        if (count > 1) {
          longConf.setCount(count);
          longConf.setArType(serialType);
        }
        return longConf;
      case String:
        vals = new ArrayList<>();
        for (Iterator<JsonNode> it = safeGetNode(node, "values").elements(); it.hasNext(); ) {
          JsonNode elem = it.next();
          vals.add(elem.asText());
        }
        ItemStringConfig strConf = new ItemStringConfig(name, label, type, vals);
        if (count > 1) {
          strConf.setCount(count);
          strConf.setArType(serialType);
        }
        return strConf;
      case Plugin:
        plugin = safeGetNode(node, "pluginName").asText();
        @SuppressWarnings("unchecked")
        Class<? extends ItemGenPlugin> clazz = (Class<? extends ItemGenPlugin>)
            ItemPluginMill.getPluginClass(plugin);

        // subclasses of this deserializer are encouraged to call this deserializer to
        // simplify their own deserialization - (i.e. do not want to duplicate code for
        // deserialization of shared fields)...
        // however a recursion loop needs to be avoided
        if (!calledOnce) {
          Annotation[] annotations = clazz.getDeclaredAnnotations();
          for (Annotation a : annotations) {
            if (a instanceof ItemPluginConfigClass) {
              Class<? extends ItemPluginConfig> confClazz = ((ItemPluginConfigClass) a).conf();
              logger.info(LogHelper.buildMsg("Init", "Deserialize Config",
                  String.format("Using class %s to deserialize item plugin config for %s",
                    confClazz.getName(),
                    clazz.getName())));
              calledOnce = true;
              // Warning: here is where unwanted recursion loop can be triggered
              return ctx.readValue(node.traverse(jsonParser.getCodec()), confClazz);
            }
          }
        }
        PluginResultType resultType = PluginResultType
            .valueOf(safeGetNode(node, "resultType").asText());
        PluginProperties props = ItemPluginMill.getPluginProps(plugin);
        JsonNode prNode = node.get("prec");
        ItemPluginConfig result = new ItemPluginConfig(props, name);

        if (prNode != null) {
          int precision = prNode.asInt();
          result.setPrec(precision);
        }

        result.type = type;
        result.label = label;
        result.name = name;
        result.count = count;
        result.arType = serialType;

        return result;
      default:
        throw new VirDevConfigException("Cannot instantiate config for type " + type);
    }
  }
}
