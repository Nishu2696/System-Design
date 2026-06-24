import java.security.Timestamp;
import java.time.LocalDateTime;

public class LogMessage {
    private String message;
    private LogLevel level;
    private LocalDateTime timestamp;

//    All args consturctor
    public LogMessage(String message, LogLevel level) {
        this.message = message;
        this.level = level;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public LogLevel getLevel() {
        return level;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
