package gift.Test.unit;

import gift.common.util.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordEncoderTest {
    private final static Logger logger = LoggerFactory.getLogger(PasswordEncoderTest.class);
    PasswordEncoder passwordEncoder = new PasswordEncoder("SHA-256");

    @Test
    public void  testingNoException() {
        String password = "4364181018";
        String encodedPassword = passwordEncoder.encode(password);
        logger.info("Encoded Password: {}", encodedPassword);
        boolean matches = passwordEncoder.matches(password, encodedPassword);
        assertTrue(matches);
    }
}
