package de.lmu.ifi.dbs.elki.application;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2012
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.util.Collection;

import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.logging.LoggingConfiguration;
import de.lmu.ifi.dbs.elki.logging.LoggingUtil;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.FormatUtil;
import de.lmu.ifi.dbs.elki.utilities.exceptions.AbortException;
import de.lmu.ifi.dbs.elki.utilities.exceptions.UnableToComplyException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.Parameterizable;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.UnspecifiedParameterException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.SerializedParameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.TrackParameters;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.ClassParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.FileParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.Flag;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.Parameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.StringParameter;
import de.lmu.ifi.dbs.elki.utilities.pairs.Pair;

/**
 * AbstractApplication sets the values for flags verbose and help.
 * <p/>
 * Any Wrapper class that makes use of these flags may extend this class. Beware
 * to make correct use of parameter settings via optionHandler as commented with
 * constructor and methods.
 * 
 * @author Elke Achtert
 * @author Erich Schubert
 * 
 * @apiviz.uses LoggingConfiguration
 */
public abstract class AbstractApplication implements Parameterizable {
  /**
   * We need a static logger in this class, for code used in "main" methods.
   */
  protected static Logging STATIC_LOGGER = Logging.getLogger(AbstractApplication.class);

  /**
   * The newline string according to system.
   */
  private static final String NEWLINE = System.getProperty("line.separator");

  /**
   * Information for citation and version.
   */
  public static final String INFORMATION = "ELKI Version 0.5.0~beta1 (2012, April)" + NEWLINE + NEWLINE + "published in:" + NEWLINE + "E. Achtert, S. Goldhofer, H.-P. Kriegel, E. Schubert, A. Zimek:" + NEWLINE + "Evaluation of Clusterings – Metrics and Visual Support." + NEWLINE + "In Proceedings of the 28th"+NEWLINE+"International Conference on Data Engineering (ICDE), Washington, DC, 2012." + NEWLINE;

  /**
   * Parameter that specifies the name of the output file.
   * <p>
   * Key: {@code -app.out}
   * </p>
   */
  public static final OptionID OUTPUT_ID = OptionID.getOrCreateOptionID("app.out", "");

  /**
   * Parameter that specifies the name of the input file.
   * <p>
   * Key: {@code -app.in}
   * </p>
   */
  public static final OptionID INPUT_ID = OptionID.getOrCreateOptionID("app.in", "");

  /**
   * Value of verbose flag.
   */
  private boolean verbose;

  /**
   * Constructor.
   * 
   * @param verbose Verbose flag.
   */
  public AbstractApplication(boolean verbose) {
    if(verbose) {
      // Note: do not unset verbose if not --verbose - someone else might
      // have set it intentionally. So don't setVerbose(verbose)!
      LoggingConfiguration.setVerbose(true);
    }
  }

  /**
   * Returns whether verbose messages should be printed while executing the
   * application.
   * 
   * @return whether verbose messages should be printed while executing the
   *         application
   */
  public final boolean isVerbose() {
    return verbose;
  }

  /**
   * Generic command line invocation.
   * 
   * Refactored to have a central place for outermost exception handling.
   * 
   * @param cls Application class to run.
   * @param args the arguments to run this application with
   */
  public static void runCLIApplication(Class<?> cls, String[] args) {
    final Flag HELP_FLAG = new Flag(OptionID.HELP);
    final Flag HELP_LONG_FLAG = new Flag(OptionID.HELP_LONG);
    final ClassParameter<Object> DESCRIPTION_PARAM = new ClassParameter<Object>(OptionID.DESCRIPTION, Object.class, true);
    final StringParameter DEBUG_PARAM = new StringParameter(OptionID.DEBUG, true);

    SerializedParameterization params = new SerializedParameterization(args);
    try {
      params.grab(HELP_FLAG);
      params.grab(HELP_LONG_FLAG);
      params.grab(DESCRIPTION_PARAM);
      params.grab(DEBUG_PARAM);
      if(DESCRIPTION_PARAM.isDefined()) {
        params.clearErrors();
        printDescription(DESCRIPTION_PARAM.getValue());
        return;
      }
      // Fail silently on errors.
      if(params.getErrors().size() > 0) {
        params.logAndClearReportedErrors();
        return;
      }
      if(DEBUG_PARAM.isDefined()) {
        LoggingUtil.parseDebugParameter(DEBUG_PARAM);
      }
    }
    catch(Exception e) {
      printErrorMessage(e);
      return;
    }
    try {
      TrackParameters config = new TrackParameters(params);
      AbstractApplication task = ClassGenericsUtil.tryInstantiate(AbstractApplication.class, cls, config);

      if((HELP_FLAG.isDefined() && HELP_FLAG.getValue()) || (HELP_LONG_FLAG.isDefined() && HELP_LONG_FLAG.getValue())) {
        LoggingConfiguration.setVerbose(true);
        STATIC_LOGGER.verbose(usage(config.getAllParameters()));
      }
      else {
        params.logUnusedParameters();
        if(params.getErrors().size() > 0) {
          LoggingConfiguration.setVerbose(true);
          STATIC_LOGGER.verbose("The following configuration errors prevented execution:\n");
          for(ParameterException e : params.getErrors()) {
            STATIC_LOGGER.verbose(e.getMessage());
          }
          STATIC_LOGGER.verbose("\n");
          STATIC_LOGGER.verbose("Stopping execution because of configuration errors.");
        }
        else {
          task.run();
        }
      }
    }
    catch(Exception e) {
      printErrorMessage(e);
    }
  }

