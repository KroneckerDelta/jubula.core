/**
 * 
 */
package org.eclipse.jubula.client.core.functions;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jubula.tools.internal.exception.InvalidDataException;
import org.eclipse.jubula.tools.internal.messagehandling.MessageIDs;

/**
 * @author BREDEX GmbH
 * 
 */
public final class ModifyDateEvaluator extends AbstractFunctionEvaluator {

    /**
     * 
     * {@inheritDoc}
     */
    public String evaluate(String[] arguments) throws InvalidDataException {
        validateParamCount(arguments, 2);
        try {
            long dateTime = Long.parseLong(arguments[0]);
            if (dateTime < 0) {
                throw new InvalidDataException("value to small: " + dateTime, //$NON-NLS-1$
                        MessageIDs.E_TOO_SMALL_VALUE);
            }
            String opString = arguments[1];
            int opStringLength = opString.length();
            if (opStringLength < 2) {
                throw new InvalidDataException("illegal value: " + opString, //$NON-NLS-1$
                        MessageIDs.E_WRONG_PARAMETER_VALUE);
            }
            String op = opString.substring(opStringLength - 1, opStringLength);
            String offsetString = opString.substring(0, opStringLength - 1);
            try {
                int offset = Integer.parseInt(offsetString);
                Date date = new Date(dateTime);
                Date result = null;
                if (op.equalsIgnoreCase("d")) { //$NON-NLS-1$
                    result = DateUtils.addDays(date, offset);
                } else if (op.equalsIgnoreCase("m")) { //$NON-NLS-1$
                    result = DateUtils.addMonths(date, offset);
                } else if (op.equalsIgnoreCase("y")) { //$NON-NLS-1$
                    result = DateUtils.addYears(date, offset);
                } else {
                    throw new InvalidDataException("illegal offset format: " //$NON-NLS-1$
                            + arguments[1], MessageIDs.E_WRONG_PARAMETER_VALUE);
                }
                return String.valueOf(result.getTime());
            } catch (NumberFormatException e) {
                throw new InvalidDataException("illegal offset format: " //$NON-NLS-1$
                        + arguments[1], MessageIDs.E_WRONG_PARAMETER_VALUE);
            }

        } catch (NumberFormatException e) {
            throw new InvalidDataException("not an integer: " + arguments[0], //$NON-NLS-1$
                    MessageIDs.E_BAD_INT);
        }

    }

}
