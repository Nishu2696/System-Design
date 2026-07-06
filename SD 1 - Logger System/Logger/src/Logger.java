import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

enum LogLevel{
    DEBUG,
    INFO,
    WARN,
    ERROR
}

class LogMessage {
    private String message;
    private LogLevel level;
    private long timestamp;

    public LogMessage(String message, LogLevel level) {
        this.message = message;
        this.level = level;
        this.timestamp = Instant.now().toEpochMilli();
    }

//    getter

    public String getMessage() {
        return message;
    }
    public LogLevel getLevel() {
        return level;
    }
    public long getTimestamp() {
        return timestamp;
    }
}

interface LogAppender {
    void append(LogMessage logMessage);
}

class ConsoleAppender implements LogAppender {
    private String formatter;
//    constructor
    public ConsoleAppender(String formatter) {
        this.formatter = formatter;
    }
    @Override
    public void append(LogMessage logMessage) {
        System.out.println(logMessage.toString());
    }
}

class FileAppender implements LogAppender {
    private String formatter;
    public FileAppender(String formatter) {
        this.formatter = formatter;
    }
    @Override
    public void append(LogMessage logMessage) {
        System.out.println(logMessage.toString());
    }
}

class HandleLogHandler {
    private List<LogAppender> observers = new ArrayList<>();

    public void subscribe(LogAppender observer) {
        observers.add(observer);
    }

    public void notifyObservers(LogMessage logMessage) {
        for (LogAppender observer : observers) {
            if (canHandle(logMessage.getLevel(), logMessage)) {
                observer.append(logMessage);
            }
        }
    }

    private boolean canHandle(LogLevel level, LogMessage logMessage) {
        return true;
    }

    public void handleMsg(LogMessage logMessage) {
        notifyObservers(logMessage);
    }
}

class DebugNotifier {
    public boolean canHandle(LogLevel level, LogMessage logMessage) {
        return level == LogLevel.DEBUG;
    }
}

class InfoNotifier {
    public boolean canHandle(LogLevel level, LogMessage logMessage) {
        return level == LogLevel.INFO;
    }
}

class WarnNotifier {
    public boolean canHandle(LogLevel level, LogMessage logMessage) {
        return level == LogLevel.WARN;
    }
}


class ErrorNotifier {
    public boolean canHandle(LogLevel level, LogMessage logMessage) {
        return level == LogLevel.ERROR;
    }
}

public class Logger {
    private static Logger instance;
    private HandleLogHandler handleLogHandler;

    private Logger() {
        handleLogHandler = new HandleLogHandler();
        handleLogHandler.subscribe(new ConsoleAppender("Console"));
        handleLogHandler.subscribe(new FileAppender("File"));
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void handleMsg(LogMessage logMessage) {
        handleLogHandler.handleMsg(logMessage);
    }

    public void info(String message) {
        LogMessage logMessage = new LogMessage(message, LogLevel.INFO);
        handleMsg(logMessage);
    }

    public void warn(String message) {
        LogMessage logMessage = new LogMessage(message, LogLevel.WARN);
        handleMsg(logMessage);
    }

    public void error(String message) {
        LogMessage logMessage = new LogMessage(message, LogLevel.ERROR);
        handleMsg(logMessage);
    }

    public void debug(String message) {
        LogMessage logMessage = new LogMessage(message, LogLevel.DEBUG);
        handleMsg(logMessage);
    }

    static void main() {
        Logger logger = getInstance();
        logger.debug("this is a debug message");
        logger.info("this is a info message");
        logger.warn("this is a warn message");
        logger.error("this is a error message");
    }
}
