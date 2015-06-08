package com.revolsys.doclet.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.doclet.DocletUtil;
import com.revolsys.io.FileUtil;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.AnnotationTypeElementDoc;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;

public class ClientDoclet {
  public static LanguageVersion languageVersion() {
    return LanguageVersion.JAVA_1_5;
  }

  public static int optionLength(String optionName) {
    optionName = optionName.toLowerCase();
    if (optionName.equals("-d") || optionName.equals("-doctitle") || optionName.equals("-docid")
      || optionName.equals("-htmlfooter") || optionName.equals("-htmlheader")
      || optionName.equals("-packagesOpen")) {
      return 2;
    }
    return -1;
  }

  public static boolean start(final RootDoc root) {
    new ClientDoclet(root).start();
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
      } else if (argName.equals("-packagesOpen")) {
        if (!"true".equalsIgnoreCase(option[1]) && !"false".equalsIgnoreCase(option[1])) {
          docerrorreporter.printError("PackagesOpen must be true or false not " + option[1]);
          return false;
        }
      }
    }

    return flag;
  }

  private String docTitle;

  private String docId;

  private final RootDoc root;

  private XmlWriter writer;

  private String destDir = ".";

  private String header;

  private String footer;

  private boolean packagesOpen = true;

  public ClientDoclet(final RootDoc root) {
    this.root = root;
  }

  public void addResponseStatusDescription(final Map<String, List<String>> responseCodes,
    final String code, final String description) {
    List<String> descriptions = responseCodes.get(code);
    if (descriptions == null) {
      descriptions = new ArrayList<String>();
      responseCodes.put(code, descriptions);
    }
    descriptions.add(description);
  }

  public void bodyContent() {
    this.writer.element(HtmlUtil.H1, this.docTitle);
    DocletUtil.description(this.writer, null, this.root);
    documentation();
  }

  public void documentation() {
    this.writer.startTag(HtmlUtil.DIV);
    for (final PackageDoc packageDoc : this.root.specifiedPackages()) {
      documentationPackage(packageDoc);
    }

    this.writer.endTagLn(HtmlUtil.DIV);
  }

  public void documentationAnnotation(final AnnotationTypeDoc annotationDoc) {
    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "javaClass");
    final String name = annotationDoc.name();

    final String id = DocletUtil.qualifiedName(annotationDoc);
    HtmlUtil.elementWithId(this.writer, HtmlUtil.H2, id, name);

    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(this.writer, annotationDoc, annotationDoc);

    final AnnotationTypeElementDoc[] elements = annotationDoc.elements();
    if (elements.length > 0) {
      DocletUtil.title(this.writer, "Annotation Elements");

      this.writer.startTag(HtmlUtil.DIV);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable parameters");
      this.writer.startTag(HtmlUtil.TABLE);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "data");
      this.writer.startTag(HtmlUtil.THEAD);
      this.writer.startTag(HtmlUtil.TR);
      this.writer.element(HtmlUtil.TH, "Column");
      this.writer.element(HtmlUtil.TH, "Type");
      this.writer.element(HtmlUtil.TH, "Default");
      this.writer.element(HtmlUtil.TH, "Description");
      this.writer.endTagLn(HtmlUtil.TR);
      this.writer.endTagLn(HtmlUtil.THEAD);

      this.writer.startTag(HtmlUtil.TBODY);
      for (final AnnotationTypeElementDoc element : elements) {
        this.writer.startTag(HtmlUtil.TR);
        final String elementName = element.name();

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "name");
        DocletUtil.anchor(this.writer, id + "." + elementName, elementName);
        this.writer.endTagLn(HtmlUtil.TD);

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "type");
        DocletUtil.typeNameLink(this.writer, element.returnType());
        this.writer.endTagLn(HtmlUtil.TD);

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "default");
        final AnnotationValue defaultValue = element.defaultValue();
        if (defaultValue == null) {
          this.writer.text("-");
        } else {
          this.writer.text(defaultValue);
        }
        this.writer.endTagLn(HtmlUtil.TD);

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "description");
        DocletUtil.description(this.writer, null, element);
        this.writer.endTagLn(HtmlUtil.TD);
        this.writer.endTagLn(HtmlUtil.TR);
      }
      this.writer.endTagLn(HtmlUtil.TBODY);

      this.writer.endTagLn(HtmlUtil.TABLE);
      this.writer.endTagLn(HtmlUtil.DIV);

    }
    this.writer.endTagLn(HtmlUtil.DIV);
    this.writer.endTagLn(HtmlUtil.DIV);
  }

  public void documentationAnnotations(final PackageDoc packageDoc) {
    final Map<String, AnnotationTypeDoc> annotations = new TreeMap<String, AnnotationTypeDoc>();
    for (final AnnotationTypeDoc annotationDoc : packageDoc.annotationTypes()) {
      annotations.put(annotationDoc.name(), annotationDoc);
    }
    if (!annotations.isEmpty()) {
      DocletUtil.title(this.writer, "Annotations");
      for (final AnnotationTypeDoc annotationDoc : annotations.values()) {
        documentationAnnotation(annotationDoc);
      }
    }
  }

  public void documentationClass(final ClassDoc classDoc) {
    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "javaClass");
    final String name = classDoc.name();

    final String id = DocletUtil.qualifiedName(classDoc);
    HtmlUtil.elementWithId(this.writer, HtmlUtil.H2, id, name);

    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(this.writer, classDoc, classDoc);

    final ConstructorDoc[] constructors = classDoc.constructors();
    if (constructors.length > 0) {
      DocletUtil.title(this.writer, "Constructors");
      for (final ConstructorDoc method : constructors) {
        documentationMethod(method);
      }
    }

    final MethodDoc[] methods = classDoc.methods();
    if (methods.length > 0) {
      DocletUtil.title(this.writer, "Methods");
      for (final MethodDoc method : methods) {
        documentationMethod(method);
      }
    }
    this.writer.endTagLn(HtmlUtil.DIV);
    this.writer.endTagLn(HtmlUtil.DIV);
  }

  public void documentationClasses(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> classes = new TreeMap<String, ClassDoc>();
    for (final ClassDoc classDoc : packageDoc.ordinaryClasses()) {
      classes.put(classDoc.name(), classDoc);
    }
    if (!classes.isEmpty()) {
      DocletUtil.title(this.writer, "Classes");
      for (final ClassDoc classDoc : classes.values()) {
        documentationClass(classDoc);
      }
    }
  }

  public void documentationEnum(final ClassDoc enumDoc) {
    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "javaClass");
    final String name = enumDoc.name();

    final String id = DocletUtil.qualifiedName(enumDoc);
    HtmlUtil.elementWithId(this.writer, HtmlUtil.H2, id, name);

    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(this.writer, enumDoc, enumDoc);

    final FieldDoc[] elements = enumDoc.enumConstants();
    if (elements.length > 0) {
      DocletUtil.title(this.writer, "Enum Constants");

      this.writer.startTag(HtmlUtil.DIV);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable parameters");
      this.writer.startTag(HtmlUtil.TABLE);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "data");
      this.writer.startTag(HtmlUtil.THEAD);
      this.writer.startTag(HtmlUtil.TR);
      this.writer.element(HtmlUtil.TH, "Constant");
      this.writer.element(HtmlUtil.TH, "Description");
      this.writer.endTagLn(HtmlUtil.TR);
      this.writer.endTagLn(HtmlUtil.THEAD);

      this.writer.startTag(HtmlUtil.TBODY);
      for (final FieldDoc element : elements) {
        this.writer.startTag(HtmlUtil.TR);
        final String elementName = element.name();

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "constant");
        this.writer.text(elementName);
        this.writer.endTagLn(HtmlUtil.TD);

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "description");
        DocletUtil.description(this.writer, null, element);
        this.writer.endTagLn(HtmlUtil.TD);
        this.writer.endTagLn(HtmlUtil.TR);
      }
      this.writer.endTagLn(HtmlUtil.TBODY);

      this.writer.endTagLn(HtmlUtil.TABLE);
      this.writer.endTagLn(HtmlUtil.DIV);

    }
    this.writer.endTagLn(HtmlUtil.DIV);
    this.writer.endTagLn(HtmlUtil.DIV);
  }

  public void documentationEnums(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> enums = new TreeMap<String, ClassDoc>();
    for (final ClassDoc enumDoc : packageDoc.enums()) {
      enums.put(enumDoc.name(), enumDoc);
    }
    if (!enums.isEmpty()) {
      DocletUtil.title(this.writer, "Enums");
      for (final ClassDoc enumDoc : enums.values()) {
        documentationEnum(enumDoc);
      }
    }
  }

  public void documentationInterfaces(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> interfaces = new TreeMap<String, ClassDoc>();
    for (final ClassDoc classDoc : packageDoc.interfaces()) {
      interfaces.put(classDoc.name(), classDoc);
    }
    if (!interfaces.isEmpty()) {
      DocletUtil.title(this.writer, "Interfaces");
      for (final ClassDoc classDoc : interfaces.values()) {
        documentationClass(classDoc);
      }
    }
  }

  public void documentationMethod(final ExecutableMemberDoc member) {
    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "javaMethod");

    this.writer.startTag(HtmlUtil.H3);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "title");
    this.writer.attribute(HtmlUtil.ATTR_ID, getId(member));
    this.writer.attribute(HtmlUtil.ATTR_TITLE, member.name());
    methodSignature(member);
    this.writer.endTagLn(HtmlUtil.H3);

    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(this.writer, member.containingClass(), member);

    parameters(member);

    if (member instanceof MethodDoc) {
      final MethodDoc method = (MethodDoc)member;
      DocletUtil.documentationReturn(this.writer, method);
    }

    this.writer.endTagLn(HtmlUtil.DIV);
    this.writer.endTagLn(HtmlUtil.DIV);
  }

  public void documentationPackage(final PackageDoc packageDoc) {
    final String name = packageDoc.name();
    this.writer.startTag(HtmlUtil.DIV);
    String cssClass = "javaPackage";
    if (this.packagesOpen) {
      cssClass += " open";
    }
    this.writer.attribute(HtmlUtil.ATTR_CLASS, cssClass);

    final String id = name;
    HtmlUtil.elementWithId(this.writer, HtmlUtil.H1, id, name);

    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(this.writer, null, packageDoc);

    documentationAnnotations(packageDoc);
    documentationEnums(packageDoc);
    documentationInterfaces(packageDoc);
    documentationClasses(packageDoc);

    this.writer.endTagLn(HtmlUtil.DIV);
    this.writer.endTagLn(HtmlUtil.DIV);
  }

  private String getAnchor(final ExecutableMemberDoc member) {
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

  private String getId(final ExecutableMemberDoc member) {
    final StringBuffer anchor = new StringBuffer();
    final ClassDoc classDoc = member.containingClass();
    final String className = DocletUtil.qualifiedName(classDoc);
    anchor.append(className);
    anchor.append(".");
    anchor.append(member.name());
    final Parameter[] parameters = member.parameters();
    for (final Parameter parameter : parameters) {
      anchor.append("-");
      final Type type = parameter.type();
      String typeName = type.qualifiedTypeName();
      typeName = typeName.replaceAll("^java.lang.", "");
      typeName = typeName.replaceAll("^java.io.", "");
      typeName = typeName.replaceAll("^java.util.", "");
      anchor.append(typeName);
      anchor.append(type.dimension());
    }
    return anchor.toString();
  }

  public void methodSignature(final ExecutableMemberDoc member) {
    this.writer.startTag(HtmlUtil.A);
    final String anchor = getAnchor(member);
    this.writer.attribute(HtmlUtil.ATTR_NAME, anchor);
    if (member instanceof MethodDoc) {
      this.writer.startTag(HtmlUtil.CODE);
      final MethodDoc method = (MethodDoc)member;
      final Type returnType = method.returnType();
      DocletUtil.typeName(this.writer, returnType);
      this.writer.text(" ");
      this.writer.endTagLn(HtmlUtil.CODE);
    }
    if (member.isStatic()) {
      this.writer.startTag(HtmlUtil.I);
    }
    this.writer.text(member.name());
    if (member.isStatic()) {
      this.writer.endTag(HtmlUtil.I);
    }
    this.writer.startTag(HtmlUtil.CODE);
    this.writer.text("(");
    final Parameter[] parameters = member.parameters();
    boolean first = true;
    for (final Parameter parameter : parameters) {
      if (first) {
        first = false;
      } else {
        this.writer.text(", ");
      }

      DocletUtil.typeName(this.writer, parameter.type());
      this.writer.text(" ");
      this.writer.text(parameter.name());
    }
    this.writer.text(")");
    this.writer.endTagLn(HtmlUtil.CODE);
    this.writer.endTagLn(HtmlUtil.A);
  }

  private void parameters(final ExecutableMemberDoc method) {
    final List<Parameter> parameters = new ArrayList<Parameter>();
    for (final Parameter parameter : method.parameters()) {
      parameters.add(parameter);
    }
    if (!parameters.isEmpty()) {
      final Map<String, Tag[]> descriptions = DocletUtil.getParameterDescriptions(method);

      DocletUtil.title(this.writer, "Parameters");

      this.writer.startTag(HtmlUtil.DIV);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable parameters");
      this.writer.startTag(HtmlUtil.TABLE);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "data");
      this.writer.startTag(HtmlUtil.THEAD);
      this.writer.startTag(HtmlUtil.TR);
      this.writer.element(HtmlUtil.TH, "Parameter");
      this.writer.element(HtmlUtil.TH, "Type");
      this.writer.element(HtmlUtil.TH, "Description");
      this.writer.endTagLn(HtmlUtil.TR);
      this.writer.endTagLn(HtmlUtil.THEAD);

      this.writer.startTag(HtmlUtil.TBODY);
      for (final Parameter parameter : parameters) {
        this.writer.startTag(HtmlUtil.TR);
        final String name = parameter.name();

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "name");
        this.writer.text(parameter.name());
        this.writer.endTagLn(HtmlUtil.TD);

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "type");

        final Type type = parameter.type();
        DocletUtil.typeNameLink(this.writer, type);
        this.writer.endTagLn(HtmlUtil.TD);

        DocletUtil.descriptionTd(this.writer, method.containingClass(), descriptions, name);
        this.writer.endTagLn(HtmlUtil.TR);
      }
      this.writer.endTagLn(HtmlUtil.TBODY);

      this.writer.endTagLn(HtmlUtil.TABLE);
      this.writer.endTagLn(HtmlUtil.DIV);
    }
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
      } else if (optionName.equals("-packagesopen")) {
        this.packagesOpen = Boolean.valueOf(option[1]);
      }
    }
    try {
      final File dir = new File(this.destDir);
      final File indexFile = new File(dir, "index.html");
      final FileWriter out = new FileWriter(indexFile);
      this.writer = new XmlWriter(out, false);
      this.writer.setIndent(false);
      this.writer.setWriteNewLine(false);
      DocletUtil.copyFiles(this.destDir);
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

        DocletUtil.head(this.writer, this.docTitle);
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
