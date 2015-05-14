package com.revolsys.ui.html.serializer.key;

import java.util.Date;

import com.revolsys.format.xml.XmlWriter;
import com.revolsys.util.DateUtil;
import com.revolsys.util.JavaBeanUtil;

/**
 * Serialize a date with the specified date format
 *
 * @author Paul Austin
 */
public class DateFormatKeySerializer extends AbstractKeySerializer {

  /** The date format style. */
  private String dateFormat = "yyyy-MM-dd HH:mm:ss";

  public DateFormatKeySerializer() {
  }

  /**
   * Construct a new DateFormatKeySerializer.
   */
  public DateFormatKeySerializer(final String name, final String dateFormat) {
    super(name);
    this.dateFormat = dateFormat;
  }

  /**
   * Get the dete format.
   *
   * @return The date format.
   */
  public String getDateFormat() {
    return this.dateFormat;
  }

  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   */
  @Override
  public void serialize(final XmlWriter out, final Object object) {
    final Object value = JavaBeanUtil.getProperty(object, getName());
    if (value == null) {
      out.text("-");
    } else if (value instanceof Date) {
      out.text(DateUtil.format(this.dateFormat, (Date)value));
    } else {
      out.text(value);
    }
  }

  /**
   * Set the dete format.
   *
   * @param dateFormat The date format.
   */
  public void setDateFormat(final String dateFormat) {
    this.dateFormat = dateFormat;
  }

}