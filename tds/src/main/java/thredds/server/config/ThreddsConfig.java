/*
 * Copyright 1998-2015 John Caron and University Corporation for Atmospheric Research/Unidata
 *
 *  Portions of this software were developed by the Unidata Program at the
 *  University Corporation for Atmospheric Research.
 *
 *  Access and use of this software shall impose the following obligations
 *  and understandings on the user. The user is granted the right, without
 *  any fee or cost, to use, copy, modify, alter, enhance and distribute
 *  this software, and any derivative works thereof, and its supporting
 *  documentation for any purpose whatsoever, provided that this entire
 *  notice appears in all copies of the software, derivative works and
 *  supporting documentation.  Further, UCAR requests that the user credit
 *  UCAR/Unidata in any publications that result from the use of this
 *  software or in any product that includes this software. The names UCAR
 *  and/or Unidata, however, may not be used in any advertising or publicity
 *  to endorse or promote any products or commercial entity unless specific
 *  written permission is obtained from UCAR/Unidata. The user also
 *  understands that UCAR/Unidata is not obligated to provide the user with
 *  any support, consulting, training or assistance of any kind with regard
 *  to the use, operation and performance of this software nor to provide
 *  the user with any updates, revisions, new versions or "bug fixes."
 *
 *  THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *  FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *  NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *  WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package thredds.server.config;

import thredds.util.ThreddsConfigReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Read and process the threddsConfig.xml file.
 * You can access the values by calling ThreddsConfig.getXXX(name1.name2), where
 * <pre>
 *  <name1>
 *   <name2>value</name2>
 *  </name1>
 * </pre>
 */
public final class ThreddsConfig {
  private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("serverStartup");
  private static ThreddsConfigReader reader;

  public static void init(String filename) {
    reader = new ThreddsConfigReader(filename, log);
  }

  static public String get(String paramName, String defValue) {
    if (reader == null) return defValue;
    return reader.get(paramName, defValue);
  }

  static public boolean hasElement(String paramName) {
    if (reader == null) return false;
    return reader.hasElement(paramName);
  }

  static public boolean getBoolean(String paramName, boolean defValue) {
    if (reader == null) return defValue;
    return reader.getBoolean(paramName, defValue);
  }

  // return null if not set
  static public Boolean getBoolean(String paramName) {
    return reader.getBoolean(paramName);
  }


  static public long getBytes(String paramName, long defValue) {
    if (reader == null) return defValue;
    return reader.getBytes(paramName, defValue);
  }

  static public int getInt(String paramName, int defValue) {
    if (reader == null) return defValue;
    return reader.getInt(paramName, defValue);
  }

  static public long getLong(String paramName, long defValue) {
    if (reader == null) return defValue;
    return reader.getLong(paramName, defValue);
  }

  static public int getSeconds(String paramName, int defValue) {
    if (reader == null) return defValue;
    return reader.getSeconds(paramName, defValue);
  }

  static public List<String> getRootList(String elementName) {
    if (reader == null) return new ArrayList<>(0);
    return reader.getRootList(elementName);
  }

}