  /**
   * Returns a usage message, explaining all known options
   * 
   * @param options Options to show in usage.
   * @return a usage message explaining all known options
   */
  public static String usage(Collection<Pair<Object, Parameter<?, ?>>> options) {
    StringBuilder usage = new StringBuilder();
    usage.append(INFORMATION);

    // Collect options
    usage.append(NEWLINE).append("Parameters:").append(NEWLINE);
    OptionUtil.formatForConsole(usage, FormatUtil.getConsoleWidth(), "   ", options);

    // FIXME: re-add constraints!
    return usage.toString();
  }

  /**
   * Print an error message for the given error.
   * 
   * @param e Error Exception.
   */
  protected static void printErrorMessage(Exception e) {
    if(e instanceof AbortException) {
      // ensure we actually show the message:
      LoggingConfiguration.setVerbose(true);
      STATIC_LOGGER.verbose(e.getMessage());
    }
    else if(e instanceof UnspecifiedParameterException) {
      STATIC_LOGGER.error(e.getMessage());
    }
    else if(e instanceof ParameterException) {
      STATIC_LOGGER.error(e.getMessage());
    }
    else {
      STATIC_LOGGER.exception(e);
    }
  }

  /**
   * Print the description for the given parameter
   */
  private static void printDescription(Class<?> descriptionClass) {
    if(descriptionClass != null) {
      LoggingConfiguration.setVerbose(true);
      STATIC_LOGGER.verbose(OptionUtil.describeParameterizable(new StringBuilder(), descriptionClass, FormatUtil.getConsoleWidth(), "    ").toString());
    }
  }

  /**
   * Runs the application.
   * 
   * @throws de.lmu.ifi.dbs.elki.utilities.exceptions.UnableToComplyException if
   *         an error occurs during running the application
   */
  public abstract void run() throws UnableToComplyException;

  /**
   * Parameterization class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   */
  public abstract static class Parameterizer extends AbstractParameterizer {
    /**
     * Verbose flag
     */
    protected boolean verbose = false;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      configVerbose(config);
      // Note: we do not run the other methods by default.
      // Only verbose will always be present!
    }

    /**
     * Get the verbose parameter.
     * 
     * @param config Parameterization
     */
    protected void configVerbose(Parameterization config) {
      final Flag verboseF = new Flag(OptionID.VERBOSE_FLAG);
      if(config.grab(verboseF)) {
        verbose = verboseF.getValue();
      }
    }

    /**
     * Get the output file parameter.
     * 
     * @param config Options
     * @return Output file
     */
    protected File getParameterOutputFile(Parameterization config) {
      return getParameterOutputFile(config, "Output filename.");
    }

    /**
     * Get the output file parameter.
     * 
     * @param config Options
     * @param description Short description
     * @return Output file
     */
    protected File getParameterOutputFile(Parameterization config, String description) {
      final FileParameter outputP = new FileParameter(OUTPUT_ID, FileParameter.FileType.OUTPUT_FILE);
      outputP.setShortDescription(description);
      if(config.grab(outputP)) {
        return outputP.getValue();
      }
      return null;
    }

    /**
     * Get the input file parameter.
     * 
     * @param config Options
     * @return Input file
     */
    protected File getParameterInputFile(Parameterization config) {
      return getParameterInputFile(config, "Input filename.");
    }

    /**
     * Get the input file parameter
     * 
     * @param config Options
     * @param description Description
     * @return Input file
     */
    protected File getParameterInputFile(Parameterization config, String description) {
      final FileParameter inputP = new FileParameter(INPUT_ID, FileParameter.FileType.INPUT_FILE);
      inputP.setShortDescription(description);
      if(config.grab(inputP)) {
        return inputP.getValue();
      }
      return null;
    }

    @Override
    protected abstract AbstractApplication makeInstance();
  }
}