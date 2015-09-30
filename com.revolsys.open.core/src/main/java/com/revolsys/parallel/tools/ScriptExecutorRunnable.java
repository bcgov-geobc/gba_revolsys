package com.revolsys.parallel.tools;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyBatchUpdateException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

import com.revolsys.beans.propertyeditor.ResourceEditorRegistrar;
import com.revolsys.collection.map.ThreadSharedAttributes;
import com.revolsys.logging.log4j.ThreadLocalFileAppender;
import com.revolsys.parallel.AbstractRunnable;
import com.revolsys.parallel.process.ProcessNetwork;
import com.revolsys.spring.factory.Parameter;

public class ScriptExecutorRunnable extends AbstractRunnable {
  private static final Logger LOG = LoggerFactory.getLogger(ScriptExecutorRunnable.class);

  private static Throwable getBeanExceptionCause(final BeanCreationException e) {
    Throwable cause = e.getCause();
    while (cause instanceof BeanCreationException || cause instanceof MethodInvocationException
      || cause instanceof PropertyAccessException || cause instanceof PropertyBatchUpdateException
      || cause instanceof InvalidPropertyException) {
      Throwable newCause;
      if (cause instanceof PropertyBatchUpdateException) {
        final PropertyBatchUpdateException batchEx = (PropertyBatchUpdateException)cause;
        newCause = batchEx.getPropertyAccessExceptions()[0];
      } else {
        newCause = cause.getCause();
      }
      if (newCause != null) {
        cause = newCause;
      } else {
        return cause;
      }
    }
    return cause;
  }

  private Map<String, Object> attributes = new LinkedHashMap<>();

  private Map<String, Object> beans = new LinkedHashMap<>();

  private boolean logScriptInfo = true;

  private final String script;

  public ScriptExecutorRunnable(final String script) {
    this.script = script;
  }

  public ScriptExecutorRunnable(final String script, final Map<String, Object> attributes) {
    this.script = script;
    this.attributes = attributes;
  }

  public void addBean(final String name, final Object value) {
    this.beans.put(name, value);
  }

  public void addBeans(final Map<String, ?> beans) {
    this.beans.putAll(beans);
  }

  @Override
  public void doRun() {
    final long startTime = System.currentTimeMillis();
    try {
      String logPath = null;
      final String logFileName = (String)this.attributes.get("logFile");
      if (logFileName != null && logFileName.trim().length() > 0) {
        final File logFile = new File(logFileName);
        final File parentFile = logFile.getParentFile();
        if (parentFile != null) {
          parentFile.mkdirs();
        }
        logPath = logFile.getAbsolutePath();
        ThreadLocalFileAppender.getAppender().setLocalFile(logPath);
      }
      if (this.logScriptInfo) {
        final StringBuffer message = new StringBuffer("Processing ");
        message.append(" -s ");
        message.append(this.script);
        if (logPath != null) {
          message.append(" -l ");
          message.append(logPath);

        }
        for (final Entry<String, Object> parameter : this.attributes.entrySet()) {
          message.append(" ");
          message.append(parameter.getKey());
          message.append("=");
          message.append(parameter.getValue());
        }
        LOG.info(message.toString());
      }
      ThreadSharedAttributes.setAttributes(this.attributes);

      final GenericApplicationContext applicationContext = new GenericApplicationContext();
      applicationContext.getBeanFactory().addPropertyEditorRegistrar(new ResourceEditorRegistrar());

      for (final Entry<String, Object> entry : this.beans.entrySet()) {
        final String key = entry.getKey();
        if (key.indexOf('.') == -1 && key.indexOf('[') == -1) {
          final Object value = entry.getValue();
          final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
          beanDefinition.setBeanClass(Parameter.class);
          final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
          propertyValues.add("type", value.getClass());
          propertyValues.add("value", value);
          applicationContext.registerBeanDefinition(key, beanDefinition);
        }
      }

      final XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(applicationContext);
      if (new File(this.script).exists()) {
        beanReader.loadBeanDefinitions("file:" + this.script);
      } else {
        beanReader.loadBeanDefinitions("classpath:" + this.script);
      }
      applicationContext.refresh();
      try {
        final Object bean = applicationContext.getBean("processNetwork");
        final ProcessNetwork pipeline = (ProcessNetwork)bean;
        pipeline.startAndWait();
      } finally {
        applicationContext.close();
        System.gc();
      }
    } catch (final BeanCreationException e) {
      final Throwable cause = getBeanExceptionCause(e);
      LOG.error(cause.getMessage(), cause);
      System.err.println(cause.getMessage());
      System.err.flush();
    } catch (final Throwable t) {
      LOG.error(t.getMessage(), t);
    }
    if (this.logScriptInfo) {
      final long endTime = System.currentTimeMillis();
      final long time = endTime - startTime;
      long seconds = time / 1000;
      final long minutes = seconds / 60;
      seconds = seconds % 60;
      LOG.info(minutes + " minutes " + seconds + " seconds");
    }
  }

  public Map<String, Object> getBeans() {
    return this.beans;
  }

  public boolean isLogScriptInfo() {
    return this.logScriptInfo;
  }

  public void setBeans(final Map<String, Object> beans) {
    this.beans = beans;
  }

  public void setLogScriptInfo(final boolean logScriptInfo) {
    this.logScriptInfo = logScriptInfo;
  }
}
