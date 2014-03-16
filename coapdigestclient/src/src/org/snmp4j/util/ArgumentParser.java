/*_############################################################################
  _##
  _##  SNMP4J - ArgumentParser.java
  _##
  _##  Copyright (C) 2003-2008  Frank Fock and Jochen Katz (SNMP4J.org)
  _##
  _##  Licensed under the Apache License, Version 2.0 (the "License");
  _##  you may not use this file except in compliance with the License.
  _##  You may obtain a copy of the License at
  _##
  _##      http://www.apache.org/licenses/LICENSE-2.0
  _##
  _##  Unless required by applicable law or agreed to in writing, software
  _##  distributed under the License is distributed on an "AS IS" BASIS,
  _##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  _##  See the License for the specific language governing permissions and
  _##  limitations under the License.
  _##
  _##########################################################################*/

package org.snmp4j.util;

import java.text.*;
import java.util.*;
import org.snmp4j.util.ArgumentParser.ArgumentFormat;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * The <code>ArgumentParser</code> parsers a command line array into Java
 * objects and associates each object with the corresponding command line option
 * according to predefined schemes for options and parameters.
 * <p>
 * The format specification for options is:
 * <pre>
 * [-&lt;option&gt;\[&lt;type&gt;[\&lt;&lt;regex&gt;\&gt;]{&lt;parameter&gt;[=&lt;default&gt;]}\]] ...
 * </pre>
 * where
 * <ul>
 * <li>'-' indicates a mandatory option ('+' would indicate an optional
 * option)</li>
 * <li>&lt;option&gt; is the name of the option, for example 'h' for 'help'</li>
 * <li>&lt;type&gt; is one of 'i' (integer), 'l' (long), and 's' (string)</li>
 * </li>&lt;regex&gt; is a regular expression pattern that describes valid
 * values</li>
 * </li>&lt;default&gt; is a default value. If a default value is given, then
 * a mandatory option is in fact optional</li>
 * </ul>
 * </p>
 * <p>
 * The format specification for parameters is:
 * <pre>
 * [-&lt;parameter&gt;[&lt;&lt;type&gt;[&lt;&lt;regex&gt;&gt;]{[=&lt;default&gt;]}]]... [+&lt;optionalParameter&gt;[&lt;type&gt;[&lt;&lt;regex&gt;&gt;]{[=&lt;default&gt;]}]]... [&lt;..&gt;]
 * </pre>
 * where
 * <ul>
 * <li>'-' indicates a mandatory parameter whereas '+' would indicate an
 * optional parameter which must not be followed by a mandatory parameter</li>
 * <li>&lt;parameter&gt; is the name of the parameter, for example 'port'</li>
 * <li>&lt;type&gt; is one of 'i' (integer), 'l' (long), and 's' (string)</li>
 * </li>&lt;regex&gt; is a regular expression pattern that describes valid
 * values</li>
 * <li>&lt;default&gt; is a default value</li>
 * <li>&lt;..&gt; (two consecutive dots after a space at the end of the pattern)
 * indicate that the last parameter may occur more than once
 * </ul>
 * </p>
 *
 * @author Frank Fock
 * @version 1.9
 */
public class ArgumentParser {

  public static final String[] TYPES = { "i", "l", "s" };
  public static final int TYPE_INTEGER = 0;
  public static final int TYPE_LONG = 1;
  public static final int TYPE_STRING = 2;

  private Map optionFormat;
  private Map parameterFormat;

  /**
   * Creates an argument parser with the specified option and parameter formats.
   * @param optionFormat
   *    the option format pattern to parse (see {@link ArgumentParser}).
   * @param parameterFormat
   *    the parameter format pattern to parse (see {@link ArgumentParser}).
   */
  public ArgumentParser(String optionFormat, String parameterFormat) {
    this.optionFormat = parseFormat(optionFormat, false);
    this.parameterFormat = parseFormat(parameterFormat, true);
  }

  public Map getOptionFormat() {
    return optionFormat;
  }

  public Map getParameterFormat() {
    return parameterFormat;
  }

