package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

public class Capability {

  private List<Request> requests = new ArrayList<Request>();

  private final List<String> exceptionFormats = new ArrayList<String>();

  private WmsLayer layer;

  public void addExceptionFormat(final String format) {
    this.exceptionFormats.add(format);
  }

  public void addRequest(final Request request) {
    this.requests.add(request);

  }

  public List<String> getExceptionFormats() {
    return this.exceptionFormats;
  }

  public WmsLayer getLayer() {
    return this.layer;
  }

  public List<Request> getRequests() {
    return this.requests;
  }

  public void setLayer(final WmsLayer layer) {
    this.layer = layer;
  }

  public void setRequests(final List<Request> requests) {
    this.requests = requests;
  }
}
