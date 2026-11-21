import java.util.HashMap;
import java.util.Map;

public class AccountSessionService {

    private final Map<String, String> userDatabase = new HashMap<>();

    private String currentSessionUser = null;


    private static final int MIN_PASSWORD_LENGTH = 8;

    
