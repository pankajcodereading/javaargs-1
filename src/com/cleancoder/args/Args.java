package com.cleancoder.args;
//CR Comment : Do not use .* while package import
import java.util.*;

import static com.cleancoder.args.ArgsException.ErrorCode.*;

//CR Comment : Missing class documentation. 
public class Args {
  // CR Comment : Properly specify access modifier for each field/params. 
  // Have a good reason to not make have those as final.
  // CR Comment : Add proper documentation of the fields of the class.
  private Map<Character, ArgumentMarshaler> marshalers;
  private Set<Character> argsFound;
  private ListIterator<String> currentArgument;

  //CR Comment : Constructor documentation along with its parameter.
  //CR Comment : access modifier for the params of every method and constructor.
  public Args(String schema, String[] args) throws ArgsException {
    marshalers = new HashMap<Character, ArgumentMarshaler>();
    argsFound = new HashSet<Character>();

    // CR Comment : It would have been better if this method returned the output (map of marshaller)
    // rather that setting it internally. 
    // This would have made this method side-effect free.
    parseSchema(schema);
    // CR Comment : Any specific value add by doing this.
    // The reason is clear after reading the Marshaller's set method implementation.
    parseArgumentStrings(Arrays.asList(args));
  }

  //CR Comment : Good to add documentation for understanding if possible.
  private void parseSchema(String schema) throws ArgsException {
    // CR Comment : Null check
    for (String element : schema.split(","))
      // CR Comment : Use braces to group the code if there is only 1 line
      if (element.length() > 0)
        // CR comment : Inconsistent error handling. While code is able to handle inputs like x,,y , but not x, ,y and throws exception
        parseSchemaElement(element.trim());
  }

  private void parseSchemaElement(String element) throws ArgsException {
    char elementId = element.charAt(0);
    String elementTail = element.substring(1);
    validateSchemaElementId(elementId);
    // CR Comment : Convert to switch case
    // CR Comment : Put the minimal code in the if block 
    // CR Comment : Use String constants
    if (elementTail.length() == 0)
      marshalers.put(elementId, new BooleanArgumentMarshaler());
    else if (elementTail.equals("*"))
      marshalers.put(elementId, new StringArgumentMarshaler());
    else if (elementTail.equals("#"))
      marshalers.put(elementId, new IntegerArgumentMarshaler());
    else if (elementTail.equals("##"))
      marshalers.put(elementId, new DoubleArgumentMarshaler());
    else if (elementTail.equals("[*]"))
      marshalers.put(elementId, new StringArrayArgumentMarshaler());
    else if (elementTail.equals("&"))
      marshalers.put(elementId, new MapArgumentMarshaler());
    else
      throw new ArgsException(INVALID_ARGUMENT_FORMAT, elementId, elementTail);
  }

  private void validateSchemaElementId(char elementId) throws ArgsException {
    if (!Character.isLetter(elementId))
      throw new ArgsException(INVALID_ARGUMENT_NAME, elementId, null);
  }

  private void parseArgumentStrings(List<String> argsList) throws ArgsException {
    for (currentArgument = argsList.listIterator(); currentArgument.hasNext();) {
      String argString = currentArgument.next();
      if (argString.startsWith("-")) {
        parseArgumentCharacters(argString.substring(1));
      } else {
        currentArgument.previous();
        break;
      }
    }
  }

  private void parseArgumentCharacters(String argChars) throws ArgsException {
    for (int i = 0; i < argChars.length(); i++)
      parseArgumentCharacter(argChars.charAt(i));
  }

  private void parseArgumentCharacter(char argChar) throws ArgsException {
    ArgumentMarshaler m = marshalers.get(argChar);
    if (m == null) {
      throw new ArgsException(UNEXPECTED_ARGUMENT, argChar, null);
    } else {
      // CR Comment : Had this info been coming from the marshaller itself from some of its method,
      // we would not have to set in this Set, which is prone to errors , if we miss to add it to set. 
      // In the other case we could have just extracted the marshaller and checked if it has been set.
      argsFound.add(argChar);
      try {
        // CR Comment : Passing the iterator to another class for modification.
        // Too much tight coupling between classes. 
        // There is an unclear contract between this class and the Marshallers.
        m.set(currentArgument);
      } catch (ArgsException e) {
        e.setErrorArgumentId(argChar);
        throw e;
      }
    }
  }

  public boolean has(char arg) {
    return argsFound.contains(arg);
  }

  public int nextArgument() {
    return currentArgument.nextIndex();
  }

  // CR Comment : Next set of functions call for design refactoring, Against the Open Close priniple.
  public boolean getBoolean(char arg) {
    return BooleanArgumentMarshaler.getValue(marshalers.get(arg));
  }

  public String getString(char arg) {
    return StringArgumentMarshaler.getValue(marshalers.get(arg));
  }

  public int getInt(char arg) {
    return IntegerArgumentMarshaler.getValue(marshalers.get(arg));
  }

  public double getDouble(char arg) {
    return DoubleArgumentMarshaler.getValue(marshalers.get(arg));
  }

  public String[] getStringArray(char arg) {
    return StringArrayArgumentMarshaler.getValue(marshalers.get(arg));
  }

  public Map<String, String> getMap(char arg) {
    return MapArgumentMarshaler.getValue(marshalers.get(arg));
  }
}