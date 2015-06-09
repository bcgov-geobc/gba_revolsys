package com.revolsys.io.map;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.util.Property;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.format.json.JsonMapIoFactory;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.JavaBeanUtil;

public class MapObjectFactoryRegistry {

  public static final Map<String, MapObjectFactory> TYPE_NAME_TO_FACTORY = new HashMap<String, MapObjectFactory>();

  static {
    addFactory(GeometryFactory.FACTORY);
    addFactory(RecordDefinitionImpl.FACTORY);
    addFactory(FieldDefinition.FACTORY);
  }

  public static void addFactory(final MapObjectFactory factory) {
    final String typeName = factory.getTypeName();
    TYPE_NAME_TO_FACTORY.put(typeName, factory);
  }

  @SuppressWarnings("unchecked")
  public static <V> V toObject(final File file) {
    final FileSystemResource resource = new FileSystemResource(file);
    return (V)toObject(resource);
  }

  @SuppressWarnings("unchecked")
  public static <V> V toObject(final Map<String, ? extends Object> map) {
    final String typeClass = CollectionUtil.getString(map, "typeClass");
    if (Property.hasValue(typeClass)) {
      // TODO factory methods and constructor arguments
      final V object = (V)JavaBeanUtil.createInstance(typeClass);
      return object;
    } else {
      final String type = CollectionUtil.getString(map, "type");
      final MapObjectFactory objectFactory = TYPE_NAME_TO_FACTORY.get(type);
      if (objectFactory == null) {
        LoggerFactory.getLogger(MapObjectFactoryRegistry.class).error(
          "No layer factory for " + type);
        return null;
      } else {
        return (V)objectFactory.toObject(map);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> V toObject(final Resource resource) {
    final Resource oldResource = SpringUtil.setBaseResource(SpringUtil.getParentResource(resource));

    try {
      final Map<String, Object> properties = JsonMapIoFactory.toMap(resource);
      return (V)MapObjectFactoryRegistry.toObject(properties);
    } catch (final Throwable t) {
      LoggerFactory.getLogger(MapObjectFactoryRegistry.class).error(
        "Cannot load object from " + resource, t);
      return null;
    } finally {
      SpringUtil.setBaseResource(oldResource);
    }
  }

  public static String toString(final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    return JsonMapIoFactory.toString(properties);
  }

  public static void write(final File file, final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    JsonMapIoFactory.write(properties, file, true);
  }

  public static void write(final Resource resource, final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    JsonMapIoFactory.write(properties, resource, true);
  }
}
