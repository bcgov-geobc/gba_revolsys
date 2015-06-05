package com.revolsys.io.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.io.NamedLinkedHashMap;

public class XmlMapIterator extends AbstractIterator<Map<String, Object>> {

  /** Flag indicating if there are more records to be read. */
  private boolean hasNext = false;

  private XMLStreamReader in;

  private final boolean single;

  private Resource resource;

  public XmlMapIterator(final Resource resource) {
    this(resource, false);
  }

  public XmlMapIterator(final Resource resource, final boolean single) {
    this.resource = resource;
    this.single = single;
  }

  @Override
  protected void doClose() {
    super.doClose();
    if (in != null) {
      StaxUtils.closeSilent(in);
      in = null;
    }
    resource = null;
  }

  @Override
  protected void doInit() {
    super.doInit();
    in = StaxUtils.createXmlReader(resource);
    if (StaxUtils.skipToStartElement(in) == XMLStreamConstants.START_ELEMENT) {
      if (single) {
        hasNext = true;
      } else {
        if (StaxUtils.skipToStartElement(in) == XMLStreamConstants.START_ELEMENT) {
          hasNext = true;
        }
      }
    }

  }

  @Override
  protected Map<String, Object> getNext() throws NoSuchElementException {
    if (hasNext) {
      final Map<String, Object> map = readMap();
      if (StaxUtils.skipToStartElement(in) != XMLStreamConstants.START_ELEMENT) {
        hasNext = false;
      }
      return map;
    } else {
      throw new NoSuchElementException();
    }
  }

  @SuppressWarnings("unchecked")
  private Object readElement() {
    final String name = in.getLocalName();
    final Map<String, Object> map = new NamedLinkedHashMap<String, Object>(name);
    int textIndex = 0;
    int elementIndex = 0;
    while (StaxUtils.next(in) != XMLStreamConstants.END_ELEMENT) {
      switch (in.getEventType()) {
        case XMLStreamConstants.CDATA:
        case XMLStreamConstants.CHARACTERS:
          final String text = in.getText();
          if (StringUtils.hasText(text)) {
            map.put("xmlText" + ++textIndex, text);
          }
        break;
        case XMLStreamConstants.SPACE:
        break;
        case XMLStreamConstants.START_ELEMENT:
          elementIndex++;
          final String tagName = in.getLocalName();
          final Object value = readElement();
          final Object oldValue = map.get(tagName);
          if (oldValue == null) {
            map.put(tagName, value);
          } else {
            List<Object> list;
            if (oldValue instanceof List) {
              list = (List<Object>)oldValue;
            } else {
              list = new ArrayList<Object>();
              list.add(oldValue);
              map.put(tagName, list);
            }
            list.add(value);

          }
        break;
        case XMLStreamConstants.COMMENT:
        break;
        default:
          System.err.println(in.getEventType() + " " + in.getText());
        break;
      }
    }
    if (elementIndex == 0) {
      if (textIndex > 0) {
        final StringBuffer fullText = new StringBuffer();
        for (final Object text : map.values()) {
          fullText.append(text);
        }
        return fullText.toString();
      }
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> readMap() {
    final String name = in.getLocalName();
    final Map<String, Object> map = new NamedLinkedHashMap<String, Object>(name);
    int textIndex = 0;
    while (StaxUtils.next(in) != XMLStreamConstants.END_ELEMENT) {
      switch (in.getEventType()) {
        case XMLStreamConstants.CDATA:
        case XMLStreamConstants.CHARACTERS:
          final String text = in.getText();
          if (StringUtils.hasText(text)) {
            map.put("xmlText" + ++textIndex, text);
          }
        break;
        case XMLStreamConstants.SPACE:
        break;
        case XMLStreamConstants.START_ELEMENT:
          final String tagName = in.getLocalName();
          final Object value = readElement();
          final Object oldValue = map.get(tagName);
          if (oldValue == null) {
            map.put(tagName, value);
          } else {
            List<Object> list;
            if (oldValue instanceof List) {
              list = (List<Object>)oldValue;
            } else {
              list = new ArrayList<Object>();
              list.add(oldValue);
              map.put(tagName, list);
            }
            list.add(value);

          }
        break;
        default:
        break;
      }
    }
    return map;
  }
}
