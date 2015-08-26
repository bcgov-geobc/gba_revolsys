package com.revolsys.doclet.java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.doclet.DocletUtil;
import com.revolsys.format.xml.XmlWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.util.HtmlUtil;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;

public class JavaDoclet {
  public static void documentationClass(final XmlWriter writer, final ClassDoc classDoc) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "javaClass");
    final String name = classDoc.name();

    DocletUtil.title(writer, DocletUtil.qualifiedName(classDoc), name);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(writer, classDoc, classDoc);

    final ConstructorDoc[] constructors = classDoc.constructors();
    if (constructors.length > 0) {
      DocletUtil.title(writer, "Constructors");
      for (final ConstructorDoc method : constructors) {
        documentationMethod(writer, method);
      }
    }

    final MethodDoc[] methods = classDoc.methods();
    if (methods.length > 0) {
      DocletUtil.title(writer, "Methods");
      for (final MethodDoc method : methods) {
        documentationMethod(writer, method);
      }
    }
    writer.endTagLn(HtmlUtil.DIV);
    writer.endTagLn(HtmlUtil.DIV);
  }

  public static void documentationMethod(final XmlWriter writer, final ExecutableMemberDoc member) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "javaMethod");

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "title");
    methodSignature(writer, member);
    writer.endTagLn(HtmlUtil.DIV);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(writer, member.containingClass(), member);

    parameters(writer, member);

    if (member instanceof MethodDoc) {
      final MethodDoc method = (MethodDoc)member;
      DocletUtil.documentationReturn(writer, method);
    }

    writer.endTagLn(HtmlUtil.DIV);
    writer.endTagLn(HtmlUtil.DIV);
  }

  public static void documentationPackage(final XmlWriter writer, final PackageDoc packageDoc) {
    final String name = packageDoc.name();
    writer.startTag(HtmlUtil.A);
    writer.attribute(HtmlUtil.ATTR_NAME, name);
    writer.text("");
    writer.endTagLn(HtmlUtil.A);
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "javaPackage");

    DocletUtil.title(writer, name, name);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(writer, null, packageDoc);
    final Map<String, ClassDoc> classes = new TreeMap<String, ClassDoc>();
    for (final ClassDoc classDoc : packageDoc.ordinaryClasses()) {
      classes.put(classDoc.name(), classDoc);
    }
    for (final ClassDoc classDoc : classes.values()) {
      documentationClass(writer, classDoc);
    }
    writer.endTagLn(HtmlUtil.DIV);
    writer.endTagLn(HtmlUtil.DIV);
  }

  public static String getAnchor(final ExecutableMemberDoc member) {
    final StringBuffer anchor = new StringBuffer();
    final ClassDoc classDoc = member.containingClass();
    final String className = DocletUtil.qualifiedName(classDoc);
    anchor.append(className);
    anchor.append(".");
    anchor.append(member.name());
    anchor.append("(");
    final Parameter[] parameters = member.parameters();
    boolean first = true;
    for (final Parameter parameter : parameters) {
      if (first) {
        first = false;
      } else {
        anchor.append(",");
      }
      final Type type = parameter.type();
      String typeName = type.qualifiedTypeName();
      typeName = typeName.replaceAll("^java.lang.", "");
      typeName = typeName.replaceAll("^java.io.", "");
      typeName = typeName.replaceAll("^java.util.", "");
      anchor.append(typeName);
      anchor.append(type.dimension());
    }
    anchor.append(")");
    return anchor.toString();
  }

  public static LanguageVersion languageVersion() {
    return LanguageVersion.JAVA_1_5;
  }

  public static void methodSignature(final XmlWriter writer, final ExecutableMemberDoc member) {
    writer.startTag(HtmlUtil.A);
    final String anchor = getAnchor(member);
    writer.attribute(HtmlUtil.ATTR_NAME, anchor);
    if (member instanceof MethodDoc) {
      writer.startTag(HtmlUtil.CODE);
      final MethodDoc method = (MethodDoc)member;
      final Type returnType = method.returnType();
      DocletUtil.typeName(writer, returnType);
      writer.text(" ");
      writer.endTagLn(HtmlUtil.CODE);
    }
    if (member.isStatic()) {
      writer.startTag(HtmlUtil.I);
    }
    writer.text(member.name());
    if (member.isStatic()) {
      writer.endTag(HtmlUtil.I);
    }
    writer.startTag(HtmlUtil.CODE);
    writer.text("(");
    final Parameter[] parameters = member.parameters();
    boolean first = true;
    for (final Parameter parameter : parameters) {
      if (first) {
        first = false;
      } else {
        writer.text(", ");
      }

      DocletUtil.typeName(writer, parameter.type());
      writer.text(" ");
      writer.text(parameter.name());
    }
    writer.text(")");
    writer.endTagLn(HtmlUtil.CODE);
    writer.endTagLn(HtmlUtil.A);
  }

  public static int optionLength(String optionName) {
    optionName = optionName.toLowerCase();
    if (optionName.equals("-d") || optionName.equals("-doctitle") || optionName.equals("-docid")
      || optionName.equals("-htmlfooter") || optionName.equals("-htmlheader")) {
      return 2;
    }
    return -1;
  }

  public static void parameters(final XmlWriter writer, final ExecutableMemberDoc method) {
    final List<Parameter> parameters = new ArrayList<Parameter>();
    for (final Parameter parameter : method.parameters()) {
      parameters.add(parameter);
    }
    if (!parameters.isEmpty()) {
      final ClassDoc containingClass = method.containingClass();
      final Map<String, Tag[]> descriptions = DocletUtil.getParameterDescriptions(method);

      DocletUtil.title(writer, "Parameters");

      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable parameters");
      writer.startTag(HtmlUtil.TABLE);
      writer.attribute(HtmlUtil.ATTR_CLASS, "data");
      writer.startTag(HtmlUtil.THEAD);
      writer.startTag(HtmlUtil.TR);
      writer.element(HtmlUtil.TH, "Parameter");
      writer.element(HtmlUtil.TH, "Type");
      writer.element(HtmlUtil.TH, "Description");
      writer.endTagLn(HtmlUtil.TR);
      writer.endTagLn(HtmlUtil.THEAD);

      writer.startTag(HtmlUtil.TBODY);
      for (final Parameter parameter : parameters) {
        writer.startTag(HtmlUtil.TR);
        final String name = parameter.name();

        writer.startTag(HtmlUtil.TD);
        writer.attribute(HtmlUtil.ATTR_CLASS, "name");
        writer.text(parameter.name());
        writer.endTagLn(HtmlUtil.TD);

        writer.startTag(HtmlUtil.TD);
        writer.attribute(HtmlUtil.ATTR_CLASS, "type");
        DocletUtil.typeNameLink(writer, parameter.type());
        writer.endTagLn(HtmlUtil.TD);

        DocletUtil.descriptionTd(writer, containingClass, descriptions, name);
        writer.endTagLn(HtmlUtil.TR);
      }
      writer.endTagLn(HtmlUtil.TBODY);

      writer.endTagLn(HtmlUtil.TABLE);
      writer.endTagLn(HtmlUtil.DIV);
    }
  }

  public static boolean start(final RootDoc root) {
    new JavaDoclet(root).start();
    return true;
  }

  public static boolean validOptions(final String options[][],
    final DocErrorReporter docerrorreporter) {
    final boolean flag = true;
    for (final String[] option : options) {
      final String argName = option[0].toLowerCase();
      if (argName.equals("-d")) {
        final String destDir = option[1];
        final File file = new File(destDir);
        if (!file.exists()) {
          docerrorreporter.printNotice("Create directory" + destDir);
          file.mkdirs();
        }
        if (!file.isDirectory()) {
          docerrorreporter.printError("Destination not a directory" + file.getPath());
          return false;
        } else if (!file.canWrite()) {
          docerrorreporter.printError("Destination directory not writable " + file.getPath());
          return false;
        }
      } else if (argName.equals("-htmlheader")) {
        if (!new File(option[1]).exists()) {
          docerrorreporter.printError("Header file does not exist" + option[1]);
          return false;
        }
      } else if (argName.equals("-htmlfooter")) {
        if (!new File(option[1]).exists()) {
          docerrorreporter.printError("Footer file does not exist" + option[1]);
          return false;
        }
      }
    }

    return flag;
  }

  private String destDir = ".";

  private String docId;

  private String docTitle;

  private String footer;

  private String header;

  private final RootDoc root;

  private XmlWriter writer;

  public JavaDoclet(final RootDoc root) {
    this.root = root;
  }

  public void bodyContent() {
    this.writer.element(HtmlUtil.H1, this.docTitle);
    DocletUtil.description(this.writer, null, this.root);
    documentation();
  }

  public void documentation() {
    this.writer.startTag(HtmlUtil.DIV);
    for (final PackageDoc packageDoc : this.root.specifiedPackages()) {
      documentationPackage(this.writer, packageDoc);
    }

    this.writer.endTagLn(HtmlUtil.DIV);
  }

  public void head() {
    this.writer.startTag(HtmlUtil.HEAD);
    this.writer.element(HtmlUtil.TITLE, this.docTitle);
    HtmlUtil.serializeCss(this.writer,
      "https://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables_themeroller.css");
    HtmlUtil.serializeCss(this.writer,
      "https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/themes/cupertino/jquery-ui.css");
    HtmlUtil.serializeCss(this.writer, "javadoc.css");
    HtmlUtil.serializeScriptLink(this.writer,
      "https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js");
    HtmlUtil.serializeScriptLink(this.writer,
      "https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js");
    HtmlUtil.serializeScriptLink(this.writer,
      "https://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js");
    HtmlUtil.serializeScriptLink(this.writer, "javadoc.js");
    this.writer.endTagLn(HtmlUtil.HEAD);
  }

  private void setOptions(final String[][] options) {
    for (final String[] option : options) {
      final String optionName = option[0];
      if (optionName.equals("-d")) {
        this.destDir = option[1];

      } else if (optionName.equals("-doctitle")) {
        this.docTitle = option[1];
      } else if (optionName.equals("-docid")) {
        this.docId = option[1];
      } else if (optionName.equals("-htmlheader")) {
        this.header = FileUtil.getFileAsString(option[1]);
      } else if (optionName.equals("-htmlfooter")) {
        this.footer = FileUtil.getFileAsString(option[1]);
      }
    }
    try {
      final File dir = new File(this.destDir);
      final File indexFile = new File(dir, "index.html");
      final FileWriter out = new FileWriter(indexFile);
      this.writer = new XmlWriter(out, false);
      this.writer.setIndent(false);
      this.writer.setWriteNewLine(false);
      FileUtil.copy(getClass().getResourceAsStream("/com/revolsys/doclet/javadoc.css"),
        new File(this.destDir, "javadoc.css"));
      FileUtil.copy(getClass().getResourceAsStream("/com/revolsys/doclet/javadoc.js"),
        new File(this.destDir, "javadoc.js"));
    } catch (final IOException e) {
      throw new IllegalArgumentException(e.fillInStackTrace().getMessage(), e);
    }
  }

  private void start() {
    try {
      setOptions(this.root.options());

      if (this.header == null) {
        this.writer.startDocument("UTF-8", "1.0");
        this.writer.docType("html", null);
        this.writer.startTag(HtmlUtil.HTML);
        this.writer.attribute(HtmlUtil.ATTR_LANG, "en");

        head();
        this.writer.startTag(HtmlUtil.BODY);
      } else {
        this.header = this.header.replaceAll("\\$\\{docTitle\\}", this.docTitle);
        this.header = this.header.replaceAll("\\$\\{docId\\}", this.docId);
        this.writer.write(this.header);
      }

      bodyContent();

      if (this.footer == null) {
        this.writer.endTagLn(HtmlUtil.BODY);

        this.writer.endTagLn(HtmlUtil.HTML);
      } else {
        this.footer = this.footer.replaceAll("\\$\\{docTitle\\}", this.docTitle);
        this.footer = this.footer.replaceAll("\\$\\{docId\\}", this.docId);
        this.writer.write(this.footer);
      }
      this.writer.endDocument();
    } finally {
      if (this.writer != null) {
        this.writer.close();
      }
    }
  }

}