  protected static Map parseFormat(String format, boolean parameterFormat) {
    Map options = new LinkedHashMap();
    ArgumentFormat last = null;
    StringTokenizer st = new StringTokenizer(format, " ");
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if ("..".equals(token)) {
        if (last != null) {
          last.vararg = true;
          break;
        }
        else {
          throw new IllegalArgumentException("'..' without parameter definition");
        }
      }
      ArgumentFormat af = new ArgumentFormat();
      last = af;
      af.parameter = parameterFormat;
      af.mandatory = (token.charAt(0) != '+');
      token = token.substring(1);
      if (token.endsWith("]")) {
        af.option = token.substring(0, token.indexOf('['));
        token = token.substring(af.option.length()+1, token.length()-1);
        StringTokenizer pt = new StringTokenizer(token, ",");
        List params = new ArrayList();
        for (int i=1; (pt.hasMoreTokens()); i++) {
          String param = pt.nextToken();
          ArgumentParameter ap = new ArgumentParameter();
          ap.name = ""+i;
          if (param.endsWith(">")) {
            int regexPos = param.indexOf('<');
            ap.pattern =
                Pattern.compile(param.substring(regexPos+1, param.length()-1));
            token = token.substring(0, regexPos);
          }
          if (param.endsWith("}")) {
            ap.type = getType(param.substring(0, param.indexOf("{")));
            param = param.substring(param.indexOf('{')+1, param.length()-1);
            int posEqual = token.indexOf('=');
            if (posEqual >= 0) {
              ap.defaultValue = param.substring(posEqual+1);
              ap.name = token.substring(0, posEqual);
            }
            else {
              ap.name = param.substring(param.indexOf("{")+1, param.length()-1);
            }
          }
          else {
            ap.type = getType(param);
          }
          params.add(ap);
        }
        af.params = (ArgumentParameter[])
            params.toArray(new ArgumentParameter[params.size()]);
      }
      else {
        af.option = token;
        if (af.parameter) {
          throw new IllegalArgumentException("Parameter "+token+" has no type");
        }
      }
      options.put(af.option, af);
    }
    return options;
  }

  private static int getType(String type) {
    return Arrays.binarySearch(TYPES, type);
  }

  /**
   * Parses the given command line and returns a map of parameter/option names
   * to a <code>List</code> of values. Each value may be of type
   * <code>Integer</code>, <code>Long</code>, and <code>String</code>.
   * @param args
   *    the command line argument list.
   * @return Map
   *    a map that returns options and parameters in the order they have been
   *    parsed, where each map entry has the option/parameter name as key and
   *    the value as value.
   * @throws ParseException
   *    if the command line does not match the patterns for options and
   *    parameters.
   */
  public Map parse(String[] args) throws ParseException {
    Map options = new LinkedHashMap();
    Iterator params = parameterFormat.values().iterator();
    ArgumentFormat lastFormat = null;
    for (int i=0; i<args.length; i++) {
      ArgumentFormat format;
      if (args[i].charAt(0) == '-') {
        String option = args[i].substring(1);
        format = (ArgumentFormat) optionFormat.get(option);
        if (format == null) {
          throw new ParseException("Unknown option "+option, i);
        }
      }
      else {
        format = params.hasNext() ? (ArgumentFormat) params.next() :
            ((lastFormat != null) && (lastFormat.isVariableLength())) ? lastFormat : null;
        if (format == null) {
          throw new ParseException("Unrecognized parameter at position "+i, i);
        }
      }
      if ((format.getParameters() != null) &&
          (format.getParameters().length > 0)) {
        int diff = (format.isParameter()) ? 1 : 0;
        List values = parseValues(args, i+(1-diff), format);
        i += Math.max(values.size() - diff, 0);
        if (format.isVariableLength() &&
            options.containsKey(format.getOption())) {
          List extValues = (List)options.get(format.getOption());
          extValues.addAll(values);
        }
        else {
          addValues2Option(format.getOption(), values, options);
        }
      }
      else {
        addValues2Option(format.getOption(), null, options);
      }
      lastFormat = format;
    }
    while (params.hasNext()) {
      ArgumentFormat af = (ArgumentFormat) params.next();
      if (af.isMandatory()) {
        throw new ArgumentParseException(-1, null, af, af.getParameters()[0]);
      }
    }
    for (Iterator it = optionFormat.values().iterator(); it.hasNext();) {
      ArgumentFormat of = (ArgumentFormat) it.next();
      if (of.isMandatory() && !options.containsKey(of.getOption())) {
        List defaults = new ArrayList();
        for (int i=0; i<of.getParameters().length; i++) {
          if (of.getParameters()[i].getDefaultValue() != null) {
            defaults.add(of.getParameters()[i].getDefaultValue());
          }
        }
        if (defaults.size() == 0) {
          throw new ArgumentParseException( -1, null, of, of.getParameters()[0]);
        }
        else {
          addValues2Option(of.getOption(), defaults, options);
        }
      }
    }
    return options;
  }

  protected void addValues2Option(String option, List values, Map options) {
    List existingValues = (List) options.get(option);
    if ((existingValues != null) && (values != null)) {
      existingValues.addAll(values);
    }
    else {
      options.put(option, values);
    }
  }

  protected List parseValues(String[] args, int offset,
                                 ArgumentFormat format) throws ParseException {
    int numParams = format.getParameters().length;
    List values = new ArrayList(numParams);
    for (int i=0; (i+offset < args.length) && (i < numParams); i++) {
      try {
        values.add(parseParameterValue(format.getParameters()[i],
                                       args[i + offset]));
      }
      catch (Exception ex) {
        ex.printStackTrace();
        int pos = i + offset;
        throw new ArgumentParseException(pos,
                                         args[pos],
                                         format,
                                         format.getParameters()[i]);
      }
    }
    return values;
  }

  protected Object parseParameterValue(ArgumentParameter type,
                                       String value) {
    if (value.startsWith("'") && value.endsWith("'")) {
      value = value.substring(1, value.length()-2);
    }
    if (type.pattern != null) {
      Matcher m = type.pattern.matcher(value);
      if (!m.matches()) {
        throw new RuntimeException("Value '"+value+"' does not match pattern '"+
            type.pattern.pattern());
      }
    }
    switch (type.getType()) {
      case TYPE_INTEGER:
        return new Integer(value);
      case TYPE_LONG:
        return new Long(value);
      default:
        return value;
    }
  }

  public static class ArgumentFormat {
    private String option;
    private boolean mandatory;
    private boolean parameter;
    private ArgumentParameter[] params;
    private boolean vararg;

    public boolean isMandatory() {
      return mandatory;
    }

    public boolean isParameter() {
      return parameter;
    }

    public String getOption() {
      return option;
    }

    public ArgumentParameter[] getParameters() {
      return params;
    }

    public boolean isVariableLength() {
      return vararg;
    }

    public String toString() {
      return "ArgumentFormat[option="+option+",parameter="+parameter+
          ",vararg="+vararg+
          ",mandatatory="+mandatory+",parameters="+Arrays.asList(params)+"]";
    }
  }

  public static class ArgumentParameter {
    private String name;
    private int type;
    private Pattern pattern;
    private String defaultValue;

    public String getName() {
      return name;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    public int getType() {
      return type;
    }

    public String toString() {
      return "ArgumentParameter[name="+name+",type="+type+
          ",patttern="+((pattern == null) ? null : pattern.pattern())+
          ",defaultValue="+defaultValue+"]";
    }
  }

  public static class ArgumentParseException extends ParseException {

    private ArgumentParameter parameterFormatDetail;
    private ArgumentFormat parameterFormat;
    private String value;

    public ArgumentParseException(int position,
                                  String value,
                                  ArgumentFormat parameterFormat,
                                  ArgumentParameter parameterFormatDetail) {
      super((value != null)
            ? "Invalid value '"+value+"' at position "+position
            : "Mandatory parameter "+"-"+parameterFormat.getOption()+
            parameterFormatDetail.getName()+" not specified", position);
      this.parameterFormat = parameterFormat;
      this.parameterFormatDetail = parameterFormatDetail;
      this.value = value;
    }

    public ArgumentParameter getParameterFormatDetail() {
      return parameterFormatDetail;
    }

    public ArgumentFormat getParameterFormat() {
      return parameterFormat;
    }

    public String getValue() {
      return value;
    }
  }

  /**
   * Test application to try out patterns and command line parameters.
   * The default option and parameter patterns can be overridden by setting
   * the system properties <code>org.snmp4j.OptionFormat</code> and
   * <code>org.snmp4j.ParameterFormat</code> respectively.
   * <p>
   * The given command line is parsed using the specified patterns and the
   * parsed values are returned on the console output.
   * </p>
   * <p>
   * The default option pattern is <code>-o1[i{parameter1}] -o2[s,l]</code>
   * and the default parameter pattern is
   * <code>-param1[i] -param2[s<(udp|tcp):.*[/[0-9]+]?>] +optParam1[l{=-100}] ..
   * </code>
   * </p>
   * @param args
   *    the command line arguments to match with the specified format patterns.
   */
  public static void main(String[] args) {
    ArgumentParser argumentparser =
        new ArgumentParser(System.getProperty("org.snmp4j.OptionFormat",
                                              "-o1[i{parameter1}] -o2[s,l]"),
                           System.getProperty("org.snmp4j.ParameterFormat",
                                              "-param1[i] -param2[s<(udp|tcp):.*[/[0-9]+]?>] +optParam1[l{=-100}] .."));
    System.out.println("Option format is: "+argumentparser.getOptionFormat());
    System.out.println("Parameter format is: "+argumentparser.getParameterFormat());
    Map options = null;
    try {
      options = argumentparser.parse(args);
      System.out.println(options);
    }
    catch (ParseException ex) {
      System.err.println("Failed to parse args: "+ex.getMessage());
      ex.printStackTrace();
    }
  }

}
