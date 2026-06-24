import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    private static Logger instance;
    private LogLevel level;
    private List<Appender> appenders;

    private Logger() {
        this.level = level;
        this.appenders = new ArrayList<>();
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void addAppender(Appender appender) {
        this.appenders.add(appender);
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    private void log(LogLevel level, String message) {
        if (level.ordinal() < this.level.ordinal()) {
            return;
        }

        LogMessage logMessage = new LogMessage(message, level);

        for (Appender appender : this.appenders) {
            appender.append(logMessage);
        }
    }

    public void info(String message) {
        this.log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        this.log(LogLevel.WARN, message);
    }

    public void error(String message) {
        this.log(LogLevel.ERROR, message);
    }

    public void debug(String message) {
        this.log(LogLevel.DEBUG, message);
    }
}
