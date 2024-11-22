package assertions;

import assertions.conditions.MessageCondition;
import assertions.conditions.StatusCodeCondition;

public class Conditions {
    public static MessageCondition hasMessage(String expectedMessage) {
        return new MessageCondition(expectedMessage);
    }
    public static StatusCodeCondition hasStatusCode(int expectedStatusCode) {
        return new StatusCodeCondition(expectedStatusCode);
    }
}
