package de.lmu.ifi.dbs.elki.utilities.optionhandling;

import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.ParameterConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Paramter class for a parameter specifying a list of integer values.
 * 
 * @author Elke Achtert
 */
public class IntListParameter extends ListParameter<Integer> {
  /**
   * Constructs an integer list parameter
   * 
   * @param optionID
   * @param constraint
   * @param optional
   * @param defaultValue
   */
  public IntListParameter(OptionID optionID, ParameterConstraint<List<Integer>> constraint, boolean optional, List<Integer> defaultValue) {
    super(optionID, constraint, optional, defaultValue);
  }

  /**
   * Constructs an integer list parameter
   * 
   * @param optionID
   * @param constraint
   */
  public IntListParameter(OptionID optionID, ParameterConstraint<List<Integer>> constraint) {
    super(optionID, constraint);
  }

  /**
   * Constructs an integer list parameter
   * 
   * @param optionID
   */
  public IntListParameter(OptionID optionID) {
    super(optionID);
  }

  @Override
  public void setValue(String value) throws ParameterException {
    if(isValid(value)) {
      String[] values = SPLIT.split(value);
      Vector<Integer> intValue = new Vector<Integer>();
      for(String val : values) {
        intValue.add(Integer.parseInt(val));
      }
      this.value = intValue;
    }
  }

  @Override
  public boolean isValid(String value) throws ParameterException {
    String[] values = SPLIT.split(value);
    if(values.length == 0) {
      throw new UnspecifiedParameterException("Wrong parameter format! Given list of integer values for parameter \"" + getName() + "\" is either empty or has the wrong format!\nParameter value required:\n" + getDescription());
    }

    // list for checking the parameter constraints
    List<Integer> intList = new ArrayList<Integer>();
    for(String val : values) {
      try {
        intList.add(Integer.parseInt(val));
      }
      catch(NumberFormatException e) {
        throw new WrongParameterValueException("Wrong parameter format for parameter \"" + getName() + "\". Given parameter " + val + " is no double!\n");
      }
    }

    for(ParameterConstraint<List<Integer>> cons : this.constraints) {
      cons.test(intList);
    }

    return true;
  }

  /**
   * Sets the default value of this parameter.
   * 
   * @param allListDefaultValue default value for all list elements of this
   *        parameter
   */
  public void setDefaultValue(int allListDefaultValue) {
    for(int i = 0; i < defaultValue.size(); i++) {
      defaultValue.set(i, allListDefaultValue);
    }
  }

  /**
   * Returns a string representation of the parameter's type.
   * 
   * @return &quot;&lt;int_1,...,int_n&gt;&quot;
   */
  @Override
  protected String getParameterType() {
    return "<int_1,...,int_n>";
  }

}
